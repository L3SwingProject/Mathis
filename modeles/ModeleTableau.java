package modeles;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import main.Capteur;

public class ModeleTableau extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private List<Capteur> capteurs;

    public ModeleTableau(List<Capteur> capteur) {
        this.capteurs=capteur;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public int getRowCount() {
        return capteurs.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Capteur capteur = capteurs.get(rowIndex);
        switch(columnIndex)
        {
            case 0:return capteur.getNom();
            case 1:return capteur.getType();
            case 2:return capteur.getLocalisation();
            case 3:return capteur.getValeurCourante();
            default:return null;
        }
    }

    public Class<?> getColumnClass(int columnIndex){
        return getValueAt(0,columnIndex).getClass();
    }

    public String getColumnName(int columnIndex) {
        switch(columnIndex)
        {
            case 0: return "Nom capteur";
            case 1: return "Type";
            case 2: return "Localisation";
            case 3: return "Valeur";
            default: return null;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

	/*public void setValueAt(Object newVal, int rowIndex, int columnIndex) {
		Capteur capteur = capteurs.get(rowIndex);
		if(isCellEditable(rowIndex, columnIndex)) {

		}
		fireTableDataChanged();
	}*/

}