package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockLastUpLimitDetailDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.statistic.StockSpecialStrategyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
 * Date: 2022/4/9
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "特殊策略")
@RequestMapping("/specialStrategy")
public class StockSpecialStrategyController {
@Autowired
    StockSpecialStrategyService stockSpecialStrategyService;


    @ApiOperation("获取2板以上涨停数据")
    @RequestMapping(path = "/getUpLimitInfo", method = RequestMethod.POST)
    public CommonResult getUpLimitInfo(@Validated @RequestBody StockEmotionDayDTO dto)  {
        return CommonResult.success( stockSpecialStrategyService.getTwoAboveUpLimitInfo(dto));
    }


    @ApiOperation("获取2板以上编辑")
    @RequestMapping(path = "/getUpLimitSign", method = RequestMethod.POST)
    public CommonResult getUpLimitSign(@Validated @RequestBody StockEmotionDayDTO dto)  {
        return CommonResult.success( stockSpecialStrategyService.getUpLimitSign(dto));
    }


    /**
     *获取涨停题材
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getUpLimitTheme", method = RequestMethod.POST)
    public CommonResult getUpLimitTheme(@Validated @RequestBody StockStrategyQueryDTO dto) throws  NoSuchMethodException, ScriptException, FileNotFoundException {
        return CommonResult.success( stockSpecialStrategyService.getUpLimitTheme(dto));
    }

    /**
     *早上开盘最上面的股票异动信息构建
     * 只需要传入日期确定哪一天
     */
    @WebLog(value = "")
    @RequestMapping(path = "/amAbOne", method = RequestMethod.POST)
    public CommonResult amAbOne(@Validated @RequestBody StockStrategyQueryDTO dto)  {
        stockSpecialStrategyService.amAbOne(dto);
        return CommonResult.success( null);
    }
    /**
     *早上开盘最上面的股票异动信息构建
     * 只需要传入日期确定哪一天
     */
    @WebLog(value = "")
    @RequestMapping(path = "/amAbTwo", method = RequestMethod.POST)
    public CommonResult amAbTwo(@Validated @RequestBody StockStrategyQueryDTO dto) {
        stockSpecialStrategyService.amAbTwo(dto);
        return CommonResult.success( null);
    }
}
