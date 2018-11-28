package com.pinyougou.page.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

@Component
public class GenHtmlListener implements MessageListener {

	@Autowired
	private ItemPageService itemPageService;

	@Override
	public void onMessage(Message message) {
		TextMessage textMessage = (TextMessage) message;
		try {
			String goodIds = textMessage.getText();
			System.out.println("接收到消息：" + goodIds);
			if (goodIds != "" && goodIds != null) {
				// 生成页面信息
				boolean b = itemPageService.getItemHtml(new Long(goodIds));
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
