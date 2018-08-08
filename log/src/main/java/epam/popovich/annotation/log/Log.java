package epam.popovich.annotation.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String className() default "String";

    String name() default "default_name";

    String value() default "default_value";

    int type() default 0;

    enum TimeInterval {MILLISECOND, NANOSECOND}

    TimeInterval interval() default TimeInterval.MILLISECOND;
}