package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.api.CommonResult;
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
 * Note:
 * <p>
 * Date: 2022/3/6
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

}
