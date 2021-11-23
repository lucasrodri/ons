/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ons.util;

import java.util.ArrayList;

/**
 *
 * @author andred
 */
public class MultiWeightEdge {
    private int id;
    private int src;
    private int dst;
    private final ArrayList<Double> weights;

    public MultiWeightEdge(int id, int source, int destination, ArrayList<Double> weights) {
        this.id = id;
        this.src = source;
        this.dst = destination;
        this.weights =  new ArrayList<>();
        this.weights.addAll(weights);
    }
    
    public MultiWeightEdge(int id, int source, int destination, double weight) {
        this.id = id;
        this.src = source;
        this.dst = destination;
        this.weights =  new ArrayList<>();
        this.weights.add(weight);
    }

    public void setDst(int dst) {
        this.dst = dst;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setSrc(int src) {
        this.src = src;
    }

    public void setWeights(ArrayList<Double> weights) {
        this.weights.clear();
        this.weights.addAll(weights);
    }
    
    public void setWeight(int index, double weight) {
        this.weights.set(index, weight);
    }

    public int getDst() {
        return dst;
    }

    public int getId() {
        return id;
    }

    public int getSrc() {
        return src;
    }

    public ArrayList<Double> getWeights() {
        return weights;
    }
    
    public double getWeight(int index) {
        return weights.get(index);
    }
    
    public void addWeight(double weight){
        this.weights.add(weight);
    }
    
    public void removeWeight(int index){
        this.weights.remove(index);
    }
}
