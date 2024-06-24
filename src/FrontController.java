package mg.p16.Spring;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

import mg.p16.Annotationcontroller.*;
import mg.p16.classe.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    private String packageName;
    private static List<String> controllerNames = new ArrayList<>();
    private HashMap<String, Mapping> urlMapping = new HashMap<>();
    private String error;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        packageName = config.getInitParameter("packageControllerName");
        try {
            scanControllers(packageName);
        } catch (Exception err) {
            error = err.getMessage();
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuffer requestURL = request.getRequestURL();
        String[] requestUrlSplitted = requestURL.toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];

        // Enlever le texte après "?" pour les liens GET
        if (controllerSearched.contains("?")) {
            controllerSearched = controllerSearched.split("\\?")[0];
        }
        System.out.println(controllerSearched);

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        if (error != null && !error.isEmpty()) {
            out.write(error);
            out.close();
            return;
        } else if (!urlMapping.containsKey(controllerSearched)) {
            out.println("<p>Aucune méthode associée à ce chemin.</p>");
            out.close();
            return;
        } else {
            Mapping mapping = urlMapping.get(controllerSearched);

            try {
                Object result = invokeControllerMethod(mapping, request);

                if (result instanceof String) {
                    out.write((String) result);
                } else if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    String url = modelView.getUrl();
                    HashMap<String, Object> data = modelView.getData();

                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = request.getRequestDispatcher(url);
                    dispatcher.forward(request, response);
                    return;
                } else {
                    out.write("Type de retour non reconnu.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                out.write("Erreur lors du traitement de la requête.");
            }
        }
        out.close();
        }

        private Object invokeControllerMethod(Mapping mapping, HttpServletRequest request) throws Exception {
            Class<?> clazz = Class.forName(mapping.getClassName());
            Method targetMethod = null;
        
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(mapping.getMethodeName())) {
                    targetMethod = method;
                    break;
                }
            }
        
            if (targetMethod == null) {
                throw new NoSuchMethodException("Méthode " + mapping.getMethodeName() + " non trouvée dans " + mapping.getClassName());
            }
        
            Object instanceClazz = clazz.getDeclaredConstructor().newInstance();
        
            Parameter[] parameters = targetMethod.getParameters();
            Object[] args = new Object[parameters.length];
        
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(Param.class)) {
                    Param param = parameters[i].getAnnotation(Param.class);
                    String paramName = param.name();
                    String paramValue = request.getParameter(paramName);
                    args[i] = paramValue;
                }
            }
        
            return targetMethod.invoke(instanceClazz, args);
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

    private void scanControllers(String packageName) throws Exception {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                throw new ServletException("Le package " + packageName + " est introuvable.");
            }

            Path classPath = Paths.get(resource.toURI());
            List<Path> classFiles = Files.walk(classPath)
                    .filter(Files::isRegularFile)
                    .filter(f -> f.toString().endsWith(".class"))
                    .toList();

            if (classFiles.isEmpty()) {
                throw new ServletException("Le package " + packageName + " est vide.");
            }

            for (Path f : classFiles) {
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

                                System.out.println("Mapping URL: " + annotationValue + " to " + className + "." + m.getName());
                                if (urlMapping.containsKey(annotationValue)) {
                                    throw new RuntimeException("Double URL: " + annotationValue);
                                } else {
                                    urlMapping.put(annotationValue, mapping);
                                }
                            }
                        }
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des contrôleurs : " + e.getMessage(), e);
        }
    }
}
