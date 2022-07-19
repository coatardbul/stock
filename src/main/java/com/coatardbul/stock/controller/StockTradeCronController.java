package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.bo.QuartzBean;
import com.coatardbul.stock.model.entity.StockTradeCron;
import com.coatardbul.stock.model.entity.StockTradeStrategy;
import com.coatardbul.stock.service.statistic.StockQuartzService;
import com.coatardbul.stock.service.statistic.StockTradeCronService;
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
@Api(tags = "定时任务相关")
@RequestMapping("/tradeCron")
public class StockTradeCronController {


    @Autowired
    StockTradeCronService stockTradeCronService;



    /**
     * 保存所有信息
     */
    @WebLog(value = "保存所有信息")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody StockTradeCron dto)  {
        stockTradeCronService.add(dto);
        return CommonResult.success(null);
    }
    /**
     * 保存所有信息
     */
    @WebLog(value = "保存所有信息")
    @RequestMapping(path = "/modify", method = RequestMethod.POST)
    public CommonResult modify(@Validated @RequestBody StockTradeCron dto)  {
        stockTradeCronService.modify(dto);
        return CommonResult.success(null);
    }
    /**
     * 保存所有信息
     */
    @WebLog(value = "保存所有信息")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@Validated @RequestBody StockTradeCron dto)  {
        stockTradeCronService.delete(dto);
        return CommonResult.success(null);
    }
}
