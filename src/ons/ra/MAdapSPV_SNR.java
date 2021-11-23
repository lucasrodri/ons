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
 * The proposed m-Adap by Xin Wan in this RSA solver.
 * @author lucas
 */
public class MAdapSPV_SNR implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
    }
    
    @Override
    public void setModulation(int modulation) {
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
        
        int modulation = EONPhysicalTopology.getMaxModulation();
        int[] spectrum;
        int requiredSlots;
        
        //System.out.println(flow.getID());
        
        // Try existent lightpaths first (Grooming)
//        lps[0] = getLeastLoadedLightpath(flow);
//        if (lps[0] instanceof LightPath) {
//            if (cp.acceptFlow(flow.getID(), lps)) {
//                return;
//            }
//        }
        
        while (true) {

            //Step 1
            // The minimal routing cost
            double D_R = Double.MAX_VALUE;
            ArrayList<Integer> P_R = new ArrayList<>();
            TreeSet<Integer> S_R = new TreeSet<>();

            //Step 2
            // Initialize the root of PVST, where LEVEL is the index of T.
            ArrayList<Integer>[] T = new ArrayList[graph.size()];
            for (int i = 0; i < T.length; i++) {
                T[i] = new ArrayList<>();
            }
            T[0].add(flow.getSource());

            //The spectrum aggregated for each node in PVST
            ArrayList<ArrayList<Integer>>[] S = new ArrayList[graph.size()];
            for (int i = 0; i < S.length; i++) {
                S[i] = new ArrayList<>();
            }
            //The routing cost for each node in PVST
            ArrayList<Double>[] D = new ArrayList[graph.size()];
            for (int i = 0; i < D.length; i++) {
                D[i] = new ArrayList<>();
            }
            //The path vector for each node in PVST
            ArrayList<ArrayList<Integer>>[] P = new ArrayList[graph.size()];
            for (int i = 0; i < P.length; i++) {
                P[i] = new ArrayList<>();
            }

            //Step3
            // Identify the source's neighbors or T[0]'s neighbors
            int[] neighbors = graph.neighbors(flow.getSource());
            // Identify the B, where the W(S_i,B+G) is identify by the method hasSlotsAvailable
            requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
            for (int i = 0; i < neighbors.length; i++) {
                if (((EONLink) cp.getPT().getLink(flow.getSource(), neighbors[i])).hasSlotsAvaiable(requiredSlots)) {
                    T[1].add(neighbors[i]);
                    S[1].add(((EONLink) cp.getPT().getLink(flow.getSource(), neighbors[i])).getSlotsAvailable(requiredSlots));
                    D[1].add(graph.getWeight(flow.getSource(), neighbors[i]));
                    ArrayList<Integer> aux = new ArrayList<Integer>();
                    aux.add(flow.getSource());
                    aux.add(neighbors[i]);
                    P[1].add(aux);
                }
            }

            //Step 6
            for (int l = 1; l < graph.size() - 1; l++) {
                //Step 7 for all leaf T^k in LEVEL 'l'
                for (int k = 0; k < T[l].size(); k++) {
                    //Step 8
                    if (T[l].get(k) == flow.getDestination() && D[l].get(k) < D_R) {
                        P_R.clear();
                        P_R.addAll(P[l].get(k));
                        D_R = D[l].get(k);
                        S_R.addAll(S[l].get(k));
                    }
                    //Step 11
                    neighbors = graph.neighbors(T[l].get(k));
                    for (int i = 0; i < neighbors.length; i++) {
                        //Step 12
                        if (D[l].get(k) + graph.getWeight(T[l].get(k), neighbors[i]) < D_R) {
                            if (!P[l].get(k).contains(neighbors[i])) {
                                if (!Psi(S[l].get(k), T[l].get(k), neighbors[i], requiredSlots).isEmpty()) {
                                    T[l + 1].add(neighbors[i]);
                                //S[l+1].get(T[l+1].indexOf(neighbors[i])).clear();
                                    //S[l+1].get(T[l+1].indexOf(neighbors[i])).addAll(Psi(S[l].get(k), T[l].get(k), neighbors[i], requiredSlots));
                                    S[l + 1].add(Psi(S[l].get(k), T[l].get(k), neighbors[i], requiredSlots));
                                    //D[l+1].set(T[l+1].indexOf(neighbors[i]), D[l].get(k) + graph.getWeight(T[l].get(k), neighbors[i]));
                                    D[l + 1].add(D[l].get(k) + graph.getWeight(T[l].get(k), neighbors[i]));
                                    ArrayList<Integer> aux = new ArrayList<Integer>();
                                    aux.addAll(P[l].get(k));
                                    //aux.add(T[l].get(k));
                                    aux.add(neighbors[i]);
                                    P[l + 1].add(aux);
                                }
                            }
                        }
                    }
                }
            }
            if (!(D_R < Double.MAX_VALUE && !P_R.isEmpty() && !S_R.isEmpty())) {
                cp.blockFlow(flow.getID());
                return;
            }

            // Shortest-Path routing
            nodes = arrayListToArrayInt(P_R);

            //System.out.println("ACEITOU " + cont++);
            spectrum = treeSetToArrayInt(S_R);

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

            for (int j = 0; j < spectrum.length; j++) {
                // Now you create the lightpath to use the createLightpath VT
                //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
                EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                        spectrum[j], (spectrum[j] + requiredSlots - 1), modulation);
                // Now you try to establish the new lightpath, accept the call
                if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                    // Single-hop routing (end-to-end lightpath)
                    lps[0] = cp.getVT().getLightpath(id);
                    if(cp.acceptFlow(flow.getID(), lps)) {
                        return;
                    } else {
                        throw new IllegalArgumentException("Nao Deveria cair aqui!!!");
                    }
                } else {
                    //nao aceito por SNR
                }
            }
            modulation--;
            if (modulation == -1) {
                cp.blockFlow(flow.getID());
                return;
            }
        }
    }

    @Override
    public void flowDeparture(long id) {
    }
    
    /**
     * Psi is: (S_jk intersection S_j, B+G)
     * @param S
     * @param j
     * @param k
     * @param requiredSlots
     * @return 
     */
    private ArrayList<Integer> Psi(ArrayList<Integer> aggregatedS, int n, int v, int requiredSlots) {
        ArrayList<Integer> slotsAvailableNV = ((EONLink) cp.getPT().getLink(n, v)).getSlotsAvailable(requiredSlots);
        ArrayList<Integer> slotsAvailable = new ArrayList<>();
        int elemento;
        Iterator<Integer> iterator = slotsAvailableNV.iterator();
        while (iterator.hasNext()) {
            elemento = iterator.next();
            if (aggregatedS.contains(elemento)) {
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

    private int[] treeSetToArrayInt(TreeSet<Integer> S) {
        int [] spectrum = new int[S.size()];
        int i = 0;
        Iterator<Integer> iterator = S.iterator();
        while (iterator.hasNext()) {
            spectrum[i] = iterator.next();
            i++;
        }
        return spectrum;
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
