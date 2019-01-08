package networking;

import main.Capteur;

import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static String databaseName = "jdbc:mysql://localhost:3306/capteurs_database";
    private static String user = "root";
    private static String pass = "";

    public static void addCapteur(Capteur capteur){
        try {
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                stmt.execute(createQueryCapteur(capteur));
            }finally{
                stmt.close();
                con.close();
            }
        }catch (SQLException ex){
            treatException(ex);
        }
    }

    public static void addValeur(float valeur, String nomCapteur){
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                stmt.execute("INSERT INTO `Valeur` (`ValeurPrise`, `CapteurCorr`) VALUE ('"+valeur+"', '"+nomCapteur+"');");
                stmt.execute("UPDATE `Capteur` SET `ValeurCapteur` = '"+valeur+"' WHERE `Capteur`.`NomCapteur` = '"+nomCapteur+"';");
            }finally{
                stmt.close();
                con.close();
            }
        }catch(SQLException ex){
            treatException(ex);
        }
    }

    public static NavigableSet<String> getNomsCapteurs(String type){
        NavigableSet<String> ret = new TreeSet<>();
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                ResultSet rs = stmt.executeQuery("SELECT `NomCapteur` FROM `Capteur` WHERE `TypeCapteur` = '"+type+"';");

                while (rs.next()){
                    String currentNom = rs.getString("NomCapteur");
                    ret.add(currentNom);
                }
            }finally{
                stmt.close();
                con.close();
            }
        }catch(SQLException ex){
            treatException(ex);
        }
        return ret;
    }

    public static NavigableMap<String, Float> getValeursCapteur(String nomCapteurs, String dateMin, String dateMax){
        NavigableMap<String, Float> ret = new TreeMap<>();
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                String query = "SELECT `ValeurPrise`, `DateValeur` FROM `Valeur` WHERE `CapteurCorr` = '"+nomCapteurs+"';";
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()){
                    Float currentValeur = rs.getFloat("ValeurPrise");
                    String currentDate = getDate(rs.getString("DateValeur"));
                    if (currentDate.compareTo(dateMin) >= 0 && currentDate.compareTo(dateMax) <= 0)
                        ret.put(currentDate, currentValeur);
                }
            }finally {
                stmt.close();
                con.close();
            }
        }catch(SQLException ex){
            treatException(ex);
        }
        return ret;
    }

    /**
     * @return - return the list of all the times recorded (without doubles)
     */
    public static List<String> getTimes(String type){//TODO: changes type to String...capteurs and select only chosen capters' times.
        NavigableSet<String> temp = new TreeSet<>();
        NavigableSet<String> typedCapteurs = getNomsCapteurs(type);
        if (typedCapteurs.isEmpty())    return new ArrayList<>();
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                String query = "SELECT `DateValeur` FROM `Valeur` WHERE `CapteurCorr` = '"+typedCapteurs.first()+"'";
                typedCapteurs.remove(typedCapteurs.first());
                while (!typedCapteurs.isEmpty()){
                    query += " || `CapteurCorr` = '"+typedCapteurs.first()+"'";
                    typedCapteurs.remove(typedCapteurs.first());
                }
                query+=";";
                ResultSet rs = stmt.executeQuery(query);
                while (rs.next()){
                    temp.add(getDate(rs.getString("DateValeur")));
                }
            }finally{
                stmt.close();
                con.close();
            }
        }catch (SQLException ex){
            treatException(ex);
        }
        return new ArrayList<>(temp);
    }

    public static void setSeuils(String nomCapteur, float seuilMin, float seuilMax){
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                stmt.execute("UPDATE `Capteur` SET `SeuilMinCapteur` = '"+seuilMin+"', `SeuilMaxCapteur` = '"+seuilMax+"' WHERE `Capteur`.`NomCapteur` = '"+nomCapteur+"'");
            }finally{
                stmt.close();
                con.close();
            }
        }catch (SQLException ex){
            treatException(ex);
        }
    }

    private static String getDate(String date){
        String[] datetime = date.split(" ");
        String time = datetime[1];
        int lastSec = Integer.valueOf(time.substring(7, 8));
        if (lastSec > 5)   lastSec = 5;
        else lastSec = 0;
        String ret = time.substring(0, 7) + lastSec;
        return ret;
    }

    private static String createQueryCapteur(Capteur capteur){
        String ret = "INSERT INTO `Capteur` (`NomCapteur`, `BatimentCapteur`, `EtageCapteur`, `LocalisationCapteur`, `SeuilMinCapteur`, `SeuilMaxCapteur`, `ValeurCapteur`, `TypeCapteur`) VALUES (";
        ret += "'"+capteur.getNom()+"', ";
        ret += "'"+capteur.getBatiment()+"', ";
        ret += "'"+capteur.getEtage()+"', ";
        ret += "'"+capteur.getLocalisation()+"', ";
        ret += "'"+capteur.getSeuilMin()+"', ";
        ret += "'"+capteur.getSeuilMax()+"', ";
        ret += "NULL, ";
        ret += "'"+capteur.getType()+"');";
        return ret;
    }

    private static void treatException(SQLException ex){
        System.out.println("SQLException: " + ex.getMessage());
        System.out.println("SQLState: " + ex.getSQLState());
        System.out.println("VendorError: " + ex.getErrorCode());
    }
}
