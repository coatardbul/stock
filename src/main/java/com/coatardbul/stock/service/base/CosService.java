package com.coatardbul.stock.service.base;

import com.coatardbul.stock.common.config.CosConfig;
import com.coatardbul.stock.model.bo.FileBo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 腾讯云COS文件上传工具类
 *
 * @author coatardbul
 */
@Slf4j
@Service
public class CosService {

    @Autowired
    CosConfig cosConfig;

    private COSClient cosClient;

    @Autowired
    public void setClientConfig() {
        // 1 初始化用户身份信息（secretId, secretKey）。
        // SECRETID和SECRETKEY请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi 进行查看和管理
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        // 2 设置 bucketName 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(cosConfig.getRegionName());
        ClientConfig clientConfig = new ClientConfig(region);
        // 这里建议设置使用 https 协议
        // 从 5.6.54 版本开始，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 3 生成 cos 客户端。
        COSClient cosClient = new COSClient(cred, clientConfig);
        this.cosClient = cosClient;
    }


    public void mkdir(String path) {
        String key = path+"/";

        // 这里创建一个空的 ByteArrayInputStream 来作为示例
        byte data[] = new byte[0];
        InputStream inputStream = new ByteArrayInputStream(data);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0);

        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucketName(), key, inputStream, objectMetadata);
        cosClient.putObject(putObjectRequest);

    }


    public String upload(String path, MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String key = path + originalFilename;
        File localFile = null;
        try {
            localFile = transferToFile(file);
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucketName(), key, localFile);
            cosClient.putObject(putObjectRequest);
            URL objectUrl = cosClient.getObjectUrl(cosConfig.getBucketName(), key);
            localFile.delete();
            return objectUrl.getHost() + objectUrl.getPath();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new Exception("文件上传失败");
        } finally {
            localFile.delete();
        }
    }

    /**
     * 用缓冲区来实现这个转换, 即创建临时文件
     * 使用 MultipartFile.transferTo()
     *
     * @param multipartFile
     * @return
     */
    private static File transferToFile(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String prefix = originalFilename.split("\\.")[0];
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        File file = File.createTempFile(prefix, suffix);
        multipartFile.transferTo(file);
        return file;
    }

    public void delete(String path, String fileName) {
        cosClient.deleteObject(cosConfig.getBucketName(), path + fileName);
    }


    public List<FileBo> getPathInfo(String path) {
        List<FileBo> result = new ArrayList<>();
        // Bucket的命名格式为 BucketName-APPID ，此处填写的存储桶名称必须为此格式
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        // 设置bucket名称
        listObjectsRequest.setBucketName(cosConfig.getBucketName());
        // prefix表示列出的object的key以prefix开始
        listObjectsRequest.setPrefix(path);
//        // deliter表示分隔符, 设置为/表示列出当前目录下的object, 设置为空表示列出所有的object
        listObjectsRequest.setDelimiter("/");
        // 设置最大遍历出多少个对象, 一次listobject最大支持1000
        listObjectsRequest.setMaxKeys(1000);
        ObjectListing objectListing = null;
        do {
            try {
                objectListing = cosClient.listObjects(listObjectsRequest);
            } catch (CosServiceException e) {
                e.printStackTrace();
            } catch (CosClientException e) {
                e.printStackTrace();
            }

            // object summary表示所有列出的object列表
            List<COSObjectSummary> cosObjectSummaries = objectListing.getObjectSummaries();
            for (COSObjectSummary cosObjectSummary : cosObjectSummaries) {
                // 文件的路径key
                String keyTemp = cosObjectSummary.getKey();
                FileBo fileBo = new FileBo();
                URL objectUrl = cosClient.getObjectUrl(cosConfig.getBucketName(), keyTemp);
                fileBo.setUrl(objectUrl.getHost() + objectUrl.getPath());
                fileBo.setFileName(keyTemp);
                fileBo.setFileType("2");
                result.add(fileBo);
            }
            // object summary表示所有列出的object列表
            List<String> commonPrefixes = objectListing.getCommonPrefixes();
            for (String contentPath : commonPrefixes) {
                FileBo fileBo = new FileBo();
                fileBo.setFileName(contentPath);
                fileBo.setFileType("1");
                result.add(fileBo);
            }

            String nextMarker = objectListing.getNextMarker();
            listObjectsRequest.setMarker(nextMarker);
        } while (objectListing.isTruncated());

        return result;
    }


}
