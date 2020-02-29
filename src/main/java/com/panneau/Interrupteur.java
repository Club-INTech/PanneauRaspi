package com.panneau;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.panneau.Panneau.teamColorChangeListener;

/**
 * Cette classe permet de récupérer l'etat d'un interrupteur
 *
 * @author rene
 * @version 2020.1
 */
class Interrupteur {
    private List<Panneau.teamColorChangeListener> listeners = new ArrayList<>();
    private Panneau.TeamColor color = Panneau.TeamColor.UNDEFINED;

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
                    System.out.println("Waiting for UDP packet from python on port "+UDPSocket.getLocalPort());
                    UDPSocket.receive(packet);                                      //méthode bloquante
                    String data = new String(packet.getData()).replace("\0","");
                    System.out.println("UDP packet recieved from "+packet.getPort() + " containing "+data);
                    if (data.equals("JAUNE")) {
                        color = Panneau.TeamColor.JAUNE;
                    } else if (data.equals("BLEU")) {
                        color = Panneau.TeamColor.BLEU;
                    } else {
                        color = Panneau.TeamColor.UNDEFINED;
                    }
                    for (teamColorChangeListener listener : listeners) {
                        listener.handleTeamColorChangedEvent(color);
                    }
                    System.out.println("Couleur : "+data);
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

    /**
     * Cette méthode permet d'ajouter un listener attendant un changement de couleur
     *
     * @param listener Inmplémentation de l'interface <code>StateChangeListener</code>
     */
    void addListener(teamColorChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Crée une instance d'interrupteur
     */
    Interrupteur(int UDPJavaPort) {
        PythonListenerThread pythonListenerThread = new PythonListenerThread(UDPJavaPort);
        pythonListenerThread.start();
    }

    public Panneau.TeamColor getColor() {
        return color;
    }
}
