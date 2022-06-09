package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.entity.StockTradeUrl;
import com.coatardbul.stock.service.statistic.StockTradeUrlService;
import io.swagger.annotations.Api;
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
 * Date: 2022/6/4
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "x")
@RequestMapping("/tradeUrl")
public class StockTradeUrlController {

    @Autowired
    StockTradeUrlService stockTradeUrlService;
    /**
     * 策略新增
     * @param dto 请求参数
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody StockTradeUrl dto) {
        stockTradeUrlService.add(dto);
        return CommonResult.success(null);
    }
    /**
     * 策略修改
     * @param dto 请求参数
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/modify", method = RequestMethod.POST)
    public CommonResult modify(@Validated @RequestBody StockTradeUrl dto) {
        stockTradeUrlService.modify(dto);
        return CommonResult.success(null);
    }
    /**
     * 策略删除
     * @param dto 请求参数
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@Validated @RequestBody StockTradeUrl dto) {
        stockTradeUrlService.delete(dto);
        return CommonResult.success(null);
    }

    /**
     * 全部策略
     * @param
     * @return
     */
    @RequestMapping(path = "/findAll", method = RequestMethod.POST)
    public CommonResult<List<StockTradeUrl>> findAll() {
        return CommonResult.success(stockTradeUrlService.findAll());
    }




}
