package com.amazon.crawl.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazon.crawl.model.message.SKURankingRequest;
import com.amazon.crawl.service.RankingService;
import com.organization.common.bean.BaseResult;

@RestController
@RequestMapping("ranking")
public class RankingController {

	@Resource
	private RankingService rankingService;

	@RequestMapping("/sku")
	public BaseResult skuRandkingController(SKURankingRequest request) {
		return rankingService.getSKURankingResult(request);
	}
}
