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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 支付
 *
 * @author lengleng
 * @date 2019-05-28 23:58:18
 */
@Data
@TableName("pay_trade_order")
@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "支付")
public class PayTradeOrder extends Model<PayTradeOrder> {

	private static final long serialVersionUID = 1L;

	/**
	 * 支付订单号
	 */
	@TableId(type = IdType.INPUT)
	private String orderId;

	/**
	 * 渠道ID
	 */
	private String channelId;

	/**
	 * 支付金额,单位元
	 */
	private String amount;

	/**
	 * 三位货币代码,人民币:cny
	 */
	private String currency;

	/**
	 * 支付状态,0-订单生成,1-支付中(目前未使用),2-支付成功,3-业务处理完成
	 */
	private String status;

	/**
	 * 客户端IP
	 */
	private String clientIp;

	/**
	 * 设备
	 */
	private String device;

	/**
	 * 商品标题
	 */
	private String subject;

	/**
	 * 商品描述信息
	 */
	private String body;

	/**
	 * 特定渠道发起时额外参数
	 */
	private String extra;

	/**
	 * 渠道商户ID
	 */
	private String channelMchId;

	/**
	 * 渠道订单号
	 */
	private String channelOrderNo;

	/**
	 * 渠道支付错误码
	 */
	private String errCode;

	/**
	 * 渠道支付错误描述
	 */
	private String errMsg;

	/**
	 * 扩展参数1
	 */
	private String param1;

	/**
	 * 扩展参数2
	 */
	private String param2;

	/**
	 * 通知地址
	 */
	private String notifyUrl;

	/**
	 * 通知次数
	 */
	private Integer notifyCount;

	/**
	 * 最后一次通知时间
	 */
	private Long lastNotifyTime;

	/**
	 * 订单失效时间
	 */
	private Long expireTime;

	/**
	 * 订单支付成功时间
	 */
	private Long paySuccTime;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;

	/**
	 * 删除标记
	 */
	@TableLogic
	private String delFlag;

	/**
	 * 租户ID
	 */
	private Integer tenantId;
	
	/**
	 * 用户Id
	 */
	private Integer userId;
	
	/**
	 * 收款码表主键
	 */
	private Integer codeId;

}
