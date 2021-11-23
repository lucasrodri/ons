/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ons;

/**
 * A path is nothing more than a list of lightpaths.
 * 
 * @author andred
 */
public class Path {
    
    private LightPath[] lightpaths;
    
    /**
     * Creates a new Path object.
     * 
     * @param lightpaths list of LightPath objects that form the Path
     */
    public Path(LightPath[] lightpaths) {
        this.lightpaths = lightpaths;
    }
    
    /**
     * This method is only for clear a path
     */
    public void resetPath(){
        this.lightpaths = null;
    }
    
    /**
     * Returns the list of lightpaths that belong to a given Path.
     * 
     * @return lightpaths list of LightPath objects that form the Path
     */
    public LightPath[] getLightpaths() {
        return lightpaths;
    }
    
    /**
     * Retrieves if this Path has this lightpath or not.
     * @param lp the lightpath
     * @return true if this Path has this lightpath or not, false otherwise
     */
    public boolean hasLightpath(LightPath lp) {
        for (LightPath lightpath : lightpaths) {
            if(lightpath.id == lp.id) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Add lightptahs in this path
     * @param lightpaths lightpaths list of LightPath objects
     */
    public void addLightpaths(LightPath[] lightpaths) {
        LightPath[] lps = new LightPath[this.lightpaths.length + lightpaths.length];
        System.arraycopy(this.lightpaths, 0, lps, 0, this.lightpaths.length);
        System.arraycopy(lightpaths, 0, lps, this.lightpaths.length, lightpaths.length);
        this.lightpaths = lps;
    }
    
    /**
     * Retrieves the source node of this path
     * @return the source node of this path, the source of the first lightpaths in this path
     */
    public int getSource(){
        return this.lightpaths[0].getSource();
    }
    
    /**
     * Retrieves the destination node of this path
     * @return the destination node of this path, the destination of the last lightpaths in this path
     */
    public int getDestination(){
        return this.lightpaths[this.lightpaths.length - 1].getDestination();
    }
    
    public int getBWAvailable(){
        if(lightpaths.length < 1){
            throw (new IllegalArgumentException());
        }
        int min = Integer.MAX_VALUE;
        int aux;
        for (LightPath lightpath : lightpaths) {
            if(lightpath instanceof EONLightPath) {
                aux = ((EONLightPath) lightpath).getBwAvailable();
                if(aux < min) {
                    min = aux;
                }
            } else if(lightpath instanceof WDMLightPath) {
                throw (new IllegalArgumentException("Method not implemented"));
            }
        }
        return min;
    }
    
    public int getBW(){
        if(lightpaths.length < 1){
            throw (new IllegalArgumentException());
        }
        int min = Integer.MAX_VALUE;
        int aux;
        for (LightPath lightpath : lightpaths) {
            if(lightpath instanceof EONLightPath) {
                aux = ((EONLightPath) lightpath).getBw();
                if(aux < min) {
                    min = aux;
                }
            } else if(lightpath instanceof WDMLightPath) {
                throw (new IllegalArgumentException("Method not implemented"));
            }
        }
        return min;
    }
    
    @Override
    public String toString(){
        String str = "";
        for (int i = 0; i < this.lightpaths.length; i++) {
            str += " LP-" + i + ": " + lightpaths[i].toString() + "\n";
        }
        return str;
    }
    
    public String toTrace(){
        String str = "";
        for (int i = 0; i < this.lightpaths.length; i++) {
            str += " LP-" + i + ": " + lightpaths[i].getID();
        }
        return str;
    }
}
