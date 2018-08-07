package com.cloudogu.blog.annotationprocessor.log;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String className() default "String";
    String name() default "default_name";
    String value() default "default_value";
    int type() default 0;
    enum TimeInterval { MILLISECOND, NANOSECOND }
    TimeInterval interval() default TimeInterval.MILLISECOND;
}