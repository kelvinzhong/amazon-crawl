package com.amazon.crawl.schedule;

import java.util.Date;
import java.util.Map;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.amazon.common.util.general.DateUtils;
import com.amazon.crawl.service.SimulationService;
import com.amazon.crawl.service.impl.SimulationServiceImpl;
import com.organization.common.config.properties.Configuration;

public class ScheduleJobs extends AbstractTaskJob {

	private static final Logger log = LoggerFactory.getLogger(ScheduleJobs.class);
	public static final String SIMULATION_TASK_KEY = "SIMULATION_TASK";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		Date now = new Date();
		Map<String, Object> map = context.getJobDetail().getJobDataMap();
		String job_name = context.getJobDetail().getKey().getName();
		SimulationService simulationService = (SimulationService) map.get(SIMULATION_TASK_KEY);
		log.debug("job {} arrive", job_name);
		try {
			if (job_name.equals(SIMULATION_TASK_KEY)) {
				log.info(" {}，分配当日模拟点击Task列表开始  >>>>>>>", now.toString());
				SimulationServiceImpl.taskDate = DateUtils.getTodayZeorHour();
				simulationService.startGenerateSimulationTask();
				log.info(" {} - {}，分配当日模拟点击Task列表结束  >>>>>>>", now.toString(), new Date().toString());
			}

		} catch (Exception e) {
			if (job_name.equals(SIMULATION_TASK_KEY))
				log.error(" {} - {}，分配当日模拟点击Task列表失败", now.toString(), new Date().toString(), e);
		}

	}

	public static void init(ApplicationContext context) {

		try {
			AbstractTaskJob.jobDetail.put(SIMULATION_TASK_KEY,
					context.getBean("simulationService", SimulationService.class));

			String cron = Configuration.getValue("SIMULATION_TASK_QUARTZ_CRON", "0 15 15 * * ?");
			AbstractTaskJob.selfInit(context, ScheduleJobs.class, SIMULATION_TASK_KEY, cron);
			log.info("********************分配当日模拟点击Task列表初始化成功********************"); // TODO
			// 每晚10.30执行生成第二天任务列表

		} catch (Exception e) {
			log.error("Create SIMULATION task init failed", e);
		}

	}
}
