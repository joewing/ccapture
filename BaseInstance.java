
import java.awt.*;
import java.io.*;
import javax.swing.*;

abstract class BaseInstance {

   public abstract BaseInstance clone();

   public abstract int getX();

   public abstract int getY();

   public abstract int getWidth();

   public abstract int getHeight();

   public abstract void move(int x, int y);

   public abstract void draw(Graphics g, int scale, int offsetx, int offsety);

   public abstract void drawHandles(Graphics g, int scale);

   public abstract boolean contains(int x, int y, int scale);

   public abstract int snapToTerminal(Point p);

   public abstract void updateMenu(Schematic schematic,
                                   JPopupMenu menu,
                                   int x,
                                   int y);

   public abstract void save(XMLElement root);

}

