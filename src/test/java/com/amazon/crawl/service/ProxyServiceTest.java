package com.amazon.crawl.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.amazon.crawl.base.BaseTest;

public class ProxyServiceTest extends BaseTest {

	@Resource
	private ProxyService proxyService;

	@Test
	public void test2() {
		proxyService.loadProxyList("C:/Users/zwbma/Desktop/HWGIP/NA2.txt");
	}
}
