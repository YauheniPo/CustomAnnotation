package com.cloudogu.blog.annotationprocessor.log;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface Log {
    String name() default "default name";
    String value() default "default value";
    int type() default 0;
    enum TimeInterval { MILLISECOND, NANOSECOND }
    TimeInterval interval() default TimeInterval.MILLISECOND;
    String format() default "Elapsed %s";

}