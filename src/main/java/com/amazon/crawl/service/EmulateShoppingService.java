package com.amazon.crawl.service;

import java.io.IOException;
import java.util.List;

import com.amazon.crawl.WebClientFactory;
import com.amazon.crawl.model.AmazonUser;
import com.amazon.crawl.model.SimulationTask;

public interface EmulateShoppingService {

	List<String> simulateClickPage(WebClientFactory factory, SimulationTask task, AmazonUser user) throws IOException, ClassNotFoundException;

	void setExtraTask(List<String> taskAsinList, SimulationTask task);

}
