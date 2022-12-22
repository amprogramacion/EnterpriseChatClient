package es.amprogramacion.enterprisechatclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Conexion {

    public Socket socket;
    public Inicio inicio;
    public Receptor receptor;

    public void ConectarSocket(Inicio iniciov) throws IOException {
        socket = new Socket("localhost", 5600);
        inicio = iniciov;
        receptor = new Receptor(inicio, socket);
        Thread receptorThread = new Thread(receptor);
        receptorThread.start();
    }
}
