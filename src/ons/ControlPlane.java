/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import ons.ra.RA;
import ons.ra.ControlPlaneForRA;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import ons.ra.RABulk;

/**
 * The Control Plane is responsible for managing resources and connection within
 * the network.
 */
public class ControlPlane implements ControlPlaneForRA { // RA is Routing Assignment Problem

    private RA ra;
    private RABulk rabulk;
    private PhysicalTopology pt;
    private VirtualTopology vt;
    private Map<Flow, Path> mappedFlowsSinglePath; // Flows that have been accepted into the network in Single Path
    private Map<Flow, MultiPath> mappedFlowsMultiPath; // Flows that have been accepted into the network Multi Path
    private Map<Flow, MultiPathProtect> mappedFlowsMultiPathProtect; // Flows that have been accepted into the network Multi Path
    private Map<Long, Flow> activeFlows; // Flows that have been accepted or that are waiting for a decision 
    private ArrayList<Path> arrayStaticPath; // Paths that have been allocated in virtual topology via addstatic
    private ArrayList<MultiPathProtect> arrayStaticProtectPath; // Protect Paths that have been allocated in virtual topology via addstatic
    private Tracer tr = Tracer.getTracerObject();
    private MyStatistics st = MyStatistics.getMyStatisticsObject();
    //For eonsimBatch:
    private Map<BulkData, Path> mappedBulks; // BulksData that have been accepted into the network
    private Map<Long, BulkData> activeBulks; // BulksData that have been accepted or that are waiting for a decision 
    private Map<Batch, Path[]> mappedBatchs; // batchs that have been accepted into the network
    private Map<Long, Batch> activeBatchs; // batchs that have been accepted or that are waiting for a decision 
    EventScheduler events;
    double currentTime;
    private int byzantine = 3;
    
    //usado para testes na camada fisica
    private PhysicalImpairments pi = PhysicalImpairments.getPhysicalImpairmentsObject();
    
    /**
     * Creates a new ControlPlane object.
     *
     * @param raModule the name of the RA class
     * @param pt the network's physical topology
     * @param vt the network's virtual topology
     */
    public ControlPlane(String raModule, PhysicalTopology pt, VirtualTopology vt) {
        Class RAClass;

        mappedFlowsSinglePath = new HashMap<>();
        mappedFlowsMultiPath = new HashMap<>();
        mappedFlowsMultiPathProtect = new HashMap<>();
        activeFlows = new HashMap<>();
        arrayStaticPath = new ArrayList<>();
        arrayStaticProtectPath = new ArrayList<>();

        this.pt = pt;
        this.vt = vt;

        try {
            RAClass = Class.forName(raModule);
            ra = (RA) RAClass.newInstance();
            ra.simulationInterface(this);
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println("The RA class does not exist in the ons.ra packet or error in RA code!");
            ArgParsing.printUsage();
        }
    }

    ControlPlane(String raModule, PhysicalTopology pt, VirtualTopology vt, EventScheduler events) {
        Class RAClass;

        mappedFlowsSinglePath = new HashMap<>();
        mappedFlowsMultiPath = new HashMap<>();
        mappedFlowsMultiPathProtect = new HashMap<>();
        activeFlows = new HashMap<>();
        arrayStaticPath = new ArrayList<>();
        arrayStaticProtectPath = new ArrayList<>();
        mappedBulks = new HashMap<>();
        activeBulks = new HashMap<>();
        mappedBatchs = new HashMap<>();
        activeBatchs = new HashMap<>();
        
        this.pt = pt;
        this.vt = vt;
        this.events = events;

        try {
            RAClass = Class.forName(raModule);
            rabulk = (RABulk) RAClass.newInstance();
            rabulk.simulationInterface(this);
            ra = rabulk;
        } catch (Throwable t) {
            //t.printStackTrace();
            System.out.println("The RA class does not exist in the ons.ra packet or error in RA code!");
            ArgParsing.printUsage();
        }
    }

    /**
     * Deals with an Event from the event queue. If it is of the
       FlowArrivalEvent kind, adds it to the list of active flows. If it is from
       the FlowDepartureEvent, removes it from the list.
     *
     * @param event the Event object taken from the queue
     */
    public void newEvent(Event event) {
        currentTime = event.getTime();
        if (event instanceof FlowArrivalEvent) {
            newFlow(((FlowArrivalEvent) event).getFlow());
            long time = System.currentTimeMillis();
            ra.flowArrival(((FlowArrivalEvent) event).getFlow());
            st.setFlowArrivalTime(System.currentTimeMillis() - time);
            st.checkOXCState();
        } else if (event instanceof FlowDepartureEvent) {
            ra.flowDeparture(((FlowDepartureEvent) event).getID());
            removeFlow(((FlowDepartureEvent) event).getID());
            st.checkOXCState();
        } else if (event instanceof BulkDataArrivalEvent) {
            newBulkData(((BulkDataArrivalEvent) event).getBulkData());
            long time = System.currentTimeMillis();
            rabulk.bulkDataArrival(((BulkDataArrivalEvent) event).getBulkData());
            st.setBulkArrivalTime(System.currentTimeMillis() - time);
        } else if (event instanceof BulkDepartureEvent) {
            rabulk.bulkDeparture(((BulkDepartureEvent) event).getID());
            removeBulk(((BulkDepartureEvent) event).getID());
            st.checkOXCState();
        } else if (event instanceof BatchArrivalEvent) {
            newBatch(((BatchArrivalEvent) event).getBatch());
            long time = System.currentTimeMillis();
            rabulk.batchArrival(((BatchArrivalEvent) event).getBatch());
            st.setBatchArrivalTime(System.currentTimeMillis() - time);
        } else if (event instanceof BatchDepartureEvent) {
            rabulk.batchDeparture(((BatchDepartureEvent) event).getID());
            removeBatch(((BatchDepartureEvent) event).getID());
            st.checkOXCState();
        } else if (event instanceof OrdinaryEvent) {
            switch (((OrdinaryEvent) event).getDescription()) {
                case "start":
                    //TODO
                    break;
                case "end":
                    ra.simulationEnd();
                    removeAllLightPaths();
                    st.checkOXCState();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Adds a given active Flow object to a determined Physical Topology.
     *
     * @param id unique identifier of the Flow object
     * @param lightpaths the Path, or list of LighPath objects
     * @return true if operation was successful, or false if a problem occurred
     */
    @Override
    public boolean acceptFlow(long id, LightPath[] lightpaths) {
        Flow flow;

        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (!canAddRateToPT(flow.getRate(), lightpaths)) {
                return false;
            }
            if (!checkLightpathContinuity(flow.getSource(), flow.getDestination(), lightpaths)) {
                return false;
            }
            if(Simulator.simType >= 1) {
                if(pi.isPhysicalDistance()) {
                    if(!vt.modulationPath(lightpaths)) {
                        return false;
                    }
                }
                if(pi.isXTaware()) {
                    if(!vt.checkCrosstalk(lightpaths)) {
                        return false;
                    }
                }
                if(pi.isXTonOthers()) {
                    if(!vt.checkCrosstalkOnOthers(lightpaths)) {
                        return false;
                    }
                }
                //SNR CHECK (only for SNR checking of this lp)
                if (pi.isSNRaware()) {
                    if (!checkSNR(lightpaths)) {
                        return false;
                    }
                }
            }
            boolean usedTransponders[] = usedTransponders(lightpaths);
            addRateToPT(flow.getRate(), lightpaths);
            for (LightPath lightpath : lightpaths) {
                lightpath.addFlow(flow);
            }
            mappedFlowsSinglePath.put(flow, new Path(lightpaths));
            tr.acceptFlow(flow, lightpaths);
            st.acceptFlow(flow, lightpaths, usedTransponders);
            return true;
        }
    }

    /**
     * Adds a given active Flow object to a determined Physical Topology for
     * Multipath
     *
     * @param id unique identifier of the Flow object
     * @param paths the paths from source and destination of Flow object
     * @param bws the bandwidth in each path
     * @return true if operation was successful, or false if a problem occurred
     */
    @Override
    public boolean acceptFlow(long id, Path[] paths, int[] bws) {
        Flow flow;
        MultiPath multipath;

        if (id < 0 || paths.length < 1 || bws.length < 1 || paths.length != bws.length) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            multipath = new MultiPath(paths);
            if (multipath.getTotalBWAvailable() < flow.getRate()) {
                return false;
            }
            int bwTotal = 0;
            for (int i = 0; i < bws.length; i++) {
                bwTotal += bws[i];
            }
            if (bwTotal < flow.getRate()) {
                return false;
            }
            if (!canAddFlowToPT(bws, paths)) {
                return false;
            }
            if (!checkPathContinuity(flow, paths)) {
                return false;
            }
            if(Simulator.simType >= 1) {
                if(pi.isPhysicalDistance()) {
                    if(!vt.modulationPath(paths)) {
                        return false;
                    }
                }
                if(pi.isXTaware()) {
                    if(!vt.checkCrosstalk(paths)) {
                        return false;
                    }
                }
                if(pi.isXTonOthers()) {
                    if(!vt.checkCrosstalkOnOthers(paths)) {
                        return false;
                    }
                }
                //SNR CHECK (only for SNR checking of this lp)
                if (pi.isSNRaware()) {
                    if (!checkSNR(paths)) {
                        return false;
                    }
                }
            }
            boolean usedTransponders[][] = usedTransponders(paths);
            addFlowToPT(bws, paths);
            for (Path path : paths) {
                for (LightPath lightpath : path.getLightpaths()) {
                    lightpath.addFlow(flow);
                }
            }
            mappedFlowsMultiPath.put(flow, multipath);
            tr.acceptFlow(flow, paths);
            st.acceptFlow(flow, paths, usedTransponders);
            return true;
        }
    }

    /**
     * Adds a given active Flow object to a determined Physical Topology for
     * Multipath Protect. If reservedProtect = false it will not be reserved.
     *      * If reservedProtect = true it will be reserved.      * The second
     * path is always the backup!
     *
     * @param id unique identifier of the Flow object
     * @param primaryPaths the primary paths from source and destination of Flow object
     * @param primaryBWS the bandwidth in primary each path
     * @param backupPaths the backup paths from source and destination of Flow object
     * @param backupBWS the bandwidth in backup each path
     * @param reservedProtect if the path is reserved or not (reserved paths are not deallocated when idle)
     * @param shared if the protection is shared or not
     * @return true if operation was successful, or false if a problem occurred
     */
    @Override
    public boolean acceptFlow(long id, Path[] primaryPaths, int[] primaryBWS, Path[] backupPaths, int[] backupBWS, boolean reservedProtect, boolean shared) {
        Flow flow;
        MultiPathProtect multiPathProtect;
        boolean usedTranspondersPrimary[][];
        boolean usedTranspondersBackup[][];
        if (id < 0 || primaryPaths.length < 1 || backupPaths.length < 1 || primaryBWS.length < 1 || backupBWS.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            MultiPath[] multiPath = new MultiPath[2];
            multiPath[0] = new MultiPath(primaryPaths);
            multiPath[1] = new MultiPath(backupPaths);
            multiPathProtect = new MultiPathProtect(multiPath);
            if (multiPathProtect.getPrimary().getTotalBWAvailable() < flow.getRate() || multiPathProtect.getBackup().getTotalBWAvailable() < flow.getRate()) {
                return false;
            }
            int bwTotal = 0;
            for (int i = 0; i < primaryBWS.length; i++) {
                bwTotal += primaryBWS[i];
            }
            if (bwTotal < flow.getRate()) {
                return false;
            }
            bwTotal = 0;
            for (int i = 0; i < primaryBWS.length; i++) {
                bwTotal += backupBWS[i];
            }
            if (bwTotal < flow.getRate()) {
                return false;
            }            
            if(shared){
                if (!canAddFlowToPT(primaryBWS, primaryPaths) || !canPutFlowToBackup(backupBWS, backupPaths)) {
                    return false;
                }
            } else {
                if (!canAddFlowToPT(primaryBWS, primaryPaths) || !canAddFlowToPT(backupBWS, backupPaths)) {
                    return false;
                }
            }
            if (!checkPathContinuity(flow, primaryPaths) || !checkPathContinuity(flow, backupPaths)) {
                return false;
            }
            if (!isDisjointedPaths(primaryPaths, backupPaths)) {
                return false;
            }
            if(Simulator.simType >= 1) {
                if(pi.isPhysicalDistance()) {
                    if(!vt.modulationPath(primaryPaths) || !vt.modulationPath(backupPaths)) {
                        return false;
                    }
                }
                if(pi.isXTaware()) {
                    if(!vt.checkCrosstalk(primaryPaths) || !vt.checkCrosstalk(backupPaths)) {
                        return false;
                    }
                }
                if(pi.isXTonOthers()) {
                    if(!vt.checkCrosstalkOnOthers(primaryPaths) || !vt.checkCrosstalkOnOthers(backupPaths)) {
                        return false;
                    }
                }
                //SNR CHECK (only for SNR checking of this lp)
                if (pi.isSNRaware()) {
                    if (!checkSNR(primaryPaths) || !checkSNR(backupPaths)) {
                        return false;
                    }
                }
            }
            usedTranspondersPrimary = usedTransponders(primaryPaths);
            usedTranspondersBackup = usedTransponders(backupPaths);
            addFlowToPT(primaryBWS, primaryPaths);
            if(!shared){
                addFlowToPT(backupBWS, backupPaths);
            }
            //Set the backup index to primary lightpaths so that can search for it in the future
            for (int i = 0; i < primaryPaths.length; i++) {
                for (LightPath lightpath : primaryPaths[i].getLightpaths()) {
                    if (lightpath.getLpBackup().isEmpty()) {
                        ArrayList<Long> backupIds = getbackupIdsLightptahs(lightpath, primaryPaths[i], backupPaths[i]);
                        for (Long backupId : backupIds) {
                            lightpath.addLpBackup(backupId);
                        }
                    }
                }
            }
            if (reservedProtect) {
                for (Path primaryPath : primaryPaths) {
                    for (LightPath lightpath : primaryPath.getLightpaths()) {
                        lightpath.setReserved(true);
                    }
                }
                for (Path backupPath : backupPaths) {
                    for (LightPath lightpath : backupPath.getLightpaths()) {
                        lightpath.setReserved(true);
                        lightpath.setBackup(true);
                    }
                }
            } else {
                for (Path backupPath : backupPaths) {
                    for (LightPath lightpath : backupPath.getLightpaths()) {
                        lightpath.setBackup(true);
                    }
                }
            }
            for (Path path : primaryPaths) {
                for (LightPath lightpath : path.getLightpaths()) {
                    lightpath.addFlow(flow);
                }
            }
            for (Path path : backupPaths) {
                for (LightPath lightpath : path.getLightpaths()) {
                    lightpath.addFlow(flow);
                }
            }
            mappedFlowsMultiPathProtect.put(flow, multiPathProtect);
            tr.acceptFlow(flow, primaryPaths, backupPaths);
            st.acceptFlow(flow, primaryPaths, backupPaths, usedTranspondersPrimary, usedTranspondersBackup);
            return true;
        }
    }

    /**
     * Removes a given Flow object from the list of active flows.
     *
     * @param id unique identifier of the Flow object
     * @return true if operation was successful, or false if a problem occurred
     */
    @Override
    public boolean blockFlow(long id) {
        Flow flow;

        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (mappedFlowsSinglePath.containsKey(flow) || mappedFlowsMultiPath.containsKey(flow) || mappedFlowsMultiPathProtect.containsKey(flow)) {
                return false;
            }
            activeFlows.remove(id);
            tr.blockFlow(flow);
            st.blockFlow(flow);
            return true;
        }
    }

    /**
     * Removes a given Flow object from the Physical Topology and then puts it
     * back, but with a new route (set of LightPath objects).
     *
     * @param id unique identifier of the Flow object
     * @param lightpaths list of LightPath objects, which form a Path
     * @return true if operation was successful, or false if a problem occurred
     */
    @Override
    public boolean rerouteFlow(long id, LightPath[] lightpaths) {
        Flow flow;
        Path oldPath;
        
        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeFlows.containsKey(id)) {
                return false;
            }
            flow = activeFlows.get(id);
            if (!mappedFlowsSinglePath.containsKey(flow)) {
                return false;
            }
            if (!canAddRateToPT(flow.getRate(), lightpaths)) {
                return false;
            }
            if (!checkLightpathContinuity(flow.getSource(), flow.getDestination(), lightpaths)) {
                return false;
            }
            if(Simulator.simType >= 1) {
                if(pi.isPhysicalDistance()) {
                    if(!vt.modulationPath(lightpaths)) {
                        return false;
                    }
                }
                if(pi.isXTaware()) {
                    if(!vt.checkCrosstalk(lightpaths)) {
                        return false;
                    }
                }
                if(pi.isXTonOthers()) {
                    if(!vt.checkCrosstalkOnOthers(lightpaths)) {
                        return false;
                    }
                }
                //SNR CHECK (only for SNR checking of this lp)
                if (pi.isSNRaware()) {
                    if (!checkSNR(lightpaths)) {
                        return false;
                    }
                }
            }
            oldPath = mappedFlowsSinglePath.get(flow);
            for (LightPath lightpath : oldPath.getLightpaths()) {
                lightpath.removeFlow(flow);
            }
            boolean usedTransponders[] = usedTransponders(lightpaths);
            boolean usedTranspondersOld[] = usedTransponders(oldPath.getLightpaths());
            addRateToPT(flow.getRate(), lightpaths);
            for (LightPath lightpath : lightpaths) {
                lightpath.addFlow(flow);
            }
            mappedFlowsSinglePath.put(flow, new Path(lightpaths));
            tr.rerouteFlow(flow, lightpaths, oldPath);
            st.rerouteFlow(flow, lightpaths, oldPath, usedTransponders, usedTranspondersOld);
            return true;
        }
    }

    /**
     * Adds a given Flow object to the HashMap of active flows. The HashMap also
     * stores the object's unique identifier (ID).
     *
     * @param flow Flow object to be added
     */
    private void newFlow(Flow flow) {
        activeFlows.put(flow.getID(), flow);
    }

    /**
     * Removes a given Flow object from the list of active flows.
     *
     * @param id the unique identifier of the Flow to be removed
     */
    private void removeFlow(long id) {
        Flow flow;
        LightPath[] lightpaths;
        Path[] paths;

        if (activeFlows.containsKey(id)) {
            flow = activeFlows.get(id);
            if (mappedFlowsSinglePath.containsKey(flow)) {
                lightpaths = mappedFlowsSinglePath.get(flow).getLightpaths();
                for (LightPath lightpath : lightpaths) {
                    lightpath.removeFlow(flow);
                }
                removeFlowFromPT(flow, lightpaths);
                mappedFlowsSinglePath.remove(flow);
            } else {
                if (mappedFlowsMultiPath.containsKey(flow)) {
                    paths = mappedFlowsMultiPath.get(flow).getPaths();
                    for (Path path : paths) {
                        for (LightPath lightpath : path.getLightpaths()) {
                            lightpath.removeFlow(flow);
                        }
                    }
                    removeFlowFromPT(flow, paths);
                    mappedFlowsMultiPath.remove(flow);
                } else {
                    if (mappedFlowsMultiPathProtect.containsKey(flow)) {
                        paths = mappedFlowsMultiPathProtect.get(flow).getPrimary().getPaths();
                        for (Path path : paths) {
                            for (LightPath lightpath : path.getLightpaths()) {
                                lightpath.removeFlow(flow);
                            }
                        }
                        removeFlowFromPT(flow, paths);
                        paths = mappedFlowsMultiPathProtect.get(flow).getBackup().getPaths();
                        for (Path path : paths) {
                            for (LightPath lightpath : path.getLightpaths()) {
                                lightpath.removeFlow(flow);
                            }
                        }
                        removeFlowFromPT(flow, paths);
                        mappedFlowsMultiPathProtect.remove(flow);
                    }
                }
            }
            activeFlows.remove(id);
        }
    }

    /**
     * Removes a given Flow object from a Physical Topology.
     *
     * @param flow the Flow object that will be removed from the PT
     * @param lightpaths a list of LighPath objects
     */
    private void removeFlowFromPT(Flow flow, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            pt.removeRate(flow.getRate(), lightpath);
            // Can the lightpath be removed?
            if (!lightpath.isReserved()) {
                if (vt.isLightpathIdle(lightpath.getID())) {
                    vt.removeLightPath(lightpath.getID());
                }
            }
        }
    }

    /**
     * Removes a given Flow object from a Physical Topology.
     *
     * @param flow the Flow object that will be removed from the PT
     * @param paths a list of Path objects
     */
    private void removeFlowFromPT(Flow flow, Path[] paths) {
        for (Path path : paths) {
            for (LightPath lightpath : path.getLightpaths()) {
                if (!lightpath.isBackupShared()) {
                    pt.removeRate(flow.getRate(), lightpath);
                }
                if (!lightpath.isReserved()) {
                    // Can the lightpath be removed?
                    if (vt.isLightpathIdle(lightpath.getID())) {
                        vt.removeLightPath(lightpath.getID());
                    }
                }
            }
        }
    }

    /**
     * Says whether or not a given Flow object can be added to a determined
     * Physical Topology, based on the amount of bandwidth the flow requires
     * opposed to the available bandwidth.
     *
     * @param rate the rate (Mbps) object to be added
     * @param lightpaths list of LightPath objects the flow uses
     * @return true if Flow object can be added to the PT, or false if it can't
     */
    private boolean canAddRateToPT(int rate, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            if (!pt.canAddRate(rate, lightpath)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Says whether or not a given array of bandwidths can be added a given
     * array of paths in its specific index.
     *
     * @param bws the Flow division bw
     * @param paths list of paths objects the flow uses
     * @return true if each bws[i] can be added in path[i], or false otherwise
     */
    private boolean canAddFlowToPT(int[] bws, Path[] paths) {
        for (int i = 0; i < paths.length; i++) {
            for (LightPath lightpath : paths[i].getLightpaths()) {
                if (!pt.canAddRate(bws[i], lightpath)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean canPutFlowToBackup(int[] bws, Path[] paths) {
        for (int i = 0; i < paths.length; i++) {
            for (LightPath lightpath : paths[i].getLightpaths()) {
                if ((pt.getBW(lightpath) < bws[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adds a Flow object to a Physical Topology. This means adding the flow to
     * the network's traffic, which simply decreases the available bandwidth.
     *
     * @param rate the rate (Mbps) object to be added
     * @param lightpaths list of LightPath objects the flow uses
     */
    private void addRateToPT(int rate, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            if (lightpath != null) {
                pt.addRate(rate, lightpath);
            }
        }
    }

    /**
     * Adds a Flow division bw to a Physical Topology. This means adding the
     * flow to the network's traffic, which simply decreases the available
     * bandwidth.
     *
     * @param bws the Flow division bw
     * @param paths list of paths objects the flow uses
     */
    private void addFlowToPT(int[] bws, Path[] paths) {
        for (int i = 0; i < paths.length; i++) {
            for (LightPath lightpath : paths[i].getLightpaths()) {
                pt.addRate(bws[i], lightpath);
            }
        }
    }

    /**
     * Retrieves a Path object, based on a given Flow object. That's possible
     * thanks to the HashMap mappedFlowsSinglePath, which maps a Flow to a Path.
     *
     * @param flow Flow object that will be used to find the Path object
     * @return Path object mapped to the given flow
     */
    @Override
    public Path getPath(Flow flow) {
        return mappedFlowsSinglePath.get(flow);
    }

    /**
     * Retrieves a MultiPath object, based on a given Flow object.
     *
     * @param flow Flow object that will be used to find the Path object
     * @return MultiPath object mapped to the given flow
     */
    @Override
    public MultiPath getMultiPath(Flow flow) {
        return mappedFlowsMultiPath.get(flow);
    }

    /**
     * Retrieves the complete set of Flow/Path pairs listed on the mapped Flows
     * in Single Path HashMap.
     *
     * @return the mappedFlowsSinglePath HashMap
     */
    @Override
    public Map<Flow, Path> getMappedFlowsSinglePath() {
        return mappedFlowsSinglePath;
    }

    /**
     * Retrieves the complete set of Flow/MultiPath pairs listed on the mapped
     * Flows in Multi Path HashMap.
     *
     * @return the mappedFlowsMultiPath HashMap
     */
    @Override
    public Map<Flow, MultiPath> getMappedFlowsMultiPath() {
        return mappedFlowsMultiPath;
    }

    /**
     * Retrieves the complete set of Flow/MultiPathProtect pairs listed on the
     * mapped Flows in MultiPathProtect HashMap.
     *
     * @return the mappedFlowsMultiPathProtect HashMap
     */
    @Override
    public Map<Flow, MultiPathProtect> getMappedFlowsMultiPathProtect() {
        return mappedFlowsMultiPathProtect;
    }    

    /**
     * Retrieves a Flow object from the list of active flows.
     *
     * @param id the unique identifier of the Flow object
     * @return the required Flow object
     */
    @Override
    public Flow getFlow(long id) {
        return activeFlows.get(id);
    }

    /**
     * Counts number of times a given LightPath object is used within the Flow
     * objects of the network.
     *
     * @param id unique identifier of the LightPath object
     * @return integer with the number of times the given LightPath object is
     * used
     */
    @Override
    public int getLightpathFlowCount(long id) {
        int num = 0;
        Path p;
        LightPath[] lps;
        ArrayList<Path> ps = new ArrayList<>(mappedFlowsSinglePath.values());
        for (Path p1 : ps) {
            p = p1;
            lps = p.getLightpaths();
            for (LightPath lp : lps) {
                if (lp.getID() == id) {
                    num++;
                    break;
                }
            }
        }
        return num;
    }

    /**
     * Retrieves a PhysicalTopology object from the Control Plane.
     *
     * @return the PhysicalTopology object
     */
    @Override
    public PhysicalTopology getPT() {
        return pt;
    }

    /**
     * Retrieves a VirtualTopology object from the Control Plane.
     *
     * @return the VirtualTopology object
     */
    @Override
    public VirtualTopology getVT() {
        return vt;
    }

    /**
     * Retrieves Event Current Time.
     *
     * @return the Event Current Time in seconds
     */
    @Override
    public double getCurrentTime() {
        return currentTime;
    }
    
    /**
     * Creates a WDM LightPath Candidate to be used in Control Plane.
     *
     * @param src the source node in this lightpath
     * @param dst the destination node in this lightpath
     * @param links the links route of this lightpath
     * @param wavelengths the wavelengths used in this lightpath
     * @return the WDMLightPath object
     */
    @Override
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths) {
        return new WDMLightPath(1, src, dst, links, wavelengths);
    }
    
    /**
     * Creates a WDM LightPath Candidate to be used in Control Plane.
     *
     * @param src the source node in this lightpath
     * @param dst the destination node in this lightpath
     * @param links the links route of this lightpath
     * @param wavelengths the wavelengths used in this lightpath
     * @param typeProtection the type protection
     * @return the WDMLightPath object
     */
    @Override
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths, String typeProtection) {
        return new WDMLightPath(1, src, dst, links, wavelengths, typeProtection);
    }

    /**
     * Creates a EON LightPath Candidate to be used in Control Plane.
     *
     * @param src the source node in this lightpath
     * @param dst the destination node in this lightpath
     * @param links the links route of this lightpath
     * @param firstSlot the first subcarrier used in this lightpath
     * @param lastSlot the last subcarrier used in this lightpath
     * @param modulation the modulation used in each subcarrier
     * @return the EONLightPath object
     */
    @Override
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation) {
        return new EONLightPath(1, src, dst, links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
    }
    
    /**
     * Creates a SDM-EON LightPath Candidate to be used in Control Plane.
     *
     * @param src the source node in this lightpath
     * @param dst the destination node in this lightpath
     * @param links the links route of this lightpath
     * @param cores the cores chosen on your particular links
     * @param firstSlot the first subcarrier used in this lightpath
     * @param lastSlot the last subcarrier used in this lightpath
     * @param modulation the modulation used in each subcarrier
     * @return the EONLightPath object
     */
    @Override
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int[] cores, int firstSlot, int lastSlot, int modulation) {
        return new EONLightPath(1, src, dst, links, cores, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
    }
    
    /**
     * Creates a EON LightPath Candidate to be used in Control Plane.
     *
     * @param src the source node in this lightpath
     * @param dst the destination node in this lightpath
     * @param links the links route of this lightpath
     * @param firstSlot the first subcarrier used in this lightpath
     * @param lastSlot the last subcarrier used in this lightpath
     * @param modulation the modulation used in each subcarrier
     * @param typeProtection the type protection
     * @return the EONLightPath object
     */
    @Override
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation, String typeProtection) {
        return new EONLightPath(1, src, dst, links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize(), typeProtection);
    }

    /**
     * Checks the lightpaths continuity in multihop and if flow src and dst is
     * equal in lightpaths
     *
     * @param src the source node
     * @param dst the destination node
     * @param lightpaths the set of lightpaths
     * @return true if evething is ok, false otherwise
     */
    private boolean checkLightpathContinuity(int src, int dst, LightPath[] lightpaths) {
        if (src == lightpaths[0].getSource() && dst == lightpaths[lightpaths.length - 1].getDestination()) {
            for (int i = 0; i < lightpaths.length - 1; i++) {
                if (!(lightpaths[i].getDestination() == lightpaths[i + 1].getSource())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks the Paths continuity in multipath
     *
     * @param flow the flow requisition
     * @param paths the set of lightpaths
     * @return true if evething is ok, false otherwise
     */
    private boolean checkPathContinuity(Flow flow, Path[] paths) {
        for (Path path : paths) {
            if (!checkLightpathContinuity(flow.getSource(), flow.getDestination(), path.getLightpaths())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the SNR of this lightpaths
     *
     * @param lightpaths the lightpaths array
     * @return true if evething is ok, false otherwise
     */
    private boolean checkSNR(LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            if ((!vt.SNRCheck(lightpath))) {
                return false;
            }
        }
        return true;
    }
    
    /*metodo usado para saber se o cenario sem camada fisica trabalha corretamente de acordo com a camada fisica*/
    private boolean checkSNRTest(LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            if (!pi.testSNR(lightpath)) {
                return false;
            }
        }
        return true;
    }
    /**/
    
    /**
     * Checks the SNR Paths
     *
     * @param paths the set of lightpaths
     * @return true if evething is ok, false otherwise
     */
    private boolean checkSNR(Path[] paths) {
        for (Path path : paths) {
            if (!checkSNR(path.getLightpaths())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns witch transponders this lightpaths used.
     *
     * @param lightpaths the array of Lightpath object
     * @return the the number of transponders used.
     */
    private boolean[] usedTransponders(LightPath[] lightpaths) {
        boolean usedTransponders[] = new boolean[lightpaths.length];
        for (int i = 0; i < lightpaths.length; i++) {
            usedTransponders[i] = false;
            if (!vt.lightpathIsInOpticalGrooming(lightpaths[i].getID()) && vt.isLightpathIdle(lightpaths[i].getID())) {
                usedTransponders[i] = true;
            }
        }
        return usedTransponders;
    }
    
    private boolean[][] usedTransponders(LightPath[][] lightpaths) {
        boolean usedTransponders[][] = new boolean[lightpaths.length][];
        for (int i = 0; i < lightpaths.length; i++) {
            usedTransponders[i] = new boolean[lightpaths[i].length];
            for (int j = 0; j < lightpaths[i].length; j++) {
                usedTransponders[i][j] = false;
                if (lightpaths[i][j] != null) {
                    if (!vt.lightpathIsInOpticalGrooming(lightpaths[i][j].getID()) && vt.isLightpathIdle(lightpaths[i][j].getID())) {
                        usedTransponders[i][j] = true;
                    }
                }
            }
        }
        return usedTransponders;
    }
    
    /**
     * Returns the number of transponders this paths used.
     *
     * @param paths the array of Path object
     * @return the the number of transponders used.
     */
    private boolean[][] usedTransponders(Path[] paths) {
        int maxLpsPath = 0;
        for (Path path : paths) {
            if(maxLpsPath < path.getLightpaths().length) {
                maxLpsPath = path.getLightpaths().length;
            }
        }
        boolean usedTransponders[][] = new boolean[paths.length][maxLpsPath];
        for (int i = 0; i < paths.length; i++) {
            for (int j = 0; j < paths[i].getLightpaths().length; j++) {
                usedTransponders[i][j] = false;
                if (!vt.lightpathIsInOpticalGrooming(paths[i].getLightpaths()[j].getID()) 
                        && vt.isLightpathIdle(paths[i].getLightpaths()[j].getID()) 
                        && !paths[i].getLightpaths()[j].isReserved()) {
                    usedTransponders[i][j] = true;
                }
            }
        }
        return usedTransponders;
    }

    /**
     * Checks if this Paths are disjointed.
     *
     * @param primaryPaths the primary multipath
     * @param backupPaths the backup multipath
     * @return true if this Paths/Multipaths are disjointed, false otherwise
     */
    private boolean isDisjointedPaths(Path[] primaryPaths, Path[] backupPaths) {
        ArrayList<Integer> primaryLinks = new ArrayList<>();
        ArrayList<Integer> backupLinks = new ArrayList<>();
        for (Path path : primaryPaths) {
            for (LightPath lightpath : path.getLightpaths()) {
                for (int i = 0; i < lightpath.getLinks().length; i++) {
                    primaryLinks.add(lightpath.getLinks()[i]);
                }
            }
        }
        for (Path path : backupPaths) {
            for (LightPath lightpath : path.getLightpaths()) {
                for (int i = 0; i < lightpath.getLinks().length; i++) {
                    backupLinks.add(lightpath.getLinks()[i]);
                }
            }
        }
        for (Integer primaryLink : primaryLinks) {
            for (Integer backupLink : backupLinks) {
                if (Objects.equals(primaryLink, backupLink)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean addStaticLightPathProtect(LightPath[] lightpaths, boolean backup, boolean reservedProtect) {
        Flow pseudoFlow = new Flow(0, lightpaths[0].getSource(), lightpaths[lightpaths.length-1].getDestination(), getHighestBW(lightpaths), 0, 0);
        if (!canAddRateToPT(pseudoFlow.getRate(), lightpaths)) {
            return false;
        }
        if (!checkLightpathContinuity(pseudoFlow.getSource(), pseudoFlow.getDestination(), lightpaths)) {
            return false;
        }
        if (Simulator.simType >= 1) {
            //MODULATION CHECK (Only for RMLSA algorithms)
            if (pi.isPhysicalDistance()) {
                if (!vt.modulationPath(lightpaths)) {
                    return false;
                }
            }
            if (pi.isXTaware()) {
                if (!vt.checkCrosstalk(lightpaths)) {
                    return false;
                }
            }
            if (pi.isXTonOthers()) {
                if (!vt.checkCrosstalkOnOthers(lightpaths)) {
                    return false;
                }
            }
            //SNR CHECK (only for SNR checking of this lp)
            if (pi.isSNRaware()) {
                if (!checkSNR(lightpaths)) {
                    return false;
                }
            }
        }
        boolean usedTransponders[] = usedTransponders(lightpaths);
        if (reservedProtect) {
            for (LightPath lightpath : lightpaths) {
                lightpath.setReserved(true);
            }
        }
        if (backup) {
            for (LightPath lightpath : lightpaths) {
                lightpath.setBackup(true);
            }
        }
        tr.addStaticLightpath(lightpaths);
        st.addStaticLightpath(lightpaths, usedTransponders);
        arrayStaticPath.add(new Path(lightpaths));
        return true;

    }
    
    @Override
    public boolean addStaticLightPathProtect(Path[] primaryPaths, Path[] backupPaths, boolean reservedProtect) {
        MultiPathProtect multiPathProtect;
        MultiPath[] multiPath = new MultiPath[2];
        multiPath[0] = new MultiPath(primaryPaths);
        multiPath[1] = new MultiPath(backupPaths);
        multiPathProtect = new MultiPathProtect(multiPath);
        Flow pseudoFlow = new Flow(0, primaryPaths[0].getSource(), primaryPaths[0].getDestination(), primaryPaths[0].getBW(), 0, 0);

        if (!checkPathContinuity(pseudoFlow, primaryPaths) || !checkPathContinuity(pseudoFlow, backupPaths)) {
            return false;
        }

        if (!isDisjointedPaths(primaryPaths, backupPaths)) {
            return false;
        }
        if (Simulator.simType >= 1) {
            //MODULATION CHECK (Only for RMLSA algorithms)
            if (pi.isPhysicalDistance()) {
                if (!vt.modulationPath(primaryPaths) || !vt.modulationPath(backupPaths)) {
                    return false;
                }
            }
            if (pi.isXTaware()) {
                if (!vt.checkCrosstalk(primaryPaths) || !vt.checkCrosstalk(backupPaths)) {
                    return false;
                }
            }
            if (pi.isXTonOthers()) {
                if (!vt.checkCrosstalkOnOthers(primaryPaths) || !vt.checkCrosstalkOnOthers(backupPaths)) {
                    return false;
                }
            }
            //SNR CHECK (only for SNR checking of this lp)
            if (pi.isSNRaware()) {
                if (!checkSNR(primaryPaths) || !checkSNR(backupPaths)) {
                    return false;
                }
            }
        }
        boolean usedTranspondersPrimary[][] = usedTransponders(primaryPaths);
        boolean usedTranspondersBackup[][] = usedTransponders(backupPaths);
        //Set the backup index to primary lightpaths so that can search for it in the future
        for (int i = 0; i < primaryPaths.length; i++) {
            for (LightPath lightpath : primaryPaths[i].getLightpaths()) {
                if (lightpath.getLpBackup().isEmpty()) {
                    ArrayList<Long> backupIds = getbackupIdsLightptahs(lightpath, primaryPaths[i], backupPaths[i]);
                    for (Long backupId : backupIds) {
                        lightpath.addLpBackup(backupId);
                    }
                }
            }
        }
        if (reservedProtect) {
            //Reservando os primarios
            for (Path primaryPath : primaryPaths) {
                for (LightPath lightpath : primaryPath.getLightpaths()) {
                    lightpath.setReserved(true);
                }
            }
            //Reservando os backups
            for (Path backupPath : backupPaths) {
                for (LightPath lightpath : backupPath.getLightpaths()) {
                    lightpath.setReserved(true);
                    lightpath.setBackup(true);
                }
            }
        } else {
            for (Path backupPath : backupPaths) {
                for (LightPath lightpath : backupPath.getLightpaths()) {
                    lightpath.setBackup(true);
                }
            }
        }
        tr.addStaticLightpath(primaryPaths, backupPaths);
        st.addStaticLightpath(primaryPaths, backupPaths, usedTranspondersPrimary, usedTranspondersBackup);
        arrayStaticProtectPath.add(multiPathProtect);
        return true;
    }

    /**
     * Retrieves the ArrayList ids of this lightpats bakcup for this lightpath
     * primary
     *
     * @param lightpath the primary lightpath
     * @param backupPaths object Path thats contains the ids backups of this
     * lightpath
     * @return
     */
    private ArrayList<Long> getbackupIdsLightptahs(LightPath lightpath, Path primaryPath, Path backupPath) {
        ArrayList<Long> backupIds = new ArrayList<>();
        int i = 0;
        for (i = 0; i < backupPath.getLightpaths().length; i++) {
            LightPath lp = backupPath.getLightpaths()[i];
            if (!containsLpBkpInPath(primaryPath, lp) && lp.getSource() == lightpath.getSource()) {
                backupIds.add(lp.getID());
                break;
            }
        }
        for (i = i + 1; i < backupPath.getLightpaths().length; i++) {
            LightPath lp = backupPath.getLightpaths()[i];
            if (lp.getSource() != lightpath.getDestination()) {
                if (lp.getDestination() != lightpath.getDestination()) {
                    backupIds.add(lp.getID());
                } else {
                    backupIds.add(lp.getID());
                    break;
                }
            } else {
                break;
            }
        }
        return backupIds;
    }

    /**
     * Retrieves if lightpath contains in backups of this primaryPaths.
     *
     * @param primaryPath the primary path object
     * @param lp the LightPath objetct
     * @return true if lightpath contains in backups of this primaryPaths, false
     * otherwise.
     */
    private boolean containsLpBkpInPath(Path primaryPath, LightPath lp) {
        for (LightPath lightpath : primaryPath.getLightpaths()) {
            for (Long lpBackup : lightpath.getLpBackup()) {
                if (lpBackup == lp.getID()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the highest bandwith in this lighpaths array
     * @param lightpaths array of LightPath object
     * @return the highest bandwith in this lighpaths array
     */
    private int getHighestBW(LightPath[] lightpaths) {
        int bw = 0;
        for (LightPath lightpath : lightpaths) {
            if(lightpath instanceof EONLightPath) {
                if(((EONLightPath) lightpath).getBw() > bw) {
                    bw = ((EONLightPath) lightpath).getBw();
                }
            } else {
                if (lightpath instanceof WDMLightPath) {
                    if (((WDMLink) pt.getLink(((WDMLightPath) lightpath).links[0])).getBandwidth() > bw) {
                        bw = ((WDMLink) pt.getLink(((WDMLightPath) lightpath).links[0])).getBandwidth();
                    }
                }
            }
        }
        return bw;
    }
    
    private void removeAllLightPaths() {
        ArrayList<Long> ids = new ArrayList<>();
        Iterator<Map.Entry<Long, LightPath>> it = vt.getLightPaths().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, LightPath> entry = it.next();
            ids.add(entry.getKey());
        }
        for (Long id : ids) {
            vt.removeLightPath(id);
        }
    }

    private void newBulkData(BulkData bulkData) {
        activeBulks.put(bulkData.getID(), bulkData);
    }

    private void removeBulk(long id) {
        BulkData bulk;
        LightPath[] lightpaths;
        if (activeBulks.containsKey(id)) {
            bulk = activeBulks.get(id);
            if (mappedBulks.containsKey(bulk)) {
                lightpaths = mappedBulks.get(bulk).getLightpaths();
                for (LightPath lightpath : lightpaths) {
                    lightpath.removeBulk(bulk);
                }
                removeBulkFromPT(bulk, lightpaths);
                mappedBulks.remove(bulk);
            }
            activeBulks.remove(id);
        }
    }

    private void newBatch(Batch batch) {
        activeBatchs.put(batch.getID(), batch);
    }

    private void removeBatch(long id) {
        Batch batch;
        LightPath[] lightpaths;
        if (activeBatchs.containsKey(id)) {
            batch = activeBatchs.get(id);
            if (mappedBatchs.containsKey(batch)) {
                for (int i = 0; i < batch.getBulks().size(); i++) {
                    if (mappedBatchs.get(batch)[i] != null) {
                        lightpaths = mappedBatchs.get(batch)[i].getLightpaths();
                        for (LightPath lightpath : lightpaths) {
                            lightpath.removeBatch(batch);
                            for (BulkData bulk : batch.getBulks()) {
                                lightpath.removeBulk(bulk);
                            }
                        }
                        removeBulkFromPT(batch.getBulks().get(i), lightpaths);
                    }
                }
                mappedBatchs.remove(batch);
            }
            activeBatchs.remove(id);
        }
    }

    /**
     * Removes a given Bulk object from a Physical Topology.
     *
     * @param bulk the BulkData object that will be removed from the PT
     * @param lightpaths a list of LighPath objects
     */
    private void removeBulkFromPT(BulkData bulk, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            if (lightpath != null) {
                int rate = (int) Math.ceil(bulk.getDataAmount() / (bulk.getDepartureTime() - bulk.getServedTime()));
                pt.removeRate(rate, lightpath);
                // Can the lightpath be removed?
                if (!lightpath.isReserved()) {
                    if (vt.isLightpathIdle(lightpath.getID())) {
                        vt.removeLightPath(lightpath.getID());
                    }
                }
            }
        }
    }

    @Override
    public boolean acceptBulkData(long id, LightPath[] lightpaths, int rate) {
        BulkData bulk;
        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeBulks.containsKey(id)) {
                return false;
            }
            bulk = activeBulks.get(id);
            bulk.setServedTime(currentTime);
            bulk.setDepartureTime(currentTime + ((double) bulk.getDataAmount()/ (double) rate));
            if(bulk.getDepartureTime() > bulk.getDeadlineTime()) {
                return false;
            }
            if (!bulk.verifyRate(rate)) {
                return false;
            }
            if (!canAddRateToPT(rate, lightpaths)) {
                return false;
            }
            if (!checkLightpathContinuity(bulk.getSource(), bulk.getDestination(), lightpaths)) {
                return false;
            }
            if(Simulator.simType >= 1) {
                //MODULATION CHECK (Only for RMLSA algorithms)
                if(pi.isPhysicalDistance()) {
                    if(!vt.modulationPath(lightpaths)) {
                        return false;
                    }
                }
                if(pi.isXTaware()) {
                    if(!vt.checkCrosstalk(lightpaths)) {
                        return false;
                    }
                }
                if(pi.isXTonOthers()) {
                    if(!vt.checkCrosstalkOnOthers(lightpaths)) {
                        return false;
                    }
                }
                //SNR CHECK (only for SNR checking of this lp)
                if (pi.isSNRaware()) {
                    if (!checkSNR(lightpaths)) {
                        return false;
                    }
                }
            }
            boolean usedTransponders[] = usedTransponders(lightpaths);
            addRateToPT(rate, lightpaths);
            for (LightPath lightpath : lightpaths) {
                lightpath.addBulk(bulk);
            }
            mappedBulks.put(bulk, new Path(lightpaths));
            tr.acceptBulk(bulk, lightpaths);
            st.acceptBulk(bulk, lightpaths, usedTransponders);
            Event event = new BulkDepartureEvent(id);
            event.setTime(bulk.getDepartureTime());
            events.addEvent(event);
            return true;
        }
    }

    @Override
    public boolean blockBulkData(long id) {
        BulkData bulk;

        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeBulks.containsKey(id)) {
                return false;
            }
            bulk = activeBulks.get(id);
            if (mappedBulks.containsKey(bulk)) {
                return false;
            }
            activeBulks.remove(id);
            tr.blockBulk(bulk);
            st.blockBulk(bulk);
            return true;
        }
    }

    @Override
    public boolean acceptBatch(long id, LightPath[][] lightpaths, int[] rate) {
        Batch batch;
        int count = 0;
        if (id < 0 || lightpaths.length < 1) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeBatchs.containsKey(id)) {
                return false;
            }
            batch = activeBatchs.get(id);
            if (lightpaths.length < batch.getSize() || rate.length < batch.getSize()) {
                throw (new IllegalArgumentException());
            }
            batch.setServedTime(currentTime);
            batch.setDepartureTime(currentTime + (batch.getMaxDeparture(rate)), rate, currentTime);
            for (int i = 0; i < batch.getBulks().size(); i++) {
                if (rate[i] != 0) {
                    if (batch.getBulks().get(i).getDepartureTime() > batch.getBulks().get(i).getDeadlineTime()) {
                        return false;
                    }
                    if (!batch.getBulks().get(i).verifyRate(rate[i])) {
                        return false;
                    }
                    if (!canAddRateToPT(rate[i], lightpaths[i])) {
                        return false;
                    }
                    if (!checkLightpathContinuity(batch.getBulks().get(i).getSource(), batch.getBulks().get(i).getDestination(), lightpaths[i])) {
                        return false;
                    }
                    if (Simulator.simType >= 1) {
                        //MODULATION CHECK (Only for RMLSA algorithms)
                        if (pi.isPhysicalDistance()) {
                            if (!vt.modulationPath(lightpaths[i])) {
                                return false;
                            }
                        }
                        if (pi.isXTaware()) {
                            if (!vt.checkCrosstalk(lightpaths[i])) {
                                return false;
                            }
                        }
                        if (pi.isXTonOthers()) {
                            if (!vt.checkCrosstalkOnOthers(lightpaths[i])) {
                                return false;
                            }
                        }
                        //SNR CHECK (only for SNR checking of this lp)
                        if (pi.isSNRaware()) {
                            if (!checkSNR(lightpaths[i])) {
                                return false;
                            }
                        }
                    }
                    count++;
                }
            }
            if(count < this.byzantine) {
                return false;
            }
            boolean usedTransponders[][] = usedTransponders(lightpaths);
            Path[] paths = new Path[batch.getBulks().size()];
            for (int i = 0; i < batch.getBulks().size(); i++) {
                if (rate[i] != 0) {
                    addRateToPT(rate[i], lightpaths[i]);
                    paths[i] = new Path(lightpaths[i]);
                    for (LightPath lightpath : lightpaths[i]) {
                        lightpath.addBulk(batch.getBulks().get(i));
                        lightpath.addBatch(batch);
                    }
                }
            }
            mappedBatchs.put(batch, paths);
            tr.acceptBatch(batch, lightpaths);
            st.acceptBatch(batch, lightpaths, usedTransponders);
            Event event = new BatchDepartureEvent(id);
            event.setTime(batch.getDepartureTime());
            events.addEvent(event);
            return true;
        }
    }

    @Override
    public boolean blockBatch(long id) {
        Batch batch;

        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!activeBatchs.containsKey(id)) {
                return false;
            }
            batch = activeBatchs.get(id);
            if (mappedBatchs.containsKey(batch)) {
                return false;
            }
            activeBatchs.remove(id);
            tr.blockBatch(batch);
            st.blockBatch(batch);
            return true;
        }
    }

    @Override
    public Flow[] getFlows(LightPath lightpath) {
        ArrayList<Flow> flowsList = lightpath.getFlows();
        Flow[] flows = new Flow[flowsList.size()];
        for (int i = 0; i < flows.length; i++) {
            flows[i] = flowsList.get(i);
        }
        return flows;
    }
    
}
