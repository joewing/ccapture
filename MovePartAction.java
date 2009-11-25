
class MovePartAction implements UserAction {

   public MovePartAction(Schematic s, BaseInstance p,
                         int ox, int oy) {
      schematic = s;
      part = p;
      oldx = ox;
      oldy = oy;
      newx = p.getX();
      newy = p.getY();
   }

   public void undo() {
      schematic.movePart(part, oldx, oldy, true);
   }

   public void redo() {
      schematic.movePart(part, newx, newy, true);
   }

   public void shift(int deltax, int deltay) {
      oldx += deltax;
      oldy += deltay;
      newx += deltax;
      newy += deltay;
   }

   private Schematic schematic;
   private BaseInstance part;
   private int oldx;
   private int oldy;
   private int newx;
   private int newy;

}

