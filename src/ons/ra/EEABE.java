
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.ra;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import ons.Batch;
import ons.BulkData;
import ons.EONLightPath;
import ons.EONLink;
import ons.EONOXC;
import ons.EONPhysicalTopology;
import ons.Flow;
import ons.LightPath;
import ons.Modulation;
import ons.MyStatistics;
import ons.util.CombinationsBatch;
import ons.util.WeightedGraph;
import ons.util.YenKSP;

/**
 * Classe AARSASJ
 * @author Leia Sousa
 * date 18/04/2016
 *
 * Algoritmo AA-RSA do artigo do SBRC
 */
public class EEABE implements RABulk {

    private ControlPlaneForRA cp;
    private WeightedGraph graph;
    private MyStatistics st;
    private int modulation;
    private int byzantine = 3;
    private int ksp = 5;
    private double n;
    private double currentTime;
    private PriorityQueue<Request> window;
    private int currentRate;
    
    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        //SaveObject.save(this.graph, "l.sav");
        this.modulation = Modulation._QPSK;
        this.window = new PriorityQueue<>(new RequestSort());
    }

    @Override
    public void simulationEnd() {
        currentTime = cp.getCurrentTime();
        cleanWindow();
    }

    private int getIndex(BulkData bulk, Batch batch) {
        for (int i = 0; i < batch.getSize(); i++) {
            if(bulk.getID() == batch.getBulks().get(i).getID()) {
                return i;
            }
        }
        return -1;
    }

    private void sortSleepMode(ArrayList<Integer>[] kpaths) {
        ArrayList<Integer> aux;
        boolean change = true;
        while (change) {            
            change = false;
            for (int i = 0; i < kpaths.length - 1; i++) {
                if(wakeNode(kpaths[i]) > wakeNode(kpaths[i + 1])) {
                    aux = kpaths[i];
                    kpaths[i] = kpaths[i + 1];
                    kpaths[i + 1] = aux;
                    change = true;
                }
            }
        }
    }

    private int wakeNode(ArrayList<Integer> kpath) {
        int count = 0;
        for (int i = 0; i < kpath.size(); i++) {
            if(cp.getPT().getNode(kpath.get(i)).isSleep()) {
                count++;
            }
        }
        return count;
    }

    private static class RequestSort implements Comparator<Request>{
        @Override
        public int compare(Request r1, Request r2) {
            if (r1.getParam() < r2.getParam()) {
                return -1;
            }
            if (r1.getParam() > r2.getParam()) {
                return 1;
            }
            return 0;
        }
        
    }
    
    private static class Request {
        
        BulkData bulk;
        Batch batch;
        double param;
        public Request(BulkData bulk) {
            this.bulk = bulk;
            this.batch = null;
            param = bulk.getDeadlineTime();
            //param = bulk.getDeadline();
            //param = bulk.getDataAmount();
        }
        public Request(Batch batch) {
            this.bulk = null;
            this.batch = batch;
            param = batch.getDeadlineTime();
            //param = batch.getMinDeadline();
            //param = batch.getMaxDataAmount();
        }

        public BulkData getBulk() {
            return bulk;
        }

        public Batch getBatch() {
            return batch;
        }

        public double getParam() {
            return param;
        }
    }
    
    private LightPath[] createLightpath(BulkData bulk, boolean chooseRate) {
        int[] nodes;
        int[] links;
        long id;
        //boolean getMaxRate = false;
        LightPath[] lps = new LightPath[1];
        n = graph.getGraphDiameter();
        boolean getMaxRate = false;

        ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(graph, bulk.getSource(), bulk.getDestination(), ksp);

        sortSleepMode(kpaths);
        
        for (int k = 0; k < kpaths.length; k++) {

            nodes = route(kpaths, k);
            if (nodes.length == 0 || nodes == null) {
                return lps;//a null lps[0]
            }

            links = new int[nodes.length - 1];
            for (int j = 0; j < nodes.length - 1; j++) {
                links[j] = cp.getPT().getLink(nodes[j], nodes[j + 1]).getID();
            }

            int rate;
            if (chooseRate) {//se true, taxa minima, se falso, taxa maxima
                rate = (int) Math.ceil((double) bulk.getDataAmount() / (bulk.getDeadlineTime() - currentTime));
            } else {
                rate = ((EONOXC) cp.getPT().getNode(0)).getCapacity() * EONPhysicalTopology.getSlotSize() * (modulation + 1);
            }
            if (rate > ((EONOXC) cp.getPT().getNode(0)).getCapacity() * EONPhysicalTopology.getSlotSize() * (modulation + 1)) {
                return lps;
            }
            int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), modulation);
            
            int[] firstSlot;
            for (int i = 0; i < links.length; i++) {
                firstSlot = ((EONLink) cp.getPT().getLink(links[i])).firstFit(requiredSlots);
                for (int j = 0; j < firstSlot.length; j++) {
                    EONLightPath lp = cp.createCandidateEONLightPath(bulk.getSource(), bulk.getDestination(), links,
                            firstSlot[j], (firstSlot[j] + requiredSlots - 1), modulation);
                    if ((id = cp.getVT().createLightpath(lp)) >= 0) {
                        lps[0] = cp.getVT().getLightpath(id);
                        currentRate = rate;
                        return lps;
                    }
                }
            }
        }
        return lps;
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

    @Override
    public void setModulation(int modulation) {
        this.modulation = modulation;
    }

    @Override
    public void bulkDataArrival(BulkData bulk) {
        System.out.println("bulk: " + bulk.toString() );
        currentTime = cp.getCurrentTime();
        tryAllocationWindow();
        LightPath[] lps = createLightpath(bulk, false);
        if (lps[0] == null) {
            //cp.blockBulkData(bulk.getID());
            window.add(new Request(bulk));
        } else {
            if(!cp.acceptBulkData(bulk.getID(), lps, currentRate)) {
                if (lps != null) {
                    cp.getVT().deallocatedLightpaths(lps);
                }
                //cp.blockBulkData(bulk.getID());
                window.add(new Request(bulk));
            }
        }
    }

    @Override
    public void batchArrival(Batch batch) {
        System.out.println("batch: " + batch.toString() );
        currentTime = cp.getCurrentTime();
        tryAllocationWindow();
        BulkData[][] bulksCombinations = CombinationsBatch.getCombinations(batch, byzantine);
        LightPath[][] lightpaths = new LightPath[batch.getSize()][1];
        int[] rate = new int[batch.getSize()];
        int index;
        boolean flag = false;
        LightPath[] lps;
        
        for (BulkData[] bulks : bulksCombinations) {
            rate = new int[batch.getSize()];
            lightpaths = new LightPath[batch.getSize()][1];
            for (int i = 0; i < bulks.length; i++) {
                flag = false;
                lps = createLightpath(bulks[i], false);
                if (lps[0] == null) {
                    for (LightPath[] lightpath : lightpaths) {
                        if (lightpath[0] != null) {
                            cp.getVT().deallocatedLightpaths(lightpath);
                        }
                    }
                    lightpaths = new LightPath[batch.getSize()][1];
                    rate = new int[batch.getSize()];
                    flag = true;
                    break;
                } else {
                    index = getIndex(bulks[i], batch);
                    lightpaths[index] = lps;
                    rate[index] = currentRate;
                }
            }
            if (!flag) {
                if (!cp.acceptBatch(batch.getID(), lightpaths, rate)) {
                    for (LightPath[] lp : lightpaths) {
                        if (lp != null) {
                            for (LightPath lp1 : lp) {
                                if (lp1 != null) {
                                    cp.getVT().deallocatedLightpath(lp1.getID());
                                }
                            }
                        }
                    }
                } else {
                    return;
                }
            } else {
                for (LightPath[] lp : lightpaths) {
                    if (lp != null) {
                        for (LightPath lp1 : lp) {
                            if (lp1 != null) {
                                cp.getVT().deallocatedLightpath(lp1.getID());
                            }
                        }
                    }
                }
            }
        }
        //cp.blockBatch(batch.getID());
        window.add(new Request(batch));
    }

    @Override
    public void bulkDeparture(long id) {
        currentTime = cp.getCurrentTime();
        tryAllocationWindow();
    }

    @Override
    public void batchDeparture(long id) {
        currentTime = cp.getCurrentTime();
        tryAllocationWindow();
    }

    @Override
    public void flowArrival(Flow flow) {
        cp.blockFlow(flow.getID());
    }

    @Override
    public void flowDeparture(long id) {
    }

    private void tryAllocationWindow() {
        cleanWindow();
        Iterator<Request> it = window.iterator();
        while (it.hasNext()) {
            Request r = it.next();
            if(r.getBulk() != null) {
                LightPath[] lps = createLightpath(r.getBulk(), false);
                if(lps[0] != null) {
                    if (!cp.acceptBulkData(r.getBulk().getID(), lps, currentRate)) {
                        if (lps != null) {
                            cp.getVT().deallocatedLightpaths(lps);
                        }
                    } else {
                        it.remove();
                        return;
                    }
                }
            } else {
                if(batchArrivalWindow(r.getBatch())) {
                    it.remove();
                }
            }
        }
    }
    
    private void cleanWindow() {
        Iterator<Request> it = window.iterator();
        while (it.hasNext()) {
            Request r = it.next();
            if(r.getBulk() != null) {
                if(r.getBulk().getDeadlineTime() < currentTime) {
                    it.remove();
                    cp.blockBulkData(r.getBulk().getID());
                }
            } else {
                if(r.getBatch().getDeadlineTime() < currentTime) {
                    it.remove();
                    cp.blockBatch(r.getBatch().getID());
                }
            }
        }            
    }
    
    public boolean batchArrivalWindow(Batch batch) {
        BulkData[][] bulksCombinations = CombinationsBatch.getCombinations(batch, byzantine);
        LightPath[][] lightpaths = new LightPath[batch.getSize()][1];
        int[] rate = new int[batch.getSize()];
        boolean flag = false;
        LightPath[] lps;

        for (BulkData[] bulks : bulksCombinations) {
            lightpaths = new LightPath[batch.getSize()][1];
            rate = new int[batch.getSize()];
            for (int i = 0; i < bulks.length; i++) {
                flag = false;
                lps = createLightpath(bulks[i], false);
                if (lps[0] == null) {
                    for (LightPath[] lightpath : lightpaths) {
                        if (lightpath[0] != null) {
                            cp.getVT().deallocatedLightpaths(lightpath);
                        }
                    }
                    lightpaths = new LightPath[batch.getSize()][1];
                    rate = new int[batch.getSize()];
                    flag = true;
                    break;
                } else {
                    //index = getIndex(bulks[i], batch);
                    lightpaths[i] = lps;
                    rate[i] = currentRate;
                }
            }
            if (!flag) {
                if (!cp.acceptBatch(batch.getID(), lightpaths, rate)) {
                    for (LightPath[] lp : lightpaths) {
                        if (lp != null) {
                            for (LightPath lp1 : lp) {
                                if (lp1 != null) {
                                    cp.getVT().deallocatedLightpath(lp1.getID());
                                }
                            }
                        }
                    }
                } else {
                    return true;
                }
            } else {
                for (LightPath[] lp : lightpaths) {
                    if (lp != null) {
                        for (LightPath lp1 : lp) {
                            if (lp1 != null) {
                                cp.getVT().deallocatedLightpath(lp1.getID());
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
