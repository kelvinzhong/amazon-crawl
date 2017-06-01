package com.amazon.crawl.service;

import java.util.List;

import com.amazon.crawl.model.SKUInfo;
import com.amazon.crawl.model.TargetSKU;

public interface SimulationService {

	void startGenerateSimulationTask();

	int predictSimulationNum(List<SKUInfo> infoList, int lastPrediction);

	void setDailyRankingAndSimulationTask(TargetSKU sku, String category, String keyword);

}
