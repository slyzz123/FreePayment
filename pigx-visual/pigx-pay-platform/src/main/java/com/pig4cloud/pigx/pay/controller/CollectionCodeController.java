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
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import com.pig4cloud.pigx.common.security.annotation.Inner;
import com.pig4cloud.pigx.pay.entity.CollectionCode;
import com.pig4cloud.pigx.pay.service.CollectionCodeService;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 收款码表
 *
 * @author pigx code generator
 * @date 2020-09-17 14:13:49
 */
@RestController
@AllArgsConstructor
@RequestMapping("/collectioncode" )
@Api(value = "collectioncode", tags = "收款码表管理")
public class CollectionCodeController {

    private final  CollectionCodeService collectionCodeService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param collectionCode 收款码表
     * @return
     */
    @Inner(false)
    @GetMapping("/page" )
    public R getCollectionCodePage(Page page, CollectionCode collectionCode) {
        return R.ok(collectionCodeService.page(page, Wrappers.query(collectionCode)));
    }


    /**
     * 通过id查询收款码表
     * @param id id
     * @return R
     */
    @Inner(false)
    @GetMapping("/{id}" )
    public R getById(@PathVariable("id" ) String id) {
        return R.ok(collectionCodeService.getById(id));
    }

    /**
     * 新增收款码表
     * @param collectionCode 收款码表
     * @return R
     */
    @Inner(false)
    @SysLog("新增收款码表" )
    @PostMapping
    public R save(@RequestBody CollectionCode collectionCode) {
        return R.ok(collectionCodeService.save(collectionCode));
    }

    /**
     * 修改收款码表
     * @param collectionCode 收款码表
     * @return R
     */
    @Inner(false)
    @SysLog("修改收款码表" )
    @PutMapping
    public R updateById(@RequestBody CollectionCode collectionCode) {
        return R.ok(collectionCodeService.updateById(collectionCode));
    }

    /**
     * 通过id删除收款码表
     * @param id id
     * @return R
     */
    @Inner(false)
    @SysLog("通过id删除收款码表" )
    @DeleteMapping("/{id}" )
    public R removeById(@PathVariable String id) {
        return R.ok(collectionCodeService.removeById(id));
    }

}
