
class GroupTool extends Tool {

   public GroupTool(Project p) {
      super(p, "Group", null);
   }

   public int getMode() {
      return Project.MODE_GROUP;
   }

}

