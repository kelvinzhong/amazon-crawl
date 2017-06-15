package com.amazon.crawl.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazon.crawl.model.message.SKUListRequest;
import com.amazon.crawl.service.ProductService;
import com.organization.common.bean.BaseResult;

@RestController
@RequestMapping("product")
public class ProductController {

	@Resource
	private ProductService productService;

	@RequestMapping("/skuList")
	public BaseResult skuListController(SKUListRequest request) {
		return productService.getSKUListResult(request);
	}
}
