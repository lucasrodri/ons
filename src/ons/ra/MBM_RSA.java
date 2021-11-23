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
import ons.util.DSP;
import ons.util.WeightedGraph;
import ons.util.YenKSP;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * The my proposed to use the optical agregation.
 * Name: Maximize the use of Best Modulation format (MBM)
 * @author lucas
 */
public class MBM_RSA implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;
    static int conta = 0;

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        //The default modulation
        this.modulation = Modulation._QPSK;
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
        LightPath[] lps;
        LightPath lp_aux;
        ArrayList<LightPath> lightpaths = new ArrayList<>();
        int usedTransponders = 0;
        
        //parametros:
        int ksp = 3;
        
        // Try existent lightpaths first (Eletrical Grooming)
        ///*
        lp_aux = getLightpathBWAvailable(flow.getSource(), flow.getDestination(), flow.getRate());
        if (lp_aux != null) {
            lightpaths.add(lp_aux);
            lps = arrayListLPToArrayLP(lightpaths);
            cp.acceptFlow(flow.getID(), lps);
            return;
        }
        //*/
        
        lp_aux = FPA(flow, ksp);//uso de FPA classico
        if (lp_aux != null) {
            lightpaths.add(lp_aux);
            lps = arrayListLPToArrayLP(lightpaths);
            cp.acceptFlow(flow.getID(), lps);
            return;
        }
        
        ArrayList<LightPath> lightpathsInGrooming = new ArrayList<>();
        
        int srcNode = flow.getSource(), dstNode;
        ArrayList<Integer> overPath;
        
        ArrayList<Integer> bestPath = new ArrayList<>(), copyPath;
        int maxSizePath = Integer.MAX_VALUE;
        
        // k-Shortest Paths routing disjoint first
        //PASSO 1
        //ArrayList<Integer>[] kpaths = DSP.disjointShortestPaths(graph, flow.getSource(), flow.getDestination());
        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);
        //PASSO 2
        ArrayList<LightPath> lpsSource = cp.getVT().getLightpathsSrc(flow.getSource());
        for (ArrayList<Integer> path : kpaths) {
            copyPath = new ArrayList<>(path);
            //PASSO 3
            if(!lightpaths.isEmpty()){
                for (LightPath lightpath : lightpaths) {
                    if (!lightpathsInGrooming.contains(lightpath)) {
                        cp.getVT().deallocatedLightpath(lightpath.getID());
                    }
                }
                lightpaths.clear();
                lightpathsInGrooming.clear();
            }
            srcNode = flow.getSource();
            LightPath candidate = searchCandidate(path, lpsSource);
            while(candidate != null){
                overPath = findOverPath(path, candidate);//path de nodes, nao e de links
                dstNode = overPath.get(overPath.size()-1);
                
                //tentando agregacao de dados
                lp_aux = getLightpathBWAvailable(srcNode, dstNode, flow.getRate());
                if(lp_aux != null){
                    lightpathsInGrooming.add(lp_aux);
                } else {
                    lp_aux = createLightPathInOpticalGrooming(overPath, flow.getRate(), candidate);
                }
                
                if (lp_aux != null) {
                    if (dstNode != flow.getDestination()) {
                        lightpaths.add(lp_aux);
                        srcNode = dstNode;
                    } else {
                        lightpaths.add(lp_aux);
                        lps = arrayListLPToArrayLP(lightpaths);
                        cp.acceptFlow(flow.getID(), lps);
                        return;
                    }
                    lpsSource = cp.getVT().getLightpathsSrc(srcNode);
                    removePath(path, overPath);
                    if (path.size() < maxSizePath) {
                        bestPath = new ArrayList<>(copyPath);
                        maxSizePath = path.size();
                    }
                } else {
                    lpsSource.remove(candidate);
                }
                candidate = searchCandidate(path, lpsSource);
            }
        }
        //agora eu tenho o bestPath vou fazer o caminho por ele:
        
        
        if (!lightpaths.isEmpty()) {
            for (LightPath lightpath : lightpaths) {
                if (!lightpathsInGrooming.contains(lightpath)) {
                    cp.getVT().deallocatedLightpath(lightpath.getID());
                }
            }
            lightpaths.clear();
            lightpathsInGrooming.clear();
        }
        srcNode = flow.getSource();
        lpsSource = cp.getVT().getLightpathsSrc(srcNode);
        LightPath candidate = searchCandidate(bestPath, lpsSource);
        while (candidate != null) {
            overPath = findOverPath(bestPath, candidate);//path de nodes, nao e de links
            dstNode = overPath.get(overPath.size() - 1);

            //tentando agregacao de dados
            lp_aux = getLightpathBWAvailable(srcNode, dstNode, flow.getRate());
            if (lp_aux != null) {
                lightpathsInGrooming.add(lp_aux);
            } else {
                lp_aux = createLightPathInOpticalGrooming(overPath, flow.getRate(), candidate);
            }

            if (lp_aux != null) {
                if (dstNode != flow.getDestination()) {
                    lightpaths.add(lp_aux);
                    srcNode = dstNode;
                } else {
                    lightpaths.add(lp_aux);
                    lps = arrayListLPToArrayLP(lightpaths);
                    cp.acceptFlow(flow.getID(), lps);
                    return;
                }
                lpsSource = cp.getVT().getLightpathsSrc(srcNode);
                removePath(bestPath, overPath);
            } else {
                lpsSource.remove(candidate);
            }
            candidate = searchCandidate(bestPath, lpsSource);
        }
        
        
        
        
        
        
        
        
        //PASSO 7:
        // k-Shortest Paths routing
        usedTransponders++;
        //srcNode = flow.getSource();
        kpaths = YenKSP.kShortestPaths(graph, srcNode, flow.getDestination(), ksp);
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
            // First-Fit spectrum assignment
            int requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
            int[] firstSlot;
            for (int i = 0; i < links.length; i++) {
                // Try the slots available in each link
                firstSlot = ((EONLink) cp.getPT().getLink(links[i])).getSlotsAvailableToArray(requiredSlots);
                for (int j = 0; j < firstSlot.length; j++) {
                    // Now you create the lightpath to use the createLightpath VT
                    //Relative index modulation: BPSK = 0; QPSK = 1; 8QAM = 2; 16QAM = 3;
                    EONLightPath lp = cp.createCandidateEONLightPath(srcNode, flow.getDestination(), links,
                            firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
                    // Now you try to establish the new lightpath, accept the call
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        // Single-hop routing (end-to-end lightpath)

                        lightpaths.add(cp.getVT().getLightpath(id));
                        lps = arrayListLPToArrayLP(lightpaths);
                        cp.acceptFlow(flow.getID(), lps);
                        return;
                    }
                }
            }
        }
        //PASSO 8:
        // Block the call
        /*
        System.err.println("Fonte:" + flow.getSource() + "; Destino:" + flow.getDestination());
        System.err.println(cp.getPT().getNode(flow.getSource()).hasFreeGroomingInputPort());
        System.err.println(cp.getPT().getNode(flow.getDestination()).hasFreeGroomingOutputPort());

        for (int i = 0; i < 42; i++) {
            ((EONLink) cp.getPT().getLink(i)).printLink();
        }
        */
        if (!lightpaths.isEmpty()) {
            for (LightPath lightpath : lightpaths) {
                if (!lightpathsInGrooming.contains(lightpath)) {
                    cp.getVT().deallocatedLightpath(lightpath.getID());
                }
            }
            lightpaths.clear();
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
    
    /**
     * Retrieves the least loaded lightpath available allocated
     * @param source the source flow
     * @param destination the destination flow
     * @param rate the rate flow
     * @return the lightpath if this exists, 'null' otherwise
     */
    private LightPath getLightpathBWAvailable(int source, int destination, int rate) {
        long abw_aux, abw = 0;
        LightPath lp_aux, lp = null;

        // Get the available lightpaths
        TreeSet<LightPath> lps = cp.getVT().getAvailableLightpaths(source, destination, rate);
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
        if(lp != null && cp.getVT().getLightpathBWAvailable(lp.getID()) >= rate){
            return lp;
        }
        return null;
    }
    
    /**
     * Find the best lightpath candidate for optical grooming.
     * The lightpath with more links in common
     * @param path the path nodes
     * @param lpsSource the set os lightptahs in this source
     * @return the best candidate lightpath
     */
    private LightPath searchCandidate(ArrayList<Integer> path, ArrayList<LightPath> lpsSource) {
        if(path.isEmpty()){
            return null;
        }
        ArrayList<LightPath> lpsCandidates = new ArrayList<>();
        // Create the linkPath
        int[] linkPath = new int[path.size() - 1];
        for (int j = 0; j < path.size() - 1; j++) {
            linkPath[j] = cp.getPT().getLink(path.get(j), path.get(j + 1)).getID();
        }
        //os que tem o primeito link em comum (PRIMEIRA TRIAGEM)
        for (LightPath lpSrc : lpsSource) {
            if (lpSrc.getLinks()[0] == linkPath[0]) {
                lpsCandidates.add(lpSrc);
            }
        }
        //destes, os que tem mais links em comum (SEGUNDA TRIAGEM)...
        int commonLinks_aux = 0;
        int commonLinks;
        LightPath finalyCandidate = null;
        
        for (LightPath candidate : lpsCandidates) {
            commonLinks = 1;
            for (int j = 1; j < candidate.getLinks().length && j < linkPath.length; j++) {
                if (candidate.getLinks()[j] == linkPath[j]) {
                    commonLinks++;
                }
            }
            if (commonLinks > commonLinks_aux) {
                finalyCandidate = candidate;
                commonLinks_aux = commonLinks;
            }
        }
        return finalyCandidate;
    }

    /**
     * Create a new lightpath in optical grooming.
     * @param path the path of this lightpath (nodes)
     * @param rate the flow rate
     * @param candidate the lightpath will be groomed
     * @return the lightpath, or null if is not possible to created
     */
    //AGORA AKI TA O ERRO!
    private LightPath createLightPathInOpticalGrooming(ArrayList<Integer> path, int rate, LightPath candidate) {
        LightPath lp = null;
        long id;
        if ((id = cp.getVT().createLightpathInOpticalGrooming(path, candidate, rate)) >= 0) {
            return cp.getVT().getLightpath(id);
        }
        return lp;
    }
    
    /**
     * Converts the ArrayList<LightPath> in Array LightPath
     * @param lightpaths the ArrayList<LightPath>
     * @return the array with objects LightPath
     */
    private LightPath[] arrayListLPToArrayLP(ArrayList<LightPath> lightpaths) {
        LightPath[] lps = new LightPath[lightpaths.size()];
        for(int i = 0; i < lightpaths.size(); i++){
            lps[i] = lightpaths.get(i);
        }
        return lps;
    }

    /**
     * Retrieves the common path of this path and this lightpath candidate
     * @param path the links path 
     * @param candidate the lightpath candidate
     * @return the common path
     */
    private ArrayList<Integer> findOverPath(ArrayList<Integer> path, LightPath candidate) {
        ArrayList<Integer> overPath = new ArrayList<>();
        // Create the linkPath de 'nodes'
        int[] nodes = new int[candidate.getLinks().length + 1];
        nodes[0] = cp.getPT().getLink(candidate.getLinks()[0]).getSource();
        for (int i = 0; i < candidate.getLinks().length; i++) {
            nodes[i+1] = cp.getPT().getLink(candidate.getLinks()[i]).getDestination();
        }
        for (int i = 0; i < path.size() && i < nodes.length; i++) {
            if(path.get(i) == nodes[i]){
                overPath.add(path.get(i));
            } else {
                return overPath;
            }
        }
        return overPath;
    }
    
    /**
     * Remove the overPath of this path.
     * The last node of the overPath can not removed
     * @param path the path of nodes
     * @param overPath the over path of node
     */
    private void removePath(ArrayList<Integer> path, ArrayList<Integer> overPath) {
        for (int i = 0; i < overPath.size() - 1; i++) {
            path.remove(overPath.get(i));
        }
    }
    
    //problema ta aki
    private LightPath FPA(Flow flow, int ksp){
        LightPath lp = null;
        // k-Shortest Paths routing disjoint first
        //PASSO 1
        ArrayList<Integer>[] kpaths = DSP.disjointShortestPaths(graph, flow.getSource(), flow.getDestination());
        //ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, flow.getSource(), flow.getDestination(), ksp);
        //PASSO 2
        ArrayList<LightPath> lpsSource = cp.getVT().getLightpathsSrc(flow.getSource());
        for (ArrayList<Integer> path : kpaths) {
            //PASSO 3
            LightPath candidate = searchCandidate(path, lpsSource);
            while(candidate != null){
                //PASSO 4
                lp = createLightPathInOpticalGrooming(path, flow.getRate(), candidate);
                //PASSO 5
                if(lp != null){
                    return lp;
                } else {
                    lpsSource.remove(candidate);
                }
                //PASSO 6
                candidate = searchCandidate(path, lpsSource);
            }
        }
        return lp;
    }
}
