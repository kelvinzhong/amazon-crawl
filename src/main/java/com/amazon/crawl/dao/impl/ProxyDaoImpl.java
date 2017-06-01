package com.amazon.crawl.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.amazon.crawl.dao.ProxyDao;
import com.amazon.crawl.model.WebProxy;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.organization.common.config.properties.Configuration;
import com.organization.common.util.general.Constants;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class ProxyDaoImpl implements ProxyDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public void insertAllProxy(List<WebProxy> proxyList) {
		mongoTemplate.insertAll(proxyList);
	}

	@Override
	public List<WebProxy> getProxyByHostList(List<String> hostList) {
		return mongoTemplate.find(query(where("host").in(hostList)), WebProxy.class);
	}

	@Override
	public void updateProxyUserCountById(String id, int count) {
		mongoTemplate.updateFirst(query(where("id").is(id)),
				new Update().inc("userCount", count).set("updateTime", new Date()), WebProxy.class);
	}

	@Override
	public void updateProxyUserCountByIdList(List<String> idList, int count) {
		mongoTemplate.updateMulti(query(where("id").in(idList)),
				new Update().set("userCount", count).set("updateTime", new Date()), WebProxy.class);
	}

	@Override
	public void removeProxyById(String id) {
		mongoTemplate.updateFirst(query(where("id").is(id).and("status").is(Constants.STATUS_NORMAL)),
				new Update().set("status", Constants.STATUS_DELETE), WebProxy.class);
	}

	@Override
	public List<WebProxy> getProxyBelowUserCount(int userCount) {
		return mongoTemplate.find(query(where("userCount").lt(userCount).and("status").is(Constants.STATUS_NORMAL))
				.limit(Configuration.getValue(Constants.LIST_SIZE, 30)), WebProxy.class);
	}

	@Override
	public WebProxy getRandomProxy(long total) {
		return mongoTemplate
				.find(query(new Criteria()).skip(new Random().nextInt((int) total - 1)).limit(1), WebProxy.class)
				.get(0);
	}

	@Override
	public long getTotalProxyCount() {
		return mongoTemplate.count(query(new Criteria()), WebProxy.class);
	}

	@Override
	public void updateProxyCookies(String id, String cookies) {
		mongoTemplate.updateFirst(query(where("id").is(id)), new Update().set("cookies", cookies), WebProxy.class);
	}
}
