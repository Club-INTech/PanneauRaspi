package com.panneau;

import java.io.IOException;

public class RaspiExample {

    public static void main(String[] args) throws IOException {

        Panneau panel=new Panneau(65100, 65100,false);
        int i=Integer.parseInt(args[0]);
        System.out.println("Tentative d'afficher "+i);
        //panel.printScore(i);
        panel.addListener(System.out::println);
        System.out.println("Couleur: "+panel.getTeamColor());
        float f = 0f;
        int index = 0;
        while (true){
            try {
                panel.setLeds(Panneau.LedColor.NOIR);
                index++;
                index %= 16;
                f += 0.1f;
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }

    }
}
