
package com.coatardbul.stock.feign.river;


import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.config.FeignLogConfig;
import com.coatardbul.stock.model.feign.CalendarDateDTO;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import com.coatardbul.stock.model.feign.StockTemplateQueryDto;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;


@FeignClient(name = "river", url="47.105.148.90:9002", configuration = FeignLogConfig.class, fallbackFactory = String.class)
public interface RiverServerFeign {

    /**
     * @return
     */
    @RequestMapping(value = "river/api/stockTemplate/getQuery", method = RequestMethod.POST)
    @Headers("Content-Type: application/json")
    public CommonResult<String> getQuery(StockTemplateQueryDto dto);


    @RequestMapping(value = "river/api/calendar/getDate", method = RequestMethod.POST)
    @Headers("Content-Type: application/json")
    public CommonResult<List<String>> getDate(CalendarDateDTO dto);


    @RequestMapping(value = "river/api/stockTemplate/findOne", method = RequestMethod.POST)
    @Headers("Content-Type: application/json")
    public CommonResult<StockTemplateDto> findOne(StockTemplateDto dto);

}
