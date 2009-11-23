
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

class Schematic extends JPanel {

   private Project project;
   private int width;
   private int height;
   private int scale = 4;
   private int rotation = 0;
   private int mode;
   private LinkedList<BaseInstance> parts = new LinkedList<BaseInstance>();
   private HashSet<BaseInstance> group = new HashSet<BaseInstance>();
   BaseInstance selection = null;

   private int wire_x1, wire_y1;
   private int wire_x2, wire_y2;
   private int oldx, oldy;

   public Schematic(Project p) {

      project = p;
      mode = project.getMode();
      parts = new LinkedList<BaseInstance>();

      wire_x1 = -1;
      wire_x2 = -1;
      oldx = -1;

      SchematicMouseListener l = new SchematicMouseListener();
      addMouseListener(l);
      addMouseMotionListener(l);

   }

   public void paint(Graphics g) {

      final int width = getWidth();
      final int height = getHeight();

      // Clear the background.
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, width, height);

      // Draw parts.
      for(BaseInstance inst : parts) {
         if(inst == selection) {
            g.setColor(Color.RED);
         } else if(group.contains(inst)) {
            g.setColor(Color.BLUE);
         } else {
            g.setColor(Color.BLACK);
         }
         inst.draw(g, scale);
         if(inst == selection) {
            g.setXORMode(Color.WHITE);
            selection.drawHandles(g, scale);
            g.setPaintMode();
         }
      }

   }

   private final class SchematicMouseListener
      implements MouseListener, MouseMotionListener {

      private void leftClick(int x, int y) {
         switch(mode) {
         case Project.MODE_INSERT:
         {
            Part part = project.getSelectedPart();
            if(part != null) {
               x /= scale;
               y /= scale;
               PartInstance p = new PartInstance(part, x, y, rotation);
               insertPart(p, false);
               repaint();
            }
            break;
         }
         case Project.MODE_WIRE:
            addWirePoint(x, y);
            break;
         case Project.MODE_GROUP:
         {
            BaseInstance part = getPart(x, y);
            if(part != null) {
               if(group.contains(part)) {
                  group.remove(part);
               } else {
                  group.add(part);
               }
               repaint();
            }
            break;
         }
         default:
            setSelection(getPart(x, y));
            break;
         }
      }

      private void rightClick(int x, int y) {
         if(mode == Project.MODE_INSERT) {
            rotate();
         } else if(mode == Project.MODE_WIRE && wire_x1 != -1) {
            endWire();
         } else {
            showMenu(x, y);
         }
      }

      public void mouseClicked(MouseEvent e) {
         switch(e.getButton()) {
         case MouseEvent.BUTTON1:
            if((e.getModifiersEx() & e.CTRL_DOWN_MASK) != 0) {
               rightClick(e.getX(), e.getY());
            } else {
               leftClick(e.getX(), e.getY());
            }
            break;
         case MouseEvent.BUTTON3:
            rightClick(e.getX(), e.getY());
            break;
         default:
            break;
         }
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
         if(mode == Project.MODE_SELECT) {
            setSelection(getPart(e.getX(), e.getY()));
         }
      }

      public void mouseReleased(MouseEvent e) {
         if(oldx != -1) {
            if(selection != null) {
               movePart(selection, selection.getX(), selection.getY(), false);
            }
            oldx = -1;
         }
      }

      public void mouseDragged(MouseEvent e) {
         if(mode == Project.MODE_SELECT && selection != null) {

            // Move a single part.
            final int x = e.getX() / scale - selection.getWidth() / 2;
            final int y = e.getY() / scale - selection.getHeight() / 2;
            if(oldx == -1) {
               oldx = selection.getX();
               oldy = selection.getY();
            }
            selection.move(x, y);
            repaint();

         } else if(mode == Project.MODE_GROUP && !group.isEmpty()) {

            // Move a group of parts.

            // Determine a bounding box for the group.
            int minx = Integer.MAX_VALUE;
            int miny = Integer.MAX_VALUE;
            int maxx = Integer.MIN_VALUE;
            int maxy = Integer.MIN_VALUE;
            for(BaseInstance i : group) {
               final int x1 = i.getX();
               final int y1 = i.getY();
               final int x2 = x1 + i.getWidth();
               final int y2 = y1 + i.getHeight();
               if(x1 < minx) {
                  minx = x1;
               }
               if(y1 < miny) {
                  miny = y1;
               }
               if(x2 > maxx) {
                  maxx = x2;
               }
               if(y2 > maxy) {
                  maxy = y2;
               }
            }
            final int width = maxx - minx;
            final int height = maxy - miny;

            // Determine the new coordinates.
            final int newx = e.getX() / scale - width / 2;
            final int newy = e.getY() / scale - height / 2;

            // Move the upper left corner to (newx, newy).
            for(BaseInstance i : group) {
               final int xoffset = i.getX() - minx;
               final int px = newx + xoffset;
               final int yoffset = i.getY() - miny;
               final int py = newy + yoffset;
               i.move(px, py);
            }

            repaint();

         }

      }

      public void mouseMoved(MouseEvent e) {
         final int x = e.getX();
         final int y = e.getY();
         if(mode == Project.MODE_WIRE) {
            drawWire(x, y);
         } else {
            BaseInstance p = getPart(x, y);
            if(p != null) {
               setSelection(p);
            }
         }
      }

   }

   public void updateSelection() {
      setMode(mode);
   }

   public void setMode(int m) {
      endWire();
      if(mode != m) {
         rotation = 0;
      }
      mode = m;
      switch(mode) {
      case Project.MODE_INSERT:
      {
         Part part = project.getSelectedPart();
         if(part != null) {
            setCursor(part.getCursor(rotation, scale));
         }
         break;
      }
      default:
         setCursor(null);
         break;
      }
   }

   private void rotate() {
      rotation = (rotation + 1) % 4;
      setMode(mode);
   }

   private void addWirePoint(int x, int y) {
      x /= scale;
      y /= scale;
      if(wire_x1 == -1) {
         wire_x1 = x;
         wire_y1 = y;
      } else if(wire_x2 == -1) {
         wire_x2 = x;
         wire_y2 = y;
      } else {
         if(wire_x1 != wire_x2 || wire_y1 != wire_y2) {
            BaseInstance part = new WireInstance(wire_x1, wire_y1,
                                                 wire_x2, wire_y2);
            insertPart(part, false);
         }
         wire_x1 = wire_x2;
         wire_y1 = wire_y2;
         wire_x2 = x;
         wire_y2 = y;
      }
      repaint();
   }

   private void endWire() {
      wire_x1 = -1;
      wire_x2 = -1;
      project.getGlassPane().setVisible(false);
      repaint();
   }

   private void drawWire(int x, int y) {
      x /= scale;
      y /= scale;
      if(wire_x1 != -1) {
         Component glassPane = project.getGlassPane();
         glassPane.setVisible(true);
         Point screenPoint = glassPane.getLocationOnScreen();
         Point localPoint = getLocationOnScreen();
         final int xoffset = localPoint.x - screenPoint.x;
         final int yoffset = localPoint.y - screenPoint.y;
         final int x1 = wire_x1 * scale + xoffset;
         final int y1 = wire_y1 * scale + yoffset;
         final int x2 = x * scale + xoffset;
         final int y2 = y * scale + yoffset;
         Graphics g = glassPane.getGraphics();
         g.setXORMode(Color.WHITE);

         // Turn off the old line.
         if(wire_x2 != -1) {
            final int oldx2 = wire_x2 * scale + xoffset;
            final int oldy2 = wire_y2 * scale + yoffset;
            g.drawLine(x1, y1, oldx2, oldy2);
         }

         // Turn on the new line.
         g.drawLine(x1, y1, x2, y2);

         g.setXORMode(Color.BLACK);

         wire_x2 = x;
         wire_y2 = y;
      }
   }

   private BaseInstance getPart(int x, int y) {

      x /= scale;
      y /= scale;
      for(BaseInstance i : parts) {
         if(i.contains(x, y, scale)) {
            return i;
         }
      }
      return null;

   }

   public void setSelection(BaseInstance p) {

      // Just return if there's nothing to do.
      if(p == selection) {
         return;
      }

      // Clear the old selection.
      if(selection != null) {

         Graphics g = getGraphics();

         g.setColor(Color.RED);
         g.setXORMode(Color.WHITE);
         selection.drawHandles(g, scale);
         g.setPaintMode();

         if(group.contains(selection)) {
            g.setColor(Color.BLUE);
         } else {
            g.setColor(Color.BLACK);
         }
         selection.draw(g, scale);

      }

      // Set the new selection.
      selection = p;

      // Draw the new selection.
      if(selection != null) {

         Graphics g = getGraphics();
         g.setColor(Color.RED);
         selection.draw(g, scale);

         g.setXORMode(Color.WHITE);
         selection.drawHandles(g, scale);
         g.setPaintMode();

      }

   }

   public void deletePart(BaseInstance p, boolean isBuffer) {
      parts.remove(p);
      if(!isBuffer) {
         project.addUndoAction(new DeletePartAction(this, p));
      }
      repaint();
   }

   public void insertPart(BaseInstance p, boolean isBuffer) {
      parts.add(p);
      if(!isBuffer) {
         project.addUndoAction(new PlacePartAction(this, p));
      }
      expand(p);
      repaint();
   }

   public void movePart(BaseInstance p, int x, int y, boolean isBuffer) {
      p.move(x, y);
      if(!isBuffer) {
         project.addUndoAction(new MovePartAction(this, p, oldx, oldy));
      }
      oldx = -1;
      expand(p);
      repaint();
   }

   /** Expand the panel if necessary to fit part completely. */
   private void expand(BaseInstance part) {

      final int padding = 64;      // Pad at least this many pixels.
      final int x = part.getX() * scale;
      final int y = part.getY() * scale;
      final int width = part.getWidth() * scale;
      final int height = part.getHeight() * scale;
      Dimension psize = getPreferredSize();
      int deltax = 0;
      int deltay = 0;

      // Check if we need to expand to the left.
      final int minx = x - padding;
      if(minx < 0) {

         // Expand. We do this by moving all parts to the right.
         deltax += -minx;
         final int delta = deltax / scale;
         for(BaseInstance i : parts) {
            i.move(i.getX() + delta, i.getY());
         }

         // TODO Fix up the undo/redo buffer.

      }

      // Check if we need to expand to the right.
      final int maxx = x + padding;
      if(maxx > psize.width) {

         // Expand. We do this by making the panel larger.
         deltax += maxx - psize.width;

      }

      // Check if we need to expand up.
      final int miny = y - padding;
      if(miny < 0) {

         // Expand. We do this by moving all parts down.
         deltay += -miny;
         final int delta = deltay / scale;
         for(BaseInstance i : parts) {
            i.move(i.getX(), i.getY() + delta);
         }

         // TODO Fix up the undo/redo buffer.

      }

      // Check if we need to expand down.
      final int maxy = y + padding;
      if(maxy > psize.height) {

         // Expand. We do this by making the panel larger.
         deltay += maxy - psize.height;

      }

      if(deltax != 0 || deltay != 0) {
         psize.width += deltax;
         psize.height += deltay;
         setPreferredSize(psize);
         revalidate();
      }

   }

   private void showMenu(int x, int y) {

      setSelection(getPart(x, y));
      if(selection == null) {
         return;
      }

      // Repaint to show the selection.
      repaint();

      JPopupMenu menu = new JPopupMenu();

      JMenuItem deleteItem = new JMenuItem("Delete");
      menu.add(deleteItem);
      deleteItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            deletePart(selection, false);
         }
      });

      JMenuItem cloneItem = new JMenuItem("Clone");
      menu.add(cloneItem);
      cloneItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
         }
      });

      selection.updateMenu(this, menu);

      menu.add(new JSeparator());

      JMenuItem cancelItem = new JMenuItem("Cancel");
      menu.add(cancelItem);

      menu.show(this, x, y);
      repaint();

   }

   public void clear() {
      parts.clear();
      group.clear();
      selection = null;
   }

   public void optimizeSize() {
      int width   = getWidth();
      int height  = getHeight();
      for(BaseInstance i : parts) {
         final int maxx = (i.getX() + i.getWidth()) * scale;
         final int maxy = (i.getY() + i.getHeight()) * scale;
         if(maxx > width) {
            width = maxx;
         }
         if(maxy > height) {
            height = maxy;
         }
      }
      setPreferredSize(new Dimension(width, height));
      revalidate();
   }

   public RenderedImage getImage() {

      Dimension dim = getPreferredSize();
      BufferedImage image = new BufferedImage(dim.width, dim.height,
                                              BufferedImage.TYPE_INT_RGB);
      Graphics g = image.getGraphics();

      // Clear the background.
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, dim.width, dim.height);

      // Draw parts.
      for(BaseInstance inst : parts) {
         g.setColor(Color.BLACK);
         inst.draw(g, scale);
      }

      return image;

   }

   public void open(InputStream stream) throws Exception {

      // Clean up the old.
      clear();

      // Parse the document.
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(stream);
      XMLElement root = new XMLElement(doc);
      XMLElement[] children = root.getChildren();
      for(XMLElement e : children) {
         if(e.getName().equals("Wire")) {
            parts.add(new WireInstance(e));
         } else if(e.getName().equals("Part")) {
            Part p = project.getPart(e.getAttributeValue("type"));
            if(p != null) {
               parts.add(new PartInstance(p, e));
            } else {
               System.out.println("unknown part");
            }
         } else {
            System.out.println("unknown tag: " + e.getName());
         }
      }

      // Draw the schematic.
      optimizeSize();
      repaint();

   }

   public void save(OutputStream stream) throws Exception {


      // Create the document.
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();
      XMLElement root = new XMLElement(doc, "Schematic");

      // Save all parts.
      for(BaseInstance i : parts) {
         i.save(root);
      }

      // Write the document.
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(stream);
      transformer.transform(source, result);

   }

}

