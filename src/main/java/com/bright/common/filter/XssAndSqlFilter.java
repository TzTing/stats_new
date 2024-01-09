package com.bright.common.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

/**
 * <p>Project: stats - XssAndSqlFilter </p>
 *
 * @author: Tz
 * @Date: 2023/12/25 10:37
 * @Description: xss攻击和sql注入过滤器
 * @version: 1.0.0
 */
@Component
public class XssAndSqlFilter implements Filter {


    private static final String SQL_REG_EXP = ".*(\\b(select|insert|into|update|delete|from|where|and|or|trancate" +
            "|drop|execute|like|grant|use|union|order|by)\\b).*";

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {



        String method = "GET";

        String param = "";
        XssAndSqlHttpServletRequestWrapper xssRequest = null;
        if (request instanceof HttpServletRequest) {
            method = ((HttpServletRequest) request).getMethod();
            xssRequest = new XssAndSqlHttpServletRequestWrapper((HttpServletRequest) request);
        }

        if("GET".equalsIgnoreCase(method)){
            // 获得所有请求参数名
            Enumeration<String> names = request.getParameterNames();
            String sql = "";
            while (names.hasMoreElements()) {
                // 得到参数名
                String name = names.nextElement();
                // 得到参数对应值
                String[] values = request.getParameterValues(name);
                for (int i = 0; i < values.length; i++) {
                    boolean isValid = isSqlInject(values[i], response);
                    if (!isValid) {
                        return;
                    }
                }
            }
        }

        if ("POST".equalsIgnoreCase(method)) {
            param = this.getBodyString(xssRequest.getReader());
            if(StringUtils.isNotBlank(param)){
                if(xssRequest.checkXSSAndSql(param)){
                    HttpServletResponse response2 = (HttpServletResponse) response;
                    response2.setContentType("text/html;charset=GBK");
                    response2.setCharacterEncoding("GBK");
                    response2.setStatus(403);
                    response2.getWriter().print("<font size=6 color=red>对不起，您的是请求非法，系统拒绝响应!</font>");
                    return;
                }
            }
        }
        if (xssRequest.checkParameter()) {
            HttpServletResponse response2 = (HttpServletResponse) response;
            response2.setContentType("text/html;charset=GBK");
            response2.setCharacterEncoding("GBK");
            response2.setStatus(403);
            response2.getWriter().print("<font size=6 color=red>对不起，您的是请求非法，系统拒绝响应!</font>");
            return;
        }
        chain.doFilter(xssRequest, response);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {

    }

    // 获取request请求body中参数
    public static String getBodyString(BufferedReader br) {
        String inputLine;
        String str = "";
        try {
            while ((inputLine = br.readLine()) != null) {
                str += inputLine;
            }
            br.close();
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }
        return str;

    }


    private boolean isSqlInject(String value, ServletResponse servletResponse) throws IOException {
        if (null != value && value.toLowerCase().matches(SQL_REG_EXP)) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setContentType("text/html;charset=GBK");
            response.setCharacterEncoding("GBK");
            response.setStatus(403);
            response.getWriter().print("<font size=6 color=red>对不起，您的是请求非法，系统拒绝响应!</font>");
            return false;
        }
        return true;
    }

}