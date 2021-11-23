/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import java.util.ArrayList;

/**
 * The Wavelength Division Multiplexing (WDM) Link represents a Fiberlink in an
 * optical network.
 *
 * @author andred
 */
public class WDMLink extends Link {

    private int wavelengths;
    private boolean[] freeWavelengths;
    private int[] availableBandwidth;
    private int bw;
    private double pc; //in W

    public WDMLink(int id, int src, int dst, double delay, double weight, int wavelengths, int bw, double pc) {
        super(id, src, dst, delay, weight);
        if (wavelengths < 1 || bw < 1) {
            throw (new IllegalArgumentException());
        } else {
            this.wavelengths = wavelengths;
            this.bw = bw;
            this.freeWavelengths = new boolean[wavelengths];
            for (int i = 0; i < wavelengths; i++) {
                this.freeWavelengths[i] = true;
            }
            this.availableBandwidth = new int[wavelengths];
            for (int i = 0; i < wavelengths; i++) {
                this.availableBandwidth[i] = bw;
            }
            this.pc = pc;
        }
    }

    public int getBandwidth() {
        return this.bw;
    }

    /**
     * Retrieves Power comsumption of this wavelengths in this link
     * @return power comsumption transponder in W
     */
    public double getPc() {
        return pc;
    }
    
    /**
     * Retrieves the number of available wavelengths for a given WDMLink.
     *
     * @return the value of the WDMLink's wavelengths attribute
     */
    public int getWavelengths() {
        return this.wavelengths;
    }
    
    /**
     * Says whether or not a determined wavelength is available.
     *
     * @param wavelength the index number of the wavelength that will be checked
     * for availability in the WDMLink's freeWavelengths vector
     * @return true if the wavelength is available
     */
    public Boolean isWLAvailable(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            return freeWavelengths[wavelength];
        }
    }
    
    /**
     * Retrieves the list of all available wavelengths in a given WDMLink.
     *
     * @return list of available wavelengths
     */
    public int[] hasWLAvailable() {
        ArrayList<Integer> wls = new ArrayList<Integer>();
        for (int i = 0; i < this.wavelengths; i++) {
            if (this.isWLAvailable(i)) {
                wls.add(i);
            }
        }
        int[] a = new int[wls.size()];
        for (int i = 0; i < wls.size(); i++) {
            a[i] = wls.get(i);
        }
        return a;
    }
    
    /**
     * Retrieves the lowest available wavelength in a given WDMLink.
     *
     * @return first true item in the freeWavelengths vector
     */
    public int firstWLAvailable() {
        for (int i = 0; i < this.wavelengths; i++) {
            if (this.isWLAvailable(i)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Retrieves how much bandwidth is available for a determined wavelength.
     *
     * @param wavelength for which available bandwidth will be verified
     * @return amount of available bandwidth
     */
    public int amountBWAvailable(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            //System.out.println("Available: "+Integer.toString(availableBandwidth[wavelength]));
            return availableBandwidth[wavelength];
        }
    }
    
    /**
     * Retrieves the list of wavelengths that have a determined amount of
     * bandwidth available.
     *
     * @param bw the minimum bandwidth required
     * @return vector of integers with the retrieved wavelengths
     */
    public int[] hasBWAvailable(int bw) {
        ArrayList<Integer> bws = new ArrayList<Integer>();
        for (int i = 0; i < this.wavelengths; i++) {
            if (this.amountBWAvailable(i) >= bw) {
                bws.add(i);
            }
        }
        int[] a = new int[bws.size()];
        for (int i = 0; i < bws.size(); i++) {
            a[i] = bws.get(i);
        }
        return a;
    }
    
    /**
     * By attributing false to a given wavelength inside the freeWavelengths
     * vector, this function "reserves" a wavelength.
     *
     * @param wavelength value of the wavelength to be reserved
     * @return true if operation was successful, or false otherwise
     */
    public boolean reserveWavelength(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            if (freeWavelengths[wavelength]) {
                freeWavelengths[wavelength] = false;
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * By attributing true to a given wavelength inside the freeWavelengths
     * vector, this function "releases" a wavelength.
     *
     * @param wavelength value of the wavelength to be released
     */
    public void releaseWavelength(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            freeWavelengths[wavelength] = true;
        }
    }
    
    /**
     * Inside a given wavelength, decreases, if possible, the available
     * bandwidth. Therefore, this is equivalent to adding traffic to the
     * network.
     *
     * @param wavelength value of the wavelength where traffic will be added
     * @param bw amount of bandwidth to be allocated
     * @return the remaining available bandwidth after operation
     */
    public int addTraffic(int wavelength, int bw) {
        if (wavelength < 0 || wavelength >= this.wavelengths || bw > availableBandwidth[wavelength]) {
            Tracer.getTracerObject().flushTrace();
            throw (new IllegalArgumentException());
        } else {
            availableBandwidth[wavelength] -= bw;
            return availableBandwidth[wavelength];
        }

    }
    
    /**
     * Inside a given wavelength, increases, if possible, the available
     * bandwidth. Therefore, this is equivalent to removing traffic from the
     * network.
     *
     * @param wavelength value of the wavelength from where traffic will be
     * removed
     * @param bw amount of bandwidth to be released
     * @return the remaining available bandwidth after operation
     */
    public int removeTraffic(int wavelength, int bw) {
        if (wavelength < 0 || wavelength >= this.wavelengths || bw > this.bw - availableBandwidth[wavelength]) {
            throw (new IllegalArgumentException());
        } else {
            availableBandwidth[wavelength] += bw;
            return availableBandwidth[wavelength];
        }
    }
    
    public double getLinkUtilization(int wavelength) {
        if (wavelength < 0 || wavelength >= this.wavelengths) {
            throw (new IllegalArgumentException());
        } else {
            return (double) 1 - ((double)amountBWAvailable(wavelength) / (double)getBandwidth());
        }
    }

    /**
     * Retrieves the number of available wavelengths that have space in the its
     * bandwidth, given WDMLink.
     *
     * @return the number of avaiable free wavelengths
     */
    public int getFreeWavelengths() {
        int cont = 0;
        for (int i = 0; i < this.wavelengths; i++) {
            if (freeWavelengths[i]) {
                cont++;
            }
        }
        return cont;
    }
    
    @Override
    public String toString() {
        String link = Long.toString(id) + ": " + Integer.toString(src) + "->" + Integer.toString(dst) + " delay: " + Double.toString(delay) + " wvls: " + Integer.toString(wavelengths) + " bw: " + Integer.toString(bw) + " weight:" + Double.toString(weight);
        return link;
    }

}
