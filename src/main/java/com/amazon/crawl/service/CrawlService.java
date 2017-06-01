package com.amazon.crawl.service;

import java.util.List;

import com.amazon.crawl.WebClientFactory;
import com.amazon.crawl.model.SKUInfo;
import com.amazon.crawl.model.WebProxy;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;

public interface CrawlService {

	List<String> getUserName(String sex);

	SKUInfo getAsinInfo(DomElement li, String keyword, String category, int columnNum);

	void crawlKeywordAsinList(String keyword, String category, WebProxy proxy, WebClientFactory factory) throws Exception;

}
