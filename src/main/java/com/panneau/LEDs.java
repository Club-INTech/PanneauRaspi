package com.panneau;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Cette classe permet de contrôler les LEDs RGB.
 * @author rene, jglrxavpok
 * @version 0.2
 * @since ever and ever
 */
public class LEDs {

    private final int ledCount;
    private int programPort;
    private boolean initiated;
    private Socket socket;
    private PrintStream output;
    private StringBuilder builder = new StringBuilder();

    /**
     * Construit une instance de LEDs
     */
    public LEDs(int ledCount, int programPort) {
        this.ledCount = ledCount;
        this.programPort = programPort;
    }

    /**
     * Vérifies que le programme des LEDs tourne. Si c'est pas le cas, on le lance
     */
    private void ensureInitiated() {
        if(initiated) {
            return;
        }
        ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "sudo", "python3", "/home/pi/panneauRaspi/LED/LED.py", String.valueOf(programPort), String.valueOf(ledCount));
        try {
            Process process = builder.start();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    super.run();
                    process.destroyForcibly();
                    try {
                        if(socket != null) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            while(!initiated) {
                try {
                    socket = new Socket("127.0.0.1", programPort);
                    output = new PrintStream(socket.getOutputStream(), true);
                } catch (IOException e) {
                    System.err.println("Echec de la connexion au process, réessai dans 0.5s: ");
                    e.printStackTrace();
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initiated = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Change la couleur de la led
     * @param c La nouvelle couleur à afficher
     */
    public void fillColor(RGBColor c) {
        ensureInitiated();
        sendCommand("fill", c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Remplit une bande de LEDs
     * @param start la première led
     * @param end la dernière led (incluse)
     * @param c la couleur à appliquer
     */
    public void fillRange(int start, int end, RGBColor c) {
        ensureInitiated();
        sendCommand("range", start, end, c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Set la couleur d'une seule led
     * @param index l'indice de la LED
     * @param c la couleur à appliquer
     */
    public void set(int index, RGBColor c) {
        ensureInitiated();
        sendCommand("set", index, c.getRed(), c.getGreen(), c.getBlue());
    }

    /**
     * Envoie une commande au programme qui gère les LEDs
     * @param parameters
     */
    private void sendCommand(Object... parameters) {
        builder.setLength(0); // reset
        for(Object obj : parameters) {
            builder.append(obj).append(" ");
        }
        output.println(builder.toString());
        output.flush();
    }

    /**
     * Enumère les couleurs possibles pour la LED
     */
    public static class RGBColor {
        public static final RGBColor ROUGE = new RGBColor(1,0,0);
        public static final RGBColor VERT = new RGBColor(0,1,0);
        public static final RGBColor BLEU = new RGBColor(0,0,1);
        public static final RGBColor JAUNE = new RGBColor(1,1,0);
        public static final RGBColor CYAN = new RGBColor(0,1,1);
        public static final RGBColor MAGENTA = new RGBColor(1,0,1);
        public static final RGBColor NOIR = new RGBColor(0,0,0);
        public static final RGBColor BLANC = new RGBColor(1,1,1);

        private final float red;
        private final float green;
        private final float blue;

        public RGBColor(float red, float green, float blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public float getBlue() {
            return blue;
        }

        public float getGreen() {
            return green;
        }

        public float getRed() {
            return red;
        }

        @Override
        public String toString() {
            return red+" "+green+" "+blue;
        }
    }
}
