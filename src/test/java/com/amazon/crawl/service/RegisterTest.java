package com.amazon.crawl.service;

import javax.annotation.Resource;

import org.junit.Test;

import com.amazon.crawl.base.BaseTest;
import com.organization.common.config.properties.Configuration;

public class RegisterTest extends BaseTest {

	@Resource
	private RegisterService registerService;

	@Test
	public void test() {
		boolean emtpyTask = false;
		while (true) {
			if (emtpyTask)
				try {
					Thread.sleep(10 * 1000);

					System.out.println("done");
				} catch (InterruptedException e) {
				}

			emtpyTask = registerService.generateRegisterTask();
		}
	}

	@Test
	public void test2() {
		boolean emtpyTask = false;
		while (true) {
			if (emtpyTask)
				try {
					System.out.println("done");
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
				}

			emtpyTask = registerService.processRegisterTask();
		}
	}
	
	@Test
	public void test3(){

		if (Configuration.getProperty("register.task.generate", false)) {
			System.out.println("asdf");
		}
	}

}
