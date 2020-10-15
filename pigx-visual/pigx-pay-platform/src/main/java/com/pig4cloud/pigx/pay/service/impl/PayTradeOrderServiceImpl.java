/*
 *    Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the name of the pig4cloud.com developer nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * Author: lengleng (wangiegie@gmail.com)
 */
package com.pig4cloud.pigx.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import com.pig4cloud.pigx.common.sequence.sequence.Sequence;
import com.pig4cloud.pigx.pay.entity.PayTradeOrder;
import com.pig4cloud.pigx.pay.entity.XPayTradeOrder;
import com.pig4cloud.pigx.pay.mapper.PayTradeOrderMapper;
import com.pig4cloud.pigx.pay.service.PayTradeOrderService;
import com.pig4cloud.pigx.pay.utils.OrderStatusEnum;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付
 *
 * @author lengleng
 * @date 2019-05-28 23:58:18
 */
@Service
public class PayTradeOrderServiceImpl extends ServiceImpl<PayTradeOrderMapper, PayTradeOrder>
		implements PayTradeOrderService {
	
	@Autowired
	private PayTradeOrderMapper payTradeOrderMapper;
	
	@Autowired
	private Sequence paySequence;

	@Override
	public PayTradeOrder saveOrder(XPayTradeOrder xPayTradeOrder) {
		PayTradeOrder payTradeOrder = new PayTradeOrder();
		payTradeOrder.setOrderId(paySequence.nextNo());
		payTradeOrder.setStatus(OrderStatusEnum.INIT.getStatus());
		payTradeOrder.setAmount(xPayTradeOrder.getAmount());
		payTradeOrder.setChannelId(xPayTradeOrder.getPayType());
		payTradeOrder.setDevice(xPayTradeOrder.getDevice());
		payTradeOrder.setBody(xPayTradeOrder.getDesc());
		payTradeOrder.setParam1(xPayTradeOrder.getNickName());
		payTradeOrder.setParam2(xPayTradeOrder.getEmail());
		payTradeOrder.setClientIp(xPayTradeOrder.getIp());
		payTradeOrder.setCurrency(xPayTradeOrder.getPayType().equals("PayPal") ? "usd" : "cny");
		payTradeOrder.setUserId(1);
		if(StringUtils.isNotBlank(xPayTradeOrder.getCodeId())){
			payTradeOrder.setCodeId(Integer.valueOf(xPayTradeOrder.getCodeId()));
		}
		payTradeOrderMapper.insert(payTradeOrder);
		return payTradeOrder;
	}

}
