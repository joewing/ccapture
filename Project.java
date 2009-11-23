
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

class Project {

   public static final int MODE_SELECT = 0;
   public static final int MODE_INSERT = 1;
   public static final int MODE_WIRE   = 2;
   public static final int MODE_GROUP  = 3;

   public Project() {

      frame = new JFrame("Capture");
      frame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            exit();
         }
      });
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

      createMenuBar();

      Container con = frame.getContentPane();
      JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      con.add(split);

      toolList = new ToolList(this);
      split.setLeftComponent(toolList);

      schematic = new Schematic(this);
      JScrollPane right = new JScrollPane(schematic);
      right.setPreferredSize(new Dimension(640, 480));
      split.setRightComponent(right);

      frame.pack();
      frame.setVisible(true);

      currentPath = new File(System.getProperty("user.dir"));

   }

   public Part getSelectedPart() {
      return toolList.getSelectedPart();
   }

   public Part getPart(String name) {
      return toolList.getPart(name);
   }

   public void updateSelection() {
      if(mode != MODE_INSERT) {
         setMode(MODE_INSERT);
      } else {
         schematic.updateSelection();
      }
   }

   public void setMode(int m) {
      mode = m;
      schematic.setMode(m);
   }

   public int getMode() {
      return mode;
   }

   public Component getGlassPane() {
      return frame.getGlassPane();
   }

   private boolean askToSave() {

      if(!changed) {
         return true;
      }

      final int rc = JOptionPane.showConfirmDialog(frame, "Save changes?");
      switch(rc) {
      case JOptionPane.YES_OPTION:
         saveFile(false);
         return true;
      case JOptionPane.NO_OPTION:
         return true;
      default: // Cancel
         return false;
      }

   }

   public void openFile() {

      if(!askToSave()) {
         return;
      }

      try {
         JFileChooser chooser = new JFileChooser(currentPath);
         switch(chooser.showOpenDialog(frame)) {
         case JFileChooser.APPROVE_OPTION:
            clear();
            currentPath = chooser.getCurrentDirectory();
            currentFile = chooser.getSelectedFile();
            FileInputStream stream = new FileInputStream(currentFile);
            schematic.open(stream);
            stream.close();
            break;
         default:
            break;
         }
      } catch(Exception ex) {
         System.out.println(ex.toString());
      }

   }

   public void saveFile(boolean displayDialog) {

      // Determine which file to use.
      File selectedFile = null;
      if(displayDialog || currentFile == null) {
         try {
            JFileChooser chooser = new JFileChooser(currentPath);
            switch(chooser.showSaveDialog(frame)) {
            case JFileChooser.APPROVE_OPTION:
               currentPath = chooser.getCurrentDirectory();
               selectedFile = chooser.getSelectedFile();
               break;
            default:
               return;
            }
         } catch(Exception ex) {
            System.out.println(ex.toString());
         }
      } else {
         selectedFile = currentFile;
      }

      // Make sure the user knows if we are over-writing a file.
      if(selectedFile.exists() && !selectedFile.equals(currentFile)) {

         // File exists. Make sure we are willing to over-write it.
         final int rc = JOptionPane.showConfirmDialog(frame,
            "File exists. Overwrite?");
         switch(rc) {
         case JOptionPane.YES_OPTION:  // Go ahead with this file.
            break;
         case JOptionPane.NO_OPTION:   // Try a different file.
            saveFile(true);
            return;
         default:                      // Cancel
            return;
         }

      }

      // Save the file.
      try {
         OutputStream stream = new FileOutputStream(selectedFile);
         schematic.save(stream);
         stream.close();
         changed = false;
      } catch(Exception ex) {
         System.out.println(ex.toString());
      }

   }

   public boolean exit() {
      if(!askToSave()) {
         return false;
      }
      System.exit(0);
      return true;
   }

   private void createMenuBar() {

      JMenuBar bar = new JMenuBar();
      frame.setJMenuBar(bar);

      // File menu.
      JMenu fileMenu = new JMenu("File");
      bar.add(fileMenu);

      JMenuItem openItem = new JMenuItem("Open");
      fileMenu.add(openItem);
      openItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            openFile();
         }
      });

      JMenuItem saveItem = new JMenuItem("Save");
      fileMenu.add(saveItem);
      saveItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            saveFile(false);
         }
      });

      JMenuItem saveAsItem = new JMenuItem("Save As");
      fileMenu.add(saveAsItem);
      saveAsItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            saveFile(true);
         }
      });

      fileMenu.add(new JSeparator());

      JMenuItem exitItem = new JMenuItem("Exit");
      fileMenu.add(exitItem);
      exitItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            exit();
         }
      });

      // Edit menu.
      JMenu editMenu = new JMenu("Edit");
      bar.add(editMenu);

      JMenuItem undoItem = new JMenuItem("Undo");
      editMenu.add(undoItem);
      undoItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            undo();
         }
      });

      JMenuItem redoItem = new JMenuItem("Redo");
      editMenu.add(redoItem);
      redoItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            redo();
         }
      });

      editMenu.add(new JSeparator());

      JMenuItem sizeItem = new JMenuItem("Resize");
      editMenu.add(sizeItem);
      sizeItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            schematic.optimizeSize();
         }
      });

      // Help menu.
      JMenu helpMenu = new JMenu("Help");
      bar.add(helpMenu);

      JMenuItem helpItem = new JMenuItem("Help");
      helpMenu.add(helpItem);

      helpMenu.add(new JSeparator());

      JMenuItem aboutItem = new JMenuItem("About");
      helpMenu.add(aboutItem);
      aboutItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
         }
      });

   }

   public void addUndoAction(UserAction action) {
      undoBuffer.add(action);
      changed = true;
   }

   public void addRedoAction(UserAction action) {
      redoBuffer.add(action);
      changed = true;
   }

   public void shiftBuffers(int deltax, int deltay) {
      for(UserAction a : undoBuffer) {
         a.shift(deltax, deltay);
      }
      for(UserAction a : redoBuffer) {
         a.shift(deltax, deltay);
      }
   }

   public void undo() {
      if(!undoBuffer.isEmpty()) {
         UserAction action = undoBuffer.removeLast();
         action.undo();
         redoBuffer.add(action);
      }
   }

   public void redo() {
      if(!redoBuffer.isEmpty()) {
         UserAction action = redoBuffer.removeLast();
         action.redo();
         undoBuffer.add(action);
      }
   }

   public void clear() {
      undoBuffer.clear();
      redoBuffer.clear();
      setMode(MODE_SELECT);
      changed = false;
      schematic.clear();
      schematic.repaint();
   }

   private JFrame frame;
   private ToolList toolList;
   private Schematic schematic;
   private int mode = MODE_SELECT;
   private boolean changed = false;;
   private File currentPath = null;
   private File currentFile = null;
   private LinkedList<UserAction> undoBuffer = new LinkedList<UserAction>();
   private LinkedList<UserAction> redoBuffer = new LinkedList<UserAction>();

}

