package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.StockTemplateEnum;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockDefineStaticMapper;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockDefineStaticDTO;
import com.coatardbul.stock.model.dto.StockPredictDto;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockDefineStatic;
import com.coatardbul.stock.model.entity.StockTemplatePredict;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/7/5
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockDefineStaticService {


    @Autowired
    StockDefineStaticMapper stockDefineStaticMapper;

    @Autowired
    BaseServerFeign baseServerFeign;



    public void add(StockDefineStatic dto) {

        stockDefineStaticMapper.deleteByDateAndObjectSign(dto.getDate(), dto.getObjectSign());
        dto.setId(baseServerFeign.getSnowflakeId());
        stockDefineStaticMapper.insert(dto);


    }

    public List<StockDefineStatic> getAll(StockDefineStaticDTO dto) {
        List<StockDefineStatic> stockDefineStatics = stockDefineStaticMapper.selectAllByDateBetweenEqualAndObjectSign(dto.getBeginDateStr(),
                dto.getEndDateStr(),
                dto.getObjectSign());
        return stockDefineStatics;
    }


}
