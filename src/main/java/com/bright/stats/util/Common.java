package com.bright.stats.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class Common {
	/**
	 * q
	 * 2018.06.20
	 * 函数替换
	 *  
	 *  格式：%{len}
	 */
	public static String replaceFun(String sql){
		if(null==DataConstants.funContrast || DataConstants.funContrast.size()==0)return sql;
		for (Map<String, Object> map : DataConstants.funContrast) {
			if(sql.indexOf("${")==-1)break;
			if(DataConstants.ISMYSQL==1){
				sql=sql.toLowerCase().replace("${"+map.get("sql_fun").toString().toLowerCase()+"}", map.get("mysql_fun").toString().toLowerCase());
			}else{
				sql=sql.toLowerCase().replace("${"+map.get("sql_fun").toString().toLowerCase()+"}", map.get("sql_fun").toString().toLowerCase());
			}
		}
		return sql;
	}
	
	/**
	 * q
	 * 2018.06.20
	 * 函数替换
	 *  
	 *  格式：%{len}
	 */
	public static String replaceFun(String sql,List<Map<Object,Object>> raDatas){
		if(null==raDatas || raDatas.size()==0)return sql;
		for (Map<Object, Object> map : raDatas) {
			if(sql.indexOf("${")==-1)break;
			if(StringUtils.equalsIgnoreCase(DataConstants.SQLTYPE,"dm")){
				String aa =  map.get("sqlstr").toString();
				System.out.println(aa);
				sql=sql.toLowerCase().replace("${"+map.get("mysql_fun").toString().toLowerCase()+"}",map.get("sqlstr").toString());
			}else{
				if(DataConstants.ISMYSQL==1){
					sql=sql.toLowerCase().replace("${"+map.get("mysql_fun").toString().toLowerCase()+"}",map.get("valuedec").toString());
				}else{
					sql=sql.toLowerCase().replace("${"+map.get("sql_fun").toString().toLowerCase()+"}",map.get("valuedec").toString());
				}
			}

		}
		return sql;
	}


	public static String replaceFun1(String sql,List<Map<String,Object>> raDatas){
		if(null==raDatas || raDatas.size()==0)return sql;
		for (Map<String, Object> map : raDatas) {
			if(sql.indexOf("${")==-1)break;
			if(StringUtils.equalsIgnoreCase(DataConstants.SQLTYPE,"dm")){
				String aa =  map.get("sqlstr").toString();
				System.out.println(aa);
				sql=sql.toLowerCase().replace("${"+map.get("mysql_fun").toString().toLowerCase()+"}",map.get("sqlstr").toString());
			}else{
				if(DataConstants.ISMYSQL==1){
					sql=sql.toLowerCase().replace("${"+map.get("mysql_fun").toString().toLowerCase()+"}",map.get("valuedec").toString());
				}else{
					sql=sql.toLowerCase().replace("${"+map.get("sql_fun").toString().toLowerCase()+"}",map.get("valuedec").toString());
				}
			}

		}
		return sql;
	}
	
	/*
	 * q
	 * 2014.10.13
	 * 
	 *  替换参数 键值key不能为空
	 *  格式：：a
	 */
	public static String replaceParamskeynoempty(String sqlstr, Map map) {
		String tmpsql = sqlstr;
		String escaped = "";
		Iterator iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			Object key = iterator.next();
			String keystr=key.toString();
			if(null==key || StringUtils.isEmpty(keystr))continue;
			escaped = Pattern.quote(':'+keystr);
			tmpsql = tmpsql.replaceAll(escaped, (map.get(key)==null)?"":map.get(key).toString());
		}
		return tmpsql;
	}
	
	
	/*
	 * q
	 * 2014.10.14
	 * 
	 * 替换参数 键值key不能为空
	 * 格式：#a#
	 */
	public static String replaceParamsbyKeyNotNull(String sqlstr, Map map) {
		String tmpsql = sqlstr.toLowerCase();
		try {
			String escaped = "";
			Iterator iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				Object key = iterator.next();
				String keystr=key.toString().toLowerCase();
				if(null==key || StringUtils.isEmpty(keystr))continue;
				escaped = Pattern.quote('#'+keystr+'#');
				tmpsql = tmpsql.replaceAll(escaped, (map.get(key)==null)?"":map.get(key).toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmpsql;
	}
	
	
	/*
	 * 2014.10.14
	 * q
	 * 替换参数
	 * 格式：#a#
	 * return StringBuffer
	 */
	public static StringBuffer replaceParamsbyStrBuf(StringBuffer sqlstr, Map map) {
		StringBuffer tmpsql = new StringBuffer(sqlstr.toString().toLowerCase());
		String value="";
		try {
			String escaped = "";
			Iterator iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				Object key = iterator.next();
				String keystr=key.toString().toLowerCase();
				if(null==key || StringUtils.isEmpty(keystr))continue;
				escaped = Pattern.quote('#'+keystr+'#');
				value=(map.get(key)==null || StringUtils.isEmpty(map.get(key).toString()))?"0":map.get(key).toString();
				if(value.indexOf("'")>0)value=replaceString(value, "'", "''", 0, value.length());
				tmpsql = new StringBuffer(tmpsql.toString().replaceAll(escaped,value));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmpsql;
	}
	
	/*
	 * 2017.09.12
	 * q
	 * 替换参数
	 * 格式：#a# replaceString替换格式不同
	 * return StringBuffer
	 */
	public static StringBuffer replaceParamToStrBuf(StringBuffer sqlstr, Map map) {
		StringBuffer tmpsql = new StringBuffer(sqlstr.toString().toLowerCase());
		String value="";
		try {
			String escaped = "";
			Iterator iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				Object key = iterator.next();
				String keystr=key.toString().toLowerCase();
				if(null==key || StringUtils.isEmpty(keystr))continue;
				escaped = Pattern.quote('#'+keystr+'#');
				value=(map.get(key)==null || StringUtils.isEmpty(map.get(key).toString()))?"0":map.get(key).toString();
				//if(value.indexOf("'")>0)value=replaceString(value, "'", "''", 0, value.length());
				tmpsql = new StringBuffer(tmpsql.toString().replaceAll(escaped,value));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tmpsql;
	}
	
	
	/*
	 * 
	 * @author q
	 * @Description: TODO 替换单引号成两个单引号，首单引号和尾单引号不替换
	 * @param str
	 * @param rstr
	 * @param searchStr
	 * @param a
	 * @param b
	 * @return
	 * @date 2015-6-26下午03:41:38
	 */
	 private static String replaceString(String str, String searchStr,String rstr, int a,int b) {
		    String tems=str.substring(a+1,b-1);
		    tems=tems.replace(searchStr, rstr);
		    return str.replace(str.substring(a+1,b-1), tems);//str.substring(0, index) + rstr + str.substring(index + 1);
		    }
	
	/*
	 * q
	 * 2015.3.25
	 * 替换参数：
	 * 格式：[%a]
	 * return string
	 */
	@SuppressWarnings("unchecked")
	public static String paramChange(String str,Map map){
		try {
			String escaped="";
			Iterator iterator=map.keySet().iterator();
			while(iterator.hasNext()){
				Object key=iterator.next();
				if(null==key)continue;
				String keystr="[%"+key.toString().toLowerCase()+"]";
				if(StringUtils.isEmpty(keystr))continue;
				
				escaped=Pattern.quote(keystr);
				str=str.replaceAll(escaped, (map.get(key)==null || StringUtils.isEmpty(map.get(key).toString()))?"null":map.get(key).toString());
			}
			
		} catch (Exception e) {
			 log.error(" paramChange error:"+e);
		}
		return str;
	}
	
	/**
	 * 
	 * @author q
	 * @Description: TODO
	 * @param str
	 * @param map
	 * @return StringBuffer 类型的字符串
	 * @date 2016-12-14下午02:38:17
	 */
	public static StringBuffer replaceByMap(String str,Map map){
		StringBuffer strbf=new StringBuffer(str);
		for (Object key : map.keySet()) {
			if(null==key)continue;
			strbf.replace(0,strbf.length(),StringUtils.replace(strbf.toString(), "${"+key.toString().toLowerCase()+"}",(null==map.get(key) || StringUtils.isEmpty(map.get(key).toString()))?"": map.get(key).toString()));
		} 
		return strbf;
	}
}
