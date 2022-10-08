package com.bright.stats.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class PropertiesUtil {

	private static PropertiesUtil properties;
	private static Properties po;
	private static final String DATABASE_CONFIG_FILE="/config/stats.properties";
	
	private PropertiesUtil(){
		po=new Properties();
		try {
			po.load(getClass().getResourceAsStream(DATABASE_CONFIG_FILE));
		} catch (Exception e) {
			log.info("读取配置文件失败！"+e);
		}
	}

	/*
	 * 构建一个实例
	 */
	private static PropertiesUtil getInstance(){
		
		if (properties==null) {
			properties=new PropertiesUtil();
		}
		return properties;
	}

	
	private String getFileValue(String key){
		return po.getProperty(key);
	}
	
	public static String getValue(String key){
		return getInstance().getFileValue(key);
		
	}
	
	public static Properties getProperties(String file_config){
		Properties pop=new Properties();
		try {
			pop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream(file_config));
		} catch (Exception e) {
			log.error("properties error :"+e.getMessage());
		}
		
		return pop;
		
	}

}
