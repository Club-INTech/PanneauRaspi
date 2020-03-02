package com.panneau;

import java.io.IOException;

import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe permet de commander le panneau comprenant l'afficheur 7 segments,
 * 2 LED, et le switch.
 *
 * @author rene, jglrxavpok
 * @version 2.0
 * @since ever
 */
public class Panneau {
    private static TeamColor teamColor = TeamColor.UNDEFINED;
    private List<teamColorChangeListener> listeners;
    private int serverTCPPort;
    private int clientUDPPort;
    private boolean initiated;
    private Socket TCPsocket;
    private PrintStream output;
    private StringBuilder builder = new StringBuilder();
    private boolean triedToLaunch;
    private boolean useSegments;

    /**
     * Enumère les deux couleurs d'équipe possibles
     */
    public enum TeamColor {
        JAUNE, BLEU, UNDEFINED;

        @Override
        public String toString() {
            return name();
        }
    }

    public enum LedColor {
        BLEU,
        JAUNE,
        NOIR;

        @Override
        public String toString() {
            return this.name();
        }
    }

    /**
     * Cette interface sert à gérer les évènements de changement de la couleur d'équipe.
     */
    public interface teamColorChangeListener {
        void handleTeamColorChangedEvent(TeamColor newColor);
    }

    /**
     * Crée une instance de panneau.
     * @param pythonTCPPort numéro de port TCP à utiliser par le serveur python
     * @param javaUDPPort numéro de port UDP à utiliser par ce client UDP
     * @param useSegments a-t-on un écran sur ce panneau?
     */
    public Panneau(int pythonTCPPort, int javaUDPPort, boolean useSegments) {
        this.serverTCPPort = pythonTCPPort;
        this.clientUDPPort = javaUDPPort;
        this.useSegments = useSegments;
        PythonListenerThread pythonListenerThread = new PythonListenerThread(javaUDPPort);
        pythonListenerThread.start();
        listeners = new ArrayList<>();
        addListener((newColor) -> {
            if (newColor == TeamColor.JAUNE) {
                setLeds(LedColor.JAUNE);
            } else if (newColor == TeamColor.BLEU) {
                setLeds(LedColor.BLEU);
            } else {
                setLeds(LedColor.NOIR);
            }
        });
        ensureInitiated();
    }

    /**
     * Permet de connaître l'état de l'interrupteur sous forme de TeamColor
     *
     * @return La TeamColor de la position de l'interrupteur
     */
    public TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * Cette méthode permet d'afficher le score
     *
     * @param score La valeur à afficher
     */
    public void printScore(int score) {
        if(!useSegments){
            System.err.println("Affichage du score désactivé pour ce panneau, n'appelez pas la fonction printScore !");
            return;
        }
        ensureInitiated();
        sendCommand("score", score);
    }

    /**
     * Cette méthode permet de changer la couleur des leds
     *
     * @param c la nouvelle couleur
     */
    public void setLeds(LedColor c) {
        ensureInitiated();
        sendCommand("set", c.toString());
    }

    /**
     * Cette méthode permet d'ajouter un listener attendant un event de changement de couleur.
     *
     * @param toAdd implémentation de l'interface <code>teamColorChangeListener</code> gérant l'évènement lors de l'appel
     */
    public void addListener(teamColorChangeListener toAdd) {
        listeners.add(toAdd);
    }

    /**
     * Envoie une commande au programme qui gère les LEDs
     */
    private void sendCommand(Object... parameters) {
        builder.setLength(0); // reset
        for (Object obj : parameters) {
            builder.append(obj).append(" ");
        }
        if (output != null) {
            output.println(builder.toString());
            output.flush();
        }
    }

    /**
     * Cette méthode permet de s'assurer que le serveur python est lancé et que la connection TCP est établie
     */
    private void ensureInitiated() {
        if (initiated) {
            return;
        }
        if (!triedToLaunch) {
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",
                    "python3 "+System.getProperty("user.dir")+"/LED/LED.py " + serverTCPPort + " " + clientUDPPort);
            //  builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            System.out.println("executing: python3 "+System.getProperty("user.dir")+"/LED/LED.py " + serverTCPPort + " " + clientUDPPort);
            try {
                Process process = builder.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        try {
                            if (TCPsocket != null) {
                                TCPsocket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        process.destroy();
                        process.destroyForcibly();
                    }
                });
                triedToLaunch = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!initiated) {
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
     * Ce thread  configure un socket UDP puis attend des packets (UDP) envoyés par le serveur python à chaque
     * changement d'état de le broche de coix de couleur
     */
    private class PythonListenerThread extends Thread implements Runnable {
        private DatagramSocket UDPSocket;

        PythonListenerThread(int UDPJavaPort) {
            try {
                UDPSocket = new DatagramSocket(UDPJavaPort);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (UDPSocket != null) {
                    byte[] buff = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buff, buff.length);
                    System.out.println("Waiting for UDP packet from python on port " + UDPSocket.getLocalPort());
                    UDPSocket.receive(packet);                                      //méthode bloquante
                    String data = new String(packet.getData()).replace("\0", "");
                    System.out.println("UDP packet recieved from " + packet.getPort() + " containing " + data);
                    if (data.equals("JAUNE")) {
                        teamColor = Panneau.TeamColor.JAUNE;
                    } else if (data.equals("BLEU")) {
                        teamColor = Panneau.TeamColor.BLEU;
                    } else {
                        teamColor = Panneau.TeamColor.UNDEFINED;
                    }
                    for (teamColorChangeListener listener : listeners) {
                        listener.handleTeamColorChangedEvent(teamColor);
                    }
                    System.out.println("Couleur : " + data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (UDPSocket != null)
                        UDPSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
