
package com.coatardbul.stock.feign.river;


import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.config.FeignLogConfig;
import com.coatardbul.stock.model.feign.CalendarDateDTO;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import com.coatardbul.stock.model.feign.StockTemplateQueryDto;
import com.coatardbul.stock.model.feign.StockTimeInterval;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


@FeignClient(name = "river", url="47.105.148.90:9002", configuration = FeignLogConfig.class, fallbackFactory = String.class)
public interface RiverServerFeign {

    /**
     * 获取根据模板id，日期，时间 查询的模板字符串
     * @return
     */
    @RequestMapping(value = "river/api/stockTemplate/getQuery", method = RequestMethod.POST)
    @Headers("Content-Type: application/json")
    public CommonResult<String> getQuery(StockTemplateQueryDto dto);

    /**
     * 间隔数据
     * @return
     */
    @RequestMapping(value = "river/timeInterval/getTimeList", method = RequestMethod.POST)
    @Headers("Content-Type: application/json")
    public CommonResult<List<String>> getTimeIntervalList(StockTimeInterval dto);

    /**
     * 获取两个时间段之间的日期
     * @param dto
     * @return
     */
    @RequestMapping(value = "river/api/calendar/getDate", method = RequestMethod.POST)
    @Headers("Content-Type: application/json")
    public CommonResult<List<String>> getDate(CalendarDateDTO dto);


    @RequestMapping(value = "river/api/stockTemplate/findOne", method = RequestMethod.POST)
    @Headers("Content-Type: application/json")
    public CommonResult<StockTemplateDto> findOne(StockTemplateDto dto);

}
