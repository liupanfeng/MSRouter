package com.meishe.ms_common;

import android.app.Activity;
import android.util.LruCache;

/**
 * 参数的 加载管理器
 * 这是用于接收参数的
 *
 *  第一步：查找 ***_MainActivity$$Parameter
 *  第二步：使用 ***_MainActivity$$Parameter  this 给他
 */
public class MSParameterManager {

    private static MSParameterManager instance;

    // private boolean isCallback;

    public static MSParameterManager getInstance() {
        if (instance == null) {
            synchronized (MSParameterManager.class) {
                if (instance == null) {
                    instance = new MSParameterManager();
                }
            }
        }
        return instance;
    }

    // LRU缓存 key=类名      value=参数加载接口
    private LruCache<String, MSParameterGet> cache;

    private MSParameterManager() {
        cache = new LruCache<>(100);
    }


    static final String FILE_SUFFIX_NAME = "$$Parameter"; // 为了这个效果：***_MainActivity + $$Parameter

    public void loadParameter(Activity activity) {
        String className = activity.getClass().getName();

        MSParameterGet parameterLoad = cache.get(className); // 先从缓存里面拿 如果有  如果没有
        if (null == parameterLoad) {
            try {
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                parameterLoad = (MSParameterGet) aClass.newInstance();
                cache.put(className, parameterLoad); // 保存到缓存
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parameterLoad.getParameter(activity);
    }
}
