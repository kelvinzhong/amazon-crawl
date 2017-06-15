package com.amazon.crawl.model.message;

import com.organization.common.bean.BaseRequest;

public class SKUListRequest extends BaseRequest {

	private String cursor;

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
}
