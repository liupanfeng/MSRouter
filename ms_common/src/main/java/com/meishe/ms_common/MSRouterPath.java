package com.meishe.ms_common;


import com.meishe.ms_annotation.MSRouterBean;

import java.util.Map;

/**
 *  key:   /app/MainActivity1
 *  value:  RouterBean(MainActivity1.class)
 */
public interface MSRouterPath {

    Map<String, MSRouterBean> getPathMap();
}
