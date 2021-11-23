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
 * This is a sample algorithm for the Routing and Spectrum Alocation problem.
 *
 * Fixed path routing is the simplest approach to finding a lightpath. The same
 * fixed route for a given source and destination pair is always used. This path
 * is computed using Dijkstra's Algorithm.
 *
 * First-Fit wavelength assignment tries to establish the lightpath using the
 * first slots set available sought in the increasing order.
 */
public class MyRSA implements RA {
    
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
    public void simulationEnd() {   
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

        // First-Fit spectrum assignment in BPSK Modulation
        int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
        for (int i = 0; i < links.length; i++){
            if (!((EONLink) cp.getPT().getLink(links[i])).hasSlotsAvaiable(requiredSlots)){
                cp.blockFlow(flow.getID()); 
                return;
            }
        }
        int firstSlot;
        for (int i = 0; i < links.length; i++){
            // Try the first slots available in each link
            firstSlot = ((EONLink) cp.getPT().getLink(links[i])).getFirstSlotAvailable(requiredSlots);
            // Now you create the lightpath to use the createLightpath VT
            //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
            EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                        firstSlot, (firstSlot + requiredSlots - 1), modulation);
            // Now you try to establish the new lightpath, accept the call
            if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                // Single-hop routing (end-to-end lightpath)
                lps[0] = cp.getVT().getLightpath(id);
                if (cp.acceptFlow(flow.getID(), lps)) {
                    return;
                } else {
                    //something wrong
                    //dealocates the lightpath in VT and try again
                    cp.getVT().deallocatedLightpath(id);
                }
            }
        }
        // Block the call
        cp.blockFlow(flow.getID());
    }

    @Override
    public void flowDeparture(long id) {
    }
}
