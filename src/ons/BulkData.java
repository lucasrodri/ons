package ons;

import java.util.Comparator;

/**
 * 
 * The BulkData class defines an object that can be thought of as a data amount transfer request
 * ,going from a source node to a destination node within a specific deadline. 
 * @author Leia Sousa 
 */

public class BulkData{
    /**
     * 
     * @return the BulkDeadlineComparator
     */
    private long id;
    private int src;
    private int dst;
    private int dataAmount;
    private double deadline;
    private int cos;
    private final int type = 1;
    private final double arrivalTime;
    private double servedTime;
    private double departureTime;
    private final double deadlineTime;
    
    /**
     * Creates a new request.
     * 
     * @param id            unique identifier
     * @param src           source node
     * @param dst           destination node
     * @param dataAmount    quantity of bulk data
     * @param deadline      maximum tolerable transfer time (seconds)
     * @param cos           classe of service
     * @param arrivalTime   the arrivaTime of this bulk
     * @param deadlineTime  the deadline time of this bulk
     */ 
    public BulkData(long id, int src, int dst, int dataAmount, double deadline, int cos, double arrivalTime, double deadlineTime) {
        if (id < 0 || src < 0 || dst < 0 || dataAmount < 1 || deadline < 0 ) {
            throw (new IllegalArgumentException());
        } else {
            this.id = id;
            this.src = src;
            this.dst = dst;
            this.dataAmount = dataAmount;
            this.deadline = deadline;
            this.cos = cos;
            this.arrivalTime = arrivalTime;
            this.deadlineTime = deadlineTime;
        }
        //System.out.println("O bulk: " +this);//test
    }

            
    /**
     * Retrieves the unique identifier for a given BulkDataUnicast request.
     * 
     * @return the value of the request's id attribute
     */
    public long getID() {
        return id;
    }
    
    public void setID(long id) {
        this.id = id;
    }
    
    /**
     * Retrieves the source node for a given BulkDataUnicast request.
     * 
     * @return the value of the request's src attribute
     */
    public int getSource() {
        return src;
    }
    
    /**
     * Retrieves the destination node for a given BulkDataUnicast request.
     * 
     * @return the value of the Flow's dst attribute
     */
    public int getDestination() {
        return dst;
    }
    
    /**
     * Retrieves the data amount to be transferred for a given BulkDataUnicast request.
     * 
     * @return the quantity of bulk data
     */
    public int getDataAmount() {
        return dataAmount;
    }
    
    /**
     * Assigns a new value to the required quantity of bulk data of a given BulkDataUnicast request.
     * 
     * @param dataAmount
     */
    public void setDataAmount(int dataAmount){
        this.dataAmount = dataAmount; 
    }
    
    /**
     * Retrieves the duration time to make the transfer, in seconds, of a given BulkDataUnicast request.
     *
     * @return the value of the BulkDataUnicast's duration attribute
     */
    public double getDeadline() {
        return deadline;
    }
    
    public int getCOS(){
        return cos;
    }
    
    /**
     * Verifies that the rate meets the bulk's requirements
     * @param rate the rate in Mb
     * @return if the rate meets the bulk's requirements, false otherwise
     */
    public boolean verifyRate(int rate) {
        return (double) this.dataAmount <= Math.ceil((double) rate * (this.departureTime-this.servedTime));
    }

    public double getDeadlineTime() {
        return deadlineTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getServedTime() {
        return servedTime;
    }

    protected void setServedTime(double servedTime) {
        this.servedTime = servedTime;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    protected void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }
    
    /**
     * Prints all information related to the arriving BulkDataUnicast request.
     * 
     * @return string containing all the values of the parameters of all requests
     */
    @Override
    public String toString(){
        String bulkDataUnicast = Long.toString(id) + ": " + Integer.toString(src) + "->" + Integer.toString(dst) + " DA: " + Integer.toString(dataAmount) + " Dl: " + Double.toString(deadline);
        return bulkDataUnicast;
    }
    
    /**
     * Creates a string with relevant information about the BulkDataUnicast request, to be
     * printed on the Trace file.
     * 
     * @return string with values of the parameters of BulkDataUnicast request
     */
    public String toTrace(){
    	String trace = Long.toString(id) + " " + Integer.toString(src) + " " + Integer.toString(dst) + " " + Integer.toString(dataAmount) + " " + Double.toString(deadline);
    	return trace;
    }
    
    public static BulkData BulkAmountComparatorAscending(BulkData b1, BulkData b2) {
        if (b1.getDataAmount() > b2.getDataAmount()) {
            return b2;
        } else {
            return b1;
        }
    }
    
    public static BulkData BulkAmountComparatorDescending(BulkData b1, BulkData b2) {
        if (b1.getDataAmount() > b2.getDataAmount()) {
            return b1;
        } else {
            return b2;
        }
    }    
    
    public static BulkData BulkDLComparatorAscending(BulkData b1, BulkData b2) {
        if (b1.getDeadline() > b2.getDeadline()) {
            return b2;
        } else {
            return b1;
        }
    }
    
    public static BulkData BulkDLComparatorDescending(BulkData b1, BulkData b2) {
        if (b1.getDeadline() > b2.getDeadline()) {
            return b1;
        } else {
            return b2;
        }
    }    
    
    
    /**
    *Sort with Lambda: In Java 8, the List interface is supports the sort method 
    * directly, no need to use Collections.sort anymore.
    * */    
}