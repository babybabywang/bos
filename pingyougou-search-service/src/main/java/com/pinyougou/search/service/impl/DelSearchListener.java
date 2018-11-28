package com.pinyougou.search.service.impl;

import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;

@Component
public class DelSearchListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {
		ObjectMessage objectMessage= (ObjectMessage) message;
		try {
			System.out.println("监听删除信息");
			itemSearchService.deleteGoodsByIds(Arrays.asList(objectMessage.getObject()));
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
