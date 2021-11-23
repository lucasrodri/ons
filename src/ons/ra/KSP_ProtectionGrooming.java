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
import ons.MultiPathProtect;
import ons.Path;
import ons.util.KSPOffline;
import ons.util.WeightedGraph;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * @author lucas
 */
public class KSP_ProtectionGrooming implements RA {

    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;
    private KSPOffline routes;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        this.modulation = Modulation._BPSK;
        int ksp = 40;
        this.routes = KSPOffline.getKSPOfflineObject(graph, ksp);
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
        int[] links = null;
        long id;
        LightPath[] lps = new LightPath[1];
        LightPath[] lpsBackup = new LightPath[1];
        Path[] primary = new Path[1], backup = new Path[1];

        // Try existent lightpaths first (Electral Grooming)
        MultiPathProtect groomingMultiPathProtect = getLeastLoadedPrimaryMultiPathProtect(flow);
        if (groomingMultiPathProtect instanceof MultiPathProtect) {
            primary = groomingMultiPathProtect.getPrimary().getPaths();
            backup = groomingMultiPathProtect.getBackup().getPaths();
            int[] bws = {flow.getRate()};
            if (!cp.acceptFlow(flow.getID(), primary, bws, backup, bws, false, false)) {
                return;
            }
        }

        // k-Shortest Paths routing
        ArrayList<Integer>[] kpaths = routes.getKShortestPaths(flow.getSource(), flow.getDestination());
        int k;
        boolean flag = false;
        for (k = 0; k < kpaths.length; k++) {

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

            //Get the distance the size in KM  link on the route
            double largestLinkKM = 0;
            for (int i = 0; i < links.length; i++) {
                largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(links[i])).getWeight();
            }
            //Adaptative modulation:
            int modulation = Modulation.getBestModulation(largestLinkKM);

            // First-Fit spectrum assignment in BPSK Modulation
            int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);

            int[] firstSlot;
            // Try the slots available in each link
            firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(requiredSlots);
            for (int j = 0; j < firstSlot.length; j++) {
                // Now you create the lightpath to use the createLightpath VT
                //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
                EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                        firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
                // Now you try to establish the new lightpath, accept the call
                if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                    // Single-hop routing (end-to-end lightpath)
                    lps[0] = cp.getVT().getLightpath(id);
                    primary[0] = new Path(lps);
                    flag = true;
                    break;
                }
            }
            if (flag) {
                break;
            }
        }
        //Backup
        for (k = k + 1; k < kpaths.length; k++) {
            int[] nodesBackup = route(kpaths, k);
            // If no possible path found, block the call
            if (nodesBackup.length == 0 || nodesBackup == null) {
                if (flag) {
                    cp.getVT().deallocatedLightpaths(lps);
                }
                cp.blockFlow(flow.getID());
                return;
            }
            // Create the links vector
            int[] linksBackup = new int[nodesBackup.length - 1];
            for (int j = 0; j < nodesBackup.length - 1; j++) {
                linksBackup[j] = cp.getPT().getLink(nodesBackup[j], nodesBackup[j + 1]).getID();
            }
            if (isDisjointed(links, linksBackup)) {
                double largestLinkKM = 0;
                for (int i = 0; i < linksBackup.length; i++) {
                    largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(linksBackup[i])).getWeight();
                }

                int modulation = Modulation.getBestModulation(largestLinkKM);
                int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);

                int[] firstSlot;
                firstSlot = ((EONLink) cp.getPT().getLink(linksBackup[0])).firstFit(requiredSlots);
                for (int j = 0; j < firstSlot.length; j++) {
                    EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), linksBackup,
                            firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        lpsBackup[0] = cp.getVT().getLightpath(id);
                        backup[0] = new Path(lpsBackup);
                        int[] bws = {flow.getRate()};
                        if (!cp.acceptFlow(flow.getID(), primary, bws, backup, bws, false, false)) {
                            throw (new IllegalArgumentException());
                        } else {
                            return;
                        }
                    }
                }
            }
        }
        if (flag) {
            cp.getVT().deallocatedLightpaths(lps);
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

    private MultiPathProtect getLeastLoadedPrimaryMultiPathProtect(Flow flow) {
        int min = Integer.MAX_VALUE;
        MultiPathProtect mpp = null;
        Iterator<Entry<Flow, MultiPathProtect>> itr = cp.getMappedFlowsMultiPathProtect().entrySet().iterator();
        while (itr.hasNext()) {
            Entry<Flow, MultiPathProtect> entry = itr.next();
            if (entry.getKey().getSource() == flow.getSource()
                    && entry.getKey().getDestination() == flow.getDestination()) {
                if (entry.getValue().getTotalBWPrimaryAvailable() >= flow.getRate()
                        && entry.getValue().getTotalBWBackupAvailable() >= flow.getRate()) {
                    if (min > entry.getValue().getTotalBWPrimaryAvailable()) {
                        min = entry.getValue().getTotalBWPrimaryAvailable();
                        mpp = entry.getValue();
                    }
                }
            }
        }
        return mpp;
    }

    private boolean isDisjointed(int[] links, int[] linksBackup) {
        for (int i = 0; i < links.length; i++) {
            for (int j = 0; j < linksBackup.length; j++) {
                if (links[i] == linksBackup[j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
