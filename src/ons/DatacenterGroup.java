/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons;

import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author lucas
 */
public class DatacenterGroup {
    
    private int id;
    private TreeSet<Integer> members;
    private int numMembers;

    public DatacenterGroup(int id) {
        this.id = id;
        this.members = new TreeSet<>();
        this.numMembers = 0;
    }

    public int getNumMembers() {
        return numMembers;
    }
    
    public int getId() {
        return id;
    }

    public TreeSet<Integer> getAllMembers() {
        return members;
    }
    
    public int getMember(int index) {
        if(index >= members.size()) {
            return -1;
        }
        Iterator<Integer> it = members.iterator();
        int i = 0;
        Integer current = null;
        while (it.hasNext() && i <= index) {
            current = it.next();
            i++;
        }
        if(current == null) {
            return -1;
        } else {
            return current;
        }
    }
    
    public boolean isMember(int member) {
        return members.contains(member);
    }

    public void addMember(int member) {
        this.members.add(member);
        numMembers++;
    }
    
    public void removeMember(int member) {
        this.members.remove(member);
        numMembers--;
    }
    
}
