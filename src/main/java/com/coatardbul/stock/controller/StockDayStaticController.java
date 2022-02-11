package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockExcelStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.statistic.StockDayStaticService;
import com.coatardbul.stock.service.statistic.StockStrategyService;
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
public class StockDayStaticController {

    @Autowired
    StockDayStaticService stockDayStaticService;

    @Autowired
    StockStrategyService stockStrategyService;


    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/refreshCookie", method = RequestMethod.POST)
    public CommonResult refreshCookie()  {
        stockDayStaticService.refreshCookie();
        return CommonResult.success(null);
    }

    /**
     * 同花顺新版问财功能
     * @param dto   基础查询对象，支持id和问句查询
     * @return
     */
    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/strategy", method = RequestMethod.POST)
    public CommonResult strategy(@Validated @RequestBody StockStrategyQueryDTO dto)  {
        return CommonResult.success(stockStrategyService.strategy(dto));
    }

    /**
     * 获取当前时间的统计数据
     * @param dto  当前时间对象，支持模板
     * @return
     */
    @WebLog(value = "获取连板标准差，中位数，adjs")
    @RequestMapping(path = "/getStatic", method = RequestMethod.POST)
    public CommonResult getStatic(@Validated @RequestBody StockStaticQueryDTO dto)  {
        return CommonResult.success(stockDayStaticService.getStatic(dto));
    }


    @WebLog(value = "获取连板标准差，中位数，adjs")
    @RequestMapping(path = "/getAllStatic", method = RequestMethod.POST)
    public CommonResult getAllStatic(@Validated @RequestBody StockExcelStaticQueryDTO  dto)  {

        return CommonResult.success( stockDayStaticService.getAllStatic(dto));
    }

    @WebLog(value = "获取连板标准差，中位数，adjs")
    @RequestMapping(path = "/saveExcel", method = RequestMethod.POST)
    public CommonResult saveExcel(@Validated @RequestBody StockExcelStaticQueryDTO  dto)  {
        stockDayStaticService.saveExcel(dto);
        return CommonResult.success(null);
    }


    @WebLog(value = "保存连板标准差，中位数，adjs")
    @RequestMapping(path = "/saveDate", method = RequestMethod.POST)
    public CommonResult saveDate(@Validated @RequestBody StockExcelStaticQueryDTO  dto)  {
        stockDayStaticService.saveDate(dto);
        return CommonResult.success(null);
    }


}
