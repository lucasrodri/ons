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

/**
 * The proposed m-Adap by Xin Wan in this RSA solver.
 * @author lucas
 */
public class MAdapSPTG implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
    }
    
    @Override
    public void setModulation(int modulation) {
    }

    @Override
    public void simulationEnd() {   
    }
    
    @Override
    public void flowArrival(Flow flow) {
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];//esse algoritmo nao faz saltos na VT
        
        //parametros:
        int ksp = 3;
        int modulation = EONPhysicalTopology.getMaxModulation();
        
        while (true) {

            // k-Shortest Paths routing disjoint first
            //PASSO 1
            ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);
            //PASSO 2
            ArrayList<LightPath> lpsSource = cp.getVT().getLightpathsSrc(flow.getSource());
            for (ArrayList<Integer> path : kpaths) {
                //PASSO 3
                if(modulationPath(path, modulation)){
                    LightPath candidate = searchCandidate(path, lpsSource);
                    while (candidate != null) {
                        //PASSO 4
                        lps[0] = createLightPathInOpticalGrooming(path, flow.getRate(), modulation, candidate);
                        //PASSO 5
                        if (lps[0] != null) {
                            cp.acceptFlow(flow.getID(), lps);
                            return;
                        } else {
                            lpsSource.remove(candidate);
                        }
                        //PASSO 6
                        candidate = searchCandidate(path, lpsSource);
                    }
                }
            }
            //PASSO 7:
            // k-Shortest Paths routing
            kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);
            for (int k = 0; k < kpaths.length; k++) {
                if (modulationPath(kpaths[k], modulation)) {
                    nodes = route(kpaths, k);
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
                    // First-Fit spectrum assignment
                    int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
                    int[] firstSlot;
                    for (int i = 0; i < links.length; i++) {
                        // Try the slots available in each link
                        firstSlot = ((EONLink) cp.getPT().getLink(links[i])).getSlotsAvailableToArray(requiredSlots);
                        for (int j = 0; j < firstSlot.length; j++) {
                    // Now you create the lightpath to use the createLightpath VT
                            //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
                            EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                                    firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
                            // Now you try to establish the new lightpath, accept the call
                            if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                                // Single-hop routing (end-to-end lightpath)
                                lps[0] = cp.getVT().getLightpath(id);
                                cp.acceptFlow(flow.getID(), lps);
                                return;
                            }
                        }
                    }
                }
            }
            modulation--;
            if (modulation == -1) {
                // Block the call
                cp.blockFlow(flow.getID());
                return;
            }
        }
        //PASSO 8:
        // Block the call
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
    
    /**
     * Find the best lightpath candidate for optical grooming.
     * The lightpath with more links in common
     * @param path the path nodes
     * @param lpsSource the set os lightptahs in this source
     * @return the best candidate lightpath
     */
    private LightPath searchCandidate(ArrayList<Integer> path, ArrayList<LightPath> lpsSource) {
        ArrayList<LightPath> lpsCandidates = new ArrayList<>();
        // Create the linkPath
        int[] linkPath = new int[path.size() - 1];
        for (int j = 0; j < path.size() - 1; j++) {
            linkPath[j] = cp.getPT().getLink(path.get(j), path.get(j + 1)).getID();
        }
        //os que tem o primeito link em comum (PRIMEIRA TRIAGEM)
        for (LightPath lpSrc : lpsSource) {
            if (lpSrc.getLinks()[0] == linkPath[0]) {
                lpsCandidates.add(lpSrc);
            }
        }
        //destes, os que tem mais links em comum (SEGUNDA TRIAGEM)...
        int commonLinks_aux = 0;
        int commonLinks;
        LightPath finalyCandidate = null;
        
        for (LightPath candidate : lpsCandidates) {
            commonLinks = 1;
            for (int j = 1; j < candidate.getLinks().length && j < linkPath.length; j++) {
                if (candidate.getLinks()[j] == linkPath[j]) {
                    commonLinks++;
                }
            }
            if (commonLinks > commonLinks_aux) {
                finalyCandidate = candidate;
                commonLinks_aux = commonLinks;
            }
        }
        return finalyCandidate;
    }

    /**
     * Create a new lightpath in optical grooming.
     * @param path the path of this lightpath (nodes)
     * @param rate the flow rate
     * @param modulation the modulation format
     * @param candidate the lightpath will be groomed
     * @return the lightpath, or null if is not possible to created
     */
    private LightPath createLightPathInOpticalGrooming(ArrayList<Integer> path, int rate, int modulation, LightPath candidate) {
        LightPath lp = null;
        long id;
        if (modulation == ((EONLightPath) candidate).getModulation()) {
            if ((id = cp.getVT().createLightpathInOpticalGrooming(path, candidate, rate)) >= 0) {
                return cp.getVT().getLightpath(id);
            }
        }
        return lp;
    }

    private boolean modulationPath(ArrayList<Integer> path, int modulation) {
        double pathLength = 0;
        for (int j = 0; j < path.size() - 1; j++) {
            pathLength += cp.getPT().getLink(path.get(j), path.get(j + 1)).getWeight();
        }
        return pathLength <= (double) Modulation.getModulationReach(modulation);
    }
}
