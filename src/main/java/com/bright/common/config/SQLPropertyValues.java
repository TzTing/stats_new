package com.bright.common.config;

import org.hibernate.engine.spi.Mapping;
import org.hibernate.type.ComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * @author: Tz
 * @Date: 2022/10/22 0:31
 */
public class SQLPropertyValues {

    protected Object[] getPkValues(Type type, Serializable id, Mapping factory) throws IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Object[] keyValues = null;
        if(type instanceof ComponentType){
            ComponentType keyType = (ComponentType) type;
            Type[] subTypes = keyType.getSubtypes();
            String[] keyNames = keyType.getPropertyNames();
            keyValues = new Object[subTypes.length];
            for(int i=0;i < subTypes.length;i++){
                Type t = subTypes[i];
                PropertyDescriptor pd = new PropertyDescriptor(keyNames[i],id.getClass());
                Object o = pd.getReadMethod().invoke(id);
                if(t instanceof EntityType){
                    EntityType ty = (EntityType)t;
                    String key = ty.getIdentifierOrUniqueKeyPropertyName(factory);
                    PropertyDescriptor p = new PropertyDescriptor(key,o.getClass());
                    Object v = p.getReadMethod().invoke(o);
                    keyValues[i] = v;
                }else{
                    keyValues[i]=o;
                }
            }
        }else{
            keyValues = new Object[]{id};
        }
        return keyValues;
    }


    protected Object[] getValues(Object[] values,Type[] types,Mapping factory){
        Object[] results = new Object[values.length];
        for(int i = 0;i < types.length; i++){
            Type type = types[i];
            if(type instanceof EntityType){
                EntityType entityType = (EntityType)type;
                String key = entityType.getIdentifierOrUniqueKeyPropertyName(factory);
                Object o = values[i];
                if(o != null){
                    try {
                        PropertyDescriptor pd = new PropertyDescriptor(key,o.getClass());
                        Object val = pd.getReadMethod().invoke(o);
                        results[i] = val;
                    } catch (IntrospectionException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }else{
                    results[i] = o;
                }
            }else{
                results[i] = values[i];
            }
        }
        return results;
    }
}
