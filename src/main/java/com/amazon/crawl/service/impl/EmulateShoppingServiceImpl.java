package com.amazon.crawl.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.amazon.crawl.HtmlUnitHandler;
import com.amazon.crawl.WebClientFactory;
import com.amazon.crawl.dao.AmazonUserDao;
import com.amazon.crawl.dao.ProxyDao;
import com.amazon.crawl.dao.SimulationDao;
import com.amazon.crawl.model.AmazonUser;
import com.amazon.crawl.model.SKUInfo;
import com.amazon.crawl.model.SimulationTask;
import com.amazon.crawl.model.UserShoppingRecord;
import com.amazon.crawl.model.WebProxy;
import com.amazon.crawl.service.CrawlService;
import com.amazon.crawl.service.EmulateShoppingService;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlEmailInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.organization.common.bean.SystemException;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

@Service
public class EmulateShoppingServiceImpl implements EmulateShoppingService {
	private static final Logger emulateShoppingLog = LoggerFactory.getLogger("EmulateShoppingTask");

	@Resource
	private ProxyDao proxyDao;
	@Resource
	private SimulationDao simulationDao;
	@Resource
	private AmazonUserDao amazonUserDao;
	@Resource
	private CrawlService crawlService;

	@Override
	public List<String> simulateClickPage(WebClientFactory factory, SimulationTask task, AmazonUser user)
			throws IOException, ClassNotFoundException {
		List<String> taskAsinList = new ArrayList<String>();
		setExtraTask(taskAsinList, task);

		WebProxy proxy = proxyDao.getProxyByHost(user.getProxyHost());
		WebClient webClient = HtmlUnitHandler.initializeClient(proxy.getHost(), proxy.getPort());

		emulateShoppingLog.info("start emulate shopping with user {} proxy {} asin {} category {} keyword {}",
				user.getNickname(), proxy.getHost(), taskAsinList, task.getCategory(), task.getKeyword());
		try {
			factory.set(webClient);

			HtmlUnitHandler.proxyForWebClient(webClient, proxy.getUserName(), proxy.getPassword());

			if (user.getCookies() != null) {
				ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(user.getCookies()));
				ObjectInputStream ois = new ObjectInputStream(bis);
				Set<Cookie> cookies = (Set<Cookie>) ois.readObject();
				cookies.forEach(cookie -> webClient.getCookieManager().addCookie(cookie));
			}

			HtmlPage home = webClient.getPage("https://www.amazon.com/");
			emulateShoppingLog.info("Open amazon home page");

			if (!home.asText().contains("Sign Out")) {

				List<Object> list = home.getByXPath("//a[@data-nav-role='signin']");
				HtmlPage signIn = ((HtmlAnchor) list.get(0)).click();
				emulateShoppingLog.info("click into sign in page {}", signIn.asXml());

				HtmlForm signInForm = signIn.getFormByName("signIn");

				HtmlEmailInput accountInput = signInForm.getInputByName("email");
				accountInput.setValueAttribute(user.getEmailAcount());

				HtmlPasswordInput passwordInput = signInForm.getInputByName("password");
				passwordInput.setValueAttribute(user.getPassword());

				HtmlSubmitInput submit = signInForm.getFirstByXPath("//input[@id='signInSubmit']");

				home = submit.click();

				emulateShoppingLog.debug("{}", home.asText());

			}

			while (true) {
				try {
					startSearch(webClient, home, taskAsinList, task, user);
					break;
				} catch (Exception e) {
					if (!e.toString().contains("Read timed out"))
						break;
				}
			}

			// data-nav-role
		} finally {
			Set<Cookie> cookies = webClient.getCookieManager().getCookies();
			if (!CollectionUtils.isEmpty(cookies))
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(bos);
					oos.writeObject(cookies);
					oos.flush();

					amazonUserDao.updateUserCookies(user.getId(), Base64.encode(bos.toByteArray()));
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			webClient.close();
			System.gc();
			factory.remove();
		}

		return taskAsinList;
	}

	public void startSearch(WebClient webClient, HtmlPage home, List<String> taskAsinList, SimulationTask task,
			AmazonUser user) throws ElementNotFoundException, IOException, URISyntaxException {
		List<String> doneAsinList = new ArrayList<String>();

		HtmlForm search = home.getFormByName("site-search");
		HtmlTextInput searchInput = search.getInputByName("field-keywords");
		searchInput.setValueAttribute(task.getKeyword());

		search.getSelectByName("url").getOptionByText(task.getCategory()).click();

		HtmlSubmitInput go = search.getInputByValue("Go");
		HtmlPage result = go.click();

		home.cleanUp();
		search = null;
		searchInput = null;
		go = null;

		int page = 0;

		while (!result.getByXPath("//a[@title='Next Page']").isEmpty()) {

			String url = result.getUrl().toURI().toString();
			HtmlUnorderedList ulList = result.getHtmlElementById("s-results-list-atf");
			Iterable<DomElement> liList = ulList.getChildElements();

			List<SKUInfo> skuInfoList = new ArrayList<SKUInfo>();
			int column = 0;

			for (DomElement li : liList) {
				SKUInfo info = null;
				if (li.asText().contains("Shop by Category")) {
					emulateShoppingLog.info("Asin {} Shop by Category", li.getAttribute("data-asin"));
					continue;
				}

				try {
					info = crawlService.getAsinInfo(li, task.getKeyword(), task.getCategory(), column);
				} catch (Exception e) {
					emulateShoppingLog.error("error during crawl asin ifno", e);
					continue;
				}

				if (info.isSponsored() == false && taskAsinList.contains(info.getAsin())
						&& !doneAsinList.contains(info.getAsin())) {
					clickIntoAsinPage(webClient, li, user); // click into asin

					doneAsinList.add(info.getAsin());
				}
				skuInfoList.add(info);
				column++;
			}

			if (taskAsinList.size() == doneAsinList.size()) {
				emulateShoppingLog.info("Complete for all asin click in currunt task, asin {} category {} keyword {}",
						taskAsinList, task.getCategory(), task.getKeyword());
				return;
			}

			if (skuInfoList.size() > 0) {
				emulateShoppingLog.info("Page {} have total {} SKU", skuInfoList.get(0).getPageNum(),
						skuInfoList.size());
				if (page == skuInfoList.get(0).getPageNum()) {
					emulateShoppingLog.warn("crawl in a loop {}", result.asXml());
					result = webClient.getPage(url.replace("page=" + page, "page=" + (page + 1)));
					continue;
				} else
					page = skuInfoList.get(0).getPageNum();

			}

			while (true) {
				DomElement nextPage = result.getElementById("pagnNextLink");
				try {
					ulList = null;
					liList = null;

					for (WebWindow w : webClient.getWebWindows())
						w.getJobManager().removeAllJobs();
					result.cleanUp();
					webClient.getCache().clear();
					System.gc();

					result = nextPage.click();
					if (StringUtils.isEmpty(result.asText())) {
						emulateShoppingLog.debug("empty next page {}", url);
						if (!url.contains("page="))
							throw new SystemException("Restart Task");

						while (true) {
							try {
								result = webClient.getPage(url.replace("page=" + page, "page=" + (page + 1)));
								if (!StringUtils.isEmpty(result.asText()))
									break;
							} catch (Exception e) {
								emulateShoppingLog.error("", e);
								if (!e.toString().contains("Read timed out"))
									return;
							}
						}
						emulateShoppingLog.debug("{}", result.asText());
					}
					nextPage = null;
					break;
				} catch (Exception e) {
					emulateShoppingLog.error("", e);
					if (e.toString().contains("Restart Task"))
						throw new SystemException("Restart Task");

					if (!e.toString().contains("Read timed out")) {

						String uri = nextPage.getAttribute("href");
						nextPage.setAttribute("href", uri.substring(0, uri.indexOf("spIA=") - 1));
						while (true) {
							try {
								result = nextPage.click();

								if (StringUtils.isEmpty(result.asText())) {
									if (!uri.contains("page="))
										throw new SystemException("Restart Task");

									while (true) {
										try {
											result = webClient
													.getPage(url.replace("page=" + page, "page=" + (page + 1)));
											if (!StringUtils.isEmpty(result.asText()))
												break;
										} catch (Exception e2) {
											emulateShoppingLog.error("", e2);
											if (!e.toString().contains("Read timed out"))
												return;
										}
									}
								} else
									break;
							} catch (Exception e2) {
								emulateShoppingLog.error("", e);
								if (!e2.toString().contains("Read timed out"))
									return;
							}
						}
						nextPage = null;
						break;
					}
				}
			}
		}

		if (result.getByXPath("//a[@title='Next Page']").isEmpty()) {
			emulateShoppingLog.info("keyword {} category {} has no next page", task.getKeyword(), task.getCategory());
		}
	}

	public void clickIntoAsinPage(WebClient webClient, DomElement li, AmazonUser user) throws IOException {
		HtmlPage asinPage = ((HtmlAnchor) li
				.getByXPath("//li[@data-asin='" + li.getAttribute("data-asin")
						+ "']//a[@class='a-link-normal s-access-detail-page  s-color-twister-title-link a-text-normal']")
				.get(0)).click();

		emulateShoppingLog.info("Found asin {} and click into asin page", li.getAttribute("data-asin"));

		amazonUserDao.upsertUserClickTimesRecord(user.getId(), li.getAttribute("data-asin"));

		if (!addAsinWishList(webClient, asinPage, user, li.getAttribute("data-asin")))
			addAsinCart(asinPage, user, li.getAttribute("data-asin"));
	}

	public boolean addAsinWishList(WebClient webClient, HtmlPage asinPage, AmazonUser user, String asin)
			throws IOException {
		if (Math.random() < 0.2) {
			emulateShoppingLog.info("try to add asin into wish list");

			UserShoppingRecord record = amazonUserDao.updateUserWishListForAsin(user.getId(), asin);

			if (record == null) {
				emulateShoppingLog.info("user {} already add asin into wish list", user.getId());
				return false;
			}

			for (HtmlForm form : asinPage.getForms())
				if (form.getId().equals("addToCart")) {

					HtmlSubmitInput submit = (HtmlSubmitInput) asinPage.getElementById("add-to-wishlist-button-submit");
					HtmlPage wishList = submit.click();

					if (wishList.asText().contains("1 item added to")) {
						emulateShoppingLog.info("add asin {} into wish list susseccfully", asin);
						return true;
					}

					if (!CollectionUtils.isEmpty(wishList.getByXPath("//input[@alt='Create a List']"))) {
						emulateShoppingLog.info("create wish list");
						HtmlPage createWishList = (HtmlPage) ((HtmlImageInput) wishList
								.getByXPath("//input[@alt='Create a List']").get(0)).click();

						if (createWishList.asText().contains("1 item added to")) {
							emulateShoppingLog.info("add asin {} into wish list susseccfully", asin);
							return true;
						}

						emulateShoppingLog.info("add asin {} fail {}", asin, createWishList.asXml());
					} else if (wishList.getElementById("WLNEW_newwl_section") != null) {
						emulateShoppingLog.info("select wish list");
						wishList.getElementById("WLNEW_newwl_section").setAttribute("class",
								"a-section a-spacing-base WLNEW_selected");

						HtmlPage addToList = (HtmlPage) ((HtmlAnchor) wishList.getElementById("WLNEW_valid_submit"))
								.click();
						if (addToList.asText().contains("1 item added to")) {
							emulateShoppingLog.info("add asin {} into wish list susseccfully", asin);
							return true;
						}

						emulateShoppingLog.info("add asin {} fail {}", asin, addToList.asXml());
					}

					break;
				}
		}

		return false;
	}

	public boolean addAsinCart(HtmlPage asinPage, AmazonUser user, String asin) throws IOException {
		if (Math.random() < 0.2) {
			emulateShoppingLog.info("try to add asin {} into cart", asin);

			UserShoppingRecord record = amazonUserDao.updateUserCartForAsin(user.getId(), asin);

			if (record == null) {
				emulateShoppingLog.info("user {} already add asin {} into cart", user.getId(), asin);
				return false;
			}

			HtmlSubmitInput submit = (HtmlSubmitInput) asinPage.getElementById("add-to-cart-button");
			HtmlPage cart = submit.click();

			if (cart.asText().contains("Added to Cart")) {
				emulateShoppingLog.info("add to cart successfully");
				return true;
			}

			emulateShoppingLog.debug("add asin {} cart fail {}", asin, cart.asXml());
		}

		return false;
	}

	@Override
	public void setExtraTask(List<String> taskAsinList, SimulationTask task) {

		taskAsinList.add(task.getAsin());

		while (true) {
			if (Math.random() < 0.5) {
				SimulationTask extra = simulationDao.getExtraSimulationTask(taskAsinList, task.getCategory(),
						task.getKeyword());

				if (extra == null) {
					emulateShoppingLog.info("no more task for category {} keyword {}", task.getCategory(),
							task.getKeyword());
					break;
				}

				taskAsinList.add(extra.getAsin());
			} else
				break;
		}
	}

	private HtmlPage getPopupPage(LinkedList<WebWindow> windows) {
		WebWindow latestWindow = windows.getLast();
		return (HtmlPage) latestWindow.getEnclosedPage();
	}
}
