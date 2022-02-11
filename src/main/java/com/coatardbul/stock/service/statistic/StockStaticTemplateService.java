package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.constants.StaticLatitudeEnum;
import com.coatardbul.stock.common.util.StockStaticModuleUtil;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.dto.StockStaticTemplateBaseDTO;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:，
 * <p>
 * Date: 2022/1/17
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockStaticTemplateService {
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverServerFeign riverServerFeign;
    @Autowired
    StockStaticTemplateMapper stockStaticTemplateMapper;

    public void add(StockStaticTemplateBaseDTO dto) {
        StockStaticTemplate add = convertAndVerifyData(dto);
        stockStaticTemplateMapper.insertSelective(add);

    }

    /**
     * 将请求对象转换成数据库对象，并验证 统计纬度，统计间隔，统计对象
     *
     * @param dto
     * @return
     */
    private StockStaticTemplate convertAndVerifyData(StockStaticTemplateBaseDTO dto) {
        StockStaticTemplate result = new StockStaticTemplate();
        //编辑
        if (StringUtils.isNotBlank(dto.getId())) {
            result.setId(dto.getId());
        } else {
            result.setId(baseServerFeign.getSnowflakeId());
        }
        //严格判断纬度是否为枚举里面的数据
        if (StaticLatitudeEnum.isHaveCode(dto.getStaticLatitude())) {
            result.setStaticLatitude(dto.getStaticLatitude());
        } else {
            throw new BusinessException("统计纬度枚举数据异常");
        }
        //间隔时间验证
        if (StaticLatitudeEnum.isNotBlankVerify(dto.getStaticLatitude(), dto.getTimeInterval())) {
            result.setTimeInterval(dto.getTimeInterval());
        }else {
            throw new BusinessException("间隔时间与统计纬度不符合");
        }
        result.setRemark(dto.getRemark());
        //todo 需要判断，暂时不判断
        result.setOrderBy(dto.getOrderBy());

        //验证统计对象是否合法
        StockStaticModuleUtil.verify(dto.getObjectEnumSign(), dto.getObjectJson());
        result.setObjectSign(dto.getObjectEnumSign());
        result.setObjectStr(dto.getObjectJson());
        return result;
    }


    public void modify(StockStaticTemplateBaseDTO dto) {
        StockStaticTemplate modify = convertAndVerifyData(dto);
        stockStaticTemplateMapper.updateByPrimaryKeySelective(modify);
    }

    private StockStaticTemplateBaseDTO convert(StockStaticTemplate dto) {
        StockStaticTemplateBaseDTO result = new StockStaticTemplateBaseDTO();
        result.setId(dto.getId());
        result.setStaticLatitude(dto.getStaticLatitude());
        result.setRemark(dto.getRemark());
        result.setOrderBy(dto.getOrderBy());
        result.setObjectEnumSign(dto.getObjectSign());
        result.setObjectJson(dto.getObjectStr());
        result.setTimeInterval(dto.getTimeInterval());
        return result;
    }
    public List<StockStaticTemplateBaseDTO> findAll() {
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAll();
        if (stockStaticTemplates != null && stockStaticTemplates.size() > 0) {
            return stockStaticTemplates.stream().map(this::convert).collect(Collectors.toList());
        }
        return null;
    }

    public void delete(StockStaticTemplateBaseDTO dto) {
        StockStaticTemplate stockStaticTemplate = stockStaticTemplateMapper.selectByPrimaryKey(dto.getId());
        if(stockStaticTemplate==null){
            throw new BusinessException("删除的模板不存在");
        }
        stockStaticTemplateMapper.deleteByPrimaryKey(dto.getId());
    }
}
