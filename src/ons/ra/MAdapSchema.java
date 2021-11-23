/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.ra;

import ons.EONPhysicalTopology;
import ons.Flow;
import ons.LightPath;
import ons.Modulation;
import ons.Path;
import ons.util.PseudoControlPlane;

/**
 * This class is a adaptive modulation model, that uses some RA class in your implementation. 
 * This adaptive modulation model was created by Xin Wan in:
 * Article: "Dynamic routing and spectrum assignment in spectrum flexible transparent optical networks",
 * Xin Wan, Nan Hua, and Xiaoping Zheng,
 * Optical Communications and Networking, IEEE/OSA Journal of, 
 * Aug 2012.
 * To resolve the RMLSA problem.
 * @author lucas
 */
public class MAdapSchema implements RA {
    
    private ControlPlaneForRA cp;
    private ControlPlaneForRA pseudoCP;
    int conta = 0;
    
    //Here we defined the RSA class:
    private final RA rsa = new KSP();
    //Here the variables of this scheme
    int maxM = EONPhysicalTopology.getMaxModulation();

    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.pseudoCP = new PseudoControlPlane(cp, rsa);
        this.rsa.simulationInterface(pseudoCP);
    }

    @Override
    public void setModulation(int modulation) {
    }
    
    @Override
    public void simulationEnd() {   
    }
    
    @Override
    public void flowArrival(Flow flow) {
        int modulation = maxM;

        while (true) {
            rsa.setModulation(modulation);
            rsa.flowArrival(flow);
            if (pseudoCP.getPath(flow) == null) { //checks that was not blocked
                if (modulation == 0) {
                    cp.blockFlow(flow.getID());
                    return;
                } else {
                    modulation--;
                }
            } else {
                if (modulationPath(pseudoCP.getPath(flow), modulation)) {
                    for (LightPath lightpath : pseudoCP.getPath(flow).getLightpaths()) {
                        cp.getPT().removeRate(flow.getRate(), lightpath);
                    }
                    cp.acceptFlow(flow.getID(), pseudoCP.getPath(flow).getLightpaths());
                    //System.err.println(conta++);
                    return;
                } else {
                    pseudoCP.blockFlow(flow.getID());
                    if (modulation == 0) {
                        cp.blockFlow(flow.getID());
                        return;
                    } else {
                        modulation--;
                    }
                }
            }
        }
    }

    private boolean modulationPath(Path path, int modulation) {
        double pathLength;
        int[] links;
        LightPath[] lps = path.getLightpaths();
        for (LightPath lp : lps) {
            links = lp.getLinks();
            pathLength = 0;
            for (int i = 0; i < links.length; i++) {
                pathLength += cp.getPT().getLink(links[i]).getWeight();
            }
            if(pathLength > (double) Modulation.getModulationReach(modulation)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public void flowDeparture(long id) {
        rsa.flowDeparture(id);
    }
}
