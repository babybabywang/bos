package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {

	/**
	 *返回全部品牌列表 
	 * @return
	 */
	public List<TbBrand>findAllBrand();
	
	/**
	 * 返回分页结果
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	/**
	 * 添加品牌
	 * @param brand
	 */
	public void insertBrand(TbBrand brand);
	/**
	 * 根据id查询brand
	 * @param id
	 * @return
	 */
	public TbBrand findBrandById(Long id);
	/**
	 * 修改品牌
	 * @param brand
	 */
	public void updateBrand(TbBrand brand);
	/**
	 * 根据id删除品牌信息
	 * @param id
	 */
	public void deleteBrandById(Long [] ids);
	/**
	 * 根据条件查询
	 * @param brand
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public PageResult findPage(TbBrand brand,int pageNum,int pageSize);
	/**
	 * 查询select2下拉列表
	 * @return
	 */
	public List<Map> selectOptionList();
}
