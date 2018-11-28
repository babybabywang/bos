package com.pinyougou.seckill.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeiXinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

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
	private SeckillOrderService seckillOrderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		//1.获取当前的登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//2.提取秒杀订单
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
		if(seckillOrder!=null){
			//将员装换为分
			return weiXinPayService.createNative(seckillOrder.getId() + "", (long)(seckillOrder.getMoney().doubleValue()*100)+"");
		}
		return new HashMap<>();
	}

	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		//1.获取当前的登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
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
				//保存订单状态
				seckillOrderService.saveOrderFromRedisToDB(username,Long.valueOf(out_trade_no), map.get("transaction_id"));
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
				
				//关闭支付订单
				Map<String,String> closePay = weiXinPayService.closePay(out_trade_no);
				if(closePay!=null&&"FAIL".equals(closePay.get("return_code"))){
					if("ORDERPAID".equals(closePay.get("err_code"))){
						result=new Result(false, "支付成功");
						//保存订单
						seckillOrderService.saveOrderFromRedisToDB(username,Long.valueOf(out_trade_no), map.get("transaction_id"));
					}
				}
				//删除订单
				if(result.isSuccess()==false){
					seckillOrderService.deteleOrderFromRedis(username, Long.valueOf(out_trade_no));
				}
				break;
			}
		}
		return result;
	}
}
