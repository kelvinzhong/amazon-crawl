package com.amazon.crawl.dao.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.amazon.crawl.dao.ProductDao;
import com.amazon.crawl.model.DailyRanking;
import com.amazon.crawl.model.SKUInfo;
import com.amazon.crawl.model.TargetSKU;
import com.organization.common.config.properties.Configuration;
import com.organization.common.util.general.Constants;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
public class ProduectDaoImpl implements ProductDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public List<TargetSKU> getSkuList(Date cursor) {
		return mongoTemplate.find(query(where("createTime").lt(cursor).and("status").is(Constants.STATUS_NORMAL))
				.with(new Sort(Sort.Direction.DESC, "createTime"))
				.limit(Configuration.getValue(Constants.LIST_SIZE, 30)), TargetSKU.class);
	}

	@Override
	public void insertKeywordSkuInfoList(List<SKUInfo> infoList) {
		mongoTemplate.insertAll(infoList);
	}

	@Override
	public SKUInfo getSKUInfo(String asin, String keyword, String category, Date date) {
		return mongoTemplate.findOne(query(where("asin").is(asin).and("keyword").is(keyword).and("category")
				.is(category).and("createTime").is(date).and("sponsored").is(false)), SKUInfo.class);
	}

	@Override
	public long getCountBeforeSKUPage(String keyword, String category, int page, Date date) {
		return mongoTemplate.count(query(where("keyword").is(keyword).and("category").is(category).and("createTime")
				.is(date).and("pageNum").lt(page).and("sponsored").is(false)), SKUInfo.class);
	}

	@Override
	public long getCountBeforeSKU(String keyword, String category, int page, int column, Date date) {
		return mongoTemplate.count(query(where("keyword").is(keyword).and("category").is(category).and("createTime")
				.is(date).and("pageNum").is(page).and("column").lt(column).and("sponsored").is(false)), SKUInfo.class);
	}

	@Override
	public void insertSKUDailyRanking(DailyRanking ranking) {
		mongoTemplate.insert(ranking);
	}

	@Override
	public void insertTargetSKU(TargetSKU tagetSku) {
		mongoTemplate.insert(tagetSku);
	}
}
