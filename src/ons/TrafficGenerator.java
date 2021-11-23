/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import java.util.ArrayList;
import java.util.TreeSet;
import ons.util.Distribution;
import org.w3c.dom.*;

/**
 * Generates the network's traffic based on the information passed through the
 * command line arguments and the XML simulation file.
 *
 * @author onsteam
 */
public class TrafficGenerator {

    private int calls;
    private double load;
    private int maxRate;
    private TrafficInfo[] callsTypesInfo;
    private TrafficPairsInfo[] pairTypesInfo;
    private double meanRate;
    private double meanHoldingTime;
    private int TotalCallsWeight;
    private int TotalPairsWeight;
    private int numberCallsTypes;
    private int numberTrafficPairTypes;
    private int[] datacenters;

    /**
     * Returns details about the network traffic pairs.
     */
    private static class TrafficPairsInfo {

        private int idSource;
        private int idDestination;
        private int weight;

        /**
         * Creates a new TrafficPairsInfo object.
         *
         * @param idSource the node source id in Physical Topology
         * @param idDestination the node destination id in Physical Topology
         * @param weight cost of the network pair
         */
        public TrafficPairsInfo(int idSource, int idDestination, int weight) {
            this.idSource = idSource;
            this.idDestination = idDestination;
            this.weight = weight;
        }
    }

    /**
     * Returns details about the network traffic: holding time, rate, class of
     * service and weight.
     */
    private static class TrafficInfo {

        private double holdingTime;
        private int DCid;
        private int rate;
        private int cos;
        private int weight;
        private int sources;
        private int dataAmount;
        private int delta;
        private double deadline;
        private int trafficType;
        

        /**
         * Creates a new TrafficInfo object.
         *
         * @param holdingTime seconds by which a call will be delayed
         * @param rate transfer rate, measured in Mbps
         * @param cos class of service, to prioritize packets based on
         * application type
         * @param weight cost of the network link
         */
        public TrafficInfo(double holdingTime, int rate, int cos, int weight) {
            this.holdingTime = holdingTime;
            this.rate = rate;
            this.cos = cos;
            this.weight = weight;
        }

        private TrafficInfo(int DCid, int dataAmount, double deadline, int cos, int weight, int trafficType) {
            this.DCid = DCid;
            this.dataAmount = dataAmount;
            this.deadline = deadline;
            this.cos = cos;
            this.weight = weight;
            this.trafficType = trafficType;
        }

        private TrafficInfo(int DCid, int sources, int dataAmount, int delta, double deadline, int cos, int weight, int trafficType) {
            this.DCid = DCid;
            this.sources = sources;
            this.dataAmount = dataAmount;
            this.delta = delta;
            this.deadline = deadline;
            this.cos = cos;
            this.weight = weight;
            this.trafficType = trafficType;
        }
    }

    public int getCalls() {
        return calls;
    }
    
    /**
     * Creates a new TrafficGenerator object. Extracts the traffic information
     * from the XML file and takes the chosen load and seed from the command
     * line arguments.
     *
     * @param xml file that contains all information about the simulation
     * @param forcedLoad range of offered loads for several simulations
     * @param pt the PhysicalTopology object
     */
    protected TrafficGenerator(Element xml, double forcedLoad, PhysicalTopology pt) {
        int rate, cos, weight, id, ids, idd, sources, dataAmount, trafficType, delta, DCid;
        double holdingTime, deadline;

        if(Main.calls == 0) {
            calls = Integer.parseInt(xml.getAttribute("calls"));
        } else {
            calls = Main.calls;
        }
        load = forcedLoad;
        if (load == 0) {
            load = Double.parseDouble(xml.getAttribute("load"));
            Main.load = this.load;
        }
        if (xml.hasAttribute("max-rate")) {
            maxRate = Integer.parseInt(xml.getAttribute("max-rate"));
        } else {
            maxRate = 0;
        }

        if (Simulator.verbose) {
            System.out.println(xml.getAttribute("calls") + " calls, " + xml.getAttribute("load") + " erlangs.");
        }

        // Process calls
        NodeList callslist = xml.getElementsByTagName("calls");
        numberCallsTypes = callslist.getLength();
        if (Simulator.verbose) {
            System.out.println(Integer.toString(numberCallsTypes) + " type(s) of calls:");
        }

        callsTypesInfo = new TrafficInfo[numberCallsTypes];

        TotalCallsWeight = 0;
        meanRate = 0;
        meanHoldingTime = 0;

        for (int i = 0; i < numberCallsTypes; i++) {
            TotalCallsWeight += Integer.parseInt(((Element) callslist.item(i)).getAttribute("weight"));
        }

        for (int i = 0; i < numberCallsTypes; i++) {
            if(((Element) callslist.item(i)).hasAttribute("type")) {
                trafficType = Integer.parseInt(((Element) callslist.item(i)).getAttribute("type"));
            } else {
                trafficType = 0;
            }
            switch(trafficType) {
                case 0:
                    holdingTime = Double.parseDouble(((Element) callslist.item(i)).getAttribute("holding-time"));
                    rate = Integer.parseInt(((Element) callslist.item(i)).getAttribute("rate"));
                    cos = Integer.parseInt(((Element) callslist.item(i)).getAttribute("cos"));
                    weight = Integer.parseInt(((Element) callslist.item(i)).getAttribute("weight"));
                    meanRate += (double) rate * ((double) weight / (double) TotalCallsWeight);
                    meanHoldingTime += holdingTime * ((double) weight / (double) TotalCallsWeight);
                    callsTypesInfo[i] = new TrafficInfo(holdingTime, rate, cos, weight);
                    if (Simulator.verbose) {
                        System.out.println("#################################");
                        System.out.println("Weight: " + Integer.toString(weight) + ".");
                        System.out.println("COS: " + Integer.toString(cos) + ".");
                        System.out.println("Rate: " + Integer.toString(rate) + "Mbps.");
                        System.out.println("Mean holding time: " + Double.toString(holdingTime) + " seconds.");
                    }       
                    break;
                case 1:
                    DCid = Integer.parseInt(((Element) callslist.item(i)).getAttribute("DCid"));
                    dataAmount = Integer.parseInt(((Element) callslist.item(i)).getAttribute("dataAmount"));
                    deadline = Double.parseDouble(((Element) callslist.item(i)).getAttribute("deadline"));
                    cos = Integer.parseInt(((Element) callslist.item(i)).getAttribute("cos"));
                    weight = Integer.parseInt(((Element) callslist.item(i)).getAttribute("weight"));
                    meanHoldingTime += deadline * ((double) weight / (double) TotalCallsWeight);
                    callsTypesInfo[i] = new TrafficInfo(DCid, dataAmount, deadline, cos, weight, trafficType);
                    if (Simulator.verbose) {
                        System.out.println("#################################");
                        System.out.println("Type of traffic: BulkData");
                        System.out.println("Data_Amount: " + Integer.toString(dataAmount) + "GB. ");
                        System.out.println("Deadline: " + Double.toString(deadline) + "seconds. ");
                        System.out.println("Weight: " + Integer.toString(weight) + ".");
                        System.out.println("COS: " + Integer.toString(cos) + ".");
                    }
                    break;
                case 2:
                    DCid = Integer.parseInt(((Element) callslist.item(i)).getAttribute("DCid"));
                    sources = Integer.parseInt(((Element) callslist.item(i)).getAttribute("sources"));
                    dataAmount = Integer.parseInt(((Element) callslist.item(i)).getAttribute("dataAmount"));
                    delta = Integer.parseInt(((Element) callslist.item(i)).getAttribute("delta"));
                    deadline = Double.parseDouble(((Element) callslist.item(i)).getAttribute("deadline"));
                    cos = Integer.parseInt(((Element) callslist.item(i)).getAttribute("cos"));
                    weight = Integer.parseInt(((Element) callslist.item(i)).getAttribute("weight"));
                    meanHoldingTime += deadline * ((double) weight / (double) TotalCallsWeight);
                    callsTypesInfo[i] = new TrafficInfo(DCid, sources, dataAmount, delta, deadline, cos, weight, trafficType);
                    if (Simulator.verbose) {
                        System.out.println("#################################");
                        System.out.println("Type of traffic: Batch");
                        System.out.println("Number_Source_Nodes: " + Integer.toString(sources) + ". ");
                        System.out.println("Data_Amount: " + Integer.toString(dataAmount) + "GB. ");
                        System.out.println("Variation_Size_Data: " + Integer.toString(delta) + "GB. ");
                        System.out.println("Deadline: " + Double.toString(deadline) + "seconds. ");
                        System.out.println("Weight: " + Integer.toString(weight) + ".");
                        System.out.println("COS: " + Integer.toString(cos) + ".");
                    }
                    break;
            }
        }
        if (Simulator.verbose) {
            System.out.println("#################################");
        }
        // Process traffic pairs/group
        TotalPairsWeight = 0;
        numberTrafficPairTypes = 0;
        NodeList pairlist = xml.getElementsByTagName("pair");
        NodeList grouplist = xml.getElementsByTagName("group");
        if (pairlist.getLength() > 0 || grouplist.getLength() > 0) {
            numberTrafficPairTypes = pt.getNumNodes() * (pt.getNumNodes() - 1);
            if (Simulator.verbose) {
                System.out.println("\nAsymmetric traffic enabled");
            }
            pairTypesInfo = new TrafficPairsInfo[numberTrafficPairTypes];
            int index = 0;
            for (int i = 0; i < pt.getNumNodes(); i++) {
                for (int j = 0; j < pt.getNumNodes(); j++) {
                    if (i != j) {
                        pairTypesInfo[index++] = new TrafficPairsInfo(i, j, 1);//default weight
                    }
                }
            }
            
            for (int i = 0; i < grouplist.getLength(); i++) {
                int idGroup1 = Integer.parseInt(((Element) grouplist.item(i)).getAttribute("idGroup1"));
                int idGroup2 = Integer.parseInt(((Element) grouplist.item(i)).getAttribute("idGroup2"));
                weight = Integer.parseInt(((Element) grouplist.item(i)).getAttribute("weight"));
                for (int j = 0; j < numberTrafficPairTypes; j++) {
                    int src = pairTypesInfo[j].idSource;
                    int dst = pairTypesInfo[j].idDestination;
                    if((pt.getNode(src).getGroup() == idGroup1) && (pt.getNode(dst).getGroup() == idGroup2)) {
                        pairTypesInfo[j].weight = weight;
                    }
                }
            }
            
            for (int i = 0; i < pairlist.getLength(); i++) {
                ids = Integer.parseInt(((Element) pairlist.item(i)).getAttribute("ids"));
                idd = Integer.parseInt(((Element) pairlist.item(i)).getAttribute("idd"));
                weight = Integer.parseInt(((Element) pairlist.item(i)).getAttribute("weight"));
                for (int j = 0; j < numberTrafficPairTypes; j++) {
                    if (pairTypesInfo[j].idSource == ids && pairTypesInfo[j].idDestination == idd) {
                        pairTypesInfo[j].weight = weight;
                        break;
                    }
                }
            }
            
            for (int i = 0; i < numberTrafficPairTypes; i++) {
                TotalPairsWeight += pairTypesInfo[i].weight;
                if (Simulator.verbose) {
                    int src = pairTypesInfo[i].idSource;
                    int dst = pairTypesInfo[i].idDestination;
                    weight = pairTypesInfo[i].weight;
                    System.out.println("#################################");
                    System.out.println("Id Source: " + Integer.toString(src) + ".");
                    System.out.println("Id Destination: " + Integer.toString(dst) + ".");
                    System.out.println("Weight: " + Integer.toString(weight) + ".");
                }
            }
            if (Simulator.verbose) {
                System.out.println("Total number of pairs Weight is: " + TotalPairsWeight);
                System.out.println("#################################");
            }
            
        }
    }

    /**
     * Generates the network's traffic.
     *
     * @param events EventScheduler object that will contain the simulation
     * events
     * @param pt the network's Physical Topology
     * @param seed a number in the interval [1,25] that defines up to 25
     * different random simulations
     */
    public void generateTraffic(PhysicalTopology pt, EventScheduler events, int seed) {

        // Compute the weight vector
        int[] callsWeightVector = new int[TotalCallsWeight];
        int[] pairsWeightVector = new int[TotalPairsWeight];
        int aux = 0;
        for (int i = 0; i < numberCallsTypes; i++) {
            for (int j = 0; j < callsTypesInfo[i].weight; j++) {
                callsWeightVector[aux++] = i;
            }
        }
        aux = 0;
        for (int i = 0; i < numberTrafficPairTypes; i++) {
            for (int j = 0; j < pairTypesInfo[i].weight; j++) {
                pairsWeightVector[aux++] = i;
            }
        }

        /* Compute the arrival time
         *
         * load = meanArrivalRate x holdingTime x bw/maxRate
         * 1/meanArrivalRate = (holdingTime x bw/maxRate)/load
         * meanArrivalTime = (holdingTime x bw/maxRate)/load
         */
        double meanArrivalTime;
        if (pt instanceof EONPhysicalTopology) {
            //Because the EON architecture is not possible to obtain a maxRate... So:
            meanArrivalTime = meanHoldingTime / load;
        } else {
            meanArrivalTime = (meanHoldingTime * (meanRate / (double) maxRate)) / load;
        }

        // Generate events
        int callsType, pairType, src, dst;
        double time = 0.0, maxTime = Double.MIN_VALUE, minTime = Double.MAX_VALUE;
        long id = 1;
        int numNodes = pt.getNumNodes();
        Distribution dist1, dist2, dist3, dist4;
        Event event;

        dist1 = new Distribution(1, seed);
        dist2 = new Distribution(2, seed);
        dist3 = new Distribution(3, seed);
        dist4 = new Distribution(4, seed);
        for (int j = 0; j < calls; j++) {
            callsType = callsWeightVector[dist1.nextInt(TotalCallsWeight)];
            if (pairsWeightVector.length > 0) {
                pairType = pairsWeightVector[dist2.nextInt(TotalPairsWeight)];
                src = pairTypesInfo[pairType].idSource;
                dst = pairTypesInfo[pairType].idDestination;
                while (pt.getNode(src).getType() == 2 || pt.getNode(src).getType() == 3 || pt.getNode(dst).getType() == 1 || pt.getNode(dst).getType() == 3) {
                    pairType = pairsWeightVector[dist2.nextInt(TotalPairsWeight)];
                    src = pairTypesInfo[pairType].idSource;
                    dst = pairTypesInfo[pairType].idDestination;
                }
            } else {
                src = dist2.nextInt(numNodes);
                while (pt.getNode(src).getType() == 2 || pt.getNode(src).getType() == 3 ) {
                    src = dist2.nextInt(numNodes);
                }             
                dst = dist2.nextInt(numNodes);
                while (pt.getNode(dst).getType() == 1 || pt.getNode(dst).getType() == 3 || src == dst) {
                    dst = dist2.nextInt(numNodes);
                }
            }
            FlowArrivalEvent flow = new FlowArrivalEvent(new Flow(id, src, dst, callsTypesInfo[callsType].rate, 0, callsTypesInfo[callsType].cos));
            event = flow;
            time += dist3.nextExponential(meanArrivalTime);
            event.setTime(time);
            events.addEvent(event);
            if(minTime > event.getTime()) {
                minTime = event.getTime();
            }
            event = new FlowDepartureEvent(id);
            event.setTime(time + dist4.nextExponential(callsTypesInfo[callsType].holdingTime));
            events.addEvent(event);
            if(maxTime < event.getTime()) {
                maxTime = event.getTime();
            }
            id++;
            flow.getFlow().setDuration(event.getTime() - time);
        }
        event = new OrdinaryEvent("start");
        event.setTime(minTime - 1.0);
        events.addEvent(event);
        event = new OrdinaryEvent("end");
        event.setTime(maxTime + 1.0);
        events.addEvent(event);
    }
    
    void generateTraffic_eonsimBulk(PhysicalTopology pt, EventScheduler events, int seed) {
        // Compute the weight vector
        int[] callsWeightVector = new int[TotalCallsWeight];
        int[] pairsWeightVector = new int[TotalPairsWeight];
        int aux = 0;
        for (int i = 0; i < numberCallsTypes; i++) {
            for (int j = 0; j < callsTypesInfo[i].weight; j++) {
                callsWeightVector[aux++] = i;
            }
        }
        aux = 0;
        for (int i = 0; i < numberTrafficPairTypes; i++) {
            for (int j = 0; j < pairTypesInfo[i].weight; j++) {
                pairsWeightVector[aux++] = i;
            }
        }

        /* Compute the arrival time
         *
         * load = meanArrivalRate x holdingTime x bw/maxRate
         * 1/meanArrivalRate = (holdingTime x bw/maxRate)/load
         * meanArrivalTime = (holdingTime x bw/maxRate)/load
         */
        double meanArrivalTime;
        if (pt instanceof EONPhysicalTopology) {
            //Because the EON architecture is not possible to obtain a maxRate... So:
            //meanArrivalTime = meanHoldingTime / load;
            //meanArrivalTime = 600 / load;
            meanArrivalTime = 1 / load;
            //meanArrivalTime = load;
        } else {
            meanArrivalTime = (meanHoldingTime * (meanRate / (double) maxRate)) / load;
        }
        
        // Generate events
        int pairType, src, dst, sources, delta, DCid, numNodeDCg;
        DatacenterGroup DCgroup;
        double time = 0.0, maxTime = Double.MIN_VALUE, minTime = Double.MAX_VALUE;
        long id = 1;
        int DCGroups = pt.getDatacentersGroup().length;
        int type;
        int numNodes = pt.getNumNodes();
        Distribution dist1, dist2, dist3, dist4, dist5;
        Event event;

        dist1 = new Distribution(1, seed);
        dist2 = new Distribution(2, seed);
        dist3 = new Distribution(3, seed);
        dist4 = new Distribution(4, seed);
        dist5 = new Distribution(3, seed);

        for (int j = 0; j < calls; j++) {
            type = callsWeightVector[dist1.nextInt(TotalCallsWeight)];
            switch (callsTypesInfo[type].trafficType) {
                case 0:
                    if (pairsWeightVector.length > 0) {
                        pairType = pairsWeightVector[dist2.nextInt(TotalPairsWeight)];
                        src = pairTypesInfo[pairType].idSource;
                        dst = pairTypesInfo[pairType].idDestination;
                        while (pt.getNode(src).getType() == 2 || pt.getNode(src).getType() == 3 || pt.getNode(dst).getType() == 1 || pt.getNode(dst).getType() == 3) {
                            pairType = pairsWeightVector[dist2.nextInt(TotalPairsWeight)];
                            src = pairTypesInfo[pairType].idSource;
                            dst = pairTypesInfo[pairType].idDestination;
                        }
                    } else {
                        src = dist2.nextInt(numNodes);
                        while (pt.getNode(src).getType() == 2 || pt.getNode(src).getType() == 3) {
                            src = dist2.nextInt(numNodes);
                        }
                        dst = dist2.nextInt(numNodes);
                        while (pt.getNode(dst).getType() == 1 || pt.getNode(dst).getType() == 3 || src == dst) {
                            dst = dist2.nextInt(numNodes);
                        }
                    }
                    FlowArrivalEvent flow = new FlowArrivalEvent(new Flow(id, src, dst, callsTypesInfo[type].rate, 0, callsTypesInfo[type].cos));
                    event = flow;
                    time += dist3.nextExponential(meanArrivalTime);
                    event.setTime(time);
                    events.addEvent(event);
                    if (minTime > event.getTime()) {
                        minTime = event.getTime();
                    }
                    event = new FlowDepartureEvent(id);
                    event.setTime(time + dist4.nextExponential(callsTypesInfo[type].holdingTime));
                    events.addEvent(event);
                    if (maxTime < event.getTime()) {
                        maxTime = event.getTime();
                    }
                    id++;
                    flow.getFlow().setDuration(event.getTime() - time);
                    break;
                case 1:
                    DCid = callsTypesInfo[type].DCid;
                    DCgroup = pt.getDatacentersGroup()[DCid];
                    numNodeDCg = DCgroup.getNumMembers();
                    src = DCgroup.getMember(dist2.nextInt(numNodeDCg));
                    dst = DCgroup.getMember(dist2.nextInt(numNodeDCg));
                    while (src == dst) {
                        dst = DCgroup.getMember(dist2.nextInt(numNodeDCg));
                    }
                    time += dist3.nextExponential(meanArrivalTime);//Sorteia o time de chegada
                    BulkData bd = new BulkData(id, src, dst, callsTypesInfo[type].dataAmount, callsTypesInfo[type].deadline, callsTypesInfo[type].cos, time, time + callsTypesInfo[type].deadline);
                    event = new BulkDataArrivalEvent(bd);
                    event.setTime(time);
                    events.addEvent(event);
                    if (minTime > event.getTime()) {
                        minTime = event.getTime();
                    }
                    if (maxTime < bd.getDeadlineTime()) {
                        maxTime = bd.getDeadlineTime();
                    }
                    id++;
                    break;
                case 2:
                    sources = callsTypesInfo[type].sources;
                    delta = callsTypesInfo[type].delta;
                    
                    DCid = callsTypesInfo[type].DCid;
                    DCgroup = pt.getDatacentersGroup()[DCid];
                    numNodeDCg = DCgroup.getNumMembers();
                    dst = DCgroup.getMember(dist2.nextInt(numNodeDCg));
                    int[] nodesSources = new int[sources];
                    TreeSet<Integer> srcs = new TreeSet<>();
                    srcs.add(dst);
                    for (int k = 0; k < sources; k++) {
                        src = DCgroup.getMember(dist2.nextInt(numNodeDCg));
                        while (srcs.contains(src)) {
                            src = DCgroup.getMember(dist2.nextInt(numNodeDCg));
                        }
                        srcs.add(src);
                        nodesSources[k] = src;
                    }
                    long[] bulksIds = new long[sources];
                    for (int k = 0; k < bulksIds.length; k++) {
                        bulksIds[k] = id++;
                    }
                    int variation;
                    int[] arrayVariations = new int[sources];
                    for (int v = 0; v < arrayVariations.length; v++) {
                        if (delta == 0) {
                            variation = callsTypesInfo[type].dataAmount;
                        } else {
                            int number = dist5.nextInt(delta) - dist5.nextInt(delta);
                            variation = callsTypesInfo[type].dataAmount + (number);
                        }
                        arrayVariations[v] = variation;
                    }
                    double[] deadline = new double[sources];
                    for (int t = 0; t < deadline.length; t++) {
                        //deadline[t] = dist5.nextInt((int)callsTypesInfo[type].deadline);
                        deadline[t] = callsTypesInfo[type].deadline;
                    }
                    time += dist3.nextExponential(meanArrivalTime);
                    Batch batch = new Batch(id++, bulksIds, nodesSources, dst, arrayVariations, deadline, callsTypesInfo[type].cos, time, deadline);
                    event = new BatchArrivalEvent(batch);
                    event.setTime(time);
                    events.addEvent(event);
                    if (minTime > event.getTime()) {
                        minTime = event.getTime();
                    }
                    if (maxTime < batch.getDeadlineTime()) {
                        maxTime = batch.getDeadlineTime();
                    }
                    break;
            } 
        }
        event = new OrdinaryEvent("start");
        event.setTime(minTime - 1.0);
        events.addEvent(event);
        event = new OrdinaryEvent("end");
        event.setTime(maxTime + 1.0);
        events.addEvent(event);
    }
}
