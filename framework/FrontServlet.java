package etu1787.framework.servlet;

import etu1787.framework.util.*;
import etu1787.framework.*;
import etu1787.framework.annotation.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import java.lang.reflect.Field;
import java.util.Enumeration;

public class FrontServlet extends HttpServlet {
    HashMap<String, Mapping> mappingUrls;

    public void init() throws ServletException {
        mappingUrls = new HashMap<String,Mapping>();
        Util util = new Util();
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        File file = new File(path);
        ArrayList<File> files = util.getFiles(file.getAbsolutePath());
        for(File fl : files){
            String f = "";
            File[] listes = fl.listFiles();
            for(int i=0;i<listes.length;i++){
                if(listes[i].isDirectory() == false){
                    String liste = listes[i].getName();
                    String [] chaine = liste.split(".class");
                    for(int j=0;j<chaine.length;j++){
                        f +=chaine[j] + "//";
                    }
                }
            }
            String [] attr = f.split("//");
            for(int k=0;k<attr.length;k++){
                String name = fl.getName() + "." +attr[k];
                try{
                    Class[] classes = util.getClasses(name);
                    for(int l=0;l<classes.length;l++){
                        Class c = classes[l];
                        Method [] method = c.getDeclaredMethods();
                        for(int m=0;m<method.length;m++){
                            if(method[m].isAnnotationPresent(Urls.class)){
                                Mapping mapping = new Mapping(c.getName(),method[m].getName());
                                String url =method[m].getAnnotation(Urls.class).value();
                                mappingUrls.put(url,mapping);
                            }
                        }
                    }
                }catch(ClassNotFoundException e){

                }
            } 
        }
    }
    
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        String url = request.getRequestURL().toString();
        Util util = new Util();
        String value = util.getUrl(url);
        
        ModelView mv = new ModelView();
        try {
            for (String key : mappingUrls.keySet()) {
                Mapping map = mappingUrls.get(key);
                String className = map.getClassName();
                String methodName = map.getMethod();
                if (value.equals(key)) {
                    Class<?> c = Class.forName(className);
                    Object classe = c.newInstance();
                    mv = (ModelView) c.getDeclaredMethod(methodName).invoke(classe);
                    
                    // Obtenez les paramètres du formulaire et associez-les aux attributs correspondants de la classe modèle
                    if(map.getMethod().equalsIgnoreCase("save")) {
                        for (Field field : c.getDeclaredFields()) {
                            String fieldName = field.getName();
                            String parameterValue = request.getParameter(fieldName);
                            if (parameterValue != null) {
                                Object values = Util.parseType(parameterValue, field.getType());
                                field.setAccessible(true);
                                field.set(classe, values);
                            }
                        }
                    }


                    HashMap<String, Object> data = mv.getData();
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        String keys = entry.getKey();
                        Object val = entry.getValue();
                        request.setAttribute(keys, val);
                    }
                    
                    RequestDispatcher dispatcher = request.getRequestDispatcher(mv.getView());
                    dispatcher.forward(request, response);
                
                }

            }
        } catch (Exception ex) {
            // Gérer les exceptions
        }
    }
  
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            processRequest(request, response);
    }
}