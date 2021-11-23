/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import ons.util.WeightedGraph;

import org.w3c.dom.*;

/**
 * The physical topology of a network refers to he physical layout of devices on
 * a network, or to the way that the devices on a network are arranged and how
 * they communicate with each other.
 *
 * @author andred
 */
public abstract class PhysicalTopology {
    
    protected int nodes;
    protected int links;
    protected OXC[] nodeVector;
    protected Link[] linkVector;
    protected Link[][] adjMatrix;
    protected DatacenterGroup[] datacenters;
    private final String topologyName;
    
    protected double mensageProcessingTime = 1.0E-5; //(in s)
    protected double configurationTimeOXC = 1.0E-5; //(in s)
    protected double propagationDelayTime = 4.0E-4; //(in s)
    protected double switchTime = 5.0E-4; //(in s)
    protected double oxcTransitionTime = 4.5E-4; //(in s)
    protected double oxcSleepModeExpenditure = 10.0; //(in percent)
    protected double oxcOperationExpenditure = 150.0; //(in W)
    protected double oxcNodeDegreeExpenditure = 85.0; //(in W)
    protected double oxcAddDropDegreeExpenditure = 100.0; //(in W)
    protected double trOverloadExpenditure = 1.683; //(in W)
    protected double trIdleExpenditure = 91.333; //(in W)
    protected double olaExpenditure = 100.0; //(in W)
    protected int spanSize = 80; //(in km)
    

    /**
     * Creates a new PhysicalTopology object. Takes the XML file containing all
     * the information about the simulation environment and uses it to populate
     * the PhysicalTopology object. The physical topology is basically composed
     * of nodes connected by links, each supporting different wavelengths.
     *
     * @param xml file that contains the simulation environment information
     */
    public PhysicalTopology(Element xml) {
        if(xml.hasAttribute("name")){
            this.topologyName = xml.getAttribute("name");
        } else {
            this.topologyName = "";
        }
        if(xml.hasAttribute("mensageProcessingTime")){
            this.mensageProcessingTime = Double.parseDouble(xml.getAttribute("mensageProcessingTime"));
        }
        if(xml.hasAttribute("configurationTimeOXC")){
            this.configurationTimeOXC = Double.parseDouble(xml.getAttribute("configurationTimeOXC"));
        }
        if(xml.hasAttribute("propagationDelayTime")){
            this.propagationDelayTime = Double.parseDouble(xml.getAttribute("propagationDelayTime"));
        }
        if(xml.hasAttribute("switchTime")){
            this.switchTime = Double.parseDouble(xml.getAttribute("switchTime"));
        }
        if(xml.hasAttribute("oxcTransitionTime")){
            this.oxcTransitionTime = Double.parseDouble(xml.getAttribute("oxcTransitionTime"));
        }
        if(xml.hasAttribute("oxcSleepModeExpenditure")){
            this.oxcSleepModeExpenditure = Double.parseDouble(xml.getAttribute("oxcSleepModeExpenditure"));
        }
        if(xml.hasAttribute("oxcOperationExpenditure")){
            this.oxcOperationExpenditure = Double.parseDouble(xml.getAttribute("oxcOperationExpenditure"));
        }
        if(xml.hasAttribute("oxcNodeDegreeExpenditure")){
            this.oxcNodeDegreeExpenditure = Double.parseDouble(xml.getAttribute("oxcNodeDegreeExpenditure"));
        }
        if(xml.hasAttribute("oxcAddDropDegreeExpenditure")){
            this.oxcAddDropDegreeExpenditure = Double.parseDouble(xml.getAttribute("oxcAddDropDegreeExpenditure"));
        }
        if(xml.hasAttribute("trOverloadExpenditure")){
            this.trOverloadExpenditure = Double.parseDouble(xml.getAttribute("trOverloadExpenditure"));
        }
        if(xml.hasAttribute("trIdleExpenditure")){
            this.trIdleExpenditure = Double.parseDouble(xml.getAttribute("trIdleExpenditure"));
        }
        if(xml.hasAttribute("olaExpenditure")){
            this.olaExpenditure = Double.parseDouble(xml.getAttribute("olaExpenditure"));
        }
        if(xml.hasAttribute("spanSize")){
            this.spanSize = Integer.parseInt(xml.getAttribute("spanSize"));
        }
        
        try {
            if (Simulator.verbose) {
                System.out.println(xml.getAttribute("name"));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public PhysicalTopology() {
        this.topologyName = "";
    }

    /**
     * Retrieves the topology name.
     * @return the string topology name
     */
    public String getTopologyName() {
        return topologyName;
    }
    
    /**
     * Retrieves the number of nodes in a given PhysicalTopology.
     *
     * @return the value of the PhysicalTopology's nodes attribute
     */
    public int getNumNodes() {
        return nodes;
    }

    public int getSpanSize() {
        return spanSize;
    }
    
    /**
     * Retrieves the number of links in a given PhysicalTopology.
     *
     * @return number of items in the PhysicalTopology's linkVector attribute
     */
    public int getNumLinks() {
        return linkVector.length;
    }

    /**
     * Retrieves a specific node in the PhysicalTopology object.
     *
     * @param id the node's unique identifier
     * @return specified node from the PhysicalTopology's nodeVector
     */
    public OXC getNode(int id) {
        return nodeVector[id];
    }
    
    /**
     * Retrives the all free grooming input ports from all nodes
     * @return the number of grooming input ports from all nodes
     */
    public int getAllFreeGroomingInputPorts(){
        int ports = 0;
        for(int i = 0; i < nodes; i++){
            ports = ports + this.getNode(i).freeGroomingInputPorts.size();
        }
        return ports;
    }

    /**
     * Retrieves a specific link in the PhysicalTopology object, based on its
     * unique identifier.
     *
     * @param linkid the link's unique identifier
     * @return specified link from the PhysicalTopology's linkVector
     */
    public Link getLink(int linkid) {
        return linkVector[linkid];
    }

    /**
     * Retrieves a specific link in the PhysicalTopology object, based on its
     * source and destination nodes.
     *
     * @param src the link's source node
     * @param dst the link's destination node
     * @return the specified link from the PhysicalTopology's adjMatrix
     */
    public Link getLink(int src, int dst) {
        return adjMatrix[src][dst];
    }

    /**
     * Retrives a given PhysicalTopology's adjancency matrix, which contains the
     * links between source and destination nodes.
     *
     * @return the PhysicalTopology's adjMatrix
     */
    public Link[][] getAdjMatrix() {
        return adjMatrix;
    }
    
    /**
     * Says whether exists or not a link between two given nodes.
     *
     * @param node1 possible link's source node
     * @param node2 possible link's destination node
     * @return true if the link exists in the PhysicalTopology's adjMatrix
     */
    public boolean hasLink(int node1, int node2) {
        return adjMatrix[node1][node2] != null;
    }

    /**
     * Checks if a path made of links makes sense by checking its continuity
     *
     * @param links to be checked
     * @return true if the link exists in the PhysicalTopology's adjMatrix
     */
    public boolean checkLinkPath(int links[]) {
        for (int i = 0; i < links.length - 1; i++) {
            if (!(getLink(links[i]).dst == getLink(links[i + 1]).src)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a weighted graph with vertices, edges and weights representing
     * the physical network nodes, links and weights implemented by this class
     * object.
     *
     * @return an WeightedGraph class object
     */
    public WeightedGraph getWeightedGraph() {
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (hasLink(i, j)) {
                    g.addEdge(i, j, getLink(i, j).getWeight());
                }
            }
        }
        return g;
    }
    
    /**
     * Returns the weighted graph with the representation of transponders [Tx/Rx] available in OXC.
     * Only node, without edges
     * 
     * @return the WeightedGraph
     */
    public WeightedGraph getTransponderGraph(){
        WeightedGraph g = null;
        int numNodes = 0;
        for(int i = 0; i < nodes; i++){
            if (nodeVector[i].hasFreeGroomingInputPort()){
                numNodes++;
            }
            if (nodeVector[i].hasFreeGroomingOutputPort()){
                numNodes++;
            }
        }
        if(numNodes > 0){
            g = new WeightedGraph(numNodes);
        }
        return g;
    }

    /**
     *
     *
     */
    public void printXpressInputFile() {

        // Edges
        System.out.println("EDGES: [");
        for (int i = 0; i < this.getNumNodes(); i++) {
            for (int j = 0; j < this.getNumNodes(); j++) {
                if (this.hasLink(i, j)) {
                    System.out.println("(" + Integer.toString(i + 1) + " " + Integer.toString(j + 1) + ") 1");
                } else {
                    System.out.println("(" + Integer.toString(i + 1) + " " + Integer.toString(j + 1) + ") 0");
                }
            }
        }
        System.out.println("]");
        System.out.println();

        // SD Pairs
        System.out.println("TRAFFIC: [");
        for (int i = 0; i < this.getNumNodes(); i++) {
            for (int j = 0; j < this.getNumNodes(); j++) {
                if (i != j) {
                    System.out.println("(" + Integer.toString(i + 1) + " " + Integer.toString(j + 1) + ") 1");
                } else {
                    System.out.println("(" + Integer.toString(i + 1) + " " + Integer.toString(j + 1) + ") 0");
                }
            }
        }
        System.out.println("]");
    }

    /**
     * Retrieves all Datacenters Groups in this Topology
     * @return all DatacentersGroup objects
     */
    protected DatacenterGroup[] getDatacentersGroup() {
        return datacenters;
    }
    
    /**
     * Retrieves a especific Datacenters Group in this Topology
     * @return the DatacentersGroup object
     */
    protected DatacenterGroup getDatacenterGroup(int index) {
        return datacenters[index];
    }
    
    /**
     * Prints all nodes and links between them in the PhysicalTopology object.
     *
     * @return string containing the PhysicalTopology's adjMatrix values
     */
    @Override
    public String toString() {
        String topo = "";
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (adjMatrix[i][j] != null) {
                    topo += adjMatrix[i][j].toString() + "\n\n";
                }
            }
        }
        return topo;
    }

    public abstract void createPhysicalLightpath(LightPath lp);

    public abstract void removePhysicalLightpath(LightPath lp);

    public abstract boolean canCreatePhysicalLightpath(LightPath lp);
    
    public abstract int getBW(LightPath lp);

    public abstract int getBWAvailable(LightPath lp);

    public abstract boolean canAddRate(int rate, LightPath lightpath);

    public abstract void addRate(int rate, LightPath lightpath);

    public abstract void removeRate(int rate, LightPath lightpath);
    
}
