package com.amazon.crawl.dao.impl;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Date;
import java.util.List;

import com.amazon.crawl.dao.SimulationDao;
import com.amazon.crawl.model.SimulationTask;

@Repository
public class SimulationDaoImpl implements SimulationDao {

	@Resource
	private MongoTemplate mongoTemplate;
	
	public void getSimulationTask(){
		mongoTemplate.find(query(where("taskTime").gt(new Date())), SimulationTask.class);
	}
	
	@Override
	public void insertSimulationTask(List<SimulationTask> taskList){
		mongoTemplate.insertAll(taskList);
	}
}
