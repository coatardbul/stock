package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.ProxyIpQueryDTO;
import com.coatardbul.stock.service.statistic.ProxyIpService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
@Api(tags = "")
@RequestMapping("/ip")
public class ProxyIpController {
    @Autowired
    ProxyIpService proxyIpService;

    /**
     * 获取ip数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/addIps", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody ProxyIpQueryDTO dto) {
        proxyIpService.addIpProcess(dto);
        return CommonResult.success(null);
    }
}
