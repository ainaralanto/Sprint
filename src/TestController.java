package controller;

import mg.p16.Annotationcontroller.AnnotationController;
import mg.p16.Annotationcontroller.AnnotationGet;

@AnnotationController
public class TestController {

    @AnnotationGet("test")
    public String testMethod() {
        return "valeur de retour testMethod";
    }

    @AnnotationGet("hello")
    public String helloMethod() {
        return "valeur de retour helloMethod";
    }
}
