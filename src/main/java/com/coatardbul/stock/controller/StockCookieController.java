package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockCookieDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.StockCookieService;
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
 * Date: 2022/1/15
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/cookie")
public class StockCookieController {

    @Autowired
    StockCookieService stockCookieService;


    @WebLog(value = "同花顺新版问财功能cookie新增")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody StockCookieDTO dto) {
        stockCookieService.add(dto);
        return CommonResult.success(null);
    }

    @WebLog(value = "同花顺新版问财功能cookie修改")
    @RequestMapping(path = "/modify", method = RequestMethod.POST)
    public CommonResult modify(@Validated @RequestBody StockCookieDTO dto) {
        stockCookieService.modify(dto);
        return CommonResult.success(null);
    }

    @WebLog(value = "同花顺新版问财功能cookie修改")
    @RequestMapping(path = "/simpleModify", method = RequestMethod.POST)
    public CommonResult simpleModify(@Validated @RequestBody StockCookieDTO dto) {
        stockCookieService.simpleModify(dto);
        return CommonResult.success(null);
    }

    @RequestMapping(path = "/findAll", method = RequestMethod.POST)
    public CommonResult findAll() {
        return CommonResult.success(stockCookieService.findAll());
    }

}
