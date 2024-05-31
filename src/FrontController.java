package mg.p16.Spring;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

import mg.p16.Annotationcontroller.*;
import mg.p16.classe.*;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    private String packageName; // Variable pour stocker le nom du package
    private static List<String> controllerNames = new ArrayList<>();
    private HashMap<String, Mapping> urlMapping = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        packageName = config.getInitParameter("packageControllerName"); // Récupération du nom du package
        scanControllers(packageName);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuffer requestURL = request.getRequestURL();
        String[] requestUrlSplitted = requestURL.toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        // if (!urlMapping.containsKey(controllerSearched)) {
        //     out.println("<p>Aucune méthode associée à ce chemin.</p>");
        // } else {
            Mapping mapping = urlMapping.get(controllerSearched);

            out.println(urlMapping.size() + " -> "+controllerSearched);

            Set<String> keys = urlMapping.keySet();

            for (String key : keys) {
                out.println("<p>"+key+"</p>");
            }

            out.println("<p>URL demandée: " + requestURL.toString() + "</p>");
            out.println("<p>Classe: " + mapping.getClassName() + "</p>");
            out.println("<p>Méthode: " + mapping.getMethodeName() + "</p>");

        // }
        out.close();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private void scanControllers(String packageName) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);
    
            if (resource == null) {
                throw new ServletException("Le package " + packageName + " est introuvable.");
            }
    
            Path classPath = Paths.get(resource.toURI());
            Files.walk(classPath)
                .filter(Files::isRegularFile)
                .filter(f -> f.toString().endsWith(".class"))
                .forEach(f -> {
                    String className = packageName + "." + f.getFileName().toString().replace(".class", "");
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(AnnotationController.class) &&
                            !Modifier.isAbstract(clazz.getModifiers())) {
                            controllerNames.add(clazz.getSimpleName());
                            Method[] methods = clazz.getMethods();
    
                            for (Method m : methods) {
                                if (m.isAnnotationPresent(AnnotationGet.class)) {
                                    Mapping mapping = new Mapping(className, m.getName());
                                    AnnotationGet annotationGet = m.getAnnotation(AnnotationGet.class);
                                    String annotationValue = annotationGet.value();

                                    urlMapping.put(annotationValue, mapping);
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
