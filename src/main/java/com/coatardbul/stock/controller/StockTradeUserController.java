package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.bo.StockTradeBO;
import com.coatardbul.stock.model.dto.StockTradeLoginDTO;
import com.coatardbul.stock.model.dto.StockUserCookieDTO;
import com.coatardbul.stock.model.entity.StockTradeUser;
import com.coatardbul.stock.service.statistic.StockTradeUserService;
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
 * Date: 2022/6/3
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "x")
@RequestMapping("/tradeUser")
public class StockTradeUserController {

    @Autowired
    StockTradeUserService stockTradeUserService;



    /**
     *
     */
    @WebLog(value = "updateCookie")
    @RequestMapping(path = "/updateCookie", method = RequestMethod.POST)
    public CommonResult updateCookie(@Validated @RequestBody StockUserCookieDTO dto) {
        stockTradeUserService.updateCookie(dto);
        return CommonResult.success(dto);
    }






}
