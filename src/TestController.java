package controller;

import mg.p16.Annotationcontroller.AnnotationController;
import mg.p16.Annotationcontroller.AnnotationGet;

@AnnotationController
public class TestController {

    @AnnotationGet("test")
    public void testMethod() {
        
    }

    @AnnotationGet("hello")
    public void helloMethod() {
    }
}
