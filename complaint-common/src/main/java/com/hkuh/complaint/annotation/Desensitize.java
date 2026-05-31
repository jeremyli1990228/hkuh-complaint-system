package com.hkuh.complaint.annotation;

import com.hkuh.complaint.constant.DesensitizeType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Desensitize {

    DesensitizeType type() default DesensitizeType.CUSTOM;

    int prefixLen() default 0;

    int suffixLen() default 0;

    String description() default "";
}
