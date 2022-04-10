package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.service.statistic.StockSpecialStrategyService;
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
 * Note:
 * <p>
 * Date: 2022/4/9
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/specialStrategy")
public class StockSpecialStrategyController {
@Autowired
    StockSpecialStrategyService stockSpecialStrategyService;

    /**
     *获取涨停数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getUpLimitInfo", method = RequestMethod.POST)
    public CommonResult getUpLimitInfo(@Validated @RequestBody StockEmotionDayDTO dto) throws IllegalAccessException, ParseException {
       ;
        return CommonResult.success( stockSpecialStrategyService.getUpLimitInfo(dto));
    }

}
