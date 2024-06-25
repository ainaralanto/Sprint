package controller;

import mg.p16.Annotationcontroller.*;
import mg.p16.Spring.ModelView;
import model.*;

@AnnotationController
public class TestController {

    @AnnotationGet("testController")
    public ModelView testMethod(@ParamObject TestModel model) {
        ModelView mv = new ModelView();
        mv.setUrl("/welcome.jsp");
        mv.addObject("model", model);
        return mv;
    }

    @AnnotationGet("form")
    public ModelView form() {
        ModelView mv = new ModelView();
        mv.setUrl("/index.jsp");
        return mv;
    }
}
