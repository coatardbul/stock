
package com.coatardbul.stock.feign.river.fallback;


import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.model.feign.StockTemplateQueryDto;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author: suxiaolei
 * @date: 2019/7/23
 */
@Slf4j
@Component
public class RiverServerFeignFallback implements FallbackFactory<RiverServerFeign> {


    @Override
    public RiverServerFeign create(Throwable throwable) {
        return new RiverServerFeign() {
            @Override
            public CommonResult<String> getQuery(StockTemplateQueryDto dto) {
                log.error("调用失败", throwable);
                return null;            }
        };
    }
}