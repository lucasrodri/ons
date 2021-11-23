/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import java.util.ArrayList;

/**
 * In an optical network, a lightpath is a clear optical path which may traverse
 * several links in the network.
 * It is also good to know that information transmitted through lightpaths does not
 * undergo any conversion to or from electrical form.
 * 
 * @author andred
 */
public abstract class LightPath {
    
    private final int DPP = 0;
    private final int DLP = 1;
    private final int SPP = 2;
    private final int SLP = 3;
    protected long id;
    protected int src;
    protected int dst;
    protected int[] links;
    protected int Tx;
    protected int Rx;
    protected boolean reserved;
    protected boolean backup;
    protected int typeProtection;
    private double snr;
    //the ids of lightpaths backups
    protected ArrayList<Long> lpBackup;
    
    protected ArrayList<Flow> flows;
    protected ArrayList<BulkData> bulks;
    protected ArrayList<Batch> batchs;

    /**
     * Creates a new LightPath object.
     * 
     * @param id            unique identifier
     * @param src           source node
     * @param dst           destination node
     * @param links         fyberlinks list composing the path
     */
    public LightPath(long id, int src, int dst, int[] links) {
        if (id < 0 || src < 0 || dst < 0 || links.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            this.id = id;
            this.src = src;
            this.dst = dst;
            this.links = links;
        }
        Tx = -1;
        Rx = -1;
        reserved = false;
        backup = false;
        lpBackup = new ArrayList<>();
        flows = new ArrayList<>();
        bulks = new ArrayList<>();
        batchs = new ArrayList<>();
    }
    
    /**
     * Retrieves the unique identifier of a given LightPath.
     * 
     * @return the LightPath's id attribute
     */
    public long getID() {
        return id;
    }

    //TODO change this to protected in master ons simulator
    public void setId(long id) {
        this.id = id;
    }

    public int getTx() {
        return Tx;
    }

    protected void setTx(int Tx) {
        this.Tx = Tx;
    }

    public int getRx() {
        return Rx;
    }

    protected void setRx(int Rx) {
        this.Rx = Rx;
    }

    public boolean isReserved() {
        return reserved;
    }

    protected void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
    
    public boolean isBackup() {
        return backup;
    }

    protected void setBackup(boolean backup) {
        this.backup = backup;
    }

    public double getSnr() {
        return snr;
    }

    //TODO change this to protected in master ons simulator
    public void setSnr(double snr) {
        this.snr = snr;
    }

    public ArrayList<Long> getLpBackup() {
        return lpBackup;
    }

    protected void addLpBackup(long lpBackupId) {
        this.lpBackup.add(lpBackupId);
    }
    
    public boolean isBackupShared() {
        if(isBackup() && (typeProtection == SLP || typeProtection == SPP)) {
            return true;
        }
        return false;
    }

    protected ArrayList<Flow> getFlows() {
        return flows;
    }

    protected void addFlow(Flow flow) {
        this.flows.add(flow);
    }
    
    protected void removeFlow(Flow flow) {
        this.flows.remove(flow);
    }

    protected ArrayList<BulkData> getBulks() {
        return bulks;
    }

    protected void addBulk(BulkData bulk) {
        this.bulks.add(bulk);
    }
    
    protected void removeBulk(BulkData bulk) {
        this.bulks.remove(bulk);
    }

    protected ArrayList<Batch> getBatchs() {
        return batchs;
    }

    protected void addBatch(Batch batch) {
        this.batchs.add(batch);
    }
    
    protected void removeBatch(Batch batch) {
        this.batchs.remove(batch);
    }
    
    /**
     * Retrieves the source node of a given LightPath.
     * 
     * @return the LightPath's src attribute
     */
    public int getSource() {
        return src;
    }
    
    /**
     * Retrieves the destination node of a given LightPath.
     * 
     * @return the LightPath's dst attribute.
     */
    public int getDestination() {
        return dst;
    }
    
    /**
     * Retrieves the LightPath's vector containing the identifier numbers
     * of the links that compose the path.
     * 
     * @return a vector of integers that represent fiberlinks identifiers
     */
    public int[] getLinks() {
        return links;
    }
    
    /**
     * The fiber links are physical hops. Therefore, by retrieving the number
     * of elements in a LightPath's list of fiber links, we get the number of
     * hops the LightPath has.
     * 
     * @return the number of hops in a given LightPath
     */
    public int getHops() {
        return links.length;
    }

    protected void setTypeProtection(String typeProtection) {
        switch (typeProtection) {
            case "DPP": this.typeProtection = 0;
                break;
            case "DLP": this.typeProtection = 1;
                break;
            case "SPP": this.typeProtection = 2;
                break;
            case "SLP": this.typeProtection = 3;
                break;
            default: this.typeProtection = 0;
                break;
        }
    }
    
    public String getTypeProtection(int typeProtection) {
        switch (typeProtection) {
            case 0: return "DPP";
            case 1: return "DLP";
            case 2: return "SPP";
            case 3: return "SLP";
        }
        return "Error";
    }
    
    public int getTypeProtection(String typeProtection) {
        switch (typeProtection) {
            case "DPP": return 0;
            case "DLP": return 1;
            case "SPP": return 2;
            case "SLP": return 3;
            default: return 0;
        }
    }
    
    public String getTypeProtection() {
        switch (typeProtection) {
            case 0: return "DPP";
            case 1: return "DLP";
            case 2: return "SPP";
            case 3: return "SLP";
        }
        return "Error";
    }
    
    /**
     * Prints all information related to a given LightPath, starting with
     * its ID, to make it easier to identify.
     * 
     * @return string containing all the values of the LightPath's parameters
     */
    @Override
    public String toString() {
        String lightpath = Long.toString(id) + " " + Integer.toString(src) + " " + Integer.toString(dst);
        return lightpath;
    }
    
    public String toTrace() {
        String lightpath = Long.toString(id) + " " + Integer.toString(src) + " " + Integer.toString(dst);
        return lightpath;
    }
}
