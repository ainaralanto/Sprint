package controller;

import mg.p16.Annotationcontroller.*;
import mg.p16.Spring.ModelView;

@AnnotationController
public class TestController {

    @AnnotationGet("testController")
    public ModelView testMethod(@Param(name = "name") String name) {
        ModelView mv = new ModelView();
        mv.setUrl("/welcome.jsp");
        mv.addObject("name", name);
        return mv;
    }

    @AnnotationGet("form")
    public ModelView form() {
        ModelView mv = new ModelView();
        mv.setUrl("/index.jsp");
        return mv;
    }
}
