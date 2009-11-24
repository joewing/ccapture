
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

class Part {

   public static int ROTATION_MASK     = 3;
   public static int MIRROR_HORZ_MASK  = 4;
   public static int MIRROR_VERT_MASK  = 8;

   private static final class LineNode {
      public int x1, y1;
      public int x2, y2;
   }

   private static final class RectNode {
      public int x, y;
      public int width, height;
   }

   private static final class CircleNode {
      public int x, y;
      public int radius;
   }

   /** Initialize the rotation table if it isn't already. */
   private static void initRotationTable() {
      if(rotationTable == null) {

         // rx = x * rotationTable[t * 4 + 0] + y * rotationTable[t * 4 + 1]
         // ry = x * rotationTable[t * 4 + 2] + y * rotationTable[t * 4 + 3]
         rotationTable = new int[6 * 4];

         // 0 degrees
         rotationTable[0 * 4 + 0] = 1;    // cos(t)
         rotationTable[0 * 4 + 1] = 0;    // sin(t)
         rotationTable[0 * 4 + 2] = 0;    // -sin(t)
         rotationTable[0 * 4 + 3] = 1;    // cos(t)

         // 90 degrees
         rotationTable[1 * 4 + 0] = 0;
         rotationTable[1 * 4 + 1] = -1;
         rotationTable[1 * 4 + 2] = 1;
         rotationTable[1 * 4 + 3] = 0;

         // 180 degrees
         rotationTable[2 * 4 + 0] = -1;
         rotationTable[2 * 4 + 1] = 0;
         rotationTable[2 * 4 + 2] = 0;
         rotationTable[2 * 4 + 3] = -1;

         // 270 degrees
         rotationTable[3 * 4 + 0] = 0;
         rotationTable[3 * 4 + 1] = 1;
         rotationTable[3 * 4 + 2] = -1;
         rotationTable[3 * 4 + 3] = 0;

         // Mirror left-right
         rotationTable[4 * 4 + 0] = -1;
         rotationTable[4 * 4 + 1] = 0;
         rotationTable[4 * 4 + 2] = 0;
         rotationTable[4 * 4 + 3] = 1;

         // Mirror top-bottom
         rotationTable[5 * 4 + 0] = 1;
         rotationTable[5 * 4 + 1] = 0;
         rotationTable[5 * 4 + 2] = 0;
         rotationTable[5 * 4 + 3] = -1;

      }
   }

   public static Part load(InputStream stream) throws Exception {

      initRotationTable();

      Part part = new Part();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(stream);
      XMLElement root = new XMLElement(doc);

      XMLElement nameElement = root.getChild("Name");
      if(nameElement == null) {
         throw new Exception("Name not set");
      }
      part.name = nameElement.getTextNormalize();

      XMLElement drawElement = root.getChild("Draw");
      if(drawElement != null) {
         for(XMLElement e : drawElement.getChildren()) {
            if(e.getName().equals("Line")) {
               parseLine(part, e);
            } else if(e.getName().equals("Rect")) {
               parseRect(part, e);
            } else if(e.getName().equals("Circle")) {
               parseCircle(part, e);
            }
         }
      }

      XMLElement connectionsElement = root.getChild("Connections");
      if(connectionsElement != null) {
         for(XMLElement e : connectionsElement.getChildren()) {
            if(e.getName().equals("Terminal")) {
               parseTerminal(part, e);
            }
         }
      }

      return part;

   }

   private static void parseLine(Part part, XMLElement e) throws Exception {

      LineNode node = new LineNode();

      String x1 = e.getAttributeValue("x1");
      if(x1 == null) {
         throw new Exception("x1 not set for Line");
      }
      node.x1 = Integer.parseInt(x1);

      String y1 = e.getAttributeValue("y1");
      if(y1 == null) {
         throw new Exception("y1 not set for Line");
      }
      node.y1 = Integer.parseInt(y1);

      String x2 = e.getAttributeValue("x2");
      if(x2 == null) {
         throw new Exception("x2 not set for Line");
      }
      node.x2 = Integer.parseInt(x2);

      String y2 = e.getAttributeValue("y2");
      if(y2 == null) {
         throw new Exception("y2 not set for Line");
      }
      node.y2 = Integer.parseInt(y2);

      if(node.x1 >= part.size) {
         part.size = node.x1 + 1;
      }
      if(node.x2 >= part.size) {
         part.size = node.x2 + 1;
      }
      if(node.y1 >= part.size) {
         part.size = node.y1 + 1;
      }
      if(node.y2 >= part.size) {
         part.size = node.y2 + 1;
      }

      part.lines.add(node);

   }

   private static void parseRect(Part part, XMLElement e) throws Exception {

      RectNode node = new RectNode();

      String x = e.getAttributeValue("x");
      if(x == null) {
         throw new Exception("x not set for Rect");
      }
      node.x = Integer.parseInt(x);

      String y = e.getAttributeValue("y");
      if(y == null) {
         throw new Exception("y not set for Rect");
      }
      node.y = Integer.parseInt(y);

      String width = e.getAttributeValue("width");
      if(width == null) {
         throw new Exception("width not set for Rect");
      }
      node.width = Integer.parseInt(width);

      String height = e.getAttributeValue("height");
      if(height == null) {
         throw new Exception("height not set for Rect");
      }
      node.height = Integer.parseInt(height);

      if(node.x + node.width > part.size) {
         part.size = node.x + node.width;
      }
      if(node.y + node.height > part.size) {
         part.size = node.y + node.height;
      }

      part.rects.add(node);

   }

   private static void parseCircle(Part part, XMLElement e) throws Exception {

      CircleNode node = new CircleNode();

      String x = e.getAttributeValue("x");
      if(x == null) {
         throw new Exception("x not set for Circle");
      }
      node.x = Integer.parseInt(x);

      String y = e.getAttributeValue("y");
      if(y == null) {
         throw new Exception("y not set for Circle");
      }
      node.y = Integer.parseInt(y);

      String radius = e.getAttributeValue("radius");
      if(radius == null) {
         throw new Exception("radius not set for Circle");
      }
      node.radius = Integer.parseInt(radius);

      if(node.x + node.radius > part.size) {
         part.size = node.x + node.radius;
      }
      if(node.y + node.radius > part.size) {
         part.size = node.y + node.radius;
      }

      part.circles.add(node);

   }

   private static void parseTerminal(Part part, XMLElement e) throws Exception {

      Point node = new Point();

      String x = e.getAttributeValue("x");
      if(x == null) {
         throw new Exception("x not set for Terminal");
      }
      node.x = Integer.parseInt(x);

      String y = e.getAttributeValue("y");
      if(y == null) {
         throw new Exception("y not set for Terminal");
      }
      node.y = Integer.parseInt(y);

      part.terminals.add(node);

   }

   private Part() {
   }

   public String getName() {
      return name;
   }

   public String toString() {
      return getName();
   }

   /** Rotate p around (0, 0) according to rotation r. */
   private void rotate(Point p, int r) {
      final int index = r & ROTATION_MASK;
      int xc = p.x * rotationTable[index * 4 + 0]
             + p.y * rotationTable[index * 4 + 1];
      int yc = p.x * rotationTable[index * 4 + 2]
             + p.y * rotationTable[index * 4 + 3];
      p.x = xc;
      p.y = yc;
      if((r & MIRROR_HORZ_MASK) != 0) {
         xc = p.x * rotationTable[4 * 4 + 0]
            + p.y * rotationTable[4 * 4 + 1];
         yc = p.x * rotationTable[4 * 4 + 2]
            + p.y * rotationTable[4 * 4 + 3];
         p.x = xc;
         p.y = yc;
      }
      if((r & MIRROR_VERT_MASK) != 0) {
         xc = p.x * rotationTable[5 * 4 + 0]
            + p.y * rotationTable[5 * 4 + 1];
         yc = p.x * rotationTable[5 * 4 + 2]
            + p.y * rotationTable[5 * 4 + 3];
         p.x = xc;
         p.y = yc;
      }
   }

   public int snapToTerminal(int x, int y, Point p, int rotation) {

      Point temp = new Point();
      int best_distance = Integer.MAX_VALUE;
      int bestx = p.x;
      int besty = p.y;
      final int offset = size / 2;
      for(Point t : terminals) {
         temp.x = x + t.x - offset;
         temp.y = y + t.y - offset;
         rotate(temp, rotation);
         temp.x += offset;
         temp.y += offset;
         final int dist = (int)temp.distance(p);
         if(dist < best_distance) {
            best_distance = dist;
            bestx = temp.x;
            besty = temp.y;
         }
      }
      p.x = bestx;
      p.y = besty;
      return best_distance;

   }

   public void draw(Graphics g, int x, int y, int rotation, int scale) {
      Point p = new Point();
      final int xoffset = size * scale / 2;
      final int yoffset = size * scale / 2;
      for(LineNode node : lines) {

         p.x = node.x1 * scale - xoffset;
         p.y = node.y1 * scale - yoffset;
         rotate(p, rotation);
         final int x1 = p.x + xoffset + x;
         final int y1 = p.y + yoffset + y;

         p.x = node.x2 * scale - xoffset;
         p.y = node.y2 * scale - yoffset;
         rotate(p, rotation);
         final int x2 = p.x + xoffset + x;
         final int y2 = p.y + yoffset + y;

         g.drawLine(x1, y1, x2, y2);

      }
      for(RectNode node : rects) {

         p.x = node.x * scale - xoffset;
         p.y = node.y * scale - yoffset;
         rotate(p, rotation);
         final int x1 = p.x + xoffset + x;
         final int y1 = p.y + yoffset + y;

         p.x = (node.x + node.width - 1) * scale - xoffset;
         p.y = node.y * scale - yoffset;
         rotate(p, rotation);
         final int x2 = p.x + xoffset + x;
         final int y2 = p.y + yoffset + y;

         p.x = node.x * scale - xoffset;
         p.y = (node.y + node.height - 1) * scale - yoffset;
         rotate(p, rotation);
         final int x3 = p.x + xoffset + x;
         final int y3 = p.y + yoffset + y;

         p.x = (node.x + node.width - 1) * scale - xoffset;
         p.y = (node.y + node.height - 1) * scale - yoffset;
         rotate(p, rotation);
         final int x4 = p.x + xoffset + x;
         final int y4 = p.y + yoffset + y;

         g.drawLine(x1, y1, x2, y2);
         g.drawLine(x1, y1, x3, y3);
         g.drawLine(x3, y3, x4, y4);
         g.drawLine(x2, y2, x4, y4);

      }
      for(CircleNode node : circles) {

         p.x = node.x * scale - xoffset;
         p.y = node.y * scale - yoffset;
         rotate(p, rotation);
         final int xc = p.x + xoffset + x;
         final int yc = p.y + yoffset + y;

         final int radius = node.radius * scale;

         g.drawOval(xc - radius / 2, yc - radius / 2, radius, radius);

      }

   }

   public void drawHandles(Graphics g, int x, int y, int rotation, int scale) {

      Point p = new Point();
      final double theta = Math.toRadians(rotation * 90.0);
      final int xoffset = size * scale / 2;
      final int yoffset = size * scale / 2;
      for(Point node : terminals) {

         p.x = node.x * scale - xoffset;
         p.y = node.y * scale - yoffset;
         rotate(p, rotation);
         final int xc = p.x + xoffset + x;
         final int yc = p.y + yoffset + y;

         final int radius = scale;

         g.fillOval(xc - radius / 2, yc - radius / 2, radius, radius);

      }

   }

   public Cursor getCursor(int rotation, int scale) {

      // Create the image.
      final int scaledSize = size * scale;
      BufferedImage image = new BufferedImage(scaledSize, scaledSize,
         BufferedImage.TYPE_INT_ARGB);
      Graphics g = image.getGraphics();
      g.setColor(new Color(0, 0, 0, 0));
      g.fillRect(0, 0, scaledSize, scaledSize);
      g.setColor(Color.BLACK);
      draw(g, 0, 0, rotation, scale);

      // Create the cursor from the image.
      Toolkit tk = Toolkit.getDefaultToolkit();
      return tk.createCustomCursor(image, new Point(0, 0), "part");
      
   }

   public int getWidth() {
      return size;
   }

   public int getHeight() {
      return size;
   }

   private String name;
   private LinkedList<LineNode> lines = new LinkedList<LineNode>();
   private LinkedList<RectNode> rects = new LinkedList<RectNode>();
   private LinkedList<CircleNode> circles = new LinkedList<CircleNode>();
   private LinkedList<Point> terminals = new LinkedList<Point>();
   private int size;

   private static int rotationTable[] = null;

}

