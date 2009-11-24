
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class PartInstance extends BaseInstance {

   public PartInstance(Part p, int x, int y, int rotation) {
      part = p;
      this.rotation = rotation;
      move(x, y);
   }

   public PartInstance(Part p, XMLElement e) throws Exception {
      part = p;

      String xstr = e.getAttributeValue("x");
      if(xstr == null) {
         throw new Exception("x not set");
      }
      x = Integer.parseInt(xstr);

      String ystr = e.getAttributeValue("y");
      if(ystr == null) {
         throw new Exception("y not set");
      }
      y = Integer.parseInt(ystr);

      String rstr = e.getAttributeValue("rotation");
      if(rstr == null) {
         throw new Exception("rotation not set");
      }
      rotation = Integer.parseInt(rstr);

   }

   public void move(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public void rotate() {
      final boolean mirror_horz = (rotation & Part.MIRROR_HORZ_MASK) != 0;
      final boolean mirror_vert = (rotation & Part.MIRROR_VERT_MASK) != 0;
      rotation = (rotation + 1) & 3;
      if(mirror_horz) {
         rotation |= Part.MIRROR_VERT_MASK;
      }
      if(mirror_vert) {
         rotation |= Part.MIRROR_HORZ_MASK;
      }
   }

   public void mirrorHorizontal() {
      rotation ^= Part.MIRROR_HORZ_MASK;
   }

   public void mirrorVertical() {
      rotation ^= Part.MIRROR_VERT_MASK;
   }

   public int getX() {
      return x;
   }

   public int getY() {
      return y;
   }

   public int getWidth() {
      return part.getWidth();
   }

   public int getHeight() {
      return part.getHeight();
   }

   public void draw(Graphics g, int scale) {
      part.draw(g, x * scale, y * scale, rotation, scale);
   }

   public void drawHandles(Graphics g, int scale) {
      part.drawHandles(g, x * scale, y * scale, rotation, scale);
   }

   public boolean contains(int x, int y, int scale) {
      x *= scale;
      y *= scale;
      final int width = part.getWidth() * scale;
      final int height = part.getHeight() * scale;
      final int x1 = this.x * scale;
      final int y1 = this.y * scale;
      final int x2 = x1 + width;
      final int y2 = y1 + width;
      return x >= x1 && x <= x2 && y >= y1 && y <= y2;
   }

   public void updateMenu(Schematic schematic, JPopupMenu menu) {

      final Schematic schem = schematic;

      menu.add(new JSeparator());

      JMenuItem rotateItem = new JMenuItem("Rotate");
      menu.add(rotateItem);
      rotateItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            rotate();
            schem.repaint();
         }
      });

      JMenuItem rlMirrorItem = new JMenuItem("Mirror right-left");
      menu.add(rlMirrorItem);
      rlMirrorItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            mirrorHorizontal();
            schem.repaint();
         }
      });

      JMenuItem tbMirrorItem = new JMenuItem("Mirror top-bottom");
      menu.add(tbMirrorItem);
      tbMirrorItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            mirrorVertical();
            schem.repaint();
         }
      });

   }

   public void save(XMLElement root) {
      XMLElement e = root.createChild("Part");
      e.setAttribute("type", part.getName());
      e.setAttribute("rotation", Integer.toString(rotation));
      e.setAttribute("x", Integer.toString(x));
      e.setAttribute("y", Integer.toString(y));
   }

   private Part part;
   private int rotation;
   private int x;
   private int y;

}

