package com.amazon.crawl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorFactory {

	private static final ExecutorService SIMULATION_EXECUTOR = Executors.newFixedThreadPool(5);
	
	private static final ExecutorService RANKING_EXECUTOR = Executors.newFixedThreadPool(5);
	
	//generate register task, process register task, process simulation task
	private static final ExecutorService TASK_EXECUTOR = Executors.newFixedThreadPool(3);

	public static ExecutorService getRankingExecutor() {
		return RANKING_EXECUTOR;
	}

	public static ExecutorService getSimulationExecutor() {
		return SIMULATION_EXECUTOR;
	}
	
	public static ExecutorService getTaskExecutor() {
		return TASK_EXECUTOR;
	}
}
