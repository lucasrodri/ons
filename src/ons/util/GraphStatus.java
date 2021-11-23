/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import ons.Flow;

/**
 *
 * @author lucas
 */
public class GraphStatus {
    
    private WeightedGraph graph;
    
    public GraphStatus(WeightedGraph graph){
        this.graph = new WeightedGraph(graph.size());
    }
    
    public boolean isReserved(Flow flow){
        return this.graph.isEdge(flow.getSource(), flow.getDestination());
    }
    
    public boolean isReserved(int source, int destination){
        return this.graph.isEdge(source, destination);
    }
    
    public void reserve(Flow flow){
        if(this.graph.isEdge(flow.getSource(), flow.getDestination())){
            throw (new IllegalArgumentException());
        }
        this.graph.addEdge(flow.getSource(), flow.getDestination(), (double) flow.getID());
    }
    
    public void reserve(int source, int destination, long id){
        if(this.graph.isEdge(source, destination)){
            throw (new IllegalArgumentException());
        }
        this.graph.addEdge(source, destination, (double) id);
    }
    
    public long whoReserved(Flow flow){
        return (long) this.graph.getWeight(flow.getSource(), flow.getDestination());
    }
    
    public long whoReserved(int source, int destination){
        return (long) this.graph.getWeight(source, destination);
    }
    
    public void free(Flow flow){
        if((long) this.graph.getWeight(flow.getSource(), flow.getDestination()) != flow.getID()){
            throw (new IllegalArgumentException());
        }
        this.graph.removeEdge(flow.getSource(), flow.getDestination());
    }
    
    public void free(int source, int destination, long id){
        if((long) this.graph.getWeight(source, destination) != id){
            throw (new IllegalArgumentException());
        }
        this.graph.removeEdge(source, destination);
    }
}
