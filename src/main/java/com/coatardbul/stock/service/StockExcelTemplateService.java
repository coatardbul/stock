package com.coatardbul.stock.service;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockExcelTemplateMapper;
import com.coatardbul.stock.model.entity.StockExcelTemplate;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/1/17
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockExcelTemplateService {
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverServerFeign riverServerFeign;
    @Autowired
    StockExcelTemplateMapper stockExcelTemplateMapper;
    @Autowired
    RiverRemoteService riverRemoteService;

    public void add(StockExcelTemplate dto) {
        dto.setId(baseServerFeign.getSnowflakeId());

        stockExcelTemplateMapper.insert(dto);


    }

    public void modify(StockExcelTemplate dto) {
        stockExcelTemplateMapper.updateByPrimaryKeySelective(dto);
    }

    public List<StockExcelTemplate> findAll() {
        return stockExcelTemplateMapper.selectByAll(null);
    }


    public String findDesc(StockExcelTemplate dto) {
        Assert.notNull(dto, "请求对象不能为空");
        Assert.notNull(dto.getId(), "id不能为空");

        StockExcelTemplate stockExcelTemplate = stockExcelTemplateMapper.selectByPrimaryKey(dto.getId());
        if (stockExcelTemplate == null) {
            throw new BusinessException("查询信息为空");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getStockQueryTemplateName(stockExcelTemplate.getRiseId()));
        sb.append(getStockQueryTemplateName(stockExcelTemplate.getFailId()));
        sb.append(getStockQueryTemplateName(stockExcelTemplate.getLimitUpId()));
        sb.append(getStockQueryTemplateName(stockExcelTemplate.getLimitUpOneId()));
        sb.append(getStockQueryTemplateName(stockExcelTemplate.getLimitUpTwoId()));
        sb.append(getStockQueryTemplateName(stockExcelTemplate.getLimitUpThreeId()));
        return sb.toString();
    }

    private String getStockQueryTemplateName(String id) {
        StockTemplateDto templateById = riverRemoteService.getTemplateById(id);

        return templateById.getName() + "\n";

    }

    public StockExcelTemplate getStandardInfo(String id, String dateStr) {
        StockExcelTemplate stockExcelTemplate = stockExcelTemplateMapper.selectByPrimaryKey(id);
        if (stockExcelTemplate != null) {
            stockExcelTemplate.setOrderStr(stockExcelTemplate.getOrderStr().replace("dateStr", dateStr.replaceAll("-", "")));
        }

        return stockExcelTemplate;
    }
}
