
import java.awt.*;
import javax.swing.*;

class ToolList extends JPanel {

   private JPanel toolPanel;
   private PartList partList;

   public ToolList(Project p) {

      setLayout(new BorderLayout());

      toolPanel = new JPanel();
      toolPanel.setLayout(new GridLayout(0, 2));
      add(toolPanel, BorderLayout.NORTH);

      addTool(new SelectTool(p));
      addTool(new GroupTool(p));
      addTool(new WireTool(p));
      addTool(new ComponentTool(p));

      partList = new PartList(p);
      add(partList, BorderLayout.CENTER);

   }

   private void addTool(Tool t) {
      JButton button = t.getButton();
      toolPanel.add(button);
   }

   public Part getSelectedPart() {
      return partList.getSelection();
   }

   public Part getPart(String name) {
      return partList.getPart(name);
   }

}

