/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.ra;

import java.util.TreeSet;
import ons.Flow;
import ons.LightPath;
import ons.WDMLightPath;
import ons.WDMPhysicalTopology;
import ons.util.Dijkstra;
import ons.util.WeightedGraph;

/**
 * This is a sample algorithm for the Grooming problem.
 *
 * The grooming algorithm tries to find the least loaded lightpath.
 *
 * Fixed path routing is the simplest approach to finding a lightpath. The same
 * fixed route for a given source and destination pair is always used. This path
 * is computed using Dijkstra's Algorithm.
 *
 * First-Fit wavelength assignment tries to establish the lightpath using the
 * first wavelength available sought in the increasing order.
 */
public class My2RWA implements RA {
    
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
    public void flowArrival(Flow flow) {
        int[] nodes;
        int[] links;
        int[] wvls;
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

        // First-Fit wavelength assignment
        wvls = new int[links.length];
        for (int i = 0; i < ((WDMPhysicalTopology) cp.getPT()).getNumWavelengths(); i++) {
            // Create the wavelengths vector
            for (int j = 0; j < links.length; j++) {
                wvls[j] = i;
            }
            // Now you create the lightpath to use the createLightpath VT
            WDMLightPath lp = new WDMLightPath(1, flow.getSource(), flow.getDestination(), links, wvls);
            // Now you try to establish the new lightpath, accept the call
            if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                // Single-hop routing (end-to-end lightpath)
                lps[0] = cp.getVT().getLightpath(id);
                cp.acceptFlow(flow.getID(), lps);
                return;
            }
        }
        // Block the call
        cp.blockFlow(flow.getID());
    }

    /**
     * Tries to find the Least-Loaded Lightpath considering the flow source
     * destination and bandwidth requirements.
     *
     * @param flow  the flow object.
     * @return lp   the least loaded lightpath, or null otherwise.
     */
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

    @Override
    public void flowDeparture(long id) {
    }

    @Override
    public void setModulation(int modulation) {
    }
    
}
