package com.amazon.crawl.dao;

import java.util.List;

import com.amazon.crawl.model.SimulationTask;

public interface SimulationDao {

	void insertSimulationTask(List<SimulationTask> taskList);

	SimulationTask getSimulationTask();

	void updateSimulationTaskStatus(String id, int status);

	SimulationTask getExtraSimulationTask(List<String> asinList, String category, String keyword);

}
