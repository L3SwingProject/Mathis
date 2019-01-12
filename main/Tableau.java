package main;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import exceptions.BadFormatMessageException;
import modeles.ModeleTableau;



public class Tableau extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private JScrollPane tableau;
    private TableColumnModel modeleColonne;
    private JTableHeader header;
    private Set<Capteur> treeSet;
    private JComboBox comboType;
    private JComboBox comboBat;
    ModeleTableau tableModel;

    public Tableau(Set<Capteur> capteur) {
        super("tableau");
        treeSet = new TreeSet<>(capteur);
        List<Capteur> liste = new ArrayList<>();
        for(Capteur c : capteur) {
            if (c.estConnect())
                liste.add(c);
        }


        tableModel = new ModeleTableau(liste);
        table = new JTable(tableModel);
        modeleColonne = table.getColumnModel();
        for(int i=0;i<4;i++) {
            modeleColonne.getColumn(i).setCellRenderer(new CellRenderer());
        }


        header = new JTableHeader(modeleColonne);
        table.setTableHeader(header);
        Set<String> itemsTset = new TreeSet<>();
        Set<String> itemsBset = new TreeSet<>();
        for(Capteur c : liste) {
            itemsTset.add(c.getType().toString());
            itemsBset.add(c.getBatiment());
        }

        String[] itemsT = new String[itemsTset.size()];
        String[] itemsB = new String[itemsBset.size()];
        itemsT[0]="Type";
        itemsB[0]="Localisation";

        int i = 1;
        for(String item : itemsTset) {
            itemsT[i]=item;
        }
        i = 1;
        for(String item : itemsBset) {
            itemsB[i]=item;
        }
        comboType = new JComboBox<String>(new DefaultComboBoxModel<String>(itemsT));
        comboBat = new JComboBox<String>(new DefaultComboBoxModel<String>(itemsB));


        TableColumn col;
        col = (TableColumn)table.getColumnModel().getColumn(1);
        col.setHeaderValue("Type");
        col.setHeaderRenderer(new ComboRenderer(itemsT));

        tableau = new JScrollPane(table);
        add(tableau);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    class CellRenderer extends DefaultTableCellRenderer {
        private static final long serialVersionUID = 1L;
        private final Color rouge = new Color(247, 62, 34);

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Capteur c = ((ModeleTableau)table.getModel()).getCapteur(row);

            System.out.println(c.getNom());
            setText(String.valueOf(value));
            setHorizontalAlignment(JLabel.LEFT);
            if (c.valeurExtSeuil()) {
                this.setBackground(rouge);
            }
            else {
                this.setBackground(Color.WHITE);
            }
            return this;
        }
    }

    class ComboRenderer extends JComboBox<String> implements TableCellRenderer {

        private static final long serialVersionUID = 1L;

        ComboRenderer(String[] itemsT) {
            for (int i = 0; i < itemsT.length; i++) {
                addItem(itemsT[i]);
            }
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            setSelectedItem(value);
            return this;
        }
    }

    public static void main(String[] args) {
        Set<Capteur> liste= new TreeSet<>();
        Capteur c1, c2, c3, c4, c5;
        try{
            c1 = new Capteur("1","ELECTRICITE:U3:2:devantSalle3");
            c2=new Capteur("2","EAU:U4:1:devantSalle6");
            c3 = new Capteur("3","AIRCOMPRIME:U1:1:devantSalle1");
            c4 = new Capteur("4","TEMPERATURE:U2:1:devantSalle6");
            c5 = new Capteur("5","TEMPERATURE:U3:3:devantSalle");
        }catch(BadFormatMessageException e){
            e.printStackTrace();
            return;
        }
        liste.add(c1);
        liste.add(c2);
        Tableau t = new Tableau(liste);
        liste.add(c3);
        liste.add(c4);
        liste.add(c5);
        t.tableModel.fireTableRowsInserted(1,4);
        c1.update(5.0f);
        c2.update(5.0f);
        c3.update(5.0f);
        c4.update(20.0f);
        c5.update(20.0f);
        t.setVisible(true);
    }
}
