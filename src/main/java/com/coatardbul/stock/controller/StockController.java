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
import com.coatardbul.stock.service.StockModuleMapperService;
import com.coatardbul.stock.service.StockPriceService;
import com.coatardbul.stock.service.TerritoryBaseInfoService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p
 * Date: 2021/6/17
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/price")
public class StockController {
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
    @Autowired
    StockModuleMapperService stockModuleMapperService;

    ExecutorService postLoanThreadPool;


    @Autowired
    public void PostLoanController() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("stock_main_thread").build();
        this.postLoanThreadPool = new ThreadPoolExecutor(
                10, 20, 1, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000),
                threadFactory);
    }


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
     * 刷新当前代码的所有基础信息
     *
     * @param dto
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshAllStockBaseInfo", method = RequestMethod.POST)
    public CommonResult refreshAllStockBaseInfo(@Validated @RequestBody StockPriceRequestDTO dto) throws InterruptedException {
        for (int i = 0; i < 1000000; i += 10000) {
            final int sb=i;
            postLoanThreadPool.submit(() -> {
                for (int j = sb; j < sb + 10000; j++) {
                    StockPriceRequestDTO stockPriceRequestDTO = new StockPriceRequestDTO();
                    String zero = getZero(6 - String.valueOf(j).length()) + j;
                    stockPriceRequestDTO.setCode(zero);
                    stockBaseService.refreshStockBaseInfo(stockPriceRequestDTO);
                }

            });
        }

        return CommonResult.success("刷新成功");
    }

    private String getZero(int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sb.append("0");
        }
        return sb.toString();
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
        for (int i = Integer.valueOf(dto.getCode()); i <= 217; i++) {
            dto.setCode(String.valueOf(i));
            try {
                stockBaseInfoService.refreshModuleBaseInfo(dto);
            } catch (Exception e) {
                return CommonResult.failed("到达" + i + "中断");
            }
            Thread.sleep(1000);
        }
        return CommonResult.success("刷新成功");
    }

    /**
     * 寻找股票与所属概念映射
     *
     * @param dto
     * @return
     * @throws InterruptedException
     */
    @WebLog(value = "")
    @RequestMapping(path = "/refreshStockModuleMapper", method = RequestMethod.POST)
    public CommonResult refreshStockModuleMapper(@Validated @RequestBody StockPriceRequestDTO dto) throws InterruptedException {
        stockModuleMapperService.refreshStockModuleMapper(dto);
        return CommonResult.success("刷新成功");
    }
}
