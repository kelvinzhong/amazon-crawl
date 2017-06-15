package com.amazon.crawl.dao;

import com.amazon.crawl.model.AmazonUser;
import com.amazon.crawl.model.UserShoppingRecord;

public interface AmazonUserDao {

	void insertAmazonUser(AmazonUser user);

	AmazonUser getRandomAmazonUser(int count);

	long getAmazonUserCount();

	void updateUserCookies(String id, String cookies);

	void upsertUserClickTimesRecord(String userId, String asin);

	UserShoppingRecord updateUserWishListForAsin(String userId, String asin);

	UserShoppingRecord updateUserCartForAsin(String userId, String asin);

}
