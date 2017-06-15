package com.amazon.crawl.http;

import java.util.Date;

import org.junit.Test;

import com.amazon.crawl.base.HttpTextConn;
import com.amazon.crawl.model.message.SKURankingRequest;

public class HttpRanking {

	@Test
	public void login() throws Exception {
		SKURankingRequest request = new SKURankingRequest();
		request.setAsin("B01N0PA3T7");
		request.setCategory("Electronics");
		request.setKeyword("braided iphone cable");
		request.setPeriod(7);
		request.setEndDay(new Date());

		HttpTextConn.sendMessage(request, "ranking/sku");
	}
}
