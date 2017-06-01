package com.amazon.crawl.dao.impl;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.amazon.crawl.dao.AmazonUserDao;
import com.amazon.crawl.model.AmazonUser;

@Repository
public class AmazonUserDaoImpl implements AmazonUserDao {

	@Resource
	private MongoTemplate mongoTemplate;
	
	@Override
	public void insertAmazonUser(AmazonUser user){
		mongoTemplate.insert(user);
	}
}
