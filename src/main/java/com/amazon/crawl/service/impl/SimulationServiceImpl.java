package com.amazon.crawl.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.amazon.common.util.general.DateUtils;
import com.amazon.crawl.WebClientFactory;
import com.amazon.crawl.dao.AmazonUserDao;
import com.amazon.crawl.dao.ProductDao;
import com.amazon.crawl.dao.ProxyDao;
import com.amazon.crawl.dao.SimulationDao;
import com.amazon.crawl.model.AmazonUser;
import com.amazon.crawl.model.DailyRanking;
import com.amazon.crawl.model.SKUInfo;
import com.amazon.crawl.model.SimulationTask;
import com.amazon.crawl.model.TargetSKU;
import com.amazon.crawl.model.WebProxy;
import com.amazon.crawl.service.CrawlService;
import com.amazon.crawl.service.EmulateShoppingService;
import com.amazon.crawl.service.SimulationService;
import com.organization.common.config.properties.Configuration;
import com.organization.common.util.general.Constants;

@Service("simulationService")
public class SimulationServiceImpl implements SimulationService {
	private static final Logger log = LoggerFactory.getLogger(SimulationServiceImpl.class);
	private static final Logger simulationLog = LoggerFactory.getLogger("SimulationTask");

	public static Date taskDate = null;
	public static Map<String, Set<String>> keywordMap = new HashMap<String, Set<String>>();
	public static Map<String, Set<String>> doneMap = new HashMap<String, Set<String>>();
	public static Map<String, List<TargetSKU>> waitingMap = new HashMap<String, List<TargetSKU>>();
	private static List<Future<?>> futureList = new ArrayList<Future<?>>();

	@Resource
	private ProductDao productDao;
	@Resource
	private CrawlService crawlService;
	@Resource
	private SimulationDao simulationDao;
	@Resource
	private AmazonUserDao amazonUserDao;
	@Resource
	private ProxyDao proxyDao;
	@Resource(name = "rankingExecutor")
	private ExecutorService rankingExecutor;
	@Resource(name = "simulationExecutor")
	private ExecutorService simulationExecutor;
	@Resource
	private EmulateShoppingService emulateShoppingService;

	@Override
	public boolean executeSimulationTask() {
		SimulationTask task = simulationDao.getSimulationTask();

		if (task == null)
			return true;

		Future<?> future = simulationExecutor.submit(() -> {
			WebClientFactory factory = new WebClientFactory();

			while (true) {
				try {
					long count = amazonUserDao.getAmazonUserCount();
					if (count == 0) {
						simulationDao.updateSimulationTaskStatus(task.getId(), Constants.STATUS_NORMAL);
						return;
					}

					AmazonUser user = amazonUserDao.getRandomAmazonUser(new Long(count).intValue());

					emulateShoppingService.simulateClickPage(factory, task, user);
					futureList.remove(0);
					break;
				} catch (Exception e) {
					simulationLog.error("", e);
				}
			}
		});
		
		futureList.add(future);
		
		if(futureList.size() > 5)
			return true;

		return false;
	}

	@Override
	public void startGenerateSimulationTask() {

		simulationLog.info("Start generate simulation task");
		Date cursor = new Date();
		long totalProxy = proxyDao.getTotalProxyCount();
		List<TargetSKU> skuList = productDao.getSkuList(cursor);

		while (!CollectionUtils.isEmpty(skuList)) {

			for (TargetSKU sku : skuList)
				for (String category : sku.getCategoryList())
					for (String keyword : sku.getKeywordList())
						if (CollectionUtils.isEmpty(keywordMap.get(category))
								|| !keywordMap.get(category).contains(keyword)) {

							Set<String> keywordSet = keywordMap.get(category);
							if (CollectionUtils.isEmpty(keywordSet))
								keywordSet = new HashSet<String>();
							keywordSet.add(keyword);
							keywordMap.put(category, keywordSet);

							rankingExecutor.submit(() -> {
								WebClientFactory factory = new WebClientFactory();

								while (true) {
									try {
										WebProxy proxy = proxyDao.getRandomProxy(totalProxy);

										crawlService.crawlKeywordAsinList(keyword, category, proxy, factory);
										break;
									} catch (Exception e) {
										simulationLog.error("", e);
									}
								}

								Set<String> doneList = doneMap.get(category);
								if (CollectionUtils.isEmpty(doneList))
									doneList = new HashSet<String>();
								doneList.add(keyword);
								doneMap.put(category, doneList);

								setDailyRankingAndSimulationTask(sku, category, keyword);

								List<TargetSKU> waitingList = waitingMap.get(category + keyword);

								if (!CollectionUtils.isEmpty(waitingList)) {
									waitingList.forEach(waitSku -> {
										log.info("Start generate asin {} simulation task and daily ranking",
												waitSku.getAsin());
										setDailyRankingAndSimulationTask(waitSku, category, keyword);
									});

									waitingMap.remove(category + keyword);
								}
							});

						} else {

							if (!CollectionUtils.isEmpty(doneMap.get(category))
									&& doneMap.get(category).contains(keyword)) {
								log.info("category {} keyword {} already scaned", category, keyword);
								setDailyRankingAndSimulationTask(sku, category, keyword);
							} else {
								List<TargetSKU> waitingList = waitingMap.get(category + keyword);
								if (CollectionUtils.isEmpty(waitingList))
									waitingList = new ArrayList<TargetSKU>();
								waitingList.add(sku);
								waitingMap.put(category + keyword, waitingList);
							}
						}

			cursor = skuList.get(skuList.size() - 1).getCreateTime();
			skuList = productDao.getSkuList(cursor);
		}

		simulationLog.info("Generate simulation task for {} finished", taskDate);
	}

	@Override
	public void setDailyRankingAndSimulationTask(TargetSKU sku, String category, String keyword) {

		DailyRanking ranking = new DailyRanking();
		ranking.setAsin(sku.getAsin());
		ranking.setCategory(category);
		ranking.setKeyword(keyword);
		ranking.setRankDate(taskDate);
		ranking.setProductName(sku.getProductName());

		SKUInfo info = productDao.getSKUInfo(sku.getAsin(), keyword, category, taskDate);

		if (info != null) {
			if (sku.isSimulate())
				simulationDao.insertSimulationTask(info.getSimulationTask(taskDate));

			ranking.setColumn(info.getColumnNum());
			ranking.setPage(info.getPageNum());
			long countBeforePage = productDao.getCountBeforeSKUPage(keyword, category, info.getPageNum(), taskDate);
			long countInPage = productDao.getCountBeforeSKU(keyword, category, info.getPageNum(), info.getColumnNum(),
					taskDate);
			ranking.setRanking((int) (countBeforePage + countInPage));
		} else {
			ranking.setRanking(-1);
			ranking.setPage(-1);
			ranking.setColumn(-1);
		}

		productDao.insertSKUDailyRanking(ranking);
	}

	@Override
	public int predictSimulationNum(List<SKUInfo> infoList, int lastPrediction) {
		List<Integer> reviewNum = new ArrayList<Integer>();
		int prediction;
		int totalReview = 0;

		if (CollectionUtils.isEmpty(infoList))
			return lastPrediction;

		int x = (int) (lastPrediction * ((0.8 / (infoList.get(0).getPageNum() + 1)) + 0.2)
				/ ((0.8 / (infoList.get(0).getPageNum())) + 0.2));

		for (SKUInfo info : infoList) {
			if (StringUtils.isEmpty(info.getReviewNum()))
				reviewNum.add(0);
			else {
				reviewNum.add(Integer.parseInt(info.getReviewNum().replaceAll(",", "")));
			}
			totalReview += reviewNum.get(reviewNum.size() - 1);
		}

		Collections.sort(reviewNum);

		if (reviewNum.size() > 10) {
			totalReview -= reviewNum.get(0);
			totalReview -= reviewNum.get(reviewNum.size() - 1);

			if (lastPrediction == -1) {
				prediction = totalReview / (reviewNum.size() - 2);
				if (prediction > 200)
					prediction = new Random().nextInt(100) + 150;
				if (!infoList.get(0).getCategory().equals("All Departments"))
					prediction *= 0.8;
			} else
				prediction = (((totalReview / (reviewNum.size() - 2)) + x) / 2);

		} else
			prediction = x;

		for (SKUInfo info : infoList)
			if (lastPrediction == -1 && prediction > 200)
				info.setPresetSimulationNum(new Random().nextInt(50));
			else
				info.setPresetSimulationNum(prediction + new Random().nextInt(50));

		return prediction;
	}

}
