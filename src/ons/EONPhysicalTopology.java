package ons;

import ons.util.WeightedGraph;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class EONPhysicalTopology extends PhysicalTopology {

    private static int slotSize;
    private static int maxModulation;
    protected static PhysicalImpairments pi;
    public static Map<Integer, Double> valueA;
    public static Map<Integer, BigInteger> valueSi;

    public EONPhysicalTopology(Element xml, PhysicalImpairments pi) {
        super(xml);
        this.pi = pi;
        maxModulation = 0;
        valueA = new HashMap<Integer, Double>();
        valueSi = new HashMap<Integer, BigInteger>();

        int id, type, group;
        int groomingInPorts, groomingOutPorts, defaultCapacity = 0, maxCapacity = 0;
        int[] defaultModulations = new int[Modulation.N_MOD];
        boolean generalModulation = false;
        double delay, weight;
        String[] parts;

        try {
            // Checking the atributtes of <nodes> tag for general values
            NodeList nodesEntities = xml.getElementsByTagName("nodes");
            if (((Element) nodesEntities.item(0)).hasAttribute("modulations")) {
                generalModulation = true;
                for (int i = 0; i < defaultModulations.length; i++) {
                    defaultModulations[i] = 0;
                }
                parts = (((Element) nodesEntities.item(0)).getAttribute("modulations").split(",[ ]*"));
                for (String part : parts) {
                    int modulationIndex = Modulation.convertModulationTypeToInteger(part);
                    if (modulationIndex != -1) {
                        defaultModulations[modulationIndex] = 1;
                    } else {
                        throw (new IllegalArgumentException("Modulation type not recognized: " + part));
                    }
                }
                for (int i = defaultModulations.length - 1; i >= 0; i--) {
                    if (defaultModulations[i] == 1 && i > maxModulation) {
                        maxModulation = i;
                        break;
                    }
                }
            }
            if (((Element) nodesEntities.item(0)).hasAttribute("capacity")) {
                defaultCapacity = Integer.parseInt(((Element) nodesEntities.item(0)).getAttribute("capacity"));
            }
            // Process nodes
            NodeList nodelist = xml.getElementsByTagName("node");
            nodes = nodelist.getLength();
            if (Simulator.verbose) {
                System.out.println(Integer.toString(nodes) + " nodes");
            }
            nodeVector = new EONOXC[nodes];
            for (int i = 0; i < nodes; i++) {
                id = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("id"));
                groomingInPorts = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("grooming-in-ports"));
                groomingOutPorts = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("grooming-out-ports"));
                
                if(((Element) nodelist.item(i)).hasAttribute("type")) {
                    type = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("type"));
                } else {
                    type = 0; //dont have a type, conventional type
                }
                if(((Element) nodelist.item(i)).hasAttribute("group")) {
                    group = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("group"));
                } else {
                    group = -1; //dont have a group
                }
                
                int[] modulations = new int[Modulation.N_MOD];
                if (((Element) nodelist.item(i)).hasAttribute("modulations")) {
                    for (int j = 0; j < defaultModulations.length; j++) {
                        modulations[j] = 0;
                    }
                    parts = (((Element) nodelist.item(i)).getAttribute("modulations").split(",[ ]*"));
                    for (String part : parts) {
                        int modulationIndex = Modulation.convertModulationTypeToInteger(part);
                        if (modulationIndex != -1) {
                            modulations[modulationIndex] = 1;
                        } else {
                            throw (new IllegalArgumentException("Modulation type not recognized: " + part));
                        }
                    }
                    for (int j = modulations.length - 1; j >= 0; j--) {
                        if (modulations[j] == 1 && j > maxModulation) {
                            maxModulation = j;
                            break;
                        }
                    }
                } else {
                    if(generalModulation) {
                        modulations = defaultModulations;
                    } else {
                        throw (new IllegalArgumentException("modulations is not set for all nodes and general modulations is not set. Check the xml."));
                    }
                }
                int capacity = 0;
                if (((Element) nodelist.item(i)).hasAttribute("capacity")) {
                    capacity = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("capacity"));
                } else {
                    if(defaultCapacity != 0) {
                        capacity = defaultCapacity;
                    } else {
                        throw (new IllegalArgumentException("capacity is not set for all nodes and general capacity is not set. Check the xml."));
                    }
                }
                if(maxCapacity < capacity) {
                    maxCapacity = capacity;
                }
                nodeVector[id] = new EONOXC(id, groomingInPorts, groomingOutPorts, type, group, capacity, modulations);
            }
            
            if (((Element) xml.getElementsByTagName("dcs_group").item(0)) != null) {
                int idDC;
                NodeList datacenterEntities = xml.getElementsByTagName("datacenter");
                if(datacenterEntities.item(0) == null) {
                    throw (new IllegalArgumentException("dcs_group tag did not has datacenter tag. Check the xml."));
                }
                int numberOfDatacentersGroups = datacenterEntities.getLength();
                datacenters = new DatacenterGroup[numberOfDatacentersGroups];
                if (Simulator.verbose) {
                    System.out.println(Integer.toString(numberOfDatacentersGroups) + " datacenters");
                }
                for (int i = 0; i < numberOfDatacentersGroups; i++) {
                    idDC = Integer.parseInt(((Element) datacenterEntities.item(i)).getAttribute("id"));
                    datacenters[idDC] = new DatacenterGroup(idDC);
                    parts = (((Element) datacenterEntities.item(i)).getAttribute("nodes").split(",[ ]*"));
                    for (String part : parts) {
                        int DCid = Integer.parseInt(part);
                        datacenters[idDC].addMember(DCid);
                    }
                }
            }
            int src, dst, defaultSlots = 0, maxSlots = 0, defaultGuardband = 0, defaultCores = 1;
            EONPhysicalTopology.slotSize = 0;
            // Checking the atributtes of <links> tag for general values
            NodeList linksEntities = xml.getElementsByTagName("links");
            if (((Element) linksEntities.item(0)).hasAttribute("slots")) {
                defaultSlots = Integer.parseInt(((Element) linksEntities.item(0)).getAttribute("slots"));
            }
            if (((Element) linksEntities.item(0)).hasAttribute("guardband")) {
                defaultGuardband = Integer.parseInt(((Element) linksEntities.item(0)).getAttribute("guardband"));
            }
            if (((Element) linksEntities.item(0)).hasAttribute("slot-size")) {
                EONPhysicalTopology.slotSize = Integer.parseInt(((Element) linksEntities.item(0)).getAttribute("slot-size"));
            }
            if (((Element) linksEntities.item(0)).hasAttribute("cores")) {
                defaultCores = Integer.parseInt(((Element) linksEntities.item(0)).getAttribute("cores"));
                if(!(defaultCores == 1 || defaultCores == 3 || defaultCores == 7 || defaultCores == 13 || defaultCores == 19)) {
                        throw (new IllegalArgumentException("cores must be 1,3,7,13 or 19. Check the xml."));
                }
            }
            // Process links
            NodeList linklist = xml.getElementsByTagName("link");
            links = linklist.getLength();
            if (Simulator.verbose) {
                System.out.println(Integer.toString(links) + " links");
            }
            linkVector = new Link[links];
            adjMatrix = new Link[nodes][nodes];
            for (int i = 0; i < links; i++) {
                id = Integer.parseInt(((Element) linklist.item(i)).getAttribute("id"));
                src = Integer.parseInt(((Element) linklist.item(i)).getAttribute("source"));
                dst = Integer.parseInt(((Element) linklist.item(i)).getAttribute("destination"));
                delay = Double.parseDouble(((Element) linklist.item(i)).getAttribute("delay"));
                weight = Double.parseDouble(((Element) linklist.item(i)).getAttribute("weight"));
                int slots = 0;
                if (((Element) linklist.item(i)).hasAttribute("slots")) {
                    slots = Integer.parseInt(((Element) linklist.item(i)).getAttribute("slots"));
                } else {
                    if(defaultSlots != 0) {
                        slots = defaultSlots;
                    } else {
                        throw (new IllegalArgumentException("slots is not set for all links and general slots is not set. Check the xml."));
                    }
                }
                if(maxSlots < slots) {
                    maxSlots = slots;
                }
                int guardband = 0;
                if (((Element) linklist.item(i)).hasAttribute("guardband")) {
                    guardband = Integer.parseInt(((Element) linklist.item(i)).getAttribute("guardband"));
                } else {
                    if(defaultGuardband != 0) {
                        guardband = defaultGuardband;
                    } else {
                        throw (new IllegalArgumentException("guardband is not set for all links and general guardband is not set. Check the xml."));
                    }
                }
                int cores = 1;
                if (((Element) linklist.item(i)).hasAttribute("cores")) {
                    cores = Integer.parseInt(((Element) linklist.item(i)).getAttribute("cores"));
                    if(!(cores == 1 || cores == 3 || cores == 7 || cores == 13 || cores == 19)) {
                        throw (new IllegalArgumentException("cores must be 1,3,7,13 or 19. Check the xml."));
                    }
                } else {
                    if(defaultCores == 3 || defaultCores == 7 || defaultCores == 13 || defaultCores == 19) {
                        cores = defaultCores;
                    } else {
                        cores = 1;
                    } 
                }
                if(cores == 1) {
                    linkVector[id] = adjMatrix[src][dst] = new EONLink(id, src, dst, delay, weight, slots, guardband);
                } else {
                    linkVector[id] = adjMatrix[src][dst] = new EONLink(id, src, dst, delay, weight, slots, guardband, cores);
                }
            }

            // Set node degree
            WeightedGraph g = getWeightedGraph();
            for (int i = 0; i < nodeVector.length; i++) {
                nodeVector[i].setNodeDegree(g.neighbors(i).length);
            }
            
            for (int i = 0; i <= maxSlots; i++) {
                valueA.put(i, A(i, maxCapacity) * 0.001);
            }
            for (int i = 0; i <= maxSlots; i++) {
                valueSi.put(i, (BigInteger) (BigFactorial(i + 2)));
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    /**
     * Retrieves the 
     * @return 
     */
    public PhysicalImpairments getPI() {
        return this.pi;
    }
    
    /**
     * Retrieves the slot size in MHz.
     *
     * @return slot size in MHz
     */
    public static int getSlotSize() {
        return slotSize;
    }

    /**
     * Retrieves the max modulation format in xml schema.
     *
     * @return the id of max modulation format allowed
     */
    public static int getMaxModulation() {
        return maxModulation;
    }

    /**
     * Allocates optical path network.
     *
     * @param lightpath the lightpath will be alocated
     */
    @Override
    public void createPhysicalLightpath(LightPath lightpath) {
        for (int i = 0; i < lightpath.links.length; i++) {
            int core = ((EONLightPath) lightpath).getCores()[i];
            ((EONLink) linkVector[lightpath.links[i]]).reserveSlots(core, lightpath.id, ((EONLightPath) lightpath).getFirstSlot(), ((EONLightPath) lightpath).getLastSlot());
        }
        //Set the transponder used in this lp and Reserve ports
        lightpath.setTx(this.getNode(this.getLink(lightpath.links[0]).getSource()).reserveGroomingInputPort());
        lightpath.setRx(this.getNode(this.getLink(lightpath.links[lightpath.links.length - 1]).getDestination()).reserveGroomingOutputPort());
        //seta o SRN do lightptah
        if (pi.isSNRaware() || pi.isCheckQoT()) {
            pi.computeSNR(lightpath);
        }
    }

    /**
     * Allocates optical path with optical grooming technique.
     *
     * @param lightpath the lightpath will be alocated
     */
    public void createPhysicalLightpathInOpticalGrooming(LightPath lightpath) {
        for (int i = 0; i < lightpath.links.length; i++) {
            int core = ((EONLightPath) lightpath).getCores()[i];
            ((EONLink) linkVector[lightpath.links[i]]).reserveSlotsInOpticalGrooming(core, lightpath.id, ((EONLightPath) lightpath).getFirstSlot(), ((EONLightPath) lightpath).getLastSlot());
        }
        if (pi.isSNRaware() || pi.isCheckQoT()) {
            pi.computeSNR(lightpath);
        }
    }

    /**
     * Allocates optical path with optical grooming technique.
     *
     * @param lpShared part of lightpath to be shared in optical grooming
     * technique
     * @param lpNew part of lightpath to be not used optical grooming technique
     * @param dstNode the destination node of the new lightpath
     * @return the lightpath created
     */
    public LightPath createPhysicalLightpathHybrid(LightPath lpShared, LightPath lpNew, int dstNode) {
        LightPath lpCreated;
        int[] linksLightpath;
        if (lpNew != null) {
            linksLightpath = new int[lpShared.links.length + lpNew.links.length];
            int i = 0;
            for (int j = 0; j < lpShared.links.length; j++) {
                linksLightpath[i] = lpShared.links[j];
                i++;
            }
            for (int j = 0; j < lpNew.links.length; j++) {
                linksLightpath[i] = lpNew.links[j];
                i++;
            }
        } else {
            linksLightpath = lpShared.links;
        }

        lpCreated = new EONLightPath(lpShared.id, lpShared.getSource(), dstNode, linksLightpath,
                ((EONLightPath) lpShared).getFirstSlot(), ((EONLightPath) lpShared).getLastSlot(),
                ((EONLightPath) lpShared).getModulation(), slotSize);
        
        for (int i = 0; i < lpShared.links.length; i++) {
            int core = ((EONLightPath) lpShared).getCores()[i];
            ((EONLink) linkVector[lpShared.links[i]]).reserveSlotsInOpticalGrooming(core, lpShared.id, ((EONLightPath) lpShared).getFirstSlot(), ((EONLightPath) lpShared).getLastSlot());
        }
        if (lpNew != null) {
            for (int i = 0; i < lpNew.links.length; i++) {
                int core = ((EONLightPath) lpNew).getCores()[i];
                ((EONLink) linkVector[lpNew.links[i]]).reserveSlots(core, lpNew.id, ((EONLightPath) lpNew).getFirstSlot(), ((EONLightPath) lpNew).getLastSlot());
            }
        }
        //Set the transponder used in this lp and Reserve only Rx port
        lpCreated.setTx(lpShared.getTx());
        lpCreated.setRx(this.getNode(dstNode).reserveGroomingOutputPort());
        if (pi.isSNRaware() || pi.isCheckQoT()) {
            pi.computeSNR(lpCreated);
        }
        return lpCreated;
    }

    /**
     * Examine whether it is possible to allocate the supplied optical path.
     *
     * @param lightpath the optical path
     * @return true if is possible, false otherwise
     */
    @Override
    public boolean canCreatePhysicalLightpath(LightPath lightpath) {
        //check continuity
        if (!checkLinkPath(lightpath.links)) {
            return false;
        }
        //modulation check
        if (!((((EONOXC) nodeVector[lightpath.getSource()]).hasModulation(((EONLightPath) lightpath).getModulation()))
                && (((EONOXC) nodeVector[lightpath.getDestination()]).hasModulation(((EONLightPath) lightpath).getModulation())))) {
            return false;
        }
        // Available transceivers
        if (!getNode(getLink(lightpath.links[0]).getSource()).hasFreeGroomingInputPort()) {
            return false;
        }
        if (!getNode(getLink(lightpath.links[lightpath.links.length - 1]).getDestination()).hasFreeGroomingOutputPort()) {
            return false;
        }
        //source transceiver capacity test
        if (((EONOXC) nodeVector[lightpath.getSource()]).getCapacity() < (((EONLightPath) lightpath).getLastSlot() - ((EONLightPath) lightpath).getFirstSlot() + 1)) {
            return false;
        }
        //slot check
        //check if each link has these required slots
        for (int i = 0; i < lightpath.links.length; i++) {
            int core = ((EONLightPath) lightpath).getCores()[i];
            if (!(((EONLink) linkVector[lightpath.links[i]]).areSlotsAvaiable(core, ((EONLightPath) lightpath).getFirstSlot(), ((EONLightPath) lightpath).getLastSlot()))) {
                return false;
            }
        }
        //check the SNR of the lightpath and the other lps that came from the overload
        if (pi.isSNRaware() && !pi.testSNR(lightpath)) {
            return false;
        }
        if (pi.isXTaware()) {
            if(!pi.checkCrosstalk((EONLightPath) lightpath)) {
                return false;
            }
        }
        if(pi.isXTonOthers()) {
            if(!pi.checkXTNeighbors((EONLightPath) lightpath)) {
                return false;
            }
        }
        if (pi.isPhysicalDistance()) {
            if (!modulationPath(lightpath)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Deallocates optical path provided.
     *
     * @param lightpath the optical path provided
     */
    @Override
    public void removePhysicalLightpath(LightPath lightpath) {
        for (int i = 0; i < lightpath.links.length; i++) {
            int core = ((EONLightPath) lightpath).getCores()[i];
            ((EONLink) linkVector[lightpath.links[i]]).releaseSlots(core, ((EONLightPath) lightpath).getFirstSlot(), ((EONLightPath) lightpath).getLastSlot());
        }
        // Release ports
        this.getNode(lightpath.getSource()).releaseGroomingInputPort(lightpath.Tx);
        this.getNode(lightpath.getDestination()).releaseGroomingOutputPort(lightpath.Rx);
    }

    /**
     * Deallocates optical path provided in optical grooming.
     *
     * @param lightpath the optical path provided
     */
    public void removePhysicalLightpathInOpticalGrooming(LightPath lightpath) {
        for (int i = 0; i < lightpath.links.length; i++) {
            int core = ((EONLightPath) lightpath).getCores()[i];
            if (((EONLink) linkVector[lightpath.links[i]]).isOpticalGrooming(core, lightpath.id)) {
                ((EONLink) linkVector[lightpath.links[i]]).releaseSlotsInOpticalGrooming(core, ((EONLightPath) lightpath).getFirstSlot(), ((EONLightPath) lightpath).getLastSlot());
            } else {
                ((EONLink) linkVector[lightpath.links[i]]).releaseSlots(core, ((EONLightPath) lightpath).getFirstSlot(), ((EONLightPath) lightpath).getLastSlot());
            }
        }
    }

    /**
     * Deallocates optical path provided in partly optical grooming.
     *
     * @param lightpath the lightpath
     */
    public void removePhysicalLightpathHybrid(LightPath lightpath) {
        for (int i = 0; i < lightpath.links.length; i++) {
            int core = ((EONLightPath) lightpath).getCores()[i];
            if (((EONLink) linkVector[lightpath.links[i]]).isOpticalGrooming(core, lightpath.id)) {
                ((EONLink) linkVector[lightpath.links[i]]).releaseSlotsInOpticalGrooming(core, ((EONLightPath) lightpath).getFirstSlot(), ((EONLightPath) lightpath).getLastSlot());
            } else {
                ((EONLink) linkVector[lightpath.links[i]]).releaseSlots(core, ((EONLightPath) lightpath).getFirstSlot(), ((EONLightPath) lightpath).getLastSlot());
            }
        }
        // Release Rx port
        getNode(lightpath.getDestination()).releaseGroomingOutputPort(lightpath.Rx);
    }

    /**
     * Retrieves the bandwidth available in Mbps in this lightpath
     *
     * @param lightpath the lightpath to be examined
     * @return the bandwidth available
     */
    @Override
    public int getBWAvailable(LightPath lightpath) {
        return ((EONLightPath) lightpath).getBwAvailable();
    }

    /**
     * Add flow in this lightpath.
     *
     * @param rate the rate to be add
     * @param lightpath the lightpath
     */
    @Override
    public void addRate(int rate, LightPath lightpath) {
        ((EONLightPath) lightpath).addFlowOnLightPath(rate);
    }

    /**
     * Retrieves the total bandwidth this lightpath
     *
     * @param lightpath the lightpath
     * @return the bandwidth in Mbps
     */
    @Override
    public int getBW(LightPath lightpath) {
        return ((EONLightPath) lightpath).getBw();
    }

    /**
     * Returns a weighted graph with vertices representing the physical network
     * nodes, and the edges representing the physical links.
     *
     * The weight of each edge receives the same value of the original link
     * weight if the link has at least slots available. Otherwise it has no
     * edges.
     *
     * @param slots the flow rate are available in slots
     * @return an WeightedGraph class object
     */
    public WeightedGraph getWeightedGraph(int slots) {
        EONLink link;
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (hasLink(i, j)) {
                    link = (EONLink) getLink(i, j);
                    if (link.maxSizeAvaiable() >= slots) {
                        g.addEdge(i, j, link.getWeight());
                    }
                }
            }
        }
        return g;
    }

    /**
     * Examine whether it is possible to extend the lightpath with the number of
     * slots provided.
     *
     * @param requiredSlots the slots provided
     * @param lpGroomable the lightpath groomable
     * @param tunnelSize the size of lpGroomable tunnel
     * @return true if lightpath can extend to the right or left, false
     * otherwise
     */
    public boolean canExtendLightpath(int requiredSlots, LightPath lpGroomable, int tunnelSize) {
        boolean l_flag = true, r_flag = true;
        // Available capacity
        if (tunnelSize + requiredSlots > ((EONOXC) nodeVector[lpGroomable.getSource()]).getCapacity()) {
            return false;
        }
        for (int i = 0; i < lpGroomable.links.length; i++) {
            int core = ((EONLightPath) lpGroomable).getCores()[i];
            if (l_flag) {
                if (!((EONLink) linkVector[lpGroomable.links[i]]).leftExtend(core, requiredSlots, lpGroomable.id)) {
                    l_flag = false;
                }
            }
            if (r_flag) {
                if (!((EONLink) linkVector[lpGroomable.links[i]]).rightExtend(core, requiredSlots, lpGroomable.id)) {
                    r_flag = false;
                }
            }
        }
        return l_flag || r_flag;
    }

    /**
     * Examine whether it is possible to extend the lightpath to left with the
     * number of slots provided.
     *
     * @param requiredSlots the slots provided
     * @param lpGroomable the lightpath groomable
     * @param tunnelSize the size of lpGroomable tunnel
     * @return true if lightpath can extend to the left, false otherwise
     */
    public boolean canExtendLightpathToLeft(int requiredSlots, LightPath lpGroomable, int tunnelSize) {
        boolean l_flag = true;
        // Available capacity
        if (tunnelSize + requiredSlots > ((EONOXC) nodeVector[lpGroomable.getSource()]).getCapacity()) {
            return false;
        }
        for (int i = 0; i < lpGroomable.links.length; i++) {
            int core = ((EONLightPath) lpGroomable).getCores()[i];
            if (l_flag) {
                if (!((EONLink) linkVector[lpGroomable.links[i]]).leftExtend(core, requiredSlots, lpGroomable.id)) {
                    l_flag = false;
                }
            }
        }
        return l_flag;
    }

    /**
     * Examine whether it is possible to extend the lightpath to right with the
     * number of slots provided.
     *
     * @param requiredSlots the slots provided
     * @param lpGroomable the lightpath groomable
     * @param tunnelSize the size of lpGroomable tunnel
     * @return true if lightpath can extend to the right, false otherwise
     */
    public boolean canExtendLightpathToRight(int requiredSlots, LightPath lpGroomable, int tunnelSize) {
        boolean r_flag = true;
        // Available capacity
        if (tunnelSize + requiredSlots > ((EONOXC) nodeVector[lpGroomable.getSource()]).getCapacity()) {
            return false;
        }
        for (int i = 0; i < lpGroomable.links.length; i++) {
            int core = ((EONLightPath) lpGroomable).getCores()[i];
            if (r_flag) {
                if (!((EONLink) linkVector[lpGroomable.links[i]]).rightExtend(core, requiredSlots, lpGroomable.id)) {
                    r_flag = false;
                }
            }
        }
        return r_flag;
    }

    /**
     * Examine whether it is possible optical grooming in this lightpaths.
     *
     * @param lpCreated the lightpath will be alocate
     * @param lpGroomable one lightpath alocated
     * @param tunnelSize the size of lpGroomable tunnel
     * @return true if lightpaths can be uses optical grooming, false otherwise
     */
    public boolean canExtendLightpath(LightPath lpCreated, LightPath lpGroomable, int tunnelSize) {
        //teste de fonte
        if (lpCreated.src != lpGroomable.src) {
            return false;
        }
        //check continuity
        if (!checkLinkPath(lpCreated.links)) {
            return false;
        }
        //modulation check
        if (!((((EONOXC) nodeVector[lpCreated.getSource()]).hasModulation(((EONLightPath) lpCreated).getModulation()))
                && (((EONOXC) nodeVector[lpCreated.getDestination()]).hasModulation(((EONLightPath) lpCreated).getModulation())))) {
            return false;
        }
        //if both have the same modulation
        if (((EONLightPath) lpCreated).getModulation() != ((EONLightPath) lpGroomable).getModulation()) {
            return false;
        }
        int requiredSlots = ((EONLightPath) lpCreated).getSlots();

        // Available capacity
        if (tunnelSize + requiredSlots > ((EONOXC) nodeVector[lpCreated.getSource()]).getCapacity()) {
            return false;
        }
        // Available slots
        if (((EONLightPath) lpCreated).getFirstSlot() > ((EONLightPath) lpGroomable).getLastSlot()) {
            for (int i = 0; i < lpCreated.links.length; i++) {
                int core = ((EONLightPath) lpCreated).getCores()[i];
                if (!((EONLink) linkVector[lpCreated.links[i]]).rightExtend(core, requiredSlots, lpGroomable.id)) {
                    return false;
                }
            }
        } else {
            for (int i = 0; i < lpCreated.links.length; i++) {
                int core = ((EONLightPath) lpCreated).getCores()[i];
                if (!((EONLink) linkVector[lpCreated.links[i]]).leftExtend(core, requiredSlots, lpGroomable.id)) {
                    return false;
                }
            }
        }
        //check the SNR of the lightpath and the other lps that came from the overload
        if (pi.isSNRaware() && !pi.testSNR(lpCreated)) {
            return false;
        }
        if (pi.isXTaware()) {
            if(!pi.checkCrosstalk((EONLightPath) lpCreated)){
                return false;
            }
        }
        if(pi.isXTonOthers()) {
            if(!pi.checkXTNeighbors((EONLightPath) lpCreated)) {
                return false;
            }
        }
        if (pi.isPhysicalDistance()) {
            if (!modulationPath(lpCreated)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Examine whether it is possible optical grooming in this lightpaths
     * ('lpShared' and 'lpGroomable') and examine whether it is possible to
     * allocate the 'lpNew'.
     *
     * @param lpShared the lightpath will be alocate
     * @param lpGroomable one lightpath alocated
     * @param tunnelSize the size of lpGroomable tunnel
     * @param lpNew the optical path
     * @param dstNode the destination node of this new lightpath
     * @return true if is possible, false otherwise
     */
    public boolean canCreatePhysicalLightpathHybrid(LightPath lpShared, LightPath lpGroomable, int tunnelSize, LightPath lpNew, int dstNode) {
        if (canExtendLightpath(lpShared, lpGroomable, tunnelSize)) {
            //Agora testa o lpNew:
            if (lpNew != null) {
                //check continuity
                if (!checkLinkPath(lpNew.links)) {
                    return false;
                }
                //if both have the same modulation
                if (((EONLightPath) lpShared).getModulation() != ((EONLightPath) lpNew).getModulation()) {
                    return false;
                }
                // Available Receiver
                if (!getNode(getLink(lpNew.links[lpNew.links.length - 1]).getDestination()).hasFreeGroomingOutputPort()) {
                    return false;
                }
                //slot test
                //check if each link has these required slots
                for (int i = 0; i < lpNew.links.length; i++) {
                    int core = ((EONLightPath) lpNew).getCores()[i];
                    if (!(((EONLink) linkVector[lpNew.links[i]]).areSlotsAvaiable(core, ((EONLightPath) lpNew).getFirstSlot(), ((EONLightPath) lpNew).getLastSlot()))) {
                        return false;
                    }
                }
                //check the SNR of the lightpath and the other lps that came from the overload
                if (pi.isSNRaware() && !pi.testSNR(lpNew)) {
                    return false;
                }
                //TODO
                //o lp new ta incompleto ele precisa ta junto do shared
                
                LightPath lpCreated;
                int[] linksLightpath;
                linksLightpath = new int[lpShared.links.length + lpNew.links.length];
                int i = 0;
                for (int j = 0; j < lpShared.links.length; j++) {
                    linksLightpath[i] = lpShared.links[j];
                    i++;
                }
                for (int j = 0; j < lpNew.links.length; j++) {
                    linksLightpath[i] = lpNew.links[j];
                    i++;
                }
                lpCreated = new EONLightPath(lpShared.id, lpShared.getSource(), dstNode, linksLightpath,
                        ((EONLightPath) lpShared).getFirstSlot(), ((EONLightPath) lpShared).getLastSlot(),
                        ((EONLightPath) lpShared).getModulation(), slotSize);
                
                if (pi.isXTaware()) {
                    if (!pi.checkCrosstalk((EONLightPath) lpCreated)) {
                        return false;
                    }
                }
                if (pi.isXTonOthers()) {
                    if (!pi.checkXTNeighbors((EONLightPath) lpCreated)) {
                        return false;
                    }
                }
                if (pi.isPhysicalDistance()) {
                    if (!modulationPath(lpCreated)) {
                        return false;
                    }
                }
                
                return true;
            } else {
                // Available Receiver
                return getNode(dstNode).hasFreeGroomingOutputPort();
            }
        }
        return false;
    }

    /**
     * Examine whether it is possible to add flow in the lightpath.
     *
     * @param rate the rate to be add
     * @param lightpath the lightpath
     * @return true if is possible, false otherwise
     */
    @Override
    public boolean canAddRate(int rate, LightPath lightpath) {
        return ((EONLightPath) lightpath).getBwAvailable() >= rate;
    }

    /**
     * Remove the rate (in Mbps) of this litghpath.
     *
     * @param rate the rate to be removed
     * @param lightpath the lightpath
     */
    @Override
    public void removeRate(int rate, LightPath lightpath) {
        ((EONLightPath) lightpath).removeFlowOnLightPath(rate);
    }

    /**
     * Retrieves the slots available in all physical topology links.
     *
     * @return the number of slots available
     */
    public int getAvailableSlots() {
        int slots = 0;
        for (int i = 0; i < links; i++) {
            slots = slots + ((EONLink) this.getLink(i)).getAvaiableSlots();
        }
        return slots;
    }
    
    /**
     * Retrieves the weight of this lightpath's links
     * @param lp the lightpath
     * @return the weight of this lightpath's links
     */
    private double getLightPathWeight(LightPath lp) {
        if (getLink(lp.getLinks()[0]).getSource() != lp.getSource()
                || getLink(lp.getLinks()[lp.getLinks().length - 1]).getDestination() != lp.getDestination()) {
            throw (new IllegalArgumentException());
        }
        double weight = 0.0;
        for (int i = 0; i < lp.getLinks().length; i++) {
            weight += getLink(lp.getLinks()[i]).getWeight();
        }
        return weight;
    }
    
    /**
     * Verifies whether the modulation' lightpaths serves all sizes of lightpaths.
     * Only for EON Simulator
     * @param lps the lightptahs
     * @return true if all candidates lightpaths serves all sizes of lightpaths, false otherwise
     */
    private boolean modulationPath(LightPath lp) {
        if (lp instanceof EONLightPath) {
            if (!(getLightPathWeight(lp) <= (double) Modulation.getModulationReach(((EONLightPath) lp).getModulation()))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Retrieves the slots avaiable in this route.
     * @param links the links in this route.
     * @param requiredSlots the slots required
     * @return the array with the merge slots avaiable in all links
     */
    public int[] getAvaiableSlotsRoute(int[] links, int requiredSlots) {
        int[] slot = ((EONLink) getLink(links[0])).firstFit(requiredSlots);
        boolean flag;
        ArrayList<Integer> out  = new ArrayList<>();
        for (int s = 0; s < slot.length; s++) {
            flag = true;
            for (int i = 1; i < links.length; i++) {
                if(!((EONLink) getLink(links[i])).areSlotsAvaiable(slot[s], slot[s] + requiredSlots - 1)) {
                    flag = false;
                }
            }
            if(flag) {
                out.add(slot[s]);
            }
        }
        return arrayListToArray(out);
    }
    
    /**
     * Retrieves the slots avaiable in this SDM route. 
     * @param links the links in this route.
     * @param requiredSlots the slots required
     * @return the array with the merge slots avaiable in all links
     */
    public int[] getAvaiableSlotsSDMRoute(int[] links, int requiredSlots) {
        
        TreeSet<Integer> unionCoresFirstLink = new TreeSet<>();
        int[] cores = new int[((EONLink) getLink(links[0])).getNumCores()];
        boolean flag1, flag2;
        //first, joins the allocable slots of all cores in the first link
        for (int c = 0; c < cores.length; c++) {
            unionCoresFirstLink.addAll(((EONLink) getLink(links[0])).getSlotsAvailable(c, requiredSlots));
        }
        Integer[] slots = unionCoresFirstLink.toArray(new Integer[0]);
        ArrayList<Integer> out  = new ArrayList<>();
        //then intersects the allocable slots of the first link with the next links for all cores
        for (Integer slot : slots) {
            flag1 = true;
            for (int i = 1; i < links.length; i++) {
                flag2 = false;
                cores = new int[((EONLink) getLink(links[i])).getNumCores()];
                for (int c = 0; c < cores.length; c++) {
                    if (((EONLink) getLink(links[i])).areSlotsAvaiable(c, slot, slot + requiredSlots - 1)) {
                        flag2 = true;
                        break;
                    } 
                }
                if (!flag2) {
                    flag1 = false;
                }
            }
            if(flag1) {
                out.add(slot);
            }
        }
        return arrayListToArray(out);
    }
    
    /**
     * Convert an ArrayList Object to array (int)
     * @param a
     * @return the int array
     */
    private int[] arrayListToArray(ArrayList<Integer> a) {
        int[] out = null;
        if (a == null) {
            return null;
        } else {
            out = new int[a.size()];
            for (int i = 0; i < a.size(); i++) {
                out[i] = a.get(i);
            }
        }
        return out;
    }
    
    /**
     * Calculate the factorial of big numbers.
     *
     * @param b the number.
     * @return the BigInteher object of b!
     */
    private BigInteger BigFactorial(int b) {
        if (valueSi.containsKey(b)) {
            return valueSi.get(b);
        }
        if (BigInteger.ONE.equals(BigInteger.valueOf(b))
                || BigInteger.ZERO.equals(BigInteger.valueOf(b))) {
            return BigInteger.ONE;
        } else {
            return BigInteger.valueOf(b).multiply(BigFactorial(b - 1));
        }
    }

    /**
     * Retrieves the current fragmentation rate in this topology.
     *
     * @return the current fragmentation of topology,
     * (1-largestFreeBlock/TotalFree) in each link
     */
    public double getExFragmentation() {
        double frag = 0;
        for (int i = 0; i < links; i++) {
            frag = frag + ((EONLink) this.getLink(i)).getExFragmentationRate();
        }
        return frag / (double) links;
    }
    
    /**
     * Retrieves the current fragmentation rate in this links.
     *
     * @param links the links
     * @return the current fragmentation of topology,
     * (1-largestFreeBlock/TotalFree) in each link
     */
    public double getExFragmentation(int[] links) {
        double frag = 0;
        for (int i = 0; i < links.length; i++) {
            frag = frag + ((EONLink) this.getLink(links[i])).getExFragmentationRate();
        }
        return frag / (double) links.length;
    }

    /**
     * Retrieves the current fragmentation rate in this topology in accordance
     * with Wu et al 2014. J. Wu, M. Zhang, F. Wang, Y. Yue, e S. Huang. An
     * optimal independent sets based greedy spectral defragmentation algorithm
     * in elastic optical network. In 2014 13th International Conference on
     * Optical Communications and Networks (ICOCN), pages 1–4, Nov 2014.
     *
     * @return the current fragmentation of topology.
     */
    public double getFragmentationWu() {
        double sum = 0;
        EONLink l;
        int maxSize, avaiableSlots;
        for (int i = 0; i < links; i++) {
            l = (EONLink) this.getLink(i);
            maxSize = l.maxSizeAvaiable();
            avaiableSlots = l.getAvaiableSlots();
            if (maxSize == avaiableSlots) {
                sum += 1;
            } else {
                sum += (((double) maxSize / (double) avaiableSlots) * (1 / marginAccount(i)));
            }
        }
        sum = sum / (double) links;
        return (1.0 - sum);
    }
    
    /**
     * Retrieves the current fragmentation rate in this links in accordance
     * with Wu et al 2014. J. Wu, M. Zhang, F. Wang, Y. Yue, e S. Huang. An
     * optimal independent sets based greedy spectral defragmentation algorithm
     * in elastic optical network. In 2014 13th International Conference on
     * Optical Communications and Networks (ICOCN), pages 1–4, Nov 2014.
     *
     * @param links the links
     * @return the current fragmentation of topology.
     */
    public double getFragmentationWu(int[] links) {
        double sum = 0;
        EONLink l;
        int maxSize, avaiableSlots;
        for (int i = 0; i < links.length; i++) {
            l = (EONLink) this.getLink(links[i]);
            maxSize = l.maxSizeAvaiable();
            avaiableSlots = l.getAvaiableSlots();
            if (maxSize == avaiableSlots) {
                sum += 1;
            } else {
                sum += (((double) maxSize / (double) avaiableSlots) * (1 / marginAccount(links[i])));
            }
        }
        sum = sum / (double) links.length;
        return (1.0 - sum);
    }

    /**
     * This method helps the getFragmentationWu() method by counting the number
     * of margins in a link. A margin is a free slot next to a busy slot
     *
     * @param link the index of link
     * @return the number of margins
     */
    private double marginAccount(int link) {
        double margin = 0;
        EONLink l = (EONLink) this.getLink(link);
        for (int c = 0; c < l.getNumCores(); c++) {
            for (int i = 1; i < (l.getCores()[c].slots.length); i++) {
                if (!(((l.getCores()[c].slots[i - 1] != 0) && (l.getCores()[c].slots[i] != 0)) || (((l.getCores()[c].slots[i - 1] == 0) && (l.getCores()[c].slots[i] == 0))))) {
                    margin++;
                }
            }
        }
        margin++;
        return margin / 2.0;
    }

    /**
     * Retrieves the current fragmentation rate in this topology in accordance
     * with Wright et al 2015. P. Wright, M. C. Parker, e A. Lord. Minimum- and
     * maximum-entropy routing and spectrum assignment for flexgrid elastic
     * optical networking [invited]. IEEE/OSA Journal of Optical Communications
     * and Networking, 7(1):A66–A72, Jan 2015.
     *
     * @return the current fragmentation of topology.
     */
    public double getFragmentationWright() {
        double fragMax = 0.0, frag = 0.0;
        EONLink l;
        int slots = ((EONLink) this.getLink(0)).getNumSlots();
        ArrayList<Integer> blocks;
        for (int i = 0; i < (slots / 2); i++) {
            fragMax += ((1.0 / (double) slots) * (Math.log(1.0) - Math.log((double) slots)));
        }
        for (int i = 0; i < links; i++) {
            l = (EONLink) this.getLink(i);
            if (l.maxSizeAvaiable() == l.getAvaiableSlots()) {
                frag += 0.0;
            } else {
                blocks = l.getLengthFreeBlock();

                for (int bl : blocks) {

                    frag += (((double) bl / (double) l.getNumSlots()) * (Math.log((double) bl) - Math.log((double) l.getNumSlots())));

                }
            }
        }
        fragMax = -fragMax;
        frag = -frag;
        return frag / (fragMax * (double) links);
    }
    
    /**
     * Retrieves the current fragmentation rate in this topology in accordance
     * with Wright et al 2015. P. Wright, M. C. Parker, e A. Lord. Minimum- and
     * maximum-entropy routing and spectrum assignment for flexgrid elastic
     * optical networking [invited]. IEEE/OSA Journal of Optical Communications
     * and Networking, 7(1):A66–A72, Jan 2015.
     *
     * @param links the links
     * @return the current fragmentation of topology.
     */
    public double getFragmentationWright(int[] links) {
        double fragMax = 0.0, frag = 0.0;
        EONLink l;
        int slots = ((EONLink) this.getLink(0)).getNumSlots();
        ArrayList<Integer> blocks;
        for (int i = 0; i < (slots / 2); i++) {
            fragMax += ((1.0 / (double) slots) * (Math.log(1.0) - Math.log((double) slots)));
        }
        for (int i = 0; i < links.length; i++) {
            l = (EONLink) this.getLink(links[i]);
            if (l.maxSizeAvaiable() == l.getAvaiableSlots()) {
                frag += 0.0;
            } else {
                blocks = l.getLengthFreeBlock();

                for (int bl : blocks) {

                    frag += (((double) bl / (double) l.getNumSlots()) * (Math.log((double) bl) - Math.log((double) l.getNumSlots())));

                }
            }
        }
        fragMax = -fragMax;
        frag = -frag;
        return frag / (fragMax * (double) links.length);
    }

    /**
     * Retrieves the current fragmentation rate in this topology in accordance
     * with Sugihara et al 2017. S. Sugihara, Y. Hirota, S. Fujii, H. Tode, e T.
     * Watanabe. Dynamic resource allocation for immediate and advance
     * reservation in space-division-multiplexing-based elastic optical
     * networks. IEEE/OSA Journal of Optical Communications and Networking,
     * 9(3):183–197, March 2017.
     *
     * @return the current fragmentation of topology.
     */
    public double getFragmentationEntropy() {
        EONLink l;
        double n = 0;
        int slots = ((EONLink) this.getLink(0)).getNumSlots();
        for (int i = 0; i < links; i++) {
            l = (EONLink) this.getLink(i);
            if (l.maxSizeAvaiable() == l.getAvaiableSlots()) {
                n += 0.0;
            } else {
                int m = 0;
                for (int c = 0; c < l.getNumCores(); c++) {
                    for (int j = 1; j < (l.getCores()[c].slots.length); j++) {
                        if (!(((l.getCores()[c].slots[j - 1] != 0) && (l.getCores()[c].slots[j] != 0)) || (((l.getCores()[c].slots[j - 1] == 0) && (l.getCores()[c].slots[j] == 0))))) {
                            m++;
                        }
                    }
                }
                n += m / (double) (slots - 1);
            }
        }
        return n / (double) links;
    }

    /**
     * Retrieves the current fragmentation rate in this links in accordance
     * with Sugihara et al 2017. S. Sugihara, Y. Hirota, S. Fujii, H. Tode, e T.
     * Watanabe. Dynamic resource allocation for immediate and advance
     * reservation in space-division-multiplexing-based elastic optical
     * networks. IEEE/OSA Journal of Optical Communications and Networking,
     * 9(3):183–197, March 2017.
     *
     * @param links the links
     * @return the current fragmentation of topology.
     */
    public double getFragmentationEntropy(int[] links) {
        EONLink l;
        double n = 0;
        int slots = ((EONLink) this.getLink(0)).getNumSlots();
        for (int i = 0; i < links.length; i++) {
            l = (EONLink) this.getLink(links[i]);
            if (l.maxSizeAvaiable() == l.getAvaiableSlots()) {
                n += 0.0;
            } else {
                int m = 0;
                for (int c = 0; c < l.getNumCores(); c++) {
                    for (int j = 1; j < (l.getCores()[c].slots.length); j++) {
                        if (!(((l.getCores()[c].slots[j - 1] != 0) && (l.getCores()[c].slots[j] != 0)) || (((l.getCores()[c].slots[j - 1] == 0) && (l.getCores()[c].slots[j] == 0))))) {
                            m++;
                        }
                    }
                }
                n += m / (double) (slots - 1);
            }
        }
        return n / (double) links.length;
    }
    
    /**
     * Retrieves the current fragmentation rate in this topology in accordance
     * with Singh and Jukan 2017. S. K. Singh e A. Jukan. Efficient spectrum
     * defragmentation with holding-time awareness in elastic optical networks.
     * IEEE/OSA Journal of Optical Communications and Networking, 9(3):B78–B89,
     * March 2017.
     *
     * @return the current fragmentation of topology.
     */
    public double getFragmentationSingh() {
        BigDecimal minus_1 = new BigDecimal(1);
        EONLink l;
        BigDecimal valueT = new BigDecimal(0), valueTL = new BigDecimal(0);
        ArrayList<Integer> blocks = new ArrayList<>();
        for (int i = 0; i < links; i++) {
            l = (EONLink) this.getLink(i);
            blocks = l.getLengthFreeBlock();
            BigDecimal r1, r2, value;
            r2 = new BigDecimal(6);
            value = new BigDecimal(0);
            for (int bl : blocks) {
                r1 = new BigDecimal(valueSi.get(bl));
                r1 = r1.divide(r2, 20, BigDecimal.ROUND_UP);
                value = value.add(r1);
            }
            BigDecimal t1;
            int avaliable = l.getAvaiableSlots();
            t1 = new BigDecimal(valueSi.get(avaliable));
            t1 = t1.divide(r2, 20, BigDecimal.ROUND_UP);
            value = value.divide(t1, 20, BigDecimal.ROUND_UP);
            valueT = minus_1.subtract(value);
            valueTL = valueTL.add(valueT);
        }
        return (valueTL.doubleValue() / (double) links);
    }
    
    /**
     * Retrieves the current fragmentation rate in this links in accordance
     * with Singh and Jukan 2017. S. K. Singh e A. Jukan. Efficient spectrum
     * defragmentation with holding-time awareness in elastic optical networks.
     * IEEE/OSA Journal of Optical Communications and Networking, 9(3):B78–B89,
     * March 2017.
     *
     * @return the current fragmentation of topology.
     */
    public double getFragmentationSingh(int[] links) {
        BigDecimal minus_1 = new BigDecimal(1);
        EONLink l;
        BigDecimal valueT = new BigDecimal(0), valueTL = new BigDecimal(0);
        ArrayList<Integer> blocks = new ArrayList<>();
        for (int i = 0; i < links.length; i++) {
            l = (EONLink) this.getLink(links[i]);
            blocks = l.getLengthFreeBlock();
            BigDecimal r1, r2, value;
            r2 = new BigDecimal(6);
            value = new BigDecimal(0);
            for (int bl : blocks) {
                r1 = new BigDecimal(valueSi.get(bl));
                r1 = r1.divide(r2, 20, BigDecimal.ROUND_UP);
                value = value.add(r1);
            }
            BigDecimal t1;
            int avaliable = l.getAvaiableSlots();
            t1 = new BigDecimal(valueSi.get(avaliable));
            t1 = t1.divide(r2, 20, BigDecimal.ROUND_UP);
            value = value.divide(t1, 20, BigDecimal.ROUND_UP);
            valueT = minus_1.subtract(value);
            valueTL = valueTL.add(valueT);
        }
        return (valueTL.doubleValue() / (double) links.length);
    }

    /**
     * Retrieves the current fragmentation rate in this topology in accordance
     * with Wang et al 2015. N. Wang, J. P. Jue, X. Wang, Q. Zhang, H. C.
     * Cankaya, e M. Sekiya. Holding-timeaware scheduling for immediate and
     * advance reservation in elastic optical networks. In 2015 IEEE
     * International Conference on Communications (ICC), pages 5180–5185, June
     * 2015.
     *
     * @return the current fragmentation of topology.
     */
    public double getFragmentationWang() {
        EONLink l;
        ArrayList<Integer> blocks = new ArrayList<>();
        double prod = 1;
        double sum = 0;
        for (int i = 0; i < links; i++) {
            l = (EONLink) this.getLink(i);
            blocks = l.getLengthFreeBlock();
            for (int n : blocks) {
                prod = prod * valueA.get(n);
            }
            sum += prod;
        }
        double max = valueA.get(((EONLink) this.getLink(0)).cores[0].slots.length);
        max = max * links;
        return (1.0 - (sum / max));
    }
    
    /**
     * Retrieves the current fragmentation rate in this links in accordance
     * with Wang et al 2015. N. Wang, J. P. Jue, X. Wang, Q. Zhang, H. C.
     * Cankaya, e M. Sekiya. Holding-timeaware scheduling for immediate and
     * advance reservation in elastic optical networks. In 2015 IEEE
     * International Conference on Communications (ICC), pages 5180–5185, June
     * 2015.
     *
     * @return the current fragmentation of topology.
     */
    public double getFragmentationWang(int[] links) {
        EONLink l;
        ArrayList<Integer> blocks = new ArrayList<>();
        double prod = 1;
        double sum = 0;
        for (int i = 0; i < links.length; i++) {
            l = (EONLink) this.getLink(links[i]);
            blocks = l.getLengthFreeBlock();
            for (int n : blocks) {
                prod = prod * valueA.get(n);
            }
            sum += prod;
        }
        double max = valueA.get(((EONLink) this.getLink(0)).cores[0].slots.length);
        max = max * links.length;
        return (1.0 - (sum / max));
    }

    /**
     * This method helps the getFragmentationWang() method.
     */
    private static double A(int n, int m) {
        double value;
        if (valueA.containsKey(n)) {
            return valueA.get(n);
        }
        if (n == 0) {
            return 0;
        } else if (n >= 1 && n <= m) {
            value = 1;
            for (int i = 1; i < n; i++) {
                value += A(i - 1, m);
            }
            return value;
        } else {
            value = 0;
            for (int i = 1; i < m; i++) {
                value += A(i - 1, m);
            }
            return value;
        }
    }
}
