package com.coatardbul.stock.common.util;

import javax.validation.constraints.NotBlank;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColletionUtil<T> {

    /**
     * 将List转换成map
     *
     * @param key
     * @param list
     * @return
     * @throws IllegalAccessException
     */
    public Map<String, T> convertListToMap(@NotBlank String key, List<T> list) throws IllegalAccessException {
        Map<String, T> map = new HashMap<>();
        for (T t : list) {
            //得到所有属性
            Field[] fields = t.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                //得到属性
                Field field = fields[i];
                //打开私有访问
                field.setAccessible(true);
                //获取属性
                String name = field.getName();
                //key
                if (key.equals(name)) {
                    map.put((String) field.get(t), t);
                }
            }
        }
        return map;
    }
}
