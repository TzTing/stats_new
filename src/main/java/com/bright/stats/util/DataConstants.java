package com.bright.stats.util;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class DataConstants {
    public static String SYSTEMPATH = ClassLoader.getSystemResource("") + "upload/";
    public static String JSVERSION = "20140730";

    public static long uploadFileMaxSize = 2000000; //2M

    public static String SZDB = null; //三资数据名称
    public static int ISMYSQL = 0; //是mysql数据库为1
    public static String SQLTYPE = "mysql"; //数据库类型 例如mssql、mysql、dm
    public static String jdbc_distnoColumn = ""; //是mysql数据库为1
    public static String excel_dist = "-1"; //对excel导入数据的地区特殊处理

    public static String SAVEPATH = "upload\\center.files\\"; //导入access路径
    public static String NEWFILENAME = "uploads\\accessfile\\";//导出access路径
    public static String SAVEEXCELPATH = "upload\\excel\\";
    public static int[] distGrades;
    public static String webServiceUrl = null;
    public static Integer openwebservice = 0;
    public static Map<String, Object> sysparams = null;

    public static List<Map<String, Object>> funContrast = null;

    final public static String _sysid = "财务系统";// 当前系统id

    public static boolean LogInfoSQLFlag = false;//是否显示调用的sql语句日志

    public static String userDatabase = "";//指定用户表的数据库


    static {
        SYSTEMPATH = DataConstants.class.getResource("/").toString();
        SYSTEMPATH = StringUtils.right(SYSTEMPATH, SYSTEMPATH.length() - "file:/".length());
        SYSTEMPATH = StringUtils.left(SYSTEMPATH, SYSTEMPATH.length() - "WEB-INF/classes/".length());
        if (StringUtils.indexOf(DataConstants.SYSTEMPATH, ":") < 0) {
            if (StringUtils.indexOf(DataConstants.SYSTEMPATH, "/") != 0) {
                SYSTEMPATH = "/" + DataConstants.SYSTEMPATH;
            }
        }


        webServiceUrl = PropertiesUtil.getValue("webServiceUrl");
        openwebservice = Integer.valueOf(PropertiesUtil.getValue("openwebservice"));
        ISMYSQL = Integer.valueOf(PropertiesUtil.getValue("ismysql"));
        SQLTYPE = PropertiesUtil.getValue("sqltype");
        SZDB = PropertiesUtil.getValue("szdb");
        jdbc_distnoColumn = PropertiesUtil.getValue("jdbc_distnocolumn");
        excel_dist = PropertiesUtil.getValue("excel_dist");

        userDatabase = PropertiesUtil.getValue("userDatabase");//用户表所在的数据库
    }

    //表格数据颜色
    public static final String NOFLAGCOLOR = "#000000"; //封存
    public static final String BALFLAGCOLOR = "#008000"; //平衡数据
    public static final String SUMFLAGCOLOR = "#0000FF"; //汇总数据
    public static final String SAVEFLAGCOLOR = "#800080"; //封存


    //得到指定地区的最大级别
    public static int getMaxDistGrade(String distNo) {
        int rvalue = 0;
        try {
            rvalue = distGrades.length;
            if (distNo.length() == 0) return rvalue;
            for (int i = 0; i < distGrades.length; i++) {
                if (distNo.indexOf(",") != -1) {
                    if (distGrades[i] == distNo.substring(0, distNo.indexOf(",")).length()) {
                        //20190729这里存在类似0408,040801,040802这个问题，所以截取第一个地区号
                        //rvalue = distNo.length() - i;

                        break;
                    }
                } else {
                    if (distGrades[i] == distNo.length()) {
                        //rvalue = distNo.length() - i;

                        break;
                    }
                }


                rvalue--;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rvalue;
    }

    //得到最大地区编号
    public static int getMaxDistNoLength(String distNo, int grades) {

        ////是否显示上级20190614 distNo出现多个情况，截取第一个
        if (distNo.indexOf(",") != -1) {
            int ii = distNo.indexOf(",");
            distNo = distNo.substring(0, ii);
        }

        int grade = -1;
        if (null == distGrades || distGrades.length == 0) return grade;
        try {
            Integer distNoLength = distNo.length();
            if (StringUtils.isEmpty(distNo)) {
                distNoLength = 1;
            }
            for (int i = 0; i < distGrades.length; i++) {
                if (distGrades[i] == distNoLength) {
                    grade = i;
                    break;
                }
            }

            if (grade != -1) {
//				System.out.println("=========================================================");
//				System.out.println((grade + grades - 1));
//				System.out.println((grade + grades - 1) > distGrades.length -1);
//				System.out.println(distGrades.length -1);
                grade = ((grade + grades - 1) > distGrades.length - 1) ? distGrades.length : grade + grades - 1;
                if (grade == -1) {
                    grade = 0;
                }
            } else {
                grade = 0;
            }

            if (distGrades.length <= grade) grade = distGrades.length - 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return distGrades[grade];
    }
}
