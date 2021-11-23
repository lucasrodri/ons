package ons;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * The Elastic Optical Network (EON) Link represents a Fiberlink in an optical
 * network divided by slots.
 *
 * @author lucasrc
 */
public class EONLink extends Link {
    
    protected Core cores[];

    public EONLink(int id, int src, int dst, double delay, double weight, int numSlots, int guardband) {
        super(id, src, dst, delay, weight);
        this.cores = new Core[1];
        this.cores[0] = new Core(0, id, numSlots, guardband);
    }
    
    public EONLink(int id, int src, int dst, double delay, double weight, int numSlots, int guardband, int cores) {
        super(id, src, dst, delay, weight);
        this.cores = new Core[cores];
        for (int i = 0; i < cores; i++) {
            this.cores[i] = new Core(i, id, numSlots, guardband);
        }
        definesNeighbors();
    }
    
    public Core[] getCores() {
        return cores;
    }
    
    public int getNumCores() {
        return cores.length;
    }
    
    /**
     * Retrieves the guardband size in this link.
     *
     * @return the guardband size
     */
    public int getGuardband() {
        return cores[0].getGuardband();
    }
    
    /**
     * Retrieves the guardband size in this link core.
     *
     * @param core core id
     * @return the guardband size
     */
    public int getGuardband(int core) {
        return cores[core].getGuardband();
    }

    /**
     * Retrieves the number of slots in this link.
     *
     * @return the number of slots in this link
     */
    public int getNumSlots() {
        int slots = 0;
        for (Core core : cores) {
            slots += core.getNumSlots();
        }
        return slots;
    }
    
    /**
     * Retrieves the number of slots in this link core.
     *
     * @param core core id
     * @return the number of slots in this link
     */
    public int getNumSlots(int core) {
        return cores[core].getNumSlots();
    }

    /**
     * Retrieves the slots available in this link.
     *
     * @return the number slots available
     */
    public int getAvaiableSlots() {
        int slots = 0;
        for (Core core : cores) {
            slots += core.getAvaiableSlots();
        }
        return slots;
    }
    
    /**
     * Retrieves the slots available in this link core.
     *
     * @param core core id
     * @return the number slots available
     */
    public int getAvaiableSlots(int core) {
        return cores[core].getAvaiableSlots();
    }

    /**
     * Retrieves the used slots in this link.
     *
     * @return the number used slots
     */
    public int getUsedSlots() {
        return cores[0].getUsedSlots();
    }
    
    /**
     * Retrieves the used slots in this link core.
     *
     * @param core core id
     * @return the number used slots
     */
    public int getUsedSlots(int core) {
        return cores[core].getUsedSlots();
    }

    /**
     * Check if there is available slots to accommodate the request int this link.
     *
     * @param requiredSlots the request slots
     * @return true if can be accommodate, false otherwise
     */
    public boolean hasSlotsAvaiable(int requiredSlots) {
        for (Core core : cores) {
            if(core.hasSlotsAvaiable(requiredSlots))
                return true;
        }
        return false;
    }
    
    /**
     * Check if there is available slots to accommodate the request in this core.
     *
     * @param core core id
     * @param requiredSlots the request slots
     * @return true if can be accommodate, false otherwise
     */
    public boolean hasSlotsAvaiable(int core, int requiredSlots) {
        return cores[core].hasSlotsAvaiable(requiredSlots);
    }

    /**
     * Retrieves the first slot available to accommodate this requisition.
     * for this link
     *
     * @param requiredSlots the required slots
     * @return the first slot available to accommodate this requisition
     */
    public int getFirstSlotAvailable(int requiredSlots) {
        return cores[0].getFirstSlotAvailable(requiredSlots);
    }
    
    /**
     * Retrieves the first slot available to accommodate this requisition.
     * for this core
     *
     * @param core core id
     * @param requiredSlots the required slots
     * @return the first slot available to accommodate this requisition
     */
    public int getFirstSlotAvailable(int core, int requiredSlots) {
        return cores[core].getFirstSlotAvailable(requiredSlots);
    }

    /**
     * Retrieves the set of slots available given a minimum size.
     * for this link
     *
     * @param requiredSlots the required slots of set
     * @return the set with first slots available to 'requiredSlots'
     */
    public ArrayList<Integer> getSlotsAvailable(int requiredSlots) {//look at all available spaces considering guard band
        return cores[0].getSlotsAvailable(requiredSlots);
    }
    
    /**
     * Retrieves the set of slots available given a minimum size.
     * for this core
     *
     * @param core id core
     * @param requiredSlots the required slots of set
     * @return the set with first slots available to 'requiredSlots'
     */
    public ArrayList<Integer> getSlotsAvailable(int core, int requiredSlots) {//look at all available spaces considering guard band
        return cores[core].getSlotsAvailable(requiredSlots);
    }

    /**
     * Retrieves the set of slots available in optical grooming given a minimum
     * size.
     * for this link
     *
     * @param requiredSlots the required slots of set
     * @return the set with first slots available in optical grooming to
     * 'requiredSlots'
     */
    public TreeSet<Integer> getSlotsAvailableOG(int requiredSlots) {
        return cores[0].getSlotsAvailableOG(requiredSlots);
    }
    
    /**
     * Retrieves the set of slots available in optical grooming given a minimum
     * size.
     * for this core
     *
     * @param core core id
     * @param requiredSlots the required slots of set
     * @return the set with first slots available in optical grooming to
     * 'requiredSlots'
     */
    public TreeSet<Integer> getSlotsAvailableOG(int core, int requiredSlots) {
        return cores[core].getSlotsAvailableOG(requiredSlots);
    }

    /**
     * Retrieves the set of slots available in optical grooming or not given a
     * minimum size.
     * for this link
     *
     * @param requiredSlots the required slots of set
     * @return the set with first slots available in optical grooming or not to
     * 'requiredSlots'
     */
    public TreeSet<Integer> getSlotsAvailableGlobal(int requiredSlots) {
        return cores[0].getSlotsAvailableGlobal(requiredSlots);
    }
    
    /**
     * Retrieves the set of slots available in optical grooming or not given a
     * minimum size.
     * for this core
     *
     * @param core core id
     * @param requiredSlots the required slots of set
     * @return the set with first slots available in optical grooming or not to
     * 'requiredSlots'
     */
    public TreeSet<Integer> getSlotsAvailableGlobal(int core, int requiredSlots) {
        return cores[core].getSlotsAvailableGlobal(requiredSlots);
    }

    /**
     * Retrieves the set of slots available given a minimum size [but in array
     * of 'int'].
     * for this link
     *
     * @param requiredSlots the required slots of set
     * @return the array with first slots available to 'requiredSlots'
     */
    public int[] getSlotsAvailableToArray(int requiredSlots) {
        return cores[0].getSlotsAvailableToArray(requiredSlots);
    }
    
    /**
     * Retrieves the set of slots available given a minimum size [but in array
     * of 'int'].
     * for this core
     *
     * @param core core id
     * @param requiredSlots the required slots of set
     * @return the array with first slots available to 'requiredSlots'
     */
    public int[] getSlotsAvailableToArray(int core, int requiredSlots) {
        return cores[core].getSlotsAvailableToArray(requiredSlots);
    }

    /**
     * Retrieves the set of slots available in optical grooming given a minimum
     * size [but in array of 'int'].
     * for this link
     *
     * @param requiredSlots the required slots of set
     * @return the array with first slots available in optical grooming to
     * 'requiredSlots'
     */
    public int[] getSlotsAvailableOGToArray(int requiredSlots) {
        return cores[0].getSlotsAvailableOGToArray(requiredSlots);
    }
    
    /**
     * Retrieves the set of slots available in optical grooming given a minimum
     * size [but in array of 'int'].
     * for this core
     *
     * @param core core id
     * @param requiredSlots the required slots of set
     * @return the array with first slots available in optical grooming to
     * 'requiredSlots'
     */
    public int[] getSlotsAvailableOGToArray(int core, int requiredSlots) {
        return cores[core].getSlotsAvailableOGToArray(requiredSlots);
    }

    /**
     * Retrieves the set of slots available in optical grooming or not given a
     * minimum size [but in array of 'int'].
     * for this link
     *
     * @param requiredSlots the required slots of set
     * @return the array with first slots available in optical grooming or not
     * to 'requiredSlots'
     */
    public int[] getSlotsAvailableGlobalToArray(int requiredSlots) {
        return cores[0].getSlotsAvailableGlobalToArray(requiredSlots);
    }
    
    /**
     * Retrieves the set of slots available in optical grooming or not given a
     * minimum size [but in array of 'int'].
     * for this core
     *
     * @param core core id
     * @param requiredSlots the required slots of set
     * @return the array with first slots available in optical grooming or not
     * to 'requiredSlots'
     */
    public int[] getSlotsAvailableGlobalToArray(int core, int requiredSlots) {
        return cores[core].getSlotsAvailableGlobalToArray(requiredSlots);
    }

    /**
     * Checks for available slots considering the guard band.
     * for this link
     *
     * @param begin the begin slot
     * @param end the end slot
     * @return true if is avaiable slots, false otherwise
     */
    public boolean areSlotsAvaiable(int begin, int end) {
        return cores[0].areSlotsAvaiable(begin, end);
    }
    
    /**
     * Checks for available slots considering the guard band.
     * for this core
     *
     * @param core core id
     * @param begin the begin slot
     * @param end the end slot
     * @return true if is avaiable slots, false otherwise
     */
    public boolean areSlotsAvaiable(int core, int begin, int end) {
        return cores[core].areSlotsAvaiable(begin, end);
    }

    /**
     * Reserve slots (with guard band) in this link, ie reserve lightpath.
     * for this link
     *
     * @param id the id of lightpath reserved
     * @param begin the begin of lightpath
     * @param end the end of lightpath
     */
    public void reserveSlots(long id, int begin, int end) {
        cores[0].reserveSlots(id, begin, end);
    }
    
    /**
     * Reserve slots (with guard band) in this link, ie reserve lightpath.
     * for this core
     *
     * @param core core id
     * @param id the id of lightpath reserved
     * @param begin the begin of lightpath
     * @param end the end of lightpath
     */
    public void reserveSlots(int core, long id, int begin, int end) {
        cores[core].reserveSlots(id, begin, end);
    }

    /**
     * Reserve slots (with guard band only the lightpath is on the edge) in this
     * link, ie reserve lightpath using optical grooming technique.
     * for this link
     *
     * @param id the id of lightpath reserved
     * @param begin the begin of lightpath
     * @param end the end of lightpath
     */
    public void reserveSlotsInOpticalGrooming(long id, int begin, int end) {
        cores[0].reserveSlotsInOpticalGrooming(id, begin, end);
    }
    
    /**
     * Reserve slots (with guard band only the lightpath is on the edge) in this
     * link, ie reserve lightpath using optical grooming technique.
     * for this core
     *
     * @param core core id
     * @param id the id of lightpath reserved
     * @param begin the begin of lightpath
     * @param end the end of lightpath
     */
    public void reserveSlotsInOpticalGrooming(int core, long id, int begin, int end) {
        cores[core].reserveSlotsInOpticalGrooming(id, begin, end);
    }

    /**
     * Release slots from this link. Examines the corresponding guard bands
     *
     * @param begin the begin
     * @param end the end
     */
    public void releaseSlots(int begin, int end) {
        cores[0].releaseSlots(begin, end);
    }
    
    /**
     * Release slots from this core. Examines the corresponding guard bands
     *
     * @param core core id
     * @param begin the begin
     * @param end the end
     */
    public void releaseSlots(int core, int begin, int end) {
        cores[core].releaseSlots(begin, end);
    }

    /**
     * Release slots from this link belonging to a tunnel. Examines the
     * corresponding guard bands in the edges
     *
     * @param begin the begin
     * @param end the end
     */
    public void releaseSlotsInOpticalGrooming(int begin, int end) {
        cores[0].releaseSlotsInOpticalGrooming(begin, end);
    }
    
    /**
     * Release slots from this core belonging to a tunnel. Examines the
     * corresponding guard bands in the edges
     *
     * @param core core id
     * @param begin the begin
     * @param end the end
     */
    public void releaseSlotsInOpticalGrooming(int core, int begin, int end) {
        cores[core].releaseSlotsInOpticalGrooming(begin, end);
    }

    /**
     * Retrieves the max size of contiguous slots available.
     * for this link
     *
     * @return the max size of contiguous slots available
     */
    public int maxSizeAvaiable() {
        int max = 0;
        for (Core core : cores) {
            if(core.maxSizeAvaiable() > max) {
                max = core.maxSizeAvaiable();
            }
        }
        return max;
    }
    
    /**
     * Retrieves the max size of contiguous slots available.
     * for this core
     *
     * @param core core id
     * @return the max size of contiguous slots available
     */
    public int maxSizeAvaiable(int core) {
        return cores[core].maxSizeAvaiable();
    }

    /**
     * Retrieves the minimun size of contiguous slots available.
     * for this link
     *
     * @return the minimun size of contiguous slots available
     */
    public int minSizeAvaiable() {
        return cores[0].minSizeAvaiable();
    }
    
    /**
     * Retrieves the minimun size of contiguous slots available.
     * for this core
     *
     * @param core core id
     * @return the minimun size of contiguous slots available
     */
    public int minSizeAvaiable(int core) {
        return cores[core].minSizeAvaiable();
    }

    /**
     * Check if this lightpath is this link
     *
     * @param id the id of lightpath
     * @return if it is true, false otherwise
     */
    public boolean hasThisLightPath(long id) {
        return cores[0].hasThisLightPath(id);
    }
    
    /**
     * Check if this lightpath is this core
     *
     * @param core core id
     * @param id the id of lightpath
     * @return if it is true, false otherwise
     */
    public boolean hasThisLightPath(int core, long id) {
        return cores[core].hasThisLightPath(id);
    }

    /**
     * Retrieves the information of this link
     *
     * @return the string information
     */
    @Override
    public String toString() {
        String link = Long.toString(id) + ": " + Integer.toString(src) + "->" + Integer.toString(dst) 
                + " delay:" + Double.toString(delay) + ";";
        for (int i = 0; i < cores.length; i++) {
            link += "| core(" + i + "): " + cores[i].numSlots + "; ";
        }
        link += "; weight:" + Double.toString(weight);
        return link;
    }

    /**
     * Print this link for debug.
     */
    public void printLink() {
        for (Core core : cores) {
            core.printCore();
        }
        System.out.println("");
    }
    
    /**
     * Print the latest 10 slots of this link, for debug.
     */
    public void printEndLink() {
        for (Core core : cores) {
            core.printEndCore();
        }
        System.out.println("");
    }

    /**
     * Retrieves the lightpath slot position.
     * for this link
     *
     * @param lp the id of lightpath
     * @return the array[2] with begin position and end position
     */
    public int[] findLightpath(long lp) {
        return cores[0].findLightpath(lp);
    }
    
    /**
     * Retrieves the lightpath slot position.
     * for this core
     *
     * @param core core id
     * @param lp the id of lightpath
     * @return the array[2] with begin position and end position
     */
    public int[] findLightpath(int core, long lp) {
        return cores[core].findLightpath(lp);
    }

    /**
     * Examine whether it is possible to extend lightpath to left.
     * for this link
     *
     * @param requiredSlots the number of slots to be extend to left
     * @param lp the lightpath id
     * @return true if is possible, false otherwise
     */
    public boolean leftExtend(int requiredSlots, long lp) {
        return cores[0].leftExtend(requiredSlots, lp);
    }
    
    /**
     * Examine whether it is possible to extend lightpath to left.
     * for this core
     *
     * @param core core id
     * @param requiredSlots the number of slots to be extend to left
     * @param lp the lightpath id
     * @return true if is possible, false otherwise
     */
    public boolean leftExtend(int core, int requiredSlots, long lp) {
        return cores[core].leftExtend(requiredSlots, lp);
    }

    /**
     * Examine whether it is possible to extend lightpath to right.
     * for this link
     *
     * @param requiredSlots the number of slots to be extend to right
     * @param lp the lightpath id
     * @return true if is possible, false otherwise
     */
    public boolean rightExtend(int requiredSlots, long lp) {
        return cores[0].rightExtend(requiredSlots, lp);
    }
    
    /**
     * Examine whether it is possible to extend lightpath to right.
     * for this core
     *
     * @param core core id
     * @param requiredSlots the number of slots to be extend to right
     * @param lp the lightpath id
     * @return true if is possible, false otherwise
     */
    public boolean rightExtend(int core, int requiredSlots, long lp) {
        return cores[core].rightExtend(requiredSlots, lp);
    }

    /**
     * Retrieves if this lightpath is in optical grooming.
     * for this link
     *
     * @param lp the lightpath id
     * @return true if this is in optical grooming, false otherwise
     */
    public boolean isOpticalGrooming(long lp) {
        return cores[0].isOpticalGrooming(lp);
    }
    
    /**
     * Retrieves if this lightpath is in optical grooming.
     * for this core 
     *
     * @param core core id
     * @param lp the lightpath id
     * @return true if this is in optical grooming, false otherwise
     */
    public boolean isOpticalGrooming(int core, long lp) {
        return cores[core].isOpticalGrooming(lp);
    }

    /**
     * Retrieves the left slot given this position slot.
     * for this link
     *
     * @param slot the slot position
     * @return the id in left position
     */
    public long getLeftSlot(int slot) {
        return cores[0].getLeftSlot(slot);
    }
    
    /**
     * Retrieves the left slot given this position slot.
     * for this core
     *
     * @param core core id
     * @param slot the slot position
     * @return the id in left position
     */
    public long getLeftSlot(int core, int slot) {
        return cores[core].getLeftSlot(slot);
    }

    /**
     * Retrieves the right slot given this position slot.
     * for this link
     *
     * @param slot the slot position
     * @return the id in right position
     */
    public long getRightSlot(int slot) {
        return cores[0].getRightSlot(slot);
    }
    
    /**
     * Retrieves the right slot given this position slot.
     * for this core
     *
     * @param core core id
     * @param slot the slot position
     * @return the id in right position
     */
    public long getRightSlot(int core, int slot) {
        return cores[core].getRightSlot(slot);
    }

    /**
     * Retrieves the number of simultaneous requests for C size that can be
     * satisfied.
     * for this link
     *
     * @param requiredSlots C = required slots
     * @return number of simultaneous requests for 'requiredSlots' size that can
     * be satisfied
     */
    public int rangeFreeSimultaneous(int requiredSlots) {
        return cores[0].rangeFreeSimultaneous(requiredSlots);
    }
    
    /**
     * Retrieves the number of simultaneous requests for C size that can be
     * satisfied.
     * for this core
     *
     * @param core core id
     * @param requiredSlots C = required slots
     * @return number of simultaneous requests for 'requiredSlots' size that can
     * be satisfied
     */
    public int rangeFreeSimultaneous(int core, int requiredSlots) {
        return cores[core].rangeFreeSimultaneous(requiredSlots);
    }

    /**
     * Retrieves the number of requests for C size that can be satisfied.
     * for this link
     *
     * @param requiredSlots C = required slots
     * @return number of simultaneous requests for 'requiredSlots' size that can
     * be satisfied
     */
    public int rangeFree(int requiredSlots) {
        return cores[0].rangeFree(requiredSlots);
    }
    
    /**
     * Retrieves the number of requests for C size that can be satisfied.
     * for this core
     *
     * @param core core id
     * @param requiredSlots C = required slots
     * @return number of simultaneous requests for 'requiredSlots' size that can
     * be satisfied
     */
    public int rangeFree(int core, int requiredSlots) {
        return cores[core].rangeFree(requiredSlots);
    }

    /**
     * Retrieves the External Fragmentation Rate of this link
     * @return the External Fragmentation Rate of this link
     */
    public double getExFragmentationRate() {
        double frag = 0;
        for (Core core : cores) {
            frag += core.getExFragmentationRate();
        }
        return frag / (double) cores.length;
    }
    
    /**
     * Retrieves the External Fragmentation Rate of this core
     * @param core core id
     * @return the External Fragmentation Rate of this core
     */
    public double getExFragmentationRate(int core) {
        return cores[core].getExFragmentationRate();
    }

    /**
     * Retrieves the Relative Fragmentation Rate of this link based on required slots
     * @param requiredSlots to calculates the Relative Fragmentation Rate
     * @return the Relative Fragmentation Rate of this link based on required slots
     */
    public double getRelativeFragmentationRate(int requiredSlots) {
        int frag = 0;
        for (Core core : cores) {
            frag += core.getRelativeFragmentationRate(requiredSlots);
        }
        return frag / (double) cores.length;
    }
    
    /**
     * Retrieves the Relative Fragmentation Rate of this core based on required slots
     * @param core core id
     * @param requiredSlots to calculates the Relative Fragmentation Rate
     * @return the Relative Fragmentation Rate of this core based on required slots
     */
    public double getRelativeFragmentationRate(int core, int requiredSlots) {
        return cores[core].getRelativeFragmentationRate(requiredSlots);
    }

    /**
     * Retrieves the Moura Fragmentation Rate of this link
     * @return the Moura Fragmentation Rate of this link
     */
    public double getMouraFragmentationRate() {
        int frag = 0;
        for (Core core : cores) {
            frag += core.getMouraFragmentationRate();
        }
        return frag / (double) cores.length;
    }
    
    /**
     * Retrieves the Moura Fragmentation Rate of this core
     * @param core core id
     * @return the Moura Fragmentation Rate of this core
     */
    public double getMouraFragmentationRate(int core) {
        return cores[core].getMouraFragmentationRate();
    }

    /**
     * The first fit slot alocation.
     * for this link
     * @param requiredSlots number of slots required for first fit
     * @return the array with first slots available to 'requiredSlots'
     */
    public int[] firstFit(int requiredSlots) {
        return cores[0].firstFit(requiredSlots);
    }
    
    /**
     * The first fit slot alocation.
     * for this core
     * @param core core id
     * @param requiredSlots number of slots required for first fit
     * @return the array with first slots available to 'requiredSlots'
     */
    public int[] firstFit(int core, int requiredSlots) {
        return cores[core].firstFit(requiredSlots);
    }

    /**
     * The last fit slot alocation.
     * for this link
     * @param requiredSlots number of slots required for last fit
     * @return the array with last slots available to 'requiredSlots'
     */
    public int[] lastFit(int requiredSlots) {
        return cores[0].lastFit(requiredSlots);
    }
    
    /**
     * The last fit slot alocation.
     * for this core
     * @param core core id
     * @param requiredSlots number of slots required for last fit
     * @return the array with last slots available to 'requiredSlots'
     */
    public int[] lastFit(int core, int requiredSlots) {
        return cores[core].lastFit(requiredSlots);
    }

    /**
     * The random fit slot alocation.
     * for this link
     * @param requiredSlots number of slots required for random fit
     * @return the array with random slots available to 'requiredSlots'
     */
    public int[] randomFit(int requiredSlots) {
        return cores[0].randomFit(requiredSlots);
    }
    
    /**
     * The random fit slot alocation.
     * for this core
     * @param core core id
     * @param requiredSlots number of slots required for random fit
     * @return the array with random slots available to 'requiredSlots'
     */
    public int[] randomFit(int core, int requiredSlots) {
        return cores[core].randomFit(requiredSlots);
    }

    /**
     * The exact fit slot alocation.
     * for this link
     * @param requiredSlots number of slots required for exact fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] exactFit(int requiredSlots) {
        return cores[0].exactFit(requiredSlots);
    }
    
    /**
     * The exact fit slot alocation.
     * for this core
     * @param core core id
     * @param requiredSlots number of slots required for exact fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] exactFit(int core, int requiredSlots) {
        return cores[core].exactFit(requiredSlots);
    }
    
    /**
     * The best fit slot alocation.
     * for this link
     * @param requiredSlots number of slots required for best fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] bestFit(int requiredSlots) {
        return cores[0].bestFit(requiredSlots);
    }
    
    /**
     * The best fit slot alocation.
     * for this core
     * @param core core id
     * @param requiredSlots number of slots required for best fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] bestFit(int core, int requiredSlots) {
        return cores[core].bestFit(requiredSlots);
    }
    
    /**
     * The first exact fit slot alocation.
     * for this link
     * @param requiredSlots number of slots required for first exact fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] firstExactFit(int requiredSlots) {
        return cores[0].firstExactFit(requiredSlots);
    }
    
    /**
     * The first exact fit slot alocation.
     * for this core
     * @param core core id
     * @param requiredSlots number of slots required for first exact fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] firstExactFit(int core, int requiredSlots) {
        return cores[core].firstExactFit(requiredSlots);
    }
    
    /**
     * The last exact fit slot alocation.
     * for this link
     * @param requiredSlots number of slots required for last exact fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] lastExactFit(int requiredSlots) {
        return cores[0].lastExactFit(requiredSlots);
    }
    
    /**
     * The last exact fit slot alocation.
     * for this core
     * @param core core id
     * @param requiredSlots number of slots required for last exact fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] lastExactFit(int core, int requiredSlots) {
        return cores[core].lastExactFit(requiredSlots);
    }

    /**
     * Spectrum Alocation TBSA (Traffic Balancing Spectrum Assignment).
     * 
     * Beyranvand, H. and Salehi, J. (2013). A quality-of-transmission aware dynamic 
     * routing and spectrum assignment scheme for future elastic optical networks. Journal of 
     * Lightwave Technology, 31(18):3043–3054.
     * 
     * @param requiredSlots number of slots required
     * @return the array with slots available to 'requiredSlots'
     */
    public int[] tbsa(int requiredSlots) {
        return cores[0].tbsa(requiredSlots);
    }
    
    /**
     * Spectrum Alocation TBSA (Traffic Balancing Spectrum Assignment).
     * 
     * Beyranvand, H. and Salehi, J. (2013). A quality-of-transmission aware dynamic 
     * routing and spectrum assignment scheme for future elastic optical networks. Journal of 
     * Lightwave Technology, 31(18):3043–3054.
     * 
     * For this core
     * 
     * @param core core id
     * @param requiredSlots number of slots required
     * @return the array with slots available to 'requiredSlots'
     */
    public int[] tbsa(int core, int requiredSlots) {
        return cores[core].tbsa(requiredSlots);
    }
    
    /**
     * Retrieves the length of all free blocks in this link
     * @return an ArrayList-Integer- object containing the length of all free blocks in this link
     */
    public ArrayList<Integer> getLengthFreeBlock() {
        ArrayList<Integer> slots = new ArrayList<>();
        for (Core core : cores) {
            slots.addAll(core.getLengthFreeBlock());
        }
        return slots;
    }
    
    /**
     * Retrieves the length of all free blocks in this core
     * @param core core id
     * @return an ArrayList-Integer- object containing the length of all free blocks in this link
     */
    public ArrayList<Integer> getLengthFreeBlock(int core) {
        return cores[core].getLengthFreeBlock();
    }
    
    /**
     * Returns all free blocks in this link.
     * The number returned is the first slot to this block.
     * @return 
     */
    public int[] getfreeBlocks() {
        return cores[0].getfreeBlocks();
    }
    
    /**
     * Returns all free blocks in this core.
     * The number returned is the first slot to this block.
     * @param core core id
     * @return 
     */
    public int[] getfreeBlocks(int core) {
        return cores[core].getfreeBlocks();
    }
    
    /**
     * Returns all free blocks in this core (Here he considers the guard band as a free block).
     * The number returned is the first slot to this block.
     * @return 
     */
    public int[] getfreeBlocksWithGB() {
        return cores[0].getfreeBlocksWithGB();
    }
    
    /**
     * Returns all free blocks in this core (Here he considers the guard band as a free block).The number returned is the first slot to this block.
     * @param core core id
     * @return 
     */
    public int[] getfreeBlocksWithGB(int core) {
        return cores[core].getfreeBlocksWithGB();
    }
    
    /**
     * Returns all occupied blocks in this core (Here he considers the guard band as a free block).
     * The number returned is the first slot to this block.
     * @return 
     */
    public int[] getOccupiedBlocks() {
        return cores[0].getOccupiedBlocks();
    }
    
    /**
     * Returns all occupied in this core (Here he considers the guard band as a free block).The number returned is the first slot to this block.
     * @param core core id
     * @return 
     */
    public int[] getOccupiedBlocks(int core) {
        return cores[core].getOccupiedBlocks();
    }
    
    /**
     * Returns all occupied LightPaths blocks in this link (Here he considers the guard band as a free block).
     * The number returned is the first slot to this block.
     * @return 
     */
    public int[] getOccupiedLightPathsBlocks() {
        return cores[0].getOccupiedLightPathsBlocks();
    }
    
    /**
     * Returns all occupied LightPaths blocks in this core (Here he considers the guard band as a free block).
     * @param core core id
     * @return 
     */
    public int[] getOccupiedLightPathsBlocks(int core) {
        return cores[core].getOccupiedLightPathsBlocks();
    }
    
    /**
     * Returns all free blocks to a final index on this link.
     * The number returned is the first slot to this block.
     * @param start start index
     * @param end end index
     * @return 
     */
    public int[] getfreeBlocksToIndex(int start, int end) {
        return cores[0].getfreeBlocksToIndex(start, end);
    }
    
    /**
     * Returns all free blocks to a final index on this core.
     * The number returned is the first slot to this block.
     * @param core core id
     * @param start start index
     * @param end end index
     * @return 
     */
    public int[] getfreeBlocksToIndex(int core, int start, int end) {
        return cores[core].getfreeBlocksToIndex(start, end);
    }
    
    /**
     * Returns all free blocks that fit into the required slots in this link.
     * The number returned is the first slot to this block.
     * @param requiredSlots number of slots required
     * @return 
     */
    public int[] getfreeRequiredSlotsBlocks(int requiredSlots) {
        return cores[0].getfreeRequiredSlotsBlocks(requiredSlots);
    }
    
    /**
     * Returns all free blocks that fit into the required slots in this core.
     * The number returned is the first slot to this block.
     * @param core core id
     * @param requiredSlots number of slots required
     * @return 
     */
    public int[] getfreeRequiredSlotsBlocks(int core, int requiredSlots) {
        return cores[core].getfreeRequiredSlotsBlocks(requiredSlots);
    }
    
    /**
     * Returns all free blocks that fit into the required slots to a final index on this link.
     * The number returned is the first slot to this block.
     * @param requiredSlots number of slots required
     * @param start start index
     * @param end end index
     * @return 
     */
    public int[] getfreeRequiredSlotsBlocksToIndex(int requiredSlots, int start, int end) {
        return cores[0].getfreeRequiredSlotsBlocksToIndex(requiredSlots, start, end);
    }
    
    /**
     * Returns all free blocks that fit into the required slots to a final index on this core.
     * The number returned is the first slot to this block.
     * @param core core id
     * @param requiredSlots number of slots required
     * @param start start index
     * @param end end index
     * @return 
     */
    public int[] getfreeRequiredSlotsBlocksToIndex(int core, int requiredSlots, int start, int end) {
        return cores[core].getfreeRequiredSlotsBlocksToIndex(requiredSlots, start, end);
    }
    
    /**
     * Retrieves the last available slot of this block.
     * for this link
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotBlock(int firstSlotBlock) {
        return cores[0].getLastSlotBlock(firstSlotBlock);
    }
    
    /**
     * Retrieves the last available slot of this block.
     * for this core
     * @param core core id
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotBlock(int core, int firstSlotBlock) {
        return cores[core].getLastSlotBlock(firstSlotBlock);
    }
    
    /**
     * Retrieves the last available slot of this block (Here he considers the guard band as a free block)
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotBlockWithGB(int firstSlotBlock) {
        return cores[0].getLastSlotBlockWithGB(firstSlotBlock);
    }
    
    /**
     * Retrieves the last available slot of this block (Here he considers the guard band as a free block)
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotBlockWithGB(int core, int firstSlotBlock) {
        return cores[core].getLastSlotBlockWithGB(firstSlotBlock);
    }
    
    /**
     * Retrieves the last occupied slot of this block.
     * for this link
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotOccupiedBlock(int firstSlotBlock) {
        return cores[0].getLastSlotOccupiedBlock(firstSlotBlock);
    }
    
    /**
     * Retrieves the last occupied slot of this block.
     * for this core
     * @param core core id
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotOccupiedBlock(int core, int firstSlotBlock) {
        return cores[core].getLastSlotOccupiedBlock(firstSlotBlock);
    }
    
    /**
     * Verify that it's possible push-pull in this link.
     * @param oldfirstSlot the first slot of the old lightpath
     * @param oldlastSlot the last slot of the old lightpath
     * @param newfirstSlot the first slot of the new lightpath
     * @param newlastSlot the last slot of the new lightpath
     * @return true if is possible to do push-pull or false otherwise
     */
    public boolean pushpull(int oldfirstSlot, int oldlastSlot, int newfirstSlot, int newlastSlot) {
        return cores[0].pushpull(oldfirstSlot, oldlastSlot, newfirstSlot, newlastSlot);
    }
    
    /**
     * Verify that it's possible push-pull in this core.
     * @param core core id
     * @param oldfirstSlot the first slot of the old lightpath
     * @param oldlastSlot the last slot of the old lightpath
     * @param newfirstSlot the first slot of the new lightpath
     * @param newlastSlot the last slot of the new lightpath
     * @return true if is possible to do push-pull or false otherwise
     */
    public boolean pushpull(int core, int oldfirstSlot, int oldlastSlot, int newfirstSlot, int newlastSlot) {
        return cores[core].pushpull(oldfirstSlot, oldlastSlot, newfirstSlot, newlastSlot);
    }
    
    /**
     * Retrieves the Spectrum Compactness (SC) in this Link.
     * YONGLI ZHAO, LIYAZHOU HU, RUIJIE ZHU, XIAOSONG YU, XINBO WANG and JIE ZHANG. 
     * Crosstalk-Aware Spectrum Defragmentation Based on Spectrum Compactness 
     * in Space Division Multiplexing Enabled Elastic Optical Networks With Multicore Fiber. 
     * IEEE Access, 2018
     * 
     * @return the Spectrum Compactness (SC) in this Link
     */
    public double getSC() {
        return cores[0].getSC();
    }
    
    /**
     * Retrieves the Spectrum Compactness (SC) in this Core.
     * YONGLI ZHAO, LIYAZHOU HU, RUIJIE ZHU, XIAOSONG YU, XINBO WANG and JIE ZHANG. 
     * Crosstalk-Aware Spectrum Defragmentation Based on Spectrum Compactness 
     * in Space Division Multiplexing Enabled Elastic Optical Networks With Multicore Fiber. 
     * IEEE Access, 2018
     * 
     * @param core the core id
     * @return the Spectrum Compactness (SC) in this Core
     */
    public double getSC(int core) {
        return cores[core].getSC();
    }

    /**
     * Retrieves the First used slot in this Link;
     * @return the First used slot in this Link;
     */
    public long getFirstUsedSlot() {
        return cores[0].getFirstUsedSlot();
    }
    
    /**
     * Retrieves the First used slot in this Core;
     * @param core the core id
     * @return the First used slot in this Core;
     */
    public long getFirstUsedSlot(int core) {
        return cores[core].getFirstUsedSlot();
    }

    /**
     * Retrieves the Last used slot in this Link;
     * @return the Last used slot in this Link;
     */
    public long getLastUsedSlot() {
        return cores[0].getLastUsedSlot();
    }
    
    /**
     * Retrieves the Last used slot in this Core;
     * @param core the id core
     * @return the Last used slot in this Core;
     */
    public long getLastUsedSlot(int core) {
        return cores[core].getLastUsedSlot();
    }
    
    /**
     * Retrieve all lightpaths ids from this link
     * @return all lightpaths ids from this link
     */
    public ArrayList<Long> getLightpaths() {
        return cores[0].getLightpaths();
    }
    
    /**
     * Retrieve all lightpath ids from this core
     * @param core the core id
     * @return all lightpath ids from this core
     */
    public ArrayList<Long> getLightpaths(int core) {
        return cores[core].getLightpaths();
    }
    
    /**
     * Retrieve the lightpath id in this index
     * @param index index slot in this link
     * @return the lightpath id in this index
     */
    public long getLightpathId(int index) {
        return cores[0].getLightpathId(index);
    }
    
    /**
     * Retrieve the lightpath id in this index from this core
     * @param core the core id
     * @param index slot in this core
     * @return the lightpath id in this index from this core
     */
    public long getLightpathId(int core, int index) {
        return cores[core].getLightpathId(index);
    }
    
    /**
     * Research challenges in optical communications towards 2020 and beyond, 
     * Bostjan Batagelj, Vijay Janyani, Saso Tomazic
     * September 2014Informacije Midem -Ljubljana- 44(3):177 - 184.
     */
    private void definesNeighbors() {
        if(cores.length == 3) {
            cores[0].addNeighbors(cores[1]);
            cores[0].addNeighbors(cores[2]);
            cores[1].addNeighbors(cores[0]);
            cores[1].addNeighbors(cores[2]);
            cores[2].addNeighbors(cores[0]);
            cores[2].addNeighbors(cores[1]);
        }
        if(cores.length == 7) {
            cores[0].addNeighbors(cores[1]);
            cores[0].addNeighbors(cores[5]);
            cores[0].addNeighbors(cores[6]);
            cores[1].addNeighbors(cores[0]);
            cores[1].addNeighbors(cores[2]);
            cores[1].addNeighbors(cores[6]);
            cores[2].addNeighbors(cores[1]);
            cores[2].addNeighbors(cores[3]);
            cores[2].addNeighbors(cores[6]);
            cores[3].addNeighbors(cores[2]);
            cores[3].addNeighbors(cores[4]);
            cores[3].addNeighbors(cores[6]);
            cores[4].addNeighbors(cores[3]);
            cores[4].addNeighbors(cores[5]);
            cores[4].addNeighbors(cores[6]);
            cores[5].addNeighbors(cores[0]);
            cores[5].addNeighbors(cores[4]);
            cores[5].addNeighbors(cores[6]);
            cores[6].addNeighbors(cores[0]);
            cores[6].addNeighbors(cores[1]);
            cores[6].addNeighbors(cores[2]);
            cores[6].addNeighbors(cores[3]);
            cores[6].addNeighbors(cores[4]);
            cores[6].addNeighbors(cores[5]);
        }
        if(cores.length == 13) {
            cores[0].addNeighbors(cores[2]);
            cores[0].addNeighbors(cores[3]);
            cores[1].addNeighbors(cores[2]);
            cores[1].addNeighbors(cores[5]);
            cores[2].addNeighbors(cores[0]);
            cores[2].addNeighbors(cores[1]);
            cores[2].addNeighbors(cores[5]);
            cores[2].addNeighbors(cores[6]);
            cores[2].addNeighbors(cores[3]);
            cores[3].addNeighbors(cores[0]);
            cores[3].addNeighbors(cores[2]);
            cores[3].addNeighbors(cores[6]);
            cores[3].addNeighbors(cores[7]);
            cores[3].addNeighbors(cores[4]);
            cores[4].addNeighbors(cores[3]);
            cores[4].addNeighbors(cores[7]);
            cores[5].addNeighbors(cores[1]);
            cores[5].addNeighbors(cores[2]);
            cores[5].addNeighbors(cores[6]);
            cores[5].addNeighbors(cores[9]);
            cores[5].addNeighbors(cores[8]);
            cores[6].addNeighbors(cores[5]);
            cores[6].addNeighbors(cores[2]);
            cores[6].addNeighbors(cores[3]);
            cores[6].addNeighbors(cores[7]);
            cores[6].addNeighbors(cores[10]);
            cores[6].addNeighbors(cores[9]);
            cores[7].addNeighbors(cores[6]);
            cores[7].addNeighbors(cores[3]);
            cores[7].addNeighbors(cores[4]);
            cores[7].addNeighbors(cores[10]);
            cores[7].addNeighbors(cores[11]);
            cores[8].addNeighbors(cores[5]);
            cores[8].addNeighbors(cores[9]);
            cores[9].addNeighbors(cores[8]);
            cores[9].addNeighbors(cores[5]);
            cores[9].addNeighbors(cores[6]);
            cores[9].addNeighbors(cores[10]);
            cores[9].addNeighbors(cores[12]);
            cores[10].addNeighbors(cores[9]);
            cores[10].addNeighbors(cores[6]);
            cores[10].addNeighbors(cores[7]);
            cores[10].addNeighbors(cores[11]);
            cores[10].addNeighbors(cores[12]);
            cores[11].addNeighbors(cores[10]);
            cores[11].addNeighbors(cores[7]);
            cores[12].addNeighbors(cores[9]);
            cores[12].addNeighbors(cores[10]);
        }
        if(cores.length == 19) {
            cores[0].addNeighbors(cores[3]);
            cores[0].addNeighbors(cores[4]);
            cores[0].addNeighbors(cores[1]);
            cores[1].addNeighbors(cores[0]);
            cores[1].addNeighbors(cores[4]);
            cores[1].addNeighbors(cores[5]);
            cores[1].addNeighbors(cores[2]);
            cores[2].addNeighbors(cores[1]);
            cores[2].addNeighbors(cores[5]);
            cores[2].addNeighbors(cores[6]);
            cores[3].addNeighbors(cores[0]);
            cores[3].addNeighbors(cores[4]);
            cores[3].addNeighbors(cores[8]);
            cores[3].addNeighbors(cores[7]);
            cores[4].addNeighbors(cores[3]);
            cores[4].addNeighbors(cores[0]);
            cores[4].addNeighbors(cores[1]);
            cores[4].addNeighbors(cores[5]);
            cores[4].addNeighbors(cores[9]);
            cores[4].addNeighbors(cores[8]);
            cores[5].addNeighbors(cores[4]);
            cores[5].addNeighbors(cores[1]);
            cores[5].addNeighbors(cores[2]);
            cores[5].addNeighbors(cores[6]);
            cores[5].addNeighbors(cores[10]);
            cores[5].addNeighbors(cores[9]);
            cores[6].addNeighbors(cores[2]);
            cores[6].addNeighbors(cores[5]);
            cores[6].addNeighbors(cores[10]);
            cores[6].addNeighbors(cores[11]);
            cores[7].addNeighbors(cores[3]);
            cores[7].addNeighbors(cores[8]);
            cores[7].addNeighbors(cores[12]);
            cores[8].addNeighbors(cores[7]);
            cores[8].addNeighbors(cores[3]);
            cores[8].addNeighbors(cores[4]);
            cores[8].addNeighbors(cores[9]);
            cores[8].addNeighbors(cores[13]);
            cores[8].addNeighbors(cores[12]);
            cores[9].addNeighbors(cores[8]);
            cores[9].addNeighbors(cores[4]);
            cores[9].addNeighbors(cores[5]);
            cores[9].addNeighbors(cores[10]);
            cores[9].addNeighbors(cores[14]);
            cores[9].addNeighbors(cores[13]);
            cores[10].addNeighbors(cores[9]);
            cores[10].addNeighbors(cores[5]);
            cores[10].addNeighbors(cores[6]);
            cores[10].addNeighbors(cores[11]);
            cores[10].addNeighbors(cores[15]);
            cores[10].addNeighbors(cores[14]);
            cores[11].addNeighbors(cores[6]);
            cores[11].addNeighbors(cores[10]);
            cores[11].addNeighbors(cores[15]);
            cores[12].addNeighbors(cores[7]);
            cores[12].addNeighbors(cores[8]);
            cores[12].addNeighbors(cores[13]);
            cores[12].addNeighbors(cores[16]);
            cores[13].addNeighbors(cores[12]);
            cores[13].addNeighbors(cores[8]);
            cores[13].addNeighbors(cores[9]);
            cores[13].addNeighbors(cores[14]);
            cores[13].addNeighbors(cores[17]);
            cores[13].addNeighbors(cores[16]);
            cores[14].addNeighbors(cores[13]);
            cores[14].addNeighbors(cores[9]);
            cores[14].addNeighbors(cores[10]);
            cores[14].addNeighbors(cores[15]);
            cores[14].addNeighbors(cores[18]);
            cores[14].addNeighbors(cores[17]);
            cores[15].addNeighbors(cores[11]);
            cores[15].addNeighbors(cores[10]);
            cores[15].addNeighbors(cores[14]);
            cores[15].addNeighbors(cores[18]);
            cores[16].addNeighbors(cores[12]);
            cores[16].addNeighbors(cores[13]);
            cores[16].addNeighbors(cores[17]);
            cores[17].addNeighbors(cores[16]);
            cores[17].addNeighbors(cores[13]);
            cores[17].addNeighbors(cores[14]);
            cores[17].addNeighbors(cores[18]);
            cores[18].addNeighbors(cores[17]);
            cores[18].addNeighbors(cores[14]);
            cores[18].addNeighbors(cores[15]);
        }
    }

    /**
     * Retrieves if another Core in this link has the same spectrum available of this core
     * @param firstSlot the first slot of the core
     * @param lastSlot the last slot of the core
     * @param core the analyzed core
     * @return true if another Core in this link has the same spectrum available of this core, false otherwise
     */
    public boolean hasAvailableCore(int firstSlot, int lastSlot, Core core) {
        boolean flag;
        for (Core c : cores) {
            flag = true;
            if(core.ID != c.ID) {
                for (int s = firstSlot; s <= lastSlot; s++) {
                    if(c.slots[s] != 0) {
                        flag = false;
                        break;
                    }
                }
            }
            if(flag) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Retrieves the Cores in this link has the same spectrum available of this core
     * @param firstSlot the first slot of the core
     * @param lastSlot the last slot of the core
     * @param core the analyzed core
     * @return the Cores in this link has the same spectrum available of this core
     */
    public int[] getAvailableCore(int firstSlot, int lastSlot, Core core) {
        ArrayList<Integer> availableCoresList = new ArrayList<>();
        boolean flag;
        for (Core c : cores) {
            flag = true;
            if(core.ID != c.ID) {
                for (int s = firstSlot; s <= lastSlot; s++) {
                    if(c.slots[s] != 0) {
                        flag = false;
                        break;
                    }
                }
            }
            if(flag) {
                availableCoresList.add(c.ID);
            }
        }
        return convertListToArray(availableCoresList);
    }
    
    /**
     * Change the spectrum core
     * @param firstSlot the first slot of the core
     * @param lastSlot the last slot of the core
     * @param oldCore the old core
     * @param newCore the new core
     */
    public void changeCore(int firstSlot, int lastSlot, Core oldCore, Core newCore) {
        if(!hasAvailableCore(firstSlot, lastSlot, newCore)) {
            throw (new IllegalArgumentException());
        } else {
            long id = cores[oldCore.ID].slots[firstSlot];
            releaseSlots(oldCore.ID, firstSlot, lastSlot);
            reserveSlots(newCore.ID, id, firstSlot, lastSlot);
        }
    }
    
    /**
     * Change the spectrum frequency in this link.
     * @param oldfirstSlot the first slot of the old lightpath
     * @param oldlastSlot the last slot of the old lightpath
     * @param newfirstSlot the first slot of the new lightpath
     * @param newlastSlot the last slot of the new lightpath
     */
    public void changeSpectrum(int oldfirstSlot, int oldlastSlot, int newfirstSlot, int newlastSlot) {
        cores[0].changeSpectrum(oldfirstSlot, oldlastSlot, newfirstSlot, newlastSlot);
    }
    
    /**
     * Change the spectrum frequency in this core.
     * @param core the id core
     * @param oldfirstSlot the first slot of the old lightpath
     * @param oldlastSlot the last slot of the old lightpath
     * @param newfirstSlot the first slot of the new lightpath
     * @param newlastSlot the last slot of the new lightpath
     */
    public void changeSpectrum(int core, int oldfirstSlot, int oldlastSlot, int newfirstSlot, int newlastSlot) {
        cores[core].changeSpectrum(oldfirstSlot, oldlastSlot, newfirstSlot, newlastSlot);
    }
    
    private int[] convertListToArray(ArrayList<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
