/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ons.ra;

import java.util.ArrayList;
import ons.Core;
import ons.EONLightPath;
import ons.EONLink;
import ons.EONPhysicalTopology;
import ons.Flow;
import ons.LightPath;
import ons.Modulation;
import ons.util.Dijkstra;
import ons.util.WeightedGraph;

/**
 * This is a sample algorithm for the Routing and Spectrum Core Alocation problem.
 *
 * Fixed path routing is the simplest approach to finding a lightpath. The same
 * fixed route for a given source and destination pair is always used. This path
 * is computed using Dijkstra's Algorithm.
 *
 * First-Fit slots set assignment tries to establish the lightpath using the
 * first slots set available sought in the increasing order.
 */
public class CASD_PushPull implements RA {
    
    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private int modulation;
    private int limiarSC = 1000;

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
    public void flowArrival(Flow flow) {
        System.out.println("Fluxo: " + flow.getID());
        CASDdefragmentationAlgorithm();
        System.out.println("Fim do CASD: " + flow.getID());
        int[] nodes;
        int[] links;
        long id;
        LightPath[] lps = new LightPath[1];

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
        
        //Get the distance the size in KM  link on the route
        double largestLinkKM = 0;
        for (int i = 0; i < links.length; i++) {
            largestLinkKM = largestLinkKM + ((EONLink) cp.getPT().getLink(links[i])).getWeight();
        }
        //Adaptative modulation:
        int modulation = Modulation.getBestModulation(largestLinkKM);

        int requiredSlots;
        requiredSlots = Modulation.convertRateToSlot(flow.getRate(), EONPhysicalTopology.getSlotSize(), modulation);
        for (int i = 0; i < links.length; i++){
            if (!((EONLink) cp.getPT().getLink(links[i])).hasSlotsAvaiable(requiredSlots)){
                cp.blockFlow(flow.getID()); 
                return;
            }
        }
        int[] firstSlot;
        
        int numCores = ((EONLink) cp.getPT().getLink(links[0])).getNumCores();
        int[] cores = new int[links.length];
        for (int core = 0; core < numCores; core++) {
            //TODO colocar FF para pares e LF para impares
            firstSlot = ((EONLink) cp.getPT().getLink(links[0])).firstFit(core, requiredSlots);
            for (int slot = 0; slot < firstSlot.length; slot++) {
                clearCores(cores);
                cores[0] = core;
                for (int link = 1; link < links.length; link++) {
                    for (int coreAux = 0; coreAux < numCores; coreAux++) {
                        if(((EONLink) cp.getPT().getLink(links[link])).areSlotsAvaiable(coreAux, firstSlot[slot], (firstSlot[slot] + requiredSlots - 1))){
                            cores[link] = coreAux;
                            break;
                        }
                    }
                    if(cores[link] == -1) {
                        break;
                    }
                }
                if(checkCores(cores)) {
                    EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), links,
                            cores, firstSlot[slot], (firstSlot[slot] + requiredSlots - 1), modulation);
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        lps[0] = cp.getVT().getLightpath(id);
                        if (!cp.acceptFlow(flow.getID(), lps)) {
                            cp.getVT().deallocatedLightpaths(lps);
                        } else {
                            return;
                        }
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

    @Override
    public void simulationEnd() {
    }

    private void clearCores(int[] cores) {
        for (int i = 0; i < cores.length; i++) {
            cores[i] = -1;
        }
    }
    
    private boolean checkCores(int[] cores) {
        for (int i = 0; i < cores.length; i++) {
            if(cores[i] == -1) {
                return false;
            }
        }
        return true;
    }

    private void CASDdefragmentationAlgorithm() {
        int numCores = ((EONLink) cp.getPT().getLink(0)).getNumCores();
        ArrayList<Core> coresToDesfrag = new ArrayList<>();
        ArrayList<Double> scCores = new ArrayList<>();
        for(int l = 0; l < cp.getPT().getNumLinks(); l++) {
            for (int c = 0; c < numCores; c++) {
                Core core = ((EONLink) cp.getPT().getLink(l)).getCores()[c];
                //Trocar para entropia!!!
                double scAtual = core.getSC();
                //Trocar para entropia!!!
                if (scAtual < limiarSC) {
                    coresToDesfrag.add(core);
                    scCores.add(scAtual);
                }
            }
        }
        sortCores(coresToDesfrag, scCores);
        for (Core coreDesfrag : coresToDesfrag) {
            for (long idLP : coreDesfrag.getLightpaths()) {
                EONLightPath currentLP = (EONLightPath) cp.getVT().getLightpath(idLP);
                int firstSlot = currentLP.getFirstSlot();
                int lastSlot = currentLP.getLastSlot();
                
                //TODO fazer um novo metodo que vai desfragmentar tanto para vertical, horizontal e diagonal
                if(((EONLink) cp.getPT().getLink(coreDesfrag.getLinkID())).hasAvailableCore(firstSlot, lastSlot, coreDesfrag)) {
                    sameSpectrumDifferentCore(currentLP, coreDesfrag);
                } else {
                    if(coreDesfrag.hasSlotsAvaiable(lastSlot - firstSlot + 1)) {
                        differentSpectrumSameCore(currentLP, coreDesfrag);
                    }
                }
                /*
                if (coreDesfrag.hasSlotsAvaiable(lastSlot - firstSlot + 1)) {
                    differentSpectrumSameCore(currentLP, coreDesfrag);
                } else {
                    if (((EONLink) cp.getPT().getLink(coreDesfrag.getLinkID())).hasAvailableCore(firstSlot, lastSlot, coreDesfrag)) {
                        sameSpectrumDifferentCore(currentLP, coreDesfrag);
                    }
                }
                */
            }
        }

    }

    /**
     * Ordenando do menor para o maior
     * @param nucleosParaDesfrag
     * @param scNucleos 
     */
    private void sortCores(ArrayList<Core> nucleosParaDesfrag, ArrayList<Double> scNucleos) {
        boolean swap = true;
        double auxSC;
        Core aux;
        while (swap) {
            swap = false;
            for (int i = 0; i < scNucleos.size() - 1; i++) {
                if (scNucleos.get(i) > scNucleos.get(i + 1)) {
                    swap = true;
                    auxSC = scNucleos.get(i);
                    scNucleos.set(i, scNucleos.get(i + 1));
                    scNucleos.set(i + 1, auxSC);
                    aux = nucleosParaDesfrag.get(i);
                    nucleosParaDesfrag.set(i, nucleosParaDesfrag.get(i + 1));
                    nucleosParaDesfrag.set(i + 1, aux);
                }
            }
        }
    }

    private boolean sameSpectrumDifferentCore(EONLightPath currentLP, Core oldCore) {
        Flow flows[] = cp.getFlows(currentLP);
        boolean flag = false;
        double SC = oldCore.getSC();
        Core core = oldCore;
        int firstSlot = currentLP.getFirstSlot();
        int lastSlot = currentLP.getLastSlot();
        int requiredSlots = lastSlot - firstSlot + 1;
        int[] availableCores = ((EONLink) cp.getPT().getLink(oldCore.getLinkID())).getAvailableCore(firstSlot, lastSlot, oldCore);
        for (int i = 0; i < availableCores.length; i++) {
            Core newCore = ((EONLink) cp.getPT().getLink(oldCore.getLinkID())).getCores()[availableCores[i]];
            ((EONLink) cp.getPT().getLink(oldCore.getLinkID())).changeCore(firstSlot, lastSlot, oldCore, newCore);
            double SCAux = newCore.getSC();
            if (SC > SCAux) {
                int[] cores = changeCores(currentLP, newCore);
                if (((EONPhysicalTopology) cp.getPT()).getPI().checkXTNeighbors(currentLP, currentLP.getLinks(), cores, firstSlot, requiredSlots)) {
                    SC = SCAux;
                    core = newCore;
                }
            }
            ((EONLink) cp.getPT().getLink(oldCore.getLinkID())).changeCore(firstSlot, lastSlot, newCore, oldCore);
        }
        if(oldCore.getID() != core.getID()) {
            flag = true;
            for (Flow flow : flows) {
                LightPath[] lps = new LightPath[1];
                int[] cores = changeCores(currentLP, core);
                EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), currentLP.getLinks(),
                        cores, firstSlot, lastSlot, currentLP.getModulation());
                if (cp.getVT().reprovisionLightPath(currentLP.getID(), lp)) {
                    lps[0] = cp.getVT().getLightpath(currentLP.getID());
                    if (!cp.rerouteFlow(flow.getID(), lps)) {
                        cp.getVT().deallocatedReprovisionLightPath(currentLP.getID(), currentLP);
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    private boolean differentSpectrumSameCore(EONLightPath currentLP, Core currentCore) {
        Flow flows[] = cp.getFlows(currentLP);
        boolean flag = false;
        double SC = currentCore.getSC();
        int oldFirstSlot = currentLP.getFirstSlot();
        int oldLastSlot = currentLP.getLastSlot();
        int requiredSlots = oldLastSlot - oldFirstSlot + 1;
        
        int spectrum = oldFirstSlot;
        
        int[] availableSlots = ((EONLink) cp.getPT().getLink(currentCore.getLinkID())).firstFit(currentCore.getID(), requiredSlots);
        for (int i = 0; i < availableSlots.length; i++) {
            int newFirstSlot = availableSlots[i];
            int newLastSlot = (availableSlots[i] + requiredSlots - 1);
            
            if (!viablePushPull(currentLP, newFirstSlot)) {
                continue;
            }
            
            ((EONLink) cp.getPT().getLink(currentCore.getLinkID())).changeSpectrum(currentCore.getID(), oldFirstSlot, oldLastSlot, newFirstSlot, newLastSlot);
            double SCAux = currentCore.getSC();
            if(SC > SCAux) {
                if (((EONPhysicalTopology) cp.getPT()).getPI().checkXTNeighbors(currentLP, currentLP.getLinks(), currentLP.getCores(), newFirstSlot, requiredSlots)) {
                    SC = SCAux;
                    spectrum = newFirstSlot;
                }
            }
            ((EONLink) cp.getPT().getLink(currentCore.getLinkID())).changeSpectrum(currentCore.getID(), newFirstSlot, newLastSlot, oldFirstSlot, oldLastSlot);
            
        }
        if(spectrum != oldFirstSlot) {
            flag = true;
            for (Flow flow : flows) {
                LightPath[] lps = new LightPath[1];
                EONLightPath lp = cp.createCandidateEONLightPath(flow.getSource(), flow.getDestination(), currentLP.getLinks(),
                        currentLP.getCores(), spectrum, (spectrum + requiredSlots - 1), currentLP.getModulation());
                if (cp.getVT().reprovisionLightPath(currentLP.getID(), lp)) {
                    lps[0] = cp.getVT().getLightpath(currentLP.getID());
                    if (!cp.rerouteFlow(flow.getID(), lps)) {
                        cp.getVT().deallocatedReprovisionLightPath(currentLP.getID(), currentLP);
                        flag = false;
                    }
                }
            }
        }
        return flag;
    }

    private int[] changeCores(EONLightPath currentLP, Core newCore) {
        int[] cores = new int[currentLP.getNumCores()];
        for (int l = 0; l < currentLP.getLinks().length; l++) {
            if(currentLP.getLinks()[l] == newCore.getLinkID()) {
                cores[l] = newCore.getID();
            } else {
                cores[l] = currentLP.getCores()[l];
            }
        }
        return cores;
    }

    private boolean viablePushPull(EONLightPath currentLP, int newFirstSlot) {
        int sourceFirstSlot = currentLP.getFirstSlot(), sourceLastSlot = currentLP.getLastSlot();
        int requiredSlots = sourceLastSlot - sourceFirstSlot + 1;
        int destinationFirstSlot = newFirstSlot, destinationLastSlot = newFirstSlot + requiredSlots - 1;
        int[] links = currentLP.getLinks();
        int[] cores = currentLP.getCores();
        
        for (int link = 0; link < links.length; link++) {
            EONLink linkAtual = ((EONLink) cp.getPT().getLink(links[link]));
            int c = cores[link];
            Core core = linkAtual.getCores()[c];
            long[] slots = core.getSlots();
            boolean isViable = false;

            // 1 - verifica se o nucleo escolhido para o enlace atual do lp 
            // apresenta slots livres entre a origem e o destino do push-pull
            //isViable = (isSlotIntervalFree(sourceFirstSlot, destinationFirstSlot, slots) 
            //&& isSlotIntervalFree(sourceLastSlot, destinationLastSlot, slots));
            isViable = core.pushpull(sourceFirstSlot, destinationFirstSlot, sourceLastSlot, destinationLastSlot);
            // se for viavel, apresenta espaco para push-pull
            if (isViable) {
                continue;
            }

            //2 - se nao for possivel, encontrar um core que tenha o intervalo 
            // de espectro sem alocacao entre o lugar que vc queira mudar
            // Esse lugar tem q ser o lugar que tenha o menor SC (pra seguir o padrao do artigo)
            for (Core core2 : linkAtual.getCores()) {
                slots = core2.getSlots();
                //isViable = (isSlotIntervalFree(sourceFirstSlot, destinationFirstSlot, slots) 
                //		&& isSlotIntervalFree(sourceLastSlot, destinationLastSlot, slots));
                isViable = core2.pushpull(sourceFirstSlot, destinationFirstSlot, sourceLastSlot, destinationLastSlot);

                if (isViable) {
                    break;
                }
            }

            if (!isViable) {
                return false;
            }
        }

        return true;
    }
}
