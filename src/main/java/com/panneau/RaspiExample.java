package com.panneau;

public class RaspiExample {

    public static void main(String[] args) {

        Panneau panel=new Panneau(65100, 65100,false);
        int i=Integer.parseInt(args[0]);
        System.out.println("Tentative d'afficher "+i);
        panel.printScore(i);
        panel.addListener(System.out::println);
        System.out.println("Couleur: "+panel.getTeamColor());
        try {
            while (true) {
                Thread.sleep(10000);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
