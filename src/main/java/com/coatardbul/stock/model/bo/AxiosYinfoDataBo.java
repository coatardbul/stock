package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Note:y轴基础数据
 * <p>
 * Date: 2022/2/11
 *
 * @author Su Xiaolei
 */
@Data
public class AxiosYinfoDataBo {


    private String name;


    private List<AxiosBaseBo> yAxiosInfo;

}
