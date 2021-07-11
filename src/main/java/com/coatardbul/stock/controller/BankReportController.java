package com.coatardbul.stock.controller;


import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.PlateTypeEnum;
import com.coatardbul.stock.mapper.BaseInfoDictMapper;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.coatardbul.stock.service.ConceptBaseInfoService;
import com.coatardbul.stock.service.IndustryBaseInfoService;
import com.coatardbul.stock.service.ModulePriceService;
import com.coatardbul.stock.service.StockBaseInfoService;
import com.coatardbul.stock.service.StockBaseService;
import com.coatardbul.stock.service.StockPriceService;
import com.coatardbul.stock.service.TerritoryBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * Note: 总分行报告列表
 * <p> TENANT_ID
 * Date: 2021/6/17
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@RequestMapping("/price")
public class BankReportController {
    @Autowired
    StockPriceService stockPriceService;
    @Autowired
    StockBaseService stockBaseService;
    @Autowired
    ModulePriceService modulePriceService;
    @Autowired
    ConceptBaseInfoService conceptBaseInfoService;
    @Autowired
    IndustryBaseInfoService industryBaseInfoService;
    @Autowired
    TerritoryBaseInfoService territoryBaseInfoService;
    @Autowired
    StockBaseInfoService stockBaseInfoService;
    @Autowired
    private BaseInfoDictMapper baseInfoDictMapper;
    @WebLog(value = "")
    @RequestMapping(path = "/refreshStockPrice", method = RequestMethod.POST)
    public CommonResult refreshStockPrice(@Validated @RequestBody StockPriceRequestDTO dto) {
        stockPriceService.refreshStockPrice(dto);
        return CommonResult.success("刷新成功");
    }


    /**
     * 刷新当前代码的所有基础信息
     *
     * @param dto
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshStockBaseInfo", method = RequestMethod.POST)
    public CommonResult refreshStockBaseInfo(@Validated @RequestBody StockPriceRequestDTO dto) {
        stockBaseService.refreshStockBaseInfo(dto);
        return CommonResult.success("刷新成功");
    }

    /**
     * 概念,行业，地域 基本信息
     *
     * @param dto
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshModuleBaseInfo", method = RequestMethod.POST)
    public CommonResult refreshModuleBaseInfo(@Validated @RequestBody StockPriceRequestDTO dto) {
        conceptBaseInfoService.refreshModuleBaseInfo(dto);
        industryBaseInfoService.refreshModuleBaseInfo(dto);
        territoryBaseInfoService.refreshModuleBaseInfo(dto);
        return CommonResult.success("刷新成功");
    }

    /**
     * 股票 基本信息
     *
     * @param dto
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshStockBaseInfoDict", method = RequestMethod.POST)
    public CommonResult refreshStockBaseInfoDict(@Validated @RequestBody StockPriceRequestDTO dto) throws InterruptedException {
        for(int i=Integer.valueOf(dto.getCode());i<=217;i++){
            dto.setCode(String.valueOf(i));
            try {
                stockBaseInfoService.refreshModuleBaseInfo(dto);
            }catch (Exception e){
                return CommonResult.failed("到达"+i+"中断");
            }
            Thread.sleep(1000);
        }
        return CommonResult.success("刷新成功");
    }
}
