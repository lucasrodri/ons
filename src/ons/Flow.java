/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

/**
 * The Flow class defines an object that can be thought of as a flow
 * of data, going from a source node to a destination node. 
 * 
 * @author andred
 */
public class Flow {

    private long id;
    private int src;
    private int dst;
    private int bw;
    private double duration;
    private int cos;

    /**
     * Creates a new Flow object.
     * 
     * @param id            unique identifier
     * @param src           source node
     * @param dst           destination node
     * @param bw            bandwidth required (Mbps)
     * @param duration      duration time (seconds)
     * @param cos           class of service
     */
    public Flow(long id, int src, int dst, int bw, double duration, int cos) {
        if (id < 0 || src < 0 || dst < 0 || bw < 1 || duration < 0 || cos < 0) {
            throw (new IllegalArgumentException());
        } else {
            this.id = id;
            this.src = src;
            this.dst = dst;
            this.bw = bw;
            this.duration = duration;
            this.cos = cos;
        }
    }
    
    /**
     * Retrieves the unique identifier for a given Flow.
     * 
     * @return the value of the Flow's id attribute
     */
    public long getID() {
        return id;
    }
    
    /**
     * Retrieves the source node for a given Flow.
     * 
     * @return the value of the Flow's src attribute
     */
    public int getSource() {
        return src;
    }
    
    /**
     * Retrieves the destination node for a given Flow.
     * 
     * @return the value of the Flow's dst attribute
     */
    public int getDestination() {
        return dst;
    }
    
    /**
     * Retrieves the required bandwidth for a given Flow.
     * 
     * @return the value of the Flow's bw attribute.
     */
    public int getRate() {
        return bw;
    }
    
    /**
     * Retrieves the duration time, in seconds, of a given Flow.
     * 
     * @return the value of the Flow's duration attribute
     */
    public double getDuration() {
        return duration;
    }
    
    /**
     * Retrieves a given Flow's "class of service".
     * A "class of service" groups together similar types of traffic
     * (for example, email, streaming video, voice,...) and treats
     * each type with its own level of service priority.
     * 
     * @return the value of the Flow's cos attribute
     */
    public int getCOS() {
        return cos;
    }
    
    /**
     * Sets the duration time, in seconds, of a given Flow.
     * 
     * @param time the time in seconds
     */
    protected void setDuration(double time) {
        duration = time;
    }
    
    /**
     * Prints all information related to a given Flow.
     * 
     * @return string containing all the values of the flow's parameters
     */
    public String toString() {
        String flow = Long.toString(id) + ": " + Integer.toString(src) + "->" + Integer.toString(dst) + " rate: " + Integer.toString(bw) + " duration: " + Double.toString(duration) + " cos: " + Integer.toString(cos);
        return flow;
    }
    
    /**
     * Creates a string with relevant information about the flow, to be
     * printed on the Trace file.
     * 
     * @return string with values of the flow's parameters
     */
    public String toTrace()
    {
    	String trace = Long.toString(id) + " " + Integer.toString(src) + " " + Integer.toString(dst) + " " + Integer.toString(bw) + " " + Double.toString(duration) + " " + Integer.toString(cos);
    	return trace;
    }
}
