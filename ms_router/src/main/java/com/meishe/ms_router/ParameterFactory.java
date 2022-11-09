package com.meishe.ms_router;

import com.meishe.ms_annotation.MSParameter;
import com.meishe.ms_router.utils.ProcessorConfig;
import com.meishe.ms_router.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class ParameterFactory {


    private MethodSpec.Builder method;

    private ClassName className;

    private Messager messager;

    private ParameterFactory(Builder builder) {
        this.messager = builder.messager;
        this.className = builder.className;

        method = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
    }

    public void addFirstStatement() {
        method.addStatement("$T t = ($T) " + ProcessorConfig.PARAMETER_NAME, className, className);
    }

    public MethodSpec build() {
        return method.build();
    }

    public void buildStatement(Element element) {
        TypeMirror typeMirror = element.asType();

        int type = typeMirror.getKind().ordinal();

        String fieldName = element.getSimpleName().toString();

        String annotationValue = element.getAnnotation(MSParameter.class).name();

        annotationValue = ProcessorUtils.isEmpty(annotationValue) ? fieldName : annotationValue;

        String finalValue = "t." + fieldName;

        String methodContent = finalValue + " = t.getIntent().";

        if (type == TypeKind.INT.ordinal()) {
            methodContent += "getIntExtra($S, " + finalValue + ")";  // 有默认值
        } else if (type == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S, " + finalValue + ")";  // 有默认值
        } else  {
            if (typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)) {
                // String类型
                methodContent += "getStringExtra($S)"; // 没有默认值
            }
        }

        if (methodContent.endsWith(")")) {
            method.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持String、int、boolean传参");
        }
    }

    public static class Builder {

        private Messager messager;

        private ClassName className;

        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public ParameterFactory build() {
            if (parameterSpec == null) {
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (className == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (messager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }

            return new ParameterFactory(this);
        }
    }
}
