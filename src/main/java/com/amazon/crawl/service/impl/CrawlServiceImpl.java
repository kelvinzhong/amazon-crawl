package com.amazon.crawl.service.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.annotation.Resource;

import org.apache.commons.httpclient.params.DefaultHttpParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.amazon.crawl.HtmlUnitHandler;
import com.amazon.crawl.WebClientFactory;
import com.amazon.crawl.dao.ProductDao;
import com.amazon.crawl.dao.ProxyDao;
import com.amazon.crawl.model.SKUInfo;
import com.amazon.crawl.model.WebProxy;
import com.amazon.crawl.service.CrawlService;
import com.amazon.crawl.service.SimulationService;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.organization.common.config.properties.Configuration;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

@Service
public class CrawlServiceImpl implements CrawlService {
	private static final Logger log = LoggerFactory.getLogger(CrawlServiceImpl.class);

	@Resource
	private ProductDao productDao;
	@Resource
	private ProxyDao proxyDao;
	@Resource
	private SimulationService simulationService;

	@Override
	public void crawlKeywordAsinList(String keyword, String category, WebProxy proxy, WebClientFactory factory)
			throws Exception {
		log.info("Try to crawl asin list in keyword '{}' of category {}, proxy with {}", keyword, category,
				proxy.getHost());

		// DefaultHttpParams.getDefaultParams().setBooleanParameter(HttpMethodParams.SINGLE_COOKIE_HEADER,
		// true);
		WebClient webClient = HtmlUnitHandler.initializeClient(proxy.getHost(), proxy.getPort());
		try {

			factory.set(webClient);

			HtmlUnitHandler.proxyForWebClient(webClient, proxy.getUserName(), proxy.getPassword());

			if (proxy.getCookies() != null) {
				ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decode(proxy.getCookies()));
				ObjectInputStream ois = new ObjectInputStream(bis);
				Set<Cookie> cookies = (Set<Cookie>) ois.readObject();
				cookies.forEach(cookie -> webClient.getCookieManager().addCookie(cookie));
			}

			HtmlPage home = webClient.getPage("https://www.amazon.com/");
			log.info("Open amazon home page");

			HtmlForm search = home.getFormByName("site-search");
			HtmlTextInput searchInput = search.getInputByName("field-keywords");
			searchInput.setValueAttribute(keyword);
			log.info("Search for '{}'", keyword);

			search.getSelectByName("url").getOptionByText(category).click();

			HtmlSubmitInput go = search.getInputByValue("Go");
			HtmlPage result = go.click();

			home.cleanUp();
			search = null;
			searchInput = null;
			go = null;

			int lastPrediction = -1;
			int page = 0;

			while (!result.getByXPath("//a[@title='Next Page']").isEmpty()) {

				HtmlUnorderedList ulList = result.getHtmlElementById("s-results-list-atf");
				Iterable<DomElement> liList = ulList.getChildElements();

				List<SKUInfo> skuInfoList = new ArrayList<SKUInfo>();
				int column = 0;

				for (DomElement li : liList) {
					SKUInfo info = null;
					if (li.asText().contains("Shop by Category")) {
						log.info("Asin {} Shop by Category", li.getAttribute("data-asin"));
						continue;
					}

					try {
						info = getAsinInfo(li, keyword, category, column);
					} catch (Exception e) {
						log.error("error during crawl asin ifno", e);
						continue;
					}

					skuInfoList.add(info);
					column++;
				}

				lastPrediction = simulationService.predictSimulationNum(skuInfoList, lastPrediction);

				if (skuInfoList.size() > 0) {
					log.info("Page {} have total {} SKU", skuInfoList.get(0).getPageNum(), skuInfoList.size());
					if (page == skuInfoList.get(0).getPageNum()) {
						log.warn("crawl in a loop {}", result.asXml());
						page = skuInfoList.get(0).getPageNum();
						break;
					}
					try {
						productDao.insertKeywordSkuInfoList(skuInfoList);
					} catch (Exception e) {
						log.error("", e);
						try {
							log.debug("currunt page sku info json list {}", JSON.toJSONString(skuInfoList));
							Thread.sleep(5000);
						} catch (Exception e2) {
							log.error("try to insert to mongo fail again", e);
						}
					}
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
						nextPage = null;
						break;
					} catch (Exception e) {
						log.error("", e);
						if (!e.toString().contains("Read timed out")) {
							String uri = nextPage.getAttribute("href");
							nextPage.setAttribute("href", uri.substring(0, uri.indexOf("spIA=") - 1));
							result = nextPage.click();
							nextPage = null;
							break;
						}
					}
				}
			}

			if (result.getByXPath("//a[@title='Next Page']").isEmpty()) {
				log.info("keyword {} category {} has no next page", keyword, category);
				log.debug("{}", result.asXml());
			}
		} finally {
			Set<Cookie> cookies = webClient.getCookieManager().getCookies();
			if (!CollectionUtils.isEmpty(cookies))
				try {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(bos);
					oos.writeObject(cookies);
					oos.flush();

					proxyDao.updateProxyCookies(proxy.getId(), Base64.encode(bos.toByteArray()));
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			webClient.close();
			System.gc();
			factory.remove();
		}
	}

	@Override
	public SKUInfo getAsinInfo(DomElement li, String keyword, String category, int columnNum) {
		SKUInfo info = new SKUInfo(
				Integer.parseInt(((HtmlSpan) li.getByXPath("//span[@class='pagnCur']").get(0)).getTextContent()),
				keyword, category, columnNum);

		// log.info("Asin: {}", li.getAttribute("data-asin"));
		info.setAsin(li.getAttribute("data-asin"));
		if (!li.getByXPath("//li[@data-asin='" + li.getAttribute("data-asin")
				+ "']//h5[@class='a-spacing-none a-color-tertiary s-sponsored-list-header a-text-normal']").isEmpty())
			info.setSponsored(true);

		info.setTitle(((HtmlAnchor) li
				.getByXPath("//li[@data-asin='" + li.getAttribute("data-asin")
						+ "']//a[@class='a-link-normal s-access-detail-page  s-color-twister-title-link a-text-normal']")
				.get(0)).getAttribute("title"));

		List<Object> company = li.getByXPath("//li[@data-asin='" + li.getAttribute("data-asin")
				+ "']//span[@class='a-size-small a-color-secondary']");
		if (!CollectionUtils.isEmpty(company) && company.size() > 1)
			info.setCompany(((HtmlSpan) company.get(1)).getTextContent());

		List<Object> price = li.getByXPath(
				"//li[@data-asin='" + li.getAttribute("data-asin") + "']//span[@class='a-color-base sx-zero-spacing']");
		if (!CollectionUtils.isEmpty(price))
			info.setCurrentPrice(((HtmlSpan) price.get(0)).getAttribute("aria-label"));
		else
			info.setCurrentPrice("Currently unavailable");

		if (!li.getByXPath("//li[@data-asin='" + li.getAttribute("data-asin") + "']//i[@aria-label='Prime']").isEmpty())
			info.setPrime(true);

		if (!li.getByXPath(
				"//li[@data-asin='" + li.getAttribute("data-asin") + "']//span[@name='" + info.getAsin() + "']")
				.isEmpty()) {
			List<Object> anchorList = li.getByXPath("//li[@data-asin='" + li.getAttribute("data-asin")
					+ "']//span[@name='" + info.getAsin() + "']/../a");
			info.setReviewNum(((HtmlAnchor) anchorList.get(anchorList.size() - 1)).getTextContent());
		}

		// System.out.println(JSON.toJSONString(info));
		return info;
	}

	@Override
	public List<String> getUserName(String sex) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		BufferedInputStream is = null;
		try {
			// 创建httpget.
			String CH_EN_switch = "/";
			// if (Math.random() < 0.1)
			CH_EN_switch += "en.php";

			HttpPost httpPost = new HttpPost("http://www.qmsjmfb.com" + CH_EN_switch);

			httpPost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

			httpPost.setHeader("Accept-Encoding", "gzip, deflate");

			httpPost.setHeader("Accept-Language", "en-US,en;q=0.8");

			httpPost.setHeader("Cache-Control", "no-cache");

			httpPost.setHeader("Connection", "keep-alive");

			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

			httpPost.setHeader("Cookie", "CNZZDATA5946644=cnzz_eid%3D2007273695-1451282005-%26ntime%3D1451282005");

			httpPost.setHeader("Host", "www.qmsjmfb.com");

			httpPost.setHeader("Origin", "http://www.qmsjmfb.com");

			httpPost.setHeader("Pragma", "no-cache");

			httpPost.setHeader("Referer", "http://www.qmsjmfb.com/");

			httpPost.setHeader("Upgrade-Insecure-Requests", "1");

			httpPost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36");

			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(3000).setConnectTimeout(3000).build();// 设置请求和传输超时时间
			httpPost.setConfig(requestConfig);

			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// 建立一个NameValuePair数组，用于存储欲传送的参数
			params.add(new BasicNameValuePair("num", "50"));
			params.add(new BasicNameValuePair("sex", sex));

			httpPost.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse response = httpclient.execute(httpPost);
			// 获取响应实体
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				is = new BufferedInputStream(entity.getContent());
				StringBuffer out = new StringBuffer();
				byte[] b = new byte[4096];
				for (int n; (n = is.read(b)) != -1;) {
					out.append(new String(b, 0, n));
				}

				// log.debug("out string : {}", out.toString());

				String flag = out.toString().substring(out.toString().indexOf("<li>") + 4,
						out.toString().lastIndexOf("</li>"));

				String[] result = flag.split("</li><li>");

				return Arrays.asList(result);
			}
		} catch (Exception e) {
			log.error("occur error on get userName : {}", e.toString());
		} finally {
			// 关闭连接,释放资源
			try {
				if (is != null) {
					is.close();
				}
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
