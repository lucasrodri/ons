/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ons.ra;

import ons.EONLightPath;
import ons.EONLink;
import ons.EONPhysicalTopology;
import ons.Flow;
import ons.LightPath;
import ons.Modulation;
import ons.util.WeightedGraph;
import ons.util.YenKSP;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * The proposed Rui Wang Pseudo Partition (PP).
 * Article: "Spectrum Management in Heterogeneous Bandwidth Networks",
 * Rui Wang and Biswananth Mukherjee,
 * Globecom 2012 - Optical Networks and Systems Symposium 
 * If COS = 0 use FirstFit, if COS = 1 use LastFit
 */
public class PP implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        //The default modulation
        this.modulation = Modulation._BPSK;
    }
    
    @Override
    public void setModulation(int modulation) {
        this.modulation = modulation;
    }

    @Override
    public void simulationEnd() {   
    }
    
    @Override
    public void flowArrival(Flow flow) {
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];
        
        int ksp = 3;
        
        // Try existent lightpaths first (Grooming)
//        lps[0] = getLeastLoadedLightpath(flow);
//        if (lps[0] instanceof LightPath) {
//            if (cp.acceptFlow(flow.getID(), lps)) {
//                return;
//            }
//        }
        
        // k-Shortest Paths routing
        
        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);
        
        for (int k = 0; k < kpaths.length; k++) {

            nodes = route(kpaths,k);
            // If no possible path found, block the call
            if (nodes.length == 0 || nodes == null) {
                cp.blockFlow(flow.getID());
                return;
            }

            // Create the links vector
            links = new int[nodes.length - 1];
            for (int j = 0; j < nodes.length - 1; j++) {
                links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
            }
            /*
            //Get the distance the size in KM  link on the route
            double largestLinkKM = 0;
            for (int i = 0; i < links.length; i++) {
                largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(links[i])).getWeight();
            }
            //Adaptative modulation:
            int modulation = Modulation.getBestModulation(largestLinkKM);
            */
            // First-Fit spectrum assignment in BPSK Modulation
            int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
            
            int[] firstSlot;
            for (int i = 0; i < links.length; i++) {
                // Try the slots available in each link
                if(selectCOS(flow.getRate()) < 3){
                    firstSlot = ((EONLink) cp.getPT().getLink(links[i])).firstFit(requiredSlots);
                } else {//COS = 3
                    firstSlot = ((EONLink) cp.getPT().getLink(links[i])).lastFit(requiredSlots);
                }
                
                
                for (int j = 0; j < firstSlot.length; j++) {
                // Now you create the lightpath to use the createLightpath VT
                    //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
                    EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                            firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
                    // Now you try to establish the new lightpath, accept the call
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        // Single-hop routing (end-to-end lightpath)
                        lps[0] = cp.getVT().getLightpath(id);
                        if (!cp.acceptFlow(flow.getID(), lps)) {
                            cp.getVT().deallocatedLightpaths(lps);
                        } else {
                            return;
                        }
                    }
                }
            }
        }
        // Block the call
        cp.blockFlow(flow.getID());
    }

    @Override
    public void flowDeparture(long id) {
    }

    private int[] route(ArrayList<Integer>[] kpaths, int k) {
        if (kpaths[k] != null) {
            int[] path = new int[kpaths[k].size()];
            for (int i = 0; i < path.length; i++) {
                path[i] = kpaths[k].get(i);
            }
            return path;
        } else {
            return null;
        }
    }
    
    private LightPath getLeastLoadedLightpath(Flow flow) {
        long abw_aux, abw = 0;
        LightPath lp_aux, lp = null;

        // Get the available lightpaths
        TreeSet<LightPath> lps = cp.getVT().getAvailableLightpaths(flow.getSource(),
                flow.getDestination(), flow.getRate());
        if (lps != null && !lps.isEmpty()) {
            while (!lps.isEmpty()) {
                lp_aux = lps.pollFirst();
                // Get the available bandwidth
                abw_aux = cp.getVT().getLightpathBWAvailable(lp_aux.getID());
                if (abw_aux > abw) {
                    abw = abw_aux;
                    lp = lp_aux;
                }
            }
        }
        return lp;
    }
    private int selectCOS(int rate) {
        switch (rate){
            case 12500:
                return 0;
            case 25000:
                return 1;
            case 50000:
                return 2;
            case 100000:
                return 3;
            case 200000:
                return 4;
            case 400000:
                return 5;
            default:
                return -1;
        }
    }
}
