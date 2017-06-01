package com.amazon.crawl.dao.impl;

import javax.annotation.Resource;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import java.util.Date;
import java.util.List;

import com.amazon.crawl.dao.RegisterDao;
import com.amazon.crawl.model.RegisterTask;
import com.amazon.crawl.model.WebProxy;
import com.organization.common.config.properties.Configuration;
import com.organization.common.util.general.Constants;

@Repository
public class RegisterDaoImpl implements RegisterDao {

	@Resource
	private MongoTemplate mongoTemplate;

	@Override
	public RegisterTask getRegisterTask() {
		return mongoTemplate.findAndModify(
				query(where("status").is(Constants.STATUS_NORMAL).and("taskTime").lt(new Date())),
				new Update().set("status", Constants.STATUS_DONE), RegisterTask.class);
	}

	@Override
	public void updateRegisterTaskStatusById(String id, int status) {
		mongoTemplate.updateFirst(query(where("id").is(id)), new Update().set("status", status), RegisterTask.class);
	}

	@Override
	public void insertRegisterTask(List<RegisterTask> taskList) {
		mongoTemplate.insertAll(taskList);
	}
}
