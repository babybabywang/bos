package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Field;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;

	@Override
	public Map search(Map searchMap) {
		// 用于存储返回数据的 map
		Map map = new HashMap<>();
		/*
		 * Query query=new SimpleQuery("*:*"); Criteria criteria=new
		 * Criteria("item_keywords").is(searchMap.get("keywords")); //添加查询条件
		 * query.addCriteria(criteria); ScoredPage<TbItem> page =
		 * solrTemplate.queryForPage(query, TbItem.class);
		 * 
		 * map.put("rows", page.getContent());
		 */

		// 空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		/**
		 * 增加代码可读性
		 */
		// 将返回的map集合追加到map集合中
		map.putAll(searchList(searchMap));

		// 2.分组查询 商品分类列表
		List<String> catagoryList = searchCategoryList(searchMap);
		map.put("catagoryList", catagoryList);
		// 3.查询品牌和规格列表

		String category = (String) searchMap.get("category");
		if (!category.equals("")) {
			map.putAll(searchBrandAndSpecList(category));
		} else {
			if (catagoryList.size() > 0) {
				map.putAll(searchBrandAndSpecList(catagoryList.get(0)));
			}
		}
		return map;
	}

	// 查询列表方法
	private Map searchList(Map searchMap) {

		// 用于存储返回数据的 map
		Map map = new HashMap<>();
		// 高亮显示

		HighlightQuery query = new SimpleHighlightQuery();

		// 构建高亮选项对象
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");// 设置那个域高亮显示
		highlightOptions.setSimplePrefix("<em style='color:red'>");// 设置前缀
		highlightOptions.setSimplePostfix("</em>");// 后缀

		// 1.1关键字查询
		query.setHighlightOptions(highlightOptions);// 为查询对象设置高亮选项
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		// 添加查询条件
		query.addCriteria(criteria);

		// 1.2按照商品分类进行过滤
		if (!"".equals(searchMap.get("category"))) {
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		// 1.3按照品牌过滤
		if (!"".equals(searchMap.get("brand"))) {
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}

		// 1.4按照规格过滤
		if (searchMap.get("spec") != null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}

		// 1.5价格范围过滤
		if (!"".equals(searchMap.get("price"))) {
			String priceStr = (String) searchMap.get("price");
			String[] price = priceStr.split("-");
			if (!price[0].equals("0")) {// 如果最低加个不等于0
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if (!price[1].equals("*")) {// 如果最高价格不等于*
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}

		}
		// 1.6分页
		Integer pageNo = (Integer) searchMap.get("pageNo");
		if (pageNo == null) {
			pageNo = 1;// 默认第一页
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if (pageSize == null) {
			pageSize = 20;// 默认20
		}
		query.setOffset((pageNo - 1) * pageSize);// 起始索引
		query.setRows(pageSize);// 每页显示的条数

		// 1.7按照价格排序
		String sortValue = (String) searchMap.get("sort");
		String sortFiled = (String) searchMap.get("sortField");
		if (sortValue != null && sortFiled != "") {

			if (sortValue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortFiled);
				query.addSort(sort);
			}
			if (sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortFiled);
				query.addSort(sort);
			}
		}

		// *******获取高亮结果集
		// 高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

		// 开启高亮入口
		List<HighlightEntry<TbItem>> highlighted = page.getHighlighted();
		// 循环高亮入口集合
		for (HighlightEntry<TbItem> h : highlighted) {
			List<Highlight> highlights = h.getHighlights();
			if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
				TbItem item = h.getEntity();// 得到引用实体
				item.setTitle(highlights.get(0).getSnipplets().get(0));// 将高亮的值设置到实体的标题中
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());// 返回总页数
		map.put("total", page.getTotalElements());// 返回的总记录数
		return map;
	}

	/**
	 * 分组查询(查询商品分类列表)
	 * 
	 * @return
	 */
	private List searchCategoryList(Map searchMap) {
		// 用户存储分组后的结果
		List<String> itemList = new ArrayList<>();

		Query query = new SimpleQuery("*:*");
		// 根据关键字查询 where
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		// 添加查询条件
		query.addCriteria(criteria);

		// 根据某个域进行分组
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");// group
																						// by
		query.setGroupOptions(groupOptions);

		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);

		// 获取分组结果对象
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		// 获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		// 获取分组入口集合
		List<GroupEntry<TbItem>> entityList = groupEntries.getContent();
		for (GroupEntry<TbItem> entity : entityList) {
			itemList.add(entity.getGroupValue());// 将分组域后的结果添加到返回的集合中

		}
		return itemList;
	}

	@Autowired
	private RedisTemplate redisTemplate;

	/**
	 * 查询品牌和规格列表
	 * 
	 * @param category
	 *            分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap();
		Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (templateId != null) {
			// 根据模板ID查询品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);
			// 根据模板ID查询规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);
		}
		return map;
	}

	@Override
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	@Override
	public void deleteGoodsByIds(List goodsIdList) {
		System.out.println("删除商品ID" + goodsIdList);
		Query query = new SimpleQuery();
		Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
