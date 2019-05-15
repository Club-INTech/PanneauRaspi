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
        float f = 0f;
        while (true){
            try {
                panel.getLeds().fillColor(new LEDs.RGBColor(f, (f+1f/3f) % 1f, (f+2f/3f) % 1f));
                f += 0.01f;
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }

    }
}
