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
        try {
            mv.setUrl("/welcome.jsp");
        } catch (Exception e) {
            // Gérer l'exception (par exemple, journaliser l'erreur, renvoyer une erreur
            // spécifique, etc.)
            e.printStackTrace();
        }
        mv.addObject("message", "Welcome to our application!");
        return mv;
    }

    @AnnotationGet("welcome")
    public ModelView welcome2() {
        ModelView mv = new ModelView();
        try {
            mv.setUrl("/welcome.jsp");
        } catch (Exception e) {
            // Gérer l'exception (par exemple, journaliser l'erreur, renvoyer une erreur
            // spécifique, etc.)
            e.printStackTrace();
        }
        mv.addObject("message", "Welcome to our application 2!");
        return mv;
    }
}
