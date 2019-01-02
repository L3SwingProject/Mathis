package networking;

import main.Capteur;

import java.sql.*;

public class DatabaseManager {
    private static String databaseName = "jdbc:mysql://localhost:3306/capteurs_database";
    private static String user = "root";
    private static String pass = "";

    public static void addCapteur(Capteur capteur){
        try {
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                stmt.execute(DatabaseManager.createQueryCapteur(capteur));
            }finally{
                stmt.close();
            }
        }catch (SQLException ex){
            DatabaseManager.treatException(ex);
        }
    }

    public static void addValeur(float valeur, String nomCapteur){
        try{
            Connection con = DriverManager.getConnection(databaseName, user, pass);
            Statement stmt = con.createStatement();
            try{
                stmt.execute("INSERT INTO `Valeur` (`ValeurPrise`, `CapteurCorr`) VALUE ('"+valeur+"', '"+nomCapteur+"');");
                stmt.execute("UPDATE `Capteur` SET `ValeurCapteur` = '"+valeur+"' WHERE `Capteur`.`NomCapteur` = '"+nomCapteur+"'");
            }finally{
                stmt.close();
            }
        }catch(SQLException ex){
            DatabaseManager.treatException(ex);
        }
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
