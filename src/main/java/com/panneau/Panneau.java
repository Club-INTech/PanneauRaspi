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
    private LED led;
    private Interrupteur interrupteur;
    private static teamColor teamColor;
    private List<teamColorChangeListener> listeners;

    /**
     * Enumère les deux couleurs d'équipe possibles
     */
    public enum teamColor {JAUNE, VIOLET;
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
    public Panneau()throws IOException, I2CFactory.UnsupportedBusNumberException {
        this(RaspiPin.GPIO_04, RaspiPin.GPIO_02, RaspiPin.GPIO_03, RaspiPin.GPIO_07);
    }

    /**
     * Crée une instance de panneau en spécifiant les pins à utiliser.
     * @param LEDRedPin Pin reliée à la cathode rouge
     * @param LEDGreenPin pin reliée à la cathode verte
     * @param LEDBluePin pin reliée à la cathode bleue
     * @param SwitchPin pin en pullup reliée à l'intterrupteur
     * @throws I2CFactory.UnsupportedBusNumberException Cette exception est levée si
     * le modèle de Raspberry utilisé n'a pas de bus I2C compatible avec cette bibliothèque.
     * @throws IOException Cette exception est levée en cas d'erreur de communication durant l'initialisation du bus I2C
     */
    public Panneau(Pin LEDRedPin, Pin LEDGreenPin, Pin LEDBluePin, Pin SwitchPin) throws IOException, I2CFactory.UnsupportedBusNumberException {
        segments=new Segments();
        led=new LED(LEDRedPin,LEDGreenPin,LEDBluePin, null);
        interrupteur=new Interrupteur(SwitchPin, null);
        if(interrupteur.getState()==PinState.HIGH){
            teamColor= Panneau.teamColor.JAUNE;
        }else{
            teamColor= Panneau.teamColor.VIOLET;
        }
        listeners=new ArrayList<>();
        interrupteur.addListener(()->{
            //System.out.println("tout va bien");
            if(interrupteur.getState()==PinState.HIGH){
                teamColor = teamColor.JAUNE;
                led.setColor(LED.RGBColor.JAUNE);
                for(teamColorChangeListener listener:listeners){
                    listener.handleTeamColorChangedEvent(Panneau.teamColor.JAUNE);
                }
                //System.out.println("JAUNE");
            }else{
                teamColor = teamColor.VIOLET;
                led.setColor(LED.RGBColor.MAGENTA);
                for(teamColorChangeListener listener:listeners){
                    listener.handleTeamColorChangedEvent(Panneau.teamColor.VIOLET);
                }
                //System.out.println("VIOLET");
            }
        });
    }

    /**
     * Cette méthode permet de récupérer la teamColor donnée par l'utilisateur
     * @return vrai si le switch est en position jaune.
     */
    public boolean isYellow(){
        return teamColor == teamColor.JAUNE;
    }

    /**
     * Cette méthode permet de récupérer la teamColor donnée par l'utilisateur
     * @return vrai si le switch est en position violette.
     */
    public boolean isViolet(){
        return teamColor == teamColor.VIOLET;
    }

    /**
     * Cette méthode permet d'afficher le score
     * @param score La valeur à afficher
     * @throws IOException En cas d'erreur de transmission
     * @throws TooManyDigitsException Si le nombre à afficher est trop grand
     */
    public void printScore(int score) throws IOException,TooManyDigitsException{
        segments.write(score);
    }

    /**
     * Permet de connaître l'état de l'interrupteur sous forme de teamColor
     * @return La teamColor de la position de l'interrupteur
     */
    public teamColor getTeamColor(){
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
        void handleTeamColorChangedEvent(teamColor newColor);
    }
}
