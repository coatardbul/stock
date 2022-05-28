package com.coatardbul.stock.model.bo;

import lombok.Data;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/5/27
 *
 * @author Su Xiaolei
 */
@Data
public class FileBo {

    private String url;

    private String fileName;

    /**
     * 1 目录 2 文件
     */
    private String fileType;
}
