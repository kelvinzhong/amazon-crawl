package com.amazon.crawl.dao;

import java.util.List;

import com.amazon.crawl.model.SimulationTask;

public interface SimulationDao {

	void insertSimulationTask(List<SimulationTask> taskList);

}
