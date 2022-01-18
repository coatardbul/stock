package com.coatardbul.stock.model.feign;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author  Su Xiaolei
 */@Data
public class CalendarDateDTO {
    /**
     * yyyy-mm-dd
     */
    @NotBlank(message = "开始日期不能为空")
    private String beginDate;
    @NotBlank(message = "结束日期不能为空")
    private String endDate;

    /**
     * 日期属性 1 工作日 3节假日2 休息日
     */
    private Integer dateProp;
}
