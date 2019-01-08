package main;

import networking.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InterfaceSwing extends Thread {
    private static NavigableSet<Capteur> list = new TreeSet<>();
    /* Composants graphiques */
    /* Partie tableau temps reel */

    /* Partie arbre gestion */

    /* Partie courbes */
    private static JFrame frameCourbe = new JFrame(); //TODO : delete
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
        buildCourbesGestionPanel();
        new Thread(() -> {
            frameCourbe.setVisible(true);
        }).start();
        Serveur.listenSimul(list);
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
        frameCourbe.add(courbesGestionPanel);   //TODO : delete
        frameCourbe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        courbesGestionPanel.setDividerSize(0);

        /* actions */
        typeCourbes.addActionListener( al -> initCapteurBoxContent(typeCourbes.getSelectedIndex() != 0) );
        capteursCourbe1.addActionListener(
                al -> {
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
                    if (capteursCourbe2.getSelectedIndex() != 0){
                        capteursCourbe3.setEnabled(true);
                    }else{
                        capteursCourbe3.setEnabled(false);
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
        frameCourbe.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        Serveur.exit();
                        e.getWindow().dispose();
                    }
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
            updateDates();
        }
    }

    private static void updateDates(){
        //TODO: get only selected captor's times.
        List<String> times;
        Lock l = new ReentrantLock();
        l.lock();
        try{
            times = DatabaseManager.getTimes(typeCourbes.getSelectedItem().toString());
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
