
import java.util.*;
import org.w3c.dom.*;

class XMLElement {

   public XMLElement(Element e) {
      element = e;
   }

   public XMLElement(Document d) throws Exception {
      element = d.getDocumentElement();
   }

   public XMLElement(Document d, String name) throws Exception {
      element = d.createElement(name);
      d.appendChild(element);
   }

   public XMLElement createChild(String name) {
      Document d = element.getOwnerDocument();
      Element e = d.createElement(name);
      element.appendChild(e);
      return new XMLElement(e);
   }

   public String getName() throws Exception {
      return element.getTagName();
   }

   public String getTextNormalize() throws Exception {

      StringBuffer buffer = new StringBuffer();
      Node n = element.getFirstChild();
      while(n != null) {
         if(n.getNodeType() == Node.TEXT_NODE) {
            buffer.append(n.getTextContent());
         }
         n = n.getNextSibling();
      }
      return buffer.toString().trim();

   }

   public String getAttributeValue(String name) throws Exception {

      NamedNodeMap attrs = element.getAttributes();
      if(attrs == null) {
         return null;
      }

      Node node = attrs.getNamedItem(name);
      if(node == null || node.getNodeType() != Node.ATTRIBUTE_NODE) {
         return null;
      }

      Attr attr = (Attr)node;
      return attr.getValue();

   }

   public void setAttribute(String name, String value) {
      element.setAttribute(name, value);
   }

   public XMLElement getChild(String name) throws Exception {
      Node n = element.getFirstChild();
      while(n != null) {
         if(n.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element)n;
            if(e.getTagName().equals(name)) {
               return new XMLElement((Element)n);
            }
         }
         n = n.getNextSibling();
      }
      return null;
   }

   public XMLElement[] getChildren() throws Exception {
      ArrayList<XMLElement> result = new ArrayList<XMLElement>();
      Node n = element.getFirstChild();
      while(n != null) {
         if(n.getNodeType() == Node.ELEMENT_NODE) {
            result.add(new XMLElement((Element)n));
         }
         n = n.getNextSibling();
      }
      return result.toArray(new XMLElement[result.size()]);
   }

   public void addContent(XMLElement c) throws Exception {
      element.appendChild(c.element);
   }

   private Element element;

}

