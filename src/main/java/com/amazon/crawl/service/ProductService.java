package com.amazon.crawl.service;

import com.amazon.crawl.model.message.SKUListRequest;
import com.organization.common.bean.BaseResult;

public interface ProductService {

	BaseResult getSKUListResult(SKUListRequest request);

}
