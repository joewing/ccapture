
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

abstract class Tool {

   private final class ToolActionListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         project.setMode(getMode());
      }
   }

   public Tool(Project p, String name, Icon icon) {
      project = p;
      if(icon != null) {
         button = new JButton(icon);
      } else {
         button = new JButton(name);
      }
      button.addActionListener(new ToolActionListener());
   }

   public JButton getButton() {
      return button;
   }

   public abstract int getMode();

   protected JButton button;
   protected Project project;

}

