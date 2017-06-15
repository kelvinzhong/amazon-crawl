package com.amazon.crawl.dao.impl;

import javax.annotation.Resource;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Date;
import java.util.Random;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.amazon.crawl.dao.AmazonUserDao;
import com.amazon.crawl.model.AmazonUser;
import com.amazon.crawl.model.UserShoppingRecord;
import com.organization.common.util.general.Constants;

@Repository
public class AmazonUserDaoImpl implements AmazonUserDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public void insertAmazonUser(AmazonUser user) {
		mongoTemplate.insert(user);
	}

	@Override
	public long getAmazonUserCount() {
		return mongoTemplate.count(query(new Criteria()), AmazonUser.class);
	}

	@Override
	public AmazonUser getRandomAmazonUser(int count) {
		return mongoTemplate
				.find(query(where("status").is(Constants.STATUS_NORMAL)).skip(new Random().nextInt(count)).limit(1),
						AmazonUser.class)
				.get(0);
	}

	@Override
	public void updateUserCookies(String id, String cookies) {
		mongoTemplate.updateFirst(query(where("id").is(id)), new Update().set("cookies", cookies), AmazonUser.class);
	}

	@Override
	public void upsertUserClickTimesRecord(String userId, String asin) {
		mongoTemplate.upsert(query(where("userId").is(userId).and("asin").is(asin)), new Update().inc("clickTimes", 1),
				UserShoppingRecord.class);
	}

	@Override
	public UserShoppingRecord updateUserWishListForAsin(String userId, String asin) {
		return mongoTemplate.findAndModify(
				query(where("userId").is(userId).and("asin").is(asin).and("wishList").is(null)),
				new Update().set("wishList", true).set("wishListTime", new Date()), UserShoppingRecord.class);
	}
	
	@Override
	public UserShoppingRecord updateUserCartForAsin(String userId, String asin){
		return mongoTemplate.findAndModify(
				query(where("userId").is(userId).and("asin").is(asin).and("cart").is(null)),
				new Update().set("cart", true).set("cartTime", new Date()), UserShoppingRecord.class);

	}
}
