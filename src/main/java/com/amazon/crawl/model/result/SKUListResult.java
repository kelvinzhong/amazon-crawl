package com.amazon.crawl.model.result;

import java.util.List;

import com.amazon.crawl.model.TargetSKU;
import com.organization.common.bean.BaseResult;

public class SKUListResult extends BaseResult {

	private List<TargetSKU> skuList;
	private String cursor;
	
	public SKUListResult(){
		this.cursor = "-1";
	}

	public List<TargetSKU> getSkuList() {
		return skuList;
	}

	public void setSkuList(List<TargetSKU> skuList) {
		this.skuList = skuList;
	}

	public String getCursor() {
		return cursor;
	}

	public void setCursor(String cursor) {
		this.cursor = cursor;
	}
}
