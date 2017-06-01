package com.amazon.crawl.model;

import java.util.Date;
import java.util.List;

public class AmazonUser {

	private String id;
	private String proxyHost;
	private String emailAcount;
	private String password;
	private String nickname;
	private List<String> asinList;
	private Date createTime;
	private Date updateTime;
	private String taskId;
	private String cookies;

	public String getCookies() {
		return cookies;
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}

	public AmazonUser() {
		this.createTime = new Date();
		this.updateTime = new Date();
	}

	public AmazonUser(RegisterTask task) {
		super();
		this.taskId = task.getId();
		this.proxyHost = task.getProxy().getHost();
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getAsinList() {
		return asinList;
	}

	public void setAsinList(List<String> asinList) {
		this.asinList = asinList;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public String getEmailAcount() {
		return emailAcount;
	}

	public void setEmailAcount(String emailAcount) {
		this.emailAcount = emailAcount;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

}
