package com.bcs.core.aspect.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Web Service Aspect Log Annotation
 *
 * @author Alan
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebServiceLog {
    String CREATE = "CREATE";
    String EDIT = "EDIT";
    String DELETE = "DELETE";
    String SEARCH = "SEARCH";

    /* Create, Edit, Delete, Search */
    String action() default "";
}