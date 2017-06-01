package com.amazon.crawl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.crawl.service.RegisterService;
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
import com.organization.common.config.properties.Configuration;

public class HtmlUnitHandler {

	private static final Logger log = LoggerFactory.getLogger(HtmlUnitHandler.class);

	@Resource(name = "taskExecutor")
	private ExecutorService taskExecutor;
	@Resource(name = "simulationExecutor")
	private ExecutorService simulationExecutor;
	@Resource
	private RegisterService registerService;

	private static boolean RUN_TASK = true;

	public void init() throws Exception {

		if (Configuration.getProperty("register.task.generate", false)) {
			startGenerateRegisterTask();
		} else
			log.info("Generate Register Task Off!");

		if (Configuration.getProperty("register.amazon.user", false)) {
			startRegisterAmazonUser();
		} else
			log.info("Register Amazon User Off!");

		startSimulation();

	}

	public void startGenerateRegisterTask() {
		taskExecutor.execute(() -> {
			boolean emtpyTask = false;
			while (RUN_TASK) {
				if (emtpyTask)
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
						log.error("interrupt during sleep", e);
					}

				emtpyTask = registerService.generateRegisterTask();
			}
		});
	}

	public void startRegisterAmazonUser() {
		taskExecutor.execute(() -> {
			boolean emtpyTask = false;
			while (RUN_TASK) {
				if (emtpyTask)
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
						log.error("interrupt during sleep", e);
					}

				emtpyTask = registerService.processRegisterTask();
			}
		});
	}

	public void startSimulation() {

		simulationExecutor.execute(() -> {

		});
	}
	
	public void shutdown(){
		simulationExecutor.shutdown();
		taskExecutor.shutdown();
		
	}

	public static WebClient initializeClient(String host, int port) {
		WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED, host, port);
//		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
//		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setDownloadImages(false);
		webClient.getOptions().setDoNotTrackEnabled(true);
		
		webClient.getCookieManager().setCookiesEnabled(true);
		webClient.getOptions().setTimeout(10000);
		webClient.waitForBackgroundJavaScript(1000 * 2);
		
		return webClient;
	}

	public static void proxyForWebClient(WebClient webClient, String userName, String password) {
		DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) webClient
				.getCredentialsProvider();
		credentialsProvider.addCredentials(userName, password);
	}
}
