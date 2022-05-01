package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockLastUpLimitDetailDTO;
import com.coatardbul.stock.service.statistic.StockSpecialStrategyService;
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
 * <p>股票异常行为，包含各种行为
 * Date: 2022/4/9
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/stockAnomalousBehavior")
public class StockAnomalousBehaviorController {
    @Autowired
    StockSpecialStrategyService stockSpecialStrategyService;


    /**
     *构建昨曾，过去数据，补充构建
     */
    @WebLog(value = "")
    @RequestMapping(path = "/buildLastUpLimitInfo", method = RequestMethod.POST)
    public CommonResult buildLastUpLimitInfo(@Validated @RequestBody StockLastUpLimitDetailDTO dto) {
        stockSpecialStrategyService.buildLastUpLimitInfo(dto);
        return CommonResult.success(null);
    }

    /**
     *强制重新构建
     */
    @WebLog(value = "")
    @RequestMapping(path = "/forceBuildLastUpLimitInfo", method = RequestMethod.POST)
    public CommonResult forceBuildLastUpLimitInfo(@Validated @RequestBody StockLastUpLimitDetailDTO dto)  {
        stockSpecialStrategyService.forceBuildLastUpLimitInfo(dto);
        return CommonResult.success(null);
    }

    /**
     *获取过去异动数据数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getAllAnomalousBehaviorData", method = RequestMethod.POST)
    public CommonResult getAllAnomalousBehaviorData(@Validated @RequestBody StockLastUpLimitDetailDTO dto) {
        return CommonResult.success( stockSpecialStrategyService.getAllAnomalousBehaviorData(dto));
    }



    /**
     *获取过去异动数据数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/deleteAnomalousBehaviorData", method = RequestMethod.POST)
    public CommonResult deleteAnomalousBehaviorData(@Validated @RequestBody StockLastUpLimitDetailDTO dto) {
        stockSpecialStrategyService.deleteAnomalousBehaviorData(dto);
        return CommonResult.success( null);
    }



    /**
     *获取过去异动统计数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getAbStatic", method = RequestMethod.POST)
    public CommonResult getAbStatic(@Validated @RequestBody StockLastUpLimitDetailDTO dto) {

        return CommonResult.success(  stockSpecialStrategyService.getAbStatic(dto));
    }

//    /**
//     *修改获取移动统计数据
//     */
//    @WebLog(value = "")
//    @RequestMapping(path = "/getAbStatic", method = RequestMethod.POST)
//    public CommonResult getsb(@Validated @RequestBody StockLastUpLimitDetailDTO dto) {
//        stockSpecialStrategyService.getAbStatic(dto);
//        return CommonResult.success( null);
//    }

}
