package com.coatardbul.stock.common.util;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.coatardbul.stock.common.exception.BusinessException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUtil() {
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static <T> T readToValue(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException var3) {
            throw new BusinessException( var3.getMessage());
        }
    }

//    public static <T> Result<T> readToResult(String json, Class<T> clazz) {
//        try {
//            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(Result.class, new Class[]{clazz});
//            return (Result)objectMapper.readValue(json, javaType);
//        } catch (Exception var3) {
//            throw new BusinessException(CommonResultCodeEnum.JSON_READ_ERROR, var3);
//        }
//    }

    public static <T> T readToValue(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception var3) {
            throw new BusinessException(var3.getMessage());
        }
    }
//
//    public static <T> ResultList<T> readToResultList(String json, Class<T> clazz) {
//        try {
//            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ResultList.class, new Class[]{clazz});
//            return (ResultList)objectMapper.readValue(json, javaType);
//        } catch (Exception var3) {
//            throw new BusinessException(CommonResultCodeEnum.JSON_READ_ERROR, var3);
//        }
//    }
//
//    public static JsonNode readToJsonNode(String json) {
//        try {
//            return objectMapper.readTree(json);
//        } catch (IOException var2) {
//            throw new BusinessException(CommonResultCodeEnum.JSON_READ_ERROR, var2);
//        }
//    }
//
//    public static <T> List<T> readToList(String json, Class<T> clazz) {
//        try {
//            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(ArrayList.class, new Class[]{clazz});
//            return (List)objectMapper.readValue(json, javaType);
//        } catch (Exception var3) {
//            throw new BusinessException(CommonResultCodeEnum.JSON_READ_ERROR, var3);
//        }
//    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception var2) {
            throw new BusinessException( var2.getMessage());
        }
    }

    static {
        objectMapper.configure(Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }
}
