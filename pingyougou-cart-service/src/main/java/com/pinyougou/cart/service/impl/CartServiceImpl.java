package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<Cart> addCart(List<Cart> cartList, Long itemId, Integer num) {
		// 1.根据商品SKU ID查询SKU商品信息
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item == null) {
			throw new RuntimeException("商品不存在");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态无效");
		}
		// 2.获取商家ID

		String sellerId = item.getSellerId();// 商家ID

		// 3.根据商家ID判断购物车列表中是否存在该商家的购物车
		Cart cart = searchCartBySellerId(cartList, sellerId);
		// 4.如果购物车列表中不存在该商家的购物车
		if (cart == null) {
			// 4.1 新建购物车对象
			cart = new Cart();
			cart.setSellerId(sellerId);// 商家ID
			cart.setSellerName(item.getSeller());// 商家名称
			// 创建一个购物车详细列表
			List<TbOrderItem> orderItemList = new ArrayList<>();
			// 创建一个订单详细对象
			TbOrderItem orderItem = createOrderItem(item, num);
			// 添加到购物车详细列表中
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			// 4.2 将新建的购物车对象添加到购物车列表
			cartList.add(cart);
		} else {
			// 5.如果购物车列表中存在该商家的购物车
			// 查询购物车明细列表中是否存在该商品
			TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
			// 5.1. 如果没有，新增购物车明细
			if (orderItem == null) {
				// 创建新的定线详情对象
				TbOrderItem createOrderItem = createOrderItem(item, num);
				cart.getOrderItemList().add(createOrderItem);
			} else {
				// 5.2. 如果有，在原购物车明细上添加数量，更改金额
				orderItem.setNum(orderItem.getNum() + num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getNum() * orderItem.getPrice().doubleValue()));
				// 如果数量操作后小于等于0，则移除
				if (orderItem.getNum() <= 0) {
					cart.getOrderItemList().remove(orderItem);// 移除购物车明细
				}
				// 如果移除后cart的明细数量为0，则将cart移除
				if (cart.getOrderItemList().size() == 0) {
					cartList.remove(cart);
				}
			}

		}
		return cartList;
	}

	/**
	 * 查询购物车对象
	 * 
	 * @param cartList
	 * @param sellerId
	 * @return
	 */
	public Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;

	}

	/**
	 * 生成订单详细对象
	 * 
	 * @param item
	 * @param num
	 * @return
	 */
	public TbOrderItem createOrderItem(TbItem item, Integer num) {
		if (num <= 0) {
			throw new RuntimeException("数量非法");
		}

		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
		return orderItem;
	}

	/**
	 * 查询订单详情对象
	 * 
	 * @param orderItemList
	 * @param itemId
	 * @return
	 */
	public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
		for (TbOrderItem tbOrderItem : orderItemList) {
			if (tbOrderItem.getItemId().longValue() == itemId.longValue()) {
				return tbOrderItem;
			}
		}
		return null;
	}

	@Override
	public List<Cart> findCartListFromRedis(String username) {
		System.out.println("从redis中提取购物车数据....." + username);
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		if (cartList == null) {
			cartList = new ArrayList<>();
		}
		return cartList;
	}

	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		System.out.println("向redis存入购物车数据....." + username);
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		System.out.println("合并购物车");
		
		for (Cart cart : cartList2) {
			for(TbOrderItem orderItem:cart.getOrderItemList()){
				cartList1=addCart(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList1;
	}

}
