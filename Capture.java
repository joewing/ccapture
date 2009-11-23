
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;

class Capture {

   private static TreeMap<String, Part> parts = new TreeMap<String, Part>();

   public static void main(String[] args) {

      // Load parts.
      try {

         final String[] files = getPartFiles();
         for(String name : files) {
            InputStream stream = Capture.class.getResourceAsStream(name);
            if(stream != null) {
               Part comp = null;
               try {
                  comp = Part.load(stream);
               } catch(Exception ex) {
                  System.err.println("Could not load " + name);
                  ex.printStackTrace(System.err);
                  comp = null;
               }
               if(comp != null) {
                  parts.put(comp.getName(), comp);
               }
            } else {
               System.err.println("Resource not found: " + name);
            }
         }

      } catch(Exception ex) {
         ex.printStackTrace(System.err);
      }

      Project project = new Project();

   }

   public static TreeMap<String, Part> getParts() {
      return parts;
   }

   private static String[] getPartFiles() throws Exception {

      HashSet<String> result = new HashSet<String>();

      // Get the URL to the parts directory.
      URL partsDir = Capture.class.getClassLoader().getResource("parts/");
      if(partsDir == null) {
         final String temp = Capture.class.getName().replace(".", "/")
                           + ".class";
         partsDir = Capture.class.getClassLoader().getResource(temp);
      }
      if(partsDir == null) {
         throw new Exception("parts directory not found");
      }

      // Return a directory listing if a normal directory.
      if(partsDir.getProtocol().equals("file")) {
         File dir = new File(partsDir.toURI());
         for(String name : dir.list()) {
            result.add("parts/" + name);
         }
      }

      // Return a listing if in a jar.
      if(partsDir.getProtocol().equals("jar")) {
         final int index = partsDir.getPath().indexOf("!");
         final String path = partsDir.getPath().substring(5, index);
         JarFile jar = new JarFile(URLDecoder.decode(path, "UTF-8"));
         Enumeration<JarEntry> entries = jar.entries();
         while(entries.hasMoreElements()) {
            final String name = entries.nextElement().getName();
            if(name.startsWith("parts/")) {
               result.add(name);
            }
         }
      }

      return result.toArray(new String[result.size()]);

   }

}

