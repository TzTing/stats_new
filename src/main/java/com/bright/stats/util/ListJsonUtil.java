package com.bright.stats.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ListJsonUtil {

	
	
	/**
	 * json字符串转成map
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<Object,Object> JsonToMap(JSONObject json){
		Map<Object,Object> param=new HashMap<Object, Object>();
//		if(StringUtils.isNotEmpty(str))
//		{
//		 JSONObject json=JSONObject.fromObject(str);
		if(json==null)return null;
		 Iterator<Object> iterator=json.keys();
		 while (iterator.hasNext()) {
			Object key=iterator.next();
			Object value=json.get(key);
			key=key.toString().toLowerCase();
			param.put(key,value);
		}
//		}
		return param;
	}
	
	/**
	 * json字符串转成map
	 * 格式：｛"a":"1","b":"2"｝
	 * key 转小写
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<Object,Object> queryMapJson(String str){
		Map<Object,Object> param=new HashMap<Object, Object>();
		if(StringUtils.isNotEmpty(str)){
			 JSONObject json=JSONObject.fromObject(str);
			 Iterator<Object> iterator=json.keys();
			 while (iterator.hasNext()) {
				Object key=iterator.next();
				Object value=json.get(key);
				key=key.toString().toLowerCase();
				param.put(key, value);
			}
		}
		return param;
	}
	
	/**
	 * 
	 * @author q 
	 * @Description: 格式 "a":"1","b":"2" 或 c:1,e:2
	 * @param str
	 * @return
	 * @date 2018-7-25下午04:07:00
	 */
	public static Map<Object,Object> queryMap5Bystr(String str){
		if (!str.trim().startsWith("{")){
			str='{' + str + '}';
		}
		str = str.replace("{", "{\"");
		str = str.replace(":", "\":\"");
		str = str.replace(",", "\",\"");
		str = str.replace("}", "\"}");
		return queryMapJson(str);
	}
	
	/**
	 * json字符串转成map 
	 * 例如：{'acc_code':'212','field':'bal_year_begin'}
	 * @return map
	 */
	public static Map<Object,Object> queryMapByStr(String str){
		Map<Object,Object> param=new HashMap<Object, Object>();
		if(StringUtils.isNotEmpty(str))
		{
		 JSONObject json=JSONObject.fromObject(str);
		 Iterator<Object> iterator=json.keys();
		 while (iterator.hasNext()) {
			Object key=iterator.next();
			Object value=json.get(key);
			String temp=value.toString();
			if(temp.indexOf(",")>=0)
			{
			String[] array=temp.split(",");
			String val="";
			for (int i = 0; i < array.length; i++) {
				val+=array[i];
				val+="','";
			}
			val=val.substring(0,val.length()-3);
			value=val;
			}
			key=key.toString().toLowerCase();
			param.put(key, value);
		}
		}
		return param;
	}
	
	
	/**
	 * json字符串转成map
	 * 格式：[{"a":"1","b":"2"}]
	 * @return
	 */
	public static List<Map<Object,Object>> JsonArrayToListMap(JSONArray array){
		List<Map<Object,Object>> listMap=new ArrayList<Map<Object,Object>>();
		for (Object obj : array) {
			if(obj instanceof JSONObject){
				listMap.add(JsonToMap((JSONObject)obj));
			}
		}
		return listMap;
	}
	
	/**
	 * 
	 * @author q
	 * @Description: TODO
	 * @param arrsy
	 * 格式：[{"a":"1","b":"2"}]
	 * @return
	 * @date 2016-12-15上午11:04:50
	 */
	public static List<Map<Object,Object>> jsonArrayToListMap(String arrsy){
		List<Map<Object,Object>> listMap=new ArrayList<Map<Object,Object>>();
		JSONArray array=JSONArray.fromObject(arrsy);
		for (Object object : array) {
			if(object instanceof JSONObject){
				listMap.add(JsonToMap((JSONObject)object));
			}
		}
		return listMap;
	}
	
	
	/*
	 * 
	 * @author q
	 * @Description:对一个javaBean对象转化成map
	 * @param bean
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @date 2016-4-8下午05:06:26
	 */
	public static Map<Object,Object> queryMapByBean(Object bean) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Map<Object,Object> map=new HashMap<Object, Object>();
		Class type =bean.getClass();
		 BeanInfo beanInfo = Introspector.getBeanInfo(type); 
		 PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors(); 
	        for (int i = 0; i< propertyDescriptors.length; i++) { 
	            PropertyDescriptor descriptor = propertyDescriptors[i]; 
	            String propertyName = descriptor.getName(); 
	            if (!propertyName.equals("class")) { 
	                Method readMethod = descriptor.getReadMethod(); 
	                Object result = readMethod.invoke(bean, new Object[0]); 
	                if (result != null) { 
	                	map.put(propertyName, result); 
	                } else { 
	                	map.put(propertyName, ""); 
	                } 
	            } 
	        } 
		return map;
		
	}
	
	
	/*
	 * 
	 * @author q
	 * @Description:对一个javaBean对象转化成map,key大写
	 * @param bean
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @date 2016-4-8下午05:06:26
	 */
	public static Map<Object,Object> queryMapByBean(Object bean,String str) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Map<Object,Object> map=new HashMap<Object, Object>();
		Class type =bean.getClass();
		 BeanInfo beanInfo = Introspector.getBeanInfo(type); 
		 PropertyDescriptor[] propertyDescriptors =  beanInfo.getPropertyDescriptors(); 
	        for (int i = 0; i< propertyDescriptors.length; i++) { 
	            PropertyDescriptor descriptor = propertyDescriptors[i]; 
	            String propertyName = descriptor.getName(); 
	            if (!propertyName.equals("class")) { 
	                Method readMethod = descriptor.getReadMethod(); 
	                Object result = readMethod.invoke(bean, new Object[0]); 
	                if (result != null) { 
	                	map.put(propertyName.toUpperCase(), result); 
	                } else { 
	                	map.put(propertyName.toUpperCase(), ""); 
	                } 
	            } 
	        } 
		return map;
		
	}
	
	/**
	 * 
	 * @author q
	 * @Description: key相同的map,合并value
	 * @param lists
	 * @return
	 * @date 2017-9-28下午07:44:18
	 */
	public static Map<Object,Object> mapCombine(List<Map<Object,Object>> lists){
		Map<Object,Object> map=new HashMap<Object, Object>();
		
		for (Map<Object, Object> m : lists) {
			Iterator<Object>it=m.keySet().iterator();
			while(it.hasNext()){
				Object key=it.next();
				if(!map.containsKey(key)){
					Map<Object,Object> list=new HashMap<Object, Object>();
					list.putAll((Map<Object,Object>)m.get(key));
					map.put(key,list);
				}else{
					((Map<Object,Object>)map.get(key)).putAll((Map<Object,Object>)m.get(key));
				}
			}
		}
		
		return map;
	}
	

	/**
	 * 功能：将json数据转换成List<Map> 参数：json 说明：json的格式如下
	 * {"description":"123","acc_code":"213","acc_name":"","debit":"","credit":"","linkno":""}
	 * 
	 * 是对原来的getMap4Json的扩展
	 * @return
	 */
	public static List<Map<Object,Object>>  getMap4Json(String jsonString) {
		List datalst = new ArrayList();
		if (!jsonString.trim().startsWith("[")){
			jsonString='[' + jsonString + ']';
		}
		JSONArray ja = JSONArray.fromObject(jsonString);
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jsonObject = ja.getJSONObject(i);
			Iterator keyIter = jsonObject.keys();
			String key;
			Object value;
			Map valueMap = new HashMap();
			while (keyIter.hasNext()) {
				key = (String) keyIter.next();
				value = jsonObject.get(key);
				key = key.toLowerCase();
				valueMap.put(key, value);
			}
			datalst.add(valueMap);
		}
		return datalst;
	}
	
	// 该过程将数据中检索到得数据转换成json其中lstdata的第一个list存放的是字段名
	public static String dataToJson(List<List<Object>> lstdata, int total) {
		List lsthead = new ArrayList();
		String jsonstr = "";
		String jsonsub = "";
		String tmpv = "";
		for (int i = 0; i < lstdata.size(); i++) {
			// ---第一个list存放的是字段名称
			if (i == 0) {
				lsthead = lstdata.get(0);
			} else {
				jsonsub = "";
				for (int j = 0; j < lsthead.size(); j++) {
					tmpv = lstdata.get(i).get(j).toString();
					tmpv = NokFunc.replaceEx(tmpv, "\"", "“");
					tmpv = NokFunc.replaceEx(tmpv, "\'", "‘");
					tmpv = NokFunc.replaceEx(tmpv, "\\", "\\\\\\\\");
					if (jsonsub.length() > 0) {
						jsonsub = jsonsub + ",\"" + lsthead.get(j).toString()
								+ "\":\"" + tmpv + "\"";

					} else {
						jsonsub = jsonsub + "\"" + lsthead.get(j).toString()
								+ "\":\"" + tmpv + "\"";
					}
				}

				if (jsonstr.length() == 0) {
					jsonstr = "{" + jsonsub + "}";
				} else {
					jsonstr = jsonstr + ",{" + jsonsub + "}";
				}
			}
		}
		if (total > 0) {
			jsonstr = "{" + "\"total\":" + total + "," + "\"rows\":" + "["
					+ jsonstr + "]}";
		} else {
			jsonstr = "[" + jsonstr + "]";
		}
		return jsonstr;
	}
}
