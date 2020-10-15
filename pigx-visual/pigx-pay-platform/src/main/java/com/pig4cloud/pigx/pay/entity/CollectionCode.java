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

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收款码表
 *
 * @author pigx code generator
 * @date 2020-09-17 14:13:49
 */
@Data
@TableName("collection_code")
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "收款码表")
public class CollectionCode extends Model<CollectionCode> {
private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @TableId
    private String id;
    /**
     * 金额
     */
    @ApiModelProperty(value="金额")
    private String amount;
    /**
     * 备注
     */
    @ApiModelProperty(value="备注")
    private String remark;
    /**
     * 支付短链接
     */
    @ApiModelProperty(value="支付短链接")
    private String wechatUrl;
    /**
     * 使用状态,0-未使用,1-使用中
     */
    @ApiModelProperty(value="使用状态,0-未使用,1-使用中")
    private String status;
   
    /**
	 * 0-正常,1-删除
	 */
	@TableLogic
    private String delFlag;
    }
