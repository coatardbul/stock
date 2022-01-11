package com.coatardbul.stock.model.bo;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Data
public class StrategyBO {
    private Integer totalNum;
    /**
     * 返回对象
     */
    private JSONArray data;

}
