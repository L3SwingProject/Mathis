package main;

import networking.*;

import java.util.NavigableSet;
import java.util.TreeSet;

public class InterfaceSwing extends Thread {
    private static NavigableSet<Capteur> list = new TreeSet<>();

    public static void main(String[] args){
//        new Thread(() -> {
//            while (true){
//                try {
//                    sleep(5000);
//                }catch(Exception e){
//                    e.printStackTrace();
//                }
//                if (!list.isEmpty())
//                    System.out.println(list);
//            }
//        }).start();
        Serveur.listenSimul(list);
    }
}
