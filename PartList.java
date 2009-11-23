
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

class PartList extends JPanel {

   private final class PartModel implements ListModel {

      public void addListDataListener(ListDataListener l) {
      }

      public Object getElementAt(int index) {
         return displayItems.get(index);
      }

      public int getSize() {
         return displayItems.size();
      }

      public void removeListDataListener(ListDataListener l) {
      }

   }

   private final class PartListener implements ListSelectionListener {
      public void valueChanged(ListSelectionEvent e) {
         project.updateSelection();
      }
   }

   public PartList(Project project) {

      this.project = project;
      TreeMap<String, Part> parts = Capture.getParts();
      for(String name : parts.keySet()) {
         Part p = parts.get(name);
         items.add(p);
         displayItems.add(p);
      }

      setLayout(new BorderLayout());

      searchField = new JTextField();
      searchField.addCaretListener(new CaretListener() {
         public void caretUpdate(CaretEvent e) {
            updateSearch();
         }
      });
      add(searchField, BorderLayout.NORTH);

      list = new JList(new PartModel());
      JScrollPane scroll = new JScrollPane(list);
      scroll.setHorizontalScrollBarPolicy(
         ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      scroll.setVerticalScrollBarPolicy(
         ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      add(scroll, BorderLayout.CENTER);
      list.addListSelectionListener(new PartListener());

   }

   public Part getSelection() {
      return (Part)list.getSelectedValue();
   }

   public Part getPart(String name) {
      if(name == null) {
         return null;
      }
      for(Part p : items) {
         if(p.getName().equals(name)) {
            return p;
         }
      }
      return null;
   }

   private void updateSearch() {

      displayItems.clear();
      if(searchField.getText().length() == 0) {
         for(Part p : items) {
            displayItems.add(p);
         }
      } else {
         String lower = searchField.getText().toLowerCase();
         for(Part p : items) {
            String temp = p.getName().toLowerCase();
            if(temp.indexOf(lower) >= 0) {
               displayItems.add(p);
            }
         }      
      }
      list.setModel(new PartModel());

   }

   private JTextField searchField;
   private JList list;
   private ArrayList<Part> items = new ArrayList<Part>();
   private ArrayList<Part> displayItems = new ArrayList<Part>();
   private Project project;

}

