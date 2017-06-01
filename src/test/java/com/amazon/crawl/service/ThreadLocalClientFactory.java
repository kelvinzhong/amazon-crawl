package com.amazon.crawl.service;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;

public class ThreadLocalClientFactory {// 单例工厂模式
	private final static ThreadLocalClientFactory instance = new ThreadLocalClientFactory();
	// 线程的本地实例存储器，用于存储WebClient实例
	private ThreadLocal<WebClient> clientThreadLocal;

	/**
	 * 构造方法，初始时线程的本地变量存储器
	 */
	public ThreadLocalClientFactory() {
		clientThreadLocal = new ThreadLocal<WebClient>();
	}

	/**
	 * 获取工厂实例
	 * 
	 * @return 工厂实例
	 */
	public static ThreadLocalClientFactory getInstance() {
		return instance;
	}

	/**
	 * 获取一个模拟FireFox3.6版本的WebClient实例
	 * 
	 * @return 模拟FireFox3.6版本的WebClient实例
	 */
	public WebClient getClient() {
		WebClient client = null;
		/**
		 * 如果当前线程已有WebClient实例，则直接返回该实例 否则重新创建一个WebClient实例并存储于当前线程的本地变量存储器
		 */
		if ((client = clientThreadLocal.get()) == null) {
			client = new WebClient(BrowserVersion.BEST_SUPPORTED);
//			client.setCssEnabled(false);
//			client.setJavaScriptEnabled(false);
			clientThreadLocal.set(client);
			System.out.println("为线程 [ " + Thread.currentThread().getName() + " ] 创建新的WebClient实例!");
		} else {
			System.out.println("线程 [ " + Thread.currentThread().getName() + " ] 已有WebClient实例,直接使用. . .");
		}
		return client;
	}
}