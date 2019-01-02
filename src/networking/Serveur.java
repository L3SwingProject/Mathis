package networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import exceptions.BadFormatMessageException;
import main.*;

public class Serveur extends Thread {
    private static NavigableSet<Capteur> list = new TreeSet<>();
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
                            Capteur toAdd;
                            try {
                                toAdd = new Capteur(infos[1], infos[2]);
                            }catch (BadFormatMessageException e) {
                                toAdd = null;
                                badFormat++;
                            }
                            if (toAdd != null) {
                                Lock l = new ReentrantLock();
                                l.lock();   //lock mutex
                                try{
                                    list.add(toAdd);
                                    keyList.put(infos[1], toAdd);
                                }finally{
                                    l.unlock(); //unlock mutex
                                }
                            }
                            break;
                        case "Donnee":
                            badFormat = 0;
                            //TODO: add value to database and update captor + check if it exceeds bounds
                            Capteur toSet = keyList.get(infos[1]);
                            toSet.update(Float.parseFloat(infos[2]));
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