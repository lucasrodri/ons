/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

/**
 * Simply runs the simulation, as long as there are events
 * scheduled to happen.
 * 
 * @author lucasrc
 */
public class SimulationRunner {

    /**
     * Creates a new SimulationRunner object.
     * 
     * @param cp the the simulation's control plane
     * @param events the simulation's event scheduler
     */
    public SimulationRunner(ControlPlane cp, EventScheduler events) {
        Event event;
        Tracer tr = Tracer.getTracerObject();
        MyStatistics st = MyStatistics.getMyStatisticsObject();        
        while ((event = events.popEvent()) != null) {
            tr.add(event);
            if (event instanceof OrdinaryEvent) {
                st.addOrdinaryEvent(event);
            } else if (cp.getPT() instanceof EONPhysicalTopology) {
                st.addEvent(event, ((EONPhysicalTopology) cp.getPT()).getAvailableSlots(), cp.getPT().getAllFreeGroomingInputPorts(), ((EONPhysicalTopology) cp.getPT()).getExFragmentation());
            } else {
                st.addEvent(event, cp.getPT().getAllFreeGroomingInputPorts());
            }
            cp.newEvent(event);
        }
    }
}
