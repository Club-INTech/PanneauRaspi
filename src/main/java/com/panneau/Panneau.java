package com.panneau;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe permet de commander le panneau comprenant l'afficheur 7 segments,
 * la LED RGB, et le switch.
 * @author rene
 * @version 1.0
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
    public enum TeamColor {JAUNE, BLEU, UNDEFINED;
        @Override
        public String toString() {
            return name();
        }
    }


        /**
         * Crée une instance de panneau.
         * le modèle de Raspberry utilisé n'a pas de bus I2C compatible avec cette bibliothèque.
         * @throws IOException Cette exception est levée en cas d'erreur de communication durant l'initialisation du bus I2C
         */
    public Panneau(int pythonTCPPort, int javaUDPPort, boolean useSegments) throws IOException {
        /*if(useSegments){
            segments=new Segments(true);
            try{
                segments.write(37);
            }catch (IOException | TooManyDigitsException e){
                System.err.println("Erreur d'initialisation du panneau");
            }
        }*/
        leds =new LEDs(pythonTCPPort, javaUDPPort);
        interrupteur=new Interrupteur(javaUDPPort);
        teamColor = interrupteur.getColor();
        listeners=new ArrayList<>();
        interrupteur.addListener((newColor)->{
            //System.out.println("tout va bien");
            if(newColor == TeamColor.JAUNE){
                leds.set(LEDs.Color.JAUNE);
                for(teamColorChangeListener listener:listeners){
                    listener.handleTeamColorChangedEvent(Panneau.TeamColor.JAUNE);
                }
            }else if (newColor == TeamColor.BLEU){
                leds.set(LEDs.Color.BLEU);
                for(teamColorChangeListener listener:listeners){
                    listener.handleTeamColorChangedEvent(Panneau.TeamColor.BLEU);
                }
            }else{
                leds.set(LEDs.Color.NOIR);
                for(teamColorChangeListener listener:listeners){
                    listener.handleTeamColorChangedEvent(TeamColor.UNDEFINED);
                }
            }
        });
    }

    public LEDs getLeds() {
        return leds;
    }

    /**
     * Cette méthode permet d'afficher le score
     * @param score La valeur à afficher
     * @throws IOException En cas d'erreur de transmission
     * @throws TooManyDigitsException Si le nombre à afficher est trop grand
     */
    public void printScore(int score) throws IOException,TooManyDigitsException{
        if(segments==null){
            System.err.println("Grâce à @rene il n'y a plus de NPE ici, mais arrêtez d'essayer d'afficher du score sur le secondaire svp.");
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
        interrupteur.addListener(toAdd);
    }

    /**
     * Cette interface sert à gérer les évènements de changement de la couleur d'équipe.
     */
    public interface teamColorChangeListener{
        void handleTeamColorChangedEvent(TeamColor newColor);
    }
}
