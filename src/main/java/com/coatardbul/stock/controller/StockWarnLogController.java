package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.entity.StockStrategyWatch;
import com.coatardbul.stock.model.entity.StockWarnLog;
import com.coatardbul.stock.model.dto.StockWarnLogQueryDto;
import com.coatardbul.stock.service.statistic.StockWarnLogService;
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
 * Note:股票的策略扫描结果存储
 * <p>
 * Date: 2022/3/6
 *
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/stockWarnLog")
public class StockWarnLogController {

@Autowired
    StockWarnLogService stockWarnLogService;

    /**
     * 全部策略
     * @param
     * @return
     */
    @RequestMapping(path = "/findAll", method = RequestMethod.POST)
    public CommonResult<List<StockWarnLog>> findAll(@Validated @RequestBody StockWarnLogQueryDto dto) {
        return CommonResult.success(stockWarnLogService.findAll(dto));
    }


    @WebLog(value = "")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@Validated @RequestBody StockWarnLogQueryDto dto) {
        stockWarnLogService.delete(dto);
        return CommonResult.success(null);
    }
}
