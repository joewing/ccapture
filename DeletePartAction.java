
class DeletePartAction implements UserAction {

   public DeletePartAction(Schematic s, BaseInstance p) {
      schematic = s;
      part = p;
   }

   public void undo() {
      schematic.insertPart(part, true);
   }

   public void redo() {
      schematic.deletePart(part, true);
   }

   public void shift(int deltax, int deltay) {
      part.move(part.getX() + deltax, part.getY() + deltay);
   }

   private Schematic schematic;
   private BaseInstance part;

}

