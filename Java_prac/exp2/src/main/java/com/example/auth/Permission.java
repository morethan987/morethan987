package com.example.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Function Annotation
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    String value();
}
