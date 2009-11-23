
import java.util.*;
import java.io.*;

class Capture {

   private static TreeMap<String, Part> parts;

   public static void main(String[] args) {

      try {

         parts = new TreeMap<String, Part>();
         File dir = new File("parts");
         File[] files = dir.listFiles();
         for(File f : files) {
            if(f.isFile()) {
               Part comp = null;
               try {
                  comp = Part.load(f);
               } catch(Exception ex) {
                  System.err.println("Could not load " + f.toString()
                                     + ": " + ex.toString());
                  comp = null;
               }
               if(comp != null) {
                  parts.put(comp.getName(), comp);
               }
            }
         }

      } catch(Exception ex) {
         ex.printStackTrace(System.out);
      }

      Project project = new Project();

   }

   public static TreeMap<String, Part> getParts() {
      return parts;
   }

}

