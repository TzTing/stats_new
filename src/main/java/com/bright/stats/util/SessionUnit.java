package com.bright.stats.util;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import eu.bitwalker.useragentutils.UserAgent;
import org.bouncycastle.util.IPAddress;
import sun.net.util.IPAddressUtil;

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

	public static String getOsAndBrowserInfo2(HttpServletRequest request){
		//req就是request请求
		UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));

		//获取浏览器信息
		Browser browser = userAgent.getBrowser();

		//获取操作系统信息
		OperatingSystem os = userAgent.getOperatingSystem();
		StringBuffer userInfo = new StringBuffer();
		userInfo.append("操作系统：" + os.toString() + " 浏览器：" + browser.toString());
		return userInfo.toString();
	}

	public static String getOsAndBrowserInfo(HttpServletRequest request) {
		String browserDetails = request.getHeader("User-Agent");
		String userAgent = browserDetails;
		String user = userAgent.toLowerCase();

		String os = "";
		String browser = "";

		//=================OS Info=======================
		if (userAgent.toLowerCase().contains("windows")) {
			os = "Windows";
		} else if (userAgent.toLowerCase().contains("mac")) {
			os = "Mac";
		} else if (userAgent.toLowerCase().contains("x11")) {
			os = "Unix";
		} else if (userAgent.toLowerCase().contains("android")) {
			os = "Android";
		} else if (userAgent.toLowerCase().contains("iphone")) {
			os = "IPhone";
		} else {
			os = "UnKnown, More-Info: " + userAgent;
		}
		//===============Browser===========================
		if (user.contains("edge")) {
			browser = (userAgent.substring(userAgent.indexOf("Edge")).split(" ")[0]).replace("/", "-");
		} else if (user.contains("msie")) {
			String substring = userAgent.substring(userAgent.indexOf("MSIE")).split(";")[0];
			browser = substring.split(" ")[0].replace("MSIE", "IE") + "-" + substring.split(" ")[1];
		} else if (user.contains("safari") && user.contains("version")) {
			browser = (userAgent.substring(userAgent.indexOf("Safari")).split(" ")[0]).split("/")[0]
					+ "-" + (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
		} else if (user.contains("opr") || user.contains("opera")) {
			if (user.contains("opera")) {
				browser = (userAgent.substring(userAgent.indexOf("Opera")).split(" ")[0]).split("/")[0]
						+ "-" + (userAgent.substring(userAgent.indexOf("Version")).split(" ")[0]).split("/")[1];
			} else if (user.contains("opr")) {
				browser = ((userAgent.substring(userAgent.indexOf("OPR")).split(" ")[0]).replace("/", "-"))
						.replace("OPR", "Opera");
			}

		} else if (user.contains("chrome")) {
			browser = (userAgent.substring(userAgent.indexOf("Chrome")).split(" ")[0]).replace("/", "-");
		} else if ((user.contains("mozilla/7.0")) || (user.contains("netscape6")) ||
				(user.contains("mozilla/4.7")) || (user.contains("mozilla/4.78")) ||
				(user.contains("mozilla/4.08")) || (user.contains("mozilla/3"))) {
			browser = "Netscape-?";

		} else if (user.contains("firefox")) {
			browser = (userAgent.substring(userAgent.indexOf("Firefox")).split(" ")[0]).replace("/", "-");
		} else if (user.contains("rv")) {
			String IEVersion = (userAgent.substring(userAgent.indexOf("rv")).split(" ")[0]).replace("rv:", "-");
			browser = "IE" + IEVersion.substring(0, IEVersion.length() - 1);
		} else {
			browser = "UnKnown, More-Info: " + userAgent;
		}

		return os + ":" + browser;
	}

	public static String realIpAddr(HttpServletRequest request){

		String ip = request.getHeader("x-forwarded-for");
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

}
