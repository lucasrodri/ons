/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lucasrc
 */
public class PrintFile {
    File file = null;
    FileWriter fw;
    BufferedWriter bw;

    public PrintFile(String fileName) {
        this.file = new File(fileName);
        try {
            this.fw = new FileWriter(file);
            this.bw = new BufferedWriter(fw);
        } catch (IOException ex) {
            Logger.getLogger(PrintFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void closeFile() {
        try {
            if (file != null) {
                this.bw.close();
                this.fw.close();
                file = null;
            }
        } catch (IOException ex) {
            Logger.getLogger(PrintFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writerFile(String str) {
        try {
            bw.write(str);
        } catch (IOException ex) {
            Logger.getLogger(PrintFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void writerFile(int c) {
        try {
            bw.write(c);
        } catch (IOException ex) {
            Logger.getLogger(PrintFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
