package ons;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class MyStatistics {
    
    private static MyStatistics singletonObject;
    private static double startTime = 0; 
    private static double endTime = 0;
    private static double simulationTime = 0; 
    private PhysicalTopology pt;
    private VirtualTopology vt;
    private int minNumberArrivals;
    private int numberArrivals;
    private int arrivals;
    private int departures;
    private int accepted;
    private int blocked;
    private int reroute;
    private long requiredBandwidth;
    private long blockedBandwidth;
    private double acumulatedCost;
    private int numNodes;
    private int[][] arrivalsPairs;
    private int[][] blockedPairs;
    private long[][] requiredBandwidthPairs;
    private long[][] blockedBandwidthPairs;
    private int acumulatedK;
    private int acumulatedHops;
    private int acumulatedLPs;
    private int numfails;
    private int flowfails;
    private int lpsfails;
    private float trafficfails;
    private long execTime;

    //add by lucasrc for Number of transmiters
    private long numLightPaths;
    private long numTransponders = 0;
    private int MAX_NumTransponders;
    private long usedTransponders = 0;
    //add by lucasrc for available slots
    private long times = 0;
    private long availableSlots;
    private boolean firstTime = false;
    private int MAX_AvailableSlots;
    //for virtual hops per request
    private long virtualHopsPrimary = 0;
    private long virtualHopsBackup = 0;
    //for physical hops per request
    private long physicalHopsPrimary = 0;
    private long physicalHopsBackup = 0;
    //for modulations requests
    private long[] modulations;
    //for Externalfragmentation
    private double fragmetationRate;
    //for power consumption
    private double totalPowerConsumptionLPs;
    private Map<Long, Double> mapLPTime;
    private double totalPowerConsumption;
    private double oxcOperationPowerConsumption[];
    private double oxcOperationPowerConsumptionTotal;
    private double currentTimeEvent;
    //for grooming
    private long qtGrooming;
    private long[] qtGroomingDiff;
    private long[][] qtGroomingPairs;
    //
    private int verboseCount = 1;
    // Diff
    private int numClasses;
    private int[] arrivalsDiff;
    private int[] blockedDiff;
    private long[] requiredBandwidthDiff;
    private long[] blockedBandwidthDiff;
    private int[][][] arrivalsPairsDiff;
    private int[][][] blockedPairsDiff;
    private int[][][] requiredBandwidthPairsDiff;
    private int[][][] blockedBandwidthPairsDiff;
    private int[] acceptedDiff;
    private int[][] acceptedPairs;
    private int[] rerouteDiff;
    private int[][] reroutePairs;
    /*Protection Metrics*/
    private long numLightPathsPrimary;
    private long[] numLightPathsPrimaryDiff;
    private long[][] numLightPathsPrimaryPairs;
    private long numLightPathsBackup;
    private long[] numLightPathsBackupDiff;
    private long[][] numLightPathsBackupPairs;
    private long qtProtect;
    private long[] qtProtectDiff;
    private long[][] qtProtectPairs;
    private long qtGroomingBackup;
    private long[] qtGroomingBackupDiff;
    private long[][] qtGroomingBackupPairs;
    private double setupTimePrimary;
    private double setupTimeBackup;
    private double setupTimeMax;
    private double[] setupTimeMaxDiff;
    private double[][] setupTimeMaxPairs;
    private double protectSwitchTime;
    private double[] protectSwitchTimeDiff;
    private double[][] protectSwitchTimePairs;
    /*Protection Constants*/
    private double mensageProcessingTime; //(in s1)
    private double configurationTimeOXC; //(in s1)
    private double propagationDelayTime; //(in s1)
    private double switchTime; //(in s1)
    /*OXC sleep mode Metrics*/
    private double[] oxcLastTimeSleep;
    private boolean[] oxcLastState;
    private long[] oxcStateChange;
    private long oxcStateChangeTotal;
    private double oxcSleepTimeTotal;
    private double[] oxcSleepTime;
    private double oxcTransitionTime; //(in s1)
    private double oxcSleepModeExpenditure; //(in percent)
    private double oxcOperationExpenditure;
    private double oxcNodeDegreeExpenditure;
    private double oxcAddDropDegreeExpenditure;
    private double trOverloadExpenditure;
    private double trIdleExpenditure;
    //60 W per fiber pair (2*30W) is considered. 
    //Moreover, 140 additional watts per amplifier location (at each span termination site).
    //So 70 per OLA plus 30 = 100W per OLA.
    private double olaExpenditure; 
    private int spanSize; //(in km)
    
    /*To measure the Total Data Transmitted*/
    private long totalDataTransmitted;
    private long totalDataRequired;
    
    //To eonsimBatch
    private int arrivalsFlow;
    private int departuresFlow;
    private int acceptedFlow;
    private int blockedFlow;
    private int[][] arrivalsPairsFlow;
    private int[][] blockedPairsFlow;
    private int[] arrivalsDiffFlow;
    private int[] blockedDiffFlow;
    private int[][][] arrivalsPairsDiffFlow;
    private int[][][] blockedPairsDiffFlow;
    private int[] acceptedDiffFlow;
    private int[][] acceptedPairsFlow;
    
    
    private int arrivalsBulk;
    private int departuresBulk;
    private int acceptedBulk;
    private int blockedBulk;
    private long requiredDataAmountBulk;
    private long blockedDataAmountBulk;
    private int[][] arrivalsPairsBulk;
    private int[][] blockedPairsBulk;
    private long[][] requiredDataAmountPairsBulk;
    private long[][] blockedDataAmountPairsBulk;
    private int[] arrivalsDiffBulk;
    private int[] blockedDiffBulk;
    private long[] requiredDataAmountDiffBulk;
    private long[] blockedDataAmountDiffBulk;
    private int[][][] arrivalsPairsDiffBulk;
    private int[][][] blockedPairsDiffBulk;
    private int[][][] requiredDataAmountPairsDiffBulk;
    private int[][][] blockedDataAmountPairsDiffBulk;
    private int[] acceptedDiffBulk;
    private int[][] acceptedPairsBulk;
    
    
    private int arrivalsBatch;
    private int departuresBatch;
    private int acceptedBatch;
    private int blockedBatch;
    private long requiredDataAmountBatch;
    private long blockedDataAmountBatch;
    private int[][] arrivalsPairsBatch;
    private int[][] blockedPairsBatch;
    private long[][] blockedDataAmountPairsBatch;
    private int[] arrivalsDiffBatch;
    private int[] blockedDiffBatch;
    private long[] requiredDataAmountDiffBatch;
    private long[] blockedDataAmountDiffBatch;
    private int[][][] arrivalsPairsDiffBatch;
    private int[][][] blockedPairsDiffBatch;
    private int[][][] blockedDataAmountPairsDiffBatch;
    private int[] acceptedDiffBatch;
    private int[][] acceptedPairsBatch;
    
    private long snrCount;
    private long snrSurplusTime;
    private double snrSurplus;
    
    private double[] snrSurplusM;
    private long[] snrSurplusMTime;
    
    private long reproSTTimes;
    private double reproSTTotal;
    private TreeSet<Long> unavailabilityFlows;
    private double unavailabilityTime;
    
    private int intDumbParameter;
    private boolean intDumbParameterIsSet;
    private String stringDumbParameter;
    private boolean stringDumbParameterIsSet;
    private double doubleDumbParameter;
    private boolean doubleDumbParameterIsSet;
    
    private long sumFlowArrivalTime;
    private long flowArrivalTimes;
    
    private long sumBulkArrivalTime;
    private long bulkArrivalTimes;
    
    private long sumBatchArrivalTime;
    private long batchArrivalTimes;
    
    private long blockedFlowAFS;
    private long blockedFlowFrag;
    private long blockedFlowQoTN;
    private long blockedFlowQoTO;
    
    private long blockedBandwidthAFS;
    private long blockedBandwidthFrag;
    private long blockedBandwidthQoTN;
    private long blockedBandwidthQoTO;
    
    /**/
    /**
     * A private constructor that prevents any other class from instantiating.
     */
    private MyStatistics() {

        numberArrivals = 0;

        arrivals = 0;
        departures = 0;
        accepted = 0;
        blocked = 0;
        reroute = 0;
        requiredBandwidth = 0;
        blockedBandwidth = 0;

        numfails = 0;
        flowfails = 0;
        lpsfails = 0;
        trafficfails = 0;

        execTime = 0;
        numLightPaths = 0;
        numLightPathsPrimary = 0;
        numLightPathsBackup = 0;
        mapLPTime = new HashMap<>();
        totalPowerConsumptionLPs = 0;
        totalPowerConsumption = 0;
        oxcOperationPowerConsumptionTotal = 0;
        qtGrooming = 0;
        qtProtect = 0;
        qtGroomingBackup = 0;
        setupTimePrimary = 0;
        setupTimeBackup = 0;
        setupTimeMax = 0;
        protectSwitchTime = 0;
        oxcSleepTimeTotal = 0;
        oxcStateChangeTotal = 0;
        totalDataTransmitted = 0;
        totalDataRequired = 0;
        snrCount = 0;
        snrSurplus = 0;
        snrSurplusTime = 0;
        reproSTTimes = 0;
        reproSTTotal = 0;
        unavailabilityFlows = new TreeSet<>();
        unavailabilityTime = 0;
        sumFlowArrivalTime = 0;
        flowArrivalTimes = 0;
        sumBulkArrivalTime = 0;
        bulkArrivalTimes = 0;
        sumBatchArrivalTime = 0;
        batchArrivalTimes = 0;
        blockedFlowAFS = 0;
        blockedFlowFrag = 0;
        blockedFlowQoTN = 0;
        blockedFlowQoTO = 0;
        blockedBandwidthAFS = 0;
        blockedBandwidthFrag = 0;
        blockedBandwidthQoTN = 0;
        blockedBandwidthQoTO = 0;
    }

    /**
     * Creates a new MyStatistics object, in case it does'n exist yet.
     *
     * @return the MyStatistics singletonObject
     */
    public static synchronized MyStatistics getMyStatisticsObject() {
        if (singletonObject == null) {
            singletonObject = new MyStatistics();
        }
        return singletonObject;
    }

    /**
     * Attributes initializer.
     *
     * @param pt the object Physical Topology
     * @param vt the object Virtual Topology
     * @param numClasses number of classes of service
     * @param minNumberArrivals minimum number of arriving events
     */
    protected void statisticsSetup(PhysicalTopology pt, VirtualTopology vt, int numClasses, int minNumberArrivals) {
        this.pt = pt;
        this.vt = vt;
        this.numNodes = pt.getNumNodes();
        this.arrivalsPairs = new int[numNodes][numNodes];
        this.blockedPairs = new int[numNodes][numNodes];
        this.requiredBandwidthPairs = new long[numNodes][numNodes];
        this.blockedBandwidthPairs = new long[numNodes][numNodes];

        this.minNumberArrivals = minNumberArrivals;

        //Diff
        this.numClasses = numClasses;
        this.arrivalsDiff = new int[numClasses];
        this.blockedDiff = new int[numClasses];
        this.acceptedDiff = new int[numClasses];
        this.rerouteDiff = new int[numClasses];
        this.requiredBandwidthDiff = new long[numClasses];
        this.blockedBandwidthDiff = new long[numClasses];
        this.arrivalsPairsDiff = new int[numNodes][numNodes][numClasses];
        this.blockedPairsDiff = new int[numNodes][numNodes][numClasses];
        this.requiredBandwidthPairsDiff = new int[numNodes][numNodes][numClasses];
        this.blockedBandwidthPairsDiff = new int[numNodes][numNodes][numClasses];
        this.acceptedPairs = new int[numNodes][numNodes];
        this.reroutePairs = new int[numNodes][numNodes];
        //
        this.modulations = new long[EONPhysicalTopology.getMaxModulation() + 1];
        qtGroomingDiff = new long[numClasses];
        qtGroomingPairs = new long[numNodes][numNodes];
        //
        numLightPathsPrimaryDiff = new long[numClasses];
        numLightPathsPrimaryPairs = new long[numNodes][numNodes];
        numLightPathsBackupDiff = new long[numClasses];
        numLightPathsBackupPairs = new long[numNodes][numNodes];
        qtProtectDiff = new long[numClasses];
        qtProtectPairs = new long[numNodes][numNodes];
        qtGroomingBackupDiff = new long[numClasses];
        qtGroomingBackupPairs = new long[numNodes][numNodes];
        setupTimeMaxDiff = new double[numClasses];
        setupTimeMaxPairs = new double[numNodes][numNodes];
        protectSwitchTimeDiff = new double[numClasses];
        protectSwitchTimePairs = new double[numNodes][numNodes];
        //
        oxcOperationPowerConsumption = new double[numNodes];
        oxcLastTimeSleep = new double[numNodes];
        oxcLastState = new boolean[numNodes];
        oxcSleepTime = new double[numNodes];
        oxcStateChange = new long[numNodes];
        
        arrivalsFlow = 0;
        departuresFlow = 0;
        acceptedFlow = 0;
        blockedFlow = 0;
        arrivalsPairsFlow = new int[numNodes][numNodes];
        blockedPairsFlow = new int[numNodes][numNodes];
        arrivalsDiffFlow = new int[numClasses];
        blockedDiffFlow = new int[numClasses];
        arrivalsPairsDiffFlow = new int[numNodes][numNodes][numClasses];
        blockedPairsDiffFlow = new int[numNodes][numNodes][numClasses];
        acceptedDiffFlow = new int[numClasses];
        acceptedPairsFlow = new int[numClasses][numClasses];

        arrivalsBulk = 0;
        departuresBulk = 0;
        acceptedBulk = 0;
        blockedBulk = 0;
        requiredDataAmountBulk = 0;
        blockedDataAmountBulk = 0;
        arrivalsPairsBulk = new int[numNodes][numNodes];
        blockedPairsBulk = new int[numNodes][numNodes];
        requiredDataAmountPairsBulk = new long[numNodes][numNodes];
        blockedDataAmountPairsBulk = new long[numNodes][numNodes];
        arrivalsDiffBulk = new int[numClasses];
        blockedDiffBulk = new int[numClasses];
        requiredDataAmountDiffBulk = new long[numClasses];
        blockedDataAmountDiffBulk = new long[numClasses];
        arrivalsPairsDiffBulk = new int[numNodes][numNodes][numClasses];
        blockedPairsDiffBulk = new int[numNodes][numNodes][numClasses];
        requiredDataAmountPairsDiffBulk = new int[numNodes][numNodes][numClasses];
        blockedDataAmountPairsDiffBulk = new int[numNodes][numNodes][numClasses];
        acceptedDiffBulk = new int[numClasses];
        acceptedPairsBulk = new int[numClasses][numClasses];

        arrivalsBatch = 0;
        departuresBatch = 0;
        acceptedBatch = 0;
        blockedBatch = 0;
        requiredDataAmountBatch = 0;
        blockedDataAmountBatch = 0;
        arrivalsPairsBatch = new int[numNodes][numNodes];
        blockedPairsBatch = new int[numNodes][numNodes];
        blockedDataAmountPairsBatch = new long[numNodes][numNodes];
        arrivalsDiffBatch = new int[numClasses];
        blockedDiffBatch = new int[numClasses];
        requiredDataAmountDiffBatch = new long[numClasses];
        blockedDataAmountDiffBatch = new long[numClasses];
        arrivalsPairsDiffBatch = new int[numNodes][numNodes][numClasses];
        blockedPairsDiffBatch = new int[numNodes][numNodes][numClasses];
        blockedDataAmountPairsDiffBatch = new int[numNodes][numNodes][numClasses];
        acceptedDiffBatch = new int[numClasses];
        acceptedPairsBatch = new int[numClasses][numClasses];

        mensageProcessingTime = pt.mensageProcessingTime;
        configurationTimeOXC = pt.configurationTimeOXC;
        propagationDelayTime = pt.propagationDelayTime;
        switchTime = pt.switchTime;
        oxcTransitionTime = pt.oxcTransitionTime;
        oxcSleepModeExpenditure = pt.oxcSleepModeExpenditure;
        oxcOperationExpenditure = pt.oxcOperationExpenditure;
        oxcNodeDegreeExpenditure = pt.oxcNodeDegreeExpenditure;
        oxcAddDropDegreeExpenditure = pt.oxcAddDropDegreeExpenditure;
        trOverloadExpenditure = pt.trOverloadExpenditure;
        trIdleExpenditure = pt.trIdleExpenditure;
        olaExpenditure = pt.olaExpenditure;
        spanSize = pt.spanSize;
        
        snrSurplusM = new double[EONPhysicalTopology.getMaxModulation() + 1];
        snrSurplusMTime = new long[EONPhysicalTopology.getMaxModulation() + 1];
    }

    /**
     * Adds an accepted flow to the statistics.
     *
     * @param flow the accepted Flow object
     * @param lightpaths list of lightpaths in the flow
     * @param usedTransponders the number of transponders used in this RA
     * solution
     */
    protected void acceptFlow(Flow flow, LightPath[] lightpaths, boolean[] usedTransponders) {
        double STPrimary = 0;
        if (this.numberArrivals > this.minNumberArrivals) {
            this.accepted++;
            this.acceptedDiff[flow.getCOS()]++;
            this.acceptedPairs[flow.getSource()][flow.getDestination()]++;
            totalDataTransmitted += flow.getRate() * flow.getDuration();
            this.virtualHopsPrimary += (long) lightpaths.length;
            int count = 0;
            for (LightPath lps : lightpaths) {
                count += lps.getHops();
            }
            this.physicalHopsPrimary += (long) count;
            int usedTr = 0, grooming = 0;
            for (int i = 0; i < usedTransponders.length; i++) {
                if(usedTransponders[i]) usedTr++;
                else grooming++;
            }
            this.usedTransponders += (long) usedTr;
            numLightPathsPrimary += lightpaths.length;
            numLightPathsPrimaryDiff[flow.getCOS()] += lightpaths.length;
            numLightPathsPrimaryPairs[flow.getSource()][flow.getDestination()] += lightpaths.length;
            qtGrooming += grooming;
            qtGroomingDiff[flow.getCOS()] += grooming;
            qtGroomingPairs[flow.getSource()][flow.getDestination()] += grooming;
            for (int i = 0; i < usedTransponders.length; i++) {
                if(usedTransponders[i]) {
                    STPrimary = calculateST(lightpaths[i]);
                    setupTimePrimary += STPrimary;
                }
            }
        }
    }
    
    protected void rerouteFlow(Flow flow, LightPath[] lightpaths, Path oldPath, boolean[] usedTransponders, boolean[] usedTranspondersOld) {
        double STPrimary = 0;
        if (this.numberArrivals > this.minNumberArrivals) {
            double st1, st2;
            st1 = calculateST(lightpaths);
            st2 = calculateST(oldPath.getLightpaths());
            unavailabilityTime += (st1 + st2);
            unavailabilityFlows.add(flow.getID());
            this.reroute++;
            this.rerouteDiff[flow.getCOS()]++;
            this.reroutePairs[flow.getSource()][flow.getDestination()]++;
            
            this.virtualHopsPrimary -= (long) oldPath.getLightpaths().length;
            this.virtualHopsPrimary += (long) lightpaths.length;
            int count = 0;
            for (LightPath lps : oldPath.getLightpaths()) {
                count += lps.getHops();
            }
            this.physicalHopsPrimary -= (long) count;
            count = 0;
            for (LightPath lps : lightpaths) {
                count += lps.getHops();
            }
            this.physicalHopsPrimary += (long) count;
            int usedTr = 0, grooming = 0;
            for (int i = 0; i < usedTranspondersOld.length; i++) {
                if(usedTranspondersOld[i]) usedTr++;
                else grooming++;
            }
            this.usedTransponders -= (long) usedTr;
            qtGrooming -= grooming;
            qtGroomingDiff[flow.getCOS()] -= grooming;
            qtGroomingPairs[flow.getSource()][flow.getDestination()] -= grooming;
            usedTr = 0;
            grooming = 0;
            for (int i = 0; i < usedTransponders.length; i++) {
                if(usedTransponders[i]) usedTr++;
                else grooming++;
            }
            this.usedTransponders += (long) usedTr;
            qtGrooming += grooming;
            qtGroomingDiff[flow.getCOS()] += grooming;
            qtGroomingPairs[flow.getSource()][flow.getDestination()] += grooming;
            
            numLightPathsPrimary -= oldPath.getLightpaths().length;
            numLightPathsPrimary += lightpaths.length;
            numLightPathsPrimaryDiff[flow.getCOS()] -= oldPath.getLightpaths().length;
            numLightPathsPrimaryDiff[flow.getCOS()] += lightpaths.length;
            numLightPathsPrimaryPairs[flow.getSource()][flow.getDestination()] -= oldPath.getLightpaths().length;
            numLightPathsPrimaryPairs[flow.getSource()][flow.getDestination()] += lightpaths.length;
            for (int i = 0; i < usedTranspondersOld.length; i++) {
                if(usedTranspondersOld[i]) {
                    STPrimary = calculateST(oldPath.getLightpaths()[i]);
                    setupTimePrimary -= STPrimary;
                }
            }
            for (int i = 0; i < usedTransponders.length; i++) {
                if(usedTransponders[i]) {
                    STPrimary = calculateST(lightpaths[i]);
                    setupTimePrimary += STPrimary;
                }
            }
        }
    }

    /**
     * Adds an accepted flow to the statistics (for Multipath).
     *
     * @param flow the accepted Flow object
     * @param paths list of paths flow used
     * @param usedTransponders the number of transponders used in this RA
     * solution
     */
    void acceptFlow(Flow flow, Path[] paths, boolean[][] usedTransponders) {
        double STPrimary = 0;
        if (this.numberArrivals > this.minNumberArrivals) {
            this.accepted++;
            this.acceptedDiff[flow.getCOS()]++;
            this.acceptedPairs[flow.getSource()][flow.getDestination()]++;
            totalDataTransmitted += flow.getRate() * flow.getDuration();
            for (Path path : paths) {
                this.virtualHopsPrimary += (long) path.getLightpaths().length;
                int count = 0;
                for (LightPath lps : path.getLightpaths()) {
                    count += lps.getHops();
                }
                this.physicalHopsPrimary += (long) count;
            }
            int usedTr = 0, grooming = 0;
            for (int i = 0; i < usedTransponders.length; i++) {
                for (int j = 0; j < usedTransponders[i].length; j++) {
                    if (usedTransponders[i][j]) {
                        usedTr++;
                    } else {
                        grooming++;
                    }
                }
            }
            this.usedTransponders += (long) usedTr;
            int lps = 0;
            for (Path path : paths) {
                lps += path.getLightpaths().length;
            }
            numLightPathsPrimary += lps;
            numLightPathsPrimaryDiff[flow.getCOS()] += lps;
            numLightPathsPrimaryPairs[flow.getSource()][flow.getDestination()] += lps;
            qtGrooming += grooming;
            qtGroomingDiff[flow.getCOS()] += grooming;
            qtGroomingPairs[flow.getSource()][flow.getDestination()] += grooming;
            for (int i = 0; i < usedTransponders.length; i++) {
                for (int j = 0; j < usedTransponders[i].length; j++) {
                    if (usedTransponders[i][j]) {
                        STPrimary = calculateST(paths[i].getLightpaths()[j]);
                        setupTimePrimary += STPrimary;
                        setupTimeMax += STPrimary;
                        setupTimeMaxDiff[flow.getCOS()] += STPrimary;
                        setupTimeMaxPairs[flow.getSource()][flow.getDestination()] += STPrimary;
                    }
                }
            }
        }
    }

    /**
     * Adds an accepted flow to the statistics (for Multipath).
     *
     * @param flow the accepted Flow object
     * @param paths list of paths flow used
     * @param usedTransponders the number of transponders used in this RA
     * solution
     */
    void acceptFlow(Flow flow, Path[] primaryPaths, Path[] backupPaths, boolean[][] usedTranspondersPrimary, boolean[][] usedTranspondersBackup) {
        double propagationDelay, STPrimary = 0, STBackup = 0, PST = 0;
        if (this.numberArrivals > this.minNumberArrivals) {
            this.accepted++;
            this.acceptedDiff[flow.getCOS()]++;
            this.acceptedPairs[flow.getSource()][flow.getDestination()]++;
            totalDataTransmitted += flow.getRate() * flow.getDuration();
            qtProtect++;
            qtProtectDiff[flow.getCOS()]++;
            qtProtectPairs[flow.getSource()][flow.getDestination()]++;
            //Operations related to the primary
            for (Path path : primaryPaths) {
                this.virtualHopsPrimary += (long) path.getLightpaths().length;
                int count = 0;
                for (LightPath lps : path.getLightpaths()) {
                    count += lps.getHops();
                }
                this.physicalHopsPrimary += (long) count;
            }
            
            for (Path path : backupPaths) {
                this.virtualHopsBackup += (long) path.getLightpaths().length;
                int count = 0;
                for (LightPath lps : path.getLightpaths()) {
                    count += lps.getHops();
                }
                this.physicalHopsBackup += (long) count;
            }
            
            int usedTr = 0, grooming = 0;
            for (int i = 0; i < usedTranspondersPrimary.length; i++) {
                for (int j = 0; j < usedTranspondersPrimary[i].length; j++) {
                    if (usedTranspondersPrimary[i][j]) {
                        usedTr++;
                    } else {
                        grooming++;
                    }
                }
            }
            this.usedTransponders += (long) usedTr;
            int lps = 0;
            for (Path path : primaryPaths) {
                lps += path.getLightpaths().length;
            }
            numLightPathsPrimary += lps;
            numLightPathsPrimaryDiff[flow.getCOS()] += lps;
            numLightPathsPrimaryPairs[flow.getSource()][flow.getDestination()] += lps;
            qtGrooming += grooming;
            qtGroomingDiff[flow.getCOS()] += grooming;
            qtGroomingPairs[flow.getSource()][flow.getDestination()] += grooming;
            for (int i = 0; i < usedTranspondersPrimary.length; i++) {
                for (int j = 0; j < usedTranspondersPrimary[i].length; j++) {
                    if (usedTranspondersPrimary[i][j]) {
                        STPrimary = calculateST(primaryPaths[i].getLightpaths()[j]);
                        if(primaryPaths[i].getLightpaths()[j].getTypeProtection().equals("SPP") || 
                           primaryPaths[i].getLightpaths()[j].getTypeProtection().equals("SLP")) {
                            STBackup += STPrimary;
                        }
                    }
                }
            }
            setupTimePrimary += STPrimary;
            //Operations related to the backup
            lps = 0;
            for (Path path : backupPaths) {
                lps += path.getLightpaths().length;
            }
            numLightPathsBackup += lps;
            numLightPathsBackupDiff[flow.getCOS()] += lps;
            numLightPathsBackupPairs[flow.getSource()][flow.getDestination()] += lps;
            usedTr = 0; 
            grooming = 0;
            for (int i = 0; i < usedTranspondersBackup.length; i++) {
                for (int j = 0; j < usedTranspondersBackup[i].length; j++) {
                    if (usedTranspondersBackup[i][j]) {
                        usedTr++;
                    } else {
                        grooming++;
                    }
                }
            }
            this.usedTransponders += (long) usedTr;
            qtGroomingBackup += grooming;
            qtGroomingBackupDiff[flow.getCOS()] += grooming;
            qtGroomingBackupPairs[flow.getSource()][flow.getDestination()] += grooming;
            for (int i = 0; i < usedTranspondersBackup.length; i++) {
                for (int j = 0; j < usedTranspondersBackup[i].length; j++) {
                    if (usedTranspondersBackup[i][j]) {
                        switch(backupPaths[i].getLightpaths()[j].getTypeProtection()) {
                            case "DPP":
                                propagationDelay = Math.ceil(lengthPath(backupPaths[i].getLightpaths()[j]) / (double) spanSize) * propagationDelayTime;
                                STBackup += (propagationDelay + (backupPaths[i].getLightpaths()[j].getHops() + 1) * (configurationTimeOXC + mensageProcessingTime));
                                break;
                            case "DLP":
                                propagationDelay = Math.ceil(lengthPath(backupPaths[i].getLightpaths()[j]) / (double) spanSize) * propagationDelayTime;
                                STBackup += (propagationDelay + (backupPaths[i].getLightpaths()[j].getHops() + 1) * (configurationTimeOXC + mensageProcessingTime));
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            
            setupTimeBackup += STBackup;
            double maxPST = 0;
            double propagationDelayPrimary = 0;
            double propagationDelayBackup = 0;
            int backupHops = 0;
            LightPath lpBackXLP;
            double maxPSTXLP = 0;
            for (Path primaryPath : primaryPaths) {
                for (LightPath lightpath : primaryPath.getLightpaths()) {
                    switch (lightpath.getTypeProtection()) {
                        case "DPP":
                            propagationDelayPrimary = Math.ceil(lengthPath(lightpath) / (double) spanSize) * propagationDelayTime;
                            propagationDelayBackup = Math.ceil(lengthPathBackups(lightpath) / (double) spanSize) * propagationDelayTime;
                            backupHops = getBackupPhysicalHops(lightpath);
                            PST = switchTime + (lightpath.getHops()-1) * propagationDelayPrimary 
                                    + (lightpath.getHops() + 1) * mensageProcessingTime 
                                    + 2.0 * (backupHops) * propagationDelayBackup 
                                    + 2.0 * (backupHops + 1) * mensageProcessingTime;
                            break;
                        case "DLP":
                            maxPSTXLP = 0;
                            for (Long lpBackup : lightpath.getLpBackup()) {
                                lpBackXLP = vt.getLightpath(lpBackup);
                                propagationDelayBackup = Math.ceil(lengthPath(lpBackXLP) / (double) spanSize) * propagationDelayTime;
                                backupHops = lpBackXLP.getHops();
                                PST = switchTime + (2.0 * propagationDelayBackup) + ((double) (2 * (backupHops + 1)) * mensageProcessingTime);
                                if(maxPSTXLP < PST) maxPSTXLP = PST;
                            }
                            PST = maxPSTXLP;
                            break;
                        case "SPP":
                            propagationDelayPrimary = Math.ceil(lengthPath(lightpath) / (double) spanSize) * propagationDelayTime;
                            propagationDelayBackup = Math.ceil(lengthPathBackups(lightpath) / (double) spanSize) * propagationDelayTime;
                            backupHops = getBackupPhysicalHops(lightpath);
                            PST = switchTime + lightpath.getHops() * propagationDelayPrimary 
                                    + (lightpath.getHops() + 1) * mensageProcessingTime
                                    + (backupHops + 1) * configurationTimeOXC
                                    + 2.0 * (backupHops) * propagationDelayBackup 
                                    + 2.0 * (backupHops + 1) * mensageProcessingTime;
                            break;
                        case "SLP":
                            maxPSTXLP = 0;
                            for (Long lpBackup : lightpath.getLpBackup()) {
                                lpBackXLP = vt.getLightpath(lpBackup);
                                propagationDelayBackup = Math.ceil(lengthPathBackups(lpBackXLP) / (double) spanSize) * propagationDelayTime;
                                backupHops = lpBackXLP.getHops();
                                PST = switchTime + (backupHops + 1) * configurationTimeOXC
                                        + 2.0 * (backupHops + 1) * mensageProcessingTime
                                        + 2.0 * backupHops * propagationDelayBackup;
                                if(maxPSTXLP < PST) maxPSTXLP = PST;
                            }
                            PST = maxPSTXLP;
                            break;
                        default:
                            PST = 0;
                            break;
                    }
                    if(maxPST < PST) {
                        maxPST = PST;
                    }
                }
            }
            protectSwitchTime += maxPST;
            protectSwitchTimeDiff[flow.getCOS()] += maxPST;
            protectSwitchTimePairs[flow.getSource()][flow.getDestination()] += maxPST;
            //STMax
            if (STPrimary > STBackup) {
                setupTimeMax += STPrimary;
                setupTimeMaxDiff[flow.getCOS()] += STPrimary;
                setupTimeMaxPairs[flow.getSource()][flow.getDestination()] += STPrimary;
            } else {
                setupTimeMax += STBackup;
                setupTimeMaxDiff[flow.getCOS()] += STBackup;
                setupTimeMaxPairs[flow.getSource()][flow.getDestination()] += STBackup;
            }
        }
    }

    void addStaticLightpath(LightPath[] lightpaths, boolean[] usedTransponders) {
        double STPrimary = 0;
        if (this.numberArrivals > this.minNumberArrivals) {
            this.virtualHopsPrimary += (long) lightpaths.length;
            int count = 0;
            for (LightPath lps : lightpaths) {
                count += lps.getHops();
            }
            this.physicalHopsPrimary += (long) count;
            int usedTr = 0, grooming = 0;
            for (int i = 0; i < usedTransponders.length; i++) {
                if(usedTransponders[i]) usedTr++;
                else grooming++;
            }
            this.usedTransponders += (long) usedTr;
            numLightPathsPrimary += lightpaths.length;
            numLightPathsPrimaryPairs[lightpaths[0].getSource()][lightpaths[lightpaths.length-1].getDestination()] += lightpaths.length;
            qtGrooming += grooming;
            qtGroomingPairs[lightpaths[0].getSource()][lightpaths[lightpaths.length-1].getDestination()] += grooming;
            for (int i = 0; i < usedTransponders.length; i++) {
                if(usedTransponders[i]) {
                    STPrimary = calculateST(lightpaths[i]);
                    setupTimePrimary += STPrimary;
                    setupTimeMax += STPrimary;
                    setupTimeMaxPairs[lightpaths[0].getSource()][lightpaths[lightpaths.length-1].getDestination()] += STPrimary;
                }
            }
        }
    }

    void addStaticLightpath(Path[] primaryPaths, Path[] backupPaths, boolean[][] usedTranspondersPrimary, boolean[][] usedTranspondersBackup) {
        double propagationDelay, STPrimary = 0, STBackup = 0, PST = 0;
        if (this.numberArrivals > this.minNumberArrivals) {
            //Operations related to the primary
            for (Path path : primaryPaths) {
                this.virtualHopsPrimary += (long) path.getLightpaths().length;
                int count = 0;
                for (LightPath lps : path.getLightpaths()) {
                    count += lps.getHops();
                }
                this.physicalHopsPrimary += (long) count;
            }
            for (Path path : backupPaths) {
                this.virtualHopsBackup += (long) path.getLightpaths().length;
                int count = 0;
                for (LightPath lps : path.getLightpaths()) {
                    count += lps.getHops();
                }
                this.physicalHopsBackup += (long) count;
            }
            int usedTr = 0, grooming = 0;
            for (int i = 0; i < usedTranspondersPrimary.length; i++) {
                for (int j = 0; j < usedTranspondersPrimary[i].length; j++) {
                    if (usedTranspondersPrimary[i][j]) {
                        usedTr++;
                    } else {
                        grooming++;
                    }
                }
            }
            this.usedTransponders += (long) usedTr;
            int lps = 0;
            for (Path path : primaryPaths) {
                lps += path.getLightpaths().length;
            }
            numLightPathsPrimary += lps;
            numLightPathsPrimaryPairs[primaryPaths[0].getSource()][primaryPaths[primaryPaths.length-1].getDestination()] += lps;
            qtGrooming += grooming;
            qtGroomingPairs[primaryPaths[0].getSource()][primaryPaths[primaryPaths.length-1].getDestination()] += grooming;
            for (int i = 0; i < usedTranspondersPrimary.length; i++) {
                for (int j = 0; j < usedTranspondersPrimary[i].length; j++) {
                    if (usedTranspondersPrimary[i][j]) {
                        STPrimary = calculateST(primaryPaths[i].getLightpaths()[j]);
                        if(primaryPaths[i].getLightpaths()[j].getTypeProtection().equals("SPP") || 
                           primaryPaths[i].getLightpaths()[j].getTypeProtection().equals("SLP")) {
                            STBackup += STPrimary;
                        }
                    }
                }
            }
            setupTimePrimary += STPrimary;
            //Operations related to the backup
            lps = 0;
            for (Path path : backupPaths) {
                lps += path.getLightpaths().length;
            }
            numLightPathsBackup += lps;
            numLightPathsBackupPairs[primaryPaths[0].getSource()][primaryPaths[primaryPaths.length-1].getDestination()] += lps;
            usedTr = 0;
            grooming = 0;
            for (int i = 0; i < usedTranspondersBackup.length; i++) {
                for (int j = 0; j < usedTranspondersBackup[i].length; j++) {
                    if (usedTranspondersBackup[i][j]) {
                        usedTr++;
                    } else {
                        grooming++;
                    }
                }
            }
            this.usedTransponders += (long) usedTr;
            qtGroomingBackup += grooming;
            qtGroomingBackupPairs[primaryPaths[0].getSource()][primaryPaths[primaryPaths.length-1].getDestination()] += grooming;
            for (int i = 0; i < usedTranspondersBackup.length; i++) {
                for (int j = 0; j < usedTranspondersBackup[i].length; j++) {
                    if (usedTranspondersBackup[i][j]) {
                        switch (backupPaths[i].getLightpaths()[j].getTypeProtection()) {
                            case "DPP":
                                propagationDelay = Math.ceil(lengthPath(backupPaths[i].getLightpaths()[j]) / (double) spanSize) * propagationDelayTime;
                                STBackup += (propagationDelay + (backupPaths[i].getLightpaths()[j].getHops() + 1) * (configurationTimeOXC + mensageProcessingTime));
                                break;
                            case "DLP":
                                propagationDelay = Math.ceil(lengthPath(backupPaths[i].getLightpaths()[j]) / (double) spanSize) * propagationDelayTime;
                                STBackup += (propagationDelay + (backupPaths[i].getLightpaths()[j].getHops() + 1) * (configurationTimeOXC + mensageProcessingTime));
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            setupTimeBackup += STBackup;
            double maxPST = 0;
            double propagationDelayPrimary = 0;
            double propagationDelayBackup = 0;
            int backupHops = 0;
            LightPath lpBackXLP;
            double maxPSTXLP = 0;
            for (Path primaryPath : primaryPaths) {
                for (LightPath lightpath : primaryPath.getLightpaths()) {
                    switch (lightpath.getTypeProtection()) {
                        case "DPP":
                            propagationDelayPrimary = Math.ceil(lengthPath(lightpath) / (double) spanSize) * propagationDelayTime;
                            propagationDelayBackup = Math.ceil(lengthPathBackups(lightpath) / (double) spanSize) * propagationDelayTime;
                            backupHops = getBackupPhysicalHops(lightpath);
                            PST = switchTime + (lightpath.getHops() - 1) * propagationDelayPrimary
                                    + (lightpath.getHops() + 1) * mensageProcessingTime
                                    + 2.0 * (backupHops) * propagationDelayBackup
                                    + 2.0 * (backupHops + 1) * mensageProcessingTime;
                            break;
                        case "DLP":
                            maxPSTXLP = 0;
                            for (Long lpBackup : lightpath.getLpBackup()) {
                                lpBackXLP = vt.getLightpath(lpBackup);
                                propagationDelayBackup = Math.ceil(lengthPath(lpBackXLP) / (double) spanSize) * propagationDelayTime;
                                backupHops = lpBackXLP.getHops();
                                PST = switchTime + (2.0 * propagationDelayBackup) + ((double) (2 * (backupHops + 1)) * mensageProcessingTime);
                                if (maxPSTXLP < PST) {
                                    maxPSTXLP = PST;
                                }
                            }
                            PST = maxPSTXLP;
                            break;
                        case "SPP":
                            propagationDelayPrimary = Math.ceil(lengthPath(lightpath) / (double) spanSize) * propagationDelayTime;
                            propagationDelayBackup = Math.ceil(lengthPathBackups(lightpath) / (double) spanSize) * propagationDelayTime;
                            backupHops = getBackupPhysicalHops(lightpath);
                            PST = switchTime + lightpath.getHops() * propagationDelayPrimary
                                    + (lightpath.getHops() + 1) * mensageProcessingTime
                                    + (backupHops + 1) * configurationTimeOXC
                                    + 2.0 * (backupHops) * propagationDelayBackup
                                    + 2.0 * (backupHops + 1) * mensageProcessingTime;
                            break;
                        case "SLP":
                            maxPSTXLP = 0;
                            for (Long lpBackup : lightpath.getLpBackup()) {
                                lpBackXLP = vt.getLightpath(lpBackup);
                                propagationDelayBackup = Math.ceil(lengthPathBackups(lpBackXLP) / (double) spanSize) * propagationDelayTime;
                                backupHops = lpBackXLP.getHops();
                                PST = switchTime + (backupHops + 1) * configurationTimeOXC
                                        + 2.0 * (backupHops + 1) * mensageProcessingTime
                                        + 2.0 * backupHops * propagationDelayBackup;
                                if (maxPSTXLP < PST) {
                                    maxPSTXLP = PST;
                                }
                            }
                            PST = maxPSTXLP;
                            break;
                        default:
                            PST = 0;
                            break;
                    }
                    if (maxPST < PST) {
                        maxPST = PST;
                    }
                }
            }
            protectSwitchTime += maxPST;
            protectSwitchTimePairs[primaryPaths[0].getSource()][primaryPaths[primaryPaths.length-1].getDestination()] += maxPST;
            //STMax
            if (STPrimary > STBackup) {
                setupTimeMax += STPrimary;
                setupTimeMaxPairs[primaryPaths[0].getSource()][primaryPaths[primaryPaths.length-1].getDestination()] += STPrimary;
            } else {
                setupTimeMax += STBackup;
                setupTimeMaxPairs[primaryPaths[0].getSource()][primaryPaths[primaryPaths.length-1].getDestination()] += STBackup;
            }
        }
    }
    
    /**
     * Adds a blocked flow to the statistics.
     *
     * @param flow the blocked Flow object
     */
    protected void blockFlow(Flow flow) {
        if (this.numberArrivals > this.minNumberArrivals) {
            int cos = flow.getCOS();
            this.blocked++;
            this.blockedFlow++;
            this.blockedDiff[cos]++;
            this.blockedDiffFlow[cos]++;
            this.blockedBandwidth += flow.getRate();
            this.blockedBandwidthDiff[cos] += flow.getRate();
            this.blockedPairs[flow.getSource()][flow.getDestination()]++;
            this.blockedPairsFlow[flow.getSource()][flow.getDestination()]++;
            this.blockedPairsDiff[flow.getSource()][flow.getDestination()][cos]++;
            this.blockedPairsDiffFlow[flow.getSource()][flow.getDestination()][cos]++;
            this.blockedBandwidthPairs[flow.getSource()][flow.getDestination()] += flow.getRate();
            this.blockedBandwidthPairsDiff[flow.getSource()][flow.getDestination()][cos] += flow.getRate();
        }
    }
    
    protected void addOrdinaryEvent(Event event) {
        if (!(event instanceof OrdinaryEvent)) {
            throw (new IllegalArgumentException());
        } else {
            switch (((OrdinaryEvent) event).getDescription()) {
                case "start":
                    setupTimeMaxDiff = new double[numClasses];
                    setupTimeMaxPairs = new double[numNodes][numNodes];
                    //protectSwitchTimeDiff = new double[numClasses];
                    //protectSwitchTimePairs = new double[numNodes][numNodes];
                    setupTimePrimary = 0;
                    setupTimeBackup = 0;
                    setupTimeMax = 0;
                    //protectSwitchTime = 0;
                    startTime = event.getTime();
                    for (int i = 0; i < oxcLastTimeSleep.length; i++) {
                        oxcLastTimeSleep[i] = (startTime + 1.0); //because is the time of first event (next event)
                        oxcLastState[i] = true;
                    }
                    break;
                case "end":
                    endTime = event.getTime();
                    simulationTime = (endTime - 1.0) - (startTime + 1.0);
                    currentTimeEvent = (endTime - 1.0);
                    //We have to disregard the first and last change of state to account 
                    //for the number of times change has occurred.
                    for (int i = 0; i < numNodes; i++) {
                        oxcStateChange[i] -= 2;
                    }
                    oxcStateChangeTotal -= 2 * numNodes;
                    break;
                default:
                    break;
            }
        }
    }

    protected void addEvent(Event event, int availableSlots, int availableTransponders, double frag) {
        if (!firstTime) {
            MAX_AvailableSlots = availableSlots;
            MAX_NumTransponders = availableTransponders;
            firstTime = true;
        }
        addEvent(event, availableTransponders);
        if (this.numberArrivals > this.minNumberArrivals) {
            this.fragmetationRate += frag;
            this.availableSlots += (long) availableSlots;
        }
    }

    /**
     * Adds an event to the statistics.
     *
     * @param event the Event object to be added
     * @param availableTransponders the atual available transponders in physical
     * topology
     */
    protected void addEvent(Event event, int availableTransponders) {
        try {
            if (!firstTime) {
                MAX_NumTransponders = availableTransponders;
                firstTime = true;
            }
            times++;
            currentTimeEvent = event.getTime();
            if (event instanceof FlowArrivalEvent) {
                this.numberArrivals++;
                if (this.numberArrivals > this.minNumberArrivals) {
                    this.numTransponders += (long) availableTransponders;
                    int cos = ((FlowArrivalEvent) event).getFlow().getCOS();
                    this.totalDataRequired += ((FlowArrivalEvent) event).getFlow().getRate() * ((FlowArrivalEvent) event).getFlow().getDuration();
                    this.arrivals++;
                    this.arrivalsDiff[cos]++;
                    this.requiredBandwidth += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.requiredBandwidthDiff[cos] += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.arrivalsPairs[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()]++;
                    this.arrivalsPairsDiff[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()][cos]++;
                    this.requiredBandwidthPairs[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()] += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.requiredBandwidthPairsDiff[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()][cos] += ((FlowArrivalEvent) event).getFlow().getRate();
                    this.arrivalsFlow++;
                    this.arrivalsDiffFlow[cos]++;                    
                    this.arrivalsPairsFlow[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()]++;
                    this.arrivalsPairsDiffFlow[((FlowArrivalEvent) event).getFlow().getSource()][((FlowArrivalEvent) event).getFlow().getDestination()][cos]++;
                }
                //
                if (Simulator.verbose && (numberArrivals == 10000 * verboseCount)) {
                    System.out.println(Integer.toString(numberArrivals));
                    verboseCount++;
                }
            } else if (event instanceof FlowDepartureEvent) {
                if (this.numberArrivals > this.minNumberArrivals) {
                    this.departures++;
                    this.departuresFlow++;
                }
            } else if (event instanceof BulkDataArrivalEvent) {
                this.numberArrivals++;
                if (this.numberArrivals > this.minNumberArrivals) {
                    this.numTransponders += (long) availableTransponders;
                    int cos = ((BulkDataArrivalEvent) event).getBulkData().getCOS();
                    this.totalDataRequired += ((BulkDataArrivalEvent) event).getBulkData().getDataAmount();
                    this.arrivals++;
                    this.arrivalsDiff[cos]++;
                    this.arrivalsPairs[((BulkDataArrivalEvent) event).getBulkData().getSource()][((BulkDataArrivalEvent) event).getBulkData().getDestination()]++;
                    this.arrivalsPairsDiff[((BulkDataArrivalEvent) event).getBulkData().getSource()][((BulkDataArrivalEvent) event).getBulkData().getDestination()][cos]++;

                    this.arrivalsBulk++;
                    this.arrivalsDiffBulk[cos]++;
                    this.requiredDataAmountBulk += ((BulkDataArrivalEvent) event).getBulkData().getDataAmount();
                    this.requiredDataAmountDiffBulk[cos] += ((BulkDataArrivalEvent) event).getBulkData().getDataAmount();
                    this.arrivalsPairsBulk[((BulkDataArrivalEvent) event).getBulkData().getSource()][((BulkDataArrivalEvent) event).getBulkData().getDestination()]++;
                    this.arrivalsPairsDiffBulk[((BulkDataArrivalEvent) event).getBulkData().getSource()][((BulkDataArrivalEvent) event).getBulkData().getDestination()][cos]++;
                    this.requiredDataAmountPairsBulk[((BulkDataArrivalEvent) event).getBulkData().getSource()][((BulkDataArrivalEvent) event).getBulkData().getDestination()] += ((BulkDataArrivalEvent) event).getBulkData().getDataAmount();
                    this.requiredDataAmountPairsDiffBulk[((BulkDataArrivalEvent) event).getBulkData().getSource()][((BulkDataArrivalEvent) event).getBulkData().getDestination()][cos] += ((BulkDataArrivalEvent) event).getBulkData().getDataAmount();
                }
                if (Simulator.verbose && (numberArrivals == 10000 * verboseCount)) {
                    System.out.println(Integer.toString(numberArrivals));
                    verboseCount++;
                }
            } else if (event instanceof BulkDepartureEvent) {
                if (this.numberArrivals > this.minNumberArrivals) {
                    this.departures++;
                    this.departuresBulk++;
                }
            } else if (event instanceof BatchArrivalEvent) {
                this.numberArrivals++;
                if (this.numberArrivals > this.minNumberArrivals) {
                    this.numTransponders += (long) availableTransponders;
                    int cos = ((BatchArrivalEvent) event).getBatch().getCOS();
                    this.totalDataRequired += ((BatchArrivalEvent) event).getBatch().getSumDataAmounts();
                    this.arrivals++;
                    this.arrivalsDiff[cos]++;
                    int[] srcs = ((BatchArrivalEvent) event).getBatch().getSources();
                    for (int i = 0; i < srcs.length; i++) {
                        this.arrivalsPairs[srcs[i]][((BatchArrivalEvent) event).getBatch().getDestination()]++;
                        this.arrivalsPairsDiff[srcs[i]][((BatchArrivalEvent) event).getBatch().getDestination()][cos]++;
                        this.arrivalsPairsBatch[srcs[i]][((BatchArrivalEvent) event).getBatch().getDestination()]++;
                        this.arrivalsPairsDiffBatch[srcs[i]][((BatchArrivalEvent) event).getBatch().getDestination()][cos]++;
                        this.requiredDataAmountPairsBulk[srcs[i]][((BatchArrivalEvent) event).getBatch().getDestination()] += ((BatchArrivalEvent) event).getBatch().getDataAmount(i);
                        this.requiredDataAmountPairsDiffBulk[srcs[i]][((BatchArrivalEvent) event).getBatch().getDestination()][cos] += ((BatchArrivalEvent) event).getBatch().getDataAmount(i);
                    }
                    this.arrivalsBatch++;
                    this.arrivalsDiffBatch[cos]++;
                    this.requiredDataAmountBatch += ((BatchArrivalEvent) event).getBatch().getSumDataAmounts();
                    this.requiredDataAmountDiffBatch[cos] += ((BatchArrivalEvent) event).getBatch().getSumDataAmounts();
                }
                if (Simulator.verbose && (numberArrivals == 10000 * verboseCount)) {
                    System.out.println(Integer.toString(numberArrivals));
                    verboseCount++;
                }
            } else if (event instanceof BatchDepartureEvent) {
                if (this.numberArrivals > this.minNumberArrivals) {
                    this.departures++;
                    this.departuresBatch++;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * This function is called during the simulation execution, but only if
     * verbose was activated.
     *
     * @param simType 0 if the physicalTopology is WDM;  0 if physicalTopology is EON.
     * @return string with the obtained statistics
     */
    protected String verboseStatistics(int simType) {
        float acceptProb, blockProb, bbr;
        float bpDiff[], bbrDiff[];
        if (accepted == 0) {
            acceptProb = 0;
        } else {
            acceptProb = ((float) accepted) / ((float) arrivals) * 100;
        }
        if (blocked == 0) {
            blockProb = 0;
            bbr = 0;
        } else {
            blockProb = ((float) blocked) / ((float) arrivals) * 100;
            bbr = ((float) blockedBandwidth) / ((float) requiredBandwidth) * 100;
        }
        bpDiff = new float[numClasses];
        bbrDiff = new float[numClasses];
        for (int i = 0; i < numClasses; i++) {
            if (blockedDiff[i] == 0) {
                bpDiff[i] = 0;
                bbrDiff[i] = 0;
            } else {
                bpDiff[i] = ((float) blockedDiff[i]) / ((float) arrivalsDiff[i]) * 100;
                bbrDiff[i] = ((float) blockedBandwidthDiff[i]) / ((float) requiredBandwidthDiff[i]) * 100;
            }
        }

        if(arrivals != (accepted + blocked)) {
            throw (new IllegalArgumentException("Error in RA code!"));
        }
        String stats = "";
        if(!unavailabilityFlows.isEmpty()) {
            stats += "Average unavailability time  \t: " + unavailabilityTime / (double) unavailabilityFlows.size() + " \n";
        }
        if(reproSTTimes != 0) {
            stats += "Average unavailability for re-provisioning  \t: " + (reproSTTotal / (double) reproSTTimes) + " \n";
        }
        if(simType > 0 && ((EONPhysicalTopology) pt).getPI().isCheckQoT()) {
            stats += "Numeber of LPs without QoT (SNR)  \t: " + Long.toString(snrCount) + " \n";
            stats += "Average SNR Surplus \t: " + snrSurplus / (double) snrSurplusTime + " \n";
            for (int i = 0; i < modulations.length; i++) {
                stats += "Average SNR Surplus ("+Modulation.getModulationName(i)+") \t: " + snrSurplusM[i] / (double) snrSurplusMTime[i] + " \n";
            }
        }
        stats += "Total Data Required \t: " + Long.toString(totalDataRequired) + " Mb\n";
        stats += "Total Data Transmitted \t: " + Long.toString(totalDataTransmitted) + " Mb\n";
        stats += "Simulation time: " + simulationTime + "s\n";
        stats += "Arrivals \t: " + Integer.toString(arrivals) + "\n";
        if (simType >= 2) {
            stats += "Arrivals Flow \t: " + Integer.toString(arrivalsFlow) + "\n";
            stats += "Arrivals Bulk \t: " + Integer.toString(arrivalsBulk) + "\n";
            stats += "Arrivals Batch \t: " + Integer.toString(arrivalsBatch) + "\n";
        }
        stats += "Departures \t: " + Integer.toString(departures) + "\n";
        if (simType >= 2) {
            stats += "Departures Flow \t: " + Integer.toString(departuresFlow) + "\n";
            stats += "Departures Bulk \t: " + Integer.toString(departuresBulk) + "\n";
            stats += "Departures Batch \t: " + Integer.toString(departuresBatch) + "\n";
        }
        stats += "Accepted \t: " + Integer.toString(accepted) + "\t(" + Float.toString(acceptProb) + "%)\n";
        if (simType >= 2) {
            if (acceptedFlow == 0) {
                acceptProb = 0;
            } else {
                acceptProb = ((float) acceptedFlow) / ((float) arrivalsFlow) * 100;
            }
            stats += "Accepted Flow \t: " + Integer.toString(acceptedFlow) + "\t(" + Float.toString(acceptProb) + "%)\n";
            if (acceptedBulk == 0) {
                acceptProb = 0;
            } else {
                acceptProb = ((float) acceptedBulk) / ((float) arrivalsBulk) * 100;
            }
            stats += "Accepted Bulk \t: " + Integer.toString(acceptedBulk) + "\t(" + Float.toString(acceptProb) + "%)\n";
            if (acceptedBatch == 0) {
                acceptProb = 0;
            } else {
                acceptProb = ((float) acceptedBatch) / ((float) arrivalsBatch) * 100;
            }
            stats += "Accepted Bacth \t: " + Integer.toString(acceptedBatch) + "\t(" + Float.toString(acceptProb) + "%)\n";
        }
        stats += "Blocked \t: " + Integer.toString(blocked) + "\t(" + Float.toString(blockProb) + "%)\n";
        stats += "Required BW \t: " + Long.toString(requiredBandwidth) + " Mbps\n";
        stats += "Blocked BW \t: " + Long.toString(blockedBandwidth) + " Mbps\n";
        stats += "BBR      \t: " + Float.toString(bbr) + "%\n";
        if (simType >= 2) {
            if (blockedFlow == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedFlow) / ((float) arrivalsFlow) * 100;
            }
            stats += "Blocked Flow \t: " + Integer.toString(blockedFlow) + "\t(" + Float.toString(blockProb) + "%)\n";
            if (blockedBulk == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedBulk) / ((float) arrivalsBulk) * 100;
                bbr = ((float) blockedDataAmountBulk) / ((float) requiredDataAmountBulk) * 100;
            }
            stats += "Blocked Bulk \t: " + Integer.toString(blockedBulk) + "\t(" + Float.toString(blockProb) + "%)\n";
            stats += "Required Data Amount Bulk \t: " + Long.toString(requiredDataAmountBulk) + " Mbps\n";
            stats += "Blocked Data Amount Bulk \t: " + Long.toString(blockedDataAmountBulk) + " Mbps\n";
            stats += "BDAR Bulk      \t: " + Float.toString(bbr) + "%\n";
            if (blockedBatch == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedBatch) / ((float) arrivalsBatch) * 100;
                bbr = ((float) blockedDataAmountBatch) / ((float) requiredDataAmountBatch) * 100;
            }
            stats += "Blocked Batch \t: " + Integer.toString(blockedBatch) + "\t(" + Float.toString(blockProb) + "%)\n";
            stats += "Required Data Amount Batch \t: " + Long.toString(requiredDataAmountBatch) + " Mbps\n";
            stats += "Blocked Data Amount Batch \t: " + Long.toString(blockedDataAmountBatch) + " Mbps\n";
            stats += "BDAR Batch      \t: " + Float.toString(bbr) + "%\n";
        }
        if (numClasses > 1) {
            stats += "Called Blocked by COS (%)" + "\n";
            for (int i = 0; i < numClasses; i++) {
                stats += "BR-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]) + "%\n";
            }
            stats += "BBR by cos" + "\n";
            for (int i = 0; i < numClasses; i++) {
                stats += "BBR-" + Integer.toString(i) + " " + Float.toString(bbrDiff[i]) + "%\n";
            }
            if (simType >= 2) {
                stats += "Called Blocked by COS (%) (Flow)" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffFlow[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffFlow[i]) / ((float) arrivalsDiffFlow[i]) * 100;
                    }
                    stats += "BR-Flow-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]) + "%\n";
                }
                stats += "Called Blocked by COS (%) (Bulk)" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffBulk[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffBulk[i]) / ((float) arrivalsDiffBulk[i]) * 100;
                        bbrDiff[i] = ((float) blockedDataAmountDiffBulk[i]) / ((float) requiredDataAmountDiffBulk[i]) * 100;
                    }
                    stats += "BR-Bulk-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]) + "%\n";
                }
                stats += "BDAR by cos Bulk" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "BDAR-Bulk-" + Integer.toString(i) + " " + Float.toString(bbrDiff[i]) + "%\n";
                }
                stats += "Called Blocked by COS (%) (Batch)" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffBatch[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffBatch[i]) / ((float) arrivalsDiffBatch[i]) * 100;
                        bbrDiff[i] = ((float) blockedDataAmountDiffBatch[i]) / ((float) requiredDataAmountDiffBatch[i]) * 100;
                    }
                    stats += "BR-Batch-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]) + "%\n";
                }
                stats += "BDAR by cos Batch" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "BDAR-Batch-" + Integer.toString(i) + " " + Float.toString(bbrDiff[i]) + "%\n";
                }
            }
        }
        stats += "\nNumber of LPs: " + numLightPaths + "\n";
        double freeTransponders = (float) numTransponders / times; //free transponders/times-requests
        double freeTranspondersRatio = (float) ((freeTransponders * 100.0) / MAX_NumTransponders);
        double usedTransponder = (double) MAX_NumTransponders - freeTransponders;
        int nodes = blockedPairs[0].length;
        //stats += "Average of free Transponders in network: " + freeTransponders + " ("+MAX_NumTransponders+")\n";
        //stats += "Average of used Transponders in network: " + usedTransponder + " ("+MAX_NumTransponders+")\n";
        stats += "Average of free Transponders by node: " + (double) freeTransponders / nodes + " (" + MAX_NumTransponders / nodes + ")\n";
        //stats += "Average of used Transponders by node: " +(double) usedTransponder/nodes + " ("+MAX_NumTransponders/nodes+")\n";
        stats += "Available Transponders ratio: " + freeTranspondersRatio + "%\n";
        //stats += "Transponders utilization ratio: " + (100.0 - freeTranspondersRatio) + "%\n";

        double used = (double) this.usedTransponders / (double) accepted;
        stats += "Average of Transponders per request: " + used + "\n";
        used = (double) this.virtualHopsPrimary / (double) accepted;
        stats += "Average of Virtual Hops per request: " + used + "\n";
        used = (double) this.physicalHopsPrimary / (double) accepted;
        stats += "Average of Physical Hops per request: " + used + "\n";
        if (qtProtect > 0) {
            used = (double) this.virtualHopsBackup / (double) accepted;
            stats += "Average of Virtual Hops Backup per request: " + used + "\n";
            used = (double) this.physicalHopsBackup / (double) accepted;
            stats += "Average of Physical Hops Backup per request: " + used + "\n";
        }
        used = ((oxcSleepTimeTotal - (oxcStateChangeTotal * oxcTransitionTime)) / (double) numNodes) / simulationTime * 100.0;
        stats += "OXC Average Sleep time: " + used + "%\n";

        if (simType >= 1) {
            double averageSpectrumAvailable = availableSlots / times;
            double spectrumAvailableRatio = (averageSpectrumAvailable * 100.0) / MAX_AvailableSlots;
            stats += "Spectrum Available ratio: " + spectrumAvailableRatio + "%\n";
            //stats += "Spectrum utilization ratio: " + (100.0 - spectrumAvailableRatio) + "%\n";
            double averageFragmentation = fragmetationRate / times;
            stats += "Mean External Fragmentation rate: " + averageFragmentation * 100 + "%\n";
        }
        double averagePowerConsumption = totalPowerConsumptionLPs / numLightPaths;
        stats += "Average Power Consumption: " + averagePowerConsumption + "\n";
        //total sleep:
        used = (oxcSleepTimeTotal - (oxcStateChangeTotal * oxcTransitionTime));
        double powerSavesW = (oxcOperationExpenditure * ((100.0 - oxcSleepModeExpenditure) / 100.0)) * used;
        used = totalPowerConsumption - powerSavesW;
        //stats += "Total Power Consumption Wh: " + totalPowerConsumption + "\n";
        stats += "Total Power Consumption Wh: " + used + "\n";
        //Energy Efficiency(Mb/Joule)
        //double enEff = (double) totalDataTransmitted / totalPowerConsumption;
        double enEff = (double) totalDataTransmitted / used;
        stats += "Energy Efficiency(Mb/Joule): " + enEff + "\n";
        stats += "Effective Energy Efficiency: " + enEff * (1.0 - (bbr / 100.0)) + "\n";
        if (simType >= 1) {
            for (int i = 0; i < modulations.length; i++) {
                stats += Modulation.getModulationName(i) + " Modulation used: " + Float.toString((float) modulations[i] / (float) numLightPaths * 100) + "%\n";
            }
        }
        //For protection
        if (qtProtect > 0) {
            stats += "Flows Protection: " + ((double) qtProtect / (double) accepted) * 100.0 + "%\n";
            if (numClasses > 1) {
                stats += "Flows Protection by cos" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "Flows Protection COS-[" + i + "]: " + ((double) qtProtectDiff[i] / (double) acceptedDiff[i]) * 100.0 + "%\n";
                }
            }
        }
        //TODO quantidade de grooming / por aceitos!
        stats += "LP with Grooming: " + ((double) qtGrooming / (double) numLightPathsPrimary) * 100.0 + "%\n";
        if (numClasses > 1) {
            stats += "LP with Grooming by cos" + "\n";
            for (int i = 0; i < numClasses; i++) {
                stats += "LP with Grooming COS-[" + i + "]: " + ((double) qtGroomingDiff[i] / (double) numLightPathsPrimaryDiff[i]) * 100.0 + "%\n";
            }
        }
        if (qtProtect > 0) {
            stats += "LP with Grooming Backup: " + ((double) qtGroomingBackup / (double) numLightPathsBackup) * 100.0 + "%\n";
            if (numClasses > 1) {
                stats += "LP with Grooming Backup by cos" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "LP with Grooming Backup COS-[" + i + "]: " + ((double) qtGroomingBackupDiff[i] / (double) numLightPathsBackupDiff[i]) * 100.0 + "%\n";
                }
            }
        }
        
        stats += "Primary Setup Time Average: " + (setupTimePrimary / (double) accepted) + " ms\n";
        stats += "Total Primary Setup Time: " + setupTimePrimary + " ms\n";
            
        if (qtProtect > 0) {
            stats += "Backup Setup Time Average: " + (setupTimeBackup / (double) accepted) + " ms\n";
            stats += "Total Backup Setup Time: " + setupTimeBackup + " ms\n";
        }
        
        stats += "Setup Time Average: " + (setupTimeMax / (double) accepted) + " ms\n";
        stats += "Total Setup Time: " + setupTimeMax + " ms\n";
        if (numClasses > 1) {
            stats += "Total Setup Time by cos" + "\n";
            for (int i = 0; i < numClasses; i++) {
                stats += "Total Setup Time COS-[" + i + "]: " + setupTimeMaxDiff[i] + " ms\n";
            }
        }
        if (qtProtect > 0) {
            stats += "Protect Switch Time Average: " + (protectSwitchTime / (double) accepted) + " ms\n";
            stats += "Total Protect Switch Time: " + protectSwitchTime + " ms\n";
            if (numClasses > 1) {
                stats += "Total Protect Switch Time by cos" + " ms\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "Total Protect Switch Time COS-[" + i + "]: " + protectSwitchTimeDiff[i] + " ms\n";
                }
            }
        }
        float brAux, bbrAux, jfi;
        float sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0; 
        int count = 0;
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (i != j) {
                    if (blockedPairs[i][j] == 0) {
                        brAux = 0;
                        bbrAux = 0;
                    } else {
                        brAux = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                        bbrAux = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    count++;
                    sum1 += brAux;
                    sum2 += brAux * brAux;
                    sum3 += bbrAux;
                    sum4 += bbrAux * bbrAux;
                }
            }
        }
        jfi = (sum1 * sum1) / ((float) count * sum2);
        if (Float.isNaN(jfi)) {
            jfi = 1;
        }
        stats += "JFI BR: " + jfi + "\n";
        jfi = (sum3 * sum3) / ((float) count * sum4);
        if (Float.isNaN(jfi)) {
            jfi = 1;
        }
        stats += "JFI BBR: " + jfi + "\n";
        
        stats += "Time(ms): " + ((double) sumFlowArrivalTime/ (double) flowArrivalTimes) + "\n";
        
        if (simType >= 2) {
            stats += "TimeBulk(ms): " + ((double) sumBulkArrivalTime/ (double) bulkArrivalTimes) + "\n";
            stats += "TimeBatch(ms): " + ((double) sumBatchArrivalTime/ (double) batchArrivalTimes) + "\n";
        }
        /*
        stats += "\nBlocking probability per s1-d node-pair:\n";
        if (qtProtect > 0) {
            stats += "Pair\tCalls\tBR\tBBR\tQtProtection\tQtGrooming\tQtGroomingBackup\tST\tPST\n";
        } else {
            stats += "Pair\tCalls\tBR\tBBR\tQtGrooming\n";
        }
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    stats += Integer.toString(i) + "->" + Integer.toString(j);
                    stats += "\t" + Integer.toString(arrivalsPairs[i][j]);
                    if (blockedPairs[i][j] == 0) {
                        blockProb = 0;
                        bbr = 0;
                    } else {
                        blockProb = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                        bbr = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    stats += "\t" + Float.toString(blockProb) + "%";
                    stats += "\t" + Float.toString(bbr) + "%";
                    if (qtProtect > 0) {
                        stats += "\t" + ((double) qtProtectPairs[i][j] / (double) acceptedPairs[i][j]) * 100.0 + "%";
                    }
                    stats += "\t" + ((double) qtGroomingPairs[i][j] / (double) numLightPathsPrimaryPairs[i][j]) * 100.0 + "%";
                    if (qtProtect > 0) {
                        stats += "\t" + ((double) qtGroomingBackupPairs[i][j] / (double) numLightPathsBackupPairs[i][j]) * 100.0 + "%";
                        stats += "\t" + setupTimeMaxPairs[i][j]/(double) acceptedPairs[i][j];
                        stats += "\t" + protectSwitchTimePairs[i][j]/(double) acceptedPairs[i][j];
                    }
                    stats +="\n";                   
                }
            }
        }
        TODO The flow, bulk and batch pairs
        */
        /*OXC Average Sleep time per node*/
        
        for (int i = 0; i < numNodes; i++) {
            used = (oxcSleepTime[i] - (oxcStateChange[i] * oxcTransitionTime)) / simulationTime * 100.0;
            stats += "OXC Average Sleep time node["+i+"]: " + used + "%\n";
        }
        
        return stats;
    }

    /**
     * Prints all the obtained statistics, but only if verbose was not
     * activated.
     *
     * @param simType 0 if the physicalTopology is WDM;  0 if physicalTopology is EON.
     */
    protected void printStatistics(int simType) {
        float acceptProb, blockProb, bbr, meanK;
        float bpDiff[], bbrDiff[];
        if (accepted == 0) {
            acceptProb = 0;
            meanK = 0;
        } else {
            acceptProb = ((float) accepted) / ((float) arrivals) * 100;
        }
        if (blocked == 0) {
            blockProb = 0;
            bbr = 0;
        } else {
            blockProb = ((float) blocked) / ((float) arrivals) * 100;
            bbr = ((float) blockedBandwidth) / ((float) requiredBandwidth) * 100;
        }
        bpDiff = new float[numClasses];
        bbrDiff = new float[numClasses];
        for (int i = 0; i < numClasses; i++) {
            if (blockedDiff[i] == 0) {
                bpDiff[i] = 0;
                bbrDiff[i] = 0;
            } else {
                bpDiff[i] = ((float) blockedDiff[i]) / ((float) arrivalsDiff[i]) * 100;
                bbrDiff[i] = ((float) blockedBandwidthDiff[i]) / ((float) requiredBandwidthDiff[i]) * 100;
            }
        }

        if(arrivals != (accepted + blocked)) {
            throw (new IllegalArgumentException("Error in RA code!"));
        }
        String stats = "";
        
        if(intDumbParameterIsSet) {
            stats += "intDumbParameter: " + jsonNormalizeNumber(intDumbParameter) + "\n";
        }
        if(stringDumbParameterIsSet) {
            stats += "stringDumbParameter: " + stringDumbParameter + "\n";
        }
        if(doubleDumbParameterIsSet) {
            stats += "doubleDumbParameter: " + jsonNormalizeNumber(doubleDumbParameter) + "\n";
        }
        
        if(!unavailabilityFlows.isEmpty()) {
            stats += "AUT: " + unavailabilityTime / (double) unavailabilityFlows.size() + " \n";
        }
        if(reproSTTimes != 0) {
            stats += "AUR: " + (reproSTTotal / (double) reproSTTimes) + " \n";
        }
        if(simType > 0 && ((EONPhysicalTopology) pt).getPI().isCheckQoT()) {
            stats += "withoutQoT: " + Long.toString(snrCount) + " \n";
            //TODO remover depois essa linha de baixo
            stats += "withoutQoT: " + (double) snrCount / (double) numLightPaths * 100 + "% \n";
            stats += "SNRSurplus: " + snrSurplus / (double) snrSurplusTime + " \n";
            for (int i = 0; i < modulations.length; i++) {
                stats += "SNRSurplus("+Modulation.getModulationName(i)+"): " + snrSurplusM[i] / (double) snrSurplusMTime[i] + " \n";
            }
        }
        stats += "TDR: " + Long.toString(totalDataRequired/1000000) + " Tb\n";
        stats += "TDT: " + Long.toString(totalDataTransmitted/1000000) + " Tb\n";
        stats += "BR: " + Float.toString(blockProb) + "%\n";
        stats += "BBR: " + Float.toString(bbr) + "%\n";
        if (simType >= 2) {
            if (blockedFlow == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedFlow) / ((float) arrivalsFlow) * 100;
            }
            stats += "BR-Flow: " + Float.toString(blockProb) + "%\n";
            if (blockedBulk == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedBulk) / ((float) arrivalsBulk) * 100;
                bbr = ((float) blockedDataAmountBulk) / ((float) requiredDataAmountBulk) * 100;
            }
            stats += "BR-Bulk: " + Float.toString(blockProb) + "%\n";
            stats += "BDAR-Bulk: " + Float.toString(bbr) + "%\n";
            if (blockedBatch == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedBatch) / ((float) arrivalsBatch) * 100;
                bbr = ((float) blockedDataAmountBatch) / ((float) requiredDataAmountBatch) * 100;
            }
            stats += "BR-Batch: " + Float.toString(blockProb) + "%\n";
            stats += "BDAR-Batch: " + Float.toString(bbr) + "%\n";
        }
        if (numClasses > 1) {
            stats += "Called Blocked by COS (%)" + "\n";
            for (int i = 0; i < numClasses; i++) {
                stats += "BR-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]) + "%\n";
            }
            stats += "BBR by cos" + "\n";
            for (int i = 0; i < numClasses; i++) {
                stats += "BBR-" + Integer.toString(i) + " " + Float.toString(bbrDiff[i]) + "%\n";
            }
            if (simType >= 2) {
                stats += "Called Blocked by COS (%) (Flow)" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffFlow[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffFlow[i]) / ((float) arrivalsDiffFlow[i]) * 100;
                    }
                    stats += "BR-Flow-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]) + "%\n";
                }
                stats += "Called Blocked by COS (%) (Bulk)" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffBulk[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffBulk[i]) / ((float) arrivalsDiffBulk[i]) * 100;
                        bbrDiff[i] = ((float) blockedDataAmountDiffBulk[i]) / ((float) requiredDataAmountDiffBulk[i]) * 100;
                    }
                    stats += "BR-Bulk-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]) + "%\n";
                }
                stats += "BDAR by cos Bulk" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "BDAR-Bulk-" + Integer.toString(i) + " " + Float.toString(bbrDiff[i]) + "%\n";
                }
                stats += "Called Blocked by COS (%) (Batch)" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffBatch[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffBatch[i]) / ((float) arrivalsDiffBatch[i]) * 100;
                        bbrDiff[i] = ((float) blockedDataAmountDiffBatch[i]) / ((float) requiredDataAmountDiffBatch[i]) * 100;
                    }
                    stats += "BR-Batch-" + Integer.toString(i) + " " + Float.toString(bpDiff[i]) + "%\n";
                }
                stats += "BDAR by cos Batch" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "BDAR-Batch-" + Integer.toString(i) + " " + Float.toString(bbrDiff[i]) + "%\n";
                }
            }
        }
        stats += "LPs: " + numLightPaths + "\n";
        
        double used = (double) this.usedTransponders / (double) accepted;
        stats += "TR: " + used + "\n";
        //TODO: Remover isso depois!
        double freeTransponders = (float) numTransponders / times; //free transponders/times-requests
        double freeTranspondersRatio = (float) ((freeTransponders * 100.0) / MAX_NumTransponders);
        double usedTransponder = (double) MAX_NumTransponders - freeTransponders;
        int nodes = blockedPairs[0].length;
        stats += "Average of free Transponders by node: " + (double) freeTransponders / nodes + " (" + MAX_NumTransponders / nodes + ")\n";
        stats += "Available Transponders ratio: " + freeTranspondersRatio + "%\n";
        //TODO até aqui
        used = (double) this.virtualHopsPrimary / (double) accepted;
        stats += "VH: " + used + "\n";
        used = (double) this.physicalHopsPrimary / (double) accepted;
        stats += "PH: " + used + "\n";
        if (qtProtect > 0) {
            used = (double) this.virtualHopsBackup / (double) accepted;
            stats += "VH_Backup: " + used + "\n";
            used = (double) this.physicalHopsBackup / (double) accepted;
            stats += "PH_Backup: " + used + "\n";
        }
        used = ((oxcSleepTimeTotal - (oxcStateChangeTotal * oxcTransitionTime)) / (double) numNodes) / simulationTime * 100.0;
        stats += "OXCAST: " + used + "%\n";

        if (simType >= 1) {
            double averageSpectrumAvailable = availableSlots / times;
            double spectrumAvailableRatio = (averageSpectrumAvailable * 100.0) / MAX_AvailableSlots;
            stats += "Spec: " + spectrumAvailableRatio + "%\n";
            //stats += "Spectrum utilization ratio: " + (100.0 - spectrumAvailableRatio) + "%\n";
            double averageFragmentation = fragmetationRate / times;
            stats += "Frag: " + averageFragmentation * 100 + "%\n";
        }
        double averagePowerConsumption = totalPowerConsumptionLPs / numLightPaths;
        stats += "PC: " + averagePowerConsumption + "\n";
        //total sleep:
        used = (oxcSleepTimeTotal - (oxcStateChangeTotal * oxcTransitionTime));
        double powerSavesW = (oxcOperationExpenditure * ((100.0 - oxcSleepModeExpenditure) / 100.0)) * used;
        used = totalPowerConsumption - powerSavesW;
        //stats += "TP(Wh): " + totalPowerConsumption + "\n";
        stats += "TP(Wh): " + used + "\n";
        //Energy Efficiency(Mb/Joule)
        //double enEff = (double) totalDataTransmitted / totalPowerConsumption;
        double enEff = (double) totalDataTransmitted / used;
        stats += "EE(Mb/Joule): " + enEff + "\n";
        stats += "EEE: " + enEff * (1.0 - (bbr / 100.0)) + "\n";
        if (simType >= 1) {
            for (int i = 0; i < modulations.length; i++) {
                stats += Modulation.getModulationName(i) + ":" + Float.toString((float) modulations[i] / (float) numLightPaths * 100) + "%\n";
            }
        }
        //For protection
        if (qtProtect > 0) {
            stats += "Protection: " + ((double) qtProtect / (double) accepted) * 100.0 + "%\n";
            if (numClasses > 1) {
                stats += "Flows Protection by cos" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "Flows Protection COS-[" + i + "]: " + ((double) qtProtectDiff[i] / (double) acceptedDiff[i]) * 100.0 + "%\n";
                }
            }
        }
        stats += "LP Grooming: " + ((double) qtGrooming / (double) numLightPathsPrimary) * 100.0 + "%\n";
        if (numClasses > 1) {
            stats += "LP Grooming by cos" + "\n";
            for (int i = 0; i < numClasses; i++) {
                stats += "LP Grooming COS-[" + i + "]: " + ((double) qtGroomingDiff[i] / (double) numLightPathsPrimaryDiff[i]) * 100.0 + "%\n";
            }
        }
        if (qtProtect > 0) {
            stats += "LP Grooming Backup: " + ((double) qtGroomingBackup / (double) numLightPathsBackup) * 100.0 + "%\n";
            if (numClasses > 1) {
                stats += "LP Grooming Backup by cos" + "\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "LP with Grooming Backup COS-[" + i + "]: " + ((double) qtGroomingBackupDiff[i] / (double) numLightPathsBackupDiff[i]) * 100.0 + "%\n";
                }
            }
        }
        
        stats += "STP: " + (setupTimePrimary / (double) accepted) + " ms\n";
        stats += "TSTP: " + setupTimePrimary + " ms\n";
        
        if (qtProtect > 0) {
            stats += "STBackup: " + (setupTimeBackup / (double) accepted) + " ms\n";
            stats += "TSTBackup: " + setupTimeBackup + " ms\n";
        }
        stats += "STmax: " + (setupTimeMax / (double) accepted) + " ms\n";
        stats += "TSTmax: " + setupTimeMax + " ms\n";
        if (numClasses > 1) {
            stats += "TST by cos" + "\n";
            for (int i = 0; i < numClasses; i++) {
                stats += "Total Setup Time COS-[" + i + "]: " + setupTimeMaxDiff[i] + " ms\n";
            }
        }
        if (qtProtect > 0) {
            stats += "PST: " + (protectSwitchTime / (double) accepted) + " ms\n";
            stats += "TPST: " + protectSwitchTime + " ms\n";
            if (numClasses > 1) {
                stats += "TPST by cos" + " ms\n";
                for (int i = 0; i < numClasses; i++) {
                    stats += "TPST COS-[" + i + "]: " + protectSwitchTimeDiff[i] + " ms\n";
                }
            }
        }
        float brAux, bbrAux, jfi;
        float sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0; 
        int count = 0;
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (i != j) {
                    if (blockedPairs[i][j] == 0) {
                        brAux = 0;
                        bbrAux = 0;
                    } else {
                        brAux = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                        bbrAux = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    count++;
                    sum1 += brAux;
                    sum2 += brAux * brAux;
                    sum3 += bbrAux;
                    sum4 += bbrAux * bbrAux;
                }
            }
        }
        jfi = (sum1 * sum1) / ((float) count * sum2);
        if (Float.isNaN(jfi)) {
            jfi = 1;
        }
        stats += "JFI BR: " + jfi + "\n";
        jfi = (sum3 * sum3) / ((float) count * sum4);
        if (Float.isNaN(jfi)) {
            jfi = 1;
        }
        stats += "JFI BBR: " + jfi + "\n";
        
        stats += "Time(ms): " + ((double) sumFlowArrivalTime/ (double) flowArrivalTimes) + "\n";
        
        if (simType >= 2) {
            stats += "TimeBulk(ms): " + ((double) sumBulkArrivalTime/ (double) bulkArrivalTimes) + "\n";
            stats += "TimeBatch(ms): " + ((double) sumBatchArrivalTime/ (double) batchArrivalTimes) + "\n";
        }
        
        /*
        stats += "\nBlocking probability per s1-d node-pair:\n";
        if (qtProtect > 0) {
            stats += "Pair\tCalls\tBR\tBBR\tQtProtection\tQtGrooming\tQtGroomingBackup\tST\tPST\n";
        } else {
            stats += "Pair\tCalls\tBR\tBBR\tQtGrooming\n";
        }
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    stats += Integer.toString(i) + "->" + Integer.toString(j);
                    stats += "\t" + Integer.toString(arrivalsPairs[i][j]);
                    if (blockedPairs[i][j] == 0) {
                        blockProb = 0;
                        bbr = 0;
                    } else {
                        blockProb = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                        bbr = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    stats += "\t" + Float.toString(blockProb) + "%";
                    stats += "\t" + Float.toString(bbr) + "%";
                    if (qtProtect > 0) {
                        stats += "\t" + ((double) qtProtectPairs[i][j] / (double) acceptedPairs[i][j]) * 100.0 + "%";
                    }
                    stats += "\t" + ((double) qtGroomingPairs[i][j] / (double) numLightPathsPrimaryPairs[i][j]) * 100.0 + "%";
                    if (qtProtect > 0) {
                        stats += "\t" + ((double) qtGroomingBackupPairs[i][j] / (double) numLightPathsBackupPairs[i][j]) * 100.0 + "%";
                        stats += "\t" + setupTimeMaxPairs[i][j]/(double) acceptedPairs[i][j];
                        stats += "\t" + protectSwitchTimePairs[i][j]/(double) acceptedPairs[i][j];
                    }
                }
            }
        }
        */
        /*OXC Average Sleep time per node*/
        /*
        for (int i = 0; i < numNodes; i++) {
            used = (oxcSleepTime[i] - (oxcStateChange[i] * oxcTransitionTime)) / simulationTime * 100.0;
            stats += "OXCAST["+i+"]: " + used + "%\n";
        }
        */
        System.out.println(stats);
    }

    protected void tableStatistics(int simType, double load) {
        if(Main.header){
            printHeader(simType);
            Main.header = false;
        }
        float acceptProb, blockProb, bbr, meanK;
        float bpDiff[], bbrDiff[]; 
        if (accepted == 0) {
            acceptProb = 0;
            meanK = 0;
        } else {
            acceptProb = ((float) accepted) / ((float) arrivals) * 100;
        }
        if (blocked == 0) {
            blockProb = 0;
            bbr = 0;
        } else {
            blockProb = ((float) blocked) / ((float) arrivals) * 100;
            bbr = ((float) blockedBandwidth) / ((float) requiredBandwidth) * 100;
        }
        bpDiff = new float[numClasses];
        bbrDiff = new float[numClasses];
        for (int i = 0; i < numClasses; i++) {
            if (blockedDiff[i] == 0) {
                bpDiff[i] = 0;
                bbrDiff[i] = 0;
            } else {
                bpDiff[i] = ((float) blockedDiff[i]) / ((float) arrivalsDiff[i]) * 100;
                bbrDiff[i] = ((float) blockedBandwidthDiff[i]) / ((float) requiredBandwidthDiff[i]) * 100;
            }
        }

        if(arrivals != (accepted + blocked)) {
            throw (new IllegalArgumentException("Error in RA code!"));
        }
        String stats = load + "\t";
        if(!unavailabilityFlows.isEmpty()) {
            stats += unavailabilityTime / (double) unavailabilityFlows.size() + " \t";
        }
        if(reproSTTimes != 0) {
            stats += (reproSTTotal / (double) reproSTTimes) + " \t";
        }
        if(simType > 0 && ((EONPhysicalTopology) pt).getPI().isCheckQoT()) {
            stats += Long.toString(snrCount) + " \t";
            stats += (snrSurplus / (double) snrSurplusTime) + " \t";
            for (int i = 0; i < modulations.length; i++) {
                stats += (snrSurplusM[i] / (double) snrSurplusMTime[i]) + " \t";
            }
            
        }
        stats += Long.toString(totalDataRequired) + "\t";
        stats += Long.toString(totalDataTransmitted) + "\t";
        
        stats += Float.toString(blockProb) + "\t";
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                stats += Float.toString(bpDiff[i]) + "\t";
            }
        }
        stats += Float.toString(bbr) + "\t";
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                stats += Float.toString(bbrDiff[i]) + "\t";
            }
        }
        
        if (simType >= 2) {
            if (blockedFlow == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedFlow) / ((float) arrivalsFlow) * 100;
            }
            stats += Float.toString(blockProb) + "\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffFlow[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffFlow[i]) / ((float) arrivalsDiffFlow[i]) * 100;
                    }
                    stats += Float.toString(bpDiff[i]) + "\t";
                }
            }
            if (blockedBulk == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedBulk) / ((float) arrivalsBulk) * 100;
                bbr = ((float) blockedDataAmountBulk) / ((float) requiredDataAmountBulk) * 100;
            }
            stats += Float.toString(blockProb) + "\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffBulk[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffBulk[i]) / ((float) arrivalsDiffBulk[i]) * 100;
                        bbrDiff[i] = ((float) blockedDataAmountDiffBulk[i]) / ((float) requiredDataAmountDiffBulk[i]) * 100;
                    }
                    stats += Float.toString(bpDiff[i]) + "\t";
                }
            }
            stats += Float.toString(bbr) + "\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += Float.toString(bbrDiff[i]) + "\t";
                }
            }
            if (blockedBatch == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedBatch) / ((float) arrivalsBatch) * 100;
                bbr = ((float) blockedDataAmountBatch) / ((float) requiredDataAmountBatch) * 100;
            }
            stats += Float.toString(blockProb) + "\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffBatch[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffBatch[i]) / ((float) arrivalsDiffBatch[i]) * 100;
                        bbrDiff[i] = ((float) blockedDataAmountDiffBatch[i]) / ((float) requiredDataAmountDiffBatch[i]) * 100;
                    }
                    stats += Float.toString(bpDiff[i]) + "\t";
                }
            }
            stats += Float.toString(bbr) + "\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += Float.toString(bbrDiff[i]) + "\t";
                }
            }
        }
        
        stats += numLightPaths + "\t";
        
        double used = (double) this.usedTransponders / (double) accepted;
        stats += used + "\t";
        used = (double) this.virtualHopsPrimary / (double) accepted;
        stats += used + "\t";
        used = (double) this.physicalHopsPrimary / (double) accepted;
        stats += used + "\t";
        if (qtProtect > 0) {
            used = (double) this.virtualHopsBackup / (double) accepted;
            stats += used + "\t";
            used = (double) this.physicalHopsBackup / (double) accepted;
            stats += used + "\t";
        }
        used = ((oxcSleepTimeTotal - (oxcStateChangeTotal * oxcTransitionTime)) / (double) numNodes) / simulationTime * 100.0;
        stats += used + "\t";

        if (simType >= 1) {
            double averageSpectrumAvailable = availableSlots / times;
            double spectrumAvailableRatio = (averageSpectrumAvailable * 100.0) / MAX_AvailableSlots;
            stats += spectrumAvailableRatio + "\t";
            double averageFragmentation = fragmetationRate / times;
            stats += averageFragmentation * 100 + "\t";
        }
        double averagePowerConsumption = totalPowerConsumptionLPs / numLightPaths;
        stats += averagePowerConsumption + "\t";
        //total sleep:
        used = (oxcSleepTimeTotal - (oxcStateChangeTotal * oxcTransitionTime));
        double powerSavesW = (oxcOperationExpenditure * ((100.0 - oxcSleepModeExpenditure) / 100.0)) * used;
        used = totalPowerConsumption - powerSavesW;
        //stats += totalPowerConsumption + "\t";
        stats += used + "\t";
        //Energy Efficiency(Mb/Joule)
        //double enEff = (double) totalDataTransmitted / totalPowerConsumption;
        double enEff = (double) totalDataTransmitted / used;
        stats += enEff + "\t";
        stats += enEff * (1.0 - (bbr / 100.0)) + "\t";
        if (simType >= 1) {
            for (int i = 0; i < modulations.length; i++) {
                stats += Float.toString((float) modulations[i] / (float) numLightPaths * 100) + "\t";
            }
        }
        //For protection
        if (qtProtect > 0) {
            stats += ((double) qtProtect / (double) accepted) * 100.0 + "\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += ((double) qtProtectDiff[i] / (double) acceptedDiff[i]) * 100.0 + "\t";
                }
            }
        }
        stats += ((double) qtGrooming / (double) numLightPathsPrimary) * 100.0 + "\t";
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                stats += ((double) qtGroomingDiff[i] / (double) numLightPathsPrimaryDiff[i]) * 100.0 + "\t";
            }
        }
        if (qtProtect > 0) {
            stats += ((double) qtGroomingBackup / (double) numLightPathsBackup) * 100.0 + "\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += ((double) qtGroomingBackupDiff[i] / (double) numLightPathsBackupDiff[i]) * 100.0 + "\t";
                }
            }
        }
        stats += (setupTimePrimary / (double) accepted) + "\t";
        stats += setupTimePrimary + "\t";
        if (qtProtect > 0) {
            stats += (setupTimeBackup / (double) accepted) + "\t";
            stats += setupTimeBackup + "\t";
        }
        stats += (setupTimeMax / (double) accepted) + "\t";
        stats += setupTimeMax + "\t";
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                stats += setupTimeMaxDiff[i] + "\t";
            }
        }
        if (qtProtect > 0) {
            stats += (protectSwitchTime / (double) accepted) + "\t";
            stats += protectSwitchTime + "\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += protectSwitchTimeDiff[i] + "\t";
                }
            }
        }
        float brAux, bbrAux, jfi;
        float sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0; 
        int count = 0;
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (i != j) {
                    if (blockedPairs[i][j] == 0) {
                        brAux = 0;
                        bbrAux = 0;
                    } else {
                        brAux = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                        bbrAux = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    count++;
                    sum1 += brAux;
                    sum2 += brAux * brAux;
                    sum3 += bbrAux;
                    sum4 += bbrAux * bbrAux;
                }
            }
        }
        jfi = (sum1 * sum1) / ((float) count * sum2);
        if (Float.isNaN(jfi)) {
            jfi = 1;
        }
        stats += jfi + "\t";
        jfi = (sum3 * sum3) / ((float) count * sum4);
        if (Float.isNaN(jfi)) {
            jfi = 1;
        }
        stats += jfi + "\t";
        stats += jfi + "\t";
        
        if (simType >= 2) {
            stats += ((double) sumFlowArrivalTime/ (double) flowArrivalTimes) + "\t";
            stats += ((double) sumBulkArrivalTime/ (double) bulkArrivalTimes) + "\t";
            stats += ((double) sumBatchArrivalTime/ (double) batchArrivalTimes);
        } else {
            stats += ((double) sumFlowArrivalTime/ (double) flowArrivalTimes);
        }
        
        //TODO: Verificar se ha erros! Completar a printHeader!
        /*Blocking probability per s1-d node-pair*/
        /*
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    if (blockedPairs[i][j] == 0) {
                        blockProb = 0;
                        bbr = 0;
                    } else {
                        blockProb = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                        bbr = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    stats += "\t" + Float.toString(blockProb);
                    stats += "\t" + Float.toString(bbr);
                    if (qtProtect > 0) {
                        stats += "\t" + ((double) qtProtectPairs[i][j] / (double) acceptedPairs[i][j]) * 100.0;
                    }
                    stats += "\t" + ((double) qtGroomingPairs[i][j] / (double) numLightPathsPrimaryPairs[i][j]) * 100.0;
                    if (qtProtect > 0) {
                        stats += "\t" + ((double) qtGroomingBackupPairs[i][j] / (double) numLightPathsBackupPairs[i][j]) * 100.0;
                        stats += "\t" + setupTimeMaxPairs[i][j] / (double) acceptedPairs[i][j];
                        stats += "\t" + protectSwitchTimePairs[i][j] / (double) acceptedPairs[i][j];
                    }
                }
            }
        }
        /*
        /*OXC Average Sleep time per node*/
        /*
        for (int i = 0; i < numNodes; i++) {
            used = (oxcSleepTime[i] - (oxcStateChange[i] * oxcTransitionTime)) / simulationTime * 100.0;
            stats += "\t" + used;
        }
        */
        System.out.println(stats);
    }
    
    protected void jsonStatistics(int simType, double load) {
        float acceptProb, blockProb, bbr, meanK, bbrAFS, bbrFrag, bbrQoTN, bbrQoTO, brAFS, brFrag, brQoTN, brQoTO;
        float bpDiff[], bbrDiff[]; 
        if (accepted == 0) {
            acceptProb = 0;
            meanK = 0;
        } else {
            acceptProb = ((float) accepted) / ((float) arrivals) * 100;
        }
        if (blocked == 0) {
            blockProb = 0;
            
            brAFS = 0;
            brFrag = 0;
            brQoTN = 0;
            brQoTO = 0;
            
            bbr = 0;
            
            bbrAFS = 0;
            bbrFrag = 0;
            bbrQoTN = 0;
            bbrQoTO = 0;
            
        } else {
            blockProb = ((float) blocked) / ((float) arrivals) * 100;
            brAFS = ((float) blockedFlowAFS) / ((float) arrivals) * 100;
            brFrag = ((float) blockedFlowFrag) / ((float) arrivals) * 100;
            brQoTN = ((float) blockedFlowQoTN) / ((float) arrivals) * 100;
            brQoTO = ((float) blockedFlowQoTO) / ((float) arrivals) * 100;
            
            bbr = ((float) blockedBandwidth) / ((float) requiredBandwidth) * 100;
            
            bbrAFS = ((float) blockedBandwidthAFS) / ((float) requiredBandwidth) * 100;
            bbrFrag = ((float) blockedBandwidthFrag) / ((float) requiredBandwidth) * 100;
            bbrQoTN = ((float) blockedBandwidthQoTN) / ((float) requiredBandwidth) * 100;
            bbrQoTO = ((float) blockedBandwidthQoTO) / ((float) requiredBandwidth) * 100;
        }
        bpDiff = new float[numClasses];
        bbrDiff = new float[numClasses];
        for (int i = 0; i < numClasses; i++) {
            if (blockedDiff[i] == 0) {
                bpDiff[i] = 0;
                bbrDiff[i] = 0;
            } else {
                bpDiff[i] = ((float) blockedDiff[i]) / ((float) arrivalsDiff[i]) * 100;
                bbrDiff[i] = ((float) blockedBandwidthDiff[i]) / ((float) requiredBandwidthDiff[i]) * 100;
            }
        }

        if(arrivals != (accepted + blocked)) {
            throw (new IllegalArgumentException("Error in RA code!"));
        }
        String stats = "{\n\t\"Load\": " + load + ",\n\t";
        
        if(intDumbParameterIsSet) {
            stats += "\"intDumbParameter\": " + jsonNormalizeNumber(intDumbParameter) + ",\n\t";
        }
        if(stringDumbParameterIsSet) {
            stats += "\"stringDumbParameter\": \"" + stringDumbParameter + "\",\n\t";
        }
        if(doubleDumbParameterIsSet) {
            stats += "\"doubleDumbParameter\": " + jsonNormalizeNumber(doubleDumbParameter) + ",\n\t";
        }
        
        if(!unavailabilityFlows.isEmpty()) {
            stats += "\"AUT\": " + jsonNormalizeNumber(unavailabilityTime / (double) unavailabilityFlows.size()) + ",\n\t";
        }
        if(reproSTTimes != 0) {
            stats += "\"AUR\": " + jsonNormalizeNumber((reproSTTotal / (double) reproSTTimes)) + ",\n\t";
        }
        if(simType > 0 && ((EONPhysicalTopology) pt).getPI().isCheckQoT()) {
            stats += "\"withoutQoT\": " + jsonNormalizeNumber(snrCount) + ",\n\t";
            stats += "\"SNRSurplus\": " + jsonNormalizeNumber(snrSurplus / (double) snrSurplusTime) + ",\n\t";
            for (int i = 0; i < modulations.length; i++) {
                stats += "\"SNRSurplus("+Modulation.getModulationName(i)+")\": " + jsonNormalizeNumber(snrSurplusM[i] / (double) snrSurplusMTime[i]) + ",\n\t";
            }
        }
        stats += "\"TDR\": " + jsonNormalizeNumber(totalDataRequired) + ",\n\t";
        stats += "\"TDT\": " + jsonNormalizeNumber(totalDataTransmitted) + ",\n\t";
        
        stats += "\"BR\": " + jsonNormalizeNumber(blockProb) + ",\n\t";
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                stats += "\"BR-"+ i +"\": " + jsonNormalizeNumber(bpDiff[i]) + ",\n\t";
            }
        }
        stats += "\"BBR\": " + jsonNormalizeNumber(bbr) + ",\n\t";
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                stats += "\"BBR-"+ i +"\": " + jsonNormalizeNumber(bbrDiff[i]) + ",\n\t";
            }
        }
        
        stats += "\"BR_AFS\": " + jsonNormalizeNumber(brAFS) + ",\n\t";
        stats += "\"BR_Frag\": " + jsonNormalizeNumber(brFrag) + ",\n\t";
        stats += "\"BR_QoTN\": " + jsonNormalizeNumber(brQoTN) + ",\n\t";
        stats += "\"BR_QoTO\": " + jsonNormalizeNumber(brQoTO) + ",\n\t";
        
        stats += "\"BBR_AFS\": " + jsonNormalizeNumber(bbrAFS) + ",\n\t";
        stats += "\"BBR_Frag\": " + jsonNormalizeNumber(bbrFrag) + ",\n\t";
        stats += "\"BBR_QoTN\": " + jsonNormalizeNumber(bbrQoTN) + ",\n\t";
        stats += "\"BBR_QoTO\": " + jsonNormalizeNumber(bbrQoTO) + ",\n\t";
        
        if (simType >= 2) {
            if (blockedFlow == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedFlow) / ((float) arrivalsFlow) * 100;
            }
            stats += "\"BR-Flow\": " + jsonNormalizeNumber(blockProb) + ",\n\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffFlow[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffFlow[i]) / ((float) arrivalsDiffFlow[i]) * 100;
                    }
                    stats += "\"BR-Flow-"+ i +"\": " + jsonNormalizeNumber(bpDiff[i]) + ",\n\t";
                }
            }
            if (blockedBulk == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedBulk) / ((float) arrivalsBulk) * 100;
                bbr = ((float) blockedDataAmountBulk) / ((float) requiredDataAmountBulk) * 100;
            }
            stats += "\"BR-Bulk\": " + jsonNormalizeNumber(blockProb) + ",\n\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffBulk[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffBulk[i]) / ((float) arrivalsDiffBulk[i]) * 100;
                        bbrDiff[i] = ((float) blockedDataAmountDiffBulk[i]) / ((float) requiredDataAmountDiffBulk[i]) * 100;
                    }
                    stats += "\"BR-Bulk-"+ i +"\": " + jsonNormalizeNumber(bpDiff[i]) + ",\n\t";
                }
            }
            stats += "\"BDAR-Bulk\": " + jsonNormalizeNumber(bbr) + ",\n\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += "\"BDAR-Bulk-"+ i +"\": " + jsonNormalizeNumber(bbrDiff[i]) + ",\n\t";
                }
            }
            if (blockedBatch == 0) {
                blockProb = 0;
                bbr = 0;
            } else {
                blockProb = ((float) blockedBatch) / ((float) arrivalsBatch) * 100;
                bbr = ((float) blockedDataAmountBatch) / ((float) requiredDataAmountBatch) * 100;
            }
            stats += "\"BR-Batch\": " + jsonNormalizeNumber(blockProb) + ",\n\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    if (blockedDiffBatch[i] == 0) {
                        bpDiff[i] = 0;
                        bbrDiff[i] = 0;
                    } else {
                        bpDiff[i] = ((float) blockedDiffBatch[i]) / ((float) arrivalsDiffBatch[i]) * 100;
                        bbrDiff[i] = ((float) blockedDataAmountDiffBatch[i]) / ((float) requiredDataAmountDiffBatch[i]) * 100;
                    }
                    stats += "\"BR-Batch-"+ i +"\": " + jsonNormalizeNumber(bpDiff[i]) + ",\n\t";
                }
            }
            stats += "\"BDAR-Batch\": " + jsonNormalizeNumber(bbr) + ",\n\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += "\"BDAR-Batch-"+ i +"\": " + jsonNormalizeNumber(bbrDiff[i]) + ",\n\t";
                }
            }
        }
        
        stats += "\"LP\": " + numLightPaths + ",\n\t";
        
        double used = (double) this.usedTransponders / (double) accepted;
        stats += "\"TR\": " + jsonNormalizeNumber(used) + ",\n\t";
        used = (double) this.virtualHopsPrimary / (double) accepted;
        stats += "\"VH\": " + jsonNormalizeNumber(used) + ",\n\t";
        used = (double) this.physicalHopsPrimary / (double) accepted;
        stats += "\"PH\": " + jsonNormalizeNumber(used) + ",\n\t";
        if (qtProtect > 0) {
            used = (double) this.virtualHopsBackup / (double) accepted;
            stats += "\"VH_Backup\": " + jsonNormalizeNumber(used) + ",\n\t";
            used = (double) this.physicalHopsBackup / (double) accepted;
            stats += "\"PH_Backup\": " + jsonNormalizeNumber(used) + ",\n\t";
        }
        used = ((oxcSleepTimeTotal - (oxcStateChangeTotal * oxcTransitionTime)) / (double) numNodes) / simulationTime * 100.0;
        stats += "\"OXCAST\": " + jsonNormalizeNumber(used) + ",\n\t";
        if (simType >= 1) {
            double averageSpectrumAvailable = availableSlots / times;
            double spectrumAvailableRatio = (averageSpectrumAvailable * 100.0) / MAX_AvailableSlots;
            stats += "\"Spec\": " + jsonNormalizeNumber(spectrumAvailableRatio) + ",\n\t";
            double averageFragmentation = fragmetationRate / times;
            stats += "\"Frag\": " + jsonNormalizeNumber(averageFragmentation * 100) + ",\n\t";
        }
        double averagePowerConsumption = totalPowerConsumptionLPs / numLightPaths;
        stats += "\"PC\": " + jsonNormalizeNumber(averagePowerConsumption) + ",\n\t";
        //total sleep:
        used = (oxcSleepTimeTotal - (oxcStateChangeTotal * oxcTransitionTime));
        double powerSavesW = (oxcOperationExpenditure * ((100.0 - oxcSleepModeExpenditure) / 100.0)) * used;
        used = totalPowerConsumption - powerSavesW;
        //stats += "\"TP\": " + jsonNormalizeNumber(totalPowerConsumption) + ",\n\t";
        stats += "\"TP\": " + jsonNormalizeNumber(used) + ",\n\t";
        //Energy Efficiency(Mb/Joule)
        //double enEff = (double) totalDataTransmitted / totalPowerConsumption;
        double enEff = (double) totalDataTransmitted / used;
        stats += "\"EE\": " + jsonNormalizeNumber(enEff) + ",\n\t";
        used = enEff * (1.0 - (bbr / 100.0));
        stats += "\"EEE\": " + jsonNormalizeNumber(used) + ",\n\t";
        if (simType >= 1) {
            for (int i = 0; i < modulations.length; i++) {
                stats += "\"" + Modulation.getModulationName(i) + "\": " +  jsonNormalizeNumber((float) modulations[i] / (float) numLightPaths * 100) + ",\n\t";
            }
        }
        
        //For protection
        if (qtProtect > 0) {
            stats += "\"Protection\": " + ((double) qtProtect / (double) accepted) * 100.0 + ",\n\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += "\"Protection-"+ i +"\": " + jsonNormalizeNumber(((double) qtProtectDiff[i] / (double) acceptedDiff[i]) * 100.0) + ",\n\t";
                }
            }
        }
        stats += "\"LPGrooming\": " + jsonNormalizeNumber(((double) qtGrooming / (double) numLightPathsPrimary) * 100.0) + ",\n\t";
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                stats += "\"LPGrooming-"+ i +"\": " + jsonNormalizeNumber(((double) qtGroomingDiff[i] / (double) numLightPathsPrimaryDiff[i]) * 100.0) + ",\n\t";
            }
        }
        if (qtProtect > 0) { 
            stats += "\"LPGroomingBack\": " + jsonNormalizeNumber(((double) qtGroomingBackup / (double) numLightPathsBackup) * 100.0) + ",\n\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += "\"LPGroomingBack-"+ i +"\": " + jsonNormalizeNumber(((double) qtGroomingBackupDiff[i] / (double) numLightPathsBackupDiff[i]) * 100.0) + ",\n\t";
                }
            }
        }
        
        stats += "\"STP\": " + jsonNormalizeNumber((setupTimePrimary / (double) accepted)) + ",\n\t";
        stats += "\"TSTP\": " + jsonNormalizeNumber(setupTimePrimary) + ",\n\t";
        
        if (qtProtect > 0) { 
            stats += "\"STBackup\": " + jsonNormalizeNumber((setupTimeBackup / (double) accepted)) + ",\n\t";
            stats += "\"TSTBackup\": " + jsonNormalizeNumber(setupTimeBackup) + ",\n\t";
        }
        
        stats += "\"STmax\": " + jsonNormalizeNumber((setupTimeMax / (double) accepted)) + ",\n\t";
        stats += "\"TSTmax\": " + jsonNormalizeNumber(setupTimeMax) + ",\n\t";
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                stats += "\"TST-" + i + "\": " + jsonNormalizeNumber(setupTimeMaxDiff[i]) + ",\n\t";
            }
        }
        
        if (qtProtect > 0) { 
            stats += "\"PST\": " + jsonNormalizeNumber((protectSwitchTime / (double) accepted)) + ",\n\t";
            stats += "\"TPST\": " + jsonNormalizeNumber(protectSwitchTime) + ",\n\t";
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    stats += "\"TPST-"+ i +"\": " + jsonNormalizeNumber(protectSwitchTimeDiff[i]) + ",\n\t";
                }
            }
        }
        float brAux, bbrAux, jfi;
        float sum1 = 0, sum2 = 0, sum3 = 0, sum4 = 0; 
        int count = 0;
        for (int i = 0; i < numNodes; i++) {
            for (int j = i + 1; j < numNodes; j++) {
                if (i != j) {
                    if (blockedPairs[i][j] == 0) {
                        brAux = 0;
                        bbrAux = 0;
                    } else {
                        brAux = ((float) blockedPairs[i][j]) / ((float) arrivalsPairs[i][j]) * 100;
                        bbrAux = ((float) blockedBandwidthPairs[i][j]) / ((float) requiredBandwidthPairs[i][j]) * 100;
                    }
                    count++;
                    sum1 += brAux;
                    sum2 += brAux * brAux;
                    sum3 += bbrAux;
                    sum4 += bbrAux * bbrAux;
                }
            }
        }
        jfi = (sum1 * sum1) / ((float) count * sum2);
        if (Float.isNaN(jfi)) {
            jfi = 1;
        }
        stats += "\"JFI_BR\": " + jsonNormalizeNumber(jfi) + ",\n\t";
        jfi = (sum3 * sum3) / ((float) count * sum4);
        if (Float.isNaN(jfi)) {
            jfi = 1;
        }
        stats += "\"JFI_BBR\": " + jsonNormalizeNumber(jfi) + ",\n\t";
        
        if (simType >= 2) {
            stats += "\"Time(ms)\": " + jsonNormalizeNumber(((double) sumFlowArrivalTime/ (double) flowArrivalTimes)) + ",\n\t";
            stats += "\"TimeBulk(ms)\": " + jsonNormalizeNumber(((double) sumBulkArrivalTime/ (double) bulkArrivalTimes)) + ",\n\t";
            stats += "\"TimeBatch(ms)\": " + jsonNormalizeNumber(((double) sumBatchArrivalTime/ (double) batchArrivalTimes));
        } else {
            stats += "\"Time(ms)\": " + jsonNormalizeNumber(((double) sumFlowArrivalTime/ (double) flowArrivalTimes));
        }
        /*OXC Average Sleep time per node*/
        /*
        for (int i = 0; i < numNodes; i++) {
            used = (oxcSleepTime[i] - (oxcStateChange[i] * oxcTransitionTime)) / simulationTime * 100.0;
            stats += ",\n\t" + "\"OXCAST["+i+"]\": " + used;
        }
        */
        stats += "\n}\n";
        System.out.println(stats);
    }

    /**
     * Terminates the singleton object.
     */
    protected void finish() {
        singletonObject = null;
    }

    protected void createLightpath(LightPath lp) {
        if (vt != null) {
            this.numLightPaths++;
            mapLPTime.put(lp.getID(), currentTimeEvent);
            if (lp instanceof EONLightPath) {
                this.modulations[((EONLightPath) lp).getModulation()]++;
            }
            totalPowerConsumptionLPs += vt.getPowerConsumption(lp);
        }
    }

    protected void removeLightpath(LightPath lp) {
        if (mapLPTime.containsKey(lp.getID())) {
            double LpPowerConsumption = vt.getPowerConsumptionWithoutOXCSetup(lp);
            totalPowerConsumption += (LpPowerConsumption * (currentTimeEvent - mapLPTime.get(lp.getID()))) + vt.getBVOXCSetupExpenditure(lp);
            oxcOperationPowerConsumptionTotal += ((oxcOperationExpenditure * (lp.getLinks().length + 1)) * (currentTimeEvent - mapLPTime.get(lp.getID())));
            oxcOperationPowerConsumption[lp.getSource()] += (oxcOperationExpenditure * (currentTimeEvent - mapLPTime.get(lp.getID())));
            for (int i = 0; i < lp.getLinks().length; i++) {
                oxcOperationPowerConsumption[pt.getLink(lp.getLinks()[i]).getDestination()] += (oxcOperationExpenditure * (currentTimeEvent - mapLPTime.get(lp.getID())));
            }

            mapLPTime.remove(lp.getID());
        }
    }
    
    protected void reprovisionLightpath(LightPath old, LightPath lp) {
        removeLightpath(old);
        createLightpath(lp);
        //Unavailability time is the deconfiguration time + setup time
        double st1, st2;
        st1 = calculateST(lp);
        st2 = calculateST(old);
        reproSTTimes++;
        reproSTTotal += (st1 + st2);
    }

    protected void deallocatedLightpath(LightPath lp) {
        this.numLightPaths--;
        mapLPTime.remove(lp.getID());
        if (lp instanceof EONLightPath) {
            this.modulations[((EONLightPath) lp).getModulation()]--;
        }
        totalPowerConsumptionLPs -= vt.getPowerConsumption(lp);
    }

    protected double lengthPath(LightPath lightpath) {
        double lpLength = 0;
        for (int i = 0; i < lightpath.getLinks().length; i++) {
            lpLength += pt.getLink(lightpath.getLinks()[i]).getWeight();
        }
        return lpLength;
    }
    
    protected double lengthPathBackups(LightPath lightpath) {
        double lpLength = 0;
        LightPath lp;
        for (Long lpBackup : lightpath.getLpBackup()) {
            lp = vt.getLightpath(lpBackup);
            for (int i = 0; i < lp.getLinks().length; i++) {
                lpLength += pt.getLink(lp.getLinks()[i]).getWeight();
            }
        }
        return lpLength;
    }
    
    private int getBackupPhysicalHops(LightPath lightpath) {
        int lpHops = 0;
        LightPath lp;
        for (Long lpBackup : lightpath.getLpBackup()) {
            lp = vt.getLightpath(lpBackup);
            lpHops += lp.getHops();
        }
        return lpHops;
    }
    
    protected double lengthPath(LightPath[] lps) {
        double lpLength = 0;
        for (LightPath lightpath : lps) {
            for (int i = 0; i < lightpath.getLinks().length; i++) {
                lpLength += pt.getLink(lightpath.getLinks()[i]).getWeight();
            }
        }
        return lpLength;
    }
    
    protected double lengthPath(Path[] paths) {
        double lpLength = 0;
        for (Path path : paths) {
            lpLength += lengthPath(path.getLightpaths());
        }
        return lpLength;
    }
    
    protected double maxLengthPath(Path[] paths) {
        double lpLength, maxLengthPath = 0;
        for (Path path : paths) {
            lpLength = 0;
            for (LightPath lightpath : path.getLightpaths()) {
                for (int i = 0; i < lightpath.getLinks().length; i++) {
                    lpLength += pt.getLink(lightpath.getLinks()[i]).getWeight();
                }
            }
            if (lpLength > maxLengthPath) {
                maxLengthPath = lpLength;
            }
        }
        return maxLengthPath;
    }
    
    protected void printHeader(int simType) {
        System.out.print("#load\t");
        if(!unavailabilityFlows.isEmpty()) {
            System.out.print("AUT\t");
        }
        if(reproSTTimes != 0) {
            System.out.print("AUR\t");
        }
        if(simType > 0 && ((EONPhysicalTopology) pt).getPI().isCheckQoT()) {
            System.out.print("withoutQoT\tSNRSurplus\t");
            for (int i = 0; i < modulations.length; i++) {
                System.out.print("SNRSurplus("+Modulation.getModulationName(i)+")\t");
            }
        }
        System.out.print("TDR\tTDT\tBR\t");
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                System.out.print("BR-" + Integer.toString(i) + "\t");
            }
        }
        System.out.print("BBR\t");
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                System.out.print("BBR-" + Integer.toString(i) + "\t");
            }
        }
        if (simType >= 2) {
            System.out.print("BR-Flow\t");
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    System.out.print("BR-Flow-" + Integer.toString(i) + "\t");
                }
            }
            System.out.print("BR-Bulk\t");
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    System.out.print("BR-Bulk-" + Integer.toString(i) + "\t");
                }
            }
            System.out.print("BDAR-Bulk\t");
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    System.out.print("BDAR-Bulk-" + Integer.toString(i) + "\t");
                }
            }
            System.out.print("BR-Batch\t");
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    System.out.print("BR-Batch-" + Integer.toString(i) + "\t");
                }
            }
            System.out.print("BDAR-Batch\t");
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    System.out.print("BDAR-Batch-" + Integer.toString(i) + "\t");
                }
            }
        }
        System.out.print("LPs\tTR\tVH\tPH\t");
        if (qtProtect > 0) {
            System.out.print("VH_Backup\tPH_Backup\t");
        }
        System.out.print("OXCAST\t");
        if (simType >= 1) {
            System.out.print("Spec\tFrag\t");
        }
        System.out.print("PC\tTP\tEE\tEEE\t");
        if (simType >= 1) {
            for (int i = 0; i < modulations.length; i++) {
                System.out.print(Modulation.getModulationName(i) + "\t");
            }
        }
        if (qtProtect > 0) {
            System.out.print("Protection\t");
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    System.out.print("Protection-" + i + "\t");
                }
            }
        }
        System.out.print("LPGrooming\t");
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                System.out.print("LPGrooming-" + i + "\t");
            }
        }
        if (qtProtect > 0) {
            System.out.print("LPGroomingBack\t");
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    System.out.print("LPGroomingBack-" + i + "\t");
                }
            }
        }
        System.out.print("STP\tTSTP\t");
        if (qtProtect > 0) {
            System.out.print("STBackup\tTSTBackup\t");
        }
        System.out.print("STmax\tTSTmax\t");
        if (numClasses > 1) {
            for (int i = 0; i < numClasses; i++) {
                System.out.print("TST-" + i + "\t");
            }
        }
        if (qtProtect > 0) {
            System.out.print("PST\tTPST\t");
            if (numClasses > 1) {
                for (int i = 0; i < numClasses; i++) {
                    System.out.print("TPST-" + i + "\t");
                }
            }
        }
        System.out.print("JFI_BR\tJFI_BBR\t");
        
        if (simType >= 2) {
            System.out.print("Time(ms)\tTimeBulk(ms)\tTimeBatch(ms)");
        } else {
            System.out.print("Time(ms)");
        }
        
        
        //TODO: Completar a printHeader de acordo com a tableStatistics!
        /*OXC Average Sleep time per node*/
        /*
        for (int i = 0; i < numNodes; i++) {
            System.out.print("\tOXCAST-"+i);
        }
        */
        System.out.print("\n");
    }

    private String jsonNormalizeNumber(Number number) {
        if (number instanceof Float) {
            if (((Float) number).isNaN() || ((Float) number).isInfinite()) {
                return "0";
            }
        } else {
            if (number instanceof Double) {
                if (((Double) number).isNaN() || ((Double) number).isInfinite()) {
                    return "0";
                }
            }
        }
        return number.toString();
    }

    protected void checkOXCState() {
        for (int i = 0; i < numNodes; i++) {
            if(pt.getNode(i).isSleep() != oxcLastState[i]) {
                if(pt.getNode(i).isSleep()) {
                    oxcLastTimeSleep[i] = currentTimeEvent;
                    oxcStateChangeTotal++;
                    oxcStateChange[i]++;
                } else {
                    oxcSleepTimeTotal += currentTimeEvent - oxcLastTimeSleep[i];
                    oxcSleepTime[i] += currentTimeEvent - oxcLastTimeSleep[i];
                    oxcStateChangeTotal++;
                    oxcStateChange[i]++;
                }
                oxcLastState[i] = pt.getNode(i).isSleep();
            }
        }
    }

    protected void acceptBulk(BulkData bulk, LightPath[] lightpaths, boolean[] usedTransponders) {
        double processingDelay, configurationDelay, propagationDelay, STPrimary = 0;
        if (this.numberArrivals > this.minNumberArrivals) {
            this.accepted++;
            this.acceptedDiff[bulk.getCOS()]++;
            this.acceptedPairs[bulk.getSource()][bulk.getDestination()]++;
            
            this.acceptedBulk++;
            totalDataTransmitted += bulk.getDataAmount();
            
            this.virtualHopsPrimary += (long) lightpaths.length;
            int count = 0;
            for (LightPath lps : lightpaths) {
                count += lps.getHops();
            }
            this.physicalHopsPrimary += (long) count;
            int usedTr = 0, grooming = 0;
            for (int i = 0; i < usedTransponders.length; i++) {
                if(usedTransponders[i]) usedTr++;
                else grooming++;
            }
            this.usedTransponders += (long) usedTr;
            numLightPathsPrimary += lightpaths.length;
            numLightPathsPrimaryDiff[bulk.getCOS()] += lightpaths.length;
            numLightPathsPrimaryPairs[bulk.getSource()][bulk.getDestination()] += lightpaths.length;
            qtGrooming += grooming;
            qtGroomingDiff[bulk.getCOS()] += grooming;
            qtGroomingPairs[bulk.getSource()][bulk.getDestination()] += grooming;
            for (int i = 0; i < usedTransponders.length; i++) {
                if(usedTransponders[i]) {
                    processingDelay = (double) (lightpaths[i].getHops() + 1) * mensageProcessingTime;
                    configurationDelay = (double) (lightpaths[i].getHops() + 1) * configurationTimeOXC;
                    propagationDelay = Math.ceil(lengthPath(lightpaths[i]) / (double) spanSize) * propagationDelayTime;
                    STPrimary = (processingDelay + configurationDelay + propagationDelay);
                    setupTimePrimary += STPrimary;
                }
            }
        }
    }

    protected void blockBulk(BulkData bulk) {
        if (this.numberArrivals > this.minNumberArrivals) {
            int cos = bulk.getCOS();
            this.blocked++;
            this.blockedBulk++;
            this.blockedDiff[cos]++;
            this.blockedDiffBulk[cos]++;
            this.blockedDataAmountBulk += bulk.getDataAmount();
            this.blockedDataAmountDiffBulk[cos] += bulk.getDataAmount();
            this.blockedPairs[bulk.getSource()][bulk.getDestination()]++;
            this.blockedPairsBulk[bulk.getSource()][bulk.getDestination()]++;
            this.blockedPairsDiff[bulk.getSource()][bulk.getDestination()][cos]++;
            this.blockedPairsDiffBulk[bulk.getSource()][bulk.getDestination()][cos]++;
            this.blockedDataAmountPairsBulk[bulk.getSource()][bulk.getDestination()] += bulk.getDataAmount();
            this.blockedDataAmountPairsDiffBulk[bulk.getSource()][bulk.getDestination()][cos] += bulk.getDataAmount();
        }
    }

    protected void blockBatch(Batch batch) {
        if (this.numberArrivals > this.minNumberArrivals) {
            int cos = batch.getCOS();
            int[] srcs = batch.getSources();
            this.blocked++;
            this.blockedBatch++;
            this.blockedDiff[cos]++;
            this.blockedDiffBatch[cos]++;
            this.blockedDataAmountBatch += batch.getSumDataAmounts();
            this.blockedDataAmountDiffBatch[cos] += batch.getSumDataAmounts();
            for (int i = 0; i < srcs.length; i++) {
                this.blockedPairs[srcs[i]][batch.getDestination()]++;
                this.blockedPairsBatch[srcs[i]][batch.getDestination()]++;
                this.blockedPairsDiff[srcs[i]][batch.getDestination()][cos]++;
                this.blockedPairsDiffBatch[srcs[i]][batch.getDestination()][cos]++;
                this.blockedDataAmountPairsBatch[srcs[i]][batch.getDestination()] += batch.getDataAmount(i);
                this.blockedDataAmountPairsDiffBatch[srcs[i]][batch.getDestination()][cos] += batch.getDataAmount(i);
            }
        }
    }

    protected void acceptBatch(Batch batch, LightPath[][] lightpaths, boolean[][] usedTransponders) {
        double processingDelay, configurationDelay, propagationDelay, STPrimary = 0;
        if (this.numberArrivals > this.minNumberArrivals) {
            this.accepted++;
            this.acceptedDiff[batch.getCOS()]++;
            int[] srcs = batch.getSources();
            
            this.acceptedBatch++;
            totalDataTransmitted += batch.getSumDataAmounts();
            
            this.virtualHopsPrimary += (long) lightpaths.length;
            int count = 0;
            for (LightPath[] lpsA : lightpaths) {
                for (LightPath lps : lpsA) {
                    if (lps != null) {
                        count += lps.getHops();
                    }
                }
            }
            this.physicalHopsPrimary += (long) count;
            int usedTr = 0, grooming = 0;
            for (int i = 0; i < usedTransponders.length; i++) {
                for (int j = 0; j < usedTransponders[i].length; j++) {
                    if (usedTransponders[i][j]) {
                        usedTr++;
                    } else {
                        grooming++;
                    }
                }
            }
            this.usedTransponders += (long) usedTr;
            numLightPathsPrimary += lightpaths.length;
            numLightPathsPrimaryDiff[batch.getCOS()] += lightpaths.length;
            
            for (int i = 0; i < srcs.length; i++) {
                this.acceptedPairs[srcs[i]][batch.getDestination()]++;
                numLightPathsPrimaryPairs[srcs[i]][batch.getDestination()] += lightpaths[i].length;
                qtGroomingPairs[srcs[i]][batch.getDestination()] += grooming;
            }
            
            qtGrooming += grooming;
            qtGroomingDiff[batch.getCOS()] += grooming;
            
            for (int i = 0; i < usedTransponders.length; i++) {
                for (int j = 0; j < usedTransponders[i].length; j++) {
                    if (usedTransponders[i][j]) {
                        processingDelay = (double) (lightpaths[i][j].getHops() + 1) * mensageProcessingTime;
                        configurationDelay = (double) (lightpaths[i][j].getHops() + 1) * configurationTimeOXC;
                        propagationDelay = Math.ceil(lengthPath(lightpaths[i][j]) / (double) spanSize) * propagationDelayTime;
                        STPrimary = (processingDelay + configurationDelay + propagationDelay);
                        setupTimePrimary += STPrimary;
                    }
                }
            }
        }
    }

    protected void addSNR() {
        snrCount++;
    }
    
    protected void addSNRSurplus(int modulation, double snr) {
        snrSurplusTime++;
        double snrThreshold = Modulation.getModulationSNRthreshold(modulation);
        double surplus;
        surplus = (snr - snrThreshold);
        snrSurplus += surplus;
        snrSurplusM[modulation] += surplus;
        snrSurplusMTime[modulation] ++;
    }
    
    protected void subSNR() {
        snrCount--;
    }
    
    protected void subSNRSurplus(int modulation, double snr) {
        snrSurplusTime--;
        double snrThreshold = Modulation.getModulationSNRthreshold(modulation);
        double surplus;
        surplus = (snr - snrThreshold);
        snrSurplus -= surplus;
        snrSurplusM[modulation] -= surplus;
        snrSurplusMTime[modulation] --;
    }
    
    private double calculateST(LightPath lp) {
        double processingDelay, configurationDelay, propagationDelay;
        processingDelay = (double) (lp.getHops() + 1) * mensageProcessingTime;
        configurationDelay = (double) (lp.getHops() + 1) * configurationTimeOXC;
        propagationDelay = Math.ceil(lengthPath(lp) / (double) spanSize) * propagationDelayTime;
        return (processingDelay + configurationDelay + propagationDelay);
    }
    
    private double calculateST(LightPath[] lp) {
        double st = 0;
        for (LightPath lightPath : lp) {
            st += calculateST(lightPath);
        }
        return st;
    }

    public void setIntDumbParameter(int i) {
        intDumbParameterIsSet = true;
        intDumbParameter = i;
    }

    public void setStringDumbParameter(String s) {
        stringDumbParameterIsSet = true;
        stringDumbParameter = s;
    }

    public void setDoubleDumbParameter(double d) {
        doubleDumbParameterIsSet = true;
        doubleDumbParameter = d;
    }

    protected void setFlowArrivalTime(long time) {
        sumFlowArrivalTime += time;
        flowArrivalTimes++;
    }
    
    protected void setBulkArrivalTime(long time) {
        sumBulkArrivalTime += time;
        bulkArrivalTimes++;
    }
    
    protected void setBatchArrivalTime(long time) {
        sumBatchArrivalTime += time;
        batchArrivalTimes++;
    }

    public void blockAFS(Flow flow) {
        blockedFlowAFS++;
        blockedBandwidthAFS += flow.getRate();
    }

    public void blockQoTN(Flow flow) {
        blockedFlowQoTN++;
        blockedBandwidthQoTN += flow.getRate();
    }

    public void blockQoTO(Flow flow) {
        blockedFlowQoTO++;
        blockedBandwidthQoTO += flow.getRate();
    }

    public void blockFrag(Flow flow) {
        blockedFlowFrag++;
        blockedBandwidthFrag += flow.getRate();
    }
    
}
