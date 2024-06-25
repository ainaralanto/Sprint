package mg.p16.Annotationcontroller;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FieldParam {
    String name() default "";
}
