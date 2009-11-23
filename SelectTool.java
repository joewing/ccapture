
class SelectTool extends Tool {

   public SelectTool(Project p) {
      super(p, "Select", null);
   }

   public int getMode() {
      return Project.MODE_SELECT;
   }

}

