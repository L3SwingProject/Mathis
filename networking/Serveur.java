package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import exceptions.BadFormatMessageException;
import main.*;

public class Serveur extends Thread {
    private static NavigableSet<Capteur> list;
    private static NavigableMap<String, Capteur> keyList = new TreeMap<>();
    private int badFormat = 0;
    //TODO: database
    final static int port = 8952;
    private Socket socket;

    /**
     * Fonction main du thread qui lance le serveur.
     * @param list - list of captors
     */
    public static void listenSimul(NavigableSet<Capteur> list){
        Serveur.list = list;
        try{
            ServerSocket socketServeur = new ServerSocket(port);
            while(true){
                Socket socketClient = socketServeur.accept();
                Serveur s = new Serveur(socketClient);
                s.start();
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
            while (!socket.isClosed()) {
                message = in.readLine();
                if (!message.isEmpty()) {
                    String[] infos = message.split(" ");
                    System.out.println(message);
                    switch (infos[0]) {
                        case "Deconnexion":
                            socket.close();
                            Capteur toDel = keyList.get(infos[1]);
                            toDel.deconnexion();
                            break;
                        case "Connexion":
                            badFormat = 0;
                            Capteur toAdd = new Capteur(infos[1], infos[2]);
                            Lock lc = new ReentrantLock();
                            lc.lock();       //lock mutex
                            try{
                                list.add(toAdd);
                                keyList.put(infos[1], toAdd);
                                DatabaseManager.addCapteur(toAdd);
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
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}