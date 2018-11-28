package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

/**
 * 品牌管理Service
 * 
 * @author Administrator
 *
 */
@Service
@Transactional
public class BrandServiceImpl implements BrandService {

	@Autowired
	private TbBrandMapper brandMapper;

	@Override
	public List<TbBrand> findAllBrand() {
		List<TbBrand> list = brandMapper.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		// pageHelp分页插件
		PageHelper.startPage(pageNum, pageSize);
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);

		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void insertBrand(TbBrand brand) {
		brandMapper.insert(brand);
	}

	@Override
	public TbBrand findBrandById(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}

	@Override
	public void updateBrand(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);
	}

	@Override
	public void deleteBrandById(Long[] ids) {
		for (Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
	}

	@Override
	public PageResult findPage(TbBrand brand, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		TbBrandExample example = new TbBrandExample();
		if (brand != null) {
			if (brand.getName() != null && brand.getName().length() > 0) {
				example.createCriteria().andNameLike("%" + brand.getName() + "%");
			}
			if (brand.getFirstChar() != null && brand.getFirstChar().length() > 0) {
				example.createCriteria().andFirstCharLike("%" + brand.getFirstChar() + "%");
			}
		}

		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}

}
