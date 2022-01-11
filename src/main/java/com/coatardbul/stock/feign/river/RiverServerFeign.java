
package com.coatardbul.stock.feign.river;


import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.config.FeignLogConfig;
import com.coatardbul.stock.model.feign.StockTemplateQueryDto;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient(name = "river",  configuration = FeignLogConfig.class, fallbackFactory = String.class)
public interface RiverServerFeign {

    /**
     * @return
     */
    @RequestMapping(value = "river/getQuery", method = RequestMethod.POST)
    @Headers("Content-Type: application/json")
    public CommonResult<String> getQuery(StockTemplateQueryDto dto);


}
