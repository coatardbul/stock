package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockDefineStaticDTO;
import com.coatardbul.stock.model.dto.StockFilterSaveInfoDTO;
import com.coatardbul.stock.model.entity.StockDefineStatic;
import com.coatardbul.stock.service.statistic.StockDefineStaticService;
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
 * Date: 2022/7/5
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "自定义统计")
@RequestMapping("/defineStatic")
public class StockDefineStaticController {


    @Autowired
    private StockDefineStaticService stockDefineStaticService;


    /**
     * 保存所有信息
     */
    @WebLog(value = "保存所有信息")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody StockDefineStatic dto)  {
        stockDefineStaticService.add(dto);
        return CommonResult.success(null);
    }


    /**
     * 获取所有
     */
    @WebLog(value = "获取所有")
    @RequestMapping(path = "/getAll", method = RequestMethod.POST)
    public CommonResult getAll(@Validated @RequestBody StockDefineStaticDTO dto)  {

        return CommonResult.success(stockDefineStaticService.getAll(dto));
    }

}
