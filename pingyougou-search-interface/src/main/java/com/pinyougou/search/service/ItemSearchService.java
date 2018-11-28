package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

	/**
	 * 根据map封装的信息，查询索引库
	 * @param searchMap
	 * @return
	 */
	public Map search(Map searchMap);
	/**
	 * 导入列表
	 * @param list
	 */
	public void importList(List list);
	
	/**
	 *  删除数据
	 * @param goodsIds
	 */
	public void deleteGoodsByIds(List goodsIdList);
}
