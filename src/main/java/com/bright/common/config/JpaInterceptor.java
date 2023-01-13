package com.bright.common.config;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author: Tz
 * @Date: 2022/10/19 19:30
 */
public class JpaInterceptor extends EmptyInterceptor{


    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        super.onDelete(entity, id, state, propertyNames, types);
    }

    @Override
    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        return super.onLoad(entity, id, state, propertyNames, types);
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
//        System.out.println("onSave....................");
        return super.onSave(entity, id, state, propertyNames, types);
    }

    @Override
    public void preFlush(Iterator entities) {
//        System.out.println(entities);
        super.preFlush(entities);
    }

    @Override
    public String onPrepareStatement(String sql) {
//        System.out.println("sql=====" + sql);
        return super.onPrepareStatement(sql);
    }
}
