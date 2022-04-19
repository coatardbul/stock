package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.ProxyIpQueryDTO;
import com.coatardbul.stock.model.entity.ProxyIp;
import com.coatardbul.stock.service.statistic.ProxyIpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/14
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "ip相关")
@RequestMapping("/ip")
public class ProxyIpController {
    @Autowired
    ProxyIpService proxyIpService;

    /**
     * 添加ip数据
     */
    @WebLog(value = "")
    @ApiOperation("使用代理添加ip信息")
    @RequestMapping(path = "/addIps", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody ProxyIpQueryDTO dto) {
        proxyIpService.addIpProcess(dto);
        return CommonResult.success(null);
    }

    /**
     *
     * @param dto
     * @return
     */
    @ApiOperation("获取所有ip信息")
    @RequestMapping(path = "/getAllIps", method = RequestMethod.POST)
    public CommonResult<List<ProxyIp>> getAllIps() {
        return CommonResult.success(  proxyIpService.getAllIps());
    }

}
