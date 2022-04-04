package com.coatardbul.stock.model.feign;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author  Su Xiaolei
 */
@Data
public class CalendarSpecialDTO {
    /**
     * YYYY-MM-DD
     */
    @NotBlank(message = "日期不能为空")
    private String dateStr;

    /**
     * 日期属性 1 工作日 3节假日2 休息日
     */
    private Integer dateProp;

    /**
     * 数字为正，为后几天的数据，
     * 数字为负，为前几天的数据
     */
    private Integer addDay;
}
