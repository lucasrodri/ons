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
import ons.MyStatistics;
import ons.util.YenKSP;

/**
 * @author lucas
 */
public class PCycle implements RA {
    
    private MyStatistics st = MyStatistics.getMyStatisticsObject();    

    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;
    private final int ksp = 8;
    private KSPOffline routes;
    private long cont = 0;
    /*int[][] vetTraffic = {{10, 14}, {14, 19}, {19, 14}, {14, 10}, {19, 20}, {20, 19}, 
        {14, 15}, {15, 14}, {10, 11}, {11, 10}, {19, 22}, {22, 19}, {18, 19}, {19, 18}, 
        {10, 18}, {18, 10}, {15, 20}, {20, 15}, {8, 10}, {10, 8}, {20, 21}, {21, 20}, 
        {3, 4}, {4, 3}, {12, 13}, {13, 12}, {15, 21}, {21, 15}, {2, 4}, {4, 2}, {13, 17}, 
        {17, 13}, {11, 15}, {15, 11}, {17, 23}, {23, 17}, {11, 12}, {12, 11}, {22, 23}, 
        {23, 22}, {4, 7}, {7, 4}, {21, 22}, {22, 21}, {2, 3}, {3, 2}, {5, 10}, {10, 5}, 
        {8, 11}, {11, 8}, {6, 12}, {12, 6}, {16, 20}, {20, 16}, {3, 6}, {6, 3}, {15, 16}, 
        {16, 15}, {2, 6}, {6, 2}, {1, 2}, {2, 1}, {16, 21}, {21, 16}, {12, 16}, {16, 12},
        {23, 5}, {9, 13}, {13, 9}, {12, 5}, {16, 17}, {17, 16}, {0, 1}, {1, 0}, {6, 7},
        {6, 8}, {7, 6}, {8, 6}, {16, 22}, {22, 16}, {9, 12}, {12, 9}, {0, 5}, {5, 0}, 
        {5, 8}, {8, 5}, {7, 9}, {8, 9}, {9, 7}, {9, 8}, {1, 5}, {5, 1}, {5, 6}, {6, 5}};
     */
    int[][] vetTraffic = {{10, 14}, {14, 19}, {19, 14}, {14, 10}, {19, 20}, {20, 19}, {14, 15}, {15, 14}, {10, 11},
    {11, 10}, {19, 22}, {22, 19}, {18, 19}, {19, 18}, {10, 18}, {18, 10}, {15, 20}, {20, 15}, {8, 10}, {10, 8}, {20, 21},
    {21, 20}, {3, 4}, {4, 3}, {12, 13}, {13, 12}, {15, 21}, {21, 15}, {2, 4}, {4, 2}, {13, 17}, {17, 13}, {11, 15}, {15, 11},
    {17, 23}, {23, 17}, {11, 12}, {12, 11}, {22, 23}, {23, 22}, {4, 7}, {7, 4}, {21, 22}, {22, 21}, {2, 3}, {3, 2}, {5, 10},
    {10, 5}, {8, 11}, {11, 8}, {6, 12}, {12, 6}, {16, 20}, {20, 16}, {3, 6}, {6, 3}, {15, 16}, {16, 15}, {2, 6}, {6, 2}, {1, 2},
    {2, 1}, {16, 21}, {21, 16}, {12, 16}, {16, 12}, {23, 5}, {9, 13}, {13, 9}, {12, 5}, {16, 17}, {17, 16}, {0, 1}, {1, 0},
    {6, 7}, {6, 8}, {7, 6}, {8, 6}, {16, 22}, {22, 16}, {9, 12}, {12, 9}, {0, 5}, {5, 0}, {5, 8}, {8, 5}, {7, 9}, {8, 9}, {9, 7},
    {9, 8}, {1, 5}, {5, 1}, {5, 6}, {6, 5}};
    
    int cycle1[][] = {{0,1},{1,2},{2,3},{3,4},{4,7},{7,9},{9,13},{13,17},{17,23},{23,22},
        {22,21},{21,20},{20,19},{19,18},{18,10},{10,14},{14,15},{15,16},{16,12},{12,11},
        {11,8},{8,6},{6,5},{5,0}};
    
    int[][] pcycle1 = {{0,1},{1,2},{2,5},{5,9},{9,8},{8,11},{11,13},{13,12},{12,10},{10,3},
        {3,4},{4,6},{6,7},{7,0}};
    
    int[][] pcycle2 = {{0,7},{7,6},{6,4},{4,3},{3,10},{10,12},{12,13},{13,11},{11,8},{8,9},
        {9,5},{5,2},{2,1},{1,0}};    

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        this.modulation = Modulation._BPSK;
        this.routes = KSPOffline.getKSPOfflineObject(graph, ksp);
//        createHamiltonCycle(2000000);
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
//        if(flow.getSource()==1&&flow.getDestination()==2)
        routesCycle(flow.getSource(), flow.getDestination(), true);
        
                
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];
        
        int ksp = 8;

        // Try existent lightpaths first (Grooming)
        lps[0] = getLeastLoadedPrimaryLP(flow.getSource(), flow.getDestination(), flow.getRate());
        if (lps[0] instanceof LightPath) {
            if (cp.acceptFlow(flow.getID(), lps)) {
                if(onCycle(flow.getSource(), flow.getDestination())){
//                            System.out.println("\n======src: "+flow.getSource()+" dst: "+flow.getDestination());
//                    st.acceptFlowCycle(flow, routeBackupCycle(flow.getSource(), flow.getDestination(), true), true);                
                }else{
//                    st.acceptFlowCycle(flow, routeBackupCycle(flow.getSource(), flow.getDestination(), true), false);                
                }
                return;
            }
        }
        
        // k-Shortest Paths routing
        
        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);
        
        for (int k = 0; k < kpaths.length; k++) {

            nodes = route(kpaths,k);
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
            //for (int i = 0; i < links.length; i++) {
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
                        if(cp.acceptFlow(flow.getID(), lps)){
                            if(onCycle(flow.getSource(), flow.getDestination())){
//                            System.out.println("\n======src: "+flow.getSource()+" dst: "+flow.getDestination());
//                                st.acceptFlowCycle(flow, routeBackupCycle(flow.getSource(), flow.getDestination(), true), true);                
                            }else{
//                                st.acceptFlowCycle(flow, routeBackupCycle(flow.getSource(), flow.getDestination(), true), false);                
                            }
                            return;
                        } else {
                            cp.getVT().deallocatedLightpaths(lps);
                        }
                    }
                }
            //}       

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

    private MultiPathProtect getLeastLoadedPrimaryMultiPathProtect(int src, int dst, int bw) {
        int min = Integer.MAX_VALUE;
        MultiPathProtect mpp = null;
        Iterator<Map.Entry<Flow, MultiPathProtect>> itr = cp.getMappedFlowsMultiPathProtect().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Flow, MultiPathProtect> entry = itr.next();
            if (entry.getKey().getSource() == src
                    && entry.getKey().getDestination() == dst) {
                if (entry.getValue().getTotalBWPrimaryAvailable() >= bw
                        && entry.getValue().getTotalBWBackupAvailable() >= bw) {
                    if (min > entry.getValue().getTotalBWPrimaryAvailable()) {
                        min = entry.getValue().getTotalBWPrimaryAvailable();
                        mpp = entry.getValue();
                    }
                }
            }
        }
        return mpp;
    }

    private boolean isDisjointed(LightPath[] lps, int[] links) {
        for (int i = 0; i < links.length; i++) {
            for (int j = 0; j < lps.length; j++) {
                for (int k = 0; k < lps[j].getLinks().length; k++) {
                    if (links[i] == lps[j].getLinks()[k]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Primeira Estrategia Singlehop agregacao
     *
     * @param flow
     * @param primary
     * @param backup
     * @return
     */
    private MultiPath[] groomingSingleHop(int src, int dst, int rate, MultiPath[] pathsDisjointed) {
        // Try existent lightpaths first (Electral Grooming)
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary, backup;
        MultiPathProtect groomingMultiPathProtect = getLeastLoadedPrimaryMultiPathProtect(src, dst, rate);
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

    /**
     * Segunda estrategia Multihop Agregacao
     *
     * @param flow
     * @param primary
     * @param backup
     * @param ksp determina a quantidade de caminhos opticos que voce deseja na
     * rota
     * @return
     */
    private MultiPath[] groomingMultiHop(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary = new Path[1], backup = new Path[1];
        WeightedGraph wg = cp.getVT().getWeightedPrimaryLightpathsGraph(rate, 1);
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
                lps[j] = cp.getVT().getMinHops(cp.getVT().getAvailablePrimaryLightpaths(nodes[j], nodes[j + 1], rate));
            }
            if (isAllDisjointedPaths(lps) && bwAvailable(lps, rate)) {
                primary[0] = new Path(lps);
                flag = true;
                break;
            }
        }
        //Backup
        if (flag) {
            //No caso do DLP
            int contBkp = 0;
            for (LightPath lp : lps) {
                contBkp += lp.getHops();
            }
            lpsBackup = new LightPath[contBkp];
            int l = 0;
            for (int i = 0; i < lps.length; i++) {
                for (Long lpBackup : lps[i].getLpBackup()) {
                    lpsBackup[l++] = cp.getVT().getLightpath(lpBackup);
                }
            }
            backup[0] = new Path(lpsBackup);
            multiPath[0] = new MultiPath(primary);
            multiPath[1] = new MultiPath(backup);
            if(isDisjointed(multiPath, pathsDisjointed)){
                return multiPath;
            } 
        }
        return null;
    }

    /**
     * Terceira estrategia Multihop agregado com novo LP
     *
     * @param flow
     * @param primary
     * @param backup
     * @param i
     * @return
     */
    private MultiPath[] groomingMultiHopNewLp(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
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
                    multiPath = groomingMultiHop(src, nodes[n], rate, ksp, null);
                    if (multiPath != null) {
                        primary = multiPath[0].getPaths();
                        backup = multiPath[1].getPaths();
                        multiPath = newLpSingleHop(nodes[n], dst, rate, ksp, multiPath);
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

    /**
     *
     * @param flow
     * @param primary
     * @param backup
     * @param ksp
     * @return
     */
    private MultiPath[] newLpSingleHop(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
        MultiPath[] multiPath = new MultiPath[2];
        Path[] primary = new Path[1], backup = new Path[1];
        int[] nodes = null;
        int[] links = null;
        long id;
        int modulationP = this.modulation;
        int modulationB = this.modulation;
        LightPath[] lps = new LightPath[1];
        LightPath[] lpsBackup;
        // k-Shortest Paths routing
        ArrayList<Integer>[] kpaths = routes.getKShortestPaths(src, dst);
        int k;
        boolean flag = false;
        for (k = 0; k < kpaths.length; k++) {
            nodes = route(kpaths, k);
            // If no possible path found, block the call
            if (nodes.length == 0 || nodes == null) {
                return null;
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
            modulationP = Modulation.getBestModulation(largestLinkKM);
            // First-Fit spectrum assignment in BPSK Modulation
            int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), modulationP);

            int[] firstSlot;
            // Try the slots available in each link
            firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(requiredSlots);
            for (int j = 0; j < firstSlot.length; j++) {
                // Now you create the lightpath to use the createLightpath VT
                //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
                EONLightPath lp = cp.createCandidateEONLightPath(src, dst, links,
                        firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulationP);
                // Now you try to establish the new lightpath, accept the call
                if (isDisjointed(pathsDisjointed, links)) {
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        // Single-hop routing (end-to-end lightpath)
                        lps[0] = cp.getVT().getLightpath(id);
                        primary[0] = new Path(lps);
                        flag = true;
                        break;
                    }
                }
            }
            if (flag) {
                break;
            }
        }
        if (flag) {
            //Backup
            boolean flag2;
            if (nodes != null) {
                lpsBackup = new LightPath[nodes.length - 1];
                for (int i = 0; i < nodes.length - 1; i++) {
                    flag2 = false;
                    kpaths = routes.getKShortestPaths(nodes[i], nodes[i + 1]);
                    for (k = 0; k < kpaths.length; k++) {
                        int[] nodesBackup = route(kpaths, k);
                        // If no possible path found, block the call
                        if (nodesBackup.length == 0 || nodesBackup == null) {
                            if (flag) {
                                cp.getVT().deallocatedLightpaths(lps);
                            }
                            return null;
                        }
                        // Create the links vector
                        int[] linksBackup = new int[nodesBackup.length - 1];
                        for (int j = 0; j < nodesBackup.length - 1; j++) {
                            linksBackup[j] = cp.getPT().getLink(nodesBackup[j], nodesBackup[j + 1]).getID();
                        }
                        if (isDisjointed(lps, linksBackup) && isDisjointed(pathsDisjointed, linksBackup)) {
                            double largestLinkKM = 0;
                            for (int j = 0; j < linksBackup.length; j++) {
                                largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(linksBackup[j])).getWeight();
                            }
                            modulationB = Modulation.getBestModulation(largestLinkKM);
                            //if (modulationP == modulationB) {
                                int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), modulationB);
                                int[] firstSlot;
                                firstSlot = ((EONLink) cp.getPT().getLink(linksBackup[0])).firstFit(requiredSlots);
                                for (int j = 0; j < firstSlot.length; j++) {
                                    EONLightPath lp = cp.createCandidateEONLightPath(nodes[i], nodes[i + 1], linksBackup,
                                            firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulationB);
                                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                                        lpsBackup[i] = cp.getVT().getLightpath(id);
                                        flag2 = true;
                                        break;
                                    }
                                }
                            //}
                        }
                        if (flag2) {
                            break;
                        }
                    }
                    if (!flag2) {
                        if (flag) {
                            cp.getVT().deallocatedLightpaths(lps);
                        }
                        // Block the call
                        for (LightPath lpsBack : lpsBackup) {
                            if (lpsBack != null) {
                                cp.getVT().deallocatedLightpath(lpsBack.getID());
                            }
                        }
                        return null;
                    }
                }
                backup[0] = new Path(lpsBackup);
                multiPath[0] = new MultiPath(primary);
                multiPath[1] = new MultiPath(backup);
                return multiPath;
            }
        }
        return null;
    }

    private MultiPath[] newLpMultiHop(int src, int dst, int rate, int ksp, MultiPath[] pathsDisjointed) {
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
                    multiPath = newLpSingleHop(src, nodes[n], rate, ksp, pathsDisjointed);
                    if (multiPath != null) {
                        primary = multiPath[0].getPaths();
                        backup = multiPath[1].getPaths();
                        multiPath = newLpSingleHop(nodes[n], dst, rate, ksp, multiPath);
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

    private void createStaticLightPathsProtect(int bw) {
        int tam = vetTraffic.length;
        int src, dst;
        for(int i=0;i<tam;i++){
                src=vetTraffic[i][0];
                dst=vetTraffic[i][1]; 
                createStaticLightPathProtectDLP(src, dst, bw);
        }
    }

    private boolean createStaticLightPathProtectDLP(int src, int dst, int bw) {
        MultiPath[] multiPath = newLpSingleHop(src, dst, bw, ksp, null);
        if (multiPath != null) {
            if (cp.addStaticLightPathProtect(multiPath[0].getPaths(), multiPath[1].getPaths(), true)) {
                return true;
            }
        }
        return false;
    }
    
    private void createHamiltonCycle(int bw) {
        int src, dst;
        
        for (int i = 0; i < cycle1.length; i++) {
            src = cycle1[i][0];
            dst = cycle1[i][1];
            createStaticLightPathProtectCycle(src, dst, bw, true, 0, 159);
//            createStaticLightPathProtectCycle(src, dst, bw, false, 160, 319);
//            createStaticLightPathProtectCycle(dst, src, bw, false, 0, 159);
            createStaticLightPathProtectCycle(dst, src, bw, true, 160, 319);
        }       
        
    }   

    private void createStaticLightPathProtectCycle(int src, int dst, int bw, boolean backup, int firstSlot, int lastSlot) {
        int[] nodes = new int[2];
        nodes[0]=src;
        nodes[1]=dst;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];        

        // Create the links vector
        links = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
        }

        EONLightPath lp = cp.createCandidateEONLightPath(src, dst, links,
                firstSlot, lastSlot, modulation);

        if ((id = cp.getVT().createLightpath(lp)) >= 0) {
            lps[0] = cp.getVT().getLightpath(id);
            if (!cp.addStaticLightPathProtect(lps, backup, true)) {
                throw (new IllegalArgumentException());                
            }
        }        
    }

//    
    private LightPath groomable(int src, int dst, int bw){
            LightPath lp;
            lp = getLeastLoadedPrimaryLP(src,dst,bw);
            if (lp instanceof LightPath) {
                return lp;
            }
            return null;                
    }

    private LightPath getLeastLoadedPrimaryLP(int source, int destination, int bw) {
        long abw_aux, abw = 0;
        LightPath lp_aux, lp = null;
        // Get the available lightpaths
        TreeSet<LightPath> lps = cp.getVT().getAvailablePrimaryLightpaths(source,destination, bw);
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

    private void printNodesRoute(int[] nodes){
        System.out.print("\n{");
        for(int i=0;i<nodes.length;i++){
            System.out.print(nodes[i]+",");
        }
       System.out.println("}");        
    }
    
    private void createLightPath(int src, int dst, int bw) {
       
    }

    private LightPath[] provisionLR2(int[] nodes, Flow flow) {
        LightPath[] lps = null;
        LightPath lp;
        ArrayList<LightPath> alps = new ArrayList<>();
        int src = nodes[0], dst, i;
        for (i = 0; i < nodes.length - 1; i++) {
            while (i < nodes.length - 1 && onCycle(nodes[i], nodes[i+1])) {
                dst = nodes[i];
                if(src == dst) {
                    dst = nodes[i+1];
                    lp = groomable(src, dst, flow.getRate());                    
                    if(lp != null) {
//                        System.out.println("src: "+src+" dst: "+dst);
                        alps.add(lp);
                    } else {
                        dealocated(alps);
                        return null;
                    }
                    i++;
                } else {
                    lp = newLightpath(src, dst, flow.getRate(), nodes);
                    if(lp != null) {
                        alps.add(lp);
                    } else {
                        dealocated(alps);
                        return null;
                    }
                }
                src = dst;
            }
        }
        if (i < nodes.length) {
            dst = nodes[i];
            if (src != dst) {
                lp = newLightpath(src, dst, flow.getRate(), nodes);
                if (lp != null) {
                    alps.add(lp);
                } else {
                    dealocated(alps);
                    return null;
                }
            }
        }
        lps = covertArrayToVectorLP(alps);
//        System.out.println("src: "+src+" dst: "+dst);
        return lps;
    }

    private boolean onCycle(int src, int dst) {
        for (int i = 0; i < cycle1.length; i++) {
            if( (cycle1[i][0] == src) && (cycle1[i][1] == dst)) {
                return true;
            }
            if( (cycle1[i][0] == dst) && (cycle1[i][1] == src)) {
                return true;
            }
        }
        return false;
    }

    private LightPath newLightpath(int src, int dst, int rate, int[] route) {
        LightPath lp = null;
        LightPath lpCandidate = null;
        long id;
        ArrayList<Integer> av = new ArrayList<>();
        int[] nodes, links;
        for (int i = 0; i < route.length; i++) {
            if(route[i] == src) {
                while (route[i] != dst) {                    
                    av.add(route[i]);
                    i++;
                }
                av.add(route[i]);
                break;
            }
        }
        nodes = covertArrayToVectorInt(av);
        links = new int[nodes.length - 1];
        for (int j = 0; j < nodes.length - 1; j++) {
            links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
        }
        int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), modulation);
        int[] firstSlot;
        firstSlot = ((EONLink) cp.getPT().getLink(nodes[0])).firstFit(requiredSlots);
        for (int j = 0; j < firstSlot.length; j++) {
            lpCandidate = cp.createCandidateEONLightPath(src, dst, links, firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
            if ((id = cp.getVT().createLightpath(lpCandidate)) >= 0) {
                lp = cp.getVT().getLightpath(id);
                return lp;
            }
        }
        return lp;
    }

    private void dealocated(ArrayList<LightPath> alps) {
        LightPath[] lps = covertArrayToVectorLP(alps);
//        cp.getVT().deallocatedLightpathsNotReserved(lps);
    }

    private LightPath[] covertArrayToVectorLP(ArrayList<LightPath> alps) {
        LightPath[] lps = new LightPath[alps.size()];
        for (int i = 0; i < lps.length; i++) {
            lps[i] = alps.get(i);
        }
        return lps;
    }
    
    private int[] covertArrayToVectorInt(ArrayList<Integer> av) {
        int[] v = new int[av.size()];
        for (int i = 0; i < v.length; i++) {
            v[i] = av.get(i);
        }
        return v;
    }
    
    private int[] routesCycle(int src, int dst, boolean straddling){
        
        int cycle1[][] = {{0,1},{1,2},{2,3},{3,4},{4,7},{7,9},{9,13},{13,17},{17,23},{23,22},
        {22,21},{21,20},{20,19},{19,18},{18,10},{10,14},{14,15},{15,16},{16,12},{12,11},
        {11,8},{8,6},{6,5},{5,0}};
        
//        int cycle2[][] = {{0,5},{5,6},{6,8},{8,11},{11,12},{12,16},{16,15},{15,14},{14,10},{10,18},
//        {18,19},{19,20},{20,21},{21,22},{22,23},{23,17},{17,13},{13,9},{9,7},{7,4},
//        {4,3},{3,2},{2,1},{1,0}};  
        
        int cycle2[][] = routeBack(cycle1);
      
        int rota1[][] = new int[24][2];        
        int rota2[][] = new int[24][2];  
                
        int i;    
        int contRota1=0, contRota2=0;
        
        System.out.println("\nsrc: "+src+" dst: "+dst);
        int salto1d=0, salto2d=0;
        for (i = 0; i < cycle1.length; i++) {
            if( cycle1[i][0] == src) {
                salto1d=i;

            }
        }
        for (i = 0; i < cycle1.length; i++) {
            if( cycle1[i][1] == dst) {
                salto2d=i;
            }
        }     
//        if(src==4&&dst==2){
//        System.out.println("\nsalto1d: "+salto1d+" salto2d: "+salto2d);            
//        }
//        System.out.println("\nsalto1d: "+salto1d+" salto2d: "+salto2d);
        System.out.println("Rota1: ");
        int contRota=0;
        if(salto1d<salto2d){
            for (i = 0; i < cycle1.length; i++) {
                if(i>salto1d-1&&i<salto2d+1){
                     rota1[contRota][0]=cycle1[i][0];
                     rota1[contRota++][1]=cycle1[i][1]; 
                     System.out.print("{"+cycle1[i][0]+","+cycle1[i][1]+"}");                     
                }
            }
//            System.out.println();
            contRota1=contRota;
        }
        if(salto1d>salto2d){ 
            contRota=0;            
            for (i = salto1d; i < cycle1.length; i++) {
//                if(i>salto1d-1){
                     rota1[contRota][0]=cycle1[i][0];
                     rota1[contRota++][1]=cycle1[i][1];  
                     System.out.print("{"+cycle1[i][0]+","+cycle1[i][1]+"}");                                          
//                }
            }       
            for (i = 0; i < salto2d+1; i++) {
                if(i<salto2d+1){                
                     rota1[contRota][0]=cycle1[i][0];
                     rota1[contRota++][1]=cycle1[i][1];  
                     System.out.print("{"+cycle1[i][0]+","+cycle1[i][1]+"}");                                          
                }
            }     
            contRota1=contRota;
            
//            int r1[] = new int[contRota+1];
////            System.out.println();
//            for (i = 0; i < contRota; i++) {
////                System.out.print("{"+rota1[i][0]+","+rota1[i][1]+"}");
//                r1[i]=rota1[i][0];
//            }        
//            r1[i]=rota1[i-1][1];            
        }    
        if(salto1d==salto2d){  
            contRota=0;            
            rota1[contRota][0]=cycle1[salto1d][0];
            rota1[contRota][1]=cycle1[salto2d][1];    
            int r1[] = new int[2]; 
            r1[0]=rota1[0][0];            
            r1[1]=rota1[0][1];  
            contRota1=1;            
        }        

        
        System.out.println("\nsrc: "+src+" dst: "+dst);
        salto1d=0;
        salto2d=0;
        for (i = 0; i < cycle1.length; i++) {
            if( cycle2[i][0] == src) {
                salto1d=i;

            }
        }
        for (i = 0; i < cycle1.length; i++) {
            if( cycle2[i][1] == dst) {
                salto2d=i;
            }
        }        
//        System.out.println("salto1d: "+salto1d+" salto2d: "+salto2d);
        System.out.println("Rota2: ");
        contRota=0;
        if(salto1d<salto2d){
            for (i = 0; i < cycle2.length; i++) {
                if(i>salto1d-1&&i<salto2d+1){
                     rota2[contRota][0]=cycle2[i][0];
                     rota2[contRota++][1]=cycle2[i][1]; 
                     System.out.print("{"+cycle1[i][0]+","+cycle1[i][1]+"}");                     
                }
            }
            System.out.println();
            contRota2=contRota;
        }
        
        if(salto1d>salto2d){
            for (i = salto1d; i < cycle2.length; i++) {
//                if(i>salto1d-1){
                     rota2[contRota][0]=cycle2[i][0];
                     rota2[contRota++][1]=cycle2[i][1];  
//                }
            }       
            for (i = 0; i < salto2d+1; i++) {
                if(i<salto2d+1){                
                     rota2[contRota][0]=cycle2[i][0];
                     rota2[contRota++][1]=cycle2[i][1];  
                }
            }  
            contRota2=contRota;
            
//            int r2[] = new int[contRota+1];
////            System.out.println();
//            for (i = 0; i < contRota; i++) {
////                System.out.print("{"+rota2[i][0]+","+rota2[i][1]+"}");
//                r2[i]=rota2[i][0];
//            }        
//            r2[i]=rota2[i-1][1];    
        }      
        if(salto1d==salto2d){   
            contRota=0;            
            rota2[contRota][0]=cycle2[salto1d][0];
            rota2[contRota][1]=cycle2[salto2d][1];    
            int r2[] = new int[2]; 
            r2[0]=rota2[0][0];            
            r2[1]=rota2[0][1];   
            contRota2=1;
            
        }          
        contRota2=0;
        for (i = 0; i < rota2.length; i++) {
            if(rota2[i][0]!=0||rota2[i][1]!=0){
                System.out.print("{"+rota2[i][0]+","+rota2[i][1]+"}");
                contRota2++;
            }
        } 
        int r2[] = new int[contRota2+1];
        int j;
        for (j = 0; j < contRota2; j++) {
           r2[j]=rota2[j][0];                   
        }    
        r2[j]=dst; 
//        System.out.println();
//        for (j = 0; j < r2.length; j++) {
//            System.out.print("-"+r2[j]);
//        }         
        System.out.println("\ncontRota2: "+contRota2);
        contRota1=0;
        for (i = 0; i < rota1.length; i++) {
            if(rota1[i][0]!=0||rota1[i][1]!=0){            
//                System.out.print("{"+rota1[i][0]+","+rota1[i][1]+"}");
                contRota1++;
            }
        }       
        int r1[] = new int[contRota1+1];
        for (j = 0; j < contRota1; j++) {
           r1[j]=rota1[j][0];                   
        }    
        r1[j]=dst; 
//        System.out.println();
//        for (j = 0; j < r1.length; j++) {
//            System.out.print("-"+r1[j]);
//        }         
//        System.out.println("\ncontRota1: "+contRota1);
//            System.out.println();        
        
//        int contR2=0;
//        for (int j = 0; j < rota2.length; j++) {
//           if(rota2[j][0]!=0&&rota2[j][1]!=0){
//               contR2++;
//           }                    
//        }
//        int r2[] = new int[contR2+1];
//        int j;
//        for (j = 0; j < contR2; j++) {
//           r2[j]=rota2[j][0];                   
//        }    
//        r2[j]=rota2[j-1][1];   
//        System.out.println();
//        for (j = 0; j < r2.length; j++) {
//            System.out.print("-"+r2[j]);
//        }   
//
//        int contR1=0;
//        for (j = 0; j < rota1.length; j++) {
//           if(rota1[j][0]!=0&&rota1[j][1]!=0){
//               contR1++;
//           }                    
//        }
//        int r1[] = new int[contR1+1];
//        for (j = 0; j < contR1; j++) {
//           r1[j]=rota1[j][0];                   
//        }    
//        r1[j]=rota1[j-1][1];   
//        System.out.println();
//        for (j = 0; j < r1.length; j++) {
//            System.out.print("-"+r1[j]);
//        }           
        
        if(r1.length>r2.length){
            if(straddling){             
               return r2;
            }else{
               return r1;
            }            
        }else{
            if(straddling){
               return r1;
            }else{
               return r2;
            }              
        }
              
//        return null;
    }
    
    private int[][] routeBack(int cycle[][]){
                
        int cycleBACK[][] = new int[24][2];
          
        int cont=0;
        for(int i=cycle.length-1;i>-1;i--){
           cycleBACK[cont][0]=cycle[i][1];  
           cycleBACK[cont][1]=cycle[i][0];
           cont++;
        }         
                
//        System.out.print("{");
//        for(int i=0;i<cont;i++){
//           System.out.print("{"+cycleBACK[i][0]+","+cycleBACK[i][1]+"},");        
//        }         
//        System.out.println("}");         
        
              
        return cycleBACK;
    }
    
    
}

