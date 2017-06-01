package com.amazon.crawl.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.util.CollectionUtils;

import com.amazon.common.util.general.DateUtils;
import com.amazon.crawl.base.BaseTest;
import com.amazon.crawl.dao.ProductDao;
import com.amazon.crawl.dao.ProxyDao;
import com.amazon.crawl.model.SKUInfo;
import com.amazon.crawl.model.TargetSKU;
import com.amazon.crawl.model.WebProxy;
import com.amazon.crawl.service.impl.SimulationServiceImpl;

public class SimulationServiceTest extends BaseTest {

	@Resource
	private SimulationService simulationService;
	@Resource
	private ProductDao productDao;
	@Resource
	private ProxyDao proxyDao;
	@Resource
	private RegisterService registerService;

	@Test
	public void test() throws InterruptedException {

		SimulationServiceImpl.taskDate = DateUtils.getTodayZeorHour();
//		System.out.println(SimulationServiceImpl.taskDate);
		simulationService.startGenerateSimulationTask();
		Thread.sleep(10000000);
	}
	
	@Test
	public void test3(){
		SKUInfo info = productDao.getSKUInfo("B01N0PA3T7", "braided iphone cable", "Electronics", SimulationServiceImpl.taskDate);
//		long totalProxy = proxyDao.getTotalProxyCount();
//		WebProxy proxy = proxyDao.getRandomProxy(totalProxy);
		String uri = "https://www.amazon.com/s/ref=sr_pg_99?rh=n%3A172282%2Ck%3Alightning+cable+black&page=99&keywords=lightning+cable+black&ie=UTF8&qid=1495859427&spIA=B07192585Q,B01N5EVOGB,B01DX3DLPG,B01MF6V2RF,B06ZYX47GZ,B01MSY2CKW,B00Y8BULT2,B01DX3DOQC,B06ZZFXX5Q,B06XFRZ4HQ,B01IZ784VQ,B071R6T9JB,B00M5JKZY0,B012UCZS8Y,B06X17V5QJ,B06ZZQLLF1,B00N80BK1M,B01MS06XFX,B06XD2JYTQ,B06ZZ7LQJY,B01FCUJ134,B01N07NB4Z,B06X9TNHBZ,B01FL0WAUG,B072L17FLB,B072L1637Z,B01N4ES86U,B01FCUJ47M,B015NMP8Q0,B01MSUUXKF,B01MY7653P,B01M3N0YDM,B017VTQXKS,B01M6TXZYY,B06Y21KPMZ,B01HRSTFYE,B0725BHT9L,B06XKSGT5G,B01N5J54FF,B01CNIIXSM,B01EIXWFKW,B06XGZ5X7K,B07114Z5TP,B018JL8E9A,B0722N4K2W,B01LYO4AC5,B071YLFCQR,B071DMYXTZ,B01C2QLGAM,B00K8GZED4,B01M9GRMLK,B06XXWGYCM,B06ZYY7MCS,B01L6JMWKO,B01MT11BOK,B01MRZC93T,B071YF2WJF,B011AQUHCI,B06XBYRVR5,B071YH4YBS,B01B2L2H2Y,B07228NFY5,B06X6J5L8C,B06WVCXLST,B071VJ9FCC,B06WGP2XJL,B06Y6N17DR,B01KVFJ7SE,B06XVPDJFV,B01DKL4XB2,B01IV37K5U,B018JMTHRM,B06WD4PQ3Z,B01N9F10WR,B01N2PMYMT,B00VDX23JY,B01IZHWVBA,B00SU7AYRY,B06ZZ2BKHW,B01N2Y05BL,B071H4V3PQ,B06XCZ2WLS,B06WVZP89B,B06ZYMXPJJ,B01MRNP3WN,B01M7OAVP7,B072537RYS,B01N97LDJF,B072F2NG7M,B072L7XB7Y,B072L81XS8,B00BGUG3EA,B01DW8TLLU,B0713XKHRY,B06XFPPH6X,B01IZ9X0FY,B06XC76DVJ,B01N3YSVT3,B0716B56BY,B06XH2BLZN,B01CNMB6GE,B06XCLJGT2,B06XR12XBQ,B01BQT7PZQ,B06XBYLGSH,B01NBV9WCY,B01F8LGGWQ,B00P2MJD5O,B01BQT7QFU,B01L2YVKLU,B01FCUJAHG,B06W9FTBGM,B06XZ4VYFC,B06XZ56ZGR,B06XZ68VWB,B0725FPL12,B0154KWS9G,B06XVZ22NF,B06XZ7BTJB,B01FCUJ7WY,B06VWT6B1S,B071DM9RST,B071RGT9Q5,B01B2L50WS,B06XCSNW62,B01MXV5NGH,B019Q7714A,B01N0PA3T7,B01GDUOOO8,B071HL32PZ,B01FCUJBA2,B06Y2D75RV,B071Z53FRB,B06XPXYPRC,B06Y5QPY9D,B01DWT6LBC,B06XRCBCNZ,B01N7K2ZBR,B0182WVY5W,B071D9THR8,B06W57STWB,B0716GZMFC,B071V7RK1K,B01NCNF8Q1,B0711BD9KF,B06Y6MFWXL,B01ALD54HE,B06Y4F958L,B07236D4MP,B06XZ48CNS,B06XZ3FRGB,B01KVJDQXW,B06WWGHZHF,B0711BVZ6V,B06XR4RKKJ,B06XP5VGB7,B06Y4Z8RKT,B06XF9W3VX,B00SW0WS8C,B01FCUIY6O,B00J46XO9U,B06VTPMV1N,B01DOEPRQ0,B06Y1VGPCT,B07233G1KL,B0719JG7CX,B071D1C28M,B07199DLG9,B06WVD2NVT,B01NAL2VCV,B06Y6MDDRR,B01FL0WAW4,B07144W4PK,B071Z1D914,B06XFSZC1C,B06X9T9FSR,B071451NZ6,B0716QVXZR,B071HLQH7Z,B01MTUD3JW,B0716JHYZT,B06VY9LYQX,B01N43Z743,B01MTPERBZ,B01N6LPA9X,B01MT0MSBB,B071LJB31M,B06XPQ3XBT,B06XC258QB,B071VHQSCH,B071YY3SJK,B01BQT7OXY,B06XC3XQ11,B06XBHQP4L,B06XY1HG9G,B06X9V4D7C";
		System.out.println(info.getAsin());
	}
	
	@Test
	public void test5(){
		boolean emtpyTask = false;
		while (true) {
			if (emtpyTask)
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
				}

			emtpyTask = registerService.generateRegisterTask();
		}
	}

	@Test
	public void test4() {

		Date cursor = new Date();
		List<TargetSKU> skuList = productDao.getSkuList(cursor);
		Map<String, Set<String>> keywordMap = new HashMap<String, Set<String>>();
		while (!CollectionUtils.isEmpty(skuList)) {

			for (TargetSKU sku : skuList)
				for (String category : sku.getCategory())
					for (String keyword : sku.getKeywordList())
						if (CollectionUtils.isEmpty(keywordMap.get(category))
								|| !keywordMap.get(category).contains(keyword)) {
							System.out.println(keyword + category);
						}
			cursor = skuList.get(skuList.size() - 1).getCreateTime();
			skuList = productDao.getSkuList(cursor);

		}
	}

	@Test
	public void test2() {
		TargetSKU target = new TargetSKU();
		target.setCreateTime(new Date());
		target.setUpdateTime(new Date());
		target.setAsin("B01N0PA3T7");
		target.setProductName("H1-LC-FNB-GB");
		target.setCategory(Arrays.asList(new String[] { "All", "Electronics" }));
		target.setKeywordList(
				Arrays.asList(new String[] { "iphone cable", "iphone cables", "lightning cable", "lightning cables",
						"lightning cable black 3 pack", "lightning cable black", "lightning cable for iphone",
						"braided lightning cable", "iphone cable 3 pack", "braided iphone cable" }));

		productDao.insertTargetSKU(target);
	}
}
