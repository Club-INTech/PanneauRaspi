package com.panneau;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;


/**
 * Cette classe contrôle un module Sparkfun de 4 afficheurs 7 segments contrôlé en I2C.
 * @author rene
 * @since ever
 * @version 1.0
 */
public class Segments {
    private I2CDevice device;
    final private int maxDigits=4;

    /**
     * Initialise le bus I2C et crée une instance de Segments.
     * @param address l'adresse de l'afficheur sur le bus I2C. La valeur 0x71 est prise par défaut si aucune ou plousieurs valeurs sont renseignées.
     * @throws com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException Si le modèle de Raspi utilisé
     */
    public Segments(int... address) throws IOException, I2CFactory.UnsupportedBusNumberException {
        I2CBus i2CBus=null;
        int i=0;
        while(i2CBus==null) {
            try {
                i2CBus = I2CFactory.getInstance(i);
            } catch (I2CFactory.UnsupportedBusNumberException e) {
                ++i;
            }
            if(i==18){
                System.out.println("Ports de 1 à 18 testés");
                throw new I2CFactory.UnsupportedBusNumberException();
            }
        }
        int displayAddress;
        if(address.length==1){
            displayAddress=address[0];
        }else{
            displayAddress=0x71;
        }
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
