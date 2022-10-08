package com.bright.stats.util;

import javax.servlet.http.HttpServletRequest;

public class SessionUnit {
	
	//读取session的值,通过读取acc_set判断是否为空
	public static String getSessionValue(HttpServletRequest request,String attrkey){
		String rvalue="";
		try{
		rvalue=request.getSession().getAttribute(attrkey).toString();
		}
		catch( NullPointerException exp){
		rvalue=null;	
		}
		return rvalue;
	}

}
