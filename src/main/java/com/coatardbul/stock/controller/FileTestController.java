package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.api.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/6/16
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/fileTest")
public class FileTestController {


    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public CommonResult<String> cosUpload(HttpServletRequest req) throws Exception {

        return null;
    }


    @ApiOperation(value = "文件下载")
    @RequestMapping(path = "/downLoadFile")
    public void downLoadFile(HttpServletResponse response) throws Exception {

        File file = new File("/Users/coatardbul/Desktop/BSC安全生产培训2022-V1.0.pdf");

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream());
        ) {
            String name = "";
            try {
                name = URLEncoder.encode("BSC安全生产培训2022-V1.0.pdf", "UTF8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            response.setContentType("application/octet-stream;charset=utf-8");
            response.addHeader("Content-Disposition", "attachment;filename=" + name + ";filename*=utf-8''" + name);
            IOUtils.copy(bis, bos);
        }

    }


    @ApiOperation(value = "文件下载")
    @RequestMapping(path = "/downLoadTest")
    public void downLoadTest(HttpServletResponse response) throws Exception {





    }




}
