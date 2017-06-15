/**
 * 
 */
package com.amazon.crawl.base;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.organization.common.bean.BaseRequest;
import com.organization.common.bean.BaseResult;
import com.organization.common.bean.Client;
import com.organization.common.util.general.StringUtils;
import com.organization.common.util.general.Utils;
import com.organization.common.util.security.SignUtils;

public class HttpTextConn {

	public static final String SIGN_SECRET = "";

	public static final String url = "http://121.201.28.228:8080/amazon-crawl/";
	private static Properties props = new Properties();

	public static JSONObject sendMessage(String surfixUrl) throws Exception {
		return sendMessage(null, surfixUrl);
	}

	@SuppressWarnings("unchecked")
	public static JSONObject sendMessage(BaseRequest baseRequest, String surfixUrl) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		Map<String, Object> baseMsgMap = null;

		HttpPost httpPost = new HttpPost(url + surfixUrl);

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(30000).setConnectTimeout(30000).build();
		httpPost.setConfig(requestConfig);

		setBaseRequest(baseRequest);

		Map<String, Object> paramMap = Utils.objectToMap(baseRequest);

		if (baseRequest == null || baseRequest.getClass() == BaseRequest.class)
			baseMsgMap = paramMap;
		else
			baseMsgMap = Utils.objectSuperClassToMap(baseRequest);
		// 创建待处理的表单域内容文本
		Map<String, String> signMap = new TreeMap<>();
		for (Entry<String, Object> entry : baseMsgMap.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			signMap.put(entry.getKey(), entry.getValue().toString());
		}

		props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config/component.factory.test"));

		paramMap.put("sign", SignUtils.sign(signMap, props.getProperty("sign.secret", "defaultSecret")));

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (Entry<String, Object> en : paramMap.entrySet()) {
			if (en.getValue() != null)
				nvps.add(new BasicNameValuePair(en.getKey(), en.getValue().toString()));

		}
		httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

		CloseableHttpResponse response = httpclient.execute(httpPost);

		int code = response.getStatusLine().getStatusCode();
		HttpEntity entity = response.getEntity();
		Header[] headers = response.getAllHeaders();
		if (HttpStatus.SC_OK == code) {

			JSONObject object = null;
			// 显示内容
			if (entity != null) {
				object = JSON.parseObject(EntityUtils.toString(entity, "utf-8"));
				Map<String, String> headerMap = new HashMap<String, String>();

				for (Header header : headers)
					headerMap.put(header.getName(), header.getValue());

				System.out.println("Header List:");
				System.out.println(JSON.toJSONString(headerMap, true));
				System.out.println("Body Param List:");
				System.out.println(JSON.toJSONString(object, true));

			}
			if (entity != null) {
				entity.consumeContent();
			}

			return object;
		} else {
			System.err
					.println(" http request error : " + code + " and errmsg: " + EntityUtils.toString(entity, "utf-8"));
			throw new Exception(" request failire , response code = " + code);
		}
	}

	private static void setBaseRequest(BaseRequest baseRequest) {
		if (baseRequest == null)
			baseRequest = new BaseRequest();

		baseRequest.setClientType(Client.ANDROID.type);

		if (StringUtils.isEmpty(baseRequest.getLoginUserId())) {
			baseRequest.setLoginUserId("-1");
		}
		if (StringUtils.isEmpty(baseRequest.getToken())) {
			baseRequest.setToken("default");
		}
		if (StringUtils.isEmpty(baseRequest.getImei())) {
			baseRequest.setImei("f4:2a:3d:10:5a");
		}
		if (StringUtils.isEmpty(baseRequest.getClientVersion())) {
			baseRequest.setClientVersion("1");
		}
		if (StringUtils.isEmpty(baseRequest.getDeviceInfo())) {
			baseRequest.setDeviceInfo("1.0.0-1280X900");
		}
		if (StringUtils.isEmpty(baseRequest.getImei())) {
			baseRequest.setImei("1.0.0-1280X900");
		}
		if (StringUtils.isEmpty(baseRequest.getDeviceType())) {
			baseRequest.setDeviceType("iPhone7,1");
		}

		baseRequest.setTimestamp(new Date().getTime());
	}

}
