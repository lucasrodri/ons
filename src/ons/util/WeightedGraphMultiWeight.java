/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

/**
 * This graphs shows the reach of each node in each modulation level to all nodes in topology.
 * If the node is unreachable in modulation level then the edge's weight is 0.
 * @author lucas
 */
public class WeightedGraphMultiWeight {
    
    private int numNodes;
    private int levels;
    private WeightedGraphMultiWeightEdge[][] edges;  //adjacency matrix
    
    public WeightedGraphMultiWeight(int n, int levels){
        numNodes = n;
        this.levels = levels;
        edges = new WeightedGraphMultiWeightEdge[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                edges[i][j] = new WeightedGraphMultiWeightEdge(levels);
            }    
        }
    }
    
    public WeightedGraphMultiWeight(WeightedGraphMultiWeight g){
        numNodes = g.size();
        levels = g.getLevels();
        edges = new WeightedGraphMultiWeightEdge[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                edges[i][j] = g.getEdge(i, j);
            }    
        }
    }
    
    public int size() {
        return numNodes;
    }
    
    public int getLevels() {
        return levels;
    }
    
    public void addEdge(int source, int target, double w, int level) {
        edges[source][target].setWeightLevel(level, w);
    }
    
    public boolean isEdge(int source, int target, int level) {
        return edges[source][target].getWeightLevel(level) > 0;
    }
    
    public void removeEdge(int source, int target, int level) {
        edges[source][target].setWeightLevel(level, 0);
    }
    
    public double getWeight(int source, int target, int level) {
        return edges[source][target].getWeightLevel(level);
    }
    
    public WeightedGraphMultiWeightEdge getEdge(int src, int dst) {
        return edges[src][dst];
    }
    
    public int[] neighbors(int vertex, int level) {
        int count = 0;
        for (WeightedGraphMultiWeightEdge edge : edges[vertex]) {
            if (edge.getWeightLevel(level) > 0) {
                count++;
            }
        }
        final int[] answer = new int[count];
        count = 0;
        for (int i = 0; i < edges[vertex].length; i++) {
            if (edges[vertex][i].getWeightLevel(level) > 0) {
                answer[count++] = i;
            }
        }
        return answer;
    }
    
    @Override
    public String toString() {
        String s = "";
        for (int l = 0; l < levels; l++) {
            s += "Level " +l+ ":\n";
            for (int j = 0; j < edges.length; j++) {
                s += Integer.toString(j) + ": ";
                for (int i = 0; i < edges[j].length; i++) {
                    if (edges[j][i].getWeightLevel(l) > 0) {
                        s += Integer.toString(i) + ":" + Double.toString(edges[j][i].getWeightLevel(l)) + " ";
                    }
                }
                s += "\n";
            }
        }
        return s;
    }
    
    public void removeNodeEdge(int node, int level) {
        //removing edges from this node
        for(int i = 0; i < numNodes; i++){
            if(isEdge(node, i, level)){
                removeEdge(node, i, level);
            }
            if(isEdge(i, node, level)){
                removeEdge(i, node, level);
            }
        }
    }
    
    public void removeNodeEdge(int node) {
        for (int i = 0; i < levels; i++) {
            removeNodeEdge(node, i);
        }
    }
    
    public void removeNode(int node) {
        //removing edges from this node
        removeNodeEdge(node);
        //remove node from the graph
        WeightedGraphMultiWeightEdge[][] newedges = new WeightedGraphMultiWeightEdge[numNodes-1][numNodes-1];
        for (int i = 0; i < numNodes-1; i++) {
            for (int j = 0; j < numNodes-1; j++) {
                newedges[i][j] = new WeightedGraphMultiWeightEdge(levels);
            }    
        }
        int k = 0, l = 0;
        for(int i = 0; i< numNodes; i++){
            if(i != node){
                l = 0;
                for(int j = 0; j< numNodes; j++){
                    if(j != node){
                        newedges[k][l] = edges[i][j];
                        l++;
                    }
                }
                k++;   
            }
        }
        numNodes--;
        edges = newedges;
    }
    
    /**
     * Retrieves the diameter of this graph.
     * @param level the edge level
     * @return the longest of all the calculated shortest paths in a network
     */
    public double getGraphDiameter(int level){
        double max = 0, min = 0;
        int[] nodes;
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    nodes = Dijkstra.getShortestPath(this, i, j, level);
                    for (int k = 0; k < nodes.length - 1; k++) {
                        min += getWeight(nodes[k], nodes[k + 1], level);
                    }
                    if(min > max){
                        max = min;
                    }
                }
                min = 0;
            }
        }
        return max;
    }
    
    /**
     * Retrieves the clustering coefficient of this node.
     * The clustering coefficient of a node is the ratio of existing links connecting a node's neighbors to each other to the maximum possible number of such links.
     * 
     * @param node the node
     * @param level the modulation level
     * @return the clustering coefficient
     */
    public double getClusteringCoefficient(int node, int level){
        int[] neighbors = neighbors(node, level);
        int connections = 0;
        for (int i = 0; i < neighbors.length; i++) {
            for (int j = 0; j < neighbors.length; j++) {
                if(neighbors[i] != neighbors[j]){
                    if(isEdge(neighbors[i], neighbors[j], level)){
                        connections++;
                    }
                }
            }
            
        }
        return (double) connections/ ((double) neighbors.length * ( (double) neighbors.length - 1.0));
    }
}
