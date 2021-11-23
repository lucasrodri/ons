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
import java.util.ArrayList;
import java.util.TreeSet;
import ons.util.Dijkstra;

/**
 * The proposed Pedro M Moura, Energy Aware Multigraph Shortest Path Algorithm (EAMGSP)
 * [Modulation best Power Consumption]. Article: "Algorithm for Energy Efficient Routing, 
 * Modulation and Spectrum Assignment", Pedro M. Moura, Rafael A. Scaraficci, Nelson L. S. da Fonseca,
 * IEEE ICC 2015, Communications QoS, Reliability and Modeling Symposium.
 */
public class EAMGSP implements RA {

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

        // Try existent lightpaths first (Grooming)
        /*
        lps[0] = getLeastLoadedLightpath(flow);
        if (lps[0] instanceof LightPath) {
            if (cp.acceptFlow(flow.getID(), lps)) {
                return;
            }
        }
        */
        int numberOfModulations = Modulation.N_MOD;
        double[] listEfficientModulation = new double[numberOfModulations];
        for (int i = 0; i < numberOfModulations; i++) {
            listEfficientModulation[i] = Double.MAX_VALUE;
        }
        // k-Shortest Paths routing

        nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());
        
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
        
        //Get the distance the size in KM  link on the route
        double largestLinkKM = 0;
        for (int i = 0; i < links.length; i++) {
            largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(links[i])).getWeight();
        }
        
        for (int m = 0; m < numberOfModulations; m++) {
            
            //Adaptative modulation:
            int modulation = Modulation.getBestModulation(largestLinkKM);
            if (modulation >= m) {

                // First-Fit spectrum assignment in BPSK Modulation
                int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), m);

                int[] firstSlot;

                // Try the slots available in each link
                firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(requiredSlots);
                for (int j = 0; j < firstSlot.length; j++) {
                    // Now you create the lightpath to use the createLightpath VT
                    //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
                    EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                            firstSlot[j], (firstSlot[j] + requiredSlots - 1), m);
                    // Now you try to establish the new lightpath, accept the call
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        // Single-hop routing (end-to-end lightpath)
                        lps[0] = cp.getVT().getLightpath(id);
                        listEfficientModulation[m] = getPowerConsumption(lps[0]);
                        cp.getVT().deallocatedLightpaths(lps);
                        break;
                    }
                }
            }

        }

        int leastModulationIndex = 0;
        for (int m = 0; m < listEfficientModulation.length; m++) {
            if (listEfficientModulation[m] < listEfficientModulation[leastModulationIndex]) {
                leastModulationIndex = m;
            }
        }

        if (listEfficientModulation[leastModulationIndex] != Integer.MAX_VALUE) {
            
            // First-Fit spectrum assignment in BPSK Modulation
            int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), leastModulationIndex);

            int[] firstSlot;

            // Try the slots available in each link
            firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(requiredSlots);
            for (int j = 0; j < firstSlot.length; j++) {
                // Now you create the lightpath to use the createLightpath VT
                //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
                EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                        firstSlot[j], (firstSlot[j] + requiredSlots - 1), leastModulationIndex);
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
    
    private double getPowerConsumption(LightPath lp) {
        return cp.getVT().getPowerConsumption(lp);
    }
     

}
