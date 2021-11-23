/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ons.ra;

import ons.EONLightPath;
import ons.LightPath;
import ons.Flow;
import ons.MultiPath;
import ons.MultiPathProtect;
import ons.Path;
import ons.PhysicalTopology;
import ons.VirtualTopology;
import ons.WDMLightPath;
import java.util.Map;

/**
 * This is the interface that provides several methods for the
 * RWA Class within the Control Plane.
 * 
 * @author andred
 */
public interface ControlPlaneForRA {

    public boolean acceptFlow(long id, LightPath[] lightpaths);
    
    public boolean acceptFlow(long id, Path[] paths, int[] bws);
    
    public boolean acceptFlow(long id, Path[] primaryPaths, int[] primaryBWS, Path[] backupPaths, int[] backupBWS, boolean reservedProtect, boolean shared);

    public boolean blockFlow(long id);
    
    public boolean acceptBulkData(long id, LightPath[] lightpaths, int rate);
    
    public boolean blockBulkData(long id);
    
    public boolean acceptBatch(long id, LightPath[][] lightpaths, int[] rate);
    
    public boolean blockBatch(long id);
    
    public boolean rerouteFlow(long id, LightPath[] lightpaths);
    
    public Flow getFlow(long id);
    
    public Path getPath(Flow flow);
    
    public MultiPath getMultiPath(Flow flow);
    
    public int getLightpathFlowCount(long id);

    public Map<Flow, Path> getMappedFlowsSinglePath();
    
    public Map<Flow, MultiPath> getMappedFlowsMultiPath();
    
    public Map<Flow, MultiPathProtect> getMappedFlowsMultiPathProtect();

    public PhysicalTopology getPT();
    
    public VirtualTopology getVT();
    
    public double getCurrentTime();
    
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths);
    
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths, String typeProtection);
    
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation);
    
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int[] cores, int firstSlot, int lastSlot, int modulation);
    
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation, String typeProtection);
    
    public boolean addStaticLightPathProtect(Path[] primaryPaths, Path[] backupPaths, boolean reservedProtect);
    
    public boolean addStaticLightPathProtect(LightPath[] lightpaths, boolean bakcup, boolean reservedProtect);

    public Flow[] getFlows(LightPath lightpath);
}