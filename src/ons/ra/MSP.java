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
import java.util.Iterator;
import java.util.TreeSet;

/**
 * The proposed Xin Wan.
 * Article: "Dynamic routing and spectrum assignment in spectrum flexible transparent optical networks",
 * Xin Wan, Nan Hua, and Xiaoping Zheng,
 * Optical Communications and Networking, IEEE/OSA Journal of, 
 * Aug 2012.
 * @author lucas
 */
public class MSP implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;
    private long cont = 0;

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
        lps[0] = getLeastLoadedLightpath(flow);
        if (lps[0] instanceof LightPath) {
            if (cp.acceptFlow(flow.getID(), lps)) {
                return;
            }
        }
        
        //Step 1
        // Initialize the visited nodes
        TreeSet<Integer> M = new TreeSet<>();
        M.add(flow.getSource());
        // Initialize the routing cost
        double[] D = new double[graph.size()];
        for(int i = 0; i < D.length; i++){
            D[i] = Double.MAX_VALUE;
        }
        D[flow.getSource()] = 0;
        // Initialize the aggregated available spectrum
        ArrayList<Integer>[] S = new ArrayList[graph.size()];
        for(int i = 0; i < S.length; i++){
            S[i] = new ArrayList<>();
        }
        S[flow.getSource()].add(0);
        // Initialize the routed path
        ArrayList<Integer>[] P = new ArrayList[graph.size()];
        for(int i = 0; i < P.length; i++){
            P[i] = new ArrayList<>();
        }
        P[flow.getSource()].add(flow.getSource());
        
        //Step 2 to 4
        // Identify the neighbors
        int[] neighbors = graph.neighbors(flow.getSource());
        // Identify the B, where the W(S_i,B+G) is identify by the method hasSlotsAvaiable
        int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
        
        for(int i = 0; i < neighbors.length; i++){
            if (((EONLink) cp.getPT().getLink(flow.getSource(), neighbors[i])).hasSlotsAvaiable(requiredSlots)) {
                D[neighbors[i]] = graph.getWeight(flow.getSource(), neighbors[i]);                
                S[neighbors[i]] = ((EONLink) cp.getPT().getLink(flow.getSource(), neighbors[i])).getSlotsAvailable(requiredSlots);
                P[neighbors[i]].add(flow.getSource());
                P[neighbors[i]].add(neighbors[i]);
            }
        }
        // Step 5 to 17
        while(!M.contains(flow.getDestination())){
            // Step 6 Search the j in N - M with the min D
            int j = minD(M,D,graph);
            if (j != -1){
                M.add(j);
                // Step 9
                // All node k in N-M
                int[] k = nodesiInNnotInM(graph.size(),M);
                for(int i = 0; i < k.length; i++){
                    if(graph.isEdge(j, k[i])){
                        if((D[k[i]] > D[j] + graph.getWeight(j, k[i])) && !Psi(S,j,k[i],requiredSlots).isEmpty()){
                            P[k[i]].clear();
                            P[k[i]].addAll(P[j]);
                            P[k[i]].add(k[i]);
                            S[k[i]].addAll(Psi(S,j,k[i],requiredSlots));
                            D[k[i]] = D[j] + graph.getWeight(j, k[i]);
                        }
                    }
                }
            } else{
                cp.blockFlow(flow.getID());
                return;
            }
        }  
        
        // Shortest-Path routing
        nodes = arrayListToArrayInt(P[flow.getDestination()]);
        //System.out.println("ACEITOU" + cont++);
        int[] spectrum = arrayListToArrayInt(S[flow.getDestination()]);
        
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
        // spectrum assignment in BPSK Modulation

        for (int j = 0; j < spectrum.length; j++) {
            // Now you create the lightpath to use the createLightpath VT
            //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
            EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                    spectrum[j], (spectrum[j] + requiredSlots - 1), modulation);
            // Now you try to establish the new lightpath, accept the call
            if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                // Single-hop routing (end-to-end lightpath)
                lps[0] = cp.getVT().getLightpath(id);
                if(!cp.acceptFlow(flow.getID(), lps)) {
                    cp.getVT().deallocatedLightpaths(lps);
                } else {
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

    private int minD(TreeSet<Integer> M, double[] D, WeightedGraph graph) {
        int node = -1;
        double distance = Double.MAX_VALUE;
        for(int i = 0; i < graph.size(); i++){
            if(!M.contains(i)){
                if(D[i] < distance){
                    node = i;
                    distance = D[i];
                }
            }
        }
        return node;
    }

    private int[] nodesiInNnotInM(int graphsize, TreeSet<Integer> M) {
        ArrayList<Integer> nodes = new ArrayList<>();
        for(int i = 0; i < graphsize; i++){
            if(!M.contains(i)){
                nodes.add(i);
            }
        }
        int[] k = new int [nodes.size()];
        for(int i = 0; i < nodes.size(); i++){
            k[i] = nodes.get(i);
        }
        return k;
    }
    
    /**
     * Psi is: (S_jk intersection S_j, B+G)
     * @param S
     * @param j
     * @param k
     * @param requiredSlots
     * @return 
     */

    private ArrayList<Integer> Psi(ArrayList<Integer>[] S, int j, int k, int requiredSlots) {
        ArrayList<Integer> slotsAvailableJK = ((EONLink) cp.getPT().getLink(j, k)).getSlotsAvailable(requiredSlots);
        ArrayList<Integer> slotsAvailable = new ArrayList<>();
        int elemento;

        Iterator<Integer> iterator = slotsAvailableJK.iterator();
        while (iterator.hasNext()) {
            elemento = iterator.next();
            if (S[j].contains(elemento)) {
                slotsAvailable.add(elemento);
            }
        }
        
        return slotsAvailable;
    }

    private int[] arrayListToArrayInt(ArrayList<Integer> P) {
        int [] path = new int[P.size()];
        for(int i = 0; i < path.length; i++){
            path[i] = P.get(i);
        }
        return path;
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
