package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/11
 *
 * @author Su Xiaolei
 */
@Data
public class AxiosAllDataBo {

    private List<String> dateTimeStrArray;

    private List<AxiosYinfoDataBo> ybaseInfo;
}
