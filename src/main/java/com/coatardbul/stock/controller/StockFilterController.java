package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockFilterDeleteInfoDTO;
import com.coatardbul.stock.model.dto.StockFilterSaveInfoDTO;
import com.coatardbul.stock.model.entity.StockFilter;
import com.coatardbul.stock.service.statistic.StockFilterService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptException;
import java.io.FileNotFoundException;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/5/8
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "股票查询")
@RequestMapping("/stockFilter")
public class StockFilterController {

    @Autowired
    StockFilterService stockFilterService;

    /**
     * 保存所有信息
     */
    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/save", method = RequestMethod.POST)
    public CommonResult save(@Validated @RequestBody StockFilterSaveInfoDTO dto)  {
        stockFilterService.save(dto);
        return CommonResult.success(null);
    }

    /**
     * 保存所有信息
     */
    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@Validated @RequestBody StockFilterDeleteInfoDTO dto)  {
        stockFilterService.delete(dto);
        return CommonResult.success(null);
    }

    /**
     * 保存所有信息
     */
    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/modify", method = RequestMethod.POST)
    public CommonResult modify(@Validated @RequestBody StockFilter dto)  {
        stockFilterService.modify(dto);
        return CommonResult.success(null);
    }
    /**
     * 过滤获取转换的数据
     */
    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/getFilterInfo", method = RequestMethod.POST)
    public CommonResult getFilterInfo(@Validated @RequestBody StockFilter dto)  {
        return CommonResult.success( stockFilterService.getFilterInfo(dto));
    }

    /**
     * 获取表中的数据
     */
    @WebLog(value = "同花顺新版问财功能")
    @RequestMapping(path = "/getDirectFilterInfo", method = RequestMethod.POST)
    public CommonResult getDirectFilterInfo(@Validated @RequestBody StockFilter dto)  {
        return CommonResult.success( stockFilterService.getDirectFilterInfo(dto));
    }
}
