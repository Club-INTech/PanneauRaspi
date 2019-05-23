package com.panneau;


import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe permet de commander le panneau comprenant l'afficheur 7 segments,
 * la LED RGB, et le switch.
 * @author rene
 * @version 0.4
 * @since ever
 */
public class Panneau {
    private Segments segments;
    private LEDs leds;
    private Interrupteur interrupteur;
    private static TeamColor teamColor;
    private List<teamColorChangeListener> listeners;

    /**
     * Enumère les deux couleurs d'équipe possibles
     */
    public enum TeamColor {JAUNE, VIOLET;
        @Override
        public String toString() {
            if(this==JAUNE){
                return "JAUNE";
            }
            return "VIOLET";
        }
    }

    /**
     * Crée une instance de panneau, en initialisant tous les paramètres par défaut;
     * @throws I2CFactory.UnsupportedBusNumberException Cette exception est levée si
     * le modèle de Raspberry utilisé n'a pas de bus I2C compatible avec cette bibliothèque.
     * @throws IOException Cette exception est levée en cas d'erreur de communication durant l'initialisation du bus I2C
     */
    public Panneau(int ledCount, int programPort, boolean useSegments)throws IOException, I2CFactory.UnsupportedBusNumberException {
        this(ledCount, programPort, RaspiPin.GPIO_07, useSegments);
    }

    public  Panneau(int ledCount, int programPort) throws  IOException, I2CFactory.UnsupportedBusNumberException{
        this(ledCount, programPort, true);
    }

    public Panneau(int ledCount, int programPort, Pin SwitchPin) throws IOException, I2CFactory.UnsupportedBusNumberException {
        this(ledCount, programPort, SwitchPin, true);
    }

        /**
         * Crée une instance de panneau en spécifiant les pins à utiliser.
         * @param SwitchPin pin en pullup reliée à l'intterrupteur
         * @throws I2CFactory.UnsupportedBusNumberException Cette exception est levée si
         * le modèle de Raspberry utilisé n'a pas de bus I2C compatible avec cette bibliothèque.
         * @throws IOException Cette exception est levée en cas d'erreur de communication durant l'initialisation du bus I2C
         */
    public Panneau(int ledCount, int programPort, Pin SwitchPin, boolean useSegments) throws IOException, I2CFactory.UnsupportedBusNumberException {
        if(useSegments){
            segments=new Segments(true);
        }
        leds =new LEDs(ledCount, programPort);
        interrupteur=new Interrupteur(SwitchPin, null);
        if(interrupteur.getState()==PinState.HIGH){
            teamColor= TeamColor.JAUNE;
        }else{
            teamColor= TeamColor.VIOLET;
        }
        listeners=new ArrayList<>();
        interrupteur.addListener(()->{
            //System.out.println("tout va bien");
            if(interrupteur.getState()==PinState.HIGH){
                teamColor = TeamColor.JAUNE;
                leds.fillColor(LEDs.RGBColor.JauneNeopixel);
                for(teamColorChangeListener listener:listeners){
                    listener.handleTeamColorChangedEvent(Panneau.TeamColor.JAUNE);
                }
                System.out.println("JAUNE");
            }else{
                teamColor = TeamColor.VIOLET;
                leds.fillColor(LEDs.RGBColor.VioletNeopixel);
                for(teamColorChangeListener listener:listeners){
                    listener.handleTeamColorChangedEvent(Panneau.TeamColor.VIOLET);
                }
                System.out.println("VIOLET");
            }
        });

        if(isYellow()){
            leds.fillColor(LEDs.RGBColor.JAUNE);
        }else {
            leds.fillColor(LEDs.RGBColor.MAGENTA);
        }

    }

    public LEDs getLeds() {
        return leds;
    }

    /**
     * Cette méthode permet de récupérer la TeamColor donnée par l'utilisateur
     * @return vrai si le switch est en position jaune.
     */
    public boolean isYellow(){
        return teamColor == TeamColor.JAUNE;
    }

    /**
     * Cette méthode permet de récupérer la TeamColor donnée par l'utilisateur
     * @return vrai si le switch est en position violette.
     */
    public boolean isViolet(){
        return teamColor == TeamColor.VIOLET;
    }

    /**
     * Cette méthode permet d'afficher le score
     * @param score La valeur à afficher
     * @throws IOException En cas d'erreur de transmission
     * @throws TooManyDigitsException Si le nombre à afficher est trop grand
     */
    public void printScore(int score) throws IOException,TooManyDigitsException{
        if(segments==null){
            System.err.println("Je ne peux pas afficher de score sans afficheur!");
            return;
        }
        segments.write(score);
    }

    /**
     * Permet de connaître l'état de l'interrupteur sous forme de TeamColor
     * @return La TeamColor de la position de l'interrupteur
     */
    public TeamColor getTeamColor(){
        return teamColor;
    }

    /**
     * Cette méthode permet d'ajouter un listener attendant un event de changement de couleur.
     * @param toAdd implémentation de l'interface <code>teamColorChangeListener</code> gérant l'évènement lors de l'appel
     */
    public void addListener(teamColorChangeListener toAdd){
        listeners.add(toAdd);
    }

    /**
     * Cette interface sert à gérer les évènements de changement de la couleur d'équipe.
     */
    public interface teamColorChangeListener{
        void handleTeamColorChangedEvent(TeamColor newColor);
    }
}
