package com.pinyougou.page.service.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.request.CollectionAdminRequest.Create;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class ItemPageServiceImpl implements ItemPageService {

	@Autowired
	private FreeMarkerConfig freeMarkerConfig;

	@Value("${pageDir}")
	private String pageDir;

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper descMapper;

	@Autowired
	private TbItemCatMapper catMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Override
	public boolean getItemHtml(Long goodsId) {
		try {
			Configuration configuration = freeMarkerConfig.getConfiguration();
			Template template = configuration.getTemplate("item.ftl");
			Map dataModel = new HashMap<>();
			// 1.加载商品表数据
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", goods);
			// 2.加载商品扩展表数据
			TbGoodsDesc goodsDesc = descMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goodsDesc", goodsDesc);
			// 3.加载三级分类信息
			String itemCat1 = catMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = catMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = catMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			dataModel.put("itemCat1", itemCat1);
			dataModel.put("itemCat2", itemCat2);
			dataModel.put("itemCat3", itemCat3);
			// 4.得到SKU的数据
			TbItemExample example = new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);// SKU id
			criteria.andStatusEqualTo("1");// 状态有效

			example.setOrderByClause("is_default desc");// 按是否默认字段 降序排序 保证第一个为默认
			List<TbItem> itemList = itemMapper.selectByExample(example);

			dataModel.put("itemList", itemList);

			Writer out = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(pageDir + goodsId + ".html"), "UTF-8"));
			template.process(dataModel, out);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	@Override
	public boolean delItemHtml(Long[] ids) {

		try {
			for (Long id : ids) {
				new File(pageDir + id + ".html").delete();
			}
			return true;
		} catch (Exception e) {

			e.printStackTrace();
			return false;
		}

	}

}
