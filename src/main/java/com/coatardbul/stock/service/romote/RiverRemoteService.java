package com.coatardbul.stock.service.romote;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.model.bo.StockStaticBO;
import com.coatardbul.stock.model.dto.StockExcelStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStaticQueryDTO;
import com.coatardbul.stock.model.feign.CalendarDateDTO;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/12
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class RiverRemoteService {

    @Autowired
    RiverServerFeign riverServerFeign;




    /**
     * 获取两个日期之间的工作日
     *
     * @param beginDateStr
     * @param endDateStr
     * @return
     */
    public List<String> getDateIntervalList(String beginDateStr, String endDateStr) {
        // 根据开始结束时间查询工作日信息
        CalendarDateDTO query = new CalendarDateDTO();
        query.setBeginDate(beginDateStr);
        query.setEndDate(endDateStr);
        query.setDateProp(1);
        CommonResult<List<String>> date = riverServerFeign.getDate(query);
        return date.getData();
    }

    public StockTemplateDto getTemplateById(String id) {
        StockTemplateDto stockTemplateDto = new StockTemplateDto();
        stockTemplateDto.setId(id);
        CommonResult<StockTemplateDto> riverDate = riverServerFeign.findOne(stockTemplateDto);
        if(riverDate.getData()==null){
            throw new BusinessException("模板id不正确");
        }
        return riverDate.getData();
    }

}
