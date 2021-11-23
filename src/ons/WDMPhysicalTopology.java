package ons;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ons.util.WeightedGraph;

public class WDMPhysicalTopology extends PhysicalTopology {

    protected int wavelengths;

    public WDMPhysicalTopology(Element xml) {
        super(xml);

        int id, src, dst, bw, type, group;
        int groomingInPorts, groomingOutPorts, wvlConverters, wvlConversionRange;
        double delay, weight, pc;

        try {
            this.wavelengths = Integer.parseInt(xml.getAttribute("wavelengths"));
            // Process nodes
            NodeList nodelist = xml.getElementsByTagName("node");
            nodes = nodelist.getLength();
            if (Simulator.verbose) {
                System.out.println(Integer.toString(nodes) + " nodes");
            }
            nodeVector = new WDMOXC[nodes];
            // <node id="0" grooming-in-ports="16" grooming-out-ports="16" wlconverters="4" wlconversion-range="2"/>
            for (int i = 0; i < nodes; i++) {
                id = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("id"));
                groomingInPorts = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("grooming-in-ports"));
                groomingOutPorts = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("grooming-out-ports"));
                
                if(((Element) nodelist.item(i)).hasAttribute("type")) {
                    type = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("type"));
                } else {
                    type = 0; //dont have a type, convetional type
                }
                if(((Element) nodelist.item(i)).hasAttribute("group")) {
                    group = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("group"));
                } else {
                    group = -1; //dont have a group
                }
                
                if(((Element) nodelist.item(i)).hasAttribute("wlconverters")) {
                    wvlConverters = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("wlconverters"));
                } else {
                    wvlConverters = 0;
                }
                if(((Element) nodelist.item(i)).hasAttribute("wlconversion-range")) {
                    wvlConversionRange = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("wlconversion-range"));
                } else {
                    wvlConversionRange = 0;
                }
                nodeVector[id] = new WDMOXC(id, groomingInPorts, groomingOutPorts, type, group, wvlConverters, wvlConversionRange);
            }

            // Process links   
            NodeList linksEntities = xml.getElementsByTagName("links");
            int defaultBw = 0;
            double defaultPC = 0;
            if (((Element) linksEntities.item(0)).hasAttribute("bandwidth")) {
                defaultBw = Integer.parseInt(((Element) linksEntities.item(0)).getAttribute("bandwidth"));
            } else {
                throw (new IllegalArgumentException("General bandwidth is not set. Check the xml."));
            }
            if (((Element) linksEntities.item(0)).hasAttribute("PC")) {
                defaultPC = Double.parseDouble(((Element) linksEntities.item(0)).getAttribute("PC"));
            } else {
                throw (new IllegalArgumentException("General PC is not set. Check the xml."));
            }
            NodeList linklist = xml.getElementsByTagName("link");
            links = linklist.getLength();
            if (Simulator.verbose) {
                System.out.println(Integer.toString(links) + " links");
            }
            linkVector = new WDMLink[links];
            adjMatrix = new WDMLink[nodes][nodes];
            for (int i = 0; i < links; i++) {
                id = Integer.parseInt(((Element) linklist.item(i)).getAttribute("id"));
                src = Integer.parseInt(((Element) linklist.item(i)).getAttribute("source"));
                dst = Integer.parseInt(((Element) linklist.item(i)).getAttribute("destination"));
                delay = Double.parseDouble(((Element) linklist.item(i)).getAttribute("delay"));
                bw = defaultBw;
                pc = defaultPC;
                weight = Double.parseDouble(((Element) linklist.item(i)).getAttribute("weight"));
                linkVector[id] = adjMatrix[src][dst] = new WDMLink(id, src, dst, delay, weight, wavelengths, bw, pc);
            }
            
            // Set node degree
            WeightedGraph g = getWeightedGraph();
            for (int i = 0; i < nodeVector.length; i++) {
                nodeVector[i].setNodeDegree(g.neighbors(i).length);
            }
            
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    /**
     * Retrieves the number of wavelengths in a given PhysicalTopology.
     *
     * @return the value of the PhysicalTopology's wavelengths attribute
     */
    public int getNumWavelengths() {
        return wavelengths;
    }

    @Override
    public void createPhysicalLightpath(LightPath lightpath) {

        int wvl1, wvl2;
        // Reserve wvl converters
        wvl1 = ((WDMLightPath) lightpath).getWavelengths()[0];
        for (int i = 1; i < ((WDMLightPath) lightpath).getWavelengths().length; i++) {
            wvl2 = ((WDMLightPath) lightpath).getWavelengths()[i];
            if (wvl1 != wvl2) { // If changed the wvl
                ((WDMOXC) this.getNode(this.getLink(lightpath.links[i]).getSource())).reserveWvlConverter();
                if (i < ((WDMLightPath) lightpath).getWavelengths().length - 1) { // If it is not the last link 
                    ((WDMOXC) this.getNode(this.getLink(lightpath.links[i]).getDestination())).reserveWvlConverter();
                }
                wvl1 = wvl2;
            }
        }
        //Set the transponder used in this lp and Reserve ports
        lightpath.setTx(this.getNode(this.getLink(lightpath.links[0]).getSource()).reserveGroomingInputPort());
        lightpath.setRx(this.getNode(this.getLink(lightpath.links[((WDMLightPath) lightpath).getWavelengths().length - 1]).getDestination()).reserveGroomingOutputPort());
        // Reserve wvls
        for (int i = 0; i < lightpath.links.length; i++) {
            ((WDMLink) this.getLink(lightpath.links[i])).reserveWavelength(((WDMLightPath) lightpath).getWavelengths()[i]);
        }
    }

    @Override
    public void removePhysicalLightpath(LightPath lightpath) {
        int wvl1, wvl2;
        // Release wvl converters
        wvl1 = ((WDMLightPath) lightpath).getWavelengths()[0];
        for (int i = 1; i < ((WDMLightPath) lightpath).getWavelengths().length; i++) {
            wvl2 = ((WDMLightPath) lightpath).getWavelengths()[i];
            if (wvl1 != wvl2) { // If changed the wvl
                ((WDMOXC) this.getNode(this.getLink(lightpath.links[i]).getSource())).releaseWvlConverter();
                if (i < ((WDMLightPath) lightpath).getWavelengths().length - 1) { // If it is not the last link 
                    ((WDMOXC) this.getNode(this.getLink(lightpath.links[i]).getDestination())).releaseWvlConverter();
                }
                wvl1 = wvl2;
            }
        }
        // Release ports
        this.getNode(this.getLink(lightpath.links[0]).getSource()).releaseGroomingInputPort(lightpath.Tx);
        this.getNode(this.getLink(lightpath.links[lightpath.links.length - 1]).getDestination()).releaseGroomingOutputPort(lightpath.Rx);
        // Release wvls
        for (int i = 0; i < lightpath.links.length; i++) {
            ((WDMLink) this.getLink(lightpath.links[i])).releaseWavelength(((WDMLightPath) lightpath).getWavelengths()[i]);
        }

    }

    @Override
    public boolean canCreatePhysicalLightpath(LightPath lightpath) {
        int wvl1, wvl2, d, src, dst;
        // Available wvl converters and range
        wvl1 = ((WDMLightPath) lightpath).getWavelengths()[0];
        for (int i = 1; i < ((WDMLightPath) lightpath).getWavelengths().length; i++) {
            wvl2 = ((WDMLightPath) lightpath).getWavelengths()[i];
            if (wvl1 != wvl2) { // If changed the wvl
                d = Math.max(wvl1, wvl2) - Math.min(wvl1, wvl2);
                src = getLink(lightpath.links[i]).getSource();
                if (!((WDMOXC) getNode(src)).hasFreeWvlConverters() || (d > ((WDMOXC) getNode(src)).getWvlConversionRange())) {
                    return false;
                }
                if (i < ((WDMLightPath) lightpath).getWavelengths().length - 1) { // If it is not the last link 
                    dst = getLink(lightpath.links[i]).getDestination();
                    if (!((WDMOXC) getNode(dst)).hasFreeWvlConverters() || (d > ((WDMOXC) getNode(dst)).getWvlConversionRange())) {
                        return false;
                    }
                }
                wvl1 = wvl2;
            }
        }
        // Available wvl transceivers
        if (!((WDMOXC) getNode(getLink(lightpath.links[0]).getSource())).hasFreeGroomingInputPort()) {
            return false;
        }
        if (!((WDMOXC) getNode(getLink(lightpath.links[lightpath.links.length - 1]).getDestination())).hasFreeGroomingOutputPort()) {
            return false;
        }
        // Available wavelengths
        for (int i = 0; i < lightpath.links.length; i++) {
            // FIXME: Felipe, to tentando entender o vc fez com esse metodo abaixo
            //ele existia no WDMSim 1.0
            if (!((WDMLink) getLink(lightpath.links[i])).isWLAvailable(((WDMLightPath) lightpath).getWavelengths()[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getBWAvailable(LightPath lightpath) {
        int aux, bw = Integer.MAX_VALUE;
        for (int i = 0; i < lightpath.links.length; i++) {
            aux = ((WDMLink) getLink(lightpath.links[i])).amountBWAvailable(((WDMLightPath) lightpath).getWavelengths()[i]);
            if (aux < bw) {
                bw = aux;
            }
        }
        return bw;
    }

    @Override
    public void addRate(int rate, LightPath lightpath) {
        for (int i = 0; i < lightpath.links.length; i++) {
            ((WDMLink) getLink(lightpath.links[i])).addTraffic(((WDMLightPath) lightpath).getWavelengths()[i], rate);
        }
    }

    @Override
    public int getBW(LightPath lightpath) {
        return ((WDMLink) getLink(lightpath.links[0])).getBandwidth();
    }
    
    /**
     * Retrieves the number of wavelength converters a given LightPath object
     * uses.
     *
     * @param lightpath the LightPath object
     * @return the number of converters the lightpath uses
     */
    public int usedConverters(WDMLightPath lightpath) {
        int[] wvls = lightpath.getWavelengths();
        int numConv = 0;
        int wvl = wvls[0];

        for (int i = 1; i < wvls.length; i++) {
            if (wvl != wvls[i]) {
                numConv++;
                wvl = wvls[i];
            }
        }
        return numConv;
    }
    
    /**
     * Returns a weighted graph with vertices representing the physical network
     * nodes, and the edges representing the physical links.
     *
     * The weight of each edge receives the same value of the original link
     * weight if the wavelength wvl in that link has at least bw Mbps of
     * bandwidth available. Otherwise it has no edges.
     *
     * @param wvl the wavelength id
     * @param bw the amount of bandwidth to be established
     * @return an WeightedGraph class object
     */
    public WeightedGraph getWeightedGraph(int wvl, int bw) {
        WDMLink link;
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (hasLink(i, j)) {
                    link = (WDMLink) getLink(i, j);
                    if (link.amountBWAvailable(wvl) >= bw) {
                        g.addEdge(i, j, link.getWeight());
                    }
                }
            }
        }
        return g;
    }

    @Override
    public boolean canAddRate(int rate, LightPath lightpath) {
        // Test the availability of resources
        for (int i = 0; i < lightpath.links.length; i++) {
            if (((WDMLink) getLink(lightpath.links[i])).amountBWAvailable(((WDMLightPath) lightpath).getWavelengths()[i]) < rate) {
                return false;
            }
        }
        return true;
    }     

    @Override
    public void removeRate(int rate, LightPath lightpath) {
        for (int i = 0; i < lightpath.links.length; i++) {
            ((WDMLink) getLink(lightpath.links[i])).removeTraffic(((WDMLightPath) lightpath).getWavelengths()[i], rate);
        }
    }
}
