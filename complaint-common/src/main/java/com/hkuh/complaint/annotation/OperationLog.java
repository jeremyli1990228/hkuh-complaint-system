package com.hkuh.complaint.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OperationLog {

    String module() default "";

    String operation() default "";

    boolean recordParams() default true;

    boolean recordResult() default false;

    String[] excludeParams() default {"password", "token", "secret", "key", "credential"};
}
