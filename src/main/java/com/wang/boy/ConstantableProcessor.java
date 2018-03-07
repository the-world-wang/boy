package com.wang.boy;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
public class ConstantableProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Constantable.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.elementUtils = env.getElementUtils();
        this.messager = env.getMessager();
        this.filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> elements, RoundEnvironment roundEnv) {

        for (Element element : roundEnv.getElementsAnnotatedWith(Constantable.class)) {

            if (element.getKind() != ElementKind.CLASS) {
                error("Only classes can be annotated with %s", Constantable.class.getSimpleName());
                return true;
            }

            TypeElement typeElement = (TypeElement) element;

            String className = typeElement.getSimpleName().toString() + "Constants";
            String qualifiedName = typeElement.getQualifiedName().toString();
            String packageName = qualifiedName.split("." + typeElement.getSimpleName().toString())[0];

            List<? extends Element> members = elementUtils.getAllMembers(typeElement);

            // type builder
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            List<ExecutableElement> elementMethods = ElementFilter.methodsIn(members);
            for (ExecutableElement method : elementMethods) {
                String methodName = method.getSimpleName().toString();

                if (methodName.startsWith("set")
                        && method.getParameters().size() == 1) {
                    String fieldName = setterToField(methodName);
                    typeBuilder.addField(generateField(fieldName));
                }
            }

            JavaFile javaFile = JavaFile.builder(packageName, typeBuilder.build())
                    .build();

            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                error(e.getMessage());
            }

        }
        return true;
    }

    private void error(String format, Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(format, args));
    }

    private static FieldSpec generateField(String fieldName) {
        return FieldSpec.builder(String.class, fieldName)
                .initializer("$S", fieldName.toLowerCase())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .build();
    }


    private static String setterToField(String method) {
        String name = method.split("set")[1];
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name);
    }
}
