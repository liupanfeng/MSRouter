package com.meishe.ms_router;

import com.google.auto.service.AutoService;
import com.meishe.ms_annotation.MSRouter;
import com.meishe.ms_annotation.MSRouterBean;
import com.meishe.ms_router.utils.ProcessorConfig;
import com.meishe.ms_router.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConfig.MS_ROUTER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_7)

@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})
public class MSRouterProcessor extends AbstractProcessor {

    private Elements elementTool;
    private Types typeTool;
    private Messager messager;
    private Filer filer;
    private String options;
    private String aptPackage;


    private Map<String, List<MSRouterBean>> mAllPathMap = new HashMap<>();
    private Map<String, String> mAllGroupMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();

        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);

        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);

        if (options != null && aptPackage != null) {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成....");
        } else {
            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 为null...");
        }

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>> lpf process...");

        if (set.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@MSRouter注解的地方呀");
            return false;
        }
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(MSRouter.class);

        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);

        TypeMirror activityMirror = activityType.asType();

        for (Element element : elements) {

            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "被@MSRetuer注解的类有：" + className);

            MSRouter msRouter=element.getAnnotation(MSRouter.class);

            MSRouterBean routerBean = new MSRouterBean.Builder()
                    .addGroup(msRouter.group())
                    .addPath(msRouter.path())
                    .addElement(element)
                    .build();

            TypeMirror elementMirror = element.asType();
            if (typeTool.isSubtype(elementMirror, activityMirror)) {
                routerBean.setTypeEnum(MSRouterBean.TypeEnum.ACTIVITY);
                messager.printMessage(Diagnostic.Kind.NOTE, "注解加到了Activity上边了" );
            } else {
                throw new RuntimeException("@MSRouter注解目前仅限用于Activity类之上");
            }


            if (checkRouterPath(routerBean)) {
                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success:" + routerBean.toString());

                List<MSRouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());

                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "@MSRouter注解未按规范配置，如：/app/MainActivity");
            }

        }

        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.MSROUTER_API_PATH);
        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.MSROUTER_API_GROUP);

        messager.printMessage(Diagnostic.Kind.NOTE, "lpf---------pathType：" +
                pathType );

        try {
            createPathFile(pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成PATH模板时，异常了 e:" + e.getMessage());
        }

        try {
            createGroupFile(groupType, pathType);
        } catch (IOException e) {
            e.printStackTrace();
            messager.printMessage(Diagnostic.Kind.NOTE, "在生成GROUP模板时，异常了 e:" + e.getMessage());
        }

        return true;
    }

    private final boolean checkRouterPath(MSRouterBean bean) {
        String group = bean.getGroup();
        String path = bean.getPath();

        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@MSRouter注解中的path值，必须要以 / 开头 "+bean.getMyClass());
            return false;
        }

        if (path.lastIndexOf("/") == 0) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@MSRouter注解未按规范配置，如：/app/MainActivity "+bean.getMyClass());
            return false;
        }

        String finalGroup = path.substring(1, path.indexOf("/", 1));

        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@MSRouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }

        return true;
    }


    private void createPathFile(TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return;
        }

        TypeName methodReturn = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(MSRouterBean.class)
        );

        for (Map.Entry<String, List<MSRouterBean>> entry : mAllPathMap.entrySet()) {

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addAnnotation(Override.class) // 给方法上添加注解  @Override
                    .addModifiers(Modifier.PUBLIC) // public修饰符
                    .returns(methodReturn) ;// 把Map<String, RouterBean> 加入方法返回

            // Map<String, RouterBean> pathMap = new HashMap<>(); // $N == 变量
            methodBuilder.addStatement("$T<$T, $T> $N = new $T<>()",
                    ClassName.get(Map.class),           // Map
                    ClassName.get(String.class),        // Map<String,
                    ClassName.get(MSRouterBean.class),    // Map<String, RouterBean>
                    ProcessorConfig.PATH_VAR1,          // Map<String, RouterBean> pathMap
                    ClassName.get(HashMap.class)        // Map<String, RouterBean> pathMap = new HashMap<>();
            );


            List<MSRouterBean> pathList = entry.getValue();
            for (MSRouterBean bean : pathList) {
                methodBuilder.addStatement("$N.put($S, $T.create($T.$L, $T.class, $S, $S))",
                        ProcessorConfig.PATH_VAR1, // pathMap.put
                        bean.getPath(), // "/personal/Personal_Main2Activity"
                        ClassName.get(MSRouterBean.class), // RouterBean
                        ClassName.get(MSRouterBean.TypeEnum.class), // RouterBean.Type
                        bean.getTypeEnum(), // 枚举类型：ACTIVITY
                        ClassName.get((TypeElement) bean.getElement()),
                        bean.getPath(), // 路径名
                        bean.getGroup() // 组名
                );
            }


            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();

            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
                    aptPackage + "." + finalClassName);

            JavaFile.builder(aptPackage,
                            TypeSpec.classBuilder(finalClassName) // 类名
                                    .addSuperinterface(ClassName.get(pathType))
                                    .addModifiers(Modifier.PUBLIC) // public修饰符
                                    .addMethod(methodBuilder.build()) // 方法的构建（方法参数 + 方法体）
                                    .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件


            mAllGroupMap.put(entry.getKey(), finalClassName);

        }


    }


    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) {
            return;
        }

        TypeName methodReturns = ParameterizedTypeName.get(
                ClassName.get(Map.class),        // Map
                ClassName.get(String.class),    // Map<String,

                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType)))

        );

        // 1.方法 public Map<String, Class<? extends ARouterPath>> getGroupMap() {
        MethodSpec.Builder methodBuidler = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(methodReturns);


        // Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
        methodBuidler.addStatement("$T<$T, $T> $N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),

                ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(pathType))),
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class));

        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodBuidler.addStatement("$N.put($S, $T.class)",
                    ProcessorConfig.GROUP_VAR1, // groupMap.put
                    entry.getKey(), // order, personal ,app
                    ClassName.get(aptPackage, entry.getValue()));
        }

        methodBuidler.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;

        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
                aptPackage + "." + finalClassName);

        JavaFile.builder(aptPackage,
                        TypeSpec.classBuilder(finalClassName) // 类名
                                .addSuperinterface(ClassName.get(groupType))
                                .addModifiers(Modifier.PUBLIC) // public修饰符
                                .addMethod(methodBuidler.build()) // 方法的构建（方法参数 + 方法体）
                                .build()) // 类构建完成
                .build() // JavaFile构建完成
                .writeTo(filer); // 文件生成器开始生成类文件

    }
}