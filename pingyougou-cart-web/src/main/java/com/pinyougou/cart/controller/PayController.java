package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.utlis.IdWorker;

import entity.Result;

/**
 * 支付控制层
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeiXinPayService weiXinPayService;

	@Reference
	private OrderService orderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		//1.获取当前的登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//2.从redis中提取支付日志
		TbPayLog payLog = orderService.searchPayLogFromRedis(username);
		if(payLog!=null){
			
			return weiXinPayService.createNative(payLog.getOutTradeNo() + "", payLog.getTotalFee()+"");
		}
		return new HashMap<>();
	}

	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		Result result = null;
		int x=0;
		while (true) {
			Map<String, String> map = weiXinPayService.queryPayStatus(out_trade_no);
			if (map == null) {// 支付出错
				result = new Result(false, "支付出错");
				break;
			}
			if (map.get("trade_state").equals("SUCCESS")) {
				result = new Result(true, "支付成功");
				//修改订单状态
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				break;
			}
			
			try {
				Thread.sleep(3000);//间隔三秒
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//防止后端一直重复循环查询订单状态
			x++;
			if (x>=100) {
				result=new Result(false, "时间超时,二维码失效");
				break;
			}
		}
		return result;
	}
}
