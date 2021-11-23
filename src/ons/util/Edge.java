/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ons.util;

/**
 *
 * @author andred
 */
public class Edge {
    private int id;
    private int src;
    private int dst;
    private double weight;

    public Edge(int id, int source, int destination, double weight) {
        this.id = id;
        this.src = source;
        this.dst = destination;
        this.weight = weight;
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

    public void setWeight(double weight) {
        this.weight = weight;
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

    public double getWeight() {
        return weight;
    }

}
