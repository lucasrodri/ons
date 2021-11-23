/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import java.io.File;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Centralizes the simulation execution. Defines what the command line arguments
 * do, and extracts the simulation information from the XML file.
 *
 * @author lucasrc
 */
public class Simulator {

    private static String simName;
    private static final Float simVersion = (float) 3.0;
    public static boolean verbose = false;
    public static boolean table = false;
    public static boolean json = false;
    public static boolean trace = false;
    public static int simType;

    /**
     * Executes simulation based on the given XML file and the used command line
     * arguments.
     *
     * @param simConfigFile name of the XML file that contains all information
     * about the simulation
     * @param raClass raClass name if is set
     * @param trace activates the Tracer class functionalities
     * @param verbose activates the printing of information about the
     * simulation, on runtime, for debugging purposes
     * @param table activates the printing of information about the simulation
     * in table format
     * @param json activates the printing of information about the simulation
     * in json format
     * @param forcedLoad range of loads for which several simulations are
     * automated; if not specified, load is taken from the XML file
     * @param seed a number in the interval [1,25] that defines up to 25
     * different random simulations
     */
    public void Execute(String simConfigFile, String raClass, boolean trace, boolean verbose, boolean table, boolean json, double forcedLoad, int seed) {

        Simulator.verbose = verbose;
        Simulator.table = table;
        Simulator.json = json;
        Simulator.trace = trace;
        Main.load = forcedLoad;

        if (Simulator.verbose) {
            System.out.println("########################################################");
            System.out.println("# Optical Networks Simulator - version " + simVersion.toString() + "  #");
            System.out.println("#######################################################\n");
        }

        try {

            long begin = System.currentTimeMillis();

            if (Simulator.verbose) {
                System.out.println("(0) Accessing simulation file " + simConfigFile + "...");
            }
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(simConfigFile));

            // normalize text representation
            doc.getDocumentElement().normalize();
            
            if (!doc.getDocumentElement().hasAttribute("type")) {
                System.out.println("Cannot find type attribute! [wdmsim | eonsim | eonsimBulk]");
                System.exit(0);
            } else {
                // check the root TAG name and version
                simType = -1;
                simName = doc.getDocumentElement().getAttribute("type");
                switch (simName) {
                    case "wdmsim":
                        if (Simulator.verbose) {
                            System.out.println("Simulation type: " + simName + " (Fixed)");
                        }
                        simType = 0;
                        break;
                    case "eonsim":
                        if (Simulator.verbose) {
                            System.out.println("Simulation type: " + simName + " (Elastic)");
                        }
                        simType = 1;
                        break;
                    case "eonsimBulk":
                        if (Simulator.verbose) {
                            System.out.println("Simulation type: " + simName + " (Elastic with Bulks)");
                        }
                        simType = 2;
                        break;
                    default:
                        System.out.println("Root element of the simulation file is " + doc.getDocumentElement().getNodeName() + ", wdmsim, eonsim or eonsimBulk is expected!");
                        System.exit(0);
                }
            }
            
            if (!doc.getDocumentElement().hasAttribute("version")) {
                System.out.println("Cannot find version attribute!");
                System.exit(0);
            }
            if (Float.compare(new Float(doc.getDocumentElement().getAttribute("version")), simVersion) != 0) {
                System.out.println("The simulation configuration file is not compatible with the simulator version!");
                System.exit(0);
            }
            if (Simulator.verbose) {
                System.out.println("(0) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            /*
             * Extract physical topology part
             */
            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(1) Loading physical topology information...");
            }

            PhysicalTopology pt;
            PhysicalImpairments pi = null;
            Modulation mod = null;
            
            if (simType == 0) {
                pt = new WDMPhysicalTopology((Element) doc.getElementsByTagName("physical-topology").item(0));
            } else {
                if (((Element) doc.getElementsByTagName("physical-impairment").item(0)) == null) {
                    pi = PhysicalImpairments.getPhysicalImpairmentsObject();
                    mod = Modulation.getModulationObject();
                } else {
                    pi = PhysicalImpairments.getPhysicalImpairmentsObject((Element) doc.getElementsByTagName("physical-impairment").item(0));
                    mod = Modulation.getModulationObject((Element) doc.getElementsByTagName("physical-impairment").item(0));
                }
                pt = new EONPhysicalTopology((Element) doc.getElementsByTagName("physical-topology").item(0), pi);
                pi.setPT(pt);
            }

            if (Simulator.verbose) {
                System.out.println(pt);
            }

            if (Simulator.verbose) {
                System.out.println("(1) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            /*
             * Extract simulation traffic part
             */
            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(2) Loading traffic information...");
            }

            EventScheduler events = new EventScheduler();
            TrafficGenerator traffic = new TrafficGenerator((Element) doc.getElementsByTagName("traffic").item(0), forcedLoad, pt);
            //For eonsimBulk
            if(simType == 2) {
                traffic.generateTraffic_eonsimBulk(pt, events, seed);
            } else {
                traffic.generateTraffic(pt, events, seed);
            }

            if (Simulator.verbose) {
                System.out.println("(2) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            /*
             * Extract simulation setup part
             */
            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(3) Loading simulation setup information...");
            }
            int numberOfCOS = 1;
            if (((Element) doc.getElementsByTagName("traffic").item(0)).hasAttribute("cos")) {
                numberOfCOS = Integer.parseInt(((Element) doc.getElementsByTagName("traffic").item(0)).getAttribute("cos"));
            }
            int statisticStart = 0;
            if (((Element) doc.getElementsByTagName("traffic").item(0)).hasAttribute("statisticStart")) {
                statisticStart = Integer.parseInt(((Element) doc.getElementsByTagName("traffic").item(0)).getAttribute("statisticStart"));
            }

            /*
            Create the trace object
            */
            Tracer tr = Tracer.getTracerObject();

            if (forcedLoad == 0) {
                forcedLoad = Main.load;
            }

            if (Simulator.trace == true) {
                if (((NodeList) doc.getElementsByTagName("trace")).getLength() != 0) {
                    tr.setTraceFile(simConfigFile.substring(0, simConfigFile.length() - 4) + "_Load_" + Double.toString(forcedLoad) + ".trace");
                }
            }
            tr.toogleTraceWriting(Simulator.trace);
            if (Simulator.verbose) {
                System.out.println("(3) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            /*
             * Extract virtual topology part
             */
            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(4) Loading virtual topology information...");
            }

            VirtualTopology vt = new VirtualTopology((Element) doc.getElementsByTagName("virtual-topology").item(0), pt);
            if (simType >= 1 && pi != null) {
                pi.setVT(vt);
            }
            if (Simulator.verbose) {
                System.out.println(vt);
            }
            if (Simulator.verbose) {
                System.out.println("(4) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            /*
            Create the statistics object
            */
            MyStatistics st = MyStatistics.getMyStatisticsObject();
            st.statisticsSetup(pt, vt, numberOfCOS, statisticStart);
            
            /*
             * Extract control plane part
             */
            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(5) Loading Control Plane information...");
            }
            
            String raPacket;
            
            if (doc.getDocumentElement().hasAttribute("raPacket")) {
                raPacket = doc.getDocumentElement().getAttribute("raPacket");
            } else {
                raPacket = "ra";
            }
            
            String raModule;
            if (raClass.equals("")) {
                raClass = ((Element) doc.getElementsByTagName("ra").item(0)).getAttribute("module");
                raModule = "ons."+ raPacket +"." + raClass;
            } else {
                raModule = "ons."+ raPacket +"." + raClass;
            }
            if (Simulator.verbose) {
                System.out.println("RA module: " + raModule);
            }
            ControlPlane cp = null;
            if(simType == 2) {
                cp = new ControlPlane(raModule, pt, vt, events);
            } else {
                cp = new ControlPlane(raModule, pt, vt);
            }
            if (Simulator.verbose) {
                System.out.println("(5) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            /*
             * Run the simulation
             */
            begin = System.currentTimeMillis();
            if (Simulator.verbose) {
                System.out.println("(6) Running the simulation...");
            }

            SimulationRunner sim = new SimulationRunner(cp, events);

            if (Simulator.verbose) {
                System.out.println("(6) Done. (" + Float.toString((float) ((float) (System.currentTimeMillis() - begin) / (float) 1000)) + " sec)\n");
            }

            if (Simulator.verbose) {
                System.out.println("Statistics for " + Double.toString(forcedLoad) + " erlangs (" + simConfigFile + "):\n");
                System.out.println(st.verboseStatistics(simType));
            } else {
                if (table) {
                    st.tableStatistics(simType, forcedLoad);
                } else {
                    if (json) {
                        st.jsonStatistics(simType, forcedLoad);
                    } else {
                        System.out.print("*** RA Module: " + raClass + " *** ");
                        System.out.print("VT Name: " + vt.getName() + " *** ");
                        System.out.println("Calls: " + traffic.getCalls() + " *** ");
                        if (forcedLoad != 0) {
                            System.out.println("Load:" + Double.toString(forcedLoad));
                        }
                        st.printStatistics(simType);
                    }
                }
            }

            // Terminate MyStatistics singleton
            st.finish();

            // Flush and close the trace file and terminate the singleton
            if (Simulator.trace == true) {
                tr.finish();
            }

        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
