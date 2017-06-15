package com.amazon.crawl.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.amazon.crawl.dao.ProductDao;
import com.amazon.crawl.model.TargetSKU;
import com.amazon.crawl.model.message.SKUListRequest;
import com.amazon.crawl.model.result.SKUListResult;
import com.amazon.crawl.service.ProductService;
import com.organization.common.bean.BaseResult;

@Service
public class ProductServiceImpl implements ProductService {

	@Resource
	private ProductDao productDao;

	@Override
	public BaseResult getSKUListResult(SKUListRequest request) {
		SKUListResult result = new SKUListResult();

		List<TargetSKU> skuList = productDao
				.getSkuList(request.getCursor() == null ? new Date() : new Date(Long.parseLong(request.getCursor())));

		if (!CollectionUtils.isEmpty(skuList)) {
			result.setSkuList(skuList);
			result.setCursor(Long.toString(skuList.get(skuList.size() - 1).getCreateTime().getTime()));
		}

		return result;
	}
}
