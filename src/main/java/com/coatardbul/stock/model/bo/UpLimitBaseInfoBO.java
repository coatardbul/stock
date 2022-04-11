package com.coatardbul.stock.model.bo;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/11
 *
 * @author Su Xiaolei
 */
@Data
public class UpLimitBaseInfoBO extends UpLimitStrongWeakBO{
    private String name;

    private String code;
}
