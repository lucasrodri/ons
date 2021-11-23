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
import ons.util.Dijkstra;
import ons.util.WeightedGraph;

/**
 * This is a sample algorithm for the Routing and Spectrum Core Alocation problem.
 *
 * Fixed path routing is the simplest approach to finding a lightpath. The same
 * fixed route for a given source and destination pair is always used. This path
 * is computed using Dijkstra's Algorithm.
 *
 * First-Fit slots set assignment tries to establish the lightpath using the
 * first slots set available sought in the increasing order.
 */
public class SP_RSCA implements RA {
    
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
    public void flowArrival(Flow flow) {
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];

        // Shortest-Path routing
        nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());
        
        // If no possible path found, block the call
        if (nodes.length == 0) {
            cp.blockFlow(flow.getID()); 
            return;
        }

        // Create the links vector
        links = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
        }

        int requiredSlots;
        requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
        for (int i = 0; i < links.length; i++){
            if (!((EONLink) cp.getPT().getLink(links[i])).hasSlotsAvaiable(requiredSlots)){
                cp.blockFlow(flow.getID()); 
                return;
            }
        }
        int[] firstSlot;
        
        int numCores = ((EONLink) cp.getPT().getLink(links[0])).getNumCores();
        int[] cores = new int[links.length];
        for (int core = 0; core < numCores; core++) {
            firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(core, requiredSlots);
            for (int slot = 0; slot < firstSlot.length; slot++) {
                clearCores(cores);
                cores[0] = core;
                for (int link = 1; link < links.length; link++) {
                    for (int coreAux = 0; coreAux < numCores; coreAux++) {
                        if(((EONLink) cp.getPT().getLink(links[link])).areSlotsAvaiable(coreAux, firstSlot[slot], (firstSlot[slot] + requiredSlots - 1))){
                            cores[link] = coreAux;
                            break;
                        }
                    }
                    if(cores[link] == -1) {
                        break;
                    }
                }
                if(checkCores(cores)) {
                    EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                            cores, firstSlot[slot], (firstSlot[slot] + requiredSlots - 1), modulation);
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
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

    @Override
    public void simulationEnd() {
    }

    private void clearCores(int[] cores) {
        for (int i = 0; i < cores.length; i++) {
            cores[i] = -1;
        }
    }
    
    private boolean checkCores(int[] cores) {
        for (int i = 0; i < cores.length; i++) {
            if(cores[i] == -1) {
                return false;
            }
        }
        return true;
    }
}
