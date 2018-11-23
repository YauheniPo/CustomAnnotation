package epam.popovich.annotation.time;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackTime {

    String name() default "default_name";

    String value() default "default_value";

    enum TimeInterval {MILLISECOND, NANOSECOND}

    TimeInterval interval() default TimeInterval.MILLISECOND;
}