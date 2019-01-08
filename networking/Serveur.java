package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import exceptions.BadFormatMessageException;
import main.*;
import modeles.ModeleArbre;

public class Serveur extends Thread {
    private static NavigableSet<Capteur> list;
    private static NavigableMap<String, Capteur> keyList = new TreeMap<>();
    private int badFormat = 0;
    final static int port = 8952;
    private static boolean isRunning = true;
    private static List<Serveur> serveurs = new ArrayList<>();
    private Socket socket;

    /**
     * Fonction main du thread qui lance le serveur.
     * @param list - list of captors
     */
    public static void listenSimul(NavigableSet<Capteur> list){
        Serveur.list = list;
        for (Capteur capteur : Serveur.list){
            keyList.put(capteur.getNom(), capteur);
        }
        try{
            ServerSocket socketServeur = new ServerSocket(port);
            while(isRunning){
                Socket socketClient = socketServeur.accept();
                serveurs.add(new Serveur(socketClient));
                serveurs.get(serveurs.size()-1).start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private Serveur(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        traitements();
    }

    private void traitements(){
        try {
            String message;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            badFormat = 0;
            while (!socket.isClosed() && isRunning) {
                message = in.readLine();
                if (message != null) {
                    String[] infos = message.split(" ");
                    System.out.println(message);
                    switch (infos[0]) {
                        case "Deconnexion":
                            socket.close();
                            Capteur toDel = keyList.get(infos[1]);
                            toDel.deconnexion();
                            return;
                        case "Connexion":
                            badFormat = 0;
                            Capteur toAdd;
                            try{
                                toAdd = new Capteur(infos[1], infos[2]);
                            }catch(BadFormatMessageException e){
                                e.printStackTrace();
                                System.out.println("message invalide a la connexion, fermeture de la socket.");
                                socket.close();
                                return;
                            }
                            Lock lc = new ReentrantLock();
                            lc.lock();       //lock mutex
                            try{
                                list.add(toAdd);
                                keyList.put(infos[1], toAdd);
                                DatabaseManager.addCapteur(toAdd);
                                InterfaceSwing.setModeleArbre(new ModeleArbre(list));
                            }finally{
                                lc.unlock(); //unlock mutex
                            }
                            break;
                        case "Donnee":
                            badFormat = 0;
                            //TODO: check if it exceeds bounds
                            float newValue = Float.parseFloat(infos[2]);
                            Capteur toSet = keyList.get(infos[1]);
                            toSet.update(newValue);
                            Lock ld = new ReentrantLock();
                            ld.lock();       //lock mutex
                            try{
                                DatabaseManager.addValeur(newValue, infos[1]);
                            }finally{
                                ld.unlock(); //unlock mutex
                            }
                            break;
                        default:
                            badFormat++;
                            if (badFormat == 3)
                                socket.close();
                    }
                }else{
                    socket.close();
                }
            }
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void exit(){
        isRunning = false;
        for (Serveur serveur : serveurs){
            serveur.interrupt();
        }
    }
}