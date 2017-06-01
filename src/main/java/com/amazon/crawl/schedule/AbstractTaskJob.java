package com.amazon.crawl.schedule;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/****
 * 定时任务基础类。抽象了一些基本的设置
 * 
 * @author xu.jianpu
 *
 */
public abstract class AbstractTaskJob implements Job {
	private static final Logger log = LoggerFactory.getLogger(AbstractTaskJob.class);

	/***
	 * 定时任务初始化
	 * 
	 * @param context
	 */
	public static void selfInit(ApplicationContext context, Class<? extends Job> clz, String name, String cron)
			throws Exception {

		log.debug("job被初始化");
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		JobDetail jobDetail = newJob(clz).withIdentity(name).build();
		setJodDetail(context, jobDetail.getJobDataMap());
		Trigger trigger = newTrigger().withIdentity(name).withSchedule(cronSchedule(cron)).build();
		scheduler.scheduleJob(jobDetail, trigger);
		scheduler.start();
	}

	protected static Map<String, Object> jobDetail = new HashMap<String, Object>();

	/***
	 * 设置具体定时任务执行时需要的从容器中获取的实例
	 * 
	 * @param dataMap
	 * @throws UossException
	 */
	protected static void setJodDetail(ApplicationContext context, JobDataMap dataMap) {
		for (Entry<String, Object> en : jobDetail.entrySet()) {
			dataMap.put(en.getKey(), en.getValue());
		}
	}
}
