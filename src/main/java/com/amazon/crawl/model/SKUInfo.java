package com.amazon.crawl.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.amazon.common.util.general.DateUtils;
import com.amazon.crawl.service.impl.SimulationServiceImpl;

public class SKUInfo {

	private String id;
	private String asin;
	private String title;
	private String company;
	private boolean sponsored;
	private boolean prime;
	private String currentPrice;
	private String reviewNum;
	private int pageNum;
	private int columnNum;
	private Date createTime;
	private int presetSimulationNum;
	private String category;
	private String keyword;

	public SKUInfo() {
		this.sponsored = false;
		this.prime = false;
		this.createTime = SimulationServiceImpl.taskDate;
	}

	public SKUInfo(int pageNum, String keyword, String category, int columnNum) {
		this();
		this.pageNum = pageNum;
		this.columnNum = columnNum;
		this.keyword = keyword;
		this.category = category;
	}

	public List<SimulationTask> getSimulationTask(Date taskDate) {
		List<SimulationTask> taskList = new ArrayList<SimulationTask>();

		for (int i = 0; i < presetSimulationNum; i++) {
			SimulationTask task = new SimulationTask();

			task.setAsin(asin);
			task.setCategory(category);
			task.setCreateTime(new Date());
			task.setKeyword(keyword);
			task.setTaskTime(
					new Date(DateUtils.getNextDate(taskDate).getTime() + new Random().nextInt(20 * 60 * 60) * 1000));
			task.setUpdateTime(new Date());

			taskList.add(task);
		}

		return taskList;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getPresetSimulationNum() {
		return presetSimulationNum;
	}

	public void setPresetSimulationNum(int presetSimulationNum) {
		this.presetSimulationNum = presetSimulationNum;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getColumnNum() {
		return columnNum;
	}

	public void setColumnNum(int columnNum) {
		this.columnNum = columnNum;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public boolean isPrime() {
		return prime;
	}

	public void setPrime(boolean prime) {
		this.prime = prime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAsin() {
		return asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public boolean isSponsored() {
		return sponsored;
	}

	public void setSponsored(boolean sponsored) {
		this.sponsored = sponsored;
	}

	public String getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(String currentPrice) {
		this.currentPrice = currentPrice;
	}

	public String getReviewNum() {
		return reviewNum;
	}

	public void setReviewNum(String reviewNum) {
		this.reviewNum = reviewNum;
	}

}
