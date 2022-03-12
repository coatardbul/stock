package com.coatardbul.stock.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/9
 *
 * @author Su Xiaolei
 */
@Data
public class StockWarnLogQueryDto {

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 日期
     */
    @NotBlank(message = "YYYY-MM-DD不能为空")
    private String dateStr;



}
