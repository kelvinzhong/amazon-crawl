package com.amazon.crawl.model.result;

import java.util.List;

import com.amazon.crawl.model.DailyRanking;
import com.organization.common.bean.BaseResult;

public class SKURankingResult extends BaseResult {

	private List<DailyRanking> rankingList;

	public List<DailyRanking> getRankingList() {
		return rankingList;
	}

	public void setRankingList(List<DailyRanking> rankingList) {
		this.rankingList = rankingList;
	}
}
