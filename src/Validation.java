package mg.p16.Annotationcontroller;

import java.lang.annotation.*;

public class Validation {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ValidEmail {
        String message() default "Email invalide.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ValidInt {
        int min() default Integer.MIN_VALUE;
        int max() default Integer.MAX_VALUE;
        String message() default "Valeur hors plage.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ValidFile {
        String[] extensions() default {}; 
        String message() default "Type de fichier non autorisé.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ValidDouble {
        String message() default "La valeur doit être un nombre décimal.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface ValidString {
        int min() default 1;
        int max() default 255;
        String message() default "La longueur de la chaîne est invalide.";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NotNull {
        String message() default "Ce champ ne peut pas être nul.";
    }
    
}
