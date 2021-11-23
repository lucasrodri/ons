/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import ons.EONLightPath;
import ons.Flow;
import ons.LightPath;
import ons.MultiPath;
import ons.MultiPathProtect;
import ons.MyStatistics;
import ons.Path;
import ons.PhysicalTopology;
import ons.VirtualTopology;
import ons.WDMLightPath;
import ons.ra.ControlPlaneForRA;
import ons.ra.RA;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is designed only for use by adaptive modulation models that use RA
 * class in its implementation. This class have specific differences in several
 * methods, pay attention.
 *
 * @author lucas
 */
public class PseudoControlPlane implements ControlPlaneForRA {

    private final ControlPlaneForRA cp;
    private final RA rsa;
    private final Map<Long, Path> mappedFlows;
    private final ArrayList<LightPath> lpsAccepts;
    private long currentId;
    protected MyStatistics st = MyStatistics.getMyStatisticsObject();

    public PseudoControlPlane(ControlPlaneForRA cp, RA rsa) {
        this.cp = cp;
        this.rsa = rsa;
        this.currentId = -1;
        this.lpsAccepts = new ArrayList<>();
        mappedFlows = new HashMap<>();
    }

    @Override
    public boolean acceptFlow(long id, LightPath[] lightpaths) {
        if (this.currentId != id) {
            this.currentId = id;
            this.lpsAccepts.clear();
        }
        if (!canAddFlowToPT(getFlow(id), lightpaths)) {
            this.lpsAccepts.clear();
            return false;
        }
        //MODULATION CHECK (somente para algoritmos RMLSA)
        if (!getVT().modulationPath(lightpaths)) {
            return false;
        }
        this.lpsAccepts.addAll(Arrays.asList(lightpaths));
        addFlowToPT(getFlow(id), lightpaths);
        if (!mappedFlows.containsKey(id)) {
            mappedFlows.put(id, new Path(lightpaths));
        } else {
            mappedFlows.get(id).addLightpaths(lightpaths);
        }
        return true;
    }

    @Override
    public boolean blockFlow(long id) {
        if (this.currentId != id) {
            this.currentId = id;
            mappedFlows.remove(id);
            this.lpsAccepts.clear();
        }
        //tem que fazer o flowDeparture tbm so se nao for blk de cara
        if (mappedFlows.containsKey(id)) {
            rsa.flowDeparture(id);
        }
        //faz o remove flow aki desses lps
        removeFlowFromPT(cp.getFlow(id), lpsAccepts);
        mappedFlows.remove(id);
        this.lpsAccepts.clear();
        return true;
    }

    @Override
    public boolean rerouteFlow(long id, LightPath[] lightpaths) {
        return cp.rerouteFlow(id, lightpaths);
    }

    @Override
    public Flow getFlow(long id) {
        return cp.getFlow(id);
    }

    @Override
    public Path getPath(Flow flow) {
        return mappedFlows.get(flow.getID());
    }

    @Override
    public int getLightpathFlowCount(long id) {
        return cp.getLightpathFlowCount(id);
    }

    @Override
    public Map<Flow, Path> getMappedFlowsSinglePath() {
        Map<Flow, Path> map = new HashMap<>();
        map.putAll(cp.getMappedFlowsSinglePath());//coloco todos do cp original
        if (mappedFlows.containsKey(currentId)) {
            map.put(cp.getFlow(currentId), mappedFlows.get(currentId));//coloco o path do id atual
        }
        return map;
    }

    @Override
    public PhysicalTopology getPT() {
        return cp.getPT();
    }

    @Override
    public VirtualTopology getVT() {
        return cp.getVT();
    }

    @Override
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths) {
        return cp.createCandidateWDMLightPath(src, dst, links, wavelengths);
    }

    @Override
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation) {
        return cp.createCandidateEONLightPath(src, dst, links, firstSlot, lastSlot, modulation);
    }

    /**
     * Adds a Flow object to a Physical Topology. This means adding the flow to
     * the network's traffic, which simply decreases the available bandwidth.
     *
     * @param flow the Flow object to be added
     * @param lightpaths list of LightPath objects the flow uses
     */
    private void addFlowToPT(Flow flow, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            cp.getPT().addRate(flow.getRate(), lightpath);
        }
    }

    /**
     * Removes a given Flow object from a Physical Topology.
     *
     * @param flow the Flow object that will be removed from the PT
     * @param lightpaths a list of LighPath objects
     */
    private void removeFlowFromPT(Flow flow, ArrayList<LightPath> lightpaths) {
        for (LightPath lightpath : lightpaths) {
            cp.getPT().removeRate(flow.getRate(), lightpath);
            if (cp.getVT().isLightpathIdle(lightpath.getID())) {
                cp.getVT().deallocatedLightpath(lightpath.getID());
            }
        }
    }

    /**
     * Says whether or not a given Flow object can be added to a determined
     * Physical Topology, based on the amount of bandwidth the flow requires
     * opposed to the available bandwidth.
     *
     * @param flow the Flow object to be added
     * @param lightpaths list of LightPath objects the flow uses
     * @return true if Flow object can be added to the PT, or false if it can't
     */
    private boolean canAddFlowToPT(Flow flow, LightPath[] lightpaths) {
        for (LightPath lightpath : lightpaths) {
            if (!cp.getPT().canAddRate(flow.getRate(), lightpath)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean acceptFlow(long id, Path[] paths, int[] bws) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public MultiPath getMultiPath(Flow flow) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Flow, MultiPath> getMappedFlowsMultiPath() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public boolean acceptFlow(long id, Path[] paths, int[] bws, boolean reservedProtect){
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Flow, MultiPathProtect> getMappedFlowsMultiPathProtect() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addStaticLightPathProtect(Path[] primaryPaths, Path[] backupPaths, boolean reservedProtect) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addStaticLightPathProtect(LightPath[] lightpaths, boolean backup, boolean reservedProtect) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths, String typeProtection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation, String typeProtection) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean acceptFlow(long id, Path[] primaryPaths, int[] primaryBWS, Path[] backupPaths, int[] backupBWS, boolean reservedProtect, boolean shared) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean acceptBulkData(long id, LightPath[] lightpaths, int rate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean blockBulkData(long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean acceptBatch(long id, LightPath[][] lightpaths, int[] rate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean blockBatch(long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getCurrentTime() {
        return cp.getCurrentTime();
    }

    @Override
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int[] cores, int firstSlot, int lastSlot, int modulation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Flow[] getFlows(LightPath lightpath) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
