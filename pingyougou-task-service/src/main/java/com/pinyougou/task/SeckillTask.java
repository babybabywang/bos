package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

/**
 * 任务调度
 * 
 * @author Administrator
 *
 */
@Component
public class SeckillTask {

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 刷新秒杀商品(每分钟更新缓存中的秒杀商品信息)
	 */
	@Scheduled(cron = "0 * * * * ?")
	public void refreshSeckillGoods() {
		System.out.println("执行了任务调度" + new Date());

		// 查询缓存中的秒杀商品ID集合
		List keys = new ArrayList<>(redisTemplate.boundHashOps("seckillGoodsList").keys());
		System.out.println(keys);
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");// 审核通过的商品
		criteria.andStockCountGreaterThan(0);// 库存数大于0
		criteria.andStartTimeLessThanOrEqualTo(new Date());// 开始日期小于等于当前日期
		criteria.andEndTimeGreaterThanOrEqualTo(new Date());// 截至日期大于等于当前日期

		if (keys.size() > 0) {
			criteria.andIdNotIn(keys);// 排除缓存中已经存在的ID
		}
		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
		// 将列表数据存入缓存
		for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoodsList").put(tbSeckillGoods.getId(), tbSeckillGoods);
			System.out.println("增量更新秒杀商品ID:" + tbSeckillGoods.getId());
		}

		System.out.println("将" + seckillGoodsList.size() + "条商品装入缓存");
	}

	@Scheduled(cron = "* * * * * ?")
	public void removeSeckillGoods() {
		// 查询出缓存中的数据,扫描每秒记录，判断时间，如果当前时间超过截至时间，移除秒杀商品
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoodsList").values();

		System.out.println("执行了清除秒杀商品的任务"+new Date());
		for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
			if (tbSeckillGoods.getEndTime().getTime() < (new Date()).getTime()) {// 如果结束日期小于当前日期，则表示过期
				//将商品同步到数据库中
				seckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
				//清除缓存
				redisTemplate.boundHashOps("seckillGoodsList").delete(tbSeckillGoods.getId());//移除缓存数据
				System.out.println("移除秒杀商品"+tbSeckillGoods.getId());
				
			}
		}
		System.out.println("移除秒杀商品任务结束");		
	}
}
