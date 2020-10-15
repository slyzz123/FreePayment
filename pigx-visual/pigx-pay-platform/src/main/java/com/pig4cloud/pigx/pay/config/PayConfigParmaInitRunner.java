package com.pig4cloud.pigx.pay.config;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.SqlParser;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ijpay.alipay.AliPayApiConfig;
import com.ijpay.alipay.AliPayApiConfigKit;
import com.ijpay.wxpay.WxPayApiConfig;
import com.ijpay.wxpay.WxPayApiConfigKit;
import com.pig4cloud.pigx.admin.api.feign.RemoteTenantService;
import com.pig4cloud.pigx.common.core.constant.CacheConstants;
import com.pig4cloud.pigx.common.core.constant.CommonConstants;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.data.tenant.TenantBroker;
import com.pig4cloud.pigx.pay.entity.PayChannel;
import com.pig4cloud.pigx.pay.service.PayChannelService;
import com.pig4cloud.pigx.pay.utils.PayChannelNameEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lengleng
 * @date 2019-05-31
 * <p>
 * 支付参数初始化
 */
@Slf4j
@Configuration
@AllArgsConstructor
public class PayConfigParmaInitRunner {

	public static Map<String, WxMpService> mpServiceMap = new HashMap<>();

	private final PayChannelService channelService;

	private final RemoteTenantService tenantService;

	@Async
	@Order
	@SqlParser(filter = true)
	@EventListener({ WebServerInitializedEvent.class })
	public void initPayConfig() {

		List<PayChannel> channelList = new ArrayList<>();
		tenantService.list(SecurityConstants.FROM_IN).getData()
				.forEach(tenant -> TenantBroker.runAs(tenant.getId(), (id) -> {
					List<PayChannel> payChannelList = channelService.list(
							Wrappers.<PayChannel>lambdaQuery().eq(PayChannel::getState, CommonConstants.STATUS_NORMAL));
					channelList.addAll(payChannelList);
				}));

		channelList.forEach(channel -> {
			JSONObject params = JSONUtil.parseObj(channel.getParam());

			// 支付宝支付
			if (PayChannelNameEnum.ALIPAY_WAP.getName().equals(channel.getChannelId())) {

				AliPayApiConfig aliPayApiConfig = AliPayApiConfig.builder().setAppId(channel.getAppId())
						.setPrivateKey(params.getStr("privateKey")).setCharset(CharsetUtil.UTF_8)
						.setAliPayPublicKey(params.getStr("publicKey")).setServiceUrl(params.getStr("serviceUrl"))
						.setSignType("RSA2").build();

				AliPayApiConfigKit.putApiConfig(aliPayApiConfig);
				log.info("新增支付宝支付参数 {}", aliPayApiConfig);
			}

			// 微信支付
			if (PayChannelNameEnum.WEIXIN_MP.getName().equals(channel.getChannelId())) {
				WxPayApiConfig wx = WxPayApiConfig.builder().appId(channel.getAppId()).mchId(channel.getChannelMchId())
						.partnerKey(params.getStr("paternerKey")).build();

				String subMchId = params.getStr("subMchId");
				if (StrUtil.isNotBlank(subMchId)) {
					wx.setSlAppId(channel.getAppId());
					wx.setSlMchId(subMchId);
				}

				WxPayApiConfigKit.putApiConfig(wx);

				WxMpService wxMpService = new WxMpServiceImpl();
				WxMpDefaultConfigImpl storage = new WxMpDefaultConfigImpl();
				storage.setAppId(channel.getAppId());
				storage.setSecret(params.getStr("secret"));
				storage.setToken(params.getStr("token"));
				wxMpService.setWxMpConfigStorage(storage);

				mpServiceMap.put(channel.getAppId(), wxMpService);
				log.info("新增微信支付参数 {} {}", wx, wxMpService);
			}
		});
	}

	/**
	 * redis 监听配置,监听 pay_redis_route_reload_topic,重新加载配置
	 * @param redisConnectionFactory redis 配置
	 * @return
	 */
	@Bean
	public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener((message, bytes) -> {
			log.warn("接收到Redis 事件 重新加载支付参数事件");
			initPayConfig();
		}, new ChannelTopic(CacheConstants.PAY_REDIS_RELOAD_TOPIC));
		return container;
	}

}
