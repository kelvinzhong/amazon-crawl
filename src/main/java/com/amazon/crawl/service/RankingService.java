package com.amazon.crawl.service;

import com.amazon.crawl.model.message.SKURankingRequest;
import com.organization.common.bean.BaseResult;

public interface RankingService {

	BaseResult getSKURankingResult(SKURankingRequest request);

}
