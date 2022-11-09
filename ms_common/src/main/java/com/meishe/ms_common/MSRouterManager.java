package com.meishe.ms_common;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.LruCache;

import com.meishe.ms_annotation.MSRouterBean;

public class MSRouterManager {

    private String group;
    private String path;

    // 提供性能  LRU缓存
    private LruCache<String, MSRouterGroup> groupLruCache;
    private LruCache<String, MSRouterPath> pathLruCache;

    private final static String FILE_GROUP_NAME = "MSRouter$$Group$$";



    private static MSRouterManager instance;

    private MSRouterManager() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);
    }


    public static MSRouterManager getInstance() {
        if (instance == null) {
            synchronized (MSRouterManager.class) {
                if (instance == null) {
                    instance = new MSRouterManager();
                }
            }
        }
        return instance;
    }

    public MSBundleManager build(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("path 请按照规则填写 ：如 /app/MainActivity");
        }

        if (path.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("path 请按照规则填写：如 /app/MainActivity");
        }

        // 截取组名  /order/Order_MainActivity  finalGroup=order
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("path 请按照规则填写：如 /app/MainActivity");
        }

        this.path =  path;  // 最终的效果：如 /order/Order_MainActivity
        this.group = finalGroup; // 例如：order，personal

        return new MSBundleManager();

    }

    public Object navigation(Context context, MSBundleManager bundleManager) {
        String groupClassName = context.getPackageName() + "." + FILE_GROUP_NAME + group;

        try{
            MSRouterGroup loadGroup = groupLruCache.get(group);
            if (null == loadGroup) {
                Class<?> aClass = Class.forName(groupClassName);
                loadGroup = (MSRouterGroup) aClass.newInstance();
                groupLruCache.put(group, loadGroup);
            }

            if (loadGroup.getGroupMap().isEmpty()) {
                throw new RuntimeException("路由表Group报废了...");
            }

            // 读取路由Path类文件
            MSRouterPath loadPath = pathLruCache.get(path);
            if (null == loadPath) {
                Class<? extends MSRouterPath> clazz = loadGroup.getGroupMap().get(group);

                loadPath = clazz.newInstance();

                // 保存到缓存
                pathLruCache.put(path, loadPath);
            }

            if (loadPath != null) {

                if (loadPath.getPathMap().isEmpty()) {
                    throw new RuntimeException("路由表Path报废了...");
                }

                MSRouterBean routerBean = loadPath.getPathMap().get(path);

                if (routerBean != null) {
                    switch (routerBean.getTypeEnum()) {
                        case ACTIVITY:
                            Intent intent = new Intent(context, routerBean.getMyClass());
                            intent.putExtras(bundleManager.getBundle()); // 携带参数
                            context.startActivity(intent, bundleManager.getBundle());
                            break;
                    }
                }

            }


        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }




}
