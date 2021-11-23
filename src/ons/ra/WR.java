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
 * The proposed Xin Wan.
 * Article: "Minimum- and Maximum-Entropy Routing and Spectrum Assignment for Flexgrid Elastic Optical Networking [Invited]",
 * Paul Wright, Michael C. Parker, and Andrew Lord,
 * J. OPT. COMMUN. NETW./VOL. 7, NO. 1/JANUARY 2015, 
 * @author lucas
 */
public class WR implements RA {

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

        int ksp = 8;
        double[] frag = new double[ksp];
        for (int i = 0; i < ksp; i++) {
            frag[i] = Double.MAX_VALUE;
        }
        // k-Shortest Paths routing

        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);
        //frag = fragAvaliation(kpaths, 0);

        for (int k = 0; k < kpaths.length; k++) {

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
                        frag[k] = ((EONPhysicalTopology) cp.getPT()).getFragmentationWright(links);
                        cp.getVT().deallocatedLightpaths(lps);
                        frag[k] = frag[k] - ((EONPhysicalTopology) cp.getPT()).getFragmentationWright(links);
                        i = links.length;
                        break;
                    }
                }
            }
        }
        
        int indexMenor = 0;
        for (int m = 0; m < frag.length; m++) {
            if (frag[m] < frag[indexMenor]) {
                indexMenor = m;
            }
        }
        
        if (frag[indexMenor] != Double.MAX_VALUE) {
            nodes = route(kpaths, indexMenor);

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

    /**
     *
     * @param kpaths
     * @param i indice da métrica
     * @return vetor de indice da fragmentação de cada caminho do kpaths do
     * menor para maior
     */
    private int[] fragAvaliation(ArrayList<Integer>[] kpaths, int i) {
        int[] nodes, fragmentation, links;
        double[] frag = new double[kpaths.length];
        fragmentation = new int[kpaths.length];
        for (int k = 0; k < kpaths.length; k++) {

            nodes = route(kpaths, k);
            // Create the links vector
            links = new int[nodes.length - 1];
            for (int j = 0; j < nodes.length - 1; j++) {
                links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
            }
            switch (i) {
                case 0:
                    frag[k] = ((EONPhysicalTopology) cp.getPT()).getExFragmentation(links);
                    break;
                case 1:
                    frag[k] = ((EONPhysicalTopology) cp.getPT()).getFragmentationWu(links);
                    break;
                case 2:
                    frag[k] = ((EONPhysicalTopology) cp.getPT()).getFragmentationWright(links);
                    break;
                case 3:
                    frag[k] = ((EONPhysicalTopology) cp.getPT()).getFragmentationWang(links);
                    break;
                case 4:
                    frag[k] = ((EONPhysicalTopology) cp.getPT()).getFragmentationEntropy(links);
                    break;
                case 5:
                    frag[k] = ((EONPhysicalTopology) cp.getPT()).getFragmentationSingh(links);
                    break;
            }
        }

        for (int m = 0; m < frag.length; m++) {
            int menor = 0;
            for (int j = 0; j < frag.length; j++) {
                if (frag[j] < frag[menor]) {
                    menor = j;
                }
            }
            fragmentation[m] = menor;
            frag[menor] = Double.MAX_VALUE;
        }

        return fragmentation;
    }

}
