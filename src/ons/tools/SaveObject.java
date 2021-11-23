/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.tools;

/**
 *
 * @author lucas
 */
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
 
public class SaveObject {
 
    public static void save(Object object, String path) {
 
           try {
             FileOutputStream saveFile = new FileOutputStream(path);
             ObjectOutputStream stream = new ObjectOutputStream(saveFile);
 
              // salva o objeto
             stream.writeObject(object);
 
             stream.close();
           } catch (Exception exc) {
             exc.printStackTrace();
           }
    }
}
