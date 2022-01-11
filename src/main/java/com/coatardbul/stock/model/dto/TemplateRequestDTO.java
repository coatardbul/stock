package com.coatardbul.stock.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2021/12/26
 *
 * @author Su Xiaolei
 */
@Data
@ApiModel
public class TemplateRequestDTO {


    @NotBlank(message = "模板样例不能为空")
    private String id;


}
