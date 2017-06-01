package com.amazon.crawl.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.amazon.crawl.dao.ProxyDao;
import com.amazon.crawl.model.WebProxy;
import com.amazon.crawl.service.ProxyService;
import com.organization.common.util.general.CollectionUtils;

@Service
public class ProxyServiceImpl implements ProxyService {

	private static final Logger log = LoggerFactory.getLogger(ProxyServiceImpl.class);

	@Resource
	private ProxyDao proxyDao;

	@Override
	public void loadProxyList(String path) {
		Properties props = new Properties();
		List<WebProxy> proxyList = new ArrayList<WebProxy>();
		List<String> hostList = new ArrayList<String>();

		try (InputStreamReader read = new InputStreamReader(new FileInputStream(new File(path)),
				StandardCharsets.UTF_8);) {
			props.load(read);
		} catch (Exception e) {
			log.error("unexpected error during read the proxy file", e);
		}

		for (Entry<Object, Object> p : props.entrySet()) {
			if (p != null) {
				WebProxy proxy = decodeProxy(p);
				if (proxy != null) {
					proxyList.add(proxy);
					hostList.add(proxy.getHost());
				}
			}
		}

		log.info("total have {} line record in txt file, convert {} into proxy", props.size(), proxyList.size());

		List<WebProxy> existProxy = proxyDao.getProxyByHostList(hostList);

		if (CollectionUtils.isEmpty(existProxy))
			proxyDao.insertAllProxy(proxyList);
		else
			log.info("{} proxy already exist!", existProxy.size());
	}

	private WebProxy decodeProxy(Entry<Object, Object> properties) {
		WebProxy proxy = new WebProxy();
		String value = (String) properties.getValue();

		proxy.setUserName((String) properties.getKey());

		int pwIndex = value.indexOf("@");
		if (pwIndex < 0)
			return null;

		String password = value.substring(0, pwIndex);

		if (StringUtils.isEmpty(password))
			return null;
		proxy.setPassword(password);

		int hostIndex = value.indexOf(":");
		if (hostIndex < 0)
			return null;

		String host = value.substring(pwIndex + 1, hostIndex);

		if (StringUtils.isEmpty(host))
			return null;
		proxy.setHost(host);

		String port = value.substring(hostIndex + 1);

		if (StringUtils.isEmpty(port))
			return null;
		proxy.setPort(Integer.parseInt(port));

		proxy.setCreateTime(new Date());
		proxy.setUpdateTime(new Date());

		return proxy;
	}
}
