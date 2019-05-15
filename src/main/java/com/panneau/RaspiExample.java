package com.panneau;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import java.io.IOException;

public class RaspiExample {

    public static void main(String[] args) throws IOException, UnsupportedBusNumberException, TooManyDigitsException {

        Panneau panel=new Panneau(16, 18900);
        int i=Integer.parseInt(args[0]);
        System.out.println(i);
        panel.printScore(i);
        panel.addListener(newColor -> System.out.println(newColor));
        System.out.println(panel.getTeamColor());
        while (true){
            try{Thread.sleep(1000);}catch (Exception e){}
        }

    }
}
