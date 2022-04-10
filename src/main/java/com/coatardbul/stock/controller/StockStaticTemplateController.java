package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockStaticTemplateBaseDTO;
import com.coatardbul.stock.service.statistic.StockStaticTemplateService;
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
 * 统计纬度模板，按照天，按照分钟统计
 * 按照天，1 统计标准差，方差  2 各个纬度涨停数据
 * 按照分钟  每个时间段的实时数据，反应情绪变化
 * <p>
 * Date: 2022/1/17
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/staticTemplate")
public class StockStaticTemplateController {

    @Autowired
    StockStaticTemplateService stockStaticTemplateService;


    /**
     * 策略新增
     * @param dto 请求参数
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    public CommonResult add(@Validated @RequestBody StockStaticTemplateBaseDTO dto) {
        stockStaticTemplateService.add(dto);
        return CommonResult.success(null);
    }
    /**
     * 策略修改
     * @param dto 请求参数
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/modify", method = RequestMethod.POST)
    public CommonResult modify(@Validated @RequestBody StockStaticTemplateBaseDTO dto) {
        stockStaticTemplateService.modify(dto);
        return CommonResult.success(null);
    }
    /**
     * 策略删除
     * @param dto 请求参数
     * @return
     */
    @WebLog(value = "")
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@Validated @RequestBody StockStaticTemplateBaseDTO dto) {
        stockStaticTemplateService.delete(dto);
        return CommonResult.success(null);
    }

    /**
     * 全部策略
     * @param
     * @return
     */
    @RequestMapping(path = "/findAll", method = RequestMethod.POST)
    public CommonResult<List<StockStaticTemplateBaseDTO>> findAll(@Validated @RequestBody StockStaticTemplateBaseDTO dto) {
        return CommonResult.success(stockStaticTemplateService.findAll(dto));
    }

}
