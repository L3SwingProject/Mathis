package main;

import modeles.ModeleArbre;
import networking.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InterfaceSwing extends Thread {
    private static NavigableSet<Capteur> list = new TreeSet<>();

    /* Composants graphiques */
    private static JFrame fenetre = new JFrame();

    /* Partie tableau temps reel */

    /* Partie arbre gestion */
    private static JSplitPane treePane;
    private static JTree jModTree = new JTree(new ModeleArbre(list));
    private static JPanel jPanelInfo = new JPanel();
    private static JLabel jLNom = new JLabel();
    private static JLabel jLLoc = new JLabel();
    private static JLabel jLType = new JLabel();
    private static JLabel jLSeuilMax = new JLabel();
    private static JLabel jLSeuilMin = new JLabel();
    private static JLabel jLEspace = new JLabel();
    private static JLabel jLModif = new JLabel();

    /* Partie courbes */
    private static JSplitPane courbesGestionPanel;
    private static ChartPanel courbesPanel;
    private static JFreeChart courbes;
    private static JPanel gestion = new JPanel();
    private static JComboBox<String> typeCourbes = new JComboBox<>();
    private static JComboBox<String> capteursCourbe1 = new JComboBox<>();
    private static JComboBox<String> capteursCourbe2 = new JComboBox<>();
    private static JComboBox<String> capteursCourbe3 = new JComboBox<>();
    private static JSpinner dateDebutCourbes;
    private static JSpinner dateFinCourbes;
    private static JLabel[] labels = new JLabel[6];
    private static JButton submitCourbes = new JButton("Afficher");
    private static NavigableSet<String> capteursTyped;

    /**
     * main function : build, print graphic componant and animate them communicating with somulators/databse.
     * @param args - none
     */
    public static void main(String[] args){
        buildInterface();
        new Thread(() -> fenetre.setVisible(true)).start();
        Serveur.listenSimul(list);
    }

    public static void setModeleArbre(ModeleArbre modele){
        jModTree.setModel(modele);
    }

    private static void buildInterface(){
        buildTreePanel();
        //buildTablePanel();
        buildCourbesGestionPanel();

        fenetre.setLayout(new BorderLayout());
        fenetre.add(treePane, BorderLayout.CENTER);
        fenetre.add(courbesGestionPanel, BorderLayout.SOUTH);
        fenetre.setSize(1000, 800);

        fenetre.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        Serveur.exit();
                        e.getWindow().dispose();
                    }
                }
        );
    }

    private static void buildCourbesGestionPanel(){
        /* Components initialization */
        /* Form part */
        createForm();

        /* Chart part */
        courbesPanel = new ChartPanel(courbes);

        courbesGestionPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        courbesGestionPanel.setLeftComponent(gestion);
        courbesGestionPanel.setRightComponent(courbesPanel);
        fenetre.add(courbesGestionPanel);   //TODO : delete
        courbesGestionPanel.setDividerSize(0);

        /* actions */
        typeCourbes.addActionListener( al -> initCapteurBoxContent(typeCourbes.getSelectedIndex() != 0) );
        capteursCourbe1.addActionListener(
                al -> {
                    if (capteursCourbe1.getItemCount() > 0) updateDates();
                    if (capteursCourbe1.getSelectedIndex() != 0 && capteursCourbe1.getItemCount() > 0){
                        capteursCourbe2.setEnabled(true);
                        submitCourbes.setEnabled(true);
                    }else{
                        capteursCourbe2.setEnabled(false);
                        submitCourbes.setEnabled(false);
                    }
                }
        );
        capteursCourbe2.addActionListener(
                al -> {
                    if (capteursCourbe2.getItemCount() > 0) updateDates();
                    if (capteursCourbe2.getSelectedIndex() != 0 && capteursCourbe2.getItemCount() > 0){
                        capteursCourbe3.setEnabled(true);
                    }else{
                        capteursCourbe3.setEnabled(false);
                    }
                }
        );
        capteursCourbe3.addActionListener(al -> {
            if (capteursCourbe3.getItemCount() > 0) updateDates();
        });
        dateDebutCourbes.addChangeListener(
                al -> {
                    if (dateDebutCourbes.getValue().equals(dateFinCourbes.getValue())){
                        dateDebutCourbes.setValue(dateDebutCourbes.getPreviousValue());
                    }
                }
        );
        dateFinCourbes.addChangeListener(
                al -> {
                    if (dateFinCourbes.getValue().equals(dateDebutCourbes.getValue())){
                        dateFinCourbes.setValue(dateFinCourbes.getNextValue());
                    }
                }
        );
        submitCourbes.addActionListener(
                al -> {
                    courbes = createChart(createCategoryDataset());
                    courbesPanel = new ChartPanel(courbes);
                    courbesGestionPanel.setRightComponent(courbesPanel);
                }
        );
    }

    private static void createForm(){
        typeCourbes.addItem("Selectionner");
        for (TypeFluide type : TypeFluide.values()){
            typeCourbes.addItem(type.toString());
        }

        //init components
        //JComboBoxes
        capteursCourbe1.addItem("Selectionner");
        capteursCourbe2.addItem("Selectionner");
        capteursCourbe3.addItem("Selectionner");


        capteursCourbe1.setEnabled(false);
        capteursCourbe2.setEnabled(false);
        capteursCourbe3.setEnabled(false);
        submitCourbes.setEnabled(false);

        //JSpinners
        dateDebutCourbes = new JSpinner();
        dateFinCourbes = new JSpinner();
        dateDebutCourbes.setEnabled(false);
        dateFinCourbes.setEnabled(false);

        dateFinCourbes.setValue(dateDebutCourbes.getNextValue());

        //labels
        labels[0] = new JLabel("Type : ", JLabel.LEFT);
        labels[1] = new JLabel("Capteur 1 : ", JLabel.LEFT);
        labels[2] = new JLabel("Capteur 2 : ", JLabel.LEFT);
        labels[3] = new JLabel("Capteur 3 : ", JLabel.LEFT);
        labels[4] = new JLabel("Debut : ", JLabel.LEFT);
        labels[5] = new JLabel("Fin : ", JLabel.LEFT);

        /*add components to panel*/
        GroupLayout layout = new GroupLayout(gestion);
        gestion.setLayout(layout);
        GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(labels[0])
                .addComponent(labels[1])
                .addComponent(labels[2])
                .addComponent(labels[3])
                .addComponent(labels[4])
                .addComponent(labels[5])
        );

        hGroup.addGroup(layout.createParallelGroup()
                .addComponent(typeCourbes)
                .addComponent(capteursCourbe1)
                .addComponent(capteursCourbe2)
                .addComponent(capteursCourbe3)
                .addComponent(dateDebutCourbes)
                .addComponent(dateFinCourbes)
                .addComponent(submitCourbes)
        );

        layout.setHorizontalGroup(hGroup);

        GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();
        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[0])
                .addComponent(typeCourbes)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[1])
                .addComponent(capteursCourbe1)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[2])
                .addComponent(capteursCourbe2)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[3])
                .addComponent(capteursCourbe3)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[4])
                .addComponent(dateDebutCourbes)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(labels[5])
                .addComponent(dateFinCourbes)
        );

        vGroup.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(submitCourbes)
        );

        layout.setVerticalGroup(vGroup);
    }

    private static void buildTreePanel(){
        //virer la root
        //jModTree.setRootVisible(false);

        //virer les icone
        DefaultTreeCellRenderer iconeTree = new  DefaultTreeCellRenderer();
        iconeTree.setClosedIcon(null);
        iconeTree.setOpenIcon(null);
        iconeTree.setLeafIcon(null);
        jModTree.setCellRenderer(iconeTree);

        //afficher info
        GroupLayout layout = new GroupLayout(jPanelInfo);
        jPanelInfo.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(jLNom)
                                .addComponent(jLLoc)
                                .addComponent(jLType)
                                .addComponent(jLSeuilMax)
                                .addComponent(jLSeuilMin)
                                .addComponent(jLEspace)
                                .addComponent(jLModif)
                        )
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(jLNom)
                        .addComponent(jLLoc)
                        .addComponent(jLType)
                        .addComponent(jLSeuilMax)
                        .addComponent(jLSeuilMin)
                        .addComponent(jLEspace)
                        .addComponent(jLModif)
        );

        //info qd on clique
        jModTree.addTreeSelectionListener(event -> {
            Object noeud = jModTree.getLastSelectedPathComponent();
            if( noeud != null && noeud.getClass().equals(Capteur.class)){
                Capteur capActu = (Capteur) noeud;
                jLNom.setText("Nom : " + capActu.getNom());
                jLLoc.setText("Localisation : "+ capActu.getLocalisation());
                jLType.setText("Type : "+ capActu.getType());
                jLSeuilMax.setText("Seuil maximum: " + capActu.getSeuilMax());
                jLSeuilMin.setText("Seuil minimum : "+ capActu.getSeuilMin());
                jLEspace.setText(" ");
                jLModif.setText("Modification des seuils :");
            }
        });

        jModTree.getModel().addTreeModelListener(
                new TreeModelListener() {
                    @Override
                    public void treeNodesChanged(TreeModelEvent e) {

                    }

                    @Override
                    public void treeNodesInserted(TreeModelEvent e) {

                    }

                    @Override
                    public void treeNodesRemoved(TreeModelEvent e) {

                    }

                    @Override
                    public void treeStructureChanged(TreeModelEvent e) {
                        System.out.println("YES");
                    }
                }
        );

        treePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(jModTree), jPanelInfo);
        treePane.setDividerLocation(300);
    }

    /**
     * Precondition : at least one captor chosen.
     */
    private static CategoryDataset createCategoryDataset(){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String nomCapteurChoisi1 = capteursCourbe1.getSelectedItem().toString();
        String nomCapteurChoisi2 = capteursCourbe2.getSelectedItem().toString();
        String nomCapteurChoisi3 = capteursCourbe3.getSelectedItem().toString();
        updateDataset(nomCapteurChoisi1, dataset);
        if (!nomCapteurChoisi2.equals("Selectionner"))   updateDataset(nomCapteurChoisi2, dataset);
        if (!nomCapteurChoisi3.equals("Selectionner"))   updateDataset(nomCapteurChoisi3, dataset);
        return dataset;
    }

    private static void updateDataset(String nomCapteur, DefaultCategoryDataset dataset){
        NavigableMap<String, Float> valeursCapteur;
        Lock l = new ReentrantLock();
        l.lock();
        try{
            valeursCapteur = DatabaseManager.getValeursCapteur(nomCapteur, dateDebutCourbes.getValue().toString(), dateFinCourbes.getValue().toString());
        }finally{
            l.unlock();
        }

        for (Map.Entry<String, Float> entry : valeursCapteur.entrySet()){
            String dateCapteurCourant = entry.getKey();
            dataset.addValue(entry.getValue(), nomCapteur, dateCapteurCourant);
        }
    }

    private static JFreeChart createChart(CategoryDataset d){
        JFreeChart chart = ChartFactory.createLineChart("Evolution des capteurs", "Secondes", "Valeur Capteurs", d, PlotOrientation.VERTICAL, true, true, false);
        return chart;
    }

    private static void initCapteurBoxContent(boolean isTypeDefined){
        capteursCourbe1.removeAllItems();
        capteursCourbe1.addItem("Selectionner");
        capteursCourbe2.removeAllItems();
        capteursCourbe2.addItem("Selectionner");
        capteursCourbe3.removeAllItems();
        capteursCourbe3.addItem("Selectionner");
        capteursCourbe1.setEnabled(false);
        capteursCourbe2.setEnabled(false);
        capteursCourbe3.setEnabled(false);
        dateDebutCourbes.setEnabled(false);
        dateFinCourbes.setEnabled(false);
        submitCourbes.setEnabled(false);
        if (isTypeDefined){
            Lock l = new ReentrantLock();
            l.lock();
            try{
                capteursTyped = DatabaseManager.getNomsCapteurs(Objects.requireNonNull(typeCourbes.getSelectedItem()).toString());
            }finally{
                l.unlock();
            }
            for (String capteur : capteursTyped){
                capteursCourbe1.addItem(capteur);
                capteursCourbe2.addItem(capteur);
                capteursCourbe3.addItem(capteur);
            }
            capteursCourbe1.setEnabled(true);
        }
    }

    private static void updateDates(){
        List<String> times;
        List<String> capteurs = new ArrayList<>();
        if (!capteursCourbe1.getSelectedItem().equals("Selectionner"))  capteurs.add(capteursCourbe1.getSelectedItem().toString());
        if (!capteursCourbe2.getSelectedItem().equals("Selectionner"))  capteurs.add(capteursCourbe2.getSelectedItem().toString());
        if (!capteursCourbe3.getSelectedItem().equals("Selectionner"))  capteurs.add(capteursCourbe3.getSelectedItem().toString());
        Lock l = new ReentrantLock();
        l.lock();
        try{
            times = DatabaseManager.getTimes(capteurs);
        }finally{
            l.unlock();
        }
        if (!times.isEmpty()) {
            dateDebutCourbes.setModel(new SpinnerListModel(times));
            dateFinCourbes.setModel(new SpinnerListModel(times));
            dateFinCourbes.setValue(dateFinCourbes.getNextValue());
            dateDebutCourbes.setEnabled(true);
            dateFinCourbes.setEnabled(true);
        }
    }
}
