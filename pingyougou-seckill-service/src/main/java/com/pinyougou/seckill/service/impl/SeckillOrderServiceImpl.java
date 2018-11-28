package com.pinyougou.seckill.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.utlis.IdWorker;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service(timeout=5000)
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);
	}

	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id) {
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			seckillOrderMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSeckillOrderExample example = new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();

		if (seckillOrder != null) {
			if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
				criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
			}
			if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
				criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
			}
			if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
				criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
			}
			if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
				criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
			}
			if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
				criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
			}
			if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
				criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
			}
			if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
				criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
			}

		}

		Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbSeckillGoodsMapper goodsMapper;

	@Override
	public void submitOrder(Long seckillId, String userId) {
		// 1.查询商品
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoodsList").get(seckillId);
		if (seckillGoods == null) {
			throw new RuntimeException("商品不存在!");
		}
		if (seckillGoods.getStockCount() <= 0) {
			throw new RuntimeException("商品已经被抢光!");
		}
		// 2.减少库存
		seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);// 减库存
		redisTemplate.boundHashOps("seckillGoodsList").put(seckillId, seckillGoods);// 存入缓存
		if (seckillGoods.getStockCount() == 0) {
			// 商品同步到数据库
			goodsMapper.updateByPrimaryKey(seckillGoods);// 更新数据库
			redisTemplate.boundHashOps("seckillGoodsList").delete(seckillId);
			System.out.println("将商品同步到数据库....");
		}
		// 3.存储秒杀订单（redis）
		TbSeckillOrder order = new TbSeckillOrder();
		order.setId(idWorker.nextId());// 流水id
		order.setSeckillId(seckillId);
		order.setUserId(userId);
		order.setMoney(seckillGoods.getCostPrice());// 金额
		order.setSellerId(seckillGoods.getSellerId());// 商家id
		order.setCreateTime(new Date());// 下单日期
		order.setStatus("0");// 状态为支付

		redisTemplate.boundHashOps("seckillOrder").put(userId, order);
		System.out.println("保存订单成功(redis)");
	}

	@Override
	public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {

		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDB(String userId, Long orderId, String transactionId) {
		System.out.println("saveOrderFromRedisToDb:" + userId);
		// 1.提取redis中的订单信息
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if (seckillOrder == null) {
			throw new RuntimeException("不存在该订单");
		}
		if (!seckillOrder.getId().equals(orderId)) {
			throw new RuntimeException("订单号错误");
		}
		// 2.修改订单实体的属性

		seckillOrder.setStatus("1");// 支付成功
		seckillOrder.setPayTime(new Date());// 支付日期
		seckillOrder.setTransactionId(transactionId);
		// 3.将订单存入数据库
		seckillOrderMapper.insert(seckillOrder);

		redisTemplate.boundHashOps("seckillOrder").delete(userId);// 情况缓存中订单信息

	}

	@Override
	public void deteleOrderFromRedis(String userId, Long orderId) {
		// 查询中缓存中的订单
		TbSeckillOrder seckillOrder = searchOrderFromRedisByUserId(userId);
		if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
			redisTemplate.boundHashOps("seckillOrder").delete(userId);// 情况缓存中订单信息
		}
		// 库存回退
		TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoodsList")
				.get(seckillOrder.getSeckillId());
		if (seckillGoods != null) {
			seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
			redisTemplate.boundHashOps("seckillGoodsList").put(seckillOrder.getSeckillId(), seckillGoods);
		} else {
			seckillGoods = new TbSeckillGoods();
			seckillGoods.setId(seckillOrder.getSeckillId());
			// 属性要设置
			seckillGoods.setStockCount(1);
			redisTemplate.boundHashOps("seckillGoodsList").put(seckillOrder.getSeckillId(), seckillGoods);
		}
		System.out.println("订单取消");
	}

}
