package com.coatardbul.stock.common.util;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReflexUtil {


    /**
     * 根据name设置对应的value
     *
     * @param name,value,Object
     */
    public static <T> void setAttributeValue(String name, Object value, Object object) throws IllegalAccessException {
        recurveSetAttributeValue(name, value, object, object.getClass());
    }

    public static <T> void recurveSetAttributeValue(String name, Object value, Object object, Class<T> tClass) throws IllegalAccessException {
        if (tClass != Object.class) {
            singleSetAttributeValue(name, value, object, tClass);
            recurveSetAttributeValue(name, value, object, tClass.getSuperclass());
        }
    }

    public static <T> void singleSetAttributeValue(String name, Object value, Object object, Class<T> tClass) throws IllegalAccessException {

        //得到所有属性
        Field[] fields = tClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {//遍历
            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String nameTemp = field.getName();
            if (name.contains("_")) {
                name = name.replace("_", "");
            }
            if (nameTemp.equalsIgnoreCase(name)) {
                String attributeTypeFullName = field.getGenericType().toString().trim();
                String attributeType = attributeTypeFullName.substring(attributeTypeFullName.lastIndexOf(".") + 1);
                if ("baseJava/string".equals(attributeType)) {
                    field.set(object, value);
                } else if ("Long".equals(attributeType)) {
                    if (value == null) {
                        field.set(object, 0L);
                    } else {
                        field.set(object, Long.parseLong(value.toString()));
                    }
                } else if ("Double".equals(attributeType)) {
                    if (value == null) {
                        field.set(object, 0.00);
                    } else {
                        field.set(object, Double.parseDouble(value.toString()));
                    }
                }
            }
        }


    }

    /**
     * @param obj
     * @return 读取当前对象的属性值的总数
     */
    public static int readAttributeCount(Object obj) {
        return recurveReadAttributeCount(obj.getClass());
    }

    public static <T> int recurveReadAttributeCount(Class<T> tClass) {
        int num = 0;
        if (tClass != Object.class) {
            num += singleReadAttributeCount(tClass);
            num += recurveReadAttributeCount(tClass.getSuperclass());
        }
        return num;
    }

    /**
     * @param obj
     * @return 读取当前对象的属性值的总数
     */
    public static void singleReadAStringAttributeList(Object obj,List<String> result) throws IllegalAccessException {
        //得到所有属性
        Field[] fields = obj.getClass().getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {//遍历
            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String name = field.getName();
            //获取属性值  .get(object)
            Object ownValue = field.get(obj);

            if(field.getGenericType().toString().contains("String")  ) {
                if(ownValue!=null){
                    result.add((String) ownValue);
                }
            }
        }
    }






    public static <T> int singleReadAttributeCount(Class<T> tClass) {
        //得到所有属性
        return tClass.getDeclaredFields().length;
    }

    /**
     * 比较两个对象不同属性的个数
     *
     * @param ownObject
     * @param newObject
     * @return
     */
    public static int compareTwoObject(Object ownObject, Object newObject) throws IllegalAccessException {
        return recurveCompareTwoObject(ownObject, newObject, ownObject.getClass());
    }

    public static <T> int recurveCompareTwoObject(Object ownObject, Object newObject, Class<T> tClass) throws IllegalAccessException {
        int num = 0;
        if (tClass != Object.class) {
            num += singleCompareTwoObject(ownObject, newObject, tClass);
            num += recurveCompareTwoObject(ownObject, newObject, tClass.getSuperclass());
        }
        return num;
    }

    public static <T> int singleCompareTwoObject(Object ownObject, Object newObject, Class<T> tClass) throws IllegalAccessException {
        int num = 0;
        //得到所有属性
        Field[] fields = tClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {//遍历
            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String name = field.getName();
            //获取属性值
            Object ownValue = field.get(ownObject);
            Object newValue = field.get(newObject);
            if (ownValue != null && newValue != null) {
                if (!(ownValue.toString().equals(newValue.toString()))) {
                    num++;
                }
            } else if (ownValue == null && newValue == null) {
            } else {
                num++;
            }
        }

        return num;

    }

    /**
     * @param preparedStatement
     * @param object
     * @param cutParam
     */
    public static <T> void setObjectToPreparedStatement(PreparedStatement preparedStatement, Object object, List<String> cutParam) throws SQLException, IllegalAccessException {
        recurveSetObjectToPreparedStatement(preparedStatement, object, object.getClass(), cutParam);
    }

    public static <T> void recurveSetObjectToPreparedStatement(PreparedStatement preparedStatement, Object object, Class<T> tClass, List<String> cutParam) throws SQLException, IllegalAccessException {
        if (tClass != Object.class) {
            singleSetObjectToPreparedStatement(preparedStatement, object, tClass, cutParam);
            recurveSetObjectToPreparedStatement(preparedStatement, object, tClass.getSuperclass(), cutParam);
        }

    }

    public static <T> void singleSetObjectToPreparedStatement(PreparedStatement preparedStatement, Object object, Class<T> tClass, List<String> cutParam) throws SQLException, IllegalAccessException {
        //得到class
        Class cls = object.getClass();
        //得到所有属性
        Field[] fields = cls.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {//遍历
            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String name = field.getName();
            //获取属性值
            Object value = field.get(object);
            if (!cutParam.contains(name)) {
                preparedStatement.setObject(i + 1, value);
            }
        }

    }

    /**
     * @param name
     * @param object
     * @param <T>
     * @return 根据name读取对象的值
     */
    public static <T> Object readValueByName(String name, Object object) throws IllegalAccessException {
        return recurveReadValueByName(name, object, object.getClass());
    }

    public static <T> Object recurveReadValueByName(String name, Object object, Class<T> tClass) throws IllegalAccessException {
        if (tClass != Object.class) {
            Object objectTemp = singleReadValueByName(name, object, tClass);
            if (objectTemp == null) {
                return recurveReadValueByName(name, object, tClass.getSuperclass());
            } else {
                return objectTemp;
            }
        }
        return null;
    }

    public static <T> Object singleReadValueByName(String name, Object object, Class<T> tClass) throws IllegalAccessException {
        Object value = null;
        //得到所有属性
        Field[] fields = tClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {//遍历
            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String nametemp = field.getName();
            if (nametemp.equalsIgnoreCase(name)) {
                value = field.get(object);
            }
        }

        return value;
    }

    /**
     * 打印对象相关的属性
     *
     * @param object
     * @param <T>
     */
    public static <T> void readAttributeValuePrint(Object object) throws IllegalAccessException {
        recurveReadAttributeValuePrint(object, object.getClass());
    }

    public static <T> void recurveReadAttributeValuePrint(Object object, Class<T> tClass) throws IllegalAccessException {
        if (tClass != Object.class) {
            singleReadAttributeValuePrint(object, tClass);
            recurveReadAttributeValuePrint(object, tClass.getSuperclass());
        }
    }

    public static <T> void singleReadAttributeValuePrint(Object object, Class<T> obj) throws IllegalAccessException {
        //得到所有属性
        Field[] fields = obj.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {//遍历

            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String name = field.getName();
            //获取属性值
            Object value = field.get(object);
            System.out.print(name + ";" + value);
            System.out.print("-----");
            System.out.print(field.getGenericType().getClass().isPrimitive());
            System.out.print("-----");
            System.out.println(field.getGenericType().toString());

        }
    }

    /**
     * 将map中对应的属性放到对象中
     */
    public  static  void setMaptoObject(Map<String,Object> map, Object object) throws IllegalAccessException {
        recurveSetMaptoObject(map,object,object.getClass());
    }

    public  static <T> void recurveSetMaptoObject(Map< String, Object > map, Object
            object, Class< T > tClass) throws IllegalAccessException {
        if (tClass != Object.class) {
            singleSetMaptoObject(map,object,tClass);
            recurveSetMaptoObject(map,object,tClass.getSuperclass());

        }
    }
    public  static <T> void singleSetMaptoObject(Map< String, Object > map, Object
            object, Class< T > tClass) throws IllegalAccessException {
        Field[] fields = tClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String name = field.getName();
            if(map.containsKey(name)&&(map.get(name)!=null && !"".equals(map.get(name)))){
                if(field.getGenericType().toString().equals(map.get(name).getClass().toString())){
                    field.set(object,map.get(name));
                }else {
                    if(field.getGenericType().toString().contains("Long") &&map.get(name).getClass().toString().contains("baseJava/string") ){
                        field.set(object,Long.parseLong((String) map.get(name)));
                    }else if(field.getGenericType().toString().contains("Integer") &&map.get(name).getClass().toString().contains("baseJava/string")){
                        field.set(object,Integer.parseInt((String) map.get(name)));
                    }else if(field.getGenericType().toString().contains("Long") &&map.get(name).getClass().toString().contains("Integer")){
                        field.set(object,((Integer)map.get(name)).longValue());
                    }
                }
            }
        }

    }

    /**
     * 读取当前对象的所有属性，包括父类的属性
     */
    public static Map<String, Object> readObjectAllAttribute (Object object) throws IllegalAccessException {
        Map<String, Object> map = new HashMap<>();
        recurveReadObjectAllAttribute(map,object,object.getClass());
        return map;
    }
    public static <T > void recurveReadObjectAllAttribute (Map< String, Object > map, Object
            object, Class< T > tClass) throws IllegalAccessException {
        if (tClass != Object.class) {
            singleReadObjectAllAttribute(map,object,tClass);
            recurveReadObjectAllAttribute(map,object,tClass.getSuperclass());

        }
    }
    public static <T > void singleReadObjectAllAttribute (Map< String, Object > map, Object
            object, Class< T > tClass) throws IllegalAccessException {
        //得到所有属性
        Field[] fields = tClass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {//遍历
            //得到属性
            Field field = fields[i];
            //打开私有访问
            field.setAccessible(true);
            //获取属性
            String name = field.getName();
            //获取属性值
            Object value = field.get(object);
            map.put(name,value);
        }
    }
}
