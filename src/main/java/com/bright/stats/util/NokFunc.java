package com.bright.stats.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NokFunc {

	
	// 替换字符串区分大小写
	public static String replaceEx(String source, String repstr, String value) {
		return replaceEx(source, repstr, value, false);
	}
	
	// 替换字符串，根据IgnoreCase标志确定是否区分大小写
	public static String replaceEx(String source, String oldstring,
			String newstring, boolean IgnoreCase) {
		String escaped = Pattern.quote(oldstring);
		;
		Matcher m;
		if (IgnoreCase) {
			m = Pattern.compile(escaped, Pattern.CASE_INSENSITIVE).matcher(
					source);
		} else {
			m = Pattern.compile(escaped, Pattern.CANON_EQ).matcher(source);
		}
		String result = m.replaceAll(newstring);
		return result;
	}
	
	public static String replaceGrdData(String source) {
		String rvalue = source;

		rvalue = replaceEx(rvalue, "\n\r", "<br>  ");
		rvalue = replaceEx(rvalue, "\r\n", "<br>  ");
		rvalue = replaceEx(rvalue, "\t", "    ");
		rvalue = replaceEx(rvalue, " ", " ");
		rvalue = replaceEx(rvalue, "\"", "\\" + "\"");
		return rvalue;
	}
	
	// 得到substr在sourcestr中出现的次数
	public static int substrCount(String sourcestr, String substr) {
		int count = 0, start = 0;
		while (start != sourcestr.length()) {
			int i = sourcestr.indexOf(substr, start);
			if (i != -1) {
				count++;
				start = i + 1;
			} else
				break;
		}
		return count;
	}
	
	/**
	 * 函数名称 getSessionMap 参数 request 描述
	 * 按“[_“+session对应的键值返回session对应的hashmap，用于系统替换操作
	 * 
	 * @author noksoft
	 * 
	 */
	public static Map getSessionMap(HttpServletRequest request) {
		Map sessionmap = new HashMap();
		HttpSession se = request.getSession();
		Enumeration en = se.getAttributeNames();
		String key = "", value = "";
		while (en.hasMoreElements()) {
			key = en.nextElement().toString();
			value = SessionUnit.getSessionValue(request, key);
			sessionmap.put("[_" + key + "]", value);
		}
		return sessionmap;
	}
	
	/**
	 * 函数名称 replaceFromMap 参数 source表示替换的源字符串，values表示需替换的信息 如source=select *
	 * from aa where :aa and :bb=cc,values中只要包含键aa值为1、键bb值为2就可以进行替换成：select *
	 * from aa where 1 and 2=cc
	 * 
	 * @param source
	 *            ：表示替换的源字符串
	 * @param values
	 *            ：表示需替换的信息,map类型
	 * @return
	 * @author noksoft
	 * @version 0.1
	 */
	@Deprecated
	public static String replaceFromMap(String source, Map values) {
		if (null == values) {
			return source;
		}
		String rvalue = source;
		String escaped = "", escaped1 = "";
		/**
		 * old 2015-01-05 Iterator iterator = values.keySet().iterator(); while
		 * (iterator.hasNext()) { Object key = iterator.next(); if
		 * (key.toString().trim().length() == 0) { continue; } escaped =
		 * Pattern.quote(":" + key.toString()); rvalue =
		 * rvalue.replaceAll(escaped, values.get(key).toString()); }
		 */
		Iterator iterator = values.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Object key = entry.getKey();
			if (key.toString().trim().length() == 0) {
				continue;
			}
			escaped = Pattern.quote(":" + key.toString());
			escaped1 = Pattern.quote(":[" + key.toString() + "]");
			rvalue = rvalue.replaceAll(escaped1, values.get(key).toString());

			rvalue = rvalue.replaceAll(escaped, values.get(key).toString());
		}
		return rvalue;
	}
	
	
	/**
	 * replaceFromMap 字符串字符进行替换 如source=select * from aa where :aa and
	 * :bb=cc,hasFormate=false 则 values中只要包含键aa值为1、键bb值为2就可以进行替换成：select * from
	 * aa where 1 and
	 * 2=cc；hasFormate=true则values中只要包含键:aa值为1、键:bb值为2才可以进行替换成：select * from aa
	 * where 1 and
	 * 2=cc，该方法主要是为了处理键值已经封装好了不需要二次处理的替换，如键值为[aa],[bb],[:aa],[:bb]等格式
	 * 
	 * @param source
	 *            ：表示替换的源字符串
	 * @param values
	 *            ：表示需替换的信息,map类型
	 * @param hasFormate
	 *            ：表示values中是否已经包含用户所需的格式了，true表示已经包含
	 * @return
	 * @author noksoft
	 * @version 0.1
	 */
	public static String replaceFromMap(String source, Map values,
			Boolean hasFormate) {
		if (null == values) {
			return source;
		}
		String rvalue = source;
		if (!hasFormate) {
			rvalue = replaceFromMap(source, values);
		} else {
			String escaped = "";
			int tmpindex = 0;
			Iterator iterator = values.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				Object key = entry.getKey();
				if (key.toString().trim().length() == 0) {
					continue;
				}
				tmpindex = key.toString().indexOf(":");
				if ((key.toString().indexOf(":") == -1)
						&& (key.toString().indexOf("[") == -1)) {
					continue;
				}
				escaped = Pattern.quote(key.toString());
				rvalue = rvalue.replaceAll(escaped, values.get(key).toString());
			}
			// --------------------------------
		}
		return rvalue;
	}
	
	/**
	 * 函数名称 replaceFromMap 参数 source表示替换的源的Hash，values表示需替换的信息,
	 * hasFormate表示values中是否已经包含用户所需的格式了 描述 将原字符串中的变量用 values对应的键的值进行替换
	 * autoFormate表示将key值格式化成[key]模式
	 * 
	 * @author noksoft
	 * 
	 */
	public static void replaceFromMap(Map source, Map values,
			Boolean hasFormate, Boolean autoFormate) {
		Iterator iterator = source.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Object key = entry.getKey();
			source.put(key, replaceFromMap(source.get(key).toString(), values,
					hasFormate, autoFormate));
		}
		// --------------------------------
	}
	
	 
	/**
	 * 函数名称 replaceFromMap 参数 source表示替换的源字符串，values表示需替换的信息,
	 * hasFormate表示values中是否已经包含用户所需的格式了 描述 将原字符串中的变量用 values对应的键的值进行替换
	 * autoFormate表示将key值格式化成[key]模式
	 * 
	 * @param source
	 *            ：表示替换的源字符串
	 * @param values
	 *            ：表示需替换的信息,map类型
	 * @param hasFormate
	 *            ：表示values中是否已经包含用户所需的格式了，true表示已经包含
	 * @param autoFormate表示将key值格式化成
	 *            [key]模式
	 * @return
	 * @author noksoft
	 * @version 0.1
	 */
	public static String replaceFromMap(String source, Map values,
			Boolean hasFormate, Boolean autoFormate) {
		if (null == values) {
			return source;
		}
		String rvalue = source;
		if (!hasFormate) {
			if (!autoFormate) {
				rvalue = replaceFromMap(source, values);
			} else {
				String escaped = "";
				/**
				 * 2015-01-06取消 Iterator iterator = values.keySet().iterator();
				 * while (iterator.hasNext()) { Object key = iterator.next(); if
				 * (key.toString().trim().length() == 0) { continue; } escaped =
				 * Pattern.quote("[" + key.toString() + "]"); rvalue =
				 * rvalue.replaceAll(escaped, values.get(key) .toString()); }
				 */
				// --------------------------------
				Iterator iterator = values.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry entry = (Map.Entry) iterator.next();
					Object key = entry.getKey();
					if (key.toString().trim().length() == 0) {
						continue;
					}
					escaped = Pattern.quote("[" + key.toString() + "]");
					rvalue = rvalue.replaceAll(escaped, values.get(key)
							.toString());
				}
				// --------------------------------

			}
		} else {
			rvalue = replaceFromMap(source, values, hasFormate);
		}
		return rvalue;
	}
	/**
	 * replaceFromList主要是对replaceFromMap扩展
	 * 
	 * @param source
	 *            ：表示替换的源字符串
	 * @param lstvalue
	 *            ：表示需替换的信息list,list <Map>类型
	 * @param lstfmt
	 *            ：表示是否已经包含用户所需的格式的数组对应于lstvalue
	 * @return
	 * @author noksoft
	 * @version 0.1
	 */
	public static String replaceFromList(String source, List<Map> lstvalue,boolean[] lstfmt) {
		String rvalue = source;
		boolean fmt = false;
		for (int i = 0; i < lstvalue.size(); i++) {
			if (null == lstfmt) {
				fmt = false;
			} else {
				if (lstfmt.length >= i) {
					fmt = lstfmt[i];
				} else {
					fmt = false;
				}
			}
			rvalue = replaceFromMap(rvalue, lstvalue.get(i), fmt);
		}
		return rvalue;
	}

	
}
