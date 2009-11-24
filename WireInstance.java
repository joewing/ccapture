
import java.util.*;
import java.awt.*;
import javax.swing.*;

class WireInstance extends BaseInstance {

   public WireInstance(int x1, int y1, int x2, int y2) {
      point1.x = x1;
      point1.y = y1;
      point2.x = x2;
      point2.y = y2;
   }

   public WireInstance(XMLElement e) throws Exception {

      String x1str = e.getAttributeValue("x1");
      if(x1str == null) {
         throw new Exception("x1 not set");
      }
      point1.x = Integer.parseInt(x1str);

      String y1str = e.getAttributeValue("y1");
      if(y1str == null) {
         throw new Exception("y1 not set");
      }
      point1.y = Integer.parseInt(y1str);

      String x2str = e.getAttributeValue("x2");
      if(x2str == null) {
         throw new Exception("x2 not set");
      }
      point2.x = Integer.parseInt(x2str);

      String y2str = e.getAttributeValue("y2");
      if(y2str == null) {
         throw new Exception("y2 not set");
      }
      point2.y = Integer.parseInt(y2str);

   }

   public int getX() {
      return point1.x < point2.x ? point1.x : point2.x;
   }

   public int getY() {
      return point1.y < point2.y ? point1.y : point2.y;
   }

   public int getWidth() {
      return Math.abs(point1.x - point2.x);
   }

   public int getHeight() {
      return Math.abs(point1.y - point2.y);
   }

   public void move(int x, int y) {

      // Move the upper-left corner to (x, y).

      // Get our current upper-left corner.
      final int basex = getX();
      final int basey = getY();

      // Determine the difference to the new upper-left corner.
      final int diffx = x - basex;
      final int diffy = y - basey;

      // Update our coordinates.
      point1.x += diffx;
      point1.y += diffy;
      point2.x += diffx;
      point2.y += diffy;

   }

   public void draw(Graphics g, int scale) {
      g.drawLine(point1.x * scale, point1.y * scale,
                 point2.x * scale, point2.y * scale);
   }

   public void drawHandles(Graphics g, int scale) {

      g.fillOval(point1.x * scale - scale / 2,
                 point1.y * scale - scale / 2, scale, scale);
      g.fillOval(point2.x * scale - scale / 2,
                 point2.y * scale - scale / 2, scale, scale);

   }

   public boolean contains(int x, int y, int scale) {

      // Check the rectangle containing the line.
      if((x < point1.x && x < point2.x) || (x > point1.x && x > point2.x)) {
         return false;
      }
      if((y < point1.y && y < point2.y) || (y > point1.y && y > point2.y)) {
         return false;
      }

      // Set (x1, y1) to be (0, 0).
      final int nx2 = point2.x - point1.x;
      final int ny2 = point2.y - point1.y;

      // Now use 'y = mx + b' if 'nx2 != 0', otherwise use
      // 'x = my + b'. We know either nx2 != 0 or ny2 != 0 since
      // we don't allow lines with zero length.
      if(nx2 != 0) {

         // Use 'y = mx + b' -> 'm = y / x'.
         final int m = ny2 / nx2;
         final int b = point1.y - m * point1.x;
         final int ty = m * x + b;
         return Math.abs(ty - y) <= 2 * scale;

      } else if(ny2 != 0) {

         // Use 'x = my + b' -> 'm = x / y'.
         final int m = nx2 / ny2;
         final int b = point1.x - m * point1.y;
         final int tx = m * y + b;
         return Math.abs(tx - x) <= 2 * scale;

      } else {

         // This should never happen, but in case we read in a
         // bad file, we allow for it.
         return false;

      }

   }

   public int snapToTerminal(Point p) {

      // Compute distances.
      final int dist1 = (int)p.distance(point1);
      final int dist2 = (int)p.distance(point2);

      // Return the smaller.
      if(dist1 < dist2) {
         p.x = point1.x;
         p.y = point1.y;
         return dist1;
      } else {
         p.x = point2.x;
         p.y = point2.y;
         return dist2;
      }

   }

   public void updateMenu(Schematic schematic, JPopupMenu menu) {
   }

   public void save(XMLElement root) {
      XMLElement e = root.createChild("Wire");
      e.setAttribute("x1", Integer.toString(point1.x));
      e.setAttribute("y1", Integer.toString(point1.y));
      e.setAttribute("x2", Integer.toString(point2.x));
      e.setAttribute("y2", Integer.toString(point2.y));
   }

   private Point point1 = new Point();
   private Point point2 = new Point();

}

