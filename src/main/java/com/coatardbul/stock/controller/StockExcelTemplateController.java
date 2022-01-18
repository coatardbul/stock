package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockCookieDTO;
import com.coatardbul.stock.model.entity.StockExcelTemplate;
import com.coatardbul.stock.service.StockExcelTemplateService;
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
 * Date: 2022/1/17
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/excelTemplate")
public class StockExcelTemplateController {

    @Autowired
    StockExcelTemplateService stockExcelTemplateService;

    @WebLog(value = "")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody StockExcelTemplate dto) {
        stockExcelTemplateService.add(dto);
        return CommonResult.success(null);
    }

    @WebLog(value = "")
    @RequestMapping(path = "/modify", method = RequestMethod.POST)
    public CommonResult modify(@Validated @RequestBody StockExcelTemplate dto) {
        stockExcelTemplateService.modify(dto);
        return CommonResult.success(null);
    }

    @RequestMapping(path = "/findAll", method = RequestMethod.POST)
    public CommonResult findAll() {
        return CommonResult.success(stockExcelTemplateService.findAll());
    }

    @RequestMapping(path = "/findDesc", method = RequestMethod.POST)
    public CommonResult findDesc(@Validated @RequestBody  StockExcelTemplate dto) {
        return CommonResult.success(stockExcelTemplateService.findDesc(dto));
    }

}
