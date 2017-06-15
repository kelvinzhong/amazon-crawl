package com.amazon.crawl.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.amazon.crawl.dao.ProductDao;
import com.amazon.crawl.model.DailyRanking;
import com.amazon.crawl.model.message.SKURankingRequest;
import com.amazon.crawl.model.result.SKURankingResult;
import com.amazon.crawl.service.RankingService;
import com.organization.common.bean.BaseResult;

@Service
public class RankingServiceImpl implements RankingService {

	@Resource
	private ProductDao productDao;

	@Override
	public BaseResult getSKURankingResult(SKURankingRequest request) {
		SKURankingResult result = new SKURankingResult();

		List<DailyRanking> rankingList = productDao.getSKURankingList(request.getAsin(), request.getCategory(),
				request.getKeyword(),
				new Date(request.getEndDay().getTime() - request.getPeriod() * 24 * 60 * 60 * 1000),
				request.getEndDay());

		result.setRankingList(rankingList);

		return result;
	}
}
