package Solution;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
//slide 19 from the turtial

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Then {
    String value();
}
