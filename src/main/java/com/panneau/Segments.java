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
     * @throws IOException En cas d'erreur de communication
     * @throws I2CFactory.UnsupportedBusNumberException Si le bus utilisé n'est pas supporté
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
        int displayAddress;
        //*
        if(scan) {
            factoryReset(i2CBus);
        }
        //System.out.println("Connecté à l'adresse "+String.format("0x%x", displayAddress));
        //*/
        displayAddress=0x71;
        device=i2CBus.getDevice(displayAddress);
        try{
            write(37);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Ecrit une valeur sur l'écran de l'afficheur
     * @param data la valeur à afficher
     * @throws IOException Cette exception est levée après deux erreurs de transmission consécutives.
     * @throws TooManyDigitsException Si le nombre à afficher contient plus de 4 chiffres.
     */
    public void write(int data)throws IOException,TooManyDigitsException{
        try{
            //device.write((byte)0x79);
            device.write(0x79, (byte)0x00);
            device.write(toByteArray(data));
        }catch (IOException e){
            I2CBus i2CBus=null;
            try {
                //System.out.println("Test sur port "+i);
                i2CBus = I2CFactory.getInstance(1);
                factoryReset(i2CBus);

                device=i2CBus.getDevice(0x71);
                //device.write((byte)0x79);
                device.write(0x79, (byte)0x00);
                device.write(toByteArray(data));
            } catch (I2CFactory.UnsupportedBusNumberException er) {
                er.printStackTrace();
            }

        }
    }

    private byte[] toByteArray(int i) throws TooManyDigitsException{
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

    private void factoryReset(I2CBus i2CBus){
        try{
            i2CBus.getDevice(0x00).write((byte)0x81);
            device.write((byte)0x81);
        }catch(IOException e) {
            System.err.println("Factory reset sur le panneau");
            for (int displayAddress = 0x03; displayAddress <= 0x77; ++displayAddress) {
                try {
                    i2CBus.getDevice(displayAddress).write((byte) 0x81);
                } catch (IOException er) {
                    //print nothing
                }
            }
        }
    }
}
