package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockWarnLogQueryDto;
import com.coatardbul.stock.model.entity.StockWarnLog;
import com.coatardbul.stock.service.statistic.StockWarnLogService;
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
 * Date: 2022/2/28
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "股票情绪查询")
@RequestMapping("/stockWaveStatic")
public class StockWaveStaticController {

}
