package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 * 
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper tbSpecificationOptionMapper;

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Specification specification) {

		// 得到规则数据
		TbSpecification tbSpecification = specification.getSpecification();
		specificationMapper.insert(tbSpecification);

		// 得到规则选项数据
		List<TbSpecificationOption> specificationOptions = specification.getSpecificationOptions();
		if (specificationOptions != null) {
			for (TbSpecificationOption tbSpecificationOption : specificationOptions) {
				tbSpecificationOption.setSpecId(tbSpecification.getId());
				tbSpecificationOptionMapper.insert(tbSpecificationOption);
			}
		}
	}

	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification) {
		// 得到规则数据
		TbSpecification tbSpecification = specification.getSpecification();
		specificationMapper.updateByPrimaryKey(tbSpecification);

		//先删除规则选项数据
		TbSpecificationOptionExample example=new TbSpecificationOptionExample();
		example.createCriteria().andSpecIdEqualTo(tbSpecification.getId());
		tbSpecificationOptionMapper.deleteByExample(example);
		
		// 得到规则选项数据
		List<TbSpecificationOption> specificationOptions = specification.getSpecificationOptions();
		for (TbSpecificationOption tbSpecificationOption : specificationOptions) {
			tbSpecificationOption.setSpecId(tbSpecification.getId());
			tbSpecificationOptionMapper.insert(tbSpecificationOption);
		}
	}

	/**
	 * 根据ID获取实体
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id) {
		Specification specification = new Specification();
		// 添加规格信息
		specification.setSpecification(specificationMapper.selectByPrimaryKey(id));
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		example.createCriteria().andSpecIdEqualTo(id);
		// 添加规格选项信息
		List<TbSpecificationOption> specificationOptions = tbSpecificationOptionMapper.selectByExample(example);
		specification.setSpecificationOptions(specificationOptions);

		return specification;// specificationMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for (Long id : ids) {
			specificationMapper.deleteByPrimaryKey(id);
			TbSpecificationOptionExample example=new TbSpecificationOptionExample();
			example.createCriteria().andSpecIdEqualTo(id);
			tbSpecificationOptionMapper.deleteByExample(example);
		}
	}

	@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);

		TbSpecificationExample example = new TbSpecificationExample();
		Criteria criteria = example.createCriteria();

		if (specification != null) {
			if (specification.getSpecName() != null && specification.getSpecName().length() > 0) {
				criteria.andSpecNameLike("%" + specification.getSpecName() + "%");
			}

		}

		Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectSpecList() {
		return specificationMapper.selectSpecList();
	}

}
