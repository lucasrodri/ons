/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import ons.tools.PrintFile;
import org.w3c.dom.Element;

/**
 *
 * @author lucas
 */
public class PhysicalImpairments {

    private static PhysicalImpairments singletonObject;
    private PhysicalTopology pt;
    private VirtualTopology vt;
    private static boolean SNRaware = false;
    private static boolean checkQoT = false;
    private static Map<Long, Double> snrLpsMap;
    private static boolean activeAse = true; //activates amplifier ASE noise
    private static boolean activeNli = true; //activates nonlinear noise in fibers
    private static final double POWER = 0; //power per channel, dBm
    private static double L = 80; //size of a span, km
    private static double NF = 6; //noise figure of the amplifier, dB
    private static final double H = 6.626E-34; //Planck constant
    private static double centerFrequency = 193.0E+12; //light frequency
    private static double alfa = 0.2; //dB/km, fiber loss
    private static double beta2 = 16.0E-24; //ps^2 = E-24, scatter parameter
    private static double gama = 1.22; //nonlinearity of fiber
    private static double C = 1.0; //dispersion compensation rate
    private static double slotBand; //1 slot bandwidth (Slot size * 1.0E+6)
    
    private static boolean XTaware = false; //Whether the simulator will take into account CrossTalk-related physical layer impacts. Only for SDM-EON.
    private static boolean DynamicActiveNeighborNumber = false; //Crosstalk will always be considered for neighbors or only when there is actually a neighboring circuit.
    private static boolean physicalDistance = false; //Whether to use the maximum range of modulation or not.
    private static boolean XTonOthers = false; //Crosstalk will be considered only in the optical circuit that will be established or will be considered in the other neighbors.
    
    /**Reference: Ruijie Zhu, Yongli Zhao, Hui Yang,
     * Haoran Chen, Jie Zhang, and Jason P. Jue, "Crosstalk-aware RCSA for
     * spatial division multiplexing enabled elastic optical networks with
     * multi-core fibers," Chin. Opt. Lett. 14, 100604- (2016)
     */
    
    private static double k = 3.16E-5; //coupling coefficient
    private static double r = 0.055; //bend radius
    private static double beta = 4.0E6; //propagation constant
    private static double w_tr = 45.0E-6; //core pitch
    
    private PhysicalImpairments() {
        snrLpsMap = new HashMap<>();
    }

    protected void setPT(PhysicalTopology pt) {
        this.pt = pt;
        L = pt.spanSize;
        slotBand = EONPhysicalTopology.getSlotSize() * (1.0E+6);
    }

    protected void setVT(VirtualTopology vt) {
        this.vt = vt;
    }

    public static synchronized PhysicalImpairments getPhysicalImpairmentsObject() {
        if (singletonObject == null) {
            singletonObject = new PhysicalImpairments();
        }
        return singletonObject;
    }

    public static synchronized PhysicalImpairments getPhysicalImpairmentsObject(Element xml) {
        if (singletonObject == null) {
            singletonObject = new PhysicalImpairments();
        }
        if (xml.hasAttribute("XTaware")) {
            switch (xml.getAttribute("XTaware")) {
                case "yes":
                    XTaware = true;
                    break;
                case "no":
                    XTaware = false;
                    break;
                default:
                    throw (new IllegalArgumentException("XTaware wrong in xml. Define it as yes/no"));
            }
        }
        if (xml.hasAttribute("SNRaware")) {
            switch (xml.getAttribute("SNRaware")) {
                case "yes":
                    SNRaware = true;
                    break;
                case "no":
                    SNRaware = false;
                    break;
                default:
                    throw (new IllegalArgumentException("SNRaware wrong in xml. Define it as yes/no"));
            }
        }
        if (xml.hasAttribute("checkQoT")) {
            switch (xml.getAttribute("checkQoT")) {
                case "yes":
                    checkQoT = true;
                    break;
                case "no":
                    checkQoT = false;
                    break;
                default:
                    throw (new IllegalArgumentException("checkQoT wrong in xml. Define it as yes/no"));
            }
        }
        if (xml.hasAttribute("physical-distance")) {
            switch (xml.getAttribute("physical-distance")) {
                case "yes":
                    physicalDistance = true;
                    break;
                case "no":
                    physicalDistance = false;
                    break;
                default:
                    throw (new IllegalArgumentException("physical-distance wrong in xml. Define it as yes/no"));
            }
        }
        if (xml.hasAttribute("dynamicNeighborNumber")) {
            switch (xml.getAttribute("dynamicNeighborNumber")) {
                case "yes":
                    DynamicActiveNeighborNumber = true;
                    break;
                case "no":
                    DynamicActiveNeighborNumber = false;
                    break;
                default:
                    throw (new IllegalArgumentException("dynamicNeighborNumber wrong in xml. Define it as yes/no"));
            }
        }
        if (xml.hasAttribute("XTonOthers")) {
            switch (xml.getAttribute("XTonOthers")) {
                case "yes":
                    XTonOthers = true;
                    break;
                case "no":
                    XTonOthers = false;
                    break;
                default:
                    throw (new IllegalArgumentException("XTonOthers wrong in xml. Define it as yes/no"));
            }
        }
        if (xml.hasAttribute("activeAse")) {
            switch (xml.getAttribute("activeAse")) {
                case "yes":
                    activeAse = true;
                    break;
                case "no":
                    activeAse = false;
                    break;
                default:
                    throw (new IllegalArgumentException("activeAse wrong in xml. Define it as yes/no"));
            }
        }
        if (xml.hasAttribute("activeNli")) {
            switch (xml.getAttribute("activeNli")) {
                case "yes":
                    activeNli = true;
                    break;
                case "no":
                    activeNli = false;
                    break;
                default:
                    throw (new IllegalArgumentException("activeNli wrong in xml. Define it as yes/no"));
            }
        }
        if (xml.hasAttribute("NF")) NF = Double.parseDouble(xml.getAttribute("NF"));
        if (xml.hasAttribute("centerFrequency")) centerFrequency = Double.parseDouble(xml.getAttribute("centerFrequency"));
        if (xml.hasAttribute("alfa")) alfa = Double.parseDouble(xml.getAttribute("alfa"));
        if (xml.hasAttribute("beta2")) beta2 = Double.parseDouble(xml.getAttribute("beta2"));
        if (xml.hasAttribute("gama")) gama = Double.parseDouble(xml.getAttribute("gama"));
        if (xml.hasAttribute("C")) C = Double.parseDouble(xml.getAttribute("C"));
        if (xml.hasAttribute("k")) k = Double.parseDouble(xml.getAttribute("k"));
        if (xml.hasAttribute("r")) r = Double.parseDouble(xml.getAttribute("r"));
        if (xml.hasAttribute("beta")) beta = Double.parseDouble(xml.getAttribute("beta"));
        if (xml.hasAttribute("w_tr")) w_tr = Double.parseDouble(xml.getAttribute("w_tr"));

        return singletonObject;
    }

    /**
     * Computes the SNR value of this lightpath and its neighbors.
     * @param lightpath the LightPath Object
     */
    protected void computeSNR(LightPath lightpath) {

        ArrayList<ArrayList<LightPath>> lightpathsNeighborsPerLink = vt.getLightpaths(lightpath);

        HashMap<Long, Double> snrPorLightpath = new HashMap<Long, Double>();

        addLightPathsInListLinks(lightpath, lightpathsNeighborsPerLink);

        for (int l = 0; l < lightpath.getLinks().length; l++) {

            for (LightPath lpAtual : lightpathsNeighborsPerLink.get(l)) {
                if (snrLpsMap.containsKey(lpAtual.getID())) {
                    lpAtual.setSnr(snrLpsMap.get(lpAtual.getID()));
                } else {
                    int primSlot = ((EONLightPath) lpAtual).getFirstSlot();
                    int ultSlot = ((EONLightPath) lpAtual).getLastSlot();
                    int[] interSlots = {primSlot, ultSlot};

                    lpAtual.setSnr(computeSNRlightpath(lpAtual, interSlots, vt.getLightpaths(lpAtual), lightpath));
                    //to set the smallest snr of links
                    if (!snrPorLightpath.containsKey(lpAtual.getID())) {
                        snrPorLightpath.put(lpAtual.getID(), lpAtual.getSnr());
                    } else if (!(snrPorLightpath.get(lpAtual.getID()) >= lpAtual.getSnr())) {
                        //update lp getting the smallest
                        lpAtual.setSnr(snrPorLightpath.get(lpAtual.getID()));
                    } else {
                        //update the map throwing the smallest value to it
                        snrPorLightpath.put(lpAtual.getID(), lpAtual.getSnr());
                    }
                }
            }
        }
        snrLpsMap.clear();
    }

    /**
     * Checks whether this lightpath can be set by meeting all QoT, (QoTN and QoTO) requirements.
     * @param lightpath the LightPath Object
     * @return true if is possible, false otherwise.
     */
    public boolean testSNR(LightPath lightpath) {

        double snrTemp = 0;

        ArrayList<ArrayList<LightPath>> lightpathsNeighborsPerLink = vt.getLightpaths(lightpath);
        HashMap<Long, Double> snrPorLightpath = new HashMap<Long, Double>();
        addLightPathsInListLinks(lightpath, lightpathsNeighborsPerLink);
        
        for (int l = 0; l < lightpath.getLinks().length; l++) {

            for (LightPath lpAtual : lightpathsNeighborsPerLink.get(l)) {

                int primSlot = ((EONLightPath) lpAtual).getFirstSlot();
                int ultSlot = ((EONLightPath) lpAtual).getLastSlot();
                int[] interSlots = {primSlot, ultSlot};

                if (snrLpsMap.containsKey(lpAtual.getID())) {
                    snrTemp = snrLpsMap.get(lpAtual.getID());
                } else {
                    snrTemp = computeSNRlightpath(lpAtual, interSlots, vt.getLightpaths(lpAtual), lightpath);
                }

                if (!Modulation.QoTVerify(((EONLightPath) lpAtual).getModulation(), snrTemp)) {
                    snrLpsMap.clear();
                    return false;
                }
                snrLpsMap.put(lpAtual.getID(), snrTemp);
                //to set the smallest snr of links
                if (!snrPorLightpath.containsKey(lpAtual.getID())) {
                    snrPorLightpath.put(lpAtual.getID(), snrTemp);
                    //update lp getting the smallest
                } else if (!(snrPorLightpath.get(lpAtual.getID()) >= snrTemp)) {
                    snrLpsMap.put(lpAtual.getID(), snrPorLightpath.get(lpAtual.getID()));
                } else {
                    //update the map throwing the smallest value to it
                    snrPorLightpath.put(lpAtual.getID(), snrTemp);
                }
            }
        }
        snrLpsMap.remove(-1);
        return true;
    }
    
    /**
     * Adds lightpath to the list of neighboring lightpaths on your link for SNR calculation only.
     *
     * @param lightpath the LightPath Object
     * @param lightpathsNeighborsPerLink the ArrayList object with all lightpaths belong the lp links
     */
    private void addLightPathsInListLinks(LightPath lightpath, ArrayList<ArrayList<LightPath>> lightpathsNeighborsPerLink) {
        for (int i = 0; i < lightpath.getLinks().length; i++) {
            lightpathsNeighborsPerLink.get(i).add(lightpath);
        }
    }

    /**
     * Retrieves if this link in this lighpath.
     * @param link the Link Object
     * @param lp the LightPath Object
     * @return if this link in this lighpath
     */
    private boolean hasLinkInThisLP(int link, LightPath lp) {
        for (int i = 0; i < lp.links.length; i++) {
            if(link == lp.links[i]) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Compute the SNR of this LightPath and their neighbors.
     * Calculated SNR Model:
     * Papers: Nonlinear Impairment Aware Resource Allocation in Elastic Optical
     * Networks (2015) Modeling of Nonlinear Signal Distortion in Fiber-Optic
     * Networks (2014).
     * @param lp The lp that will be calculated the SNR
     * @param spectrumAssigned lp Center Frequency
     * @param lightpathsNeighborsPerLink the ArrayList object with all lightpaths belong the lp links
     * @param newLP if set, will be the candidate LP that will be added to the neighbors group.
     * @return The SNR valeu of this lp
     */
    private double computeSNRlightpath(LightPath lp, int spectrumAssigned[], ArrayList<ArrayList<LightPath>> lightpathsNeighborsPerLink, LightPath newLP) {
        
        if (newLP != null) {
            for (int i = 0; i < lp.links.length; i++) {
                if(hasLinkInThisLP(lp.links[i], newLP)) {
                    lightpathsNeighborsPerLink.get(i).add(newLP);
                }
            }
        }
        double Ptx = ratioOfDB(POWER) * 1.0E-3; //W, transmitter power
        double Pase = 0.0;
        double Pnli = 0.0;

        int requiredSlots = ((EONLightPath) lp).getSlots(); //Number of Slots Required
        double fs = slotBand; //Hz
        double Bsi = requiredSlots * fs; //request bandwidth

        double totalSlots = ((EONLink) pt.getLink(0)).getNumSlots();
        double lowerFrequency = centerFrequency - (fs * (totalSlots / 2.0)); //Hz, half slots removed because centerFrequency = 193.0E+12 is the center frequency of the optical spectrum
        double fi = lowerFrequency + (fs * (spectrumAssigned[0])) + (Bsi / 2); //central frequency of the request

        double strengthDensity = 50000.0/EONPhysicalTopology.getSlotSize(); //50 GHz for WDM wavelength 

        double I = Ptx / (fs * strengthDensity); //signal strength density for WDM 50 GHz
        
        for (int l = 0; l < lp.getLinks().length; l++) {
            Link enlace = pt.getLink(lp.getLinks()[l]);
            double Ns = (int) Math.ceil((enlace.getWeight()) / L); //number of spans
            
            if (activeNli) {
                double noiseNli = Ns * getGnli(lp, I, Bsi, fi, lowerFrequency, lightpathsNeighborsPerLink.get(l));
                Pnli = Pnli + noiseNli;
            }

            if (activeAse) {
                double noiseAse = Ns * getAse(centerFrequency);
                Pase = Pase + noiseAse;
            }
        }

        double SNR = I / ((2.0 * Pase) + Pnli);
        return ratioForDB(SNR);
    }

    /**
     * Get the value of ASE (Amplified Spontaneous Emission) in SNR Model.
     * Reference: - Closed-form expressions for nonlinear transmission 
     * performance of densely spaced coherent optical OFDM systems (2010) - A
     * Quality-of-Transmission Aware Dynamic Routing and Spectrum Assignment
     * Scheme for Future Elastic Optical Networks (2013)
     *
     * @param gain, linear
     * @param frequency central frenquency
     * @return double - ase linear
     */
    private double getAse(double frequency) {
        double noiseFigureLinear = ratioOfDB(NF);
        double G0 = alfa * L; //ganho em dB do amplificador
        double gainLinear = ratioOfDB(G0);

        double ase = 0.5 * H * frequency * noiseFigureLinear * (gainLinear - 1.0);
        return ase;
    }

    /**
     * Converts a value in dB to a linear value (ratio)
     *
     * @param dB valeu
     * @return ratio
     */
    public double ratioOfDB(double dB) {
        double ratio;
        ratio = Math.pow(10.0, (dB / 10.0));
        return ratio;
    }

    /**
     * Function that returns the inverse hyperbolic sine of the argument Math.asin = arcsin
     *
     * @param x - double
     * @return double arcsin
     */
    private double arcsin(double x) {
        return Math.log(x + Math.sqrt(x * x + 1.0));
    }
    
    /**
     * Get the value of Gnli (gaussian nonlinear) in SNR Model.
     * Reference: - Nonlinear Impairment Aware Resource Allocation in Elastic Optical Networks (2015)
     * @param lp the LightPath Object
     * @param I signal strength
     * @param Bsi request bandwidth
     * @param fi central frequency of the request
     * @param lowerFrequency central frequency of the request
     * @param lightpathsNeighborsPerLink
     * @return 
     */
    private double getGnli(LightPath lp, double I, double Bsi, double fi, double lowerFrequency, ArrayList<LightPath> lightpathsNeighborsPerLink) {
        double alfaLinear = ratioOfDB(alfa);
        if (beta2 < 0.0) {
            beta2 = -1.0 * beta2;
        }
        //double he = getHe(Ns, alfa, L, Math.E, C);
        //double mi = (3.0 * gama * gama * I * I * I * he) / (2.0 * Math.PI * alfaLinear * beta2);
        double mi = (3.0 * gama * gama * I * I * I) / (2.0 * Math.PI * alfaLinear * beta2);

        double ro = (Math.PI * Math.PI * beta2) / (2.0 * alfaLinear);
        double p1 = arcsin(ro * Bsi * Bsi);
        double p2 = 0.0;
        
        for (LightPath lpTemp : lightpathsNeighborsPerLink) {
            //TODO For Optical grooming
            //if (!lp.equals(lpTemp) && vt.lightPaths.containsKey(lp.getID()) && !vt.getTunnel(lp.getID()).contains(lpTemp)) {
            if (!lp.equals(lpTemp)) {
                double fs = slotBand;
                double numOfSlots = ((EONLightPath) lpTemp).getSlots();
                double Bsj = numOfSlots * fs; //request bandwidth
                double fj = lowerFrequency + (fs * (((EONLightPath) lpTemp).getFirstSlot())) + (Bsj / 2); //frequencia central da requisicao

                double deltaFij = fi - fj;
                if (deltaFij < 0.0) {
                    deltaFij = -1.0 * deltaFij;
                }

                double d1 = deltaFij + (Bsj / 2);
                double d2 = deltaFij - (Bsj / 2);

                double ln = Math.log(d1 / d2);
                p2 += ln;
            }
        }
        double gnli = mi * (p1 + p2);
        return gnli;
    }

    /**
     * Compute the SNR of this lp.
     * @param lp the LightPath Object
     * @return the SNR of this lp
     */
    public double computeSNRlightpath(LightPath lp) {
        int[] interSlots = {((EONLightPath) lp).getFirstSlot(), ((EONLightPath) lp).getLastSlot()};
        return computeSNRlightpath(lp, interSlots, vt.getLightpaths(lp), null);
    }
    
    /**
     * Computes the smallest variation (DELTA) of neighboring SNR if this lightpath is allocated.
     * @param lightpath the LightPath Object
     * @return deltaSNR, If retrieves MAX_VALUE its not possible established this lp
     */
    public double computeDeltaSNRNeighbors(LightPath lightpath) {
        double delta = Double.MAX_VALUE;
        double snrTemp = 0;

        ArrayList<ArrayList<LightPath>> lightpathsNeighborsPerLink = vt.getLightpaths(lightpath);
        HashMap<Long, Double> snrPorLightpath = new HashMap<Long, Double>();
        addLightPathsInListLinks(lightpath, lightpathsNeighborsPerLink);
        
        for (int l = 0; l < lightpath.getLinks().length; l++) {

            for (LightPath lpAtual : lightpathsNeighborsPerLink.get(l)) {

                int primSlot = ((EONLightPath) lpAtual).getFirstSlot();
                int ultSlot = ((EONLightPath) lpAtual).getLastSlot();
                int[] interSlots = {primSlot, ultSlot};

                if (snrLpsMap.containsKey(lpAtual.getID())) {
                    snrTemp = snrLpsMap.get(lpAtual.getID());
                } else {
                    snrTemp = computeSNRlightpath(lpAtual, interSlots, vt.getLightpaths(lpAtual), lightpath);
                }

                if (!Modulation.QoTVerify(((EONLightPath) lpAtual).getModulation(), snrTemp)) {
                    snrLpsMap.clear();
                    return Double.MAX_VALUE;
                }
                double SNRthreshold = Modulation.getModulationSNRthreshold(((EONLightPath) lpAtual).getModulation());
                if((SNRthreshold - snrTemp) < delta) {
                    delta = SNRthreshold - snrTemp;
                }
                
                
                snrLpsMap.put(lpAtual.getID(), snrTemp);
                //to set the smallest snr of links
                if (!snrPorLightpath.containsKey(lpAtual.getID())) {
                    snrPorLightpath.put(lpAtual.getID(), snrTemp);
                    //update lp getting the smallest
                } else if (!(snrPorLightpath.get(lpAtual.getID()) >= snrTemp)) {
                    snrLpsMap.put(lpAtual.getID(), snrPorLightpath.get(lpAtual.getID()));
                } else {
                    //update the map throwing the smallest value to it
                    snrPorLightpath.put(lpAtual.getID(), snrTemp);
                }
            }
        }
        snrLpsMap.remove(-1);
        
        return delta;
    }
    
    /**
     * Converts a ratio (linear) to decibel
     *
     * @param ratio
     * @return dB
     */
    public static double ratioForDB(double ratio) {
        double dB;
        dB = 10.0 * Math.log10(ratio);
        return dB;
    }
    
    /**
     * Computes and retrieves the SNR of this lightpath
     * @param lp the LightPath Object
     * @return the SNR of this lightpath
     */
    public double getSNR(LightPath lp) {
        computeSNR(lp);
        return lp.getSnr();
    }

    /**
     * Usado para verificar se o XT eh viavel para os circuitos ja alocados nos
     * nucleos vizinhos ao nucleo escolhido, dentro dos indices de slots do novo
     * circuito.
     *
     * @param lp
     * @param links
     * @param cores
     * @param firstSlot
     * @param requiredSlots
     * @return
     */
    public boolean checkXTNeighbors(LightPath lp, int[] links, int[] cores, int firstSlot, int requiredSlots) {
        // arraylist com os circuitos ja verificados
        ArrayList<LightPath> lpsVerificados = new ArrayList<LightPath>();

        // percorre a lista de enlaces do novo circuito
        for (int l = 0; l < links.length; l++) {
            // lista de vizinhos do nucleo escolhido para o enlace at
            ArrayList<Core> coresVizinhos = ((EONLink) pt.getLink(links[l])).getCores()[cores[l]].getNeighbors();

            // para cada nucleo vizinho
            for (Core core : coresVizinhos) {
                //lista de slots do nucleo atual
                long[] slotsAtual = core.getSlots();
                //percorre os slots correspondentes ao intervalo a ser alocado pelo novo circuito
                for (int slot = firstSlot; slot <= firstSlot + requiredSlots - 1; slot++) {
                    // se o slot esta ocupado
                    if (slotsAtual[slot] > 0) {	//recupera o lightpath vizinho pelo valor do id encontrado no slot
                        EONLightPath lpAtual = (EONLightPath) vt.getLightpath(slotsAtual[slot]);

                        // impede a computacao redundante do XT
                        if (!lpsVerificados.contains(lpAtual) && lpAtual != lp) {	//reavaliar o XT do lightpath ja alocado
                            if (checkCrosstalk(lpAtual.getLinks(), lpAtual.getCores(), lpAtual.getFirstSlot(), lpAtual.getLastSlot() - lpAtual.getFirstSlot() + 1, lpAtual.getModulation())) {
                                lpsVerificados.add(lpAtual);
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    public boolean checkXTNeighbors(LightPath lp) {
        return checkXTNeighbors(lp, lp.getLinks(), ((EONLightPath) lp).getCores(), ((EONLightPath) lp).getFirstSlot(), ((EONLightPath) lp).getSlots());
    }

    /**
     * Para cenario de vizinhos dinamicos, o algoritmo percorre a lista de
     * links, e verifica quais nucleos estao ocupados na faixa espectral a ser
     * ocupada pelo novo circuito. Em necario de vizinhos estaticos, verifica
     * qual dos nucleos escolhidos apresenta a maior quantidade de vizinhos.
     *
     * @param links lista de enlaces da rota
     * @param cores lista de nucleos da rota
     * @param firstSlot slot inicial para o circuito
     * @param requiredSlots numero de slots do circuito
     * @return int quantidade de vizinhos diferentes ocupados ao longo da rota.
     */
    protected int checkNumberOfNeighbors(int[] links, int[] cores, int firstSlot,int requiredSlots) {
        int n = 0;
        if (DynamicActiveNeighborNumber) {
            int numVizinhos = 0, maxNumVizinhos = 0;

            // percorre a lista de enlaces da rota escolhida
            for (int l = 0; l < links.length; l++) {
                numVizinhos = 0;
                // busca o enlace atual
                EONLink linkAtual = ((EONLink) pt.getLink(links[l]));

                if (cores[l] == -1) {
                    throw new IllegalArgumentException("Enlace sem nucleo selecionado!");
                }

                // recupera a lista de vizinhos do nucleo escolhido para o enlace atual
                ArrayList<Core> vizinhos = linkAtual.getCores()[cores[l]].getNeighbors();

                // evitar execucoes desnecessarias do for
                if (vizinhos.size() <= maxNumVizinhos) {
                    continue;
                }

                // percorre a lista de vizinhos
                for (Core vizinho : vizinhos) {
                    //recupera a lista de slots do vizinho atual
                    long[] slotsVizinho = vizinho.getSlots();
                    // percorre o intervalo de slots escolhido para o novo circuito
                    for (int slot = firstSlot; slot <= firstSlot + requiredSlots - 1; slot++) {
                        // se o slot do vizinho estiver ocupado por circuito
                        if (slotsVizinho[slot] > 0) {
                            numVizinhos++;
                            break;
                        }
                    }
                }
                // se o link atual apresenta a maior quantidade de nucleos vizinhos ativos
                if (numVizinhos > maxNumVizinhos) {
                    maxNumVizinhos = numVizinhos;
                }
            }
            n = maxNumVizinhos;
        } else {
            for (int i = 0; i < links.length; i++) {
                EONLink linkAtual = ((EONLink) pt.getLink(links[i]));
                int numVizinhosAtual = linkAtual.getCores()[cores[i]].getNeighbors().size();
                if (numVizinhosAtual > n) {
                    n = numVizinhosAtual;
                }
            }
        }
        return n;
    }

    /**
     * Verificacao de XT.
     *
     * @param links
     * @param cores
     * @param firstSlot
     * @param requiredSlots
     * @param modulation
     * @return
     */
    public boolean checkCrosstalk(int[] links, int[] cores, int firstSlot, int requiredSlots, int modulation) {
        double tamanho = 0.0;
        int numVizinhos = 0;
        for (int i = 0; i < links.length; i++) {
            EONLink link = ((EONLink) pt.getLink(links[i]));
            tamanho += link.getWeight();
        }
        numVizinhos = checkNumberOfNeighbors(links, cores, firstSlot, requiredSlots);
        return crosstalk(numVizinhos, tamanho) <= Modulation.getModulationXTthreshold(modulation);
    }
    
    public boolean checkCrosstalk(EONLightPath lp) {
        double tamanho = getLightPathLength(lp);
        int numVizinhos = 0;
        numVizinhos = checkNumberOfNeighbors(lp.links, lp.getCores(), lp.getFirstSlot(), lp.getLastSlot() - lp.getFirstSlot() + 1);
        return crosstalk(numVizinhos, tamanho) <= Modulation.getModulationXTthreshold(lp.getModulation());
    }

    /**
     * Retorna valor de crosstalk Referencia: Ruijie Zhu, Yongli Zhao, Hui Yang,
     * Haoran Chen, Jie Zhang, and Jason P. Jue, "Crosstalk-aware RCSA for
     * spatial division multiplexing enabled elastic optical networks with
     * multi-core fibers," Chin. Opt. Lett. 14, 100604- (2016)
     *
     * Limiar de XT desta referencia = -25 dB
     *
     * @param x numero de nucleos adjacentes
     * @param L comprimento do enlace, em km
     * @return double - valor de crosstalk para o nucleo
     */
    public double crosstalk(int x, double L) {
        double xt = 0, h = 0;
        double n = (double) x;

        //converter L de km para m
        L = L * 1000;

        h = (2.0 * k * k * r) / (beta * w_tr);

        xt = (n - (n * Math.exp(-(n + 1.0) * 2.0 * h * L)))
                / (1.0 + (n * Math.exp(-(n + 1.0) * 2.0 * h * L)));

        double xtDB = 10 * Math.log10(xt);

        return xtDB;
    }

    public boolean isXTaware() {
        return XTaware;
    }

    public void setXTaware(boolean xTaware) {
        XTaware = xTaware;
    }

    public boolean isDynamicActiveNeighborNumber() {
        return DynamicActiveNeighborNumber;
    }

    public void setDynamicActiveNeighborNumber(boolean dynamicActiveNeighborNumber) {
        DynamicActiveNeighborNumber = dynamicActiveNeighborNumber;
    }

    public boolean isXTonOthers() {
        return XTonOthers;
    }

    public void setXTonOthers(boolean xTonOthers) {
        XTonOthers = xTonOthers;
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        PhysicalImpairments.k = k;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        PhysicalImpairments.r = r;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(double beta) {
        PhysicalImpairments.beta = beta;
    }

    public double getW_tr() {
        return w_tr;
    }

    public void setW_tr(double w_tr) {
        PhysicalImpairments.w_tr = w_tr;
    }

    public boolean isSNRaware() {
        return SNRaware;
    }

    public boolean isCheckQoT() {
        return checkQoT;
    }
    
    public boolean isPhysicalDistance() {
        return physicalDistance;
    }

    /**
     * Retrieves the length of this lightpath's links
     *
     * @param lp yhe lightpath
     * @return the length of this lightpath's links
     */
    public double getLightPathLength(LightPath lp) {
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
}
