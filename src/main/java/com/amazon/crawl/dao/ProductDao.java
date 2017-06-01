package com.amazon.crawl.dao;

import java.util.Date;
import java.util.List;

import com.amazon.crawl.model.DailyRanking;
import com.amazon.crawl.model.SKUInfo;
import com.amazon.crawl.model.TargetSKU;

public interface ProductDao {

	List<TargetSKU> getSkuList(Date cursor);

	void insertKeywordSkuInfoList(List<SKUInfo> infoList);

	SKUInfo getSKUInfo(String asin, String keyword, String category, Date date);

	void insertSKUDailyRanking(DailyRanking ranking);

	void insertTargetSKU(TargetSKU tagetSku);

	long getCountBeforeSKUPage(String keyword, String category, int page, Date date);

	long getCountBeforeSKU(String keyword, String category, int page, int column, Date date);

}
