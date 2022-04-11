package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockUpLimitNumDTO;
import com.coatardbul.stock.model.dto.StockValPriceDTO;
import com.coatardbul.stock.service.statistic.StockUpLimitValPriceService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

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

    /**
     * 对当前股票的量价关系（竞价金额，竞价换手，竞价涨幅，成交额，成交换手）进行分析，并描述涨停信息
     * @param dto  股票代码和时间
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/execute", method = RequestMethod.POST)
    public CommonResult execute(@Validated @RequestBody StockValPriceDTO dto) {
        stockUpLimitValPriceService.volPriceProcess(dto);
        return CommonResult.success(null);
    }
    @WebLog(value = "")
    @RequestMapping(path = "/strongWeak", method = RequestMethod.POST)
    public CommonResult strongWeak(@Validated @RequestBody StockValPriceDTO dto) throws ParseException {
        stockUpLimitValPriceService.strongWeakProcess(dto);
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


    /**
     * 根据名称获取涨停信息
     * @param dto
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getDescribe", method = RequestMethod.POST)
    public CommonResult getDescribe(@Validated @RequestBody StockUpLimitNumDTO dto) {
        return CommonResult.success( stockUpLimitValPriceService.getDescribe(dto));
    }
}
