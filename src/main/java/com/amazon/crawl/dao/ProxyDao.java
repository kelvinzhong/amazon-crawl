package com.amazon.crawl.dao;

import java.util.List;
import java.util.Set;

import com.amazon.crawl.model.WebProxy;
import com.gargoylesoftware.htmlunit.util.Cookie;

public interface ProxyDao {

	void insertAllProxy(List<WebProxy> proxyList);

	List<WebProxy> getProxyByHostList(List<String> hostList);

	void updateProxyUserCountById(String id, int count);

	void removeProxyById(String id);

	List<WebProxy> getProxyBelowUserCount(int userCount);

	void updateProxyUserCountByIdList(List<String> idList, int count);

	WebProxy getRandomProxy(long total);

	long getTotalProxyCount();

	void updateProxyCookies(String id, String bs);

}
