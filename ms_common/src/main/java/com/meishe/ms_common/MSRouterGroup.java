package com.meishe.ms_common;

import java.util.Map;

public interface MSRouterGroup {

    Map<String, Class<? extends MSRouterPath>> getGroupMap();

}
