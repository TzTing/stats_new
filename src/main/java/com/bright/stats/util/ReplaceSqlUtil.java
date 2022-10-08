package com.bright.stats.util;

import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.second.User;
import org.apache.commons.lang3.StringUtils;

public class ReplaceSqlUtil {
	
	public static String getSql(String sql, String keyword) {
		User user = SecurityUtil.getLoginUser();
		TableType tableType = user.getTableType();
		String writeDate = DateUtil.getAllDate(DateUtil.getCurrDate());
		
		

		sql = StringUtils.replace(sql, "${keyword}", StringUtils.trimToEmpty(keyword));
//		sql = StringUtils.replace(sql, "${_distid}", StringUtils.trimToEmpty(keyword));
		sql = StringUtils.replace(sql, "${userId}", String.valueOf(user.getId()));
		sql = StringUtils.replace(sql, "${__userId}", String.valueOf(user.getId()));
		sql = StringUtils.replace(sql, "${__userName}", String.valueOf(user.getUsername()));
		sql = StringUtils.replace(sql, "${__tableTypeId}", String.valueOf(tableType == null ? null : tableType.getId()));
		sql = StringUtils.replace(sql, "${__szdb}", DataConstants.SZDB);
		
		//小写
		sql = StringUtils.replace(sql, "${__userid}", String.valueOf(user.getId()));
		sql = StringUtils.replace(sql, "${__username}", String.valueOf(user.getUsername()));
		sql = StringUtils.replace(sql, "${__tabletypeid}", String.valueOf(tableType == null ? null : tableType.getId()));
	 
		sql = StringUtils.replace(sql, "${distNo}", StringUtils.trimToEmpty(user.getDistNo()));
		sql = StringUtils.replace(sql, "${distno}", StringUtils.trimToEmpty(user.getDistNo()));
//		sql = StringUtils.replace(sql, "${__distNoNextLength}", String.valueOf(user.getDistNoNextLength()));//暂时关闭 TODO
		sql = StringUtils.replace(sql, "${distName}", StringUtils.trimToEmpty(user.getDistName()));
		sql = StringUtils.replace(sql, "${ztfields}", user.getZtSql(""));
		sql = StringUtils.replace(sql, "${ztsql}", user.getZtSql(""));
		sql = StringUtils.replace(sql, "${writer}", StringUtils.trimToEmpty(user.getUsername()));
		sql = StringUtils.replace(sql, "${writeDate}", StringUtils.trimToEmpty(writeDate));
		sql = StringUtils.replace(sql, "${_udistNo}", StringUtils.trimToEmpty(user.getDistNo())); //q
		sql = StringUtils.replace(sql, "${_tableType}",String.valueOf(tableType == null ? null : tableType.getTableType())); //q
		
		//小写
//		sql = StringUtils.replace(sql, "${__distnonextlength}", String.valueOf(user.getDistNoNextLength()));//暂时关闭 TODO
		sql = StringUtils.replace(sql, "${distname}", StringUtils.trimToEmpty(user.getDistName()));
		sql = StringUtils.replace(sql, "${writedate}", StringUtils.trimToEmpty(writeDate));
		sql = StringUtils.replace(sql, "${_udistno}", StringUtils.trimToEmpty(user.getDistNo())); //q
		sql = StringUtils.replace(sql, "${_tabletype}",String.valueOf(tableType == null ? null : tableType.getTableType())); //q
		
		return sql;
	}
	
	
	/**
	 * 
	 * @author q
	 * @Description: TODO
	 * @param sql
	 * @param keyword
	 * @return
	 * @date 2016-8-5上午10:52:50
	 */
	public static String replaceSql(String sql, String keyword) {
		User user = SecurityUtil.getLoginUser();
		TableType tableType = user.getTableType();
		String writeDate = DateUtil.getAllDate(DateUtil.getCurrDate());
		sql = StringUtils.replace(sql, "${keyword}", StringUtils.trimToEmpty(keyword));
		sql = StringUtils.replace(sql, "${userId}", String.valueOf(user.getId()));
		sql = StringUtils.replace(sql, "${userId}", String.valueOf(user.getId()));
		sql = StringUtils.replace(sql, "${userName}", String.valueOf(user.getUsername()));
		sql = StringUtils.replace(sql, "${tableTypeId}", String.valueOf(tableType == null ? null : tableType.getId()));
		sql = StringUtils.replace(sql, "${szdb}", DataConstants.SZDB);
		
		sql = StringUtils.replace(sql, "${distNo}", StringUtils.trimToEmpty(user.getDistNo()));
		sql = StringUtils.replace(sql, "${distno}", StringUtils.trimToEmpty(user.getDistNo()));
//		sql = StringUtils.replace(sql, "${distNoNextLength}", String.valueOf(user.getDistNoNextLength()));//暂时关闭 TODO
		sql = StringUtils.replace(sql, "${distName}", StringUtils.trimToEmpty(user.getDistName()));
		sql = StringUtils.replace(sql, "${ztfields}", user.getZtSql(""));
		sql = StringUtils.replace(sql, "${ztsql}", user.getZtSql(""));
		sql = StringUtils.replace(sql, "${writer}", StringUtils.trimToEmpty(user.getUsername()));
		sql = StringUtils.replace(sql, "${writeDate}", StringUtils.trimToEmpty(writeDate));
		sql = StringUtils.replace(sql, "${udistNo}", StringUtils.trimToEmpty(user.getDistNo())); //q
		sql = StringUtils.replace(sql, "${tableType}",String.valueOf(tableType == null ? null : tableType.getTableType())); //q

		return sql;
	}
	

	public static String replaceSql(String sql, String key,String value) {
		sql=StringUtils.replace(sql, "${#"+key.toLowerCase()+"#}", value);
		return sql;
	}
	
	public static String replaceSqlSub(String sql, String tableName,Integer years,Integer months,String topDistid) {
		sql=StringUtils.replace(sql, "${#tableName#}", tableName);
		sql=StringUtils.replace(sql, "${#years#}", String.valueOf(years));
		sql=StringUtils.replace(sql, "${#months#}",String.valueOf(months));
		sql=StringUtils.replace(sql, "${#topdistid#}",topDistid);
		
		return sql;
	}
	
	public static String getTJSSSql(String sql, String keyword) {
	
		String writeDate = DateUtil.getAllDate(DateUtil.getCurrDate());
		sql = StringUtils.replace(sql, "${keyword}", StringUtils.trimToEmpty(keyword));
		sql = StringUtils.replace(sql, "${szdb}", DataConstants.SZDB);
//		sql = StringUtils.replace(sql, "${distNo}", StringUtils.trimToEmpty(user.getDistNo()));
//		sql = StringUtils.replace(sql, "${distno}", StringUtils.trimToEmpty(user.getDistNo()));
//		sql = StringUtils.replace(sql, "${distName}", StringUtils.trimToEmpty(user.getDistName()));
//		sql = StringUtils.replace(sql, "${writer}", StringUtils.trimToEmpty(user.getUserName()));
		sql = StringUtils.replace(sql, "${writeDate}", StringUtils.trimToEmpty(writeDate));
//		sql = StringUtils.replace(sql, "${udistNo}", StringUtils.trimToEmpty(user.getDistNo())); //q
		return sql;
	
	}

}
