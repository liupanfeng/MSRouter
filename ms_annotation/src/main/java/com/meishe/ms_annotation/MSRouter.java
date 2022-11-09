package com.meishe.ms_annotation;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE) // 类上
@Retention(CLASS) // 编译期
public @interface MSRouter {

    // 详细路由路径（必填），如："/app/MainActivity"
    String path();

    // 路由组名（选填，如果不填写，可以从path中截取出来）
    String group() default "";
}