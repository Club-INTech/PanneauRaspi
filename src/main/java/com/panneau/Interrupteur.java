package com.panneau;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe permet de récupérer l'etat d'un interrupteur
 * @author rene
 * @version 1.0
 * @since ever
 */
public class Interrupteur {
    private GpioController parent;
    private GpioPinDigitalInput pin;
    private PinState state;
    private List<StateChangeListener> listeners=new ArrayList<>();

    /**
     * Cette méthode permet d'ajouter un listener attendant un changement de couleur
     * @param listener Inmplémentation de l'interface <code>StateChangeListener</code>
     */
    public void addListener(StateChangeListener listener){
        listeners.add(listener);
    }

    private void stateChanged(){
        for(StateChangeListener listener:listeners){
            listener.handleStateChangedEvent();
        }
    }

    /**
     * Crée une instance d'interrupteur
     * @param pin Pin sur lequel est branché l'interrupteur
     * @param gpio Contôleur gpio de la raspi. Mettre <code>null</code> s'il n'a pas encore été initialisé.
     */
    public Interrupteur(Pin pin, GpioController gpio){
        if(gpio==null){
            parent= GpioFactory.getInstance();
        }else{
            parent=gpio;
        }
        this.pin=parent.provisionDigitalInputPin(pin, "Switch_Pin", PinPullResistance.PULL_UP);
        parent.setShutdownOptions(true);
        this.pin.setDebounce(50);
        this.pin.addListener((GpioPinListenerDigital)event->{state=event.getState();this.stateChanged();});
        state=this.pin.getState();
    }

    /**
     * Permet de récupérer la valeur du pin.
     * @return La valeur du pin
     */
    public PinState getState(){
        return state;
    }

    /**
     * Interface permettant d'éxécuter du code lors d'un evènement de changement de couleur.
     */
    public interface StateChangeListener {
        void handleStateChangedEvent();
    }
}
