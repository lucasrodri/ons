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
 * First-Fit slots set assignment tries to establish the lightpath using the
 * first slots set available sought in the increasing order.
 */
public class DP implements RA {

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
        for (int i = 0; i < links.length; i++) {
            if (!((EONLink) cp.getPT().getLink(links[i])).hasSlotsAvaiable(requiredSlots)) {
                cp.blockFlow(flow.getID());
                return;
            }
        }
        int[] firstSlot;

        for (int i = 0; i < links.length; i++) {
            // Try the slots available in each link
            firstSlot = ((EONLink) cp.getPT().getLink(links[i])).firstFit(requiredSlots);
            for (int j = 0; j < firstSlot.length; j++) {
                if (isRange(firstSlot[j], selectCOS(flow.getRate()))) {
                    // Now you create the lightpath to use the createLightpath VT
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
        // Block the call
        cp.blockFlow(flow.getID());
    }

    @Override
    public void flowDeparture(long id) {
    }

    private boolean isRange(int indexLink, int cos) {
        double sizeLink = ((EONLink) cp.getPT().getLink(0)).getNumSlots();
        switch (cos) {
            case 0:
                if (indexLink <= (int) Math.ceil(sizeLink * 0.05)) {
                    return true;
                }
                break;
            case 1:
                if (indexLink > (int) Math.ceil(sizeLink * 0.05) && indexLink <= (int) Math.ceil(sizeLink * 0.1333)) {
                    return true; 
                }
                break;
            case 2:
                if (indexLink > (int) Math.ceil(sizeLink * 0.1333) && indexLink <= (int) Math.ceil(sizeLink * 0.2666)) {
                    return true; 
                }
                break;
            case 3:
                if (indexLink > (int) Math.ceil(sizeLink * 0.2666) && indexLink <= (int) Math.ceil(sizeLink * 0.4666)) {
                    return true; 
                }
                break;
            case 4:
                if (indexLink > (int) Math.ceil(sizeLink * 0.4666) && indexLink <= (int) Math.ceil(sizeLink * 0.7332)) {
                    return true; 
                }
                break;
            case 5:
                if (indexLink > (int) Math.ceil(sizeLink * 0.7332) && indexLink <= (int) sizeLink) {
                    return true; 
                }
                break;
            default:
                return true;
        }
        return false;
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
