package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockPredictDto;
import com.coatardbul.stock.model.dto.StockValPriceDTO;
import com.coatardbul.stock.service.statistic.StockPredictService;
import com.coatardbul.stock.service.statistic.StockUpLimitValPriceService;
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
 * Note:涨停量价最多支持9天，
 * <p>
 * Date: 2022/4/4
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/stockValPrice")
public class StockUpLimitValPriceController {
    @Autowired
    StockUpLimitValPriceService stockUpLimitValPriceService;

    @WebLog(value = "")
    @RequestMapping(path = "/execute", method = RequestMethod.POST)
    public CommonResult execute(@Validated @RequestBody StockValPriceDTO dto) {
        stockUpLimitValPriceService.execute(dto);
        return CommonResult.success(null);
    }
    @WebLog(value = "")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@Validated @RequestBody StockValPriceDTO dto) {
        stockUpLimitValPriceService.delete(dto);
        return CommonResult.success(null);
    }

    @WebLog(value = "")
    @RequestMapping(path = "/getAll", method = RequestMethod.POST)
    public CommonResult getAll(@Validated @RequestBody StockValPriceDTO dto) {
        return CommonResult.success( stockUpLimitValPriceService.getAll(dto));
    }
}
