package com.panneau;

public class RaspiExample {

    public static void main(String[] args) {
        System.out.println("Current dir: "+ System.getProperty("user.dir"));
        Panneau panel=new Panneau(65100, 65100,false);
        int i=Integer.parseInt(args[0]);
        System.out.println("Tentative d'afficher "+i);
        panel.printScore(i);
        panel.addListener(System.out::println);
        System.out.println("Couleur: "+panel.getTeamColor());
        try {
            while (true) {
                panel.setLeds(Panneau.LedColor.NOIR);
                Thread.sleep(1000);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
