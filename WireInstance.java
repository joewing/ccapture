
import java.util.*;
import java.awt.*;
import javax.swing.*;

import org.jdom.*;

class WireInstance extends BaseInstance {

   public WireInstance(int x1, int y1, int x2, int y2) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
   }

   public WireInstance(Element e) throws Exception {

      String x1str = e.getAttributeValue("x1");
      if(x1str == null) {
         throw new Exception("x1 not set");
      }
      x1 = Integer.parseInt(x1str);

      String y1str = e.getAttributeValue("y1");
      if(y1str == null) {
         throw new Exception("y1 not set");
      }
      y1 = Integer.parseInt(y1str);

      String x2str = e.getAttributeValue("x2");
      if(x2str == null) {
         throw new Exception("x2 not set");
      }
      x2 = Integer.parseInt(x2str);

      String y2str = e.getAttributeValue("y2");
      if(y2str == null) {
         throw new Exception("y2 not set");
      }
      y2 = Integer.parseInt(y2str);

   }

   public int getX() {
      return x1 < x2 ? x1 : x2;
   }

   public int getY() {
      return y1 < y2 ? y1 : y2;
   }

   public int getWidth() {
      return Math.abs(x1 - x2);
   }

   public int getHeight() {
      return Math.abs(y1 - y2);
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
      x1 += diffx;
      y1 += diffy;
      x2 += diffx;
      y2 += diffy;

   }

   public void draw(Graphics g, int scale) {
      g.drawLine(x1 * scale, y1 * scale, x2 * scale, y2 * scale);
   }

   public void drawHandles(Graphics g, int scale) {

      g.fillOval(x1 * scale - scale / 2, y1 * scale - scale / 2, scale, scale);
      g.fillOval(x2 * scale - scale / 2, y2 * scale - scale / 2, scale, scale);

   }

   public boolean contains(int x, int y, int scale) {

      // Check the rectangle containing the line.
      if((x < x1 && x < x2) || (x > x1 && x > x2)) {
         return false;
      }
      if((y < y1 && y < y2) || (y > y1 && y > y2)) {
         return false;
      }

      // Set (x1, y1) to be (0, 0).
      final int nx2 = x2 - x1;
      final int ny2 = y2 - y1;

      // Now use 'y = mx + b' if 'nx2 != 0', otherwise use
      // 'x = my + b'. We know either nx2 != 0 or ny2 != 0 since
      // we don't allow lines with zero length.
      if(nx2 != 0) {

         // Use 'y = mx + b' -> 'm = y / x'.
         final int m = ny2 / nx2;
         final int b = y1 - m * x1;
         final int ty = m * x + b;
         return Math.abs(ty - y) <= 2 * scale;

      } else if(ny2 != 0) {

         // Use 'x = my + b' -> 'm = x / y'.
         final int m = nx2 / ny2;
         final int b = x1 - m * y1;
         final int tx = m * y + b;
         return Math.abs(tx - x) <= 2 * scale;

      } else {

         // This should never happen, but in case we read in a
         // bad file, we allow for it.
         return false;

      }

   }

   public void updateMenu(Schematic schematic, JPopupMenu menu) {
   }

   public void save(Element root) {
      Element e = new Element("Wire");
      root.addContent(e);
      e.setAttribute("x1", Integer.toString(x1));
      e.setAttribute("y1", Integer.toString(y1));
      e.setAttribute("x2", Integer.toString(x2));
      e.setAttribute("y2", Integer.toString(y2));
   }

   private int x1, y1;
   private int x2, y2;

}

