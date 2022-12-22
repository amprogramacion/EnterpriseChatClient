/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package es.amprogramacion.enterprisechatclient;

import static es.amprogramacion.enterprisechatclient.Inicio.chats;
import static es.amprogramacion.enterprisechatclient.Inicio.con;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author Angelmp2
 */
public class Receptor extends Thread {

    public Socket socket;
    public Inicio inicio;
    public DialogoUsername dg = new DialogoUsername(this);

    public Receptor(Inicio inicio, Socket socket) {
        this.socket = socket;
        this.inicio = inicio;
    }

    @Override
    public void run() {
        try {
            DataInputStream datainput = new DataInputStream(socket.getInputStream());
            while (true) {
                String response = datainput.readUTF();
                parsearComandoCliente(response);
            }
        } catch (IOException ex) {
            Logger.getLogger(Receptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Parsea el comando enviado por el cliente
     *
     * @param message
     */
    public void parsearComandoCliente(String message) {
        System.out.println("Recibo cliente: " + message);
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(message);

            String comando = (String) json.get("command");
            JSONObject params = (JSONObject) json.get("params");

            JSONObject obj = new JSONObject();
            JSONObject params_send = new JSONObject();
            switch (comando) {
                case "IdentificarUsuario":
                    obj = new JSONObject();
                    obj.put("command", "SetUsername");
                    String username_generated = InetAddress.getLocalHost().getHostName() + "_" + System.getProperty("user.name");
                    params_send.put("username", username_generated);
                    this.inicio.setMyUsername(username_generated);
                    this.inicio.setTitle("EnterpriseChatClient - Conectado como " + username_generated);
                    obj.put("params", params_send);
                    send(this.socket, obj.toJSONString());
                    obj.put("command", "UserList");
                    send(this.socket, obj.toJSONString());
                    break;
                case "UsernameDuplicado":
                    if (dg.isVisible() == false) {
                        dg.setVisible(true);
                        Inicio.jList1.setEnabled(false);
                    } else {
                        dg.txtUser.setText("");
                        dg.btnCambiarUsername.setEnabled(true);
                        dg.btnCambiarUsername.setText("Cambiar nombre de usuario");
                    }
                    this.inicio.setTitle("EnterpriseChatClient - Necesario nombre de usuario");
                    obj.put("command", "UserList");
                    send(this.socket, obj.toJSONString());
                    break;
                case "UsernameAceptado":
                    if (dg.isVisible() == true) {
                        dg.setVisible(false);
                    }
                    this.inicio.setMyUsername(params.get("username").toString());
                    this.inicio.setTitle("EnterpriseChatClient - Conectado como " + this.inicio.getMyUsername());
                    Inicio.jList1.setEnabled(true);
                    obj.put("command", "UserList");
                    send(this.socket, obj.toJSONString());
                    break;
                case "ListaUsuariosOnline":
                    Inicio.jList1.removeAll();
                    DefaultListModel<String> modelo = new DefaultListModel<>();
                    JSONObject users = (JSONObject) params.get("users");
                    Inicio.jList1.setModel(modelo);
                    users.forEach((key, value) -> {
                        if (!value.equals(this.inicio.getMyUsername())) {
                            modelo.addElement(value.toString());
                        }
                    });

                    break;
                case "ChatRecibido":
                    if (params.get("UsuarioReceptor").toString().equals(this.inicio.getMyUsername())) {
                        try {
                            Chat chat = Inicio.chats.get(params.get("UsuarioEmisor").toString());
                            chat.RecibirMensaje(params.get("UsuarioEmisor").toString(), params.get("msg").toString());
                        } catch (NullPointerException ex) { //Chat no existe, hay que crear la ventana
                            if (!params.get("msg").toString().substring(0, 2).equals("<-")) {
                                Chat chat = new Chat();
                                chat.setUser(params.get("UsuarioEmisor").toString());
                                chat.setVisible(true);
                                chat.setConexion(con);
                                chat.setEmisor(params.get("UsuarioReceptor").toString());
                                chats.put(params.get("UsuarioEmisor").toString(), chat);
                                chat.RecibirMensaje(params.get("UsuarioEmisor").toString(), params.get("msg").toString());
                            }
                        }
                    }
                    if (params.get("UsuarioEmisor").toString().equals(this.inicio.getMyUsername())) {
                        try {
                            Chat chat = Inicio.chats.get(params.get("UsuarioReceptor").toString());
                            chat.RecibirMensaje(params.get("UsuarioEmisor").toString(), params.get("msg").toString());
                        } catch (NullPointerException ex) { //Chat no existe, hay que crear la ventana
                            if (!params.get("msg").toString().substring(0, 2).equals("<-")) {
                                Chat chat = new Chat();
                                chat.setUser(params.get("UsuarioReceptor").toString());
                                chat.setVisible(true);
                                chat.setConexion(con);
                                chat.setEmisor(params.get("UsuarioEmisor").toString());
                                chats.put(params.get("UsuarioReceptor").toString(), chat);
                                chat.RecibirMensaje(params.get("UsuarioEmisor").toString(), params.get("msg").toString());
                            }
                        }
                    }
                    break;
                default:
                    System.out.println("Comando desconocido: " + comando);
                    break;
            }
        } catch (Exception ex) {
            System.out.println("Excepcion en parsearComando: " + ex);
        }
    }

    /**
     * Función que envía mensajes a un cliente en particular
     *
     * @param txt
     * @param usuario
     */
    public static void send(Socket usuario, String txt) {
        try {
            OutputStream aux = usuario.getOutputStream();
            DataOutputStream flujo_salida = new DataOutputStream(aux);
            flujo_salida.writeUTF(txt);
            System.out.println("Se envia: " + txt);
        } catch (IOException ex) {
            Logger.getLogger(Receptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
