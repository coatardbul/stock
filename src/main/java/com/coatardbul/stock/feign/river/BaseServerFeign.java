
package com.coatardbul.stock.feign.river;


import com.coatardbul.stock.common.config.FeignLogConfig;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "baseServer", url = "124.222.217.230:9007",configuration = FeignLogConfig.class, fallbackFactory = String.class)
public interface BaseServerFeign {

    /**
     * @return
     */
    @RequestMapping(value = "baseServer/snowflakeId", method = RequestMethod.GET)
    @Headers("Content-Type: application/json")
    public String getSnowflakeId();


}
