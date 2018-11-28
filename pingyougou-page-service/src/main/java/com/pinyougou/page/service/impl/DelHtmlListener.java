package com.pinyougou.page.service.impl;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;

import com.pinyougou.page.service.ItemPageService;

public class DelHtmlListener implements MessageListener {

	@Autowired
	private ItemPageService itemPageService;
	@Override
	public void onMessage(Message msg) {
		ObjectMessage objectMessage=(ObjectMessage) msg;
		try {
			Long [] ids = (Long[]) objectMessage.getObject();
			System.out.println("ItemDeleteListener监听接收到消息..."+ids);
			boolean b = itemPageService.delItemHtml(ids);
			System.out.println("网页删除结果："+b);		
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
