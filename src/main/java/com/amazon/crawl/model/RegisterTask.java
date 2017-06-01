package com.amazon.crawl.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class RegisterTask {

	private String id;
	private WebProxy proxy;
	private Date createTime;
	private Date taskTime;
	private Date finishTime;
	private int status;

	public RegisterTask(WebProxy proxy) {
		this.proxy = proxy;
		this.taskTime = new Date(new Date().getTime() + new Random().nextInt(2 * 24 * 60 * 60) * 1000);
		this.createTime = new Date();
	}

	public Date getTaskTime() {
		return taskTime;
	}

	public void setTaskTime(Date taskTime) {
		this.taskTime = taskTime;
	}

	public static List<RegisterTask> generateRegisterTask(WebProxy proxy, int taskNum) {
		List<RegisterTask> taskList = new ArrayList<RegisterTask>();
		for (int i = 0; i < taskNum; i++)
			taskList.add(new RegisterTask(proxy));
		return taskList;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public WebProxy getProxy() {
		return proxy;
	}

	public void setProxy(WebProxy proxy) {
		this.proxy = proxy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
