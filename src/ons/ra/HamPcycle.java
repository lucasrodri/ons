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
import ons.Path;
import ons.util.KSPOffline;
import ons.util.WeightedGraph;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import ons.MultiPath;
import ons.MultiPathProtect;
import ons.util.YenKSP;

/**
 * @author lucas
 */
public class HamPcycle implements RA {

    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;
    private final int ksp = 3;
    private KSPOffline routes;
    private long cont = 0;
    
    //NSFNET
    //int[][] pcycle1 = {{0,1},{1,2},{2,5},{5,9},{9,8},{8,11},{11,13},{13,12},{12,10},{10,3},{3,4},{4,6},{6,7},{7,0}};
    //int[][] pcycle2 = {{0,7},{7,6},{6,4},{4,3},{3,10},{10,12},{12,13},{13,11},{11,8},{8,9},{9,5},{5,2},{2,1},{1,0}};
    
    //USANET
    int[][] pcycle1 = {{0,1},{1,2},{2,3},{3,4},{4,7},{7,9},{9,13},{13,17},{17,23},{23,22},{22,21},{21,20},{20,19},{19,18},{18,10},{10,14},{14,15},{15,16},{16,12},{12,11},{11,8},{8,6},{6,5},{5,0}};
    int[][] pcycle2 = {{0,5},{5,6},{6,8},{8,11},{11,12},{12,16},{16,15},{15,14},{14,10},{10,18},{18,19},{19,20},{20,21},{21,22},{22,23},{23,17},{17,13},{13,9},{9,7},{7,4},{4,3},{3,2},{2,1},{1,0}};
  
    //PANEURO
    //int[][] pcycle1 = {{1,4},{4,6},{6,2},{2,0},{0,5},{5,7},{7,10},{10,11},{11,13},{13,15},{15,14},{14,19},{19,27},{27,26},{26,22},{22,20},{20,21},{21,24},{24,25},{25,23},{23,18},{18,17},{17,16},{16,12},{12,9},{9,3},{3,1}};    
    //int[][] pcycle2 = {{1,3},{3,9},{9,12},{12,16},{16,17},{17,18},{18,23},{23,25},{25,24},{24,21},{21,20},{20,22},{22,26},{26,27},{27,19},{19,14},{14,15},{15,13},{13,11},{11,10},{10,7},{7,5},{5,0},{0,2},{2,6},{6,4},{4,1}};  
    
    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        this.modulation = Modulation._BPSK;
        this.routes = KSPOffline.getKSPOfflineObject(graph, ksp);
        createStaticLightPathsProtectNSFNETCicle1(1987500);
        createStaticLightPathsProtectNSFNETCicle2(1987500);
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
        
        MultiPath[] multiPath;
        //1 singlehop: agregacao
        multiPath = groomingSingleHopSPP(flow.getSource(), flow.getDestination(), flow.getRate(), null);
        if (multiPath != null) {
            int[] bws = {flow.getRate()};
            if (cp.acceptFlow(flow.getID(), multiPath[0].getPaths(), bws, multiPath[1].getPaths(), bws, false, true)) {
                return;
            } else {
                throw (new IllegalArgumentException());
            }
        }

        //2 multihop: agregacao
        multiPath = groomingMultiHopSPP(flow.getSource(), flow.getDestination(), flow.getRate(), ksp, null);
        if (multiPath != null) {
            int[] bws = {flow.getRate()};
            if (cp.acceptFlow(flow.getID(), multiPath[0].getPaths(), bws, multiPath[1].getPaths(), bws, false, true)) {
                return;
            } else {
                throw (new IllegalArgumentException());
            }
        }

        //3 multihop: agregacao + novo lp
        multiPath = groomingMultiHopNewLpSPP(flow.getSource(), flow.getDestination(), flow.getRate(), ksp, null);
        if (multiPath != null) {
            int[] bws = {flow.getRate()};
            if (cp.acceptFlow(flow.getID(), multiPath[0].getPaths(), bws, multiPath[1].getPaths(), bws, false, true)) {
                return;
            } else {
                throw (new IllegalArgumentException());
            }
        }
        
        //4 singlehop: novo lp
        multiPath = newLpSingleHopSPP(flow.getSource(), flow.getDestination(), flow.getRate(), ksp, null);
        if (multiPath != null) {
            int[] bws = {flow.getRate()};
            if (cp.acceptFlow(flow.getID(), multiPath[0].getPaths(), bws, multiPath[1].getPaths(), bws, false, true)) {
                return;
            } else {
                throw (new IllegalArgumentException());
            }
        }

        //5 multihop: novo lp
        multiPath = newLpMultiHopSPP(flow.getSource(), flow.getDestination(), flow.getRate(), ksp, null);
        if (multiPath != null) {
            int[] bws = {flow.getRate()};
            if (cp.acceptFlow(flow.getID(), multiPath[0].getPaths(), bws, multiPath[1].getPaths(), bws, false, true)) {
                return;
            } else {
                throw (new IllegalArgumentException());
            }
        }
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

    private boolean isDisjointed(LightPath[] lps, LightPath[] lpsBackup) {
        if (lps != null && lpsBackup != null) {
            for (int i = 0; i < lpsBackup.length; i++) {
                int[] links = lpsBackup[i].getLinks();
                if(!isDisjointed(lps, links)){
                    return false;
                }
            }
        }
        return true;
    }
    
    private boolean isDisjointed(LightPath[] lps, int[] links) {
        if (lps != null) {
            for (int i = 0; i < links.length; i++) {
                for (int j = 0; j < lps.length; j++) {
                    for (int k = 0; k < lps[j].getLinks().length; k++) {
                        if (links[i] == lps[j].getLinks()[k]) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Verifica se todos os caminhos opticos sao disjuntos
     * @param lps
     * @return 
     */
    private boolean isAllDisjointedPaths(LightPath[] lps) {
        TreeSet<Integer> links = new TreeSet<>();
        for (int i = 0; i < lps.length; i++) {
            for (int l = 0; l < lps[i].getLinks().length; l++) {
                if(!links.contains(lps[i].getLinks()[l])) {
                    links.add(lps[i].getLinks()[l]);
                } else {
                    return false;
                }
            }
            //NAO FAZ SENTIDO ESTE FOR PORQUE E COMPARTILHADO
            /*
            for (Long lpBackup : lps[i].getLpBackup()) {
                LightPath lp = cp.getVT().getLightpath(lpBackup);
                for (int l = 0; l < lp.getLinks().length; l++) {
                    if (!links.contains(lp.getLinks()[l])) {
                        links.add(lp.getLinks()[l]);
                    } else {
                        return false;
                    }
                }
            }
            */
        }
        return true;
    }

    /**
     * Verifica se tem banda disponivel nos caminhos ópticos e em seus backups
     * @param lps
     * @param rate
     * @return 
     */
    private boolean bwAvailable(LightPath[] lps, int rate) {
        for (int i = 0; i < lps.length; i++) {
            for (int l = 0; l < lps[i].getLinks().length; l++) {
                if(((EONLightPath) lps[i]).getBwAvailable() < rate) {
                    return false;
                }
            }
            for (Long lpBackup : lps[i].getLpBackup()) {
                LightPath lp = cp.getVT().getLightpath(lpBackup);
                if(((EONLightPath) lp).getBwAvailable() < rate) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isDisjointed(MultiPath[] multiPath, MultiPath[] pathsDisjointed) {
        if(pathsDisjointed != null) {
            for (MultiPath mp : pathsDisjointed) {
                for (Path path : mp.getPaths()) {
                    for (LightPath lp : path.getLightpaths()) {
                        if(!isDisjointed(multiPath, lp.getLinks())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isDisjointed(MultiPath[] multiPath, int[] links) {
        if(multiPath != null) {
            for (MultiPath mp : multiPath) {
                for (Path path : mp.getPaths()) {
                    for (LightPath lp : path.getLightpaths()) {
                        for (int i = 0; i < lp.getLinks().length; i++) {
                            for (int j = 0; j < links.length; j++) {
                                if(lp.getLinks()[i] == links[j]) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private boolean isDisjointed(MultiPath[] multiPath, LightPath[] lps) {
        if (multiPath != null && lps != null) {
            for (int i = 0; i < lps.length; i++) {
                int[] links = lps[i].getLinks();
                if(!isDisjointed(multiPath, links)){
                    return false;
                }
            }
        }
        return true;
    }
    
    private void createStaticLightPathsProtectNSFNETCicle1(int bw) {
        int tam = pcycle1.length;
        int src, dst;
        for(int i=0;i<tam;i++){
                src=pcycle1[i][0];
                dst=pcycle1[i][1]; 
                createStaticLightPathProtectSPP(src, dst, bw);
        }
    }

    private void createStaticLightPathsProtectNSFNETCicle2(int bw) {
        int tam = pcycle2.length;
        int src, dst;
        for(int i=0;i<tam;i++){
                src=pcycle2[i][0];
                dst=pcycle2[i][1]; 
                createStaticLightPathProtectSPP(src, dst, bw);
        }
    }
    private boolean createStaticLightPathProtectSPP(int src, int dst, int bw) {
        MultiPath[] multiPath = newLpSPPCycle(src, dst, bw, null);
        if (multiPath != null) {
            if (cp.addStaticLightPathProtect(multiPath[0].getPaths(), multiPath[1].getPaths(), true)) {
//                System.out.println("src: "+src+" dst: "+dst);
                return true;
            }
        }
        return false;
    }

    private MultiPath[] newLpSingleHopSPP(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary = new Path[1], backup = new Path[1];
        LightPath[] lps = new LightPath[1];
        LightPath[] lpsBackup = new LightPath[1];
        // k-Shortest Paths routing
        ArrayList<Integer>[] kpaths = routes.getKShortestPaths(src, dst);
        for (int pk = 0; pk < kpaths.length; pk++) {
            lps[0] = createLightPathSPP(src, dst, rate, pk, kpaths, null, pathsDisjointed);
            if(lps[0] != null) {
                for (int bk = 0; bk < kpaths.length; bk++) {
                    lpsBackup = createLPBackupSPP(src, dst, rate, lps, pathsDisjointed);
                    if (lpsBackup != null) {
                        if (lpsBackup[0] != null) {
                            primary[0] = new Path(lps);
                            backup[0] = new Path(lpsBackup);
                            multiPath[0] = new MultiPath(primary);
                            multiPath[1] = new MultiPath(backup);
                            return multiPath;
                        }
                    }
                }
                cp.getVT().deallocatedLightpaths(lps);
            }
        }
        return null;
    } 
    
    private MultiPath[] newLpSingleHopSPP2(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary = new Path[1], backup = new Path[1];
        LightPath[] lps = new LightPath[1];
        LightPath[] lpsBackup;
        int bkpNodes[];
        // k-Shortest Paths routing
        ArrayList<Integer>[] kpathsBackup;
        ArrayList<Integer>[] kpathsPrimary = routes.getKShortestPaths(src, dst);
        for (int pk = 0; pk < kpathsPrimary.length; pk++) {
            lps[0] = createLightPathSPP(src, dst, rate, pk, kpathsPrimary, null, pathsDisjointed);
            if(lps[0] != null) {
                //lpsBackup[0] = createLPBackupDPP(src, dst, rate, bk, kpaths, lps, pathsDisjointed);
                lpsBackup = createBackupSPP(lps, rate, pathsDisjointed);
                if (lpsBackup != null) {
                    primary[0] = new Path(lps);
                    backup[0] = new Path(lpsBackup);
                    multiPath[0] = new MultiPath(primary);
                    multiPath[1] = new MultiPath(backup);
                    return multiPath;
                }
                cp.getVT().deallocatedLightpaths(lps);
            }
        }
        return null;
    }   

    private LightPath createLightPathSPP(int src, int dst, int rate, int k, ArrayList<Integer>[] kpaths, LightPath[] disjointedLP, MultiPath[] pathsDisjointed) {
        LightPath lp = null;
        long id;
        int nodes[] = route(kpaths, k);
        // If no possible path found, block the call
        if (nodes.length == 0 || nodes == null) {
            return null;
        }
        // Create the links vector
        int links[] = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
        }
        //Get the distance the size in KM  link on the route
        double largestLinkKM = 0;
        for (int i = 0; i < links.length; i++) {
            largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(links[i])).getWeight();
        }
        //Adaptative modulation:
        int m = Modulation.getBestModulation(largestLinkKM);
        int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), m);

        int[] firstSlot;
        firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(requiredSlots);
        for (int j = 0; j < firstSlot.length; j++) {
            EONLightPath lpCandidate = cp.createCandidateEONLightPath(src, dst, links,
                    firstSlot[j], (firstSlot[j] + requiredSlots - 1), m, "SPP");
            if (isDisjointed(pathsDisjointed, links) && isDisjointed(disjointedLP, links)) {
                if ((id = cp.getVT().createLightpath(lpCandidate)) >= 0) {
                    lp = cp.getVT().getLightpath(id);
                    break;
                }
            }
        }
        return lp;
    }
    
    private LightPath[] createBackupSPP(LightPath[] lps, int rate, MultiPath[] pathsDisjointed) {
        LightPath lpsBackup[] = new LightPath[lps[0].getLinks().length];
        LightPath lpAux;
        int[] bkpNodes = getRouteNodes(lps[0]);
        for (int i = 0; i < bkpNodes.length - 1; i++) {
            lpAux = null;
            ArrayList<Integer>[] kpathsBackup = routes.getKShortestPaths(bkpNodes[i], bkpNodes[i + 1]);
            for (int bk = 0; bk < kpathsBackup.length; bk++) {
                lpAux = createLPBackupSPP(bkpNodes[i], bkpNodes[i + 1], rate, bk, kpathsBackup, lps, pathsDisjointed);
                if (lpAux != null) {
                    lpsBackup[i] = lpAux;
                    break;
                }
            }
            if (lpAux == null) {
                for (LightPath lpsBack : lpsBackup) {
                    if (lpsBack != null) {
                        cp.getVT().deallocatedLightpath(lpsBack.getID());
                    }
                }
                return null;
            }
        }
        return lpsBackup;
    }

    private MultiPath[] newLpMultiHopSPP(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary = new Path[1], backup = new Path[1];
        ArrayList<Integer>[] kpaths = routes.getKShortestPaths(src, dst);
        int k, nodes[] = null;
        for (k = 0; k < kpaths.length; k++) {
            nodes = route(kpaths, k);
            // If no possible path found, block the call
            if (nodes.length == 0 || nodes == null) {
                return null;
            }
            if (nodes.length >= 3) {
                for (int n = nodes.length - 2; n > 0; n--) {
                    multiPath = newLpSingleHopSPP2(src, nodes[n], rate, ksp, pathsDisjointed);
                    if (multiPath != null) {
                        primary = multiPath[0].getPaths();
                        backup = multiPath[1].getPaths();
                        multiPath = newLpSingleHopSPP(nodes[n], dst, rate, ksp, multiPath);
                        if (multiPath != null) {
                            primary[0].addLightpaths(multiPath[0].getPaths()[0].getLightpaths());
                            backup[0].addLightpaths(multiPath[1].getPaths()[0].getLightpaths());
                            multiPath[0] = new MultiPath(primary);
                            multiPath[1] = new MultiPath(backup);
                            if (isDisjointed(multiPath, pathsDisjointed)) {
                                return multiPath;
                            } else {
                                throw (new IllegalArgumentException());
                            }
                        } else {
                            cp.getVT().deallocatedLightpaths(primary[0].getLightpaths());
                            cp.getVT().deallocatedLightpaths(backup[0].getLightpaths());
                        }
                    }
                }
            }
            
        }
        return null;
    }
 
    private int[] getRouteNodes(LightPath lp) {
        int routeNodes[] = new int[lp.getLinks().length + 1];
        routeNodes[0] = cp.getPT().getLink(lp.getLinks()[0]).getSource();
        for (int i = 0; i < lp.getLinks().length; i++) {
            routeNodes[i+1] = cp.getPT().getLink(lp.getLinks()[i]).getDestination();
        }
        return routeNodes;
    }

    private LightPath createLPBackupSPP(int src, int dst, int rate, int k, ArrayList<Integer>[] kpaths, LightPath[] disjointedLP, MultiPath[] pathsDisjointed) {
        LightPath lp = null;
        long id;
        int nodes[] = route(kpaths, k);
        // If no possible path found, block the call
        if (nodes.length == 0 || nodes == null) {
            return null;
        }
        // Create the links vector
        int links[] = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
        }
        //Get the distance the size in KM  link on the route
        double largestLinkKM = 0;
        for (int i = 0; i < links.length; i++) {
            largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(links[i])).getWeight();
        }
        //Adaptative modulation:
        int m = Modulation.getBestModulation(largestLinkKM);
        int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), m);

        int[] firstSlot;
        firstSlot = ((EONLink) cp.getPT().getLink(links[0])).lastFit(requiredSlots);
        for (int j = 0; j < firstSlot.length; j++) {
            EONLightPath lpCandidate = cp.createCandidateEONLightPath(src, dst, links,
                    firstSlot[j], (firstSlot[j] + requiredSlots - 1), m, "SPP");
            if (isDisjointed(pathsDisjointed, links) && isDisjointed(disjointedLP, links)) {
                if ((id = cp.getVT().createLightpath(lpCandidate)) >= 0) {
                    lp = cp.getVT().getLightpath(id);
                    break;
                }
            }
        }
        return lp;
    }    
 
    private MultiPath[] groomingSingleHopSPP(int src, int dst, int rate, MultiPath[] pathsDisjointed) {
        // Try existent lightpaths first (Electral Grooming)
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary, backup;
        MultiPathProtect groomingMultiPathProtect = getLeastLoadedPrimaryMultiPathProtectSPP(src, dst, rate);
        if (groomingMultiPathProtect instanceof MultiPathProtect) {
            primary = groomingMultiPathProtect.getPrimary().getPaths();
            backup = groomingMultiPathProtect.getBackup().getPaths();
            multiPath[0] = new MultiPath(primary);
            multiPath[1] = new MultiPath(backup);
            if(isDisjointed(multiPath, pathsDisjointed)){
                return multiPath;
            }
        }
        return null;
    }

        private MultiPathProtect getLeastLoadedPrimaryMultiPathProtectSPP(int src, int dst, int bw) {
        int min = Integer.MAX_VALUE;
        MultiPathProtect mpp = null;
        Iterator<Map.Entry<Flow, MultiPathProtect>> itr = cp.getMappedFlowsMultiPathProtect().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Flow, MultiPathProtect> entry = itr.next();
            if (entry.getKey().getSource() == src
                    && entry.getKey().getDestination() == dst) {
//                if(bw<=40000&&entry.getValue().getTotalBWPrimaryAvailable()<400000){
                    if (entry.getValue().getTotalBWPrimaryAvailable() >= bw
                            && entry.getValue().getTotalBWBackupAvailable() >= bw
                            && entry.getValue().getPrimary().getPaths()[0].getLightpaths()[0].getTypeProtection().equals("SPP")) {
                        if (min > entry.getValue().getTotalBWPrimaryAvailable()) {
                            min = entry.getValue().getTotalBWPrimaryAvailable();
                            mpp = entry.getValue();
                        }
//                    }
                }
            }
        }
        return mpp;
    }

     private MultiPath[] groomingMultiHopSPP(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary = new Path[1], backup = new Path[1];
        WeightedGraph wg = cp.getVT().getWeightedPrimaryLightpathsGraphProtection(rate, 1, "SPP");
        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(wg, src, dst, ksp);
        boolean flag = false;
        int k, nodes[] = null;
        LightPath[] lps = new LightPath[1], lpsBackup;
        for (k = 0; k < kpaths.length; k++) {
            nodes = route(kpaths, k);
            // If no possible path found, block the call
            if (nodes.length == 0 || nodes == null) {
                return null;
            }
            // Create the lightpaths route
            lps = new LightPath[nodes.length - 1];
            for (int j = 0; j < nodes.length - 1; j++) {
                lps[j] = cp.getVT().getMinHops(cp.getVT().getAvailablePrimaryLightpathsProtection(nodes[j], nodes[j + 1], rate, "SPP"));
            }
            if (isAllDisjointedPaths(lps) && bwAvailable(lps, rate)) {
                primary[0] = new Path(lps);
                flag = true;
                break;
            }
        }
        //Backup
        if (flag) {
            //No caso do SPP
            lpsBackup = createLPBackupSPP(src, dst, rate, lps, pathsDisjointed);
            if (lpsBackup != null) {
                backup[0] = new Path(lpsBackup);
                multiPath[0] = new MultiPath(primary);
                multiPath[1] = new MultiPath(backup);
                if (isDisjointed(multiPath, pathsDisjointed)) {
                    return multiPath;
                }
            }
        }
        return null;
    }

    private MultiPath[] groomingMultiHopNewLpSPP(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary = new Path[1], backup = new Path[1];
        ArrayList<Integer>[] kpaths = routes.getKShortestPaths(src, dst);
        int k, nodes[] = null;
        for (k = 0; k < kpaths.length; k++) {
            nodes = route(kpaths, k);
            // If no possible path found, block the call
            if (nodes.length == 0 || nodes == null) {
                return null;
            }
            if (nodes.length >= 3) {
                //TODO Talvez isso deixe o algoritmo muito guloso e isso seja ruim.
                //Fiz o teste somente com o ultimo e ficou 1% melhor que com todos
                //Mas eu to deixando assim por questão de completude
                for (int n = nodes.length - 2; n > 0; n--) {
                    multiPath = groomingMultiHopSPP(src, nodes[n], rate, ksp, null);
                    if (multiPath != null) {
                        primary = multiPath[0].getPaths();
                        backup = multiPath[1].getPaths();
                        multiPath = newLpSingleHopSPP(nodes[n], dst, rate, ksp, multiPath);
                        if (multiPath != null) {
                            primary[0].addLightpaths(multiPath[0].getPaths()[0].getLightpaths());
                            backup[0].addLightpaths(multiPath[1].getPaths()[0].getLightpaths());
                            multiPath[0] = new MultiPath(primary);
                            multiPath[1] = new MultiPath(backup);
                            if (isDisjointed(multiPath, pathsDisjointed)) {
                                return multiPath;
                            } else {
                                throw (new IllegalArgumentException());
                            }
                        }
                    }
                }
            }
            
        }
        return null;
    }

    private MultiPath[] newLpSPPCycle(int src, int dst, int rate, Object object) {
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary = new Path[1], backup = new Path[1];
        LightPath[] lps = new LightPath[1];
        LightPath[] lpsBackup = new LightPath[1];
        lps[0] = createLPPrimarySPPCycle(src, dst, rate);
        if (lps[0] != null) {
            lpsBackup = createLPBackupSPPCycle(src, dst, rate, lps);
            if (lpsBackup != null) {
                primary[0] = new Path(lps);
                backup[0] = new Path(lpsBackup);
                multiPath[0] = new MultiPath(primary);
                multiPath[1] = new MultiPath(backup);
                return multiPath;
            }
            cp.getVT().deallocatedLightpaths(lps);
        }
        return null;
    }

    private LightPath createLPPrimarySPPCycle(int src, int dst, int rate) {
        LightPath lp = null;
        long id;
        int nodes[] = {src, dst};
        // If no possible path found, block the call
        if (nodes.length == 0 || nodes == null) {
            return null;
        }
        // Create the links vector
        int links[] = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
        }
        //Get the distance the size in KM  link on the route
        double largestLinkKM = 0;
        for (int i = 0; i < links.length; i++) {
            largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(links[i])).getWeight();
        }
        //Adaptative modulation:
        int m = Modulation.getBestModulation(largestLinkKM);
        int requiredSlots = (int) (((EONLink) cp.getPT().getLink(0)).getNumSlots() / 2) - 1;
        int[] firstSlot;
        firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(requiredSlots);
        for (int j = 0; j < firstSlot.length; j++) {
            EONLightPath lpCandidate = cp.createCandidateEONLightPath(src, dst, links,
                    firstSlot[j], (firstSlot[j] + requiredSlots - 1), m, "SPP");
            if ((id = cp.getVT().createLightpath(lpCandidate)) >= 0) {
                lp = cp.getVT().getLightpath(id);
                break;
            }
        }
        return lp;
    }

    private LightPath[] createLPBackupSPPCycle(int src, int dst, int rate, LightPath[] lps) {
        LightPath lp[] = null;
        long id;
        int nodes[] = getCycleRouteBackup(src, dst);
        lp = new LightPath[nodes.length - 1];
        // If no possible path found, block the call
        if (nodes.length == 0 || nodes == null) {
            return null;
        }
        for (int i = 0; i < nodes.length - 1; i++) {
            int links[] = new int[1];
            links[0] = cp.getPT().getLink(nodes[i], nodes[i + 1]).getID();
            int m = Modulation.getBestModulation(((EONLink) cp.getPT().getLink(links[0])).getWeight());
            int requiredSlots = (int) (((EONLink) cp.getPT().getLink(0)).getNumSlots() / 2) - 1;
            int[] firstSlot;
            firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(requiredSlots);
            if(firstSlot.length != 0) {
                for (int j = 0; j < firstSlot.length; j++) {
                    EONLightPath lpCandidate = cp.createCandidateEONLightPath(nodes[i], nodes[i + 1], links,
                            firstSlot[j], (firstSlot[j] + requiredSlots - 1), m, "SPP");
                    if (isDisjointed(lps, links)) {
                        if (!hasLightpathBackup(nodes[i], nodes[i + 1])) {
                            if ((id = cp.getVT().createLightpath(lpCandidate)) >= 0) {
                                lp[i] = cp.getVT().getLightpath(id);
                                break;
                            }
                        } else {
                            for (LightPath candidate : cp.getVT().getLightpaths(nodes[i], nodes[i + 1])) {
                                if (candidate.isBackup()) {
                                    lp[i] = candidate;
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }    
            } else {
                if (hasLightpathBackup(nodes[i], nodes[i + 1])) {
                    for (LightPath candidate : cp.getVT().getLightpaths(nodes[i], nodes[i + 1])) {
                        if (candidate.isBackup()) {
                            lp[i] = candidate;
                            break;
                        }
                    }
                }
            }
            
        }
        return lp;
    }

    private int[] getCycleRouteBackup(int src, int dst) {
        int route[] = null;
        //int[][] pcycle1 = {{0,1},{1,2},{2,5},{5,9},{9,8},{8,11},{11,13},{13,12},{12,10},{10,3},{3,4},{4,6},{6,7},{7,0}};
        //int[][] pcycle2 = {{0,7},{7,6},{6,4},{4,3},{3,10},{10,12},{12,13},{13,11},{11,8},{8,9},{9,5},{5,2},{2,1},{1,0}};
        ArrayList<Integer> pathNodes = new ArrayList<>();
        pathNodes.add(src);
        if(cycle(pcycle1, src, dst)) {
            int i = nextIndexCicle(pcycle2, src, dst);
            while (pcycle2[i][1] != dst) {
                if(pcycle2[i][0] == src) {
                    src = pcycle2[i][1];
                    pathNodes.add(src);
                }
                i++;
                if(i >= pcycle2.length) {
                    i = 0;
                }
            }
        } else {
            int i = nextIndexCicle(pcycle1, src, dst);
            while (pcycle1[i][1] != dst) {
                if(pcycle1[i][0] == src) {
                    src = pcycle1[i][1];
                    pathNodes.add(src);
                }
                i++;
                if(i >= pcycle1.length) {
                    i = 0;
                }
            }
        }
        pathNodes.add(dst);
        route = new int[pathNodes.size()];
        for (int i = 0; i < pathNodes.size(); i++) {
            route[i] = pathNodes.get(i);
        }
        return route;
    }

    private boolean cycle(int[][] pcycle, int src, int dst) {
        for (int i = 0; i < pcycle.length; i++) {
            if (pcycle[i][0] == src && pcycle[i][1] == dst) {
                return true;
            }
        }
        return false;
    }

    private int nextIndexCicle(int[][] pcycle, int src, int dst) {
        //int[][] pcycle1 = {{0,1},{1,2},{2,5},{5,9},{9,8},{8,11},{11,13},{13,12},{12,10},{10,3},{3,4},{4,6},{6,7},{7,0}};
        //int[][] pcycle2 = {{0,7},{7,6},{6,4},{4,3},{3,10},{10,12},{12,13},{13,11},{11,8},{8,9},{9,5},{5,2},{2,1},{1,0}};
        for (int i = 0; i < pcycle.length; i++) {
            if (pcycle[i][0] == dst && pcycle[i][1] == src) {
                if(i + 1 < pcycle.length) {
                    return i + 1;
                } else {
                    return 0;
                }
            }
        }
        return -1;
    }

    private boolean hasLightpathBackup(int src, int dst) {
        if (!cp.getVT().hasLightpath(src, dst)) {
            return false;
        } else {
            for (LightPath lp : cp.getVT().getLightpaths(src, dst)) {
                if(lp.isBackup()) {
                    return true;
                }
            }
        }
        return false;
    }

    private LightPath[] createLPBackupSPP(int src, int dst, int rate, LightPath[] lpsPrimary, MultiPath[] pathsDisjointed) {
        WeightedGraph wg = cp.getVT().getWeightedBackupLightpathsGraphProtection(rate, 1, "SPP");
        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(wg, src, dst, ksp);
        boolean flag = false;
        int k, nodes[] = null;
        LightPath[] lps = new LightPath[1];
        for (k = 0; k < kpaths.length; k++) {
            nodes = route(kpaths, k);
            // If no possible path found, block the call
            if (nodes.length == 0 || nodes == null) {
                return null;
            }
            // Create the lightpaths route
            lps = new LightPath[nodes.length - 1];
            for (int j = 0; j < nodes.length - 1; j++) {
                lps[j] = cp.getVT().getMinHops(cp.getVT().getAvailableBackupLightpathsProtection(nodes[j], nodes[j + 1], rate, "SPP"));
            }
            if(isDisjointed(lpsPrimary, lps) && isDisjointed(pathsDisjointed, lps)) {
                return lps;
            }
        }
        return null;
    }
     
        
}