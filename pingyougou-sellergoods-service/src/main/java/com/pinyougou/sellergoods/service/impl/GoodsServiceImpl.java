package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper descMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemCatMapper catMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");// 设置未申请状态
		goodsMapper.insert(goods.getGoods());
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());// 设置商品id
		// 插入商品详细表
		descMapper.insert(goods.getGoodsDesc());
		saveItemList(goods);
		
	}

	private void saveItemList(Goods goods){
		if ("1".equals(goods.getGoods().getIsEnableSpec())) {
			List<TbItem> itemList = goods.getItemList();
			for (TbItem tbItem : itemList) {
				// 构建标题 SPU名称+规格选项值
				String title = goods.getGoods().getGoodsName();// SUP名称
				Map<String, Object> specMap = JSON.parseObject(tbItem.getSpec());
				for (String key : specMap.keySet()) {
					title += " " + specMap.get(key);
				}
				tbItem.setTitle(title);
				setItemValues(tbItem, goods);
				itemMapper.insert(tbItem);
			}
		} else {
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());
			item.setPrice(goods.getGoods().getPrice());
			item.setNum(99999);// 库存数量
			item.setStatus("1");// 状态
			item.setIsDefault("1");
			setItemValues(item, goods);
			itemMapper.insert(item);
		}
	}
	private void setItemValues(TbItem tbItem, Goods goods) {
		// 商品分类
		tbItem.setCategoryid(goods.getGoods().getCategory3Id());
		// 更新日期
		tbItem.setCreateTime(new Date());
		tbItem.setUpdateTime(new Date());

		tbItem.setGoodsId(goods.getGoods().getId());
		tbItem.setSellerId(goods.getGoods().getSellerId());// 商家id

		TbItemCat itemCat = catMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		tbItem.setCategory(itemCat.getName());

		// 品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		tbItem.setBrand(brand.getName());

		// 商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		tbItem.setSeller(seller.getNickName());
		// 图片地址（取spu的第一个图片）
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if (imageList.size() > 0) {
			tbItem.setImage((String) imageList.get(0).get("url"));
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods) {
		goods.getGoods().setAuditStatus("0");//设置未申请状态:如果是经过修改的商品，需要重新设置状态
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		descMapper.updateByPrimaryKey(goods.getGoodsDesc());
		TbItemExample example=new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(goods.getGoods().getId());
		//删除原有的sku列表数据		
		itemMapper.deleteByExample(example);
		saveItemList(goods);
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id) {
		//生成一个goods组合对象
		Goods goods=new Goods();
		//将goods封装进去
		goods.setGoods(goodsMapper.selectByPrimaryKey(id));
		//将goodsdesc封装进去
		goods.setGoodsDesc(descMapper.selectByPrimaryKey(id));
		//将itemList封装进去
		TbItemExample example=new TbItemExample();
		example.createCriteria().andGoodsIdEqualTo(id);
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");//表示逻辑删除
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbGoodsExample example = new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		//指定条件未逻辑删除
		criteria.andIsDeleteIsNull();
		if (goods != null) {
			if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}
			if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
				criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
			}
			if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
				criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
			}
			if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
				criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
			}
			if (goods.getCaption() != null && goods.getCaption().length() > 0) {
				criteria.andCaptionLike("%" + goods.getCaption() + "%");
			}
			if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
				criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
			}
			if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
				criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
			}
			if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
				criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
			}

		}

		Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
		TbItemExample example=new TbItemExample();
		example.createCriteria().andStatusEqualTo(status);//状态
		example.createCriteria().andGoodsIdIn(Arrays.asList(goodsIds));
		
		List<TbItem> list = itemMapper.selectByExample(example);
		return list;
	}
	

}
