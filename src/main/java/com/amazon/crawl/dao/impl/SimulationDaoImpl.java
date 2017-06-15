package com.amazon.crawl.dao.impl;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Date;
import java.util.List;

import com.amazon.crawl.dao.SimulationDao;
import com.amazon.crawl.model.RegisterTask;
import com.amazon.crawl.model.SimulationTask;
import com.organization.common.util.general.Constants;

@Repository
public class SimulationDaoImpl implements SimulationDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public void insertSimulationTask(List<SimulationTask> taskList) {
		mongoTemplate.insertAll(taskList);
	}

	@Override
	public SimulationTask getSimulationTask() {
		return mongoTemplate.findAndModify(
				query(where("status").is(Constants.STATUS_NORMAL).and("taskTime").lt(new Date())),
				new Update().set("status", Constants.STATUS_DONE), SimulationTask.class);
	}

	@Override
	public void updateSimulationTaskStatus(String id, int status) {
		mongoTemplate.updateFirst(query(where("id").is(id)), new Update().set("status", status), SimulationTask.class);
	}

	@Override
	public SimulationTask getExtraSimulationTask(List<String> asinList, String category, String keyword) {
		return mongoTemplate.findAndModify(
				query(where("asin").nin(asinList).and("category").is(category).and("keyword").is(keyword).and("status")
						.is(Constants.STATUS_NORMAL)),
				new Update().set("status", Constants.STATUS_DONE), SimulationTask.class);
	}
}
