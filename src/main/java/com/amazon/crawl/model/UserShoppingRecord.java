package com.amazon.crawl.model;

import java.util.Date;

public class UserShoppingRecord {

	private String id;
	private String userId;
	private String asin;
	private boolean cart;
	private boolean wishList;
	private int clickTimes;
	private Date cartTime;
	private Date wishListTime;

	public int getClickTimes() {
		return clickTimes;
	}

	public void setClickTimes(int clickTimes) {
		this.clickTimes = clickTimes;
	}

	public Date getCartTime() {
		return cartTime;
	}

	public void setCartTime(Date cartTime) {
		this.cartTime = cartTime;
	}

	public Date getWishListTime() {
		return wishListTime;
	}

	public void setWishListTime(Date wishListTime) {
		this.wishListTime = wishListTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAsin() {
		return asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public boolean isCart() {
		return cart;
	}

	public void setCart(boolean cart) {
		this.cart = cart;
	}

	public boolean isWishList() {
		return wishList;
	}

	public void setWishList(boolean wishList) {
		this.wishList = wishList;
	}

}
