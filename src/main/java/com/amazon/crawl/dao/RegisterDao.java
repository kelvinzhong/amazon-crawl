package com.amazon.crawl.dao;

import java.util.List;

import com.amazon.crawl.model.RegisterTask;
import com.amazon.crawl.model.WebProxy;

public interface RegisterDao {

	RegisterTask getRegisterTask();

	void insertRegisterTask(List<RegisterTask> taskList);

	void updateRegisterTaskStatusById(String id, int status);

}
