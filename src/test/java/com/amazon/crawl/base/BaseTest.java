package com.amazon.crawl.base;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/spring-test.xml", "classpath*:/spring/mongo-service-config.xml" })
public class BaseTest {

	@Before
	public void before() {
		System.out.println("======================= New test Begin =======================");
	}

	@After
	public void after() {
		System.out.println("======================== Test Finish ========================");
	}
}
