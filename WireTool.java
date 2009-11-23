
class WireTool extends Tool {

   public WireTool(Project p) {
      super(p, "Wire", null);
   }

   public int getMode() {
      return Project.MODE_WIRE;
   }

}

