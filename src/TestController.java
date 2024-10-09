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

    @AnnotationGet("testController2")
    public ModelView testMethod2(@ParamObject TestModel model, String message) {
        ModelView mv = new ModelView();
        mv.setUrl("/welcome2.jsp");
        mv.addObject("model", model);
        mv.addObject("message", message);
        return mv;
    }

    @AnnotationGet("form")
    public ModelView form() {
        ModelView mv = new ModelView();
        mv.setUrl("/index.jsp");
        return mv;
    }

    @AnnotationGet("form2")
    public ModelView form2() {
        ModelView mv = new ModelView();
        mv.setUrl("/index2.jsp");
        return mv;
    }
}
