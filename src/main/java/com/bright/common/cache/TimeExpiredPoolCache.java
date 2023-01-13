package com.bright.common.cache;


import org.apache.poi.ss.formula.functions.T;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: Tz
 * @Date: 2022/11/04 16:03
 */
public class TimeExpiredPoolCache {

    /**
     * 过期时间默认10分钟
     */
    private static final long DEFAULT_CACHED_MILLIS = 10 * 60 * 1000L;

    /**
     * 定时清理默认10分钟
     */
    private static final long TIMER_MILLIS = 10 * 60 * 1000L;

    /**
     * 对象池
     */
    private static ConcurrentHashMap<String, DataWrapper<?>> dataPool = null;
    /**
     * 对象单例
     */
    private static TimeExpiredPoolCache instance = null;
    private TimeExpiredPoolCache() {
        dataPool = new ConcurrentHashMap<String, DataWrapper<?>>();
    }
    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new TimeExpiredPoolCache();
            initTimer();
        }
    }
    public static TimeExpiredPoolCache getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }
    /**
     * 定时器定时清理过期缓存
     */
    private static Timer timer = new Timer();
    private static void initTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    clearExpiredCaches();
                } catch (Exception e) {
                    //logger.error("clearExpiredCaches error.", e);
                }
            }
        }, TIMER_MILLIS, TIMER_MILLIS);
    }

    /**
     * 缓存数据
     * @param key key值
     * @param data 缓存数据
     * @param cachedMillis 过期时间
     * @param dataRenewer 刷新数据
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T put(String key, T data, long cachedMillis, DataRenewer<T> dataRenewer) throws Exception {
        DataWrapper<T> dataWrapper = (DataWrapper<T>)dataPool.get(key);
        if (data == null && dataRenewer != null) {
            data = dataRenewer.renewData();
        }
        //当重新获取数据为空，直接返回不做put
        if (data == null){
            return null;
        }
        if (dataWrapper != null) {
            //更新
            dataWrapper.update(data, cachedMillis);
        } else {
            dataWrapper = new DataWrapper<T>(data, cachedMillis);
            dataPool.put(key, dataWrapper);
        }
        return data;
    }
    /**
     * 直接设置缓存值和时间
     * @param key
     * @param data
     * @param cachedMillis
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T put(String key, T data, long cachedMillis){
        DataWrapper<T> dataWrapper = (DataWrapper<T>)dataPool.get(key);
        if (dataWrapper != null) {
            //更新
            dataWrapper.update(data, cachedMillis);
        } else {
            dataWrapper = new DataWrapper<T>(data, cachedMillis);
            dataPool.put(key, dataWrapper);
        }
        return data;
    }
    /**
     * 默认构造时间的缓存数据
     * @param key
     * @param data
     * @param dataRenewer
     * @return
     */
    @Deprecated
    public <T> T put(String key, T data, DataRenewer<T> dataRenewer) throws Exception {
        return put(key, data, DEFAULT_CACHED_MILLIS, dataRenewer);
    }
    /**
     * 获取缓存
     * @param key
     * @param cachedMillis
     * @param dataRenewer
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, long cachedMillis, DataRenewer<T> dataRenewer) throws Exception {
        DataWrapper<T> dataWrapper = (DataWrapper<T>)dataPool.get(key);
        if (dataWrapper != null && !dataWrapper.isExpired()) {
            return dataWrapper.data;
        }
        return put(key, null, cachedMillis, dataRenewer);
    }
    @SuppressWarnings("unchecked")
    public <T> T get(String key) throws Exception {
        DataWrapper<T> dataWrapper = (DataWrapper<T>)dataPool.get(key);
        if (dataWrapper != null && !dataWrapper.isExpired()) {
            return dataWrapper.data;
        }
        return null;
    }
    /**
     * 清除缓存
     */
    public void clear() {
        dataPool.clear();
    }

    /**
     * 删除指定key的value
     * */
    public void remove(String key){
        dataPool.remove(key);
    }

    /**
     * 获取缓存的数量
     * */
    public int size(){
        return dataPool.size();
    }

    /**
     * 获取缓存的数量
     * */
    public List<Object> getDataPool() {
        List<Object> list = new ArrayList<>();
        list = dataPool.values().stream().map(e -> e.data).collect(Collectors.toList());
        return list;
    }



    /**
     * 数据封装
     */
    private class DataWrapper<T> {
        /**
         * 数据
         */
        private T data;
        /**
         * 到期时间
         */
        private long expiredTime;
        /**
         * 缓存时间
         */
        private long cachedMillis;
        private DataWrapper(T data, long cachedMillis) {
            this.update(data, cachedMillis);
        }
        public void update(T data, long cachedMillis) {
            this.data = data;
            this.cachedMillis = cachedMillis;
            this.updateExpiredTime();
        }
        public void updateExpiredTime() {
            if(cachedMillis == 0){
                this.expiredTime = 0;
            } else {
                this.expiredTime = System.currentTimeMillis() + cachedMillis;
            }
        }
        /**
         * 数据是否过期
         * @return
         */
        public boolean isExpired() {
            if (this.expiredTime > 0) {
                return System.currentTimeMillis() > this.expiredTime;
            }
            return true;
        }
    }
    /**
     * 数据构造
     */
    public interface DataRenewer<T> {
        public T renewData();
    }

    /**
     * 清除过期的缓存
     */
    private static void clearExpiredCaches() {
        List<String> expiredKeyList = new LinkedList<String>();

        for(Map.Entry<String, DataWrapper<?>> entry : dataPool.entrySet()){
            if (entry.getValue().isExpired()) {
                expiredKeyList.add(entry.getKey());
            }
        }
        for (String key : expiredKeyList) {
            dataPool.remove(key);
        }
    }

}
