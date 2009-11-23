
class ComponentTool extends Tool {

   public ComponentTool(Project p) {
      super(p, "New", null);
   }

   public int getMode() {
      return Project.MODE_INSERT;
   }

}

