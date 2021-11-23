/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ons;

/**
 * Methods to treat the outgoing of a Flow object.
 * 
 * @author andred
 */
public class OrdinaryEvent extends Event{
    
    private String description;
    
    /**
     * Creates a new OrdinaryEvent object.
     * 
     * @param description the description of this ordinary event
     */
    public OrdinaryEvent(String description) {
        this.description = description;
    }

    /**
     * Retrieves the description of the OrdinaryEvent object.
     * 
     * @return the OrdinaryEvent's description attribute
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Prints all information related to the ordinary event.
     * 
     * @return string containing all the values of the ordinary event.
     */
    public String toString() {
        return "Ordinary event-" + "Description: " + description;
    }
}
