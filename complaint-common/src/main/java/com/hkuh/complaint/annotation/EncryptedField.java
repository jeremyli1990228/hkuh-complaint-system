package com.hkuh.complaint.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface EncryptedField {

    String algorithm() default "AES";

    String description() default "";
}
