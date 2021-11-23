/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * The Optical Cross-Connects (OXCs) can switch the optical signal coming
 * in on a wavelenght of an input fiber link to the same wavelength in an
 * output fiber link. The OXC may also switch the optical signal on an
 * incoming wavelength of an input fiber link to some other wavelength on
 * an output fiber link.
 * 
 * The OXC object has grooming input and output ports.
 * Traffic grooming is the process of grouping many small data flows
 * into larger units, so they can be processed as single units.
 * Grooming in OXCs has the objective of minimizing the cost of the network.
 * 
 * @author andred
 */
public abstract class OXC {

    protected int id;
    protected int groomingInputPorts;
    protected int groomingOutputPorts;
    protected TreeSet<Integer> freeGroomingInputPorts;
    protected TreeSet<Integer> freeGroomingOutputPorts; 
    protected int nodeDegree; 
    private boolean sleep; //indicates whether the OXC is sleeping or awake
    private final int type; //indicates the OXC type, used in TrafficGenerator
    private final int group; //indicates the OXC group, used in TrafficGenerator
    
    /**
     * Creates a new OXC object.All its attributes must be given
 given by parameter, except for the free grooming input and output
 ports, that, at the beginning of the simulation, are the same as 
 the total number of grooming input and output ports.
     * 
     * @param id the OXC's unique identifier
     * @param groomingInputPorts total number of grooming input ports
     * @param groomingOutputPorts total number of grooming output ports
     * @param type OXC type, used in TrafficGenerator
     * @param group OXC group, used in TrafficGenerator
     */
    public OXC(int id, int groomingInputPorts, int groomingOutputPorts, int type, int group) {
        this.id = id;
        this.groomingInputPorts = groomingInputPorts;
        this.freeGroomingInputPorts = startGroomingPorts(groomingInputPorts);
        this.groomingOutputPorts = groomingOutputPorts;
        this.freeGroomingOutputPorts = startGroomingPorts(groomingOutputPorts);
        this.sleep = true;
        this.type = type;
        this.group = group;
    }
    
    /**
     * Retrieves the OXC's unique identifier.
     * 
     * @return the OXC's id attribute
     */
    public int getID() {
        return id;
    }

    /**
     * Retrieves the node degree (number of neighbors)
     * @return the number of neighbors in this node
     */
    public int getNodeDegree() {
        return nodeDegree;
    }

    protected void setNodeDegree(int nodeDegree) {
        this.nodeDegree = nodeDegree;
    }
    
    /**
     * Says whether or not a given OXC has free
     * grooming input port(s).
     * 
     * @return true if the OXC has free grooming input port(s)
     */
    public boolean hasFreeGroomingInputPort() {
        return !freeGroomingInputPorts.isEmpty();
    }
    
    /**
     * Says whether or not a given groomingInputPort in this OXC has free
     * 
     * @param groomingInputPort id of groomingInputPort
     * @return true if the OXC has free grooming input port id
     */
    public boolean hasFreeGroomingInputPort(int groomingInputPort) {
        return freeGroomingInputPorts.contains(groomingInputPort);
    }
    
    /**
     * Says whether or not a given OXC has all free
     * grooming input port(s).
     * 
     * @return true if the OXC has all free grooming input port(s)
     */
    public boolean allFreeGroomingInputPort() {
        return freeGroomingInputPorts.size() == groomingInputPorts;
    }
    
    /**
     * By decreasing the number of free grooming input ports,
     * this function "reserves" a grooming input port.
     * 
     * @return the number of free grooming input port, if the number is -1 is because has some error
     */
    public int reserveGroomingInputPort() {
        if (!freeGroomingInputPorts.isEmpty()) {
            int r = freeGroomingInputPorts.pollFirst();
            changeState();
            return r;
        } else {
            return -1;//if some lightpath has transponder -1 is because has some error in simulator's code
        }
    }
    
    /**
     * By increasing the number of free grooming input ports,
     * this function "releases" a grooming input port.
     * The "groomingInputPort" can be -1 when the "VirtualTopology" remove "Lightpath" that are on a optical grooming.
     * 
     * @param groomingInputPort the grooming input Port to be released
     * @return false if there are no grooming input ports to be freed
     */
    public boolean releaseGroomingInputPort(int groomingInputPort) {
        if ((freeGroomingInputPorts.size() < groomingInputPorts) && (groomingInputPort >= 0)) {
            freeGroomingInputPorts.add(groomingInputPort);
            changeState();
            return true;
        }
        return false;
    }
    
    /**
     * Says whether or not a given OXC has free
     * grooming output port(s).
     * 
     * @return true if the OXC has free grooming output port(s)
     */
    public boolean hasFreeGroomingOutputPort() {
        return !freeGroomingOutputPorts.isEmpty();
    }
    
    /**
     * Says whether or not a given groomingOutputPort in this OXC has free
     * 
     * @param groomingOutputPort id of groomingOutputPort
     * @return true if the OXC has free grooming output port id
     */
    public boolean hasFreeGroomingOutputPort(int groomingOutputPort) {
        return freeGroomingOutputPorts.contains(groomingOutputPort);
    }
    
    /**
     * Says whether or not a given OXC has all free
     * grooming output port(s).
     * 
     * @return true if the OXC has all free grooming output port(s)
     */
    public boolean allFreeGroomingOutputPort() {
        return freeGroomingOutputPorts.size() == groomingOutputPorts;
    }
    
    /**
     * By decreasing the number of free grooming output ports,
     * this function "reserves" a grooming output port.
     * 
     * @return the number of free grooming output port, if the number is -1 is because has some error
     */
    public int reserveGroomingOutputPort() {
        if (!freeGroomingOutputPorts.isEmpty()) {
            int r = freeGroomingOutputPorts.pollFirst();
            changeState();
            return r;
        } else {
            return -1;
        }
    }
    
    /**
     * By increasing the number of free grooming output ports,
     * this function "releases" a grooming output port.
     * The "groomingOutputPort" can be -1 when the "VirtualTopology" remove "Lightpath" that are on a optical grooming.
     * 
     * @param groomingOutputPort the grooming output Port to be released
     * @return false if there are no grooming output ports to be freed
     */
    public boolean releaseGroomingOutputPort(int groomingOutputPort) {
        if ((freeGroomingOutputPorts.size() < groomingOutputPorts) && (groomingOutputPort >= 0)) {
            freeGroomingOutputPorts.add(groomingOutputPort);
            changeState();
            return true;
        }
        return false;
    }
    
    /**
     * Start grooming ports from the number of groomingPorts provided.
     * @param groomingPorts the groomingPorts provided
     * @return the TreeSet with the freeGroomingPorts
     */
    private TreeSet<Integer> startGroomingPorts(int groomingPorts){
        TreeSet<Integer> ports = new TreeSet<>();
        for(int i = 0; i < groomingPorts; i++){
            ports.add(i);
        }
        return ports;
    }

    public int getGroomingInputPorts() {
        return groomingInputPorts;
    }
    
    public int[] getFreeGroomingInputPorts() {
        int[] out = new int[freeGroomingInputPorts.size()];
        Iterator<Integer> it = freeGroomingInputPorts.iterator();
        int index = 0;
        while (it.hasNext()) {
            out[index++] = it.next();
        }
        return out;
    }
    
    public int[] getActiveGroomingInputPorts() {
        int[] out = new int[groomingInputPorts - freeGroomingInputPorts.size()];
        int index = 0;
        for (int i = 0; i < groomingInputPorts; i++) {
            if(!freeGroomingInputPorts.contains(i)){
                out[index++] = i;
            }
        }
        return out;
    }

    public int getGroomingOutputPorts() {
        return groomingOutputPorts;
    }
    
    public int[] getFreeGroomingOutputPorts() {
        int[] out = new int[freeGroomingOutputPorts.size()];
        Iterator<Integer> it = freeGroomingOutputPorts.iterator();
        int i = 0;
        while (it.hasNext()) {
            out[i++] = it.next();
        }
        return out;
    }
    
    public int[] getActiveGroomingOutputPorts() {
        int[] out = new int[groomingOutputPorts - freeGroomingOutputPorts.size()];
        int index = 0;
        for (int i = 0; i < groomingOutputPorts; i++) {
            if(!freeGroomingOutputPorts.contains(i)){
                out[index++] = i;
            }
        }
        return out;
    }
    
    private void changeState() {
        boolean state = true;
        if(freeGroomingInputPorts.size() != groomingInputPorts) {
            state = false;
        }
        if(freeGroomingOutputPorts.size() != groomingOutputPorts) {
            state = false;
        }
        sleep = state;
    }

    /**
     * Retrieves whether the OXC is sleeping or awake
     * @return 
     */
    public boolean isSleep() {
        return sleep;
    }

    /**
     * Retrieves the OXC's Type.
     * 
     * @return the OXC's type attribute
     */
    public int getType() {
        return type;
    }

    /**
     * Retrieves the OXC's Group.
     * 
     * @return the OXC's group attribute
     */
    public int getGroup() {
        return group;
    }
}
