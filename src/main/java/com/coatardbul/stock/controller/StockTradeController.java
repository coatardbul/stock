package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.bo.StockTradeBO;
import com.coatardbul.stock.model.dto.StockTradeLoginDTO;
import com.coatardbul.stock.model.dto.StockUserCookieDTO;
import com.coatardbul.stock.model.entity.StockTradeBuyConfig;
import com.coatardbul.stock.model.entity.StockTradeSellJob;
import com.coatardbul.stock.model.entity.StockTradeUrl;
import com.coatardbul.stock.service.statistic.StockTradeService;
import com.coatardbul.stock.service.statistic.StockTradeUserService;
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
 * Note:
 * <p>
 * Date: 2022/6/3
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "x")
@RequestMapping("/trade")
public class StockTradeController {

    @Autowired
    StockTradeService stockTradeService;

    /**
     * 查询持仓
     *
     * @param
     * @return
     */
    @RequestMapping(path = "/queryAssetAndPosition", method = RequestMethod.POST)
    public CommonResult queryAssetAndPosition() {
        String result = stockTradeService.queryAssetAndPosition();
        return CommonResult.success(result);
    }

    /**
     * 添加卖出信息
     *
     * @param
     * @return
     */
    @RequestMapping(path = "/addSellInfo", method = RequestMethod.POST)
    public CommonResult addSellInfo(@Validated @RequestBody StockTradeSellJob dto) {
         stockTradeService.addSellInfo(dto);
        return CommonResult.success(null);
    }
    @RequestMapping(path = "/modifySellInfo", method = RequestMethod.POST)
    public CommonResult modifySellInfo(@Validated @RequestBody StockTradeSellJob dto) {
        stockTradeService.modifySellInfo(dto);
        return CommonResult.success(null);
    }
    @RequestMapping(path = "/deleteSellInfo", method = RequestMethod.POST)
    public CommonResult deleteSellInfo(@Validated @RequestBody StockTradeSellJob dto) {
        stockTradeService.deleteSellInfo(dto);
        return CommonResult.success(null);
    }

    @RequestMapping(path = "/querySellInfo", method = RequestMethod.POST)
    public CommonResult querySellInfo(@Validated @RequestBody StockTradeSellJob dto) {
        List<StockTradeSellJob> stockTradeSellJobs = stockTradeService.querySellInfo(dto);
        return CommonResult.success(stockTradeSellJobs);
    }


    /**
     * 同步买入信息
     * @param dto
     * @return
     */
    @RequestMapping(path = "/syncBuyInfo", method = RequestMethod.POST)
    public CommonResult syncBuyInfo() {
         stockTradeService.syncBuyInfo();
        return CommonResult.success(null);
    }

    @RequestMapping(path = "/initBuyInfo", method = RequestMethod.POST)
    public CommonResult initBuyInfo() {
        stockTradeService.initBuyInfo();
        return CommonResult.success(null);
    }



}
