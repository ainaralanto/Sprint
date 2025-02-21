package mg.p16.Spring;

import java.io.*;
import java.lang.reflect.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

import mg.p16.Annotationcontroller.*;
import mg.p16.classe.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import jakarta.servlet.annotation.MultipartConfig;

import com.thoughtworks.paranamer.*;
import com.google.gson.*;

@MultipartConfig
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

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuffer requestURL = request.getRequestURL();
        String[] requestUrlSplitted = requestURL.toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];

        if (controllerSearched.contains("?")) {
            controllerSearched = controllerSearched.split("\\?")[0];
        }
        System.out.println(controllerSearched);

        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        try{
            if (error != null && !error.isEmpty()) {
                throw new ServletException(error);
            }
            
            if (!urlMapping.containsKey(controllerSearched)) {
                throw new ServletException("Aucune méthode associée à ce chemin : " + controllerSearched);
            }
            else {
                Mapping mapping = urlMapping.get(controllerSearched);
                String method = request.getMethod();

                if (!request.getMethod().equalsIgnoreCase(mapping.getHttpMethod())) {
                    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    throw new ServletException("Méthode HTTP non autorisée pour l'URL : " + controllerSearched);

                }

                Object result = invokeControllerMethod(mapping, request);

                Method targetMethod = getTargetMethod(mapping);

                if (targetMethod.isAnnotationPresent(Restapi.class)) {
                    response.setContentType("application/json");
                    Gson gson = new Gson();  

                    if (result instanceof ModelView) {
                        ModelView modelView = (ModelView) result;
                        String json = gson.toJson(modelView.getData());
                        out.write(json);
                    } else {
                        String json = gson.toJson(result);
                        out.write(json);
                    }
                } else {
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
                        throw new ServletException("Type de retour non reconnu.");
                    }
                }

            }
        } catch (Exception e) {
            sendErrorResponse(response, "Une erreur est survenue : " + e.getMessage());
        }
        out.close();
    }


        private void handleJsonResponse(Object result, HttpServletResponse response) throws IOException {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();

            Gson gson = new Gson();
            String jsonResponse;

            if (result instanceof ModelView) {
                ModelView modelView = (ModelView) result;
                jsonResponse = gson.toJson(modelView.getData());
            } else {
                jsonResponse = gson.toJson(result);
            }

            out.print(jsonResponse);
            out.flush();
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
    
                    validate(paramObject);
                    args[i] = paramObject;

                } else if (parameters[i].isAnnotationPresent(UploadFile.class)) {
                    UploadFile uploadFileAnnotation = parameters[i].getAnnotation(UploadFile.class);
                    String fileNameParam = uploadFileAnnotation.value(); 
                    Part filePart = request.getPart(fileNameParam);
                
                    if (filePart != null && filePart.getSize() > 0) {
                        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                
                        String uploadDir = request.getServletContext().getRealPath("/uploads/");
                        File uploadFolder = new File(uploadDir);
                        if (!uploadFolder.exists()) {
                            uploadFolder.mkdirs();
                        }
                
                        Path filePath = Paths.get(uploadDir, fileName);
                
                        try (InputStream fileContent = filePart.getInputStream()) {
                            Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
                        }
                
                        Fichier fichier = new Fichier(fileName, filePath.toString());
                        fichier.setContenu(Files.readAllBytes(filePath));

                        args[i] = fichier;
                    } else {
                        args[i] = null;
                    }

                    
                }
                
                else {
                    throw new RuntimeException("ETU002527 ; Le paramètre " + parameters[i].getName() + " dans la méthode " + targetMethod.getName() + " n'est pas annoté.");
                }
            }
    
            return targetMethod.invoke(instanceClazz, args);
        }

        public static void validate(Object paramObject) throws Exception {
            for (Field field : paramObject.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object fieldValue;
                try {
                    fieldValue = field.get(paramObject);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Erreur d'accès au champ " + field.getName(), e);
                }
        
                if (field.isAnnotationPresent(Validation.NotNull.class) && fieldValue == null || fieldValue.toString() == "") {
                    System.out.println(field.getName()+" value: "+fieldValue+" ,misy not null");
                    throw new ServletException(field.getAnnotation(Validation.NotNull.class).message());
                }
                
                if (field.isAnnotationPresent(Validation.ValidEmail.class) && fieldValue != null) {
                    System.out.println(field.getName()+" misy email");

                    String email = fieldValue.toString();
                    if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                        throw new ServletException(field.getAnnotation(Validation.ValidEmail.class).message());
                    }
                }
                
                if (field.isAnnotationPresent(Validation.ValidInt.class) && fieldValue != null) {
                    int value = Integer.parseInt(fieldValue.toString());
                    Validation.ValidInt validInt = field.getAnnotation(Validation.ValidInt.class);
                    if (value < validInt.min() || value > validInt.max()) {
                        throw new ServletException(validInt.message());
                    }
                }
                
                if (field.isAnnotationPresent(Validation.ValidString.class) && fieldValue != null) {
                    int length = fieldValue.toString().length();
                    Validation.ValidString validString = field.getAnnotation(Validation.ValidString.class);
                    if (length < validString.min() || length > validString.max()) {
                        throw new ServletException(validString.message());
                    }
                }
            }
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
                        String httpMethod = "GET";
                        String annotationValue = "";
                        boolean isPost = false;
    
                        if (m.isAnnotationPresent(AnnotationGet.class)) {
                            annotationValue = m.getAnnotation(AnnotationGet.class).value();
                        } else if (m.isAnnotationPresent(AnnotationPost.class)) {
                            annotationValue = m.getAnnotation(AnnotationPost.class).value();
                            httpMethod = "POST";
                            isPost = true;
                        }
    
                        if (!annotationValue.isEmpty()) {
                            Mapping mapping = new Mapping(className, m.getName(), httpMethod);
                            if (urlMapping.containsKey(annotationValue)) {
                                throw new RuntimeException("Conflit d'URL : " + annotationValue);
                            }
                            urlMapping.put(annotationValue, mapping);
                        }
    
                        // // Vérifier si la méthode contient des paramètres annotés @UploadFile
                        // for (Parameter param : m.getParameters()) {
                        //     if (param.isAnnotationPresent(UploadFile.class)) {
                        //         if (!isPost) {
                        //             throw new RuntimeException("L'annotation @UploadFile doit être utilisée avec une requête POST.");
                        //         }
                        //     }
                        // }
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    
    private void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, String message) throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("error/error.jsp").forward(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html lang='fr'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Erreur - Page introuvable</title>");
        out.println("<style>");
        out.println("body { background-color: #f8d7da; font-family: Arial, sans-serif; text-align: center; padding: 50px; }");
        out.println(".error-container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2); display: inline-block; max-width: 600px; width: 90%; }");
        out.println("h2 { color: #721c24; }");
        out.println("p { color: #721c24; font-size: 18px; }");
        out.println(".error-code { font-size: 72px; font-weight: bold; color: #dc3545; margin: 0; }");
        out.println("@keyframes fadeIn { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }");
        out.println(".error-container { animation: fadeIn 0.5s ease-in-out; }");
        out.println("</style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='error-container'>");
        out.println("<p class='error-code'>⚠ 500</p>");
        out.println("<h2>Une erreur est survenue</h2>");
        out.println("<p><strong>" + message + "</strong></p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }
    
}
