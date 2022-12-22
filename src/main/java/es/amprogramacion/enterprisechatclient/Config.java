/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package es.amprogramacion.enterprisechatclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author Angelmp2
 */
public class Config {

    public static void CrearConfiguracion() {
        File configFile = new File(System.getenv("APPDATA")+" \\EnterpriseChatClient\\config.properties");
        try {
            if (!configFile.exists()) {
                if (configFile.mkdirs()) {
                    System.out.println("Directorio creado");
                } else {
                    System.out.println("Error al crear directorio");
                }
                FileWriter writer = new FileWriter(configFile);
                writer.close();
            }

        } catch (FileNotFoundException ex) {
            // file does not exist
        } catch (IOException ex) {
            // I/O error
        }
    }
}
