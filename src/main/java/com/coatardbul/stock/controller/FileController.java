package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.bo.FileBo;
import com.coatardbul.stock.model.dto.FileQueryDto;
import com.coatardbul.stock.service.base.CosService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/5/27
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/file")
public class FileController {


    @Autowired
    CosService cosService;

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public CommonResult<String> cosUpload(MultipartFile file, HttpServletRequest req) throws Exception {
        String upload = cosService.upload( req.getParameter("path"), file);
        return CommonResult.success(upload) ;
    }

    @RequestMapping(path = "/getHeadList", method = RequestMethod.POST)
    public CommonResult<List<FileBo>> getHeadList(@RequestBody FileQueryDto dto) throws Exception {
        List<FileBo> pathInfo = cosService.getPathInfo(dto.getPath());
        return CommonResult.success(pathInfo);
    }
    @RequestMapping(path = "/mkdir", method = RequestMethod.POST)
    public CommonResult mkdir(@RequestBody FileQueryDto dto) throws Exception {
         cosService.mkdir(dto.getPath());
         return CommonResult.success(null);
    }

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@RequestBody FileQueryDto dto) throws Exception {
         cosService.delete(dto.getPath(),dto.getFileName());
        return CommonResult.success(null);
    }
}