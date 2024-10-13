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

import com.thoughtworks.paranamer.*;
import com.google.gson.*;


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

            if (controllerSearched.contains("?")) {
                controllerSearched = controllerSearched.split("\\?")[0];
            }

            PrintWriter out = response.getWriter();
            Gson gson = new Gson();  // Pour la conversion en JSON
            response.setContentType("application/json");  // Par défaut, la réponse est en JSON

            if (error != null && !error.isEmpty()) {
                out.write(gson.toJson(error));
                out.close();
                return;
            } else if (!urlMapping.containsKey(controllerSearched)) {
                out.write(gson.toJson("Aucune méthode associée à ce chemin."));
                out.close();
                return;
            } else {
                Mapping mapping = urlMapping.get(controllerSearched);

                try {
                    // Invoquer la méthode contrôleur
                    Object result = invokeControllerMethod(mapping, request);

                    Method targetMethod = getTargetMethod(mapping);  // Obtenir la méthode cible

                    if (targetMethod.isAnnotationPresent(Restapi.class)) {
                        // Si la méthode est annotée avec @Restapi, gérer la réponse en JSON
                        response.setContentType("application/json");

                        if (result instanceof ModelView) {
                            // Si c'est un ModelView, envoyer l'attribut "data" en JSON
                            ModelView modelView = (ModelView) result;
                            String json = gson.toJson(modelView.getData());
                            out.write(json);
                        } else {
                            // Sinon, transformer directement en JSON
                            String json = gson.toJson(result);
                            out.write(json);
                        }
                    } else {
                        // Si pas d'annotation @Restapi, continuer avec le comportement classique
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
                    }
                } catch (Exception e) {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.write(gson.toJson("Erreur interne: " + e.getMessage()));
                    e.printStackTrace(out);
                }
            }
            out.close();
        }

        private Method getTargetMethod(Mapping mapping) throws ClassNotFoundException, NoSuchMethodException {
            Class<?> clazz = Class.forName(mapping.getClassName());
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals(mapping.getMethodeName())) {
                    return method;
                }
            }
            throw new NoSuchMethodException("Méthode " + mapping.getMethodeName() + " non trouvée dans " + mapping.getClassName());
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
    
            Paranamer paranamer = new BytecodeReadingParanamer();
            String[] paramNames = paranamer.lookupParameterNames(targetMethod, false);
    
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(Param.class)) {
                    Param param = parameters[i].getAnnotation(Param.class);
                    String paramName = param.name();
                    String paramValue = request.getParameter(paramName);
                    args[i] = paramValue;
                } else if (parameters[i].isAnnotationPresent(ParamObject.class)) {
                    Class<?> paramType = parameters[i].getType();
                    Object paramObject = paramType.getDeclaredConstructor().newInstance();
    
                    for (Field field : paramType.getDeclaredFields()) {
                        String fieldName = field.getName();
                        if (field.isAnnotationPresent(FieldParam.class)) {
                            FieldParam fieldParam = field.getAnnotation(FieldParam.class);
                            if (!fieldParam.name().isEmpty()) {
                                fieldName = fieldParam.name();
                            }
                        }
    
                        String fieldValue = request.getParameter(fieldName);
                        if (fieldValue != null) {
                            field.setAccessible(true);
                            field.set(paramObject, convertToFieldType(field, fieldValue));
                        }
                    }
    
                    args[i] = paramObject;
                } else {
                    throw new RuntimeException("ETU002527 ; Le paramètre " + parameters[i].getName() + " dans la méthode " + targetMethod.getName() + " n'est pas annoté.");
                }
            }
    
            return targetMethod.invoke(instanceClazz, args);
        }

        private Object convertToFieldType(Field field, String value) {
            Class<?> fieldType = field.getType();
            if (fieldType == String.class) {
                return value;
            } else if (fieldType == int.class || fieldType == Integer.class) {
                return Integer.parseInt(value);
            } else if (fieldType == long.class || fieldType == Long.class) {
                return Long.parseLong(value);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                return Boolean.parseBoolean(value);
            }
            return null;
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
