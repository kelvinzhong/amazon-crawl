package com.amazon.crawl.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.amazon.crawl.HtmlUnitHandler;
import com.amazon.crawl.dao.AmazonUserDao;
import com.amazon.crawl.dao.ProxyDao;
import com.amazon.crawl.dao.RegisterDao;
import com.amazon.crawl.model.AmazonUser;
import com.amazon.crawl.model.RegisterTask;
import com.amazon.crawl.model.WebProxy;
import com.amazon.crawl.service.CrawlService;
import com.amazon.crawl.service.RegisterService;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlEmailInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.nimbusds.jose.util.Base64;
import com.organization.common.bean.SystemException;
import com.organization.common.config.properties.Configuration;
import com.organization.common.util.general.CollectionUtils;
import com.organization.common.util.general.Constants;

@Service
public class RegisterServiceImpl implements RegisterService {
	private static final Logger log = LoggerFactory.getLogger(RegisterServiceImpl.class);
	private static final Logger registerLog = LoggerFactory.getLogger("RegisterTask");

	@Resource
	private ProxyDao proxyDao;
	@Resource
	private RegisterDao registerDao;
	@Resource
	private AmazonUserDao amazonUserDao;
	@Resource
	private CrawlService crawlService;

	private static final String DEFAULT_PROXY_USER_COUNT = "defaultProxyUserCount";
	private static final int defaultProxyUserCount = 10;
	private static final BlockingQueue<String> nameQueque = new LinkedBlockingQueue<String>();

	@Override
	public boolean generateRegisterTask() {

		List<String> proxyIdList = new ArrayList<String>();

		List<WebProxy> proxyList = proxyDao
				.getProxyBelowUserCount(Configuration.getValue(DEFAULT_PROXY_USER_COUNT, defaultProxyUserCount));

		if (!CollectionUtils.isEmpty(proxyList)) {
			for (WebProxy proxy : proxyList) {
				List<RegisterTask> taskList = RegisterTask.generateRegisterTask(proxy,
						Configuration.getValue(DEFAULT_PROXY_USER_COUNT, defaultProxyUserCount) - proxy.getUserCount());
				proxyIdList.add(proxy.getId());

				registerDao.insertRegisterTask(taskList);
			}

			proxyDao.updateProxyUserCountByIdList(proxyIdList,
					Configuration.getValue(DEFAULT_PROXY_USER_COUNT, defaultProxyUserCount));
		} else
			return true;

		return false;
	}

	@Override
	public boolean processRegisterTask() {

		RegisterTask task = registerDao.getRegisterTask();

		if (task == null)
			return true;

		WebClient webClient = null;
		AmazonUser user = new AmazonUser(task);

		try {
			webClient = HtmlUnitHandler.initializeClient(task.getProxy().getHost(), task.getProxy().getPort());
			HtmlUnitHandler.proxyForWebClient(webClient, task.getProxy().getUserName(), task.getProxy().getPassword());

			registerLog.info("Start register amazon user");
			if (registerAmazonUser(webClient, user)) {
				Set<Cookie> cookies = webClient.getCookieManager().getCookies();
				if (!CollectionUtils.isEmpty(cookies))
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(bos);
						oos.writeObject(cookies);
						oos.flush();

						user.setCookies(org.apache.xerces.impl.dv.util.Base64.encode(bos.toByteArray()));
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				registerLog.info("Register amazon user successfully! {}", JSON.toJSONString(user));
				amazonUserDao.insertAmazonUser(user);
			} else {
				registerLog.warn("Register task {} fail!", task.getId());
				registerDao.updateRegisterTaskStatusById(task.getId(), Constants.STATUS_DELETE);
			}

		} catch (Exception e) {
			registerLog.error("error during register amazon user!", e);
			registerDao.updateRegisterTaskStatusById(task.getId(), Constants.STATUS_DELETE);
		} finally {

			// try {
			// // 随机0-30秒时间模拟用户关闭窗口延迟
			// Thread.sleep(new Random().nextInt(30) * 1000);
			// } catch (InterruptedException e) {
			// log.error("interrupt during sleep", e);
			// }
			webClient.getCurrentWindow().getJobManager().removeAllJobs();
			webClient.close();
			log.debug("release web client resource");
		}

		return false;
	}

	private boolean registerAmazonUser(WebClient webClient, AmazonUser user) {

		try {
			HtmlPage page = webClient.getPage("https://www.amazon.com/");
			registerLog.info("Open amazon home page");

			HtmlPage userHome = null;
			HtmlPage register = null;
			List<Object> anchorList = null;
			do {

				if (userHome != null) {
					registerLog.info("Email address already in use, register fail, try again");

					userHome.getByXPath("//a");
					anchorList = userHome.getByXPath("//a[@class='a-link-normal']");

					userHome.cleanUp();
					userHome = null;
					register.cleanUp();
					register = null;

				} else {
					page.getByXPath("//a");
					anchorList = page.getByXPath("//a[@class='a-link-normal']");
				}

				for (Object o : anchorList)
					if (((HtmlAnchor) o).getHrefAttribute().contains("www.amazon.com/ap/register")) {
						register = ((HtmlAnchor) o).click();
						registerLog.info("Click into register page");
						break;
					}

				if (register == null) {
					registerLog.error("Seems the link of register page changed in https://www.amazon.com!");
					return false;
				}

				HtmlForm form = register.getFormByName("register");

				setRegisterForm(form, user);

				HtmlSubmitInput button = form.getFirstByXPath("//input[@id='continue']");
				userHome = button.click();
				registerLog.info("Submit register");

			} while (userHome.asText().contains("Email address already in use"));

			HtmlForm search = userHome.getFormByName("site-search");

			if (search == null)
				return false;

		} catch (Exception e) {
			registerLog.error("error during register user", e);
			if(e.toString().contains("Read timed out")){
				user.setStatus(Constants.STATUS_DELETE);
				amazonUserDao.insertAmazonUser(user);
			}
				
			return false;
		}

		return true;
	}

	private void setRegisterForm(HtmlForm form, AmazonUser user) {

		HtmlTextInput name = form.getInputByName("customerName");
		user.setNickname(getAmazonUserNickName());
		name.setValueAttribute(user.getNickname());

		HtmlEmailInput email = form.getInputByName("email");
		user.setEmailAcount(userRename(user.getNickname()) + "@"
				+ Email.values()[new Random().nextInt(Email.values().length)].getPostfix());
		email.setValueAttribute(user.getEmailAcount());

		HtmlPasswordInput password = form.getInputByName("password");
		if (Math.random() < 0.2)
			user.setPassword(user.getEmailAcount().substring(0, user.getEmailAcount().indexOf("@")));
		else
			user.setPassword(
					Base64.encode(UUID.randomUUID().toString()).toString().substring(0, new Random().nextInt(19) + 6));
		password.setValueAttribute(user.getPassword());

		HtmlPasswordInput passwordCheck = form.getInputByName("passwordCheck");
		passwordCheck.setValueAttribute(user.getPassword());
	}

	private String getAmazonUserNickName() {

		if (nameQueque.isEmpty()) {
			String sex = Math.random() < 0.5 ? "nan" : "nv";

			List<String> nameList = crawlService.getUserName(sex);

			while (CollectionUtils.isEmpty(nameList))
				nameList = crawlService.getUserName(sex);

			nameList.forEach(name -> nameQueque.offer(name));
		}

		try {
			return nameQueque.take();
		} catch (InterruptedException e) {
			registerLog.error("error during take a name from queque", e);
		}
		return null;
	}

	private String userRename(String name) {
		String newName = name;

		if (name.contains(" ")) // 两个英文单词隔开空格
			if (Math.random() < 0.3)
				newName = name.replaceAll("\\s", "_");
			else
				newName = name.replaceAll("\\s", "");

		newName = newName.replaceAll("\\u0028", "");
		newName = newName.replaceAll("\\u0029", "");

		if (Math.random() < 0.3) {
			if (capitalIndex(newName) != -1) // 保留第一个英文单词
				newName = newName.substring(0, capitalIndex(name)).trim();
		} else if (Math.random() > 0.7)
			if (capitalIndex(newName) != -1) // 保留后面一个英文单词
				newName = newName.substring(capitalIndex(name)).trim();

		double random = Math.random();

		if (random < 0.3)
			newName = newName.toLowerCase(); // 全部转小写
		else if (random > 0.7)
			newName = newName.toUpperCase(); // 全部大写

		if (Math.random() < 0.3)
			newName += new Random().nextInt(27) + 1972;

		if (Math.random() < 0.3) {
			newName += "0" + new Random().nextInt(9);
			if (Math.random() < 0.5)
				newName += new Random().nextInt(28);
		}

		return newName;
	}

	private static int capitalIndex(String name) {
		Pattern p = Pattern.compile("[A-Z]");
		for (char b : name.toCharArray()) {
			Matcher m = p.matcher(String.valueOf(b));
			if (m.find() && name.indexOf(b) != 0)
				return name.indexOf(b);
		}
		return -1;
	}

	enum Email {
		QQ("qq.com"), WANGYI("163.com"), GOOGLE("gmail.com"), WANGYI2("126.com"), HOTMAIL("hotmail.com"), MSN(
				"msn.com"), YAHU("yahoo.com"), SINA("sina.com"), OUTLOOK(
						"outlook.com"), MAIL("mail.com"), INBOX("inbox.com"), ICOULD("icould.com"), AOL("aol.com");

		private String postfix;

		Email(String postfix) {
			this.postfix = postfix;
		}

		public String getPostfix() {
			return postfix;
		}

	}

}
