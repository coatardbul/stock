package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.StockStrategyService;
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
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "股票查询")
@RequestMapping("/stockQuery")
public class StockQueryController {

    @Autowired
    StockStrategyService stockStrategyService;


    /**
     * 同花顺新版问财功能
     * @param dto
     * @return
     */

    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/strategy", method = RequestMethod.POST)
    public CommonResult strategy(@Validated @RequestBody StockStrategyQueryDTO dto)  {
        return CommonResult.success(stockStrategyService.strategy(dto));
    }

    @WebLog(value = "获取连板标准差，中位数，adjs")
    @RequestMapping(path = "/getStatic", method = RequestMethod.POST)
    public CommonResult getStatic(@Validated @RequestBody StockStaticQueryDTO dto)  {
        return CommonResult.success(stockStrategyService.getStatic(dto));
    }

}
