package controller;

import mg.p16.Annotationcontroller.AnnotationController;
import mg.p16.Annotationcontroller.AnnotationGet;
import mg.p16.Spring.ModelView;

@AnnotationController
public class TestController {

    @AnnotationGet("hello")
    public String hello() {
        return "Hello, World!";
    }

    @AnnotationGet("welcome")
    public ModelView welcome() {
        ModelView mv = new ModelView();
        mv.setUrl("/welcome.jsp");
        mv.addObject("message", "Message voalohany");
        mv.addObject("message2", "Message faharoa");
        return mv;
    }
}
