package com.pinyougou.search.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;

@RestController
@RequestMapping("/itemsearch")
public class ItemSearchController {

	@Reference
	private ItemSearchService itemSearchService;

	@RequestMapping(value="/search",method=RequestMethod.POST)
	public Map search(@RequestBody Map searchMap) {
		return itemSearchService.search(searchMap);
	}
}
