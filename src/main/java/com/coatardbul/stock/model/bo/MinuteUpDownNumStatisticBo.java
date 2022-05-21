package com.coatardbul.stock.model.bo;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/5/14
 *
 * @author Su Xiaolei
 */
@Data
public class MinuteUpDownNumStatisticBo {

    private String upLevel5;
    private String upLevel4;
    private String upLevel3;
    private String upLevel2;
    private String upLevel1;
    private String level0;
    private String downLevel1;
    private String downLevel2;
    private String downLevel3;
    private String downLevel4;
    private String downLevel5;


}
