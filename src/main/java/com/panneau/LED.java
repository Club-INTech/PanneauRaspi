package com.panneau;

import com.pi4j.io.gpio.*;
import sun.rmi.runtime.Log;

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
        if(gpio==null){
            this.gpio= GpioFactory.getInstance();
        }else{
            this.gpio=gpio;
        }
        this.gpio.setShutdownOptions(false);
        this.r=this.gpio.provisionDigitalOutputPin(r, "Led_Red", defaultState);
        this.g=this.gpio.provisionDigitalOutputPin(g, "Led_Green", defaultState);
        this.b=this.gpio.provisionDigitalOutputPin(b, "Led_Blue", defaultState);
    }

    /**
     * Change la couleur de la led
     * @param r composante rouge de la nouvelle couleur
     * @param g composante verte de la nouvelle couleur
     * @param b composante bleue de la nouvelle couleur
     */
    public void setColor(boolean r, boolean g, boolean b){
        if(r){
            this.r.high();
        }else{
            this.r.low();
        }

        if(g){
            this.g.high();
        }else{
            this.g.low();
        }

        if(b){
            this.b.high();
        }else{
            this.b.low();
        }
        System.err.println("Couleur"+r+" "+g+" "+b);
    }

    public GpioController getGpioController(){
        return this.gpio;
    }

    /**
     * Change la couleur de la led
     * @param c La nouvelle couleur à afficher
     */
    public void setColor(RGBColor c){
        int v=c.value;
        boolean b=v%2==1;
        v/=2;
        boolean g=v%2==1;
        v/=2;
        boolean r=v%2==1;
        setColor(r, g, b);
    }

    //TODO: en utilisant le PWM on peut passer en 256 nuances

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
