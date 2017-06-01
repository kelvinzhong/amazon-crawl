package com.amazon.crawl;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.amazon.crawl.schedule.ScheduleJobs;
import com.organization.common.config.properties.Configuration;

public class AppServlet extends HttpServlet {
	private static final Logger log = LoggerFactory.getLogger(AppServlet.class);
	private ApplicationContext ctx;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		log.debug("app servlet get!");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.ctx = WebApplicationContextUtils.getWebApplicationContext(config.getServletContext());

		log.debug("Schedule start init!");
		// 检查是否开启模拟点击
		if ("on".equals(Configuration.getProperty("SIMULATION_TASK", "on"))) {
			log.debug("Simulation job init!");
			ScheduleJobs.init(ctx);
		}
	}

}
