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

package com.pig4cloud.pigx.pay.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.PayPalRESTException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.annotation.Inner;
import com.pig4cloud.pigx.pay.config.PaypalPaymentIntent;
import com.pig4cloud.pigx.pay.config.PaypalPaymentMethod;
import com.pig4cloud.pigx.pay.entity.CollectionCode;
import com.pig4cloud.pigx.pay.entity.PayTradeOrder;
import com.pig4cloud.pigx.pay.entity.PayTradeOrderEntity;
import com.pig4cloud.pigx.pay.entity.XPayTradeOrder;
import com.pig4cloud.pigx.pay.service.CollectionCodeService;
import com.pig4cloud.pigx.pay.service.PayTradeOrderService;
import com.pig4cloud.pigx.pay.service.PaypalService;
import com.pig4cloud.pigx.pay.utils.IpInfoUtils;
import com.pig4cloud.pigx.pay.utils.OrderStatusEnum;

import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 支付
 *
 * @author lengleng
 * @date 2019-05-28 23:58:18
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/order")
@Api(value = "order", tags = "订单")
public class PayTradeOrderController {

	private final PayTradeOrderService payTradeOrderService;
	
	private final StringRedisTemplate redisTemplate;
	
    private final PaypalService paypalService;
    
    private final  CollectionCodeService collectionCodeService;
	
    private static final Long IP_EXPIRE = 2L;
	
	private static final String CLOSE_KEY="XPAY_CLOSE_KEY";
	
	public static final String PAYPAL_SUCCESS_URL = "/success";
    public static final String PAYPAL_CANCEL_URL = "/cancel";

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param payTradeOrder 支付
	 * @return
	 */
    @Inner(false)
	@GetMapping("/page")
	public R getPayTradeOrderPage(Page page, PayTradeOrder payTradeOrder) {
		return R.ok(payTradeOrderService.page(page, Wrappers.query(payTradeOrder)));
	}

	/**
	 * 通过id查询支付
	 * @param orderId id
	 * @return R
	 */
    @Inner(false)
	@GetMapping("/{orderId}")
	public R getById(@PathVariable("orderId") String orderId) {
		return R.ok(payTradeOrderService.getById(orderId));
	}
	
	/**
	 * 新增支付
	 * @param payTradeOrder 支付
	 * @return R
	 */
    @Inner(false)
	@SysLog("新增支付")
	@PostMapping("/add")
	public R xpaySave(@RequestBody XPayTradeOrder xPayTradeOrder, HttpServletRequest request) {
        //判断是否开启支付
        String isOpen = redisTemplate.opsForValue().get(CLOSE_KEY);
        Long expireOpen = redisTemplate.getExpire(CLOSE_KEY, TimeUnit.HOURS);
        if(StringUtils.isNotBlank(isOpen)){
            String msg = "";
            if(expireOpen<0){
                msg = "系统暂时关闭，如有疑问请进行反馈";
            }else{
                msg = "暂停支付，剩余"+expireOpen+"小时后开放";
            }
            return R.failed(msg);
        }
        //防炸库验证
        String ip= IpInfoUtils.getIpAddr(request);
        if("0:0:0:0:0:0:0:1".equals(ip)){
            ip="127.0.0.1";
        }
        String temp=redisTemplate.opsForValue().get(ip);
        Long expire = redisTemplate.getExpire(ip,TimeUnit.SECONDS);
        if(StringUtils.isNotBlank(temp)){
            //return R.failed("您提交的太频繁啦，请"+expire+"秒后再试");
        }
        
        PayTradeOrderEntity payTradeOrderEntity = new PayTradeOrderEntity();
        //微信支付
        if(xPayTradeOrder.getPayType().equals("Wechat")){
        	Map<String, Object> columnMap = new HashMap<String, Object>();
        	columnMap.put("amount", xPayTradeOrder.getAmount());
        	columnMap.put("status", "0");
			List<CollectionCode> list = collectionCodeService.listByMap(columnMap);
			if(list.isEmpty()){
				return R.failed("系统繁忙，请稍后支付或使用其他支付方式");
			}
			CollectionCode collectionCode = list.get(0);
			collectionCode.setStatus("1");
			collectionCodeService.updateById(collectionCode);
			xPayTradeOrder.setCodeId(collectionCode.getId());
			payTradeOrderEntity.setRemark(collectionCode.getRemark());
			payTradeOrderEntity.setWechatUrl(collectionCode.getWechatUrl());
        }
        //生成支付订单
        xPayTradeOrder.setIp(ip);
        PayTradeOrder payTradeOrder = payTradeOrderService.saveOrder(xPayTradeOrder);
        payTradeOrderEntity.setPayTradeOrder(payTradeOrder);
		//记录缓存
        //redisTemplate.opsForValue().set(ip,"added",IP_EXPIRE, TimeUnit.MINUTES);
		return R.ok(payTradeOrderEntity);
	}
	
	/**
	 * 扫码改变状态
	 * @param request
	 */
	@Inner(false)
    @RequestMapping(method = RequestMethod.GET, value = "/openAlipay.html")
    public void scan(HttpServletRequest request){
		String id = request.getParameter("id");
		if(StringUtils.isNotBlank(id)){
			PayTradeOrder payTradeOrder = payTradeOrderService.getById(id);
			if(payTradeOrder != null && StringUtils.isNotBlank(payTradeOrder.getStatus()) 
					&& payTradeOrder.getStatus().equals(OrderStatusEnum.INIT.getStatus())){
				payTradeOrder.setStatus(OrderStatusEnum.SCANED.getStatus());
	        	payTradeOrder.setUpdateTime(LocalDateTime.now());
	        	payTradeOrderService.updateById(payTradeOrder);
			}
		}
    }
	
	@Inner(false)
	@SysLog("支付宝支付回调")
	@RequestMapping(method = RequestMethod.GET, value = "/payCallback")
	public R payCallback(HttpServletRequest request) {
		//更新支付结果
		String orderId = request.getParameter("orderId");
		if(StringUtils.isNotBlank(orderId)){
			PayTradeOrder payTradeOrder = payTradeOrderService.getById(orderId);
			if(payTradeOrder == null){
				return R.failed("订单不存在");
			}
			if(payTradeOrder.getStatus().equals(OrderStatusEnum.SUCCESS.getStatus())){
				return R.failed("订单已支付成功");
			}
			payTradeOrder.setStatus(OrderStatusEnum.SUCCESS.getStatus());
	    	payTradeOrder.setUpdateTime(LocalDateTime.now());
	    	boolean flag = payTradeOrderService.updateById(payTradeOrder);
	    	log.info("支付宝支付回调:{}",payTradeOrder);
			return R.ok(flag);
		}else{
			return R.failed("订单号不能为空");
		}
	}
	
	@Inner(false)
	@SysLog("微信支付回调")
	@RequestMapping(method = RequestMethod.GET, value = "/WechatPayBack")
	public R WechatPayBack(HttpServletRequest request) {
		//更新支付结果
		String remark = request.getParameter("remark");
		if(StringUtils.isBlank(remark)){
			return R.failed("备注不能为空");
		}
		String amount = request.getParameter("amount");
		if(StringUtils.isBlank(amount)){
			return R.failed("金额不能为空");
		}
		//获取二维码主键
		Map<String, Object> listMap = new HashMap<String, Object>();
		listMap.put("amount", amount);
		listMap.put("remark", remark);
		List<CollectionCode> codeList = collectionCodeService.listByMap(listMap);
		if(codeList.isEmpty()){
			return R.failed("收款二维码不存在");
		}
		if(codeList.size() > 1){
			return R.failed("收款二维码重复");
		}
		String codeId = codeList.get(0).getId();
		
		Map<String, Object> columnMap = new HashMap<String, Object>();
    	columnMap.put("code_id", codeId);
    	columnMap.put("channel_id", "Wechat");
    	columnMap.put("status", OrderStatusEnum.INIT.getStatus());
		List<PayTradeOrder> list = payTradeOrderService.listByMap(columnMap);
		if(list.isEmpty()){
			return R.failed("订单已支付或收款二维码已过期");
		}
		if(list.size() > 1){
			return R.failed("订单错误");
		}
		
		PayTradeOrder payTradeOrder = list.get(0);
		payTradeOrder.setStatus(OrderStatusEnum.SUCCESS.getStatus());
    	payTradeOrder.setUpdateTime(LocalDateTime.now());
    	payTradeOrderService.updateById(payTradeOrder);
    	
    	log.info("微信支付回调:{}",payTradeOrder);
    	
    	CollectionCode code = collectionCodeService.getById(codeId);
		code.setStatus("0");
		return R.ok(collectionCodeService.updateById(code));
	}
	
	/**
	 * 支付倒计时结束，未支付订单状态改为支付失败
	 * @param orderId
	 * @return
	 */
	@Inner(false)
	@PostMapping("/updateStatus/{orderId}")
	public void updateStatus(@PathVariable("orderId") String orderId) {
		PayTradeOrder payTradeOrder = payTradeOrderService.getById(orderId);
		if(payTradeOrder.getStatus().equals(OrderStatusEnum.INIT.getStatus())){
			payTradeOrder.setStatus(OrderStatusEnum.FAIL.getStatus());
			payTradeOrder.setUpdateTime(LocalDateTime.now());
			payTradeOrderService.updateById(payTradeOrder);
			
			if(payTradeOrder.getChannelId().equals("Wechat")){
				Integer codeId = payTradeOrder.getCodeId();
				CollectionCode code = collectionCodeService.getById(codeId);
				code.setStatus("0");
				collectionCodeService.updateById(code);
			}
		}
	}
		
	/**
	 * paypal跳转
	 * @param payTradeOrder
	 * @param request
	 * @return
	 */
	@Inner(false)
	@RequestMapping(method = RequestMethod.POST, value = "/pay")
    public String pay(@RequestBody PayTradeOrder payTradeOrder,HttpServletRequest request){
        String cancelUrl = payTradeOrder.getExtra() + "/pay/order" + PAYPAL_CANCEL_URL;
        String successUrl = payTradeOrder.getExtra() + "/pay/order" + PAYPAL_SUCCESS_URL;
        try {
            Payment payment = paypalService.createPayment(
            		Double.valueOf(payTradeOrder.getAmount()), 
                    "USD", 
                    PaypalPaymentMethod.paypal, 
                    PaypalPaymentIntent.sale,
                    payTradeOrder.getOrderId(), 
                    cancelUrl, 
                    successUrl);
            for(Links links : payment.getLinks()){
                if(links.getRel().equals("approval_url")){
                    return links.getHref();
                }
            }
        } catch (PayPalRESTException e) {
        	log.info("paypal跳转",e.getMessage());
        }
        return "error";
    }

	/**
	 * paypal成功回调
	 * @param paymentId
	 * @param payerId
	 * @return
	 */
	@Inner(false)
    @RequestMapping(method = RequestMethod.GET, value = PAYPAL_SUCCESS_URL)
    public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId){
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if(payment.getState().equals("approved")){
            	//获取订单Id
            	List<Transaction> list = payment.getTransactions();
            	Transaction transaction = list.get(0);
            	String orderId = transaction.getDescription();
            	//更新支付结果
            	PayTradeOrder payTradeOrder = payTradeOrderService.getById(orderId);
            	payTradeOrder.setStatus(OrderStatusEnum.SUCCESS.getStatus());
            	payTradeOrder.setUpdateTime(LocalDateTime.now());
            	payTradeOrderService.updateById(payTradeOrder);
                return "支付成功";
            }
        } catch (PayPalRESTException e) {
        	log.info("paypal成功回调",e.getMessage());
        }
        return "支付失败";
    }
	
	/**
	 * paypal取消回调
	 * @return
	 */
	@Inner(false)
    @RequestMapping(method = RequestMethod.GET, value = PAYPAL_CANCEL_URL)
    public String cancelPay(){
        return "取消支付";
    }
	
	@Inner(false)
	@RequestMapping(method = RequestMethod.GET, value = "/orderTask")
	public void orderTask(){
		//查询支付方式为微信，并且支付状态是下单的记录
		LocalDateTime now = LocalDateTime.now();
		Map<String, Object> columnMap = new HashMap<String, Object>();
    	columnMap.put("channel_id", "Wechat");
    	columnMap.put("status", OrderStatusEnum.INIT.getStatus());
		List<PayTradeOrder> list = payTradeOrderService.listByMap(columnMap);
		for(int i=0;i<list.size();i++){
			PayTradeOrder payTradeOrder = list.get(i);
			LocalDateTime createTime = payTradeOrder.getCreateTime();
			Duration duration = Duration.between(createTime,now);
			long millis = duration.toMillis()/1000;  //秒
			//订单创建时间大于90s,未支付，解除二维码占用
			if(millis > 90){
				payTradeOrder.setStatus(OrderStatusEnum.FAIL.getStatus());
				payTradeOrder.setUpdateTime(LocalDateTime.now());
				payTradeOrderService.updateById(payTradeOrder);
				
				Integer codeId = payTradeOrder.getCodeId();
				CollectionCode code = collectionCodeService.getById(codeId);
				code.setStatus("0");
				collectionCodeService.updateById(code);
			}
		}
	}

}
