/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

/**
 *
 * @author lucasrc
 */
public class MultiPathProtect {

    private MultiPath[] multipaths = new MultiPath[2];

    public MultiPathProtect(MultiPath[] multiPaths) {
        if (multiPaths.length != 2) {
            throw (new IllegalArgumentException());
        } else {
            this.multipaths = multiPaths;
        }
    }

    public MultiPath[] getMultiPaths() {
        return multipaths;
    }

    public MultiPath getPrimary() {
        return multipaths[0];
    }
    
    public void setPrimary(MultiPath[] multipaths) {
        multipaths[0] = multipaths[0];
    }    

    public void setBackup(MultiPath[] multipaths) {
        multipaths[1] = multipaths[1];
    }    
    
    public int getTotalBWPrimaryAvailable(){
        return multipaths[0].getTotalBWAvailable();
    }
    
    public int getTotalBWPrimary(){
        return multipaths[0].getTotalBW();
    }
    
    public MultiPath getBackup() {
        return multipaths[1];
    }
    
    public int getTotalBWBackupAvailable(){
        return multipaths[1].getTotalBWAvailable();
    }
    
    public int getTotalBWBackup(){
        return multipaths[1].getTotalBW();
    }

    boolean hasLightpath(LightPath lightpath) {
        for (MultiPath multipath : multipaths) {
            if(multipath.hasLightpath(lightpath)) {
                return true;
            }
        }
        return false;
    }
    
}
