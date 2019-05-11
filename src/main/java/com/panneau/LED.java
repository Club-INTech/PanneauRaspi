package com.panneau;

import com.pi4j.io.gpio.*;

import java.io.File;
import java.io.IOException;

/**
 * Cette classe permet de contrôler une LED RGB.
 * @author rene
 * @version 0.1
 * @since ever
 */
public class LED {
    private GpioPinDigitalOutput r;
    private GpioPinDigitalOutput g;
    private GpioPinDigitalOutput b;
    final private PinState defaultState=PinState.LOW;
    private GpioController gpio;

    /**
     * Construit une instance de LED
     * @param r pin contrôllant le rouge
     * @param g pin contrôllant le vert
     * @param b pin conrôllant le bleu
     * @param gpio Contrôleur gpio de la raspi. Mettre <code>null</code> s'il n'a pas encore été initialisé.
     */
    public LED(Pin r, Pin g, Pin b, GpioController gpio){
        /*
        Runtime run=Runtime.getRuntime();
        try{
            run.exec("sudo python3");
            run.exec("import board");
            run.exec("import neopixel");
            run.exec("pixel=noepixel.NeoPixel(board.D18, 15)");

        }catch (IOException e){
            e.printStackTrace();
        }
         */
    }

    /**
     * Change la couleur de la led
     * @param r composante rouge de la nouvelle couleur
     * @param g composante verte de la nouvelle couleur
     * @param b composante bleue de la nouvelle couleur
     */
    public void setColor(boolean r, boolean g, boolean b){
        try {
            int R = r ? 1 : 0;
            int G = g ? 1 : 0;
            int B = b ? 1 : 0;
            Runtime.getRuntime().exec("sudo python3 LED.py "+R*100+" "+G*100+" "+B*100);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public GpioController getGpioController(){
        return this.gpio;
    }

    /**
     * Change la couleur de la led
     * @param c La nouvelle couleur à afficher
     */
    public void setColor(RGBColor c){
        if(c==RGBColor.MAGENTA){
            try {
                //Process p=Runtime.getRuntime().exec(new String[]{"sudo","python3","LED.py","100","0","127"}, new String[], new File("~/panneauRaspi/"));
                Process p=Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "sudo python3 ~/panneauRaspi/LED.py 100 0 127"});
                p.waitFor();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        if(c==RGBColor.JAUNE){
            try {
                Process p=Runtime.getRuntime().exec(new String[]{"sudo","python3","~/panneauRaspi/LED.py","127","90","0"});
                p.waitFor();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        int v=c.value;
        boolean b=v%2==1;
        v/=2;
        boolean g=v%2==1;
        v/=2;
        boolean r=v%2==1;
        setColor(r, g, b);
    }

    /**
     * Enumère les couleurs possibles pour la LED
     */
    public enum RGBColor {
        ROUGE(0b100),
        VERT(0b010),
        BLEU(0b001),
        JAUNE(0b110),
        CYAN(0b011),
        MAGENTA(0b101),
        NOIR(0b000),
        BLANC(0b111);

        private int value;
        RGBColor(int val){value=val;}

        @Override
        public String toString() {
            switch (this){
                case JAUNE:
                    return "JAUNE";
                case MAGENTA:
                    return "MAGENTA";
                case BLEU:
                    return "BLEU";
                case CYAN:
                    return "CYAN";
                case NOIR:
                    return "NOIR";
                case VERT:
                    return "VERT";
                case BLANC:
                    return "BLANC";
                case ROUGE:
                    return "ROUGE";
                default:
                    return null;
            }
        }
    }
}
