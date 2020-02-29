package com.panneau;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public class PanneauClient {
    private int programPort;
    private boolean initiated;
    private Socket socket;
    private PrintStream output;
    private StringBuilder builder = new StringBuilder();
    private boolean triedToLaunch;
    private boolean has7segs;

    public PanneauClient(int programPort, boolean has7segs){
        this.programPort = programPort;
        this.has7segs = has7segs;
    }

    private void ensureInitiated() {
        if(initiated) {
            return;
        }
        if( ! triedToLaunch) {
            ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "sudo python3 /home/intech/panneauRaspi/LED/LED.py " + programPort + " " + has7segs);
            //  builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            try {
                Process process = builder.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        process.destroyForcibly();
                        //fillColor(LEDs.RGBColor.NOIR);
                        try {
                            if(socket != null) {
                                socket.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
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
        if(!initiated){
            try {
                socket = new Socket("localhost", programPort);
                output = new PrintStream(socket.getOutputStream(), true);
                initiated = true;
            } catch (IOException e) {
                System.err.println("Échec de la connexion au process, essai plus tard...");
                e.printStackTrace();
            }
        }
    }
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
}
