package ons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeSet;

/**
 * The Space Division Multiplexing Elastic Optical Network (SDM-EON) Core represents a Fiberlink core in an optical
 * network divided by slots.
 *
 * @author lucasrc
 */
public class Core {
    
    protected int ID;
    protected int linkID;
    protected int numSlots;
    public long slots[];
    protected static int guardband;
    protected ArrayList<Core> neighbors;
    
    public Core(int ID, int linkID, int numSlots, int guardband) {
        this.ID = ID;
        this.linkID = linkID;
        this.numSlots = numSlots;
        this.slots = new long[this.numSlots];
        for (int i = 0; i < this.numSlots; i++) {
            this.slots[i] = 0;
        }
        this.guardband = guardband;
        this.neighbors = new ArrayList<>();
    }
    
    protected void addNeighbors(Core core) {
        neighbors.add(core);
    }

    public ArrayList<Core> getNeighbors() {
        return neighbors;
    }

    public int getID() {
        return ID;
    }

    public int getLinkID() {
        return linkID;
    }
    
    /**
     * Retrieves the slots.
     *
     * @return the slots of this core
     */
    public long[] getSlots() {
        return slots;
    }
    
    /**
     * Retrieves the guardband size.
     *
     * @return the guardband size
     */
    public int getGuardband() {
        return this.guardband;
    }

    /**
     * Retrieves the number of slots in this core.
     *
     * @return the number of slots in this core
     */
    public int getNumSlots() {
        return numSlots;
    }
    
    /**
     * Retrieves the slots available in this core.
     *
     * @return the number slots available
     */
    public int getAvaiableSlots() {
        int cont = 0;
        for (int i = 0; i < numSlots; i++) {
            if (this.slots[i] == 0) {
                cont++;
            }
        }
        return cont;
    }

    /**
     * Retrieves the used slots in this core.
     *
     * @return the number used slots
     */
    public int getUsedSlots() {
        return numSlots - getAvaiableSlots();
    }

    /**
     * Check if there is available slots to accommodate the request.
     *
     * @param requiredSlots the request slots
     * @return true if can be accommodate, false otherwise
     */
    public boolean hasSlotsAvaiable(int requiredSlots) {
        if (requiredSlots > numSlots) {
            //throw (new IllegalArgumentException());
            return false;
        }
        int i = 0, cont = 0;
        while (i < numSlots) {
            if (this.slots[i] == 0) {
                cont++;
                if (cont == requiredSlots) {
                    return true;
                }
                i++;
            } else {
                cont = 0;
                i++;
            }
        }
        return false;
    }

    /**
     * Retrieves the first slot available to accommodate this requisition.
     *
     * @param requiredSlots the required slots
     * @return the first slot available to accommodate this requisition
     */
    public int getFirstSlotAvailable(int requiredSlots) {
        if (requiredSlots > numSlots) {
            throw (new IllegalArgumentException());
        }
        int i = 0, cont = 0;
        while (i < numSlots) {
            if (this.slots[i] == 0) {
                cont++;
                if (cont == requiredSlots) {
                    return i - requiredSlots + 1;
                }
                i++;
            } else {
                cont = 0;
                i++;
            }
        }
        return -1;
    }

    /**
     * Retrieves the set of slots available given a minimum size.
     *
     * @param requiredSlots the required slots of set
     * @return the set with first slots available to 'requiredSlots'
     */
    public ArrayList<Integer> getSlotsAvailable(int requiredSlots) {//look at all available spaces considering guard band
        ArrayList<Integer> slotsAvailable = new ArrayList<>();
        for (int i = 0; i <= numSlots - requiredSlots; i++) {
            if (this.slots[i] == 0) {
                if (areSlotsAvaiable(i, i + requiredSlots - 1)) {
                    slotsAvailable.add(i);
                }
            }
        }
        return slotsAvailable;
    }

    /**
     * Retrieves the set of slots available in optical grooming given a minimum
     * size.
     *
     * @param requiredSlots the required slots of set
     * @return the set with first slots available in optical grooming to
     * 'requiredSlots'
     */
    public TreeSet<Integer> getSlotsAvailableOG(int requiredSlots) {
        TreeSet<Integer> slotsAvailable = new TreeSet<>();
        int[] position;
        for (int i = 0; i <= numSlots - requiredSlots; i++) {
            if (this.slots[i] > 0) {
                position = findLightpath(this.slots[i]);
                if (rightExtend(requiredSlots, this.slots[i])) {
                    slotsAvailable.add(position[1] + 1);
                }
                if (leftExtend(requiredSlots, this.slots[i])) {
                    slotsAvailable.add(position[0] - requiredSlots);
                }
            }
        }
        return slotsAvailable;
    }

    /**
     * Retrieves the set of slots available in optical grooming or not given a
     * minimum size.
     *
     * @param requiredSlots the required slots of set
     * @return the set with first slots available in optical grooming or not to
     * 'requiredSlots'
     */
    public TreeSet<Integer> getSlotsAvailableGlobal(int requiredSlots) {
        TreeSet<Integer> slotsAvailable = new TreeSet<>();
        int[] position;
        for (int i = 0; i <= numSlots - requiredSlots; i++) {
            if (this.slots[i] == 0) {
                if (areSlotsAvaiable(i, i + requiredSlots - 1)) {
                    slotsAvailable.add(i);
                }
            } else if (this.slots[i] > 0) {
                position = findLightpath(this.slots[i]);
                if (rightExtend(requiredSlots, this.slots[i])) {
                    slotsAvailable.add(position[1] + 1);
                }
                if (leftExtend(requiredSlots, this.slots[i])) {
                    slotsAvailable.add(position[0] - requiredSlots);
                }
            }
        }
        return slotsAvailable;
    }

    /**
     * Retrieves the set of slots available given a minimum size [but in array
     * of 'int'].
     *
     * @param requiredSlots the required slots of set
     * @return the array with first slots available to 'requiredSlots'
     */
    public int[] getSlotsAvailableToArray(int requiredSlots) {
        ArrayList<Integer> slotsAvailable = getSlotsAvailable(requiredSlots);
        int[] out = new int[slotsAvailable.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = slotsAvailable.get(i);
        }
        return out;
    }

    /**
     * Retrieves the set of slots available in optical grooming given a minimum
     * size [but in array of 'int'].
     *
     * @param requiredSlots the required slots of set
     * @return the array with first slots available in optical grooming to
     * 'requiredSlots'
     */
    public int[] getSlotsAvailableOGToArray(int requiredSlots) {
        TreeSet<Integer> slotsAvailable = getSlotsAvailableOG(requiredSlots);
        int[] out = new int[slotsAvailable.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = slotsAvailable.pollFirst();
        }
        return out;
    }

    /**
     * Retrieves the set of slots available in optical grooming or not given a
     * minimum size [but in array of 'int'].
     *
     * @param requiredSlots the required slots of set
     * @return the array with first slots available in optical grooming or not
     * to 'requiredSlots'
     */
    public int[] getSlotsAvailableGlobalToArray(int requiredSlots) {
        TreeSet<Integer> slotsAvailable = getSlotsAvailableGlobal(requiredSlots);
        int[] out = new int[slotsAvailable.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = slotsAvailable.pollFirst();
        }
        return out;
    }

    /**
     * Checks for available slots considering the guard band.
     *
     * @param begin the begin slot
     * @param end the end slot
     * @return true if is avaiable slots, false otherwise
     */
    public boolean areSlotsAvaiable(int begin, int end) {
        if (begin < 0 || end >= numSlots || begin > end) {
            //throw (new IllegalArgumentException());
            return false;
        }
        if (begin < this.guardband) {
            while (begin <= end) {
                if (this.slots[begin] == 0) {
                    begin++;
                } else {
                    return false;
                }
            }
            for (int i = 0; i < this.guardband && begin < slots.length; i++) {
                if (this.slots[begin] == -1 || this.slots[begin] == 0) {
                    begin++;
                } else {
                    return false;
                }
            }
            return true;
        } else if (end > (numSlots - 1) - this.guardband) {
            for (int i = (begin - this.guardband); i < begin; i++) {
                if (!(this.slots[i] == -1 || this.slots[i] == 0)) {
                    return false;
                }
            }
            while (begin <= end) {
                if (this.slots[begin] == 0) {
                    begin++;
                } else {
                    return false;
                }
            }
            return true;
        } else {
            for (int i = (begin - this.guardband); i < begin; i++) {
                if (!(this.slots[i] == -1 || this.slots[i] == 0)) {
                    return false;
                }
            }
            while (begin <= end) {
                if (this.slots[begin] == 0) {
                    begin++;
                } else {
                    return false;
                }
            }
            for (int i = 0; i < this.guardband; i++) {
                if (this.slots[begin] == -1 || this.slots[begin] == 0) {
                    begin++;
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Reserve slots (with guard band) in this core, ie reserve lightpath.
     *
     * @param id the id of lightpath reserved
     * @param begin the begin of lightpath
     * @param end the end of lightpath
     */
    protected void reserveSlots(long id, int begin, int end) {
        if (begin < 0 || end >= numSlots || begin > end || id == 0) {
            throw (new IllegalArgumentException());
        }
        if (begin < this.guardband) {
            for (int i = 0; i < begin; i++) {
                this.slots[i] = -1;
            }
            while (begin <= end) {
                this.slots[begin] = id;
                begin++;
            }
            for (int i = 0; i < this.guardband; i++) {
                this.slots[begin] = -1;
                begin++;
            }
        } else if (end > (numSlots - 1) - this.guardband) {
            for (int i = (numSlots - 1); i > end; i--) {
                this.slots[i] = -1;
            }
            for (int i = (begin - this.guardband); i < begin; i++) {
                this.slots[i] = -1;
            }
            while (begin <= end) {
                this.slots[begin] = id;
                begin++;
            }
        } else {
            for (int i = (begin - this.guardband); i < begin; i++) {
                this.slots[i] = -1;
            }
            while (begin <= end) {
                this.slots[begin] = id;
                begin++;
            }
            for (int i = 0; i < this.guardband; i++) {
                this.slots[begin] = -1;
                begin++;
            }
        }
    }

    /**
     * Reserve slots (with guard band only the lightpath is on the edge) in this
     * core, ie reserve lightpath using optical grooming technique.
     *
     * @param id the id of lightpath reserved
     * @param begin the begin of lightpath
     * @param end the end of lightpath
     */
    protected void reserveSlotsInOpticalGrooming(long id, int begin, int end) {
        if (begin < 0 || end >= numSlots || begin > end || id == 0) {
            throw (new IllegalArgumentException());
        }
        //"-2" is the free slot inter lps (tunnel middle)
        if (slots[begin] == -2) {
            while (begin <= end) {
                this.slots[begin] = id;
                begin++;
            }
        } else//se ele tiver na borda esquerda
        if (begin < guardband) {            
            while (begin <= end) {
                this.slots[begin] = id;
                begin++;
            }
            if (slots[begin] == -1 || slots[begin] == 0) {
                for (int i = 0; i < guardband; i++) {
                    slots[begin] = -1;
                    begin++;
                }
            }
        } else//se ele tiver na borda direita
        if (end > (numSlots - 1) - this.guardband) {
            if (slots[begin - 1] == -1 || slots[begin - 1] == 0) {
                for (int i = (begin - this.guardband); i < begin; i++) {
                    this.slots[i] = -1;
                }
            }
            while (begin <= end) {
                this.slots[begin] = id;
                begin++;
            }
        } else {//se nao
            if (slots[begin - 1] == -1 || slots[begin - 1] == 0) {
                for (int i = (begin - this.guardband); i < begin; i++) {
                    this.slots[i] = -1;
                }
            }
            while (begin <= end) {
                this.slots[begin] = id;
                begin++;
            }
            if (slots[begin] == -1 || slots[begin] == 0) {
                for (int i = 0; i < this.guardband; i++) {
                    this.slots[begin] = -1;
                    begin++;
                }
            }
        }
    }

    /**
     * Release slots from this core. Examines the corresponding guard bands
     *
     * @param begin the begin
     * @param end the end
     */
    protected void releaseSlots(int begin, int end) {//method rather complicated. DO NOT TOUCH!!!
        if (begin < 0 || end >= numSlots || begin > end) {
            throw (new IllegalArgumentException());
        }
        if (begin < this.guardband) {
            for (int i = 0; i < begin; i++) {
                this.slots[i] = 0;
            }
            while (begin <= end) {
                this.slots[begin] = 0;
                begin++;
            }
            if (this.slots[begin + this.guardband] == 0) {
                for (int i = 0; i < this.guardband; i++) {
                    this.slots[begin] = 0;
                    begin++;
                }
            } else if (this.slots[begin + this.guardband] == -1) {
                int k = begin + this.guardband;
                while (this.slots[k] == -1) {
                    k++;
                }
                int tirar = k - (begin + this.guardband);
                for (int i = 0; i < tirar; i++) {
                    this.slots[begin] = 0;
                    begin++;
                }
            }
        } else if (end > (numSlots - 1) - this.guardband) {
            for (int i = (numSlots - 1); i > end; i--) {
                this.slots[i] = 0;
            }
            if (this.slots[begin - this.guardband - 1] == 0) {
                for (int i = (begin - this.guardband); i < begin; i++) {
                    this.slots[i] = 0;
                }
            }
            if (this.slots[begin - this.guardband - 1] == -1) {
                int k = begin - this.guardband - 1;
                while (this.slots[k] == -1) {
                    k--;
                }
                int tirar = (begin - this.guardband - 1) - k;
                for (int i = (begin - tirar); i < begin; i++) {
                    this.slots[i] = 0;
                }
            }
            while (begin <= end) {
                this.slots[begin] = 0;
                begin++;
            }
        } else {
            if (begin == this.guardband) {
                for (int i = (begin - this.guardband); i < begin; i++) {
                    this.slots[i] = 0;
                }
            } else {
                if (this.slots[begin - this.guardband - 1] == 0) {
                    for (int i = (begin - this.guardband); i < begin; i++) {
                        this.slots[i] = 0;
                    }
                }
                if (this.slots[begin - this.guardband - 1] == -1) {
                    int k = begin - this.guardband - 1;
                    while (this.slots[k] == -1) {
                        k--;
                    }
                    int tirar = (begin - this.guardband - 1) - k;
                    for (int i = (begin - tirar); i < begin; i++) {
                        this.slots[i] = 0;
                    }
                }
            }
            while (begin <= end) {
                this.slots[begin] = 0;
                begin++;
            }
            if (end == numSlots - 1 - this.guardband) {
                for (int i = 0; i < this.guardband; i++) {
                    this.slots[begin] = 0;
                    begin++;
                }
            } else if (this.slots[begin + this.guardband] == 0) {
                for (int i = 0; i < this.guardband; i++) {
                    this.slots[begin] = 0;
                    begin++;
                }
            } else if (this.slots[begin + this.guardband] == -1) {
                int k = begin + this.guardband;
                while (this.slots[k] == -1) {
                    k++;
                }
                int tirar = k - (begin + this.guardband);
                for (int i = 0; i < tirar; i++) {
                    this.slots[begin] = 0;
                    begin++;
                }
            }
        }
    }

    /**
     * Release slots from this core belonging to a tunnel. Examines the
     * corresponding guard bands in the edges
     *
     * @param begin the begin
     * @param end the end
     */
    protected void releaseSlotsInOpticalGrooming(int begin, int end) {//simulator method more complicated. DO NOT TOUCH!!!
        if (begin < 0 || end >= numSlots || begin > end) {
            throw (new IllegalArgumentException());
        }
        int slotsRemoved = end - begin + 1;
        if (begin == 0) {//inicio de tudo
            if (slots[end + 1] == -2) {//se a direita tem -2
                //encontrar ate onde o -2 vai
                int position = end + 1;
                while (slots[position] == -2) {
                    position++;
                }
                end = position - 1;
                slotsRemoved = end - begin + 1;
                for (int i = 0; i < slotsRemoved; i++) {
                    slots[begin + i] = 0;
                }
                if (end - guardband + 1 >= 0) {
                    for (int i = 0; i < guardband; i++) {
                        slots[end - i] = -1;
                    }
                }
            } else {
                for (int i = 0; i < slotsRemoved; i++) {
                    slots[begin + i] = 0;
                }
                if (end - guardband + 1 >= 0) {
                    for (int i = 0; i < guardband; i++) {
                        slots[end - i] = -1;
                    }
                }
            }
        } else if (end == numSlots - 1) {//fim de tudo
            if (slots[begin - 1] == -2) {//se a esquerda tem -2 
                //encontrar ate onde o -2 vai
                int position = begin - 1;
                while (slots[position] == -2) {
                    position--;
                }
                begin = position + 1;
                slotsRemoved = end - begin + 1;
                for (int i = 0; i < slotsRemoved; i++) {
                    slots[begin + i] = 0;
                }
                if (begin + guardband - 1 < numSlots) {
                    for (int i = 0; i < guardband; i++) {
                        slots[begin + i] = -1;
                    }
                }
            } else {
                for (int i = 0; i < slotsRemoved; i++) {
                    slots[begin + i] = 0;
                }
                if (begin + guardband - 1 < numSlots) {
                    for (int i = 0; i < guardband; i++) {
                        slots[begin + i] = -1;
                    }
                }
            }
        } else if (slots[begin - 1] == 0 || slots[begin - 1] == -1) {//se ta esquerda do tunel
            if (begin - guardband - 1 > -1 && slots[begin - guardband - 1] != 0) {//se a esquerda do tunel tem um lp
                //descobrir onde esse lp ta
                int lp = begin - guardband;
                for (int i = 0; i < guardband + guardband; i++) {
                    if (slots[lp] > 0) {
                        break;
                    }
                    lp--;
                }
                //coloca a guarda ao lado desse lp...
                for (int i = 1; i <= guardband; i++) {
                    slots[lp + i] = -1;
                }
                //zera em diante até o begin
                for (int i = lp + guardband + 1; i < begin; i++) {
                    slots[i] = 0;
                }
                if (slots[end + 1] == -2) {//se a direita tem -2 
                    //encontrar ate onde o -2 vai
                    int position = end + 1;
                    while (slots[position] == -2) {
                        position++;
                    }
                    end = position - 1;
                    slotsRemoved = end - begin + 1;
                    for (int i = 0; i < slotsRemoved; i++) {
                        slots[end - i] = 0;
                    }
                    for (int i = 0; i < guardband; i++) {
                        slots[end - i] = -1;
                    }
                } else {
                    for (int i = 0; i < slotsRemoved; i++) {
                        slots[end - i] = 0;
                    }
                    for (int i = 0; i < guardband; i++) {
                        slots[end - i] = -1;
                    }
                }
            } else //a esquerda nao tem lp
            //se a direita tem -2 
            if (slots[end + 1] == -2) {
                //encontrar ate onde o -2 vai
                int position = end + 1;
                while (slots[position] == -2) {
                    position++;
                }
                end = position - 1;
                slotsRemoved = end - begin + 1;
                for (int i = 0; i < slotsRemoved + guardband && end - i > -1; i++) {
                    slots[end - i] = 0;
                }
                for (int i = 0; i < guardband; i++) {
                    slots[end - i] = -1;
                }
            } else {
                //se a direita NAO tem -2 
                for (int i = 0; i < slotsRemoved + guardband && end - i > -1; i++) {
                    slots[end - i] = 0;
                }
                for (int i = 0; i < guardband; i++) {
                    slots[end - i] = -1;
                }
            }
        } else if (slots[end + 1] == 0 || slots[end + 1] == -1) {//se ta direita do tunel
            if (end + guardband + 1 < numSlots && slots[end + guardband + 1] != 0) {//se a direita do tunel tem um lp
                //descobrir onde esse lp ta
                int lp = end + guardband;
                for (int i = 0; i < guardband + guardband; i++) {
                    if (slots[lp] > 0) {
                        break;
                    }
                    lp++;
                }
                //coloca a guarda ao lado desse lp...
                for (int i = 1; i <= guardband; i++) {
                    slots[lp - i] = -1;
                }
                //zera em diante até o end
                for (int i = lp - guardband - 1; i > end; i--) {
                    slots[i] = 0;
                }
                if (slots[begin - 1] == -2) {//se a esquerda tem -2 
                    int position = begin - 1;
                    while (slots[position] == -2) {
                        position--;
                    }
                    begin = position + 1;
                    slotsRemoved = end - begin + 1;
                    for (int i = 0; i < slotsRemoved; i++) {
                        slots[begin + i] = 0;
                    }
                    for (int i = 0; i < guardband; i++) {
                        slots[begin + i] = -1;
                    }
                } else {
                    for (int i = 0; i < slotsRemoved; i++) {
                        slots[begin + i] = 0;
                    }
                    for (int i = 0; i < guardband; i++) {
                        slots[begin + i] = -1;
                    }
                }
            } else //se a direita nao tem um lp
            //se a esquerda tem -2 
            if (slots[begin - 1] == -2) {
                //encontrar ate onde o -2 vai
                int position = begin - 1;
                while (slots[position] == -2) {
                    position--;
                }
                begin = position + 1;
                slotsRemoved = end - begin + 1;
                for (int i = 0; i < slotsRemoved + guardband && begin + i < numSlots; i++) {
                    slots[begin + i] = 0;
                }
                for (int i = 0; i < guardband; i++) {
                    slots[begin + i] = -1;
                }
            } else {
                //se a esquerda NAO tem -2 
                for (int i = 0; i < slotsRemoved + guardband && begin + i < numSlots; i++) {
                    slots[begin + i] = 0;
                }
                for (int i = 0; i < guardband; i++) {
                    slots[begin + i] = -1;
                }
            }
        } else {
            //ele ta no meio do tunel
            for (int i = 0; i < slotsRemoved; i++) {
                slots[begin + i] = -2;
            }
        }
    }

    /**
     * Retrieves the max size of contiguous slots available.
     *
     * @return the max size of contiguous slots available
     */
    public int maxSizeAvaiable() {
        int cont = 0, max = 0;
        for (int i = 0; i < numSlots; i++) {
            if (this.slots[i] == 0) {
                cont++;
            } else {
                if (cont > max) {
                    max = cont;
                }
                cont = 0;
            }
        }
        if (cont > max) {
            return cont;
        }
        return max;
    }

    /**
     * Retrieves the minimun size of contiguous slots available.
     *
     * @return the minimun size of contiguous slots available
     */
    public int minSizeAvaiable() {
        int cont = 0, min = numSlots;
        boolean flag = true;
        for (int i = 0; i < numSlots; i++) {
            if (this.slots[i] == 0) {
                cont++;
                flag = true;
            } else {
                if (cont < min && flag) {
                    min = cont;
                    flag = false;
                }
                cont = 0;
            }
        }
        if (cont < min && flag) {
            return cont;
        }
        return min;
    }

    /**
     * Check if this lightpath is this core
     *
     * @param id the id of lightpath
     * @return if it is true, false otherwise
     */
    public boolean hasThisLightPath(long id) {
        if (id < 0) {
            throw (new IllegalArgumentException());
        }
        int i = 0;
        while (i < numSlots) {
            if (this.slots[i] == id) {
                return true;
            } else {
                i++;
            }
        }
        return false;
    }

    /**
     * Retrieves the information of this core
     *
     * @return the string information
     */
    @Override
    public String toString() {
        String core = "LinkID: " + linkID + "; Slots:" + numSlots;
        return core;
    }

    /**
     * Print this core for debug.
     */
    public void printCore() {
        System.out.print("\n linkID:" + this.linkID + " |" + "ID: " + this.ID + " |");
        for (int i = 0; i < numSlots; i++) {
            System.out.print(this.slots[i] + "|");
        }
        System.out.println("");
    }
    
    /**
     * Print the latest 10 slots of this core, for debug.
     */
    public void printEndCore() {
        System.out.print("\n linkID:" + this.linkID + " |" + "ID: " + this.ID + " |");
        for (int i = numSlots-10; i < numSlots; i++) {
            System.out.print(this.slots[i] + "|");
        }
        System.out.println("");
    }

    /**
     * Retrieves the lightpath slot position.
     *
     * @param lp the id of lightpath
     * @return the array[2] with begin position and end position
     */
    public int[] findLightpath(long lp) {
        if (lp <= 0) {
            throw (new IllegalArgumentException());
        }
        int[] position = {-1, -1};
        int i;
        boolean b_flag = true, e_flag = false, flag = false;
        for (i = 0; i < numSlots; i++) {
            if (e_flag) {
                position[1] = i - 1;
                e_flag = false;
            }
            if (slots[i] == lp) {
                if (b_flag) {
                    position[0] = i;
                    b_flag = false;
                }
                e_flag = true;
                flag = true;
            }
            if (flag && !e_flag) {
                break;
            }
        }
        if (e_flag) {
            position[1] = i - 1;
        }
        return position;
    }

    /**
     * Examine whether it is possible to extend lightpath to left.
     *
     * @param requiredSlots the number of slots to be extend to left
     * @param lp the lightpath id
     * @return true if is possible, false otherwise
     */
    public boolean leftExtend(int requiredSlots, long lp) {
        int[] position = findLightpath(lp);
        if (position[0] == -1 || position[1] == -1) {
            throw (new IllegalArgumentException());
        }
        int count = 0;
        //Se ele esta na borda esquerda do tunel
        if (position[0] - 1 > -1 && (slots[position[0] - 1] == -1 || slots[position[0] - 1] == 0)) {
            int i;
            for (i = position[0] - 1; i > -1 && i > (position[0] - 1 - requiredSlots - guardband); i--) {
                if (slots[i] == -1 || slots[i] == 0) {
                    count++;
                } else {
                    return false;
                }
            }
            if(i == -1) {
                return count >= requiredSlots;
            } else {
                return count == requiredSlots + guardband;
            }
        } else {//entao ele nao ficara na borda do tunel (inter tunnel)
            for (int i = position[0] - 1; i > -1 && i > (position[0] - 1 - requiredSlots); i--) {
                if (slots[i] == -2) {
                    count++;
                }
            }
            return count == requiredSlots;
        }
    }

    /**
     * Examine whether it is possible to extend lightpath to right.
     *
     * @param requiredSlots the number of slots to be extend to right
     * @param lp the lightpath id
     * @return true if is possible, false otherwise
     */
    public boolean rightExtend(int requiredSlots, long lp) {
        int[] position = findLightpath(lp);
        if (position[0] == -1 || position[1] == -1) {
            throw (new IllegalArgumentException());
        }
        int count = 0;
        //Se ele esta na borda direita do tunel
        if (position[1] + 1 < numSlots && (slots[position[1] + 1] == -1 || slots[position[1] + 1] == 0)) {
            int i;
            for (i = position[1] + 1; i < numSlots && i < (position[1] + 1 + requiredSlots + guardband); i++) {
                if (slots[i] == -1 || slots[i] == 0) {
                    count++;
                } else {
                    return false;
                }
            }
            if(i == numSlots) {
                return count >= requiredSlots;
            } else {
                return count == requiredSlots + guardband;
            }
        } else {//entao ele nao ficara na borda do tunel (inter tunnel)
            for (int i = position[1] + 1; i < numSlots && i < (position[1] + 1 + requiredSlots); i++) {
                if (slots[i] == -2) {
                    count++;
                }
            }
            return count == requiredSlots;
        }
    }

    /**
     * Retrieves if this lightpath is in optical grooming
     *
     * @param lp the lightpath id
     * @return true if this is in optical grooming, false otherwise
     */
    public boolean isOpticalGrooming(long lp) {
        int[] position = findLightpath(lp);
        if (position[0] == -1 || position[1] == -1) {
            return false;
        }
        if (position[0] == 0) {//ponta esquerda
            return !(slots[position[1] + 1] == -1);
        } else if (position[1] == numSlots - 1) {//ponta direita
            return !(slots[position[0] - 1] == -1);
        } else {//ta no meio
            return !((slots[position[0] - 1] == -1 || slots[position[0] - 1] == 0)
                    && (slots[position[1] + 1] == -1 || slots[position[1] + 1] == 0));
        }
    }

    /**
     * Retrieves the left slot given this position slot
     *
     * @param slot the slot position
     * @return the id in left position
     */
    public long getLeftSlot(int slot) {
        if (slot == 0) {
            throw (new IllegalArgumentException());
        }
        return this.slots[slot - 1];
    }

    /**
     * Retrieves the right slot given this position slot
     *
     * @param slot the slot position
     * @return the id in right position
     */
    public long getRightSlot(int slot) {
        if (slot == numSlots - 1) {
            throw (new IllegalArgumentException());
        }
        return this.slots[slot + 1];
    }

    /**
     * Retrieves the number of simultaneous requests for C size that can be
     * satisfied
     *
     * @param requiredSlots C = required slots
     * @return number of simultaneous requests for 'requiredSlots' size that can
     * be satisfied
     */
    public int rangeFreeSimultaneous(int requiredSlots) {
        if (requiredSlots > numSlots) {
            throw (new IllegalArgumentException());
        }
        int i = 0, cont = 0, contPossibles = 0;
        while (i < numSlots) {
            if (this.slots[i] == 0) {
                cont++;
                if (cont == requiredSlots) {
                    contPossibles++;
                    cont = 0;
                }
                i++;
            } else {
                cont = 0;
                i++;
            }
        }
        return contPossibles;
    }

    /**
     * Retrieves the number of requests for C size that can be satisfied
     *
     * @param requiredSlots C = required slots
     * @return number of simultaneous requests for 'requiredSlots' size that can
     * be satisfied
     */
    public int rangeFree(int requiredSlots) {
        if (requiredSlots > numSlots) {
            throw (new IllegalArgumentException());
        }
        int i = 0, cont = 0, contPossibles = 0;
        while (i < numSlots) {
            if (this.slots[i] == 0) {
                cont++;
                if (cont == requiredSlots) {
                    contPossibles++;
                    cont--;
                }
                i++;
            } else {
                cont = 0;
                i++;
            }
        }
        return contPossibles;
    }

    /**
     * Retrieves the External Fragmentation Rate of this core
     * @return the External Fragmentation Rate of this core
     */
    public double getExFragmentationRate() {
        double largestFreeBlock = (double) maxSizeAvaiable();
        double totalFree = (double) getAvaiableSlots();
        if (totalFree == 0) {
            return 1;
        }
        return (double) 1 - (largestFreeBlock / totalFree);
    }

    /**
     * Retrieves the Relative Fragmentation Rate of this core based on required slots
     * @param requiredSlots to calculates the Relative Fragmentation Rate
     * @return the Relative Fragmentation Rate of this core based on required slots
     */
    public double getRelativeFragmentationRate(int requiredSlots) {
        double rangeFree = (double) rangeFreeSimultaneous(requiredSlots);
        double totalFree = (double) getAvaiableSlots();
        if (totalFree == 0) {
            return 1;
        }
        return (double) 1 - ((requiredSlots * rangeFree) / totalFree);
    }

    /**
     * Retrieves the Moura Fragmentation Rate of this core
     * @return the Moura Fragmentation Rate of this core
     */
    public double getMouraFragmentationRate() {
        double largestFreeBlock = (double) maxSizeAvaiable();
        double totalFree = (double) getAvaiableSlots();
        if (totalFree == 0) {
            return 1;
        }
        return (double) ((totalFree - largestFreeBlock) / totalFree);
    }

    /**
     * The first fit slot alocation
     * @param requiredSlots number of slots required for first fit
     * @return the array with first slots available to 'requiredSlots'
     */
    public int[] firstFit(int requiredSlots) {
        return getSlotsAvailableToArray(requiredSlots);
    }

    /**
     * The last fit slot alocation
     * @param requiredSlots number of slots required for last fit
     * @return the array with last slots available to 'requiredSlots'
     */
    public int[] lastFit(int requiredSlots) {
        int[] ff = getSlotsAvailableToArray(requiredSlots);
        int[] lf = new int[ff.length];
        for (int i = ff.length - 1, j = 0; i >= 0; i--, j++) {
            lf[j] = ff[i];
        }
        return lf;
    }

    /**
     * The random fit slot alocation
     * @param requiredSlots number of slots required for random fit
     * @return the array with random slots available to 'requiredSlots'
     */
    public int[] randomFit(int requiredSlots) {
        int[] rf = getSlotsAvailableToArray(requiredSlots);
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < rf.length; i++) {
            list.add(rf[i]);
        }
        Collections.shuffle(list);
        for (int i = 0; i < rf.length; i++) {
            rf[i] = list.get(i);
        }
        return rf;
    }

    /**
     * The exact fit slot alocation
     * @param requiredSlots number of slots required for exact fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] exactFit(int requiredSlots) {
        int[] bf = bestFit(requiredSlots);
        int[] ef = new int[bf.length];
        ArrayList<Integer> selectedBlock = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < bf.length; i++) {
            if (isExactFit(bf[i], requiredSlots)) {
                ef[j] = bf[i];
                j++;
                selectedBlock.add(i);
            }
        }
        for (int i = bf.length - 1; i > 0; i--) {
            if (!selectedBlock.contains(i)) {
                ef[j] = bf[i];
                j++;
                selectedBlock.add(i);
            }
        }
        return ef;
    }
    
    /**
     * The best fit slot alocation.
     * Blocks closer to request slots
     * @param requiredSlots number of slots required for best fit
     * @return the array with exact slots available to 'requiredSlots'
     */
    public int[] bestFit(int requiredSlots) {
        int[] bfAux = firstFit(requiredSlots);
        int[] bf = new int[bfAux.length];
        int[] sizeBlock = new int[bfAux.length];
        for (int s = 0; s < bfAux.length; s++) {
            sizeBlock[s] = getSizeBlock(bfAux[s]);
        }
        int aux1, aux2;
        boolean swap = true;
        while (swap) {
            swap = false;
            for (int i = 0; i < bfAux.length - 1; i++) {
                if (sizeBlock[i] > sizeBlock[i + 1]) {
                    swap = true;
                    aux1 = bfAux[i];
                    bfAux[i] = bfAux[i+1];
                    bfAux[i+1] = aux1;
                    aux2 = sizeBlock[i];
                    sizeBlock[i] = sizeBlock[i+1];
                    sizeBlock[i+1] = aux2;
                }
            }
        }
        ArrayList<Integer> selectedBlock = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < bfAux.length; i++) {
            if (isExactFit(bfAux[i], requiredSlots)) {
                bf[j] = bfAux[i];
                j++;
                selectedBlock.add(i);
            }
        }
        for (int i = 0; i < bfAux.length; i++) {
            if (!selectedBlock.contains(i)) {
                bf[j] = bfAux[i];
                j++;
                selectedBlock.add(i);
            }
        }
        return bf;
    }
    
    /**
     * Returns the number of slots available until the beginning of the next block.
     * @param availableSlot index of available slots
     * @return the size of this block
     */
    private int getSizeBlock(int availableSlot) {
        int size = 0;
        for (int i = availableSlot; i < slots.length; i++) {
            if(slots[i] == 0) {
                size++;
            } else {
                return size;
            }
        }
        return size;
    }
    
    /**
     * The first exact fit slot alocation
     * @param requiredSlots number of slots required
     * @return the array with slots available to 'requiredSlots'
     */
    public int[] firstExactFit(int requiredSlots) {
        int[] ff = firstFit(requiredSlots);
        int[] ef = new int[ff.length];
        ArrayList<Integer> selectedBlock = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < ff.length; i++) {
            if (isExactFit(ff[i], requiredSlots)) {
                ef[j] = ff[i];
                j++;
                selectedBlock.add(i);
            }
        }
        for (int i = 0; i < ff.length; i++) {
            if (!selectedBlock.contains(i)) {
                ef[j] = ff[i];
                j++;
                selectedBlock.add(i);
            }
        }
        return ef;
    }
    
    /**
     * The last exact fit slot alocation
     * @param requiredSlots number of slots required
     * @return the array with slots available to 'requiredSlots'
     */
    public int[] lastExactFit(int requiredSlots) {
        int[] lf = lastFit(requiredSlots);
        int[] ef = new int[lf.length];
        ArrayList<Integer> selectedBlock = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < lf.length; i++) {
            if (isExactFit(lf[i], requiredSlots)) {
                ef[j] = lf[i];
                j++;
                selectedBlock.add(i);
            }
        }
        for (int i = 0; i < lf.length; i++) {
            if (!selectedBlock.contains(i)) {
                ef[j] = lf[i];
                j++;
                selectedBlock.add(i);
            }
        }
        return ef;
    }

    
    /**
     * If this slot required exact fit 
     * @param i index slot
     * @param requiredSlots number of required slots
     * @return true if this index slot is exact fit for requiredSlots, false otherwise
     */
    private boolean isExactFit(int i, int requiredSlots) {
        if(i-1 >= 0 && slots[i-1] == 0) {//se esquerda for 0 retorna falso
            return false;
        } else {
            if(i + requiredSlots >= numSlots) {//se ta na direita
                return false;
            } else {
                if(slots[i+requiredSlots] == 0){
                    return false;
                }
            }
        }
        return true;
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
        int[] blocks = getfreeRequiredSlotsBlocks(requiredSlots);
        int W1End, W2End;
        int numberOfBlocks = blocks.length;
        if(blocks.length == 0) {
            return blocks;
        }
        int W1Start = blocks[0];
        while (numberOfBlocks > 1) {
            int W2Start = blocks[ceilDiv(blocks.length, 2)];
            
            W1End = W2Start;
            
            W2End = blocks[blocks.length-1];
            
            if(getFreeSlots(W1Start, W1End) > getFreeSlots(W2Start, W2End)) {
                blocks = getfreeRequiredSlotsBlocksToIndex(requiredSlots, W1Start, W1End);
            } else {
                W1Start = W2Start;
                blocks = getfreeRequiredSlotsBlocksToIndex(requiredSlots, W2Start, W2End);
            }
            numberOfBlocks = blocks.length;
        }
        //Will return only one block. 
        //Check the possible slots of this block that can accommodate the demand.
        //this will allow you to slide the block window
        return slideBlockWindow(blocks[0], requiredSlots);
        //return (blocks);
    }
    
    private int[] slideBlockWindow(int block, int requiredSlots) {
        ArrayList<Integer> slotsAvailable = new ArrayList<>();
        for (int i = block; i <= getLastSlotBlock(block) - requiredSlots; i++) {
            if (this.slots[i] == 0) {
                if (areSlotsAvaiable(i, i + requiredSlots - 1)) {
                    slotsAvailable.add(i);
                }
            }
        }
        return arrayListToArray(slotsAvailable);
    }
    
    private int ceilDiv(int x, int y) {
        return -Math.floorDiv(-x, y);
    }
    
    /**
     * Retrieves the number of free blocks in this interval
     * @param start start interval 
     * @param end end interval
     * @return the number of blocks if 0's
     */
    public int getFreeSlots(int start, int end) {
        int freeBlocks = 0;
        for (int i = start; i < end; i++) {
            if(slots[i] == 0) {
                freeBlocks++;
            }
        }
        return freeBlocks;
    }
    
    /**
     * Retrieves the length of all free blocks in this core
     * @return an ArrayList-Integer- object containing the length of all free blocks in this core
     */
    public ArrayList<Integer> getLengthFreeBlock() {
        ArrayList<Integer> bl = new ArrayList<>();
        int cont = 0;
        boolean begin = false;
        for (int i = 0; i < numSlots; i++) {
            if (this.slots[i] == 0) {
                if (!begin) {
                    begin = true;
                }
                cont++;
            } else {
                if (begin) {
                    bl.add(cont);
                    begin = false;
                }
                cont = 0;
            }
        }
        if (begin) {
            bl.add(cont);
        }
        return bl;
    }
    
    /**
     * Counts the amount of free segments (free blocks) in the core.
     * @return 
     */
    private double countSegments() {
        double numSegments = 0;
        long beforeSlot = -1, currentSlot = 0;
        for (int slot = 0; slot < slots.length; slot++) {
            currentSlot = slots[slot];
            if (currentSlot == 0 && beforeSlot != currentSlot) {
                numSegments++;
            }
            beforeSlot = currentSlot;
        }
        return numSegments;
    }
    
    /**
     * Returns all free blocks in this core.
     * The number returned is the first slot to this block.
     * @return 
     */
    public int[] getfreeBlocks() {
        ArrayList<Integer> blocks = new ArrayList<>();
        int aux = -1;
        boolean flag = false;
        for (int s = 0; s < slots.length; s++) {
            if(!flag) {
                if(slots[s] == 0) {
                    aux = s;
                    flag = true;
                }
            } else {
                if(slots[s] != 0) {
                    blocks.add(aux);
                    flag = false;
                }
            }
        }
        if(flag) {
            blocks.add(aux);
        }
        return arrayListToArray(blocks);
    }
    
    /**
     * Returns all free blocks to a final index on this core.
     * The number returned is the first slot to this block.
     * @param start start index
     * @param end final index
     * @return 
     */
    public int[] getfreeBlocksToIndex(int start, int end) {
        ArrayList<Integer> blocks = new ArrayList<>();
        int aux = -1;
        boolean flag = false;
        for (int s = start; s < end; s++) {
            if(!flag) {
                if(slots[s] == 0) {
                    aux = s;
                    flag = true;
                }
            } else {
                if(slots[s] != 0) {
                    blocks.add(aux);
                    flag = false;
                }
            }
        }
        if(flag) {
            blocks.add(aux);
        }
        return arrayListToArray(blocks);
    }
    
    /**
     * Returns all free blocks in this core (Here he considers the guard band as a free block).
     * The number returned is the first slot to this block.
     * @return 
     */
    public int[] getfreeBlocksWithGB() {
        ArrayList<Integer> blocks = new ArrayList<>();
        int aux = -1;
        boolean flag = false;
        for (int s = 0; s < slots.length; s++) {
            if(!flag) {
                //-2 is the inter tunnel
                if(slots[s] <= 0 && slots[s] != -2) {
                    aux = s;
                    flag = true;
                }
            } else {
                if(slots[s] > 0) {
                    blocks.add(aux);
                    flag = false;
                }
            }
        }
        if(flag) {
            blocks.add(aux);
        }
        return arrayListToArray(blocks);
    }
    
    /**
     * Returns all occupied blocks in this core.
     * The number returned is the first slot to this block.
     * @return 
     */
    public int[] getOccupiedBlocks() {
        ArrayList<Integer> blocks = new ArrayList<>();
        int aux = -1;
        boolean flag = false;
        for (int s = 0; s < slots.length; s++) {
            if(!flag) {
                if(slots[s] > 0) {
                    aux = s;
                    flag = true;
                }
            } else {
                if(slots[s] <= 0 && slots[s] != -2) {
                    blocks.add(aux);
                    flag = false;
                }
            }
        }
        if(flag) {
            blocks.add(aux);
        }
        return arrayListToArray(blocks);
    }
    
    /**
     * Returns all occupied LightPaths blocks in this core.
     * The number returned is the first slot to this block.
     * @return 
     */
    public int[] getOccupiedLightPathsBlocks() {
        ArrayList<Integer> blocks = new ArrayList<>();
        long lp = -100;
        for (int s = 0; s < slots.length; s++) {
            if(slots[s] > 0 && slots[s] != lp) {
                blocks.add(s);
                lp = slots[s];
            }
        }
        return arrayListToArray(blocks);
    }
            
    /**
     * Returns all free blocks that fit into the required slots in this core.
     * The number returned is the first slot to this block.
     * @param requiredSlots number of slots required
     * @return 
     */
    public int[] getfreeRequiredSlotsBlocks(int requiredSlots) {
        int[] allBlocks = getfreeBlocks();
        ArrayList<Integer> blocks = new ArrayList<>();
        for (int i = 0; i < allBlocks.length; i++) {
            if(getLastSlotBlock(allBlocks[i]) - allBlocks[i] >= requiredSlots) {
                blocks.add(allBlocks[i]);
            }
        }
        return arrayListToArray(blocks);
    }
    
    /**
     * Returns all free blocks that fit into the required slots to a final index on this core.
     * The number returned is the first slot to this block.
     * @param requiredSlots number of slots required
     * @param start start index
     * @param end final index
     * @return 
     */
    public int[] getfreeRequiredSlotsBlocksToIndex(int requiredSlots, int start, int end) {
        int[] allBlocks = getfreeBlocksToIndex(start, end);
        ArrayList<Integer> blocks = new ArrayList<>();
        for (int i = 0; i < allBlocks.length; i++) {
            if(getLastSlotBlock(allBlocks[i]) - allBlocks[i] >= requiredSlots) {
                blocks.add(allBlocks[i]);
            }
        }
        return arrayListToArray(blocks);
    }
    
    /**
     * Retrieves the last available slot of this block
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotBlock(int firstSlotBlock) {
        int i;
        for (i = firstSlotBlock + 1; i < slots.length; i++) {
            if(slots[i] != 0) {
                return i-1;
            }
        }
        if(i == slots.length) {
            return i-1;
        }
        return -1;
    }
    
    /**
     * Retrieves the last occupied slot of this block
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotOccupiedBlock(int firstSlotBlock) {
        int i;
        for (i = firstSlotBlock + 1; i < slots.length; i++) {
            //tem que ser diferente de -2 (inter tunnel)
            if(slots[i] <= 0 && slots[i] != -2) {
                return i-1;
            }
        }
        if(i == slots.length) {
            return i-1;
        }
        return -1;
    }
    
    /**
     * Retrieves the last available slot of this block (Here he considers the guard band as a free block)
     * @param firstSlotBlock the first slot of this block
     * @return the last of this block
     */
    public int getLastSlotBlockWithGB(int firstSlotBlock) {
        int i;
        for (i = firstSlotBlock + 1; i < slots.length; i++) {
            if(slots[i] > 0) {
                return i-1;
            }
        }
        if(i == slots.length) {
            return i-1;
        }
        return -1;
    }
    
    /**
     * Convert an ArrayList Object to array (int)
     * @param a
     * @return the int array
     */
    private int[] arrayListToArray(ArrayList<Integer> a) {
        int[] out = null;
        if (a == null) {
            return null;
        } else {
            out = new int[a.size()];
            for (int i = 0; i < a.size(); i++) {
                out[i] = a.get(i);
            }
        }
        return out;
    }
    
    /**
     * Verify that it's possible push-pull in this core.
     * @param oldfirstSlot the first slot of the old lightpath
     * @param oldlastSlot the last slot of the old lightpath
     * @param newfirstSlot the first slot of the new lightpath
     * @param newlastSlot the last slot of the new lightpath
     * @return true if is possible to do push-pull or false otherwise
     */
    public boolean pushpull(int oldfirstSlot, int oldlastSlot, int newfirstSlot, int newlastSlot) {
        int i;
        if (oldfirstSlot < newfirstSlot) {
            for (i = oldlastSlot + 1; i <= newlastSlot; i++) {
                if (slots[i] > 0) {
                    return false;
                }
            }
            if (i < slots.length && slots[i] == -1) {
                return false;
            }
        } else { 
            for (i = oldfirstSlot - 1; i >= newfirstSlot; i--) {
                if (slots[i] > 0) {
                    return false;
                }
            }
            if (i > 0 && slots[i] == -1) {
                return false;
            }
        }
        return true;
    }

    /**
     * Retrieves the Spectrum Compactness (SC) in this Core.
     * YONGLI ZHAO, LIYAZHOU HU, RUIJIE ZHU, XIAOSONG YU, XINBO WANG and JIE ZHANG. 
     * Crosstalk-Aware Spectrum Defragmentation Based on Spectrum Compactness 
     * in Space Division Multiplexing Enabled Elastic Optical Networks With Multicore Fiber. 
     * IEEE Access, 2018
     * 
     * @return the Spectrum Compactness (SC) in this Core
     */
    public double getSC() {
        long firstSlot = getFirstUsedSlot();
        long lastSlot = getLastUsedSlot();
        if (firstSlot == -1 || lastSlot == -1) {
            return Double.MAX_VALUE;
        }
        double freeSlots = getAvaiableSlots();
        double usedSlots = numSlots - freeSlots;
        double availableSegments = countSegments();
        
        double sc = (lastSlot - firstSlot + 1 / usedSlots) * (freeSlots / availableSegments);
        if (sc == Double.POSITIVE_INFINITY) {
            sc = Double.MAX_VALUE;
        }
        return sc;
    }
    
    /**
     * Retrieves the current fragmentation rate based in Entropy Equation.
     * Wright et al 2015. P. Wright, M. C. Parker, e A. Lord. Minimum- and
     * maximum-entropy routing and spectrum assignment for flexgrid elastic
     * optical networking [invited]. IEEE/OSA Journal of Optical Communications
     * and Networking, 7(1):A66–A72, Jan 2015.
     *
     * @return the current fragmentation of topology.
     */
    public double getEntropy() {
        double frag = 0.0;
        ArrayList<Integer> blocks;
        
        if (maxSizeAvaiable() == getAvaiableSlots()) {
            return 0;
        } else {
            blocks = getLengthFreeBlock();
            for (int bl : blocks) {
                frag += (((double) bl / (double) getNumSlots()) * (Math.log((double) bl) - Math.log((double) getNumSlots())));
            }
            return frag;
        }
    }

    /**
     * Retrieves the First used slot in this Core;
     * @return the First used slot in this Core;
     */
    public long getFirstUsedSlot() {
        long slot = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != 0) {
                return i;
            }
        }
        return slot;
    }

    /**
     * Retrieves the Last used slot in this Core;
     * @return the Last used slot in this Core;
     */
    public long getLastUsedSlot() {
        long slot = -1;
        for (int i = slots.length - 1; i >= 0; i--) {
            if (slots[i] != 0) {
                return i;
            }
        }
        return slot;
    }

    /**
     * Retrieve all lightpath ids from this core
     * @return all lightpath ids from this core
     */
    public ArrayList<Long> getLightpaths() {
        ArrayList<Long> lps = new ArrayList<>();
        for (long slot : slots) {
            if (!lps.contains(slot) && slot > 0) {
                lps.add(slot);
            }
        }
        return lps;
    }
    
    /**
     * Retrieve the lightpath id in this index
     * @param index slot in this core
     * @return the lightpath id in this index
     */
    public long getLightpathId(int index) {
        return slots[index];
    }
    
    /**
     * Change the spectrum frequency in this core.
     * @param oldfirstSlot the first slot of the old lightpath
     * @param oldlastSlot the last slot of the old lightpath
     * @param newfirstSlot the first slot of the new lightpath
     * @param newlastSlot the last slot of the new lightpath
     */
    public void changeSpectrum(int oldfirstSlot, int oldlastSlot, int newfirstSlot, int newlastSlot) {
        long id = slots[oldfirstSlot];
        releaseSlots(oldfirstSlot, oldlastSlot);
        reserveSlots(id, newfirstSlot, newlastSlot);
    }
}
