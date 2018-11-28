package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.utlis.CookieUtil;

import entity.Result;

/**
 * 购物车Controller
 * 
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference(timeout = 6000)
	private CartService cartService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private HttpServletResponse response;

	/**
	 * 读取购物车
	 * 
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {

		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		// 从cookie中提出购物车
		String cookieValue = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (cookieValue == null || cookieValue.equals("")) {
			cookieValue = "[]";
		}
		List<Cart> cartList = JSON.parseArray(cookieValue, Cart.class);

		if (username.equals("anonymousUser")) {// 如果未登录

			return cartList;
		} else {// 如果已登录

			// 从redis提取购物车
			List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
			if (cartList.size() > 0)// 看是否合并过
			{
				System.out.println("执行合并购物车");

				// 得到合并后的购物车
				List<Cart> mergeCartList = cartService.mergeCartList(cartList, cartListFromRedis);
				// 清空cookie中的购物车
				CookieUtil.deleteCookie(request, response, "cartList");
				// 将合并后的数据存入redis
				cartService.saveCartListToRedis(username, mergeCartList);
				return mergeCartList;
			}
			return cartListFromRedis;
		}

	}

	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")//注解方式使用CORS跨域
	public Result addGoodsToCartList(Long itemId, Integer num) {
		//解决跨域问题
		//response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//可以访问的域 *代表全部域(此方法不需要操作cookie)
		//response.setHeader("Access-Control-Allow-Credentials", "true");//允许使用cookie  如果操作cookie必须加上这句话  上面不允许写成*
	
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前的登录人" + username);
		try {
			// 得到购物车对象
			List<Cart> cartList = findCartList();
			// 调用服务操作购物车
			cartList = cartService.addCart(cartList, itemId, num);
			if (username.equals("anonymousUser")) {// 如果未登录
				// 将新的购物车列表存入cookie
				CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");

			} else {// 如果登录

				// 将购物车放入redis中
				cartService.saveCartListToRedis(username, cartList);
			}
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}
}
