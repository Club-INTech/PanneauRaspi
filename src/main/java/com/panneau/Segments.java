package com.panneau;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;


/**
 * Cette classe contrôle un module Sparkfun de 4 afficheurs 7 segments contrôlé en I2C.
 * Lien de la datasheet: https://github.com/sparkfun/Serial7SegmentDisplay/wiki/Serial-7-Segment-Display-Datasheet
 * @author rene
 * @since ever
 * @version 1.0
 */
public class Segments {
    private I2CDevice device;
    final private int maxDigits=4;

    /**
     * Initialise le bus I2C et crée une instance de Segments en envoyant un factory reset sur le broadcast
     */
    public Segments() throws  IOException, I2CFactory.UnsupportedBusNumberException {
        this(false);
    }

    /**
     * Initialise le bus I2C et crée une instance de Segments
     * @param scan Pour scanner toutes les adresses, sinon envoie "factory reset" au broadcast
     * @throws IOException
     * @throws I2CFactory.UnsupportedBusNumberException
     */
    public Segments(boolean scan) throws IOException, I2CFactory.UnsupportedBusNumberException {
        I2CBus i2CBus=null;
        //System.out.println("Entre dans le constructeur du 7 segments");
        int i=0;
        while(i2CBus==null) {
            try {
                //System.out.println("Test sur port "+i);
                i2CBus = I2CFactory.getInstance(i);
            } catch (I2CFactory.UnsupportedBusNumberException e) {
                ++i;
            }
            if(i==18){
                //System.out.println("Ports de 0 à 17 testés, aucun ne répond");
                throw new I2CFactory.UnsupportedBusNumberException();
            }
        }
        if(!scan) {
            i2CBus.getDevice(0x00).write((byte) 0x81); //envoie "factory reset" sur le broadcast
        }
        int displayAddress=0x71;
        //*
        if(scan) {
            displayAddress=0x03;
            device = null;
            while (device == null && displayAddress <= 0x77) {
                try {
                    device = i2CBus.getDevice(displayAddress);
                    Thread.sleep(1);// je sais pas si c'est utile mais on verra
                    device.write(toByteArray(0x81));
                } catch (IOException e) {
                    ++displayAddress;
                    device = null;
                } catch (TooManyDigitsException e) {
                    //Ne fait rien MDR
                    //A quel moment "0" ferait lever cette exception
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        //System.out.println("Connecté à l'adresse "+String.format("0x%x", displayAddress));
        //*/
        displayAddress=0x71;
        device=i2CBus.getDevice(displayAddress);
    }

    /**
     * Ecrit une valeur sur l'écran de l'afficheur
     * @param data la valeur à afficher
     * @throws IOException Cette exception est levée après deux erreurs de transmission consécutives.
     * @throws TooManyDigitsException Si le nombre à afficher contient plus de 4 chiffres.
     */
    public void write(int data)throws IOException,TooManyDigitsException{
        try{
            device.write(toByteArray(data));
        }catch (IOException e){
            I2CBus i2CBus=null;
            try {
                //System.out.println("Test sur port "+i);
                i2CBus = I2CFactory.getInstance(1);
            } catch (I2CFactory.UnsupportedBusNumberException er) {
                er.printStackTrace();
            }

            int displayAddress = 0x03;
            device = null;
            while (device == null && displayAddress <= 0x77) {
                try {
                    device = i2CBus.getDevice(displayAddress);
                    Thread.sleep(1);// je sais pas si c'est utile mais on verra
                    device.write(toByteArray(0x81));
                } catch (IOException er) {
                    //System.out.println("Adresse " + String.format("0x%x", displayAddress) + ": aucune réponse"); //Print de debug
                    ++displayAddress;
                    device = null;
                } catch (TooManyDigitsException er) {
                    //Ne fait rien MDR
                    //A quel moment "0" ferait lever cette exception
                } catch (InterruptedException er) {
                    er.printStackTrace();
                }
            }
            device=i2CBus.getDevice(0x71);
            device.write((byte)0x79);
            device.write((byte)0x00);
            device.write(toByteArray(data));

        }
    }

    private byte[] toByteArray(int i)throws TooManyDigitsException{
        byte[] buff=new byte[4];
        int tmp=i;
        for(int j=1; j<=4; ++j){
            buff[4-j]=(byte)(tmp%10);
            tmp/=10;

        }
        if(tmp>0){
            throw new TooManyDigitsException((int)Math.log(tmp)+1, this.maxDigits);
        }
        return buff;
    }
}
