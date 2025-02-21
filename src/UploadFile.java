package mg.p16.Annotationcontroller;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface UploadFile {
    String value(); // Nom du champ dans la requête multipart
}
