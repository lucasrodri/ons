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
import java.util.TreeSet;

/**
 * This is a sample algorithm for Rounting Modulation Level Spectrum Allocation (RMLSA) problem.
 *
 * The grooming algorithm tries to find the least loaded lightpath.
 * The modelation level adopted is the best given the distance from the chosen path.
 *
 * Fixed path routing is the simplest approach to finding a lightpath. The same
 * fixed route for a given source and destination pair is always used. This path
 * is computed using Dijkstra's Algorithm.
 *
 * First-Fit slots set assignment tries to establish the lightpath using the
 * first slots set available sought in the increasing order.
 */
public class My4RSA implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
    }
    
    @Override
    public void simulationEnd() {   
    }
    
    @Override
    public void setModulation(int modulation) {
    }

    @Override
    public void flowArrival(Flow flow) {
        //((EONLink) cp.getPT().getLink(6)).printLink();
        /**
         * Usar agregacao de dados, avaliar as distancias e atribuir uma modulacao nesse algoritmo.
        */
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];
        
        // Try existent lightpaths first (Grooming)
        lps[0] = getLeastLoadedLightpath(flow);
        if (lps[0] instanceof LightPath) {
            if (cp.acceptFlow(flow.getID(), lps)) {
                return;
            }
        }

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

        //Get the distance the size in KM  link on the route
        double largestLinkKM = 0;
        for (int i = 0; i < links.length; i++) {
            largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(links[i])).getWeight();
        }
        //Adaptative modulation:
        int modulation = Modulation.getBestModulation(largestLinkKM);
        
        // First-Fit spectrum assignment in some Modulation 
        int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
        
        for (int i = 0; i < links.length; i++){
            if (!((EONLink) cp.getPT().getLink(links[i])).hasSlotsAvaiable(requiredSlots)){
                cp.blockFlow(flow.getID()); 
                return;
            }
        }
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
        // Block the call
        cp.blockFlow(flow.getID());
    }

    @Override
    public void flowDeparture(long id) {
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
}
