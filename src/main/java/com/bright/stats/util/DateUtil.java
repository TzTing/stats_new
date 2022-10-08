/**
 * 日期工具类
 */
package com.bright.stats.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ncidream
 * 
 */
@Slf4j
public class DateUtil {

	public final static Integer YEARS = 1;

	public final static Integer MONTHS = 2;

	public final static Integer DAYS = 3;


	public static int getDaysOfMonth(int years, int months) {
		int days[] = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		if (2 == months && 0 == (years % 4)
				&& (0 != (years % 100) || 0 == (years % 400))) {
			days[1] = 29;
		}
		return (days[months - 1]);
	}

	public static Date getCurrDate() {
		TimeZone tz = TimeZone.getTimeZone("GMT+08:00"); 
		TimeZone.setDefault(tz); 
		
		GregorianCalendar now = new GregorianCalendar();

		SimpleDateFormat fmtrq = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowDate = fmtrq.format(now.getTime());
		
//		Date now = new Date();
//	    Calendar cal = Calendar.getInstance();
//	     
//	    DateFormat d1 = DateFormat.getDateInstance(); //默认语言（汉语）下的默认风格（MEDIUM风格，比如：2008-6-16 20:54:53）
//	    String str1 = d1.format(now);

		Date nowdate = null;

		try {
			nowdate = fmtrq.parse(nowDate);
		} catch (ParseException e1) {
			log.error("得到当前日期发生错误");
		}
		return nowdate;
	}
	


	public static String getDate(Date date) {
		if (date == null) {
			return "";
		}
		

		TimeZone tz = TimeZone.getTimeZone("GMT+08:00"); 
		TimeZone.setDefault(tz); 
		
		SimpleDateFormat fmtrq = new SimpleDateFormat("yyyy-MM-dd",
				Locale.CHINA);
		return fmtrq.format(date);
	}
	
	/**
	 * 
	 * @author q
	 * @Description: TODO
	 * @param date
	 * @return
	 * @date 2015-9-16上午11:25:56
	 */
	public static String getDate(Object date) {
		return getDate((date==null)?null:convert(date.toString()));
	}
	
	public static String getAllDate(Object date){
		if (date == null) {
			return "";
		}
		TimeZone tz = TimeZone.getTimeZone("GMT+08:00"); 
		TimeZone.setDefault(tz); 
		
		SimpleDateFormat fmtrq = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return fmtrq.format(date);
	}
	public static String getAllDate(Date date) {
		if (date == null) {
			return "";
		}
		TimeZone tz = TimeZone.getTimeZone("GMT+08:00"); 
		TimeZone.setDefault(tz); 
		
		SimpleDateFormat fmtrq = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return fmtrq.format(date);
	}

	
	public static String getAllDateIncludeHm(Date date) {
		if (date == null) {
			return "";
		}
		

		TimeZone tz = TimeZone.getTimeZone("GMT+08:00"); 
		TimeZone.setDefault(tz); 
		
		SimpleDateFormat fmtrq = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S");
		return fmtrq.format(date);
	}
	
	public static String getDate(Date date, Boolean hhmmss) {
		if(!hhmmss) {
			return getDate(date);
		}
		
		if (date == null) {
			return "";
		}		
		
		TimeZone tz = TimeZone.getTimeZone("GMT+08:00"); 
		TimeZone.setDefault(tz); 
		String rvalue = new SimpleDateFormat("yyyyMMddHHmmssSSSS").format(new Date());
//		logger.info(rvalue);
		return rvalue;
		
//		SimpleDateFormat fmtrq = new SimpleDateFormat("yyyyMMddHHmmssSSSS",
//				Locale.CHINA);
//		logger.info(String.valueOf(fmtrq.format(date)));
//		return fmtrq.format(date);
	}
	
	public static Date convert(String date) {
		TimeZone tz = TimeZone.getTimeZone("GMT+08:00"); 
		TimeZone.setDefault(tz); 
		
		SimpleDateFormat fmtrq = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return fmtrq.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getDate(Date date, Integer yearMonthDay) {
		TimeZone tz = TimeZone.getTimeZone("GMT+08:00"); 
		TimeZone.setDefault(tz); 
		
		String rvalue = "";
		SimpleDateFormat fmtrq = null;
		switch (yearMonthDay) {
		case 1:
			fmtrq = new SimpleDateFormat("yyyy", Locale.CHINA);
			rvalue = fmtrq.format(date);
			break;
		case 2:
			fmtrq = new SimpleDateFormat("MM", Locale.CHINA);
			rvalue = fmtrq.format(date);
			break;
		case 3:
			fmtrq = new SimpleDateFormat("dd", Locale.CHINA);
			rvalue = fmtrq.format(date);
			break;
		}
		return rvalue;
	}

	public static int getDate(String date, Integer yearMonthDay) {
		int rvalue = 0;
		if(StringUtils.isEmpty(date)) return rvalue;
		
		return Integer.valueOf(getDate(convert(date), yearMonthDay));
	}
	
	/**
	 * @author dongBad
	*/
	public static String getMillisecondStr(){
		 Calendar calendar = Calendar.getInstance();
		 String millisecondStr = String.valueOf(calendar.getTimeInMillis());
		return millisecondStr;
	}

	/**
	 * @author dongBad
	*/
	public static boolean isDataStr(String str){
		boolean rValue = false;
		SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
		try{
			format.parse(str);
			rValue = true;
		}catch(Exception e){
			rValue = false;
		}
		return rValue;
	}

	public static Integer queryYearsByparam(Object obj){
		Integer years=0;
		SimpleDateFormat format=new SimpleDateFormat("yyyy");
		try {
			
			years=Integer.parseInt(format.format(format.parse(obj.toString())));
		} catch (ParseException e) {
			log.error("queryYearsByparam error:"+e);
		}
		
		return years;
	}
}
