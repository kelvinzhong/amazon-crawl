package com.amazon.crawl.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.junit.Test;

import com.amazon.crawl.HtmlUnitHandler;
import com.amazon.crawl.base.BaseTest;
import com.amazon.crawl.dao.ProxyDao;
import com.amazon.crawl.dao.RegisterDao;
import com.amazon.crawl.model.RegisterTask;
import com.amazon.crawl.model.WebProxy;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlEmailInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
import com.nimbusds.jose.util.Base64;

public class CrawlHandlerTest extends BaseTest {

	@Resource
	private CrawlService crawlService;
	@Resource
	private RegisterDao registerDao;
	@Resource
	private RegisterService registerService;
	@Resource
	private ProxyDao proxyDao;
	@Resource(name = "simulationExecutor")
	private ExecutorService simulationExecutor;

	@Test
	public void test() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		System.out.println(crawlService.getUserName("nv"));
	}

	@Test
	public void test3() {
		// List<WebProxy> webProxy = proxyDao.getProxyBelowUserCount(1);
		// System.out.println(webProxy.size());
		// List<RegisterTask> task = new ArrayList<RegisterTask>();
		// task.add(new RegisterTask(webProxy.get(0)));
		// registerDao.insertRegisterTask(task);

		registerService.processRegisterTask();
	}

	@Test
	public void test4() throws InterruptedException {
//		simulationExecutor.execute(() -> {
//
//			try {
//				crawlService.crawlKeywordAsinList("iphone", "Electronics","172.106.163.10", 7777, "hanyun723046", "hanyun723046");
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println("finish iphone");
//		});
		
		simulationExecutor.execute(() -> {

			try {
				WebProxy proxy = new WebProxy();
				proxy.setHost("");
//				crawlService.crawlKeywordAsinList("iphone cable", "Electronics",proxy);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("finish iphone cable");
		});
		Thread.sleep(1000000000);
	}

	@Test
	public void test5()
			throws FailingHttpStatusCodeException, MalformedURLException, IOException, InterruptedException {

		for (int i = 0; i < 500; i++) {

			WebClient webClient = initilizeCilent();
			try {
				HtmlPage page = webClient.getPage("http://www.amazon.com.cn");

				System.out.println(i);
				if ((i % 5 == 0)) {
					System.out.println(i);
				}
				// Thread.sleep(3000);
			} finally {
				webClient.getCurrentWindow().getJobManager().removeAllJobs();
				webClient.close();
				System.gc();
			}
		}

	}
	
	@Test
	public void test6(){
		int x = (int)(100 * ((0.8 / (10)) + 0.2)
				/ ((0.8 / (9)) + 0.2));
		System.out.println(x);
	}

	public static WebClient initilizeCilent() {
		final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);

		return webClient;
	}

	@Test
	public void test2() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		try (final WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED, "172.106.163.62", 7777)) {

			final DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) webClient
					.getCredentialsProvider();
			credentialsProvider.addCredentials("hanyun83361", "hanyun83361");

			System.out.println("start");
			final HtmlPage page = webClient.getPage("https://www.amazon.com/");
			webClient.waitForBackgroundJavaScript(1000 * 3);
			webClient.setJavaScriptTimeout(0);
			System.out.println(page.getTitleText());

			// System.out.println(page.asText());
			page.getByXPath("//a");
			page.getByXPath("//a[@class='a-link-normal']").forEach(a -> {
				if (((HtmlAnchor) a).getHrefAttribute().contains("www.amazon.com/ap/register")) {
					try {

						webClient.getOptions().setProxyConfig(new ProxyConfig("172.106.183.249", 7777));
						credentialsProvider.clear();
						credentialsProvider.addCredentials("hanyun803136", "hanyun8031361");
						HtmlPage test = ((HtmlAnchor) a).click();
						System.out.println(test.getTitleText());
						final HtmlForm form = test.getFormByName("register");
						final HtmlSubmitInput button = form.getFirstByXPath("//input[@id='continue']");

						final HtmlTextInput name = form.getInputByName("customerName");
						name.setValueAttribute("kelvinzhong1");

						final HtmlEmailInput email = form.getInputByName("email");
						email.setValueAttribute("zwbmaple2@163.com");

						final HtmlPasswordInput password = form.getInputByName("password");
						password.setValueAttribute("zhongwenbo");

						final HtmlPasswordInput passwordCheck = form.getInputByName("passwordCheck");
						passwordCheck.setValueAttribute("zhongwenbo");

						HtmlPage account = button.click();

						System.out.println("register complite");
						final HtmlForm search = account.getFormByName("site-search");

						final HtmlTextInput keyword = search.getInputByName("field-keywords");
						keyword.setValueAttribute("iphone cable");

						final HtmlSubmitInput iphone = search.getInputByValue("Go");

						HtmlPage result = iphone.click();
						System.out.println(result.asText());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

			});

		}
	}

}
