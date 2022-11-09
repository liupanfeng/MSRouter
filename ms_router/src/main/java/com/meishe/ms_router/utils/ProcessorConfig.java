package com.meishe.ms_router.utils;

public interface ProcessorConfig {

    String MS_ROUTER_PACKAGE = "com.meishe.ms_annotation.MSRouter";

    String OPTIONS = "moduleName"; //目的是接收 每个module名称


    String APT_PACKAGE = "packageNameForAPT"; // 目的是接收 包名（APT 存放的包名）


    public static final String STRING_PACKAGE = "java.lang.String";

    public static final String ACTIVITY_PACKAGE = "android.app.Activity";

    String MS_ROUTER_API_PACKAGE = "com.meishe.ms_common";

    String MSROUTER_API_GROUP = MS_ROUTER_API_PACKAGE + ".MSRouterGroup";

    String MSROUTER_API_PATH = MS_ROUTER_API_PACKAGE + ".MSRouterPath";

    String PATH_METHOD_NAME = "getPathMap";

    // 路由组，中的 Group 里面的 方法名
    String GROUP_METHOD_NAME = "getGroupMap";

    String PATH_VAR1 = "pathMap";

    String GROUP_VAR1 = "groupMap";

    String PATH_FILE_NAME = "MSRouter$$Path$$";

    String GROUP_FILE_NAME = "MSRouter$$Group$$";




    String PARAMETER_PACKAGE = "com.meishe.ms_annotation.MSParameter";

    String AROUTER_AIP_PARAMETER_GET = MS_ROUTER_API_PACKAGE + ".ParameterGet";

    String PARAMETER_NAME = "targetParameter";

    String PARAMETER_METHOD_NAME = "getParameter";

    String PARAMETER_FILE_NAME = "$$MSParameter";

    public static final String STRING = "java.lang.String";

}
