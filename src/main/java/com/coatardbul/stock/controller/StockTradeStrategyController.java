package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockTradeStrategyDTO;
import com.coatardbul.stock.model.entity.StockDefineStatic;
import com.coatardbul.stock.model.entity.StockTradeStrategy;
import com.coatardbul.stock.service.statistic.StockTradeStrategyService;
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
 * Date: 2022/7/16
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "自定义策略")
@RequestMapping("/tradeStrategy")
public class StockTradeStrategyController {


    @Autowired
    StockTradeStrategyService stockTradeStrategyService;

    /**
     * 保存所有信息
     */
    @WebLog(value = "保存所有信息")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody StockTradeStrategy dto)  {
        stockTradeStrategyService.add(dto);
        return CommonResult.success(null);
    }

    /**
     * 修改所有信息
     */
    @WebLog(value = "修改所有信息")
    @RequestMapping(path = "/modify", method = RequestMethod.POST)
    public CommonResult modify(@Validated @RequestBody StockTradeStrategy dto)  {
        stockTradeStrategyService.modify(dto);
        return CommonResult.success(null);
    }

    /**
     * 删除信息
     */
    @WebLog(value = "删除信息")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@Validated @RequestBody StockTradeStrategy dto)  {
        stockTradeStrategyService.delete(dto);
        return CommonResult.success(null);
    }

    /**
     * 保存所有信息
     */
    @WebLog(value = "获取所有信息")
    @RequestMapping(path = "/findAll", method = RequestMethod.POST)
    public CommonResult findAll(@Validated @RequestBody StockTradeStrategyDTO dto)  {
        return CommonResult.success( stockTradeStrategyService.findAll(dto));
    }
}
