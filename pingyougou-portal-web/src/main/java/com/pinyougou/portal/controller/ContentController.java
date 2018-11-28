package com.pinyougou.portal.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
/**
 * 前台广告controller
 * @author Administrator
 *
 */

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.pojo.TbContent;
@RestController
@RequestMapping("/content")
public class ContentController {

	@Reference
	private ContentService contentService;
	
	/**
	 * 根据分类id查询前台广告页面
	 * @param categoryId
	 * @return
	 */
	@RequestMapping(value="/findByCategoryId/{categoryId}",method=RequestMethod.GET)
	public List<TbContent> findByCategoryId(@PathVariable Long categoryId)
	{
		List<TbContent> list = contentService.findListByCateId(categoryId);
		return list;
	}
}
