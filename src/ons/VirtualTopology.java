package ons;

import ons.util.WeightedGraph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.w3c.dom.*;

/**
 * The virtual topology is created based on a given Physical Topology and on the
 * lightpaths specified on the XML file.
 *
 * @author andred
 */
public class VirtualTopology {

    private String name;
    protected long nextLightpathID;
    protected TreeSet<LightPath>[][] adjMatrix;
    protected int adjMatrixSize;
    protected Map<Long, LightPath> lightPaths;
    protected PhysicalTopology pt;
    protected Tracer tr = Tracer.getTracerObject();
    protected MyStatistics st = MyStatistics.getMyStatisticsObject();
    private HashMap<Integer, ArrayList<LightPath>> lightpathsInLink;
    private PhysicalImpairments pi;

    private static class LightPathSort implements Comparator<LightPath> {

        @Override
        public int compare(LightPath lp1, LightPath lp2) {
            if (lp1.getID() < lp2.getID()) {
                return -1;
            }
            if (lp1.getID() > lp2.getID()) {
                return 1;
            }
            return 0;
        }
    }

    /**
     * Creates a new VirtualTopology object.
     *
     * @param xml file that contains all simulation information
     * @param pt Physical Topology of the network
     */
    @SuppressWarnings("unchecked")
    public VirtualTopology(Element xml, PhysicalTopology pt) {
        if (Simulator.simType > 0) {
            pi = PhysicalImpairments.getPhysicalImpairmentsObject();
        }
        int nodes, lightpaths;
        nextLightpathID = 1;
        lightPaths = new HashMap<Long, LightPath>();
        lightpathsInLink = new HashMap<>();
        for (int l = 0; l < pt.links; l++) {
            lightpathsInLink.put(l, new ArrayList<LightPath>());
        }

        try {
            this.pt = pt;
            if (Simulator.verbose) {
                System.out.println(xml.getAttribute("name"));
            }
            this.name = xml.getAttribute("name");

            adjMatrixSize = nodes = pt.getNumNodes();

            // Process lightpaths
            adjMatrix = new TreeSet[nodes][nodes];
            for (int i = 0; i < nodes; i++) {
                for (int j = 0; j < nodes; j++) {
                    if (i != j) {
                        adjMatrix[i][j] = new TreeSet<LightPath>(new LightPathSort());
                    }
                }
            }
            NodeList lightpathlist = xml.getElementsByTagName("lightpath");
            lightpaths = lightpathlist.getLength();
            if (Simulator.verbose) {
                System.out.println(Integer.toString(lightpaths) + " lightpath(s)");
            }
            if (lightpaths > 0) {
                int src, dst, links[], wavelengths[], firstSlot, lastSlot, modulation;
                String s[], reserved;
                LightPath lp;
                for (int i = 0; i < lightpaths; i++) {
                    reserved = "";
                    src = Integer.parseInt(((Element) lightpathlist.item(i)).getAttribute("src"));
                    dst = Integer.parseInt(((Element) lightpathlist.item(i)).getAttribute("dst"));
                    s = ((Element) lightpathlist.item(i)).getAttribute("links").split(",");
                    links = new int[s.length];
                    for (int j = 0; j < s.length; j++) {
                        links[j] = Integer.parseInt(s[j]);
                    }
                    if (((Element) lightpathlist.item(i)).hasAttribute("wavelengths")) {
                        s = ((Element) lightpathlist.item(i)).getAttribute("wavelengths").split(",");
                        wavelengths = new int[s.length];
                        for (int j = 0; j < s.length; j++) {
                            wavelengths[j] = Integer.parseInt(s[j]);
                        }
                        lp = new WDMLightPath(1, src, dst, links, wavelengths);
                    } else {
                        firstSlot = Integer.parseInt(((Element) lightpathlist.item(i)).getAttribute("firstSlot"));
                        lastSlot = Integer.parseInt(((Element) lightpathlist.item(i)).getAttribute("lastSlot"));
                        modulation = Modulation.convertModulationTypeToInteger(((Element) lightpathlist.item(i)).getAttribute("modulation"));
                        if (((Element) lightpathlist.item(i)).hasAttribute("reserved")) {
                            reserved = ((Element) lightpathlist.item(i)).getAttribute("reserved");
                        }
                        lp = new EONLightPath(1, src, dst, links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                        lp.reserved = Boolean.parseBoolean(reserved);
                    }
                    if (createLightpath(lp) < 0) {
                        throw (new IllegalArgumentException("Virtual Topology XML Error"));
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * First, creates a lightpath in the Physical Topology through the
     * createLightpathInPT function. Then, gets the lightpath's source and
     * destination nodes, so a new LightPath object can finally be created and
     * added to the lightPaths HashMap and to the adjMatrix TreeSet.
     *
     * @param lp the lightpath created by the User to test
     * @return -1 if LightPath object cannot be created, or its unique
     * identifier otherwise
     */
    public long createLightpath(LightPath lp) {
        long id;
        id = this.nextLightpathID;
        lp.setId(id);
        if (pt.getLink(lp.getLinks()[0]).getSource() != lp.getSource()
                || pt.getLink(lp.getLinks()[lp.getLinks().length - 1]).getDestination() != lp.getDestination()) {
            throw (new IllegalArgumentException());
        } else {
            if (!pt.canCreatePhysicalLightpath(lp)) {
                return -1;
            }
            pt.createPhysicalLightpath(lp);
            adjMatrix[lp.getSource()][lp.getDestination()].add(lp);
            lightPaths.put(nextLightpathID, lp);
            putLightpathInLinks(lp);
            tr.createLightpath(lp);
            st.createLightpath(lp);
            this.nextLightpathID++;
            SNRStatistics(lp);
            return id;
        }
    }

    public String getName() {
        return name;
    }
    
    /**
     * Checks and creates a lightpath with optical grooming over existing
     * lightpath (fully).
     *
     * @param lpCreated the lightpath created by the User to test
     * @param lpGroomable the lightpath will be groomable
     * @return -1 if LightPath object cannot be created, or its unique
     * identifier otherwise
     */
    public long createLightpathInOpticalGrooming(LightPath lpCreated, LightPath lpGroomable) {
        long id;
        //vai usar o canExtendLightpath da pt
        if (pt.getLink(lpCreated.getLinks()[0]).getSource() != lpCreated.getSource()
                || pt.getLink(lpCreated.getLinks()[lpCreated.getLinks().length - 1]).getDestination() != lpCreated.getDestination()) {
            throw (new IllegalArgumentException());
        }
        if (!lightPaths.containsKey(lpGroomable.id)) {
            throw (new IllegalArgumentException());
        } else {
            if (!((EONPhysicalTopology) pt).canExtendLightpath(lpCreated, lpGroomable, getTunnelSize(lpGroomable.getID()))) {
                return -1;
            }
            id = this.nextLightpathID;
            lpCreated.setId(id);

            lpCreated.Tx = lpGroomable.Tx;
            lpCreated.Rx = lpGroomable.Rx;

            ((EONPhysicalTopology) pt).createPhysicalLightpathInOpticalGrooming(lpCreated);

            adjMatrix[lpCreated.getSource()][lpCreated.getDestination()].add(lpCreated);
            lightPaths.put(nextLightpathID, lpCreated);
            putLightpathInLinks(lpCreated);
            tr.createLightpath(lpCreated);
            st.createLightpath(lpCreated);
            this.nextLightpathID++;
            SNRStatistics(lpCreated);
            return id;
        }
    }

    /**
     * Checks and creates a lightpath with optical grooming over existing
     * lightpath. The lightpath created will have the same modulation format of
     * the existing lightpath.
     *
     * @param path the path of the new lightpath
     * @param lpGroomable the lightpath will be groomable
     * @param rate the flow rate in Mbps
     * @return -1 if LightPath object cannot be created, or its unique
     * identifier otherwise
     */
    public long createLightpathInOpticalGrooming(ArrayList<Integer> path, LightPath lpGroomable, int rate) {
        if (path.size() < 1) {
            throw (new IllegalArgumentException());
        }
        if (!lightPaths.containsKey(lpGroomable.id)) {
            throw (new IllegalArgumentException());
        } else {

            int firstSlot, lastSlot, dstNode = 0;
            int modulation = ((EONLightPath) lpGroomable).getModulation();
            int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), modulation);

            int[] linksShared, linksNew, linkPath;
            long id;
            LightPath lpShared, lpCreated, lpNew = null;

            // Create the linkPath
            linkPath = new int[path.size() - 1];
            for (int j = 0; j < path.size() - 1; j++) {
                linkPath[j] = pt.getLink(path.get(j), path.get(j + 1)).getID();
            }
            int common;
            for (common = 0; common < linkPath.length && common < lpGroomable.links.length; common++) {
                if (linkPath[common] != lpGroomable.links[common]) {
                    break;
                }
            }
            if (common == 0) {
                throw (new IllegalArgumentException("First link is not in common"));
            }
            //FAZENDO OS LINKS DO SHARED E DO NEW
            // Create the links vector

            linksShared = new int[common];
            System.arraycopy(lpGroomable.links, 0, linksShared, 0, common);

            linksNew = new int[linkPath.length - common];
            System.arraycopy(linkPath, common, linksNew, 0, linkPath.length - common);
            //cria o lpShared da direira

            if (Arrays.equals(linksShared, lpGroomable.links) && linksNew.length == 0) {//entao eles estão totalmente um encima do outro
                firstSlot = ((EONLightPath) lpGroomable).getLastSlot() + 1;
                lastSlot = firstSlot + requiredSlots - 1;
                lpCreated = new EONLightPath(1, lpGroomable.getSource(), lpGroomable.getDestination(),
                        lpGroomable.links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                if ((id = createLightpathInOpticalGrooming(lpCreated, lpGroomable)) >= 0) {
                    return id;
                } else {//esquerda
                    lastSlot = ((EONLightPath) lpGroomable).getFirstSlot() - 1;
                    firstSlot = lastSlot - requiredSlots + 1;
                    lpCreated = new EONLightPath(1, lpGroomable.getSource(), lpGroomable.getDestination(),
                            lpGroomable.links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    return createLightpathInOpticalGrooming(lpCreated, lpGroomable);
                }
            } else {//entao ele ta parcialmente
                //se o linksNew.lenght == 0 ele nao estende e esta sobre a rota
                //se o linksNew.lenght > 0 ele estende ou bifurca a rota
                //testa ambos lps com ambos os lados: teste da direita
                firstSlot = ((EONLightPath) lpGroomable).getLastSlot() + 1;
                lastSlot = firstSlot + requiredSlots - 1;
                lpShared = new EONLightPath(1, lpGroomable.getSource(), pt.linkVector[lpGroomable.getLinks()[common - 1]].dst,
                        linksShared, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                if (linksNew.length > 0) {
                    lpNew = new EONLightPath(1, lpShared.getSource(), path.get(path.size() - 1),
                            linksNew, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    dstNode = lpNew.getDestination();
                } else {
                    dstNode = path.get(path.size() - 1);
                }
                //teste da direita
                //lpGroomable é o q existe 
                //lpShared é a parte no lpnovo que vai ser compartilhado
                //lpNew é a parte que vai ser bifurcada se for o caso
                if (!((EONPhysicalTopology) pt).canCreatePhysicalLightpathHybrid(lpShared, lpGroomable, getTunnelSize(lpGroomable.getID()), lpNew, dstNode)) {
                    //não deu pra direita, vamos tentar a esquerda
                    lastSlot = ((EONLightPath) lpGroomable).getFirstSlot() - 1;
                    firstSlot = lastSlot - requiredSlots + 1;
                    lpShared = new EONLightPath(1, lpGroomable.getSource(), pt.linkVector[lpGroomable.getLinks()[common - 1]].dst,
                            linksShared, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    if (linksNew.length > 0) {
                        lpNew = new EONLightPath(1, lpShared.getSource(), path.get(path.size() - 1),
                                linksNew, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                        dstNode = lpNew.getDestination();
                    } else {
                        dstNode = path.get(path.size() - 1);
                    }
                    //teste da esquerda
                    if (!((EONPhysicalTopology) pt).canCreatePhysicalLightpathHybrid(lpShared, lpGroomable, getTunnelSize(lpGroomable.getID()), lpNew, dstNode)) {
                        return -1;
                    }
                }
                id = this.nextLightpathID;
                lpShared.setId(id);
                if (lpNew != null) {
                    lpNew.setId(id);
                }
                lpShared.Tx = lpGroomable.Tx;
                lpCreated = ((EONPhysicalTopology) pt).createPhysicalLightpathHybrid(lpShared, lpNew, dstNode);

                adjMatrix[lpCreated.getSource()][lpCreated.getDestination()].add(lpCreated);
                lightPaths.put(nextLightpathID, lpCreated);
                putLightpathInLinks(lpCreated);
                tr.createLightpath(lpCreated);
                st.createLightpath(lpCreated);
                this.nextLightpathID++;
                SNRStatistics(lpCreated);
                return id;
            }
        }
    }

    /**
     * Usado na PLI!
     * Checks and creates a lightpath with optical grooming over existing lightpath.
     * The lightpath created will have the same modulation format of the existing lightpath.
     * 
     * @param path the path of the new lightpath
     * @param lpGroomable the lightpath will be groomable
     * @param rate the flow rate in Mbps
     * @param extendRight whether the tunnel will be extended to the right or left (to right if is true, to left if is false)
     * @return -1 if LightPath object cannot be created, or its unique
     * identifier otherwise
     */
    public long createLightpathInOpticalGrooming(ArrayList<Integer> path, LightPath lpGroomable, int rate, boolean extendRight) {
        if (path.size() < 1) {
            throw (new IllegalArgumentException());
        }
        if (!lightPaths.containsKey(lpGroomable.id)) {
            throw (new IllegalArgumentException());
        } else {

            int firstSlot, lastSlot, dstNode = 0;
            int modulation = ((EONLightPath) lpGroomable).getModulation();
            int requiredSlots = Modulation.convertRateToSlot(rate, EONPhysicalTopology.getSlotSize(), modulation);

            int[] linksShared, linksNew, linkPath;
            long id;
            LightPath lpShared, lpCreated, lpNew = null;

            // Create the linkPath
            linkPath = new int[path.size() - 1];
            for (int j = 0; j < path.size() - 1; j++) {
                linkPath[j] = pt.getLink(path.get(j), path.get(j + 1)).getID();
            }
            int common;
            for (common = 0; common < linkPath.length && common < lpGroomable.links.length; common++) {
                if (linkPath[common] != lpGroomable.links[common]) {
                    break;
                }
            }
            if (common == 0) {
                throw (new IllegalArgumentException("First link is not in common"));
            }
            //FAZENDO OS LINKS DO SHARED E DO NEW
            // Create the links vector

            linksShared = new int[common];
            System.arraycopy(lpGroomable.links, 0, linksShared, 0, common);

            linksNew = new int[linkPath.length - common];
            System.arraycopy(linkPath, common, linksNew, 0, linkPath.length - common);
            //cria o lpShared da direira

            if (Arrays.equals(linksShared, lpGroomable.links) && linksNew.length == 0) {//entao eles estão totalmente um encima do outro
                if (extendRight) {
                    firstSlot = ((EONLightPath) lpGroomable).getLastSlot() + 1;
                    lastSlot = firstSlot + requiredSlots - 1;
                    lpCreated = new EONLightPath(1, lpGroomable.getSource(), lpGroomable.getDestination(),
                            lpGroomable.links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    return createLightpathInOpticalGrooming(lpCreated, lpGroomable);
                } else {//esquerda
                    lastSlot = ((EONLightPath) lpGroomable).getFirstSlot() - 1;
                    firstSlot = lastSlot - requiredSlots + 1;
                    lpCreated = new EONLightPath(1, lpGroomable.getSource(), lpGroomable.getDestination(),
                            lpGroomable.links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    return createLightpathInOpticalGrooming(lpCreated, lpGroomable);
                }
            } else {//entao ele ta parcialmente
                //se o linksNew.lenght == 0 ele nao estende e esta sobre a rota
                //se o linksNew.lenght > 0 ele estende ou bifurca a rota
                //testa ambos lps com ambos os lados: teste da direita
                if (extendRight) {
                    firstSlot = ((EONLightPath) lpGroomable).getLastSlot() + 1;
                    lastSlot = firstSlot + requiredSlots - 1;
                    lpShared = new EONLightPath(1, lpGroomable.getSource(), pt.linkVector[lpGroomable.getLinks()[common - 1]].dst,
                            linksShared, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    if (linksNew.length > 0) {
                        lpNew = new EONLightPath(1, lpShared.getSource(), path.get(path.size() - 1),
                                linksNew, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                        dstNode = lpNew.getDestination();
                    } else {
                        dstNode = path.get(path.size() - 1);
                    }
                    //teste da direita
                    //lpGroomable é o q existe 
                    //lpShared é a parte no lpnovo que vai ser compartilhado
                    //lpNew é a parte que vai ser bifurcada se for o caso
                    if (!((EONPhysicalTopology) pt).canCreatePhysicalLightpathHybrid(lpShared, lpGroomable, getTunnelSize(lpGroomable.getID()), lpNew, dstNode)) {
                        return -1;
                    }
                } else {
                    //não é pra direita, vamos tentar a esquerda
                    lastSlot = ((EONLightPath) lpGroomable).getFirstSlot() - 1;
                    firstSlot = lastSlot - requiredSlots + 1;
                    lpShared = new EONLightPath(1, lpGroomable.getSource(), pt.linkVector[lpGroomable.getLinks()[common - 1]].dst,
                            linksShared, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    if (linksNew.length > 0) {
                        lpNew = new EONLightPath(1, lpShared.getSource(), path.get(path.size() - 1),
                                linksNew, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                        dstNode = lpNew.getDestination();
                    } else {
                        dstNode = path.get(path.size() - 1);
                    }
                    //teste da esquerda
                    if (!((EONPhysicalTopology) pt).canCreatePhysicalLightpathHybrid(lpShared, lpGroomable, getTunnelSize(lpGroomable.getID()), lpNew, dstNode)) {
                        return -1;
                    }
                }
                id = this.nextLightpathID;
                lpShared.setId(id);
                if (lpNew != null) {
                    lpNew.setId(id);
                }
                lpShared.Tx = lpGroomable.Tx;
                lpCreated = ((EONPhysicalTopology) pt).createPhysicalLightpathHybrid(lpShared, lpNew, dstNode);

                adjMatrix[lpCreated.getSource()][lpCreated.getDestination()].add(lpCreated);
                lightPaths.put(nextLightpathID, lpCreated);
                putLightpathInLinks(lpCreated);
                tr.createLightpath(lpCreated);
                st.createLightpath(lpCreated);
                this.nextLightpathID++;
                SNRStatistics(lpCreated);
                return id;
            }
        }
    }

    /**
     * First, removes a given lightpath in the Physical Topology through the
     * removeLightpathInPT function. Then, gets the lightpath's source and
     * destination nodes, to remove it from the lightPaths HashMap and the
     * adjMatrix TreeSet. If the lightpath is in optical grooming then your 'Tx'
     * is -1 for 'PhysicalTopology' don't release the input grooming port This
     * method should be used only by ControlPlane class
     *
     * @param id the unique identifier of the lightpath to be removed
     * @return true if operation was successful, or false otherwise
     */
    public boolean removeLightPath(long id) {
        LightPath lp;
        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!lightPaths.containsKey(id)) {
                return false;
            }
            lp = lightPaths.get(id);
            if (lightpathIsInOpticalGrooming(id)) {
                //verifica se ele nao e hibrido
                if (lightpathIsHybrid(id)) {
                    lp.Tx = -1;
                    ((EONPhysicalTopology) pt).removePhysicalLightpathHybrid(lp);
                } else {
                    lp.Tx = -1;
                    ((EONPhysicalTopology) pt).removePhysicalLightpathInOpticalGrooming(lp);
                }
            } else {
                pt.removePhysicalLightpath(lp);
            }

            removeLightpathInLinks(lp);
            lightPaths.remove(id);
            adjMatrix[lp.getSource()][lp.getDestination()].remove(lp);
            st.removeLightpath(lp);
            tr.removeLightpath(lp);
            return true;
        }
    }

    /**
     * This method serves to deallocate one lightpath that was not used to being
     * accepted in a request. This method should be used by the RA classes.
     *
     * @param id the unique identifier of the lightpath to be removed
     * @return true if operation was successful, or false otherwise
     */
    public boolean deallocatedLightpath(long id) {
        st.deallocatedLightpath(lightPaths.get(id));
        if (!isLightpathIdle(id)) {
            throw (new IllegalArgumentException());
        }
        LightPath lp;

        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!lightPaths.containsKey(id)) {
                return false;
            }
            lp = lightPaths.get(id);
            if (lightpathIsInOpticalGrooming(id)) {
                //verifica se ele nao e hibrido
                if (lightpathIsHybrid(id)) {
                    lp.Tx = -1;
                    ((EONPhysicalTopology) pt).removePhysicalLightpathHybrid(lp);
                } else {
                    lp.Tx = -1;
                    ((EONPhysicalTopology) pt).removePhysicalLightpathInOpticalGrooming(lp);
                }
            } else {
                pt.removePhysicalLightpath(lp);
            }

            removeLightpathInLinks(lp);
            lightPaths.remove(id);
            adjMatrix[lp.getSource()][lp.getDestination()].remove(lp);
            tr.deallocatedLightpath(lp);
            
            if (Simulator.simType > 0 && pi.isCheckQoT()) {
                if (pi.isSNRaware()) {
                    st.subSNRSurplus(((EONLightPath) lp).getModulation(), lp.getSnr());
                } else {
                    double snr = pi.computeSNRlightpath(lp);
                    st.subSNRSurplus(((EONLightPath) lp).getModulation(), snr);
                    if (!checkSNRLightpath(lp)) {
                        st.subSNR();
                    }
                }
            }
            return true;
        }
    }

    /**
     * This method serves to deallocate lightpaths that was not used to being
     * accepted in a request. This method should be used by the RA classes.
     *
     * @param lps the lightptahs set
     */
    public void deallocatedLightpaths(LightPath[] lps) {
        for (LightPath lp : lps) {
            if (isLightpathIdle(lp.getID())) {
                deallocatedLightpath(lp.getID());
            }
        }
    }

    /**
     * Removes a given lightpath from the Physical Topology and then puts it
     * back, but with a new route (set of links). The lightpath can't be in
     * optical grooming
     *
     * @param id the id of old lightpath
     * @param lp the new lightpath
     * @return true if operation was successful, or false otherwise
     */
    public boolean reprovisionLightPath(long id, LightPath lp) {
        LightPath old;
        lp.setId(id);
        if (!lightPaths.containsKey(id) && !lightpathIsInOpticalGrooming(id)) {
            return false;
        } else {
            old = lightPaths.get(id);
            pt.removePhysicalLightpath(old);
            if (!pt.canCreatePhysicalLightpath(lp)) {
                pt.createPhysicalLightpath(old);
                return false;
            }
            lightPaths.remove(id);
            removeLightpathInLinks(old);
            adjMatrix[old.getSource()][old.getDestination()].remove(old);
            tr.removeLightpath(old);

            pt.createPhysicalLightpath(lp);
            adjMatrix[lp.getSource()][lp.getDestination()].add(lp);
            lightPaths.put(id, lp);
            putLightpathInLinks(lp);
            tr.createLightpath(lp);
            st.reprovisionLightpath(old, lp);
            SNRStatistics(lp);
        }
        return true;
    }

    /**
     * This method serves to deallocate one lightpath that was not used to being
     * reroute in a request. This method should be used by the RA classes.
     *
     * @param id the unique identifier of the lightpath to be reprovisioned
     * @param OldLightpath the old lightpath before the reprovisioned
     * @return true if operation was successful, or false otherwise
     */
    public boolean deallocatedReprovisionLightPath(long id, LightPath OldLightpath) {
        st.deallocatedLightpath(lightPaths.get(id));
        if (!isLightpathIdle(id)) {
            throw (new IllegalArgumentException());
        }
        LightPath lp;
        if (id < 0) {
            throw (new IllegalArgumentException());
        } else {
            if (!lightPaths.containsKey(id)) {
                return false;
            }
            lp = lightPaths.get(id);
            if (lightpathIsInOpticalGrooming(id)) {
                //verifica se ele nao e hibrido
                if (lightpathIsHybrid(id)) {
                    lp.Tx = -1;
                    ((EONPhysicalTopology) pt).removePhysicalLightpathHybrid(lp);
                } else {
                    lp.Tx = -1;
                    ((EONPhysicalTopology) pt).removePhysicalLightpathInOpticalGrooming(lp);
                }
            } else {
                pt.removePhysicalLightpath(lp);
            }
            removeLightpathInLinks(lp);
            lightPaths.remove(id);
            adjMatrix[lp.getSource()][lp.getDestination()].remove(lp);
            tr.deallocatedLightpath(lp);

            if (!pt.canCreatePhysicalLightpath(OldLightpath)) {
                throw (new IllegalArgumentException());
            }
            pt.createPhysicalLightpath(OldLightpath);
            adjMatrix[OldLightpath.getSource()][OldLightpath.getDestination()].add(OldLightpath);
            lightPaths.put(id, OldLightpath);
            putLightpathInLinks(OldLightpath);
            tr.createLightpath(OldLightpath);
            st.createLightpath(OldLightpath);
            
            if (Simulator.simType > 0 && pi.isCheckQoT()) {
                if (pi.isSNRaware()) {
                    st.subSNRSurplus(((EONLightPath) lp).getModulation(), lp.getSnr());
                } else {
                    double snr = pi.computeSNRlightpath(lp);
                    st.subSNRSurplus(((EONLightPath) lp).getModulation(), snr);
                    if (!checkSNRLightpath(lp)) {
                        st.subSNR();
                    }
                }
            }
            return true;
        }
    }

    /**
     * Implements the push pull technique.
     *
     *
     * @param id the id of old lightpath
     * @param lp the new lightpath
     * @return true if operation was successful, or false otherwise
     */
    public boolean pushPullLightPath(long id, LightPath lp) {
        LightPath old;
        lp.setId(id);
        if (!lightPaths.containsKey(id) && !lightpathIsInOpticalGrooming(id)) {
            return false;
        } else {
            old = lightPaths.get(id);
            if (!freeSpecBetweenLps(old, lp)) {
                return false;
            }
            pt.removePhysicalLightpath(old);
            if (!pt.canCreatePhysicalLightpath(lp)) {
                pt.createPhysicalLightpath(old);
                return false;
            }
            lightPaths.remove(id);
            adjMatrix[old.getSource()][old.getDestination()].remove(old);
            tr.removeLightpath(old);
            pt.createPhysicalLightpath(lp);
            adjMatrix[lp.getSource()][lp.getDestination()].add(lp);
            lightPaths.put(id, lp);
            tr.createLightpath(lp);
            SNRStatistics(lp);
        }
        return true;
    }

    public boolean freeSpecBetweenLps(LightPath old, LightPath lp) {
        if (old.links.length != lp.links.length) {
            return false;
        } else {
            for (int i = 0; i < lp.links.length; i++) {
                int core = ((EONLightPath) lp).getCores()[i];
                if (old.links[i] != lp.links[i]) {
                    return false;
                } else if (old instanceof EONLightPath && lp instanceof EONLightPath) {
                    if (!((EONLink) pt.getLink(lp.links[i])).pushpull(core, ((EONLightPath) old).getFirstSlot(), ((EONLightPath) old).getLastSlot(), ((EONLightPath) lp).getFirstSlot(), ((EONLightPath) lp).getLastSlot())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Says whether or not a given LightPath object has a determined amount of
     * available bandwidth.
     *
     * @param src the lightpath's source node
     * @param dst the lightpath's destination node
     * @param bw required amount of bandwidth
     * @return true if lightpath is available
     */
    public boolean isLightpathAvailable(int src, int dst, int bw) {
        TreeSet<LightPath> lps = getLightpaths(src, dst);
        for (LightPath lp : lps) {
            if (getLightpathBWAvailable(lp.getID()) >= bw) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves a TreeSet with the Virtual Topology's available lightpaths.
     *
     * @param src the lightpath's source node
     * @param dst the lightpath's destination node
     * @param bw required amount of available bandwidth the lightpath must have
     * @return a TreeSet with the available lightpaths
     */
    public TreeSet<LightPath> getAvailableLightpaths(int src, int dst, int bw) {
        TreeSet<LightPath> lps = getLightpaths(src, dst);

        if (lps != null && !lps.isEmpty()) {
            Iterator<LightPath> it = lps.iterator();

            while (it.hasNext()) {
                if (getLightpathBWAvailable(it.next().getID()) < bw) {
                    it.remove();
                }
            }
            return lps;
        } else {
            return null;
        }
    }

    /**
     * Retrieves a TreeSet with the Virtual Topology's available lightpaths.
     * Only the primary lightpaths. Not backups
     *
     * @param src the lightpath's source node
     * @param dst the lightpath's destination node
     * @param bw required amount of available bandwidth the lightpath must have
     * @return a TreeSet with the available lightpaths
     */
    public TreeSet<LightPath> getAvailablePrimaryLightpaths(int src, int dst, int bw) {
        TreeSet<LightPath> lps = getLightpaths(src, dst);

        if (lps != null && !lps.isEmpty()) {
            Iterator<LightPath> it = lps.iterator();

            while (it.hasNext()) {
                LightPath lp = it.next();
                if (getLightpathBWAvailable(lp.getID()) < bw || lp.isBackup()) {
                    it.remove();
                }
            }
            return lps;
        } else {
            return null;
        }
    }

    /**
     * Retrieves a TreeSet with the Virtual Topology's available lightpaths.
     * Only the primary lightpaths. Not backups
     *
     * @param src the lightpath's source node
     * @param dst the lightpath's destination node
     * @param bw required amount of available bandwidth the lightpath must have
     * @param typeProtection type of protection
     * @return a TreeSet with the available lightpaths
     */
    public TreeSet<LightPath> getAvailablePrimaryLightpathsProtection(int src, int dst, int bw, String typeProtection) {
        TreeSet<LightPath> lps = getLightpaths(src, dst);

        if (lps != null && !lps.isEmpty()) {
            Iterator<LightPath> it = lps.iterator();

            while (it.hasNext()) {
                LightPath lp = it.next();
                if (getLightpathBWAvailable(lp.getID()) < bw || lp.isBackup() || !lp.getTypeProtection().equals(typeProtection)) {
                    it.remove();
                }
            }
            return lps;
        } else {
            return null;
        }
    }

    /**
     * Retrieves a TreeSet with the Virtual Topology's available lightpaths.
     * Only the backup lightpaths. Not backups
     *
     * @param src the lightpath's source node
     * @param dst the lightpath's destination node
     * @param bw required amount of available bandwidth the lightpath must have
     * @param typeProtection type of protection
     * @return a TreeSet with the available lightpaths
     */
    public TreeSet<LightPath> getAvailableBackupLightpathsProtection(int src, int dst, int bw, String typeProtection) {
        TreeSet<LightPath> lps = getLightpaths(src, dst);

        if (lps != null && !lps.isEmpty()) {
            Iterator<LightPath> it = lps.iterator();

            while (it.hasNext()) {
                LightPath lp = it.next();
                if (getLightpathBWAvailable(lp.getID()) < bw || !lp.isBackup() || !lp.getTypeProtection().equals(typeProtection)) {
                    it.remove();
                }
            }
            return lps;
        } else {
            return null;
        }
    }

    /**
     * Retrieves the number of active lightpaths in virtual topology.
     *
     * @return the number of active lightpaths in virtual topology
     */
    public int getNumberOfActiveLightPaths() {
        return lightPaths.size();
    }

    public Map<Long, LightPath> getLightPaths() {
        return lightPaths;
    }

    /**
     * Retrieves the number of active lightpaths in this source nodes.
     *
     * @param srcNodes the source nodes in virtual topology
     * @return the number of active lightpaths in in this nodes
     */
    public int getNumberOfActiveLightPaths(TreeSet<Integer> srcNodes) {
        Iterator<Entry<Long, LightPath>> itr = lightPaths.entrySet().iterator();
        int count = 0;
        while (itr.hasNext()) {
            Entry<Long, LightPath> next = itr.next();
            if (srcNodes.contains(next.getValue().getSource())) {
                count++;
            }
        }
        return count;
    }

    /**
     * Retrieves the available bandwidth of a given lightpath.
     *
     * @param id the lightpath's unique identifier
     * @return amount of available bandwidth
     */
    public int getLightpathBWAvailable(long id) {
        LightPath lp = lightPaths.get(id);
        return pt.getBWAvailable(lp);
    }

    /**
     * Retrieves the used bandwidth of a given lightpath.
     *
     * @param id the lightpath's unique identifier
     * @return amount of used bandwidth
     */
    public int getLightpathBWUsed(long id) {
        LightPath lp = lightPaths.get(id);
        return pt.getBW(lp) - pt.getBWAvailable(lp);
    }

    /**
     * Retrieves the bandwidth of a given lightpath.
     *
     * @param id the lightpath's unique identifier
     * @return amount of bandwidth
     */
    public int getLightpathBW(long id) {
        LightPath lp = lightPaths.get(id);
        return pt.getBW(lp);
    }

    /**
     * Says whether or not a given lightpath is idle, i.e., all its bandwidth is
     * available.
     *
     * @param id the lightpath's unique identifier
     * @return true if lightpath is idle, or false otherwise
     */
    public boolean isLightpathIdle(long id) {
        LightPath lp = lightPaths.get(id);
        int bwTotal = pt.getBW(lp);
        int bwAvailable = pt.getBWAvailable(lp);
        return bwTotal == bwAvailable;
    }

    /**
     * Says whether or not a given lightpath is full, i.e., all its bandwidth is
     * allocated.
     *
     * @param id the lightpath's unique identifier
     * @return true if lightpath is full, or false otherwise
     */
    public boolean isLightpathFull(long id) {
        return getLightpathBWAvailable(id) == 0;
    }

    /**
     * Retrieves a determined LightPath object from the Virtual Topology.
     *
     * @param id the lightpath's unique identifier
     * @return the required lightpath
     */
    public LightPath getLightpath(long id) {
        if (id < 0) {
            throw (new IllegalArgumentException());
        } else if (lightPaths.containsKey(id)) {
            return lightPaths.get(id);
        } else {
            return null;
        }
    }

    /**
     * Retrieves the TreeSet with all LightPath objects that belong to the
     * Virtual Topology.
     *
     * @param src the lightpath's source node
     * @param dst the lightpath's destination node
     * @return the TreeSet with all of the lightpaths
     */
    public TreeSet<LightPath> getLightpaths(int src, int dst) {
        return new TreeSet<>(adjMatrix[src][dst]);
    }

    /**
     * Retrieves the Lightpath Object that has this source, this destination and this firstSlotIndex
     * ** Only works in **eonsim** simulator
     * @param src the lightpath's source node 
     * @param dst the lightpath's destination node
     * @param firstSlotIndex the lightpath's first slot index
     * @return the Lightpath Object
     */
    public LightPath getLightpath(int src, int dst, int firstSlotIndex) {
        if(Simulator.simType < 1) {
            return null;
        }
        Iterator<LightPath> it = adjMatrix[src][dst].iterator();
        while (it.hasNext()) {
            LightPath lp = it.next();
            if (((EONLightPath) lp).getFirstSlot() == firstSlotIndex) {
                return lp;
            }
        }
        return null;
    }
    
    /**
     * Retrieves the LightPaths objects that belong in this lightpath in all
     * links.
     *
     * @param lp the lightpath
     * @return the ArrayList object with all lightpaths belong the lp links
     */
    public ArrayList<ArrayList<LightPath>> getLightpaths(LightPath lp) {
        ArrayList<ArrayList<LightPath>> lps = new ArrayList<>();
        for (int l = 0; l < lp.links.length; l++) {
            ArrayList<LightPath> lpsTemp = getLightpathsInLink(lp.links[l]);
            lpsTemp.remove(lp);
            lps.add(lpsTemp);
        }
        return lps;
    }

    /**
     * Retrieves the LightPaths objects that belong in this link.
     *
     * @param link the id link in Physical Topology
     * @return the ArrayList object with all lightpaths belong this link
     */
    public ArrayList<LightPath> getLightpathsInLink(int link) {
        return new ArrayList<>(lightpathsInLink.get(link));
    }

    /**
     * (Deprecated) Retrieves the LightPaths objects that belong in this link.
     *
     * @param link the id link in Physical Topology
     * @return the ArrayList object with all lightpaths belong this link
     */
    public ArrayList<LightPath> getLightpathsInLink2(int link) {
        ArrayList<LightPath> lps = new ArrayList<>();
        int[] idLinks;
        if (link < 0 || link > pt.links) {
            throw (new IllegalArgumentException());
        } else {
            Iterator<Entry<Long, LightPath>> itr1 = lightPaths.entrySet().iterator();
            while (itr1.hasNext()) {
                Entry<Long, LightPath> entry = itr1.next();
                idLinks = entry.getValue().getLinks();
                for (int i = 0; i < idLinks.length; i++) {
                    if (idLinks[i] == link) {
                        lps.add(entry.getValue());
                        break;
                    }
                }
            }
        }
        return lps;
    }

    private void putLightpathInLinks(LightPath lp) {
        int[] links = lp.getLinks();
        for (int i = 0; i < links.length; i++) {
            lightpathsInLink.get(links[i]).add(lp);
        }
    }

    private void removeLightpathInLinks(LightPath lp) {
        int[] links = lp.getLinks();
        for (int i = 0; i < links.length; i++) {
            lightpathsInLink.get(links[i]).remove(lp);
        }
    }

    /**
     * Retrieves the Set with all LightPath objects originated in this source.
     *
     * @param src the source
     * @return the Set with all of the lightpaths originated in this source
     */
    public ArrayList<LightPath> getLightpathsSrc(int src) {
        ArrayList<LightPath> lps = new ArrayList<>();
        for (int i = 0; i < pt.getNumNodes(); i++) {
            if (src != i) {
                lps.addAll(getLightpaths(src, i));
            }
        }
        return lps;
    }

    /**
     * Retrieves the Set with all LightPath objects originated in this source in
     * this modulation.
     *
     * @param src the source
     * @param modulation the modulation considered
     * @return the Set with all of the lightpaths originated in this source
     */
    public ArrayList<LightPath> getLightpathsSrcModulation(int src, int modulation) {
        ArrayList<LightPath> lps = new ArrayList<>();
        for (int i = 0; i < pt.getNumNodes(); i++) {
            if (src != i) {
                Iterator<LightPath> it = getLightpaths(src, i).iterator();
                while (it.hasNext()) {
                    LightPath lp = it.next();
                    if (((EONLightPath) lp).getModulation() == modulation) {
                        lps.add(lp);
                    }
                }
            }
        }
        return lps;
    }

    /**
     * Retrieves the Set with all LightPath objects originated in this
     * destination.
     *
     * @param dst the destination
     * @return the Set with all of the lightpaths originated in this source
     */
    public ArrayList<LightPath> getLightpathsDst(int dst) {
        ArrayList<LightPath> lps = new ArrayList<>();
        for (int i = 0; i < pt.getNumNodes(); i++) {
            if (dst != i) {
                lps.addAll(getLightpaths(i, dst));
            }
        }
        return lps;
    }

    /**
     * Retrieves the Set with all LightPath objects originated in this source
     * and in this link.
     *
     * @param src the source
     * @param link the id link in Physical Topology
     * @return the Set with all of the lightpaths originated in this source and
     * this link
     */
    public ArrayList<LightPath> getLightpathsSrc(int src, int link) {
        ArrayList<LightPath> lpsReturn = new ArrayList<>();
        ArrayList<LightPath> lps = getLightpathsInLink(link);
        for (LightPath lp : lps) {
            if (lp.getSource() == src) {
                lpsReturn.add(lp);
            }
        }
        return lpsReturn;
    }

    /**
     * Retrieves the Set with all LightPath objects originated in this source
     * and used this Tx.
     *
     * @param src the source
     * @param tx the id of the tx
     * @return the Set with all of the lightpaths originated in this source used
     * this Tx
     */
    public ArrayList<LightPath> getLightpathsSrcTx(int src, int tx) {
        ArrayList<LightPath> lpsReturn = new ArrayList<>();
        ArrayList<LightPath> lps = getLightpathsSrc(src);
        for (LightPath lp : lps) {
            if (lp.getTx() == tx) {
                lpsReturn.add(lp);
            }
        }
        return lpsReturn;
    }

    /**
     * Retrieves the Set with all LightPath objects belong this link and used
     * this Tx.
     *
     * @param link the link Id
     * @param tx the id of the tx
     * @return the Set with all LightPath objects belong this link and used this
     * Tx
     */
    public ArrayList<LightPath> getLightpathsLinkTx(int link, int tx) {
        ArrayList<LightPath> lpsReturn = new ArrayList<>();
        ArrayList<LightPath> lps = getLightpathsInLink(link);
        for (LightPath lp : lps) {
            if (lp.getTx() == tx) {
                lpsReturn.add(lp);
            }
        }
        return lpsReturn;
    }

    /**
     * Retrieves the adjacency matrix of the Virtual Topology.
     *
     * @return the VirtualTopology object's adjMatrix
     */
    public TreeSet<LightPath>[][] getAdjMatrix() {
        return adjMatrix;
    }

    /**
     * Says whether or not a lightpath exists, based only on its source and
     * destination nodes.
     *
     * @param src the lightpath's source node
     * @param dst the lightpath's destination node
     * @return true if the lightpath exists, or false otherwise
     */
    public boolean hasLightpath(int src, int dst) {
        if (adjMatrix[src][dst] != null) {
            if (!adjMatrix[src][dst].isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the number of hops of the lightpath with minimum physical hops
     * accommodate this rate.
     *
     * @param src the lightpath's source node
     * @param dst the lightpath's destination node
     * @param bw the rate accommodate
     * @return the hops of lightpath with minimun physical hops
     */
    public int getHopsMinLightpath(int src, int dst, int bw) {
        if (!(adjMatrix[src][dst] != null && !adjMatrix[src][dst].isEmpty())) {
            return 0;
        } else {
            return getMinHops(getAvailableLightpaths(src, dst, bw)).getHops();
        }
    }

    /**
     * Says whether or not a lightpath can be created, based only on its links
     * and wavelengths.
     *
     * @param links list of integers that represent the links that form the
     * lightpath
     * @param wavelengths list of wavelength values used in the lightpath links
     * @return true if the lightpath can be created, or false otherwise
     */
    //public abstract boolean canCreateLightpath(LightpathArguments args);
    /**
     * Retrieves the number of links (or hops) a given LightPath object has.
     *
     * @param lp the LightPath object
     * @return the number of hops the lightpath has
     */
    public int hopCount(LightPath lp) {
        return lp.getLinks().length;
    }

    /**
     * Retrieves the lightpaths of a weighted graph without weights.
     *
     * @param bw required amount of bandwidth
     * @return a weighted graph formed only by the lightpaths without weights
     */
    public WeightedGraph getLightpathsGraph(int bw) {
        int nodes = pt.getNumNodes();
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i != j) {
                    if (getAvailableLightpaths(i, j, bw) != null) {
                        g.addEdge(i, j, 1);
                    }
                }
            }
        }
        return g;
    }

    /**
     * Retrieves the lightpaths of a weighted graph with weights. Get the
     * minimum lightpath physical hops
     *
     * @param bw required amount of bandwidth
     * @param w the weight by hop
     * @return a weighted graph formed only by the lightpaths with weight
     */
    public WeightedGraph getWeightedLightpathsGraph(int bw, double w) {
        int nodes = pt.getNumNodes();
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i != j) {
                    LightPath lightpath = getMinHops(getAvailableLightpaths(i, j, bw));
                    if (lightpath != null) {
                        g.addEdge(i, j, lightpath.getHops() * w);
                    }
                }
            }
        }
        return g;
    }

    /**
     * Retrieves the lightpaths of a weighted graph with weights (only primary
     * lightpaths). Get the minimum lightpath physical hops
     *
     * @param bw required amount of bandwidth
     * @param w the weight by hop
     * @return a weighted graph formed only by the lightpaths with weight
     */
    public WeightedGraph getWeightedPrimaryLightpathsGraph(int bw, double w) {
        int nodes = pt.getNumNodes();
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i != j) {
                    LightPath lightpath = getMinHops(getAvailablePrimaryLightpaths(i, j, bw));
                    if (lightpath != null) {
                        g.addEdge(i, j, lightpath.getHops() * w);
                    }
                }
            }
        }
        return g;
    }

    /**
     * Retrieves the lightpaths of a weighted graph with weights (only primary
     * lightpaths). Get the minimum lightpath physical hops
     *
     * @param bw required amount of bandwidth
     * @param w the weight by hop
     * @param typeProtection type of protection
     * @return a weighted graph formed only by the lightpaths with weight
     */
    public WeightedGraph getWeightedPrimaryLightpathsGraphProtection(int bw, double w, String typeProtection) {
        int nodes = pt.getNumNodes();
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i != j) {
                    LightPath lightpath = getMinHops(getAvailablePrimaryLightpathsProtection(i, j, bw, typeProtection));
                    if (lightpath != null) {
                        g.addEdge(i, j, lightpath.getHops() * w);
                    }
                }
            }
        }
        return g;
    }

    /**
     * Retrieves the lightpaths of a weighted graph with weights (only backup
     * lightpaths). Get the minimum lightpath physical hops
     *
     * @param bw required amount of bandwidth
     * @param w the weight by hop
     * @param typeProtection type of protection
     * @return a weighted graph formed only by the lightpaths with weight
     */
    public WeightedGraph getWeightedBackupLightpathsGraphProtection(int bw, double w, String typeProtection) {
        int nodes = pt.getNumNodes();
        WeightedGraph g = new WeightedGraph(nodes);
        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                if (i != j) {
                    LightPath lightpath = getMinHops(getAvailableBackupLightpathsProtection(i, j, bw, typeProtection));
                    if (lightpath != null) {
                        g.addEdge(i, j, lightpath.getHops() * w);
                    }
                }
            }
        }
        return g;
    }

    /**
     * Retrieves the lightpath with minimum physical hops.
     *
     * @param availableLightpaths the TreeSet with the lighhtpaths
     * @return the lightpath with minimun physical hops
     */
    public LightPath getMinHops(TreeSet<LightPath> availableLightpaths) {
        TreeSet<LightPath> lps = null;
        if (availableLightpaths != null && !availableLightpaths.isEmpty()) {
            lps = new TreeSet<>(availableLightpaths);
        }
        LightPath lp_aux, lp = null;
        int h_aux, h = Integer.MAX_VALUE;
        if (lps != null && !lps.isEmpty()) {
            while (!lps.isEmpty()) {
                lp_aux = lps.pollFirst();
                if (lp_aux != null) {
                    h_aux = lp_aux.getHops();
                    if (h_aux < h) {
                        h = h_aux;
                        lp = lp_aux;
                    }
                }
            }
        }
        return lp;
    }

    /**
     * Retrieves if the lightpath [already allocated] is in optical grooming
     *
     * @param id the id of lightpath
     * @return true if the lightpath is in optical grooming, false otherwise
     */
    public boolean lightpathIsInOpticalGrooming(long id) {
        LightPath lp;
        if (!lightPaths.containsKey(id)) {
            return false;
        } else {
            lp = lightPaths.get(id);
            if (lp.getFlows().size() > 1) {
                return true;
            }
            Iterator<Entry<Long, LightPath>> itr1 = lightPaths.entrySet().iterator();
            while (itr1.hasNext()) {
                Entry<Long, LightPath> entry = itr1.next();
                if((!lp.equals(entry.getValue())) && (lp.Tx == entry.getValue().Tx)
                        && lp.src == entry.getValue().src){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves if the lightpath [already allocated] is in optical grooming and
     * normal alocated
     *
     * @param id the id of lightpath
     * @return true if the lightpath is in optical grooming and normal alocated,
     * false otherwise
     */
    private boolean lightpathIsHybrid(long id) {
        LightPath lp;
        if (!lightPaths.containsKey(id)) {
            return false;
        } else {
            lp = lightPaths.get(id);
            Iterator<Entry<Long, LightPath>> itr1 = lightPaths.entrySet().iterator();
            while (itr1.hasNext()) {
                Entry<Long, LightPath> entry = itr1.next();
                if ((!lp.equals(entry.getValue())) && (lp.Tx == entry.getValue().Tx)
                        && lp.src == entry.getValue().src && !Arrays.equals(lp.getLinks(), entry.getValue().getLinks())) {
                    return true;
                } else if ((!lp.equals(entry.getValue())) && (lp.Tx == entry.getValue().Tx)
                        && lp.src == entry.getValue().src && Arrays.equals(lp.getLinks(), entry.getValue().getLinks()) && lp.Rx != entry.getValue().Rx) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Retrieves the number of lightpaths are groomed in this lightpath's
     * tunnel.
     *
     * @param id the id of lightpath
     * @return the number of lightpaths in this lightpath's tunnel
     */
    public int numOfLightpathsTunnel(long id) {
        LightPath lp;
        int num = 0;
        if (!lightPaths.containsKey(id)) {
            return 0;
        } else {
            num++;
            lp = lightPaths.get(id);
            Iterator<Entry<Long, LightPath>> itr1 = lightPaths.entrySet().iterator();
            while (itr1.hasNext()) {
                Entry<Long, LightPath> entry = itr1.next();
                if ((!lp.equals(entry.getValue())) && (lp.Tx == entry.getValue().Tx)
                        && lp.src == entry.getValue().src) {
                    num++;
                }
            }
        }
        return num;
    }

    /**
     * Retrieves the lightpaths this tunnel.
     *
     * @param id the id of lightpath belonging this tunnel
     * @return the the lightpaths this tunnel.
     */
    public ArrayList<LightPath> getTunnel(long id) {
        ArrayList<LightPath> lps = new ArrayList<>();
        LightPath lp;
        if (!lightPaths.containsKey(id)) {
            throw (new IllegalArgumentException());
        } else {
            lp = lightPaths.get(id);
            lps.add(lp);
            Iterator<Entry<Long, LightPath>> itr1 = lightPaths.entrySet().iterator();
            while (itr1.hasNext()) {
                Entry<Long, LightPath> entry = itr1.next();
                if ((!lp.equals(entry.getValue())) && (lp.Tx == entry.getValue().Tx)
                        && lp.src == entry.getValue().src) {
                    lps.add(entry.getValue());
                }
            }
        }
        return lps;
    }

    /**
     * Retrieves the size of this tunnel (number of slots).
     *
     * @param id the id of lightpath belonging this tunnel
     * @return the size of this lightpath's tunnel
     */
    public int getTunnelSize(long id) {
        LightPath lp;
        int size = 0;
        if (!lightPaths.containsKey(id)) {
            return 0;
        } else {
            lp = lightPaths.get(id);
            EONLink link = (EONLink) pt.getLink(lp.getLinks()[0]);
            int core = ((EONLightPath) lp).getCores()[0];
            int slot = ((EONLightPath) lp).getFirstSlot();
            size++; //contando a si mesmo
            //contando para esquerda:
            slot--;
            //tenho que caminhar para esquerda ate o 0 - Tenho que entrar no while se ele for o -2
            while (slot != -1 && (link.getCores()[core].slots[slot] > 0 || link.getCores()[core].slots[slot] == -2)) {
                //nao posso contar o -2 como um slot usado no tunel
                if (link.getCores()[core].slots[slot] > 0) {
                    size++;
                }
                slot--;
            }
            //contando para direita:
            slot = ((EONLightPath) lp).getFirstSlot() + 1;
            int maxSlots = ((EONLink) pt.getLink(0)).getNumSlots();
            while (slot != maxSlots && (link.getCores()[core].slots[slot] > 0 || link.getCores()[core].slots[slot] == -2)) {
                if (link.getCores()[core].slots[slot] > 0) {
                    size++;
                }
                slot++;
            }
            /* comentado pq acima fica mais otimizado que fazer um interator em todos os lps da rede. Mas aqui tbm faz a mesma coisa!
            size = ((EONLightPath) lp).getSlots();
            Iterator<Entry<Long, LightPath>> itr1 = lightPaths.entrySet().iterator();
            while (itr1.hasNext()) {
                Entry<Long, LightPath> entry = itr1.next();
                if((!lp.equals(entry.getValue())) && (lp.Tx == entry.getValue().Tx)
                        && lp.src == entry.getValue().src){
                    size = size + ((EONLightPath) entry.getValue()).getSlots();
                }
            }
            */
            return size;
        }
    }

    /**
     * Prints all lightpaths belonging to the Virtual Topology.
     *
     * @return string containing all the elements of the adjMatrix TreeSet
     */
    @Override
    public String toString() {
        String vtopo = "";
        for (int i = 0; i < adjMatrixSize; i++) {
            for (int j = 0; j < adjMatrixSize; j++) {
                if (adjMatrix[i][j] != null) {
                    if (!adjMatrix[i][j].isEmpty()) {
                        vtopo += adjMatrix[i][j].toString() + "\n\n";
                    }
                }
            }
        }
        return vtopo;
    }

    public long getNextLightpathID() {
        return nextLightpathID;
    }
    
    public void backNextLightpathID() {
        nextLightpathID--;
    }

    /**
     * Checks if lightpath with optical grooming over existing lightpath. The
     * lightpath created will have the same modulation format of the existing
     * lightpath.
     *
     * @param path the path of the new lightpath
     * @param lpGroomable the lightpath will be groomable
     * @param requiredSlots the number of slots as required
     * @return true if it's possible create this lightpath, false otherwise
     */
    public boolean canCreateLightpathInOpticalGrooming(ArrayList<Integer> path, LightPath lpGroomable, int requiredSlots) {
        if (path.size() < 1) {
            throw (new IllegalArgumentException());
        }
        if (!lightPaths.containsKey(lpGroomable.id)) {
            throw (new IllegalArgumentException());
        } else {

            int firstSlot, lastSlot, dstNode = 0;
            int modulation = ((EONLightPath) lpGroomable).getModulation();
            int[] linksShared, linksNew, linkPath;
            LightPath lpShared, lpCreated, lpNew = null;

            // Create the linkPath
            linkPath = new int[path.size() - 1];
            for (int j = 0; j < path.size() - 1; j++) {
                linkPath[j] = pt.getLink(path.get(j), path.get(j + 1)).getID();
            }
            int common;
            for (common = 0; common < linkPath.length && common < lpGroomable.links.length; common++) {
                if (linkPath[common] != lpGroomable.links[common]) {
                    break;
                }
            }
            //FAZENDO OS LINKS DO SHARED E DO NEW
            // Create the links vector

            linksShared = new int[common];
            System.arraycopy(lpGroomable.links, 0, linksShared, 0, common);

            linksNew = new int[linkPath.length - common];
            System.arraycopy(linkPath, common, linksNew, 0, linkPath.length - common);
            //cria o lpShared da direira

            if (Arrays.equals(linksShared, lpGroomable.links) && linksNew.length == 0) {//entao eles estão totalmente um encima do outro
                firstSlot = ((EONLightPath) lpGroomable).getLastSlot() + 1;
                lastSlot = firstSlot + requiredSlots - 1;
                lpCreated = new EONLightPath(1, lpGroomable.getSource(), lpGroomable.getDestination(),
                        lpGroomable.links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());

                if (((EONPhysicalTopology) pt).canExtendLightpath(lpCreated, lpGroomable, getTunnelSize(lpGroomable.getID()))) {
                    return true;
                } else {//esquerda
                    lastSlot = ((EONLightPath) lpGroomable).getFirstSlot() - 1;
                    firstSlot = lastSlot - requiredSlots + 1;
                    lpCreated = new EONLightPath(1, lpGroomable.getSource(), lpGroomable.getDestination(),
                            lpGroomable.links, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    return ((EONPhysicalTopology) pt).canExtendLightpath(lpCreated, lpGroomable, getTunnelSize(lpGroomable.getID()));
                }
            } else {//entao ele ta parcialmente
                //se o linksNew.lenght == 0 ele nao estende e esta sobre a rota
                //se o linksNew.lenght > 0 ele estende ou bifurca a rota
                //testa ambos lps com ambos os lados: teste da direita
                firstSlot = ((EONLightPath) lpGroomable).getLastSlot() + 1;
                lastSlot = firstSlot + requiredSlots - 1;
                lpShared = new EONLightPath(1, lpGroomable.getSource(), pt.linkVector[lpGroomable.getLinks()[common - 1]].dst,
                        linksShared, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                if (linksNew.length > 0) {
                    lpNew = new EONLightPath(1, lpShared.getSource(), path.get(path.size() - 1),
                            linksNew, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    dstNode = lpNew.getDestination();
                } else {
                    dstNode = path.get(path.size() - 1);
                }
                //teste da direita
                if (!((EONPhysicalTopology) pt).canCreatePhysicalLightpathHybrid(lpShared, lpGroomable, getTunnelSize(lpGroomable.getID()), lpNew, dstNode)) {
                    //não deu pra direita, vamos tentar a esquerda
                    lastSlot = ((EONLightPath) lpGroomable).getFirstSlot() - 1;
                    firstSlot = lastSlot - requiredSlots + 1;
                    lpShared = new EONLightPath(1, lpGroomable.getSource(), pt.linkVector[lpGroomable.getLinks()[common - 1]].dst,
                            linksShared, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                    if (linksNew.length > 0) {
                        lpNew = new EONLightPath(1, lpShared.getSource(), path.get(path.size() - 1),
                                linksNew, firstSlot, lastSlot, modulation, EONPhysicalTopology.getSlotSize());
                        dstNode = lpNew.getDestination();
                    } else {
                        dstNode = path.get(path.size() - 1);
                    }
                    //teste da esquerda
                    if (!((EONPhysicalTopology) pt).canCreatePhysicalLightpathHybrid(lpShared, lpGroomable, getTunnelSize(lpGroomable.getID()), lpNew, dstNode)) {
                        return false;
                    }
                } else {
                    return true;
                }
                return false;
            }
        }
    }

    /**
     * Find the best lightpath candidate for optical grooming. The lightpath
     * with more links in common
     *
     * @param path the path nodes
     * @param lpsSource the set os lightptahs in this source
     * @return the best candidate lightpath
     */
    public LightPath searchBestCandidate(ArrayList<Integer> path, ArrayList<LightPath> lpsSource) {
        if (path.isEmpty()) {
            return null;
        }
        ArrayList<LightPath> lpsCandidates = new ArrayList<>();
        // Create the linkPath
        int[] linkPath = new int[path.size() - 1];
        for (int j = 0; j < path.size() - 1; j++) {
            linkPath[j] = pt.getLink(path.get(j), path.get(j + 1)).getID();
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
     * Retrieves the weight of this lightpath's links
     *
     * @param lp yhe lightpath
     * @return the weight of this lightpath's links
     */
    public double getLightPathWeight(LightPath lp) {
        if (pt.getLink(lp.getLinks()[0]).getSource() != lp.getSource()
                || pt.getLink(lp.getLinks()[lp.getLinks().length - 1]).getDestination() != lp.getDestination()) {
            throw (new IllegalArgumentException());
        }
        double weight = 0.0;
        for (int i = 0; i < lp.getLinks().length; i++) {
            weight += pt.getLink(lp.getLinks()[i]).getWeight();
        }
        return weight;
    }

    /**
     * Verifies whether the modulation serves all sizes of lightpaths
     *
     * @param lps the lightptahs
     * @param modulation the modulation tests
     * @return true if all candidates lightpaths serves all sizes of lightpaths,
     * false otherwise
     */
    public boolean modulationPath(LightPath[] lps, int modulation) {
        for (LightPath lp : lps) {
            if (isLightpathIdle(lp.getID())) {//only new lightpaths
                if (!(getLightPathWeight(lp) <= (double) Modulation.getModulationReach(modulation))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifies whether the modulation' lightpaths serves all sizes of
     * lightpaths. Only for EON Simulator
     *
     * @param lps the lightptahs
     * @return true if all candidates lightpaths serves all sizes of lightpaths,
     * false otherwise
     */
    public boolean modulationPath(LightPath[] lps) {
        for (LightPath lp : lps) {
            if (lp instanceof EONLightPath) {
                if (isLightpathIdle(lp.getID())) {//only new lightpaths
                    if (!(getLightPathWeight(lp) <= (double) Modulation.getModulationReach(((EONLightPath) lp).getModulation()))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Verifies whether Crosstalk' the lightpaths serves all sizes of
     * lightpaths. Only for EON QoT Simulator
     *
     * @param lightpaths
     * @return
     */
    boolean checkCrosstalk(LightPath[] lps) {
        for (LightPath lp : lps) {
            if (lp instanceof EONLightPath) {
                if (isLightpathIdle(lp.getID())) {//only new lightpaths
                    if (!((EONPhysicalTopology) pt).pi.checkCrosstalk((EONLightPath) lp)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Verifies whether Crosstalk' the lightpaths in Path serves all sizes of
     * lightpaths. Only for EON QoT Simulator
     *
     * @param paths Object Path
     * @return
     */
    boolean checkCrosstalk(Path[] paths) {
        for (Path path : paths) {
            if (!checkCrosstalk(path.getLightpaths())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies whether Crosstalk in the neighbors of this lightpaths serves all
     * sizes. Only for SDM-EON Simulator
     *
     * @param lps the lightptahs
     * @return
     */
    public boolean checkCrosstalkOnOthers(LightPath[] lps) {
        for (LightPath lp : lps) {
            if (lp instanceof EONLightPath) {
                if (isLightpathIdle(lp.getID())) {//only new lightpaths
                    if (!((EONPhysicalTopology) pt).pi.checkXTNeighbors(lp)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Verifies whether Crosstalk in the neighbors of this paths serves all
     * sizes. Only for SDM-EON Simulator
     *
     * @param paths the path
     * @return
     */
    public boolean checkCrosstalkOnOthers(Path[] paths) {
        for (Path path : paths) {
            if (!checkCrosstalkOnOthers(path.getLightpaths())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifies whether the modulation' paths serves all sizes of lightpaths in
     * each path. Only for EON Simulator
     *
     * @param paths the paths
     * @return true if all candidates lightpaths serves all sizes of lightpaths,
     * false otherwise
     */
    public boolean modulationPath(Path[] paths) {
        for (Path path : paths) {
            if (!modulationPath(path.getLightpaths())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the current Power Consumption in this topology
     *
     * @return the Power Consumption based in number of slots used and its
     * modulations
     */
    public double getPowerConsumption() {
        double power = 0;
        for (LightPath lp : lightPaths.values()) {
            power += getPowerConsumption(lp);
        }
        return power;
    }

    /**
     * Retrieves the current Power Consumption in this link
     *
     * @param link the id of link
     * @return the Power Consumption based in number of slots used and its
     * modulations
     */
    public double getPowerConsumption(int link) {
        double power = 0;
        for (LightPath lp : getLightpathsInLink(link)) {
            power += getPowerConsumption(lp);
        }
        return power;
    }

    /**
     * Retrieves the OXC setup expendituret in this lightpath
     *
     * @param lp the lightpath
     * @return the OXC setup expendituret in this lightpath in W
     */
    public double getBVOXCSetupExpenditure(LightPath lp) {
        double pc = 0;
        //creates nodes vector
        int[] nodes = new int[lp.getLinks().length + 1];
        nodes[0] = pt.getLink(lp.getLinks()[0]).getSource();
        for (int i = 0; i < lp.getLinks().length; i++) {
            nodes[i + 1] = pt.getLink(lp.getLinks()[i]).getDestination();
        }
        for (int i = 0; i < nodes.length; i++) {
            pc += (pt.getNode(nodes[i]).getNodeDegree() * pt.oxcNodeDegreeExpenditure) + (pt.getNode(nodes[i]).getGroomingInputPorts() * pt.oxcAddDropDegreeExpenditure);
        }
        return pc;
    }

    /**
     * Retrieves the power consumption without OXC setup in this lightpath
     *
     * @param lp the lightpath
     * @return the power consumption without OXC setup in this lightpath in W
     */
    public double getPowerConsumptionWithoutOXCSetup(LightPath lp) {
        double power = 0;
        if (lp instanceof EONLightPath) {
            power = (pt.trOverloadExpenditure
                    * Modulation.getModulationPC(((EONLightPath) lp).getModulation())
                    * (double) ((EONLightPath) lp).getSlots()
                    + pt.trIdleExpenditure) // BVT expenditure 
                    + (pt.oxcOperationExpenditure * (lp.getLinks().length + 1))// BV-OXC operational expenditure
                    + getOLAExpenditure(lp);//OLA expenditure
        } else {
            power = ((WDMLink) pt.getLink(0)).getPc() // BVT expenditure 
                    + (pt.oxcOperationExpenditure * (lp.getLinks().length + 1))// BV-OXC operational expenditure
                    + getOLAExpenditure(lp);//OLA expenditure
        }
        return power;
    }

    /**
     * Retrieves the power consumption in this lightpath
     *
     * @param lp the lightpath
     * @return the power consumption in this lightpath in W
     */
    public double getPowerConsumption(LightPath lp) {
        double power = 0;
        if (lp instanceof EONLightPath) {
            power = (pt.trOverloadExpenditure
                    * Modulation.getModulationPC(((EONLightPath) lp).getModulation())
                    * (double) ((EONLightPath) lp).getSlots()
                    + pt.trIdleExpenditure) // BVT expenditure 
                    + getBVOXCExpenditure(lp)// BV-OXC expenditure
                    + getOLAExpenditure(lp);//OLA expenditure
        } else {
            power = ((WDMLink) pt.getLink(0)).getPc()// BVT expenditure 
                    + getBVOXCExpenditure(lp)// BV-OXC expenditure
                    + getOLAExpenditure(lp);//OLA expenditure
        }
        return power;
    }

    /**
     * Retrieves the BVOXCs Power Consumption of this lightpath
     *
     * @param lp the lightpath
     * @return the BVOXCs Power Consumption of this lightpath based in number
     * OXC used
     */
    public double getBVOXCExpenditure(LightPath lp) {
        double pc = 0;
        //creates nodes vector
        int[] nodes = new int[lp.getLinks().length + 1];
        nodes[0] = pt.getLink(lp.getLinks()[0]).getSource();
        for (int i = 0; i < lp.getLinks().length; i++) {
            nodes[i + 1] = pt.getLink(lp.getLinks()[i]).getDestination();
        }
        for (int i = 0; i < nodes.length; i++) {
            pc += (pt.getNode(nodes[i]).getNodeDegree() * pt.oxcNodeDegreeExpenditure)
                    + (pt.getNode(nodes[i]).getGroomingInputPorts() * pt.oxcAddDropDegreeExpenditure)
                    + pt.oxcOperationExpenditure;
        }
        return pc;
    }

    /**
     * Retrieves the OLA Power Consumption of this lightpath
     *
     * @param lp the lightpath
     * @return the BVOXCs Power Consumption of this lightpath based in number
     * OXC used
     */
    private double getOLAExpenditure(LightPath lp) {
        double lpWeight = 0;
        for (int i = 0; i < lp.getLinks().length; i++) {
            lpWeight += pt.getLink(lp.getLinks()[i]).getWeight();
        }
        double ola = Math.ceil((lpWeight / (double) pt.spanSize) - 1) + 2;
        //60 W per fiber pair (2*30W) is considered. 
        //Moreover, 140 additional watts per amplifier location (at each span termination site).
        //So 70 per OLA plus 30 = 100W per OLA.
        return (ola * pt.olaExpenditure);
    }

    /**
     * Checks if the lightPath's SNR is above the threshold
     *
     * @param lightpath
     * @return true if the lightPath's SNR is above the threshold or false
     * otherwise
     */
    public boolean SNRCheck(LightPath lightpath) {
        return Modulation.QoTVerify(((EONLightPath) lightpath).getModulation(), ((EONLightPath) lightpath).getSnr());
    }

    /**
     * Checks whether lightpath meets all SNR restrictions
     *
     * @param lightpath LightPath Object
     * @return true if lightpath meets all SNR restrictions false otherwise
     */
    public boolean checkSNRLightpath(LightPath lightpath) {
        return pi.testSNR(lightpath);
    }

    /**
     * Calculates the Setup Time of this lightpath
     *
     * @param lp the lightpath
     * @return st time in s of this lightpath
     */
    public double calculateST(LightPath lp) {
        double processingDelay, configurationDelay, propagationDelay;
        processingDelay = (double) (lp.getHops() + 1) * pt.mensageProcessingTime;
        configurationDelay = (double) (lp.getHops() + 1) * pt.configurationTimeOXC;
        propagationDelay = Math.ceil(lengthPath(lp) / (double) pt.spanSize) * pt.propagationDelayTime;
        return (processingDelay + configurationDelay + propagationDelay);
    }

    /**
     * Retrieves the lightpath size in km
     *
     * @param lightpath the lightpath
     * @return the lightpath size in km
     */
    public double lengthPath(LightPath lightpath) {
        double lpLength = 0;
        for (int i = 0; i < lightpath.getLinks().length; i++) {
            lpLength += pt.getLink(lightpath.getLinks()[i]).getWeight();
        }
        return lpLength;
    }
    
    /**
     * Calculates SNR statistics when a lightpath is created.
     * @param lp the lightpath object
     */
    private void SNRStatistics(LightPath lp) {
        if (Simulator.simType > 0 && pi.isCheckQoT()) {
            if (pi.isSNRaware()) {
                st.addSNRSurplus(((EONLightPath) lp).getModulation(), lp.getSnr());
            } else {
                double snr = pi.computeSNRlightpath(lp);
                st.addSNRSurplus(((EONLightPath) lp).getModulation(), snr);
                if (!checkSNRLightpath(lp)) {
                    st.addSNR();
                }
            }
        }
    }

}
