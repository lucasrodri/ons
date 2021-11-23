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
import ons.PhysicalImpairments;
import ons.util.Dijkstra;
import ons.util.WeightedGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * The proposed by Italo
 *
 * @author lucas
 */
public class ARTO implements RA {

    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;
    private static int count = 0;
    private PhysicalImpairments pi = PhysicalImpairments.getPhysicalImpairmentsObject();

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
        double[] snr;
        long id;
        int noEsc;
        LightPath[] lps;
        ArrayList<Integer> listaNosRegeneradores = new ArrayList<>();
        
        // k-Shortest Paths routing
        nodes = Dijkstra.getShortestPath(graph, flow.getSource(), flow.getDestination());
        // If no possible path found, block the call
        if (nodes.length == 0 || nodes == null) {
            cp.blockFlow(flow.getID());
            return;
        }
        snr = new double[nodes.length];
        int indexOrigem = 0;
        //FOR DAS LINHA 5 A 14 DO ARTO
        for (int indexAtual = 0; indexAtual < nodes.length; indexAtual++) {
            if((snr[indexAtual] = getSNR(flow.getRate(), nodes, indexAtual, indexOrigem)) < Modulation._BPSK_SNR){  
                if(snr[indexAtual] < 0){
                    cp.blockFlow(flow.getID());
                    return;
                }
                noEsc = nodes[indexAtual];
                while (!cp.getPT().getNode(noEsc).hasFreeGroomingInputPort() || !cp.getPT().getNode(noEsc).hasFreeGroomingOutputPort()) {                    
                    indexAtual--;
                    noEsc = nodes[indexAtual];
                    if(noEsc == nodes[indexOrigem]){
                        cp.blockFlow(flow.getID());
                        return;
                    }
                }
                listaNosRegeneradores.add(noEsc);
                indexOrigem = indexAtual;
            }
        }
        listaNosRegeneradores.add(flow.getDestination());
        int [] modList = new int[listaNosRegeneradores.size()];
        int requiredSlots, r = 0, noOrigem = flow.getSource();
        lps = new LightPath[listaNosRegeneradores.size()];
        
        //ja temos os nos de regeneracao agora falta escolher a modulacao
        //FOR DAS LINHAS 16 A 29
        for (Integer regenerador : listaNosRegeneradores) {
            modList[r] = Modulation.getBestModulationSNR(snr[getIndex(regenerador, nodes)]);
            requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modList[r]);
            for (int m = Modulation._BPSK; m < EONPhysicalTopology.getMaxModulation(); m++) {
                int requiredSlotsTemp = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), m);
                if(requiredSlots == requiredSlotsTemp){
                    modList[r] = m;
                    break;
                }
            }
            //vamos alocar agora da noOrigem ate esse regenerador com a modList[r]
            
            int [] route = new int[getIndex(regenerador, nodes)-getIndex(noOrigem, nodes)+1];
            System.arraycopy(nodes, getIndex(noOrigem, nodes), route, 0, getIndex(regenerador, nodes)-getIndex(noOrigem, nodes)+1);
            
            if ((id = createLightpath(requiredSlots, route, modList[r])) >= 0) {
                lps[r] = cp.getVT().getLightpath(id);
            } else {
                //vai que Né!!!
                for (int k = 0; k < r; k++) {
                    cp.getVT().deallocatedLightpath(lps[k].getID());
                }
                cp.blockFlow(flow.getID());
                return;
            }
            noOrigem = regenerador;
            r++;
        }
        if (!cp.acceptFlow(flow.getID(), lps)) {
            //Esse if nunca vai entrar, mas vai que né!!!
            cp.getVT().deallocatedLightpaths(lps);
            cp.blockFlow(flow.getID());
        }
    }

    @Override
    public void flowDeparture(long id) {
    }

    private double getSNR(int rate, int[] nodes, int indexAtual, int indexOrigem) {
        if(indexAtual == indexOrigem){
            return Integer.MAX_VALUE;
        }
        int [] route = new int[indexAtual-indexOrigem+1];
        System.arraycopy(nodes, indexOrigem, route, 0, indexAtual-indexOrigem+1);
        LightPath lp = algoritmoDeAlocacacao(rate, route);
        if(lp == null){
            return -1;
        }
        return pi.computeSNRlightpath(lp);
    }

    private int getIndex(Integer regenerador, int[] nodes) {
        for (int i = 0; i < nodes.length; i++) {
            if(nodes[i] == regenerador) return i;
        }
        return -1;
    }

    private LightPath algoritmoDeAlocacacao(int rate, int[] route) {
        LightPath lp;
        // Create the links vector
        int[] links = new int[route.length - 1];
        for (int j = 0; j < route.length - 1; j++) {
            links[j] = cp.getPT().getLink(route[j], route[j + 1]).getID();
        }
        int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), Modulation._BPSK);
        int[] firstSlot;
        for (int i = 0; i < links.length; i++) {
            // Try the slots available in each link
            firstSlot = ((EONLink) cp.getPT().getLink(links[i])).getSlotsAvailableToArray(requiredSlots);
            for (int j = 0; j < firstSlot.length; j++) {
                // Now you create the lightpath to use the createLightpath VT
                lp = cp.createCandidateEONLightPath(route[0], route[route.length-1], links,
                        firstSlot[j], (firstSlot[j] + requiredSlots - 1), Modulation._BPSK);
                if (cp.getPT().canCreatePhysicalLightpath(lp)) {
                    return lp;
                }
            }
        }
        return null; //nao consegue criar rota
    }

    private long createLightpath(int requiredSlots, int[] route, int mod) {
        long id;
        // Create the links vector
        int[] links = new int[route.length - 1];
        for (int j = 0; j < route.length - 1; j++) {
            links[j] = cp.getPT().getLink(route[j], route[j + 1]).getID();
        }
        int[] firstSlot;
        for (int i = 0; i < links.length; i++) {
            // Try the slots available in each link
            firstSlot = ((EONLink) cp.getPT().getLink(links[i])).getSlotsAvailableToArray(requiredSlots);
            for (int j = 0; j < firstSlot.length; j++) {
                // Now you create the lightpath to use the createLightpath VT
                EONLightPath lp = cp.createCandidateEONLightPath(route[0], route[route.length-1], links,
                        firstSlot[j], (firstSlot[j] + requiredSlots - 1), mod);
                // Now you try to establish the new lightpath, accept the call                    
                if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                    return id;
                } 
            }
        }
        return -1;
    }
}
