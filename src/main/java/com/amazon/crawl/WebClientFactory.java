package com.amazon.crawl;

import com.gargoylesoftware.htmlunit.WebClient;

public class WebClientFactory {
	
	private final static WebClientFactory instance = new WebClientFactory();
	private ThreadLocal<WebClient> clientThreadLocal;
	
	public WebClientFactory() {
		clientThreadLocal = new ThreadLocal<WebClient>();
	}
	
	public void set(WebClient webClient){
		clientThreadLocal.set(webClient);
	}
	
	public void remove(){
		clientThreadLocal.remove();
	}
}
