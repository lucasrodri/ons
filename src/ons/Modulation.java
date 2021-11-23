/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author lucasrc
 */
public final class Modulation {

    private static Modulation singletonObject;
    private static PhysicalImpairments pi = null;
    /**
     * Represents the number of modulations used.
     */
    public static final int N_MOD = 8;

    public static final int _BPSK = 0;
    public static final int _QPSK = 1;
    public static final int _8QAM = 2;
    public static final int _16QAM = 3;
    public static final int _32QAM = 4;
    public static final int _64QAM = 5;
    public static final int _128QAM = 6;
    public static final int _256QAM = 7;

    /**
     * Represents the reach of the each modulation in kilometers.
     */
    public static int _BPSKReach = 8000;
    public static int _QPSKReach = (int) _BPSKReach/2;
    public static int _8QAMReach = (int) _QPSKReach/2;
    public static int _16QAMReach = (int) _8QAMReach/2;
    public static int _32QAMReach = (int) _16QAMReach/2;
    public static int _64QAMReach = (int) _32QAMReach/2;
    public static int _128QAMReach = (int) _64QAMReach/2;
    public static int _256QAMReach = (int) _128QAMReach/2;

    /**
     * Represents the power consumption of the each modulation in W.
     */
    public static double _BPSKPC = 112.374;
    public static double _QPSKPC = 133.416;
    public static double _8QAMPC = 154.457;
    public static double _16QAMPC = 175.498;
    public static double _32QAMPC = 196.539;
    public static double _64QAMPC = 217.581;
    public static double _128QAMPC = 238.623;
    public static double _256QAMPC = 259.665;

    /**
     * Represents the SNR threshold of the modulation format
     */
    public static double _BPSK_SNR = 6.0;
    public static double _QPSK_SNR = 9.0;
    public static double _8QAM_SNR = 12.0;
    public static double _16QAM_SNR = 15.0;
    public static double _32QAM_SNR = 18.0;
    public static double _64QAM_SNR = 21.0;
    public static double _128QAM_SNR = 24.0;
    public static double _256QAM_SNR = 27.0;

    /**
     * Represents the XT threshold of the modulation format
     */
    public static double _BPSK_XT = -14.0;
    public static double _QPSK_XT = -18.5;
    public static double _8QAM_XT = -21.0;
    public static double _16QAM_XT = -25.0;
    public static double _32QAM_XT = -27.0;
    public static double _64QAM_XT = -34.0;
    public static double _128QAM_XT = -41.5;//Estimad values
    public static double _256QAM_XT = -47.0;//Estimad values
    
    public static synchronized Modulation getModulationObject() {
        if (singletonObject == null) {
            singletonObject = new Modulation();
        }
        if(Simulator.simType == 2) {
            pi = PhysicalImpairments.getPhysicalImpairmentsObject();
        }
        return singletonObject;
    }
    
    public static synchronized Modulation getModulationObject(Element xml) {
        if (singletonObject == null) {
            singletonObject = new Modulation();
        }
        if(Simulator.simType == 2) {
            pi = PhysicalImpairments.getPhysicalImpairmentsObject();
        }
        NodeList nodelist = xml.getElementsByTagName("modulation");
        int modulations = nodelist.getLength();
        String name;
        for (int i = 0; i < modulations; i++) {
                double XTthreshold = 0.0, SNRthreshold = 0.0, PC = 0.0;
                int maxReach = 0;
                name = ((Element) nodelist.item(i)).getAttribute("name");
                if (((Element) nodelist.item(i)).hasAttribute("XTthreshold")) {
                    XTthreshold = Double.parseDouble(((Element) nodelist.item(i)).getAttribute("XTthreshold"));
                }
                if (((Element) nodelist.item(i)).hasAttribute("SNRthreshold")) {
                    SNRthreshold = Double.parseDouble(((Element) nodelist.item(i)).getAttribute("SNRthreshold"));
                }
                if (((Element) nodelist.item(i)).hasAttribute("PC")) {
                    PC = Double.parseDouble(((Element) nodelist.item(i)).getAttribute("PC"));
                }
                if (((Element) nodelist.item(i)).hasAttribute("maxReach")) {
                    maxReach = Integer.parseInt(((Element) nodelist.item(i)).getAttribute("maxReach"));
                }
                switch (name) {
                    case "BPSK":
                        if (maxReach != 0) _BPSKReach = maxReach;
                        if (XTthreshold != 0) _BPSK_XT = XTthreshold;
                        if (SNRthreshold != 0) _BPSK_SNR = SNRthreshold;
                        if (PC != 0) _BPSKPC = PC;
                        break;
                    case "QPSK":
                        if (maxReach != 0) _QPSKReach = maxReach;
                        if (XTthreshold != 0) _QPSK_XT = XTthreshold;
                        if (SNRthreshold != 0) _QPSK_SNR = SNRthreshold;
                        if (PC != 0) _QPSKPC = PC;
                        break;
                    case "8QAM":
                        if (maxReach != 0) _8QAMReach = maxReach;
                        if (XTthreshold != 0) _8QAM_XT = XTthreshold;
                        if (SNRthreshold != 0) _8QAM_SNR = SNRthreshold;
                        if (PC != 0) _8QAMPC = PC;
                        break;
                    case "16QAM":
                        if (maxReach != 0) _16QAMReach = maxReach;
                        if (XTthreshold != 0) _16QAM_XT = XTthreshold;
                        if (SNRthreshold != 0) _16QAM_SNR = SNRthreshold;
                        if (PC != 0) _16QAMPC = PC;
                        break;
                    case "32QAM":
                        if (maxReach != 0) _32QAMReach = maxReach;
                        if (XTthreshold != 0) _32QAM_XT = XTthreshold;
                        if (SNRthreshold != 0) _32QAM_SNR = SNRthreshold;
                        if (PC != 0) _32QAMPC = PC;
                        break;
                    case "64QAM":
                        if (maxReach != 0) _64QAMReach = maxReach;
                        if (XTthreshold != 0) _64QAM_XT = XTthreshold;
                        if (SNRthreshold != 0) _64QAM_SNR = SNRthreshold;
                        if (PC != 0) _64QAMPC = PC;
                        break;
                    case "128QAM":
                        if (maxReach != 0) _128QAMReach = maxReach;
                        if (XTthreshold != 0) _128QAM_XT = XTthreshold;
                        if (SNRthreshold != 0) _128QAM_SNR = SNRthreshold;
                        if (PC != 0) _128QAMPC = PC;
                        break;
                    case "256QAM":
                        if (maxReach != 0) _256QAMReach = maxReach;
                        if (XTthreshold != 0) _256QAM_XT = XTthreshold;
                        if (SNRthreshold != 0) _256QAM_SNR = SNRthreshold;
                        if (PC != 0) _256QAMPC = PC;
                        break;
                }
        }
        return singletonObject;
    }
    
    /**
     * Retrieves the number of slots needed this rate in this conditions.
     *
     * @param rate the rate flow in Mbps
     * @param slotSize the slotSize in MHz
     * @param modulation the modulation Type
     * @return the number of slots
     */
    public static int convertRateToSlot(int rate, int slotSize, int modulation) {
        int slots;
        double m;
        m = 1.0 + (double) modulation;
        slots = (int) Math.ceil(((double) rate) / (((double) slotSize) * m));
        return slots;
    }

    /**
     * Retrieves the rate in Mbps that the amount of slots supports under this
     * modulation.
     *
     * @param numberOfSlots the number of slots
     * @param slotSize the slotSize in MHz
     * @param modulation the modulation Type
     * @return the rate in Mbps
     */
    public static int convertSlotToRate(int numberOfSlots, int slotSize, int modulation) {
        int rate;
        double m;
        m = 1.0 + (double) modulation;
        rate = (int) ((double) numberOfSlots * (double) slotSize * m);
        return rate;
    }

    /**
     * Retrieves the modulation name from the id integer value.
     *
     * @param id integer correponding to the modulation format
     * @return the modulation name
     */
    public static String getModulationName(int id) {
        switch (id) {
            case 0:
                return "BPSK";
            case 1:
                return "QPSK";
            case 2:
                return "8QAM";
            case 3:
                return "16QAM";
            case 4:
                return "32QAM";
            case 5:
                return "64QAM";
            case 6:
                return "128QAM";
            case 7:
                return "256QAM";
        }
        return "ERROR!";
    }

    /**
     * Convert the string value of modulation type to the equivalent id integer
     * value, definied on this class.
     *
     * @param name the name of modulation format
     * @return integer correponding to the modulation format
     */
    public static int convertModulationTypeToInteger(String name) {
        String toLowerCase = name.toLowerCase();
        switch (toLowerCase) {
            case "256qam":
                return _256QAM;
            case "128qam":
                return _128QAM;
            case "64qam":
                return _64QAM;
            case "32qam":
                return _32QAM;
            case "16qam":
                return _16QAM;
            case "8qam":
                return _8QAM;
            case "qpsk":
                return _QPSK;
            case "bpsk":
                return _BPSK;
        }
        return -1;
    }

    /**
     * Retrieves the reach of modulation.
     *
     * @param idModulation the modulation id
     * @return the reach of modulation
     */
    public static int getModulationReach(int idModulation) {
        //return Integer.MAX_VALUE;
        switch (idModulation) {
            case 0:
                return _BPSKReach;
            case 1:
                return _QPSKReach;
            case 2:
                return _8QAMReach;
            case 3:
                return _16QAMReach;
            case 4:
                return _32QAMReach;
            case 5:
                return _64QAMReach;
            case 6:
                return _128QAMReach;
            case 7:
                return _256QAMReach;
        }
        return -1;
    }

    /**
     * Retrieves the Power Consumption of this modulation.
     *
     * @param idModulation the modulation id
     * @return the Power Consumption of modulation
     */
    public static double getModulationPC(int idModulation) {
        switch (idModulation) {
            case 0:
                return _BPSKPC;
            case 1:
                return _QPSKPC;
            case 2:
                return _8QAMPC;
            case 3:
                return _16QAMPC;
            case 4:
                return _32QAMPC;
            case 5:
                return _64QAMPC;
            case 6:
                return _128QAMPC;
            case 7:
                return _256QAMPC;
        }
        return -1;
    }

    /**
     * Check the modulation QoT given the route lenght
     *
     * @param idModulation the modulation id
     * @param lenght the route size
     * @return true if the modulation supports this snr false otherwise
     */
    public static boolean QoTDistanceVerify(int idModulation, double lenght) {
        //return true;
        switch (idModulation) {
            case 0:
                return (double) _BPSKReach >= lenght;
            case 1:
                return (double) _QPSKReach >= lenght;
            case 2:
                return (double) _8QAMReach >= lenght;
            case 3:
                return (double) _16QAMReach >= lenght;
            case 4:
                return (double) _32QAMReach >= lenght;
            case 5:
                return (double) _64QAMReach >= lenght;
            case 6:
                return (double) _128QAMReach >= lenght;
            case 7:
                return (double) _256QAMReach >= lenght;
        }
        return false;
    }
    
    /**
     * Check the modulation QoT given the SNR
     *
     * @param idModulation the modulation id
     * @param snr the snr checks
     * @return true if the modulation supports this snr false otherwise
     */
    public static boolean QoTVerify(int idModulation, double snr) {
        //return true;
        switch (idModulation) {
            case 0:
                return (double) _BPSK_SNR <= snr;
            case 1:
                return (double) _QPSK_SNR <= snr;
            case 2:
                return (double) _8QAM_SNR <= snr;
            case 3:
                return (double) _16QAM_SNR <= snr;
            case 4:
                return (double) _32QAM_SNR <= snr;
            case 5:
                return (double) _64QAM_SNR <= snr;
            case 6:
                return (double) _128QAM_SNR <= snr;
            case 7:
                return (double) _256QAM_SNR <= snr;
        }
        return false;
    }

    /**
     * Retrieves the best modulation format given the distance
     *
     * @param distance the distance in Km
     * @return the best modulation format for this distance
     */
    public static int getBestModulation(double distance) {
        int maxM = EONPhysicalTopology.getMaxModulation();
        if (distance > _BPSKReach) {
            return -1;
        } else if (distance <= _256QAMReach && maxM >= _256QAM) {
            return _256QAM;
        } else if (distance <= _128QAMReach && maxM >= _128QAM) {
            return _128QAM;
        } else if (distance <= _64QAMReach && maxM >= _64QAM) {
            return _64QAM;
        } else if (distance <= _32QAMReach && maxM >= _32QAM) {
            return _32QAM;
        } else if (distance <= _16QAMReach && maxM >= _16QAM) {
            return _16QAM;
        } else if (distance <= _8QAMReach && maxM >= _8QAM) {
            return _8QAM;
        } else if (distance <= _QPSKReach && maxM >= _QPSK) {
            return _QPSK;
        } else {
            return _BPSK;
        }
    }

    /**
     * Retrieves the best modulation format given the snr
     *
     * @param snr the SNR
     * @return the best modulation format for this SNR
     */
    public static int getBestModulationSNR(double snr) {
        int maxM = EONPhysicalTopology.getMaxModulation();
        if (snr < _BPSK_SNR) {
            return -1;
        } else if (snr >= _256QAM_SNR && maxM >= _256QAM) {
            return _256QAM;
        } else if (snr >= _128QAM_SNR && maxM >= _128QAM) {
            return _128QAM;
        } else if (snr >= _64QAM_SNR && maxM >= _64QAM) {
            return _64QAM;
        } else if (snr >= _32QAM_SNR && maxM >= _32QAM) {
            return _32QAM;
        } else if (snr >= _16QAM_SNR && maxM >= _16QAM) {
            return _16QAM;
        } else if (snr >= _8QAM_SNR && maxM >= _8QAM) {
            return _8QAM;
        } else if (snr >= _QPSK_SNR && maxM >= _QPSK) {
            return _QPSK;
        } else {
            return _BPSK;
        }
    }
    
    /**
     * Retrieves the SNR threshold of this modulation
     * @param idModulation the modulation id
     * @return the SNR threshold of this idModulation
     */
    public static double getModulationSNRthreshold(int idModulation) {
        switch (idModulation) {
            case 0:
                return _BPSK_SNR;
            case 1:
                return _QPSK_SNR;
            case 2:
                return _8QAM_SNR;
            case 3:
                return _16QAM_SNR;
            case 4:
                return _32QAM_SNR;
            case 5:
                return _64QAM_SNR;
            case 6:
                return _128QAM_SNR;
            case 7:
                return _256QAM_SNR;
            default:
                return -1;
        }
    }
     /**
     * Retrieves the XT threshold of this modulation
     * @param idModulation the modulation id
     * @return the XT threshold of this idModulation
     */
    public static double getModulationXTthreshold(int idModulation) {
        switch (idModulation) {
            case 0:
                return _BPSK_XT;
            case 1:
                return _QPSK_XT;
            case 2:
                return _8QAM_XT;
            case 3:
                return _16QAM_XT;
            case 4:
                return _32QAM_XT;
            case 5:
                return _64QAM_XT;
            case 6:
                return _128QAM_XT;
            case 7:
                return _256QAM_XT;
            default:
                return -1;
        }
    }
}
