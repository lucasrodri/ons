# Welcome to ONS Simulator!

Hi! I'm Lucas Rodrigues Costa and I am the main developer of ONS. We developed the ONS to support our master's and PhD research at the University of Brasília - UnB. 

ONS was developed in Java and uses discrete events to simulate traffic requests from a WDM (Wavelength-division multiplexing) or EON (Elastic Optical Networks) optical network. Its capabilities include developing and evaluating new research related to the Routing and Wavelength Assignment (RWA) problem of WDM networks and to the problem of Routing, Modulation Level, and Spectrum Allocation - RMLSA) of EON networks, both in dynamic traffic scenarios. 

ONS is designed to make it easy to implement new algorithms (RWA/RSA/RMLSA) providing agile implementation and satisfactory performance even when simulated in large network topologies.

With ONS it is also possible to develop algorithms for EON-SDM (EON-Space Division Multiplexing), SNRware algorithms counting on several parameters for execution in several scenarios in the literature.

## About

**The ONS is a simulator of discrete events able to simulate the dynamic traffic in WDM and EON networks.**

**Prerequisite: JVM 8 (Java Virtual Machine)**

If you wish to use the ONS, please quote it using the following example:

@Misc{ONS,  
Title = {{ONS} – Optical Network Simulator},

Author = {Lucas R. Costa and André C. Drummond},  
HowPublished = {\url{//ons-simulator.com}},  
Year = {2021}  
}

## How to compile

-   Download the [base code](http://ons-simulator.com/donwload/)
-   Open your favorite IDE such as [NetBeans](http://www.netbeans.org/) or [Eclipse](http://www.eclipse.org/) (make sure to download from the website)
-   Create a new project with existing sources
-   Create the project folder
-   Add the ONS Source folder
-   Build the project using the JAR file. For this you must set the Main.java and press Build Main Project
-   Make sure the xml file for simulation is located in the project folder

## How to execute

_you@computer:~$ java -jar ONS.jar_  
_Usage: EONSim -f <simulation_file> -s <seed> [-trace] [-ra <raClass>] [ [-l <load>] or [-L <minload maxload step>] ] [-verbose | -table | -json]_

The required parameters are:

-   **simulation_file**: the xml file which contains the parameters for the simulation;
-   **seed**: This is a number in the range [1-25] that defines 25 different internally chosen seed sets to maximize the quality of the random sequences used. To generate confidence interval results in the same simulation, it is necessary to run with different seed sets.

The optional parameters are:

-   **trace**: define how the trace file is generated;
-   **ra**: set a different routing algorithm from the ONS RA Classes instead of the pre-established;
-   **load**: change the loadwork in the simulation;
-   **minload** **maxload** **step**: allows the automation of several executions of one same simulation for loads in range of an established interval [minload, maxload] or in increments [step];
-   **verbose**: set the way the on-screen message generation will work;
-   **table**: set if the result will be shown in a table;
-   **json**: set if the result will be generated as a json output file

## Execution examples

_you@computer:~$ java -jar ONS.jar nsfnet.xml 1 > out.txt_

**In this example, the simulator will perform the simulation described in the file ”nsfnet.xml” using the first seed group.**

**The load is unique and is defined in the XML file.**

_you@computer:~$ java -jar ONS.jar nsfnet.xml 1 -trace – verbose > out.txt_

**In this example, the simulator will record all data in a trace file (named in XML) and print complete simulation information.**

_you@computer:~$ java -jar ONS.jar nsfnet.xml 2 100 200 20 > out.txt_

**In this example, the simulator will perform the simulation described in the ”nsfnet.xml” file using the second seed group for a load range in Erlang ranging from 100 to 200, with increments of 20, ie 6 times the simulation. D****escribed in the file with the loads [100, 120, 140, 160, 180, 200].**

All results will be written to a file named “out.txt”

## Output

**If you run a simple simulation, in the end it will print standard output with some statistics. Such as:**

-   Band Lock Rate (BBR)
-   Blocking Rate by Class Of Service (COS)
-   Number of optical paths (LPs) established in the network
-   Average percentage of transmitters available throughout the simulation
-   Number of transmitters per request (when considering data aggregation techniques)
-   Traffic (grooming) – A transmitter can travel more than a request
-   Average virtual hops per request
-   Average physical hops per request

**For the EON scenario the following metrics are added:**

-   Average percentage of spectrum available throughout the simulation
-   Average percentage of use by modulation

**Throughout the simulation all actions are recorded in a trace file if the trace flag has been triggered. The simulator will record the following data:**

-   Arrivals and departures of events
-   Optical path creation or removal
-   Accept or block call

**There are many other metrics not listed here.  You are also able to develop your own metrics.**

## Output example

#### Example output from a standard simulation in the WDM network scenario

    BR: 17.401%  
    BBR: 19.819853%  
    Called Blocked by COS (%)  
    BP-0 13.872051%  
    BP-1 17961311%  
    BP-2 23.277143%
    
    LPs: 52831  
    Available Transponders: 35.11159896850586%  
    Transponders per request: 0.6396082277025146  
    Virtual Hops per request: 1.0  
    Physical Hops per request: 2.2926790881245536

#### Example output from a standard simulation in the EON network scenario

    BR: 4.4059997%  
    BBR: 5.2949085%  
    Called Blocked by COS (%)  
    BP-0 3.804479%  
    BP-1 5.0312014%  
    BP-2 5.875493%
    
    LPs: 83503  
    Available Transponders: 18.150667190551758%  
    Transponders per request: 0.878110080551875  
    Virtual Hops per request: 1.0  
    Physical Hops per request: 2.561959745094328  
    Spectrum Available: 78.775%  
    BPSK Modulation used: 18.195753%  
    QPSK Modulation used: 45.88458%  
    8QAM Modulation used: 26.916399%  
    16QAM Modulation used: 9.003269%

## Trace example

```console
flow-arrived 0.01943150511955905 0 6 9 10000 0 1  
lightpath-created 0 6 9 24_0 26_0 34_0 31_0  
flow-accepted – 0 6 9 10000 0 1 0  
flow-arrived 0.028230462306205267 1 11 7 10000 0 1  
lightpath-created 1 11 7 38_0 35_0 27_0  
flow-accepted – 1 11 7 10000 0 1 1  
flow-arrived 0.049388013572842365 2 5 2 10000 0 1  
lightpath-created 2 5 2 11_0  
flow-accepted – 2 5 2 10000 0 1 2  
flow-arrived 0.06355296782039067 3 13 12 10000 0 1  
lightpath-created 3 13 12 33_0 30_0  
flow-accepted – 3 13 12 10000 0 1 3  
flow-arrived 0.06975521562545164 4 2 6 10000 0 1  
lightpath-created 4 2 6 10_0 17_0 18_0  
flow-accepted – 4 2 6 10000 0 1 4  
flow-arrived 0.07339832232143151 5 2 0 10000 0 1  
lightpath-created 5 2 0 3_0  
flow-accepted – 5 2 0 10000 0 1 5  
flow-arrived 0.07363968525302261 6 7 5 10000 0 1  
lightpath-created 6 7 5 25_0 19_0 16_0  
flow-accepted – 6 7 5 10000 0 1 6  
flow-arrived 0.08197054287260293 7 4 13 10000 0 1  
lightpath-created 7 4 13 18_1 24_1 26_1 36_1  
flow-accepted – 7 4 13 10000 0 1 7  
flow-departed 0.08282859807576376 4 – – – – –  
lightpath-removed 4 2 6 10_0 17_0 18_0
```

## XML sample for EON/WDM simulations

Listed below are the xml examples that describe the different scenarios/topologies for the simulator. Note: There are several parameters that are configured within this file. More details in the comments of each file.

| Scenario | XML Link |
| ------ | ------ |
| EON-General| [xml-file](https://github.com/lucasrodri/ons/blob/main/xml/xml-default/USA.xml) |
| EON-BULK-BATCH| [xml-file](https://github.com/lucasrodri/ons/blob/main/xml/EON-BULK-BATCH/USA.xml) |
| EON-RMLSA| [xml-file](https://github.com/lucasrodri/ons/blob/main/xml/EON-RMLSA/USA.xml) |
| EON-RSA| [xml-file](https://github.com/lucasrodri/ons/blob/main/xml/EON-RSA/USA.xml) |
| EON-SDM| [xml-file](https://github.com/lucasrodri/ons/blob/main/xml/EON-SDM/USA.xml) |
| EON-SNRaware| [xml-file](https://github.com/lucasrodri/ons/blob/main/xml/EON-SNRaware/USA.xml) |
| EON-BULK-BATCH| [xml-file](https://github.com/lucasrodri/ons/blob/main/xml/EON-BULK-BATCH/USA.xml) |
| WDM| [xml-file](https://github.com/lucasrodri/ons/blob/main/xml/WDM/USA.xml) |

## XML sample topologies 

| Topology name| XML sample | Image |
| ------ | ------ | ------ |
| 8NodesClick | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/8NodesClick.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/8NodesClick.png)|
| 8NodesRing | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/8NodesRing.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/8NodesRing.png)|
| COST239 | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/COST239.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/COST239.png)|
| GERMAN | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/GERMAN.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/GERMAN.pdf)|
| LargePacificBell | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/LargePacificBell.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/LargePacificBell.png)|
| Manhattan4x4 | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/Manhattan4x4.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/Manhattan4x4.png)|
| Manhattan5x5 | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/Manhattan5x5.xml) | |
| MediumPacificBell | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/MediumPacificBell.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/MediumPacificBell.png)|
| NSFNET_21 | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/NSFNET_21.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/NSFNET_21.png)|
| NSFNET_22 | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/NSFNET_22.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/NSFNET_22.png)|
| PANEURO | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/PANEURO.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/PANEURO.pdf)|
| SmallPacificBell | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/SmallPacificBell.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/SmallPacificBell.png) |
| USA | [xml-file](https://github.com/lucasrodri/ons/blob/main/Topologies/xml-sample/USA.xml) | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/USA.pdf) |
| Japan| | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/Japan.png) |
| France| | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/France.png) |
| Italy| | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/Italy.png) |
| Finalnd| | [image-file](https://github.com/lucasrodri/ons/blob/main/Topologies/images/Finalnd.png) |



## Routing Algorithms Interface

These are the methods that RWA/RSA/RMLSA algorithms must implement:

-   _**public void simulationInterface(ControlPlaneForRA cp)**_ : This method is responsible for providing access to all relevant simulation information, including information about physical and virtual topologies. In addition, it provides a feedback interface to the control plan.

-   _**public void flowArrival(Flow flow)**:_ Whenever a new call arrives on the network the control plan executes this method, informing the algorithm of the arrival of a new event and providing all relevant information about this new call.

-   _**public void flowDeparture(long id)**_: Whenever a call comes from the network, this method is called.

Your new algorithm will be able to interact with the control plane through a specific interface that provides some key methods.

```java
public interface ControlPlaneForRA {
    public boolean acceptFlow(long id, LightPath[] lightpaths);
    public boolean acceptFlow(long id, Path[] paths, int[] bws);
    public boolean acceptFlow(long id, Path[] primaryPaths, int[] primaryBWS, Path[] backupPaths, int[] backupBWS, boolean reservedProtect, boolean shared);
    public boolean blockFlow(long id);
    public boolean acceptBulkData(long id, LightPath[] lightpaths, int rate);
    public boolean blockBulkData(long id);
    public boolean acceptBatch(long id, LightPath[][] lightpaths, int[] rate); 
    public boolean blockBatch(long id);
    public boolean rerouteFlow(long id, LightPath[] lightpaths);
    public Flow getFlow(long id); 
    public Path getPath(Flow flow); 
    public MultiPath getMultiPath(Flow flow);  
    public int getLightpathFlowCount(long id);
    public Map<Flow, Path> getMappedFlowsSinglePath(); 
    public Map<Flow, MultiPath> getMappedFlowsMultiPath();  
    public Map<Flow, MultiPathProtect> getMappedFlowsMultiPathProtect();
    public PhysicalTopology getPT();   
    public VirtualTopology getVT();   
    public double getCurrentTime();   
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths);    
    public WDMLightPath createCandidateWDMLightPath(int src, int dst, int[] links, int[] wavelengths, String typeProtection);   
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation);    
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int[] cores, int firstSlot, int lastSlot, int modulation);    
    public EONLightPath createCandidateEONLightPath(int src, int dst, int[] links, int firstSlot, int lastSlot, int modulation, String typeProtection);    
    public boolean addStaticLightPathProtect(Path[] primaryPaths, Path[] backupPaths, boolean reservedProtect);
    public boolean addStaticLightPathProtect(LightPath[] lightpaths, boolean bakcup, boolean reservedProtect);
    public Flow[] getFlows(LightPath lightpath);
}
```

On each call, your algorithm can either accept (via the acceptFlow method) or block (via the blockFlow method) this call. In addition, the algorithm can call the other methods to aid in its procedures.

## JavaDoc

For details, see <a href="https://ons-simulator.com/javadoc" target="_blank">JavaDoc</a>


## WebSite

For details, see <a href="https://ons-simulator.com/" target="_blank">https://ons-simulator.com/</a>




