package com.pinyougou.pay.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.utlis.HttpClient;

@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {
	@Value("${appid}")
	private String appid;

	@Value("${partner}")
	private String partner;

	@Value("${notifyurl}")
	private String notify_url;

	@Value("${partnerkey}")
	private String partnerkey;

	/**
	 * 生成微信支付二维码
	 */
	public Map createNative(String out_trade_no, String total_fee) {
		// 1.参数封装
		Map param = new HashMap<>();
		param.put("appid", appid);// 公众帐号id
		param.put("mch_id", partner);// 商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		param.put("body", "pinyougou");// 商品描述
		param.put("out_trade_no", out_trade_no);// 商户订单号
		param.put("total_fee", total_fee);// 标价金额(分)
		param.put("spbill_create_ip", "127.0.0.1");// 终端IP
		param.put("notify_url", notify_url);// 通知地址
		param.put("trade_type", "NATIVE");// 交易类型

		try {
			// 2.生成要发送的xml
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("请求的参数:" + xmlParam);
			// 需要请求地址
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();// 发送post请求
			// 3.获得结果
			String xmlResult = httpClient.getContent();
			System.out.println("返回结果:" + xmlResult);
			// 将xml结果转换成map集合
			Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);

			// 新建一个map集合 用于存储生成支付二维码的连接地址
			Map map1 = new HashMap<>();
			map1.put("code_url", mapResult.get("code_url"));// 生成支付二维码的链接地址
			map1.put("out_trade_no", out_trade_no);
			map1.put("total_fee", total_fee);

			return map1;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	/**
	 * 查询支付订单状态
	 */
	@Override
	public Map queryPayStatus(String out_trade_no) {
		// 1.封装参数
		Map param = new HashMap<>();
		param.put("appid", appid);// 公众账号ID
		param.put("mch_id", partner);// 商户号
		param.put("out_trade_no", out_trade_no);// 商户订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串

		// 生成一个带前面的xml
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println("发送的请求:" + xmlParam);
			// 2.发送请求

			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			// 3.获取结果
			String xmlRelsut = httpClient.getContent();
			Map<String, String> reslut = WXPayUtil.xmlToMap(xmlRelsut);
			System.out.println("查询API返回结果" + xmlRelsut);
			return reslut;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}

	}

	@Override
	public Map closePay(String out_trade_no) {

		Map param = new HashMap();
		param.put("appid", appid);// 公众账号ID
		param.put("mch_id", partner);// 商户号
		param.put("out_trade_no", out_trade_no);// 订单号
		param.put("nonce_str", WXPayUtil.generateNonceStr());// 随机字符串
		String url = "https://api.mch.weixin.qq.com/pay/closeorder";
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			HttpClient client = new HttpClient(url);
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			String result = client.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(result);
			System.out.println(map);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
