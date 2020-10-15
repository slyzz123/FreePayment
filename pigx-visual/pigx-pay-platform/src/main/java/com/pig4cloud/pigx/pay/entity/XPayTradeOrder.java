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

package com.pig4cloud.pigx.pay.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class XPayTradeOrder extends Model<XPayTradeOrder> {

	private static final long serialVersionUID = 1L;

	/**
	 * 昵称
	 */
	private String nickName;

	/**
	 * 支付金额,单位元
	 */
	private String amount;

	/**
	 * 支付方式
	 */
	private String payType;

	/**
	 * 邮箱
	 */
	private String email;

	/**
	 * 留言
	 */
	private String desc;

	/**
	 * 支付设备是否为移动端
	 */
	private String mobile;

	/**
	 * 设备
	 */
	private String device;
	
	/**
	 * ip
	 */
	private String ip;
	
	/**
	 * codeId
	 */
	private String codeId;

}
