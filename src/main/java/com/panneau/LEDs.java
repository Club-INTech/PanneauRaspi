package com.panneau;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Cette classe permet de contrôler les LEDs RGB.
 * @author rene, jglrxavpok
 * @version 1.0
 * @since ever and ever
 */
public class LEDs {

    private int serverTCPPort;
    private int clientUDPPort;
    private boolean initiated;
    private Socket TCPsocket;
    private PrintStream output;
    private StringBuilder builder = new StringBuilder();
    private boolean triedToLaunch;

    /**
     * Construit une instance de LEDs
     */
    public LEDs(int serverTCPPort, int clientUDPPort) {
        this.serverTCPPort = serverTCPPort;
        this.clientUDPPort = clientUDPPort;
    }

    /**
     * Vérifies que le programme des LEDs tourne. Si c'est pas le cas, on le lance
     */
    private void ensureInitiated() {
        if(initiated) {
            return;
        }
        if( ! triedToLaunch) {
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "python3 /home/intech/PanneauRaspi/LED/LED.py " + serverTCPPort + " "+ clientUDPPort);
            //  builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            try {
                Process process = builder.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            if(TCPsocket != null) {
                                TCPsocket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("killing python");
                        process.destroy();
                        process.destroyForcibly();
                        System.out.println("python killed");
                    }
                });
                triedToLaunch = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(20);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        if(!initiated) {
            try {
                TCPsocket = new Socket("localhost", serverTCPPort);
                output = new PrintStream(TCPsocket.getOutputStream(), true);
                initiated = true;
                System.out.println("Connection TCP au serveur python établie");
            } catch (IOException e) {
                System.err.println("Echec de la connexion au process, réessai plus tard...");
                e.printStackTrace();
            }
        }
    }


    /**
     * Set la couleur de la led
     * @param c la couleur à appliquer
     */
    public void set(Color c) {
        ensureInitiated();
        sendCommand("set", c.toString());
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
        if(output != null) {
            output.println(builder.toString());
            output.flush();
        }
    }

    /**
     * Enumère les couleurs possibles pour la LED
     */
    public enum Color{
        BLEU,
        JAUNE,
        NOIR;

        @Override
        public String toString() {
            return this.name();
        }
    }
}
