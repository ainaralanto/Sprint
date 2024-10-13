package mg.p16.classe;

public class Mapping {
    String className;
    String methodeName;
    String httpMethod;

    public Mapping(String className, String methodeName, String httpMethod) {
        this.className = className;
        this.methodeName = methodeName;
        this.httpMethod = httpMethod;
    }
    
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodeName() {
        return methodeName;
    }

    public void setMethodeName(String methodeName) {
        this.methodeName = methodeName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    
}
