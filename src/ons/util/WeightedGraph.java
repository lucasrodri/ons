/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * A weighted graph associates a label (weight) with every edge in the graph.
 * If a pair of nodes has weight equal to zero, it means the edge between them
 * doesn't exist.
 * 
 * @author andred
 */
public class WeightedGraph implements Serializable{

    protected int numNodes;
    protected double[][] edges;  // adjacency matrix
    
    /**
     * Creates a new WeightedGraph object with no edges,
     * 
     * @param n number of nodes the new graph will have
     */
    public WeightedGraph(int n) {
        edges = new double[n][n];
        numNodes = n;
    }
    
    /**
     * Creates a new WeightedGraph object, based on an already existing
     * weighted graph.
     * 
     * @param g the graph that will be copied into the new one
     */
    public WeightedGraph(WeightedGraph g) {
        numNodes = g.numNodes;
        edges = new double[numNodes][numNodes];
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                edges[i][j] = g.getWeight(i, j);
            }
        }
    }
    
    /**
     * Retrieves the size of the graph, i.e., the amount of vertexes it has.
     * 
     * @return integer with the quantity of nodes in the graph
     */
    public int size() {
        return numNodes;
    }
    
    /**
     * Creates a new edge within the graph, which requires its two vertexes
     * and its weight.
     * 
     * @param source the edge's source node 
     * @param target the edge's destination node
     * @param w the value of the edge's weight
     */
    public void addEdge(int source, int target, double w) {
        edges[source][target] = w;
    }
    
    /**
     * Says whether or not a given pair of nodes has an edge between them.
     * 
     * @param source the source node
     * @param target the destination node
     * @return true if the edge exists, or false otherwise
     */
    public boolean isEdge(int source, int target) {
        return edges[source][target] > 0;
    }
    
    /**
     * Removes a given edge from the graph by simply attributing
     * zero to its source and target coordinates within the matrix of edges.
     * 
     * @param source the edge's source node
     * @param target the edge's destination node
     */
    public void removeEdge(int source, int target) {
        edges[source][target] = 0;
    }
    
    /**
     * Retrieves the weight of a given edge on the graph.
     * 
     * @param source the edge's source node
     * @param target the edge's destination node
     * @return the value of the edge's weight
     */
    public double getWeight(int source, int target) {
        return edges[source][target];
    }
    
    /**
     * Sets a determined weight to a given edge on the graph.
     * 
     * @param source the edge's source node
     * @param target the edge's destination node
     * @param w the value of the weight
     */
    public void setWeight(int source, int target, double w) {
        edges[source][target] = w;
    }
    
    /**
     * Retrieves the neighbors of a given vertex. 
     * The vertices that are reachable by this vertex
     * @param vertex index of the vertex within the matrix of edges
     * @return list with indexes of the vertex's neighbors
     */
    public int[] neighbors(int vertex) {
        int count = 0;
        for (int i = 0; i < edges[vertex].length; i++) {
            if (edges[vertex][i] > 0) {
                count++;
            }
        }
        final int[] answer = new int[count];
        count = 0;
        for (int i = 0; i < edges[vertex].length; i++) {
            if (edges[vertex][i] > 0) {
                answer[count++] = i;
            }
        }
        return answer;
    }
    
    /**
     * Retrieves the neighbors of a given vertex. 
     * The vertices that reach this vertex
     * @param vertex index of the vertex within the matrix of edges
     * @return list with indexes of the vertex's neighbors
     */
    public int[] neighbors2(int vertex) {
        int count = 0;
        for (int i = 0; i < edges[vertex].length; i++) {
            if (edges[i][vertex] > 0) {
                count++;
            }
        }
        final int[] answer = new int[count];
        count = 0;
        for (int i = 0; i < edges[vertex].length; i++) {
            if (edges[i][vertex] > 0) {
                answer[count++] = i;
            }
        }
        return answer;
    }
    
    /**
     * Prints all information related to the weighted graph.
     * For each vertex, shows the vertexes is is adjacent to and the
     * weight of each edge.
     * 
     * @return string containing the edges of each vertex
     */
    @Override
    public String toString() {
        String s = "";
        for (int j = 0; j < edges.length; j++) {
            s += Integer.toString(j) + ": ";
            for (int i = 0; i < edges[j].length; i++) {
                if (edges[j][i] > 0) {
                    s += Integer.toString(i) + ":" + Double.toString(edges[j][i]) + " ";
                }
            }
            s += "\n";
        }
        return s;
    }
    
    /**
     * Remode all edges node
     * 
     * @param node the node
     */
    public void removeNodeEdge(int node) {
        //removing edges from this node
        for(int i = 0; i < numNodes; i++){
            if(isEdge(node, i)){
                removeEdge(node, i);
            }
            if(isEdge(i, node)){
                removeEdge(i, node);
            }
        }
    }
    
    /**
     * Remove node in graph
     * 
     * @param node the node 
     */
    public void removeNode(int node) {
        //removing edges from this node
        removeNodeEdge(node);
        //remove node from the graph
        double[][] newedges = new double[numNodes-1][numNodes-1];
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
     * Creates a new node in graph.
     */
    public void addNode(){
        double[][] newedges = new double[numNodes+1][numNodes+1];
        for(int i = 0; i < numNodes; i++){
            System.arraycopy(edges[i], 0, newedges[i], 0, numNodes);
        }
        numNodes++;
        edges = newedges;
    }
    
    /**
     * Retrieves the diameter of this graph.
     * @return the longest of all the calculated shortest paths in a network
     */
    public double getGraphDiameter(){
        double max = 0, min = 0;
        int[] nodes;
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    nodes = Dijkstra.getShortestPath(this, i, j);
                    for (int k = 0; k < nodes.length - 1; k++) {
                        min += getWeight(nodes[k], nodes[k + 1]);
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
     * Retornas a maior rota das k-5 menores
     * @return a maior rota das k-5 menores
     */
    public double getGraphDiameter_mod(int k){
        double max = 0, min = 0;
        int[] nodes;
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(this, i, j, k);
                    for (int r = 0; r < kpaths.length; r++) {
                        nodes = route(kpaths, r);
                        for (int l = 0; l < nodes.length - 1; l++) {
                            min += getWeight(nodes[l], nodes[l + 1]);
                        }
                        if (min > max) {
                            max = min;
                        }
                        min = 0;
                    }
                }
            }
        }
        return max;
    }
    
    /**
     * Retrieves the clustering coefficient of this node.
     * The clustering coefficient of a node is the ratio of existing links connecting a node's neighbors to each other to the maximum possible number of such links.
     * 
     * @param node the node
     * @return the clustering coefficient
     */
    public double getClusteringCoefficient(int node){
        int[] neighbors = neighbors(node);
        int connections = 0;
        for (int i = 0; i < neighbors.length; i++) {
            for (int j = 0; j < neighbors.length; j++) {
                if(neighbors[i] != neighbors[j]){
                    if(isEdge(neighbors[i], neighbors[j])){
                        connections++;
                    }
                }
            }
            
        }
        return (double) connections/ ((double) neighbors.length * ( (double) neighbors.length - 1.0));
    }
    
    /**
     * Retrieves the max local clustering coefficient in this graph.
     * 
     * @return the max local clustering coefficient
     */
    public double getMaxClusteringCoefficient(){
        double max = 0;
        double aux;
        for (int i = 0; i < this.numNodes; i++) {
            aux = getClusteringCoefficient(i);
            if(aux > max){
                max = aux;
            }
        }
        return max;
    }
    
    /**
     * Retrieves the sum of all local clustering coefficient in this graph.
     * 
     * @return the sum of all local clustering coefficient
     */
    public double getSumClusteringCoefficient(){
        double sum = 0;
        for (int i = 0; i < this.numNodes; i++) {
            sum += getClusteringCoefficient(i);
        }
        return sum;
    }
    
    /**
     * Retrieves the average clustering coefficient of this graph.
     * 
     * @return the average clustering coefficient
     */
    public double getAverageClusteringCoefficient(){
        return getSumClusteringCoefficient() / (double) this.numNodes;
    }
    
    /**
     * Retrieves the global clustering coefficient of this graph.
     * 
     * @return the global clustering coefficient
     */
    public double getGlobalClusteringCoefficient(){
        ArrayList<TreeSet<Integer>> triangles = new ArrayList<>();
        ArrayList<TreeSet<Integer>> triplets = new ArrayList<>();
        TreeSet<Integer> triangle;
        TreeSet<Integer> triplet;
        for (int node = 0; node < numNodes; node++) {
            int[] neighbors = neighbors(node);
            for (int i = 0; i < neighbors.length; i++) {
                for (int j = 0; j < neighbors.length; j++) {
                    if (neighbors[i] != neighbors[j]) {
                        triplet = new TreeSet<>();
                        triplet.add(node);
                        triplet.add(neighbors[i]);
                        triplet.add(neighbors[j]);
                        if (!triplets.contains(triplet)) {
                            triplets.add(triplet);
                        }
                        if (isEdge(neighbors[i], neighbors[j])) {      
                            triangle = new TreeSet<>();
                            triangle.add(node);
                            triangle.add(neighbors[i]);
                            triangle.add(neighbors[j]);
                            if(!triangles.contains(triangle)){
                                triangles.add(triangle);
                            }
                        }
                    }
                }

            }
        }
        return (double) triangles.size() / (double) triplets.size();
    }
    
    /**
     * Retrieves the Average path length of this graph.
     * Average path length is calculated by finding the shortest path between all pairs of nodes, adding them up, and then dividing by the total number of pairs.
     * @return the average path length of this graph
     */
    public double getAveragePathLength(){
        double sumDistance = 0;
        int[] nodes;
        int nodePairs = 0;
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if(i != j){
                    nodePairs++;
                    nodes = Dijkstra.getShortestPath(this, i, j);
                    for (int k = 0; k < nodes.length - 1; k++) {
                        sumDistance += getWeight(nodes[k], nodes[k + 1]);
                    }
                }
            }
        }
        return sumDistance/ (double) nodePairs;
    }
    
    /**
     * Retrieves the max possible wheight by 'k' hops
     * @param k the k of YensKSP algorithm
     * @return the max possible weight
     */
    public double getMaxPossiblePathWeight(int k){
        int weightLightpath;
        int maxWeight = 0;
        
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if(i != j){
                    ArrayList<Integer>[] kpaths = YenKSP.kShortestPaths(this, i, j, k);
                    for (ArrayList<Integer> kpath : kpaths) {
                        weightLightpath = 0;
                        for (int l = 0; l < kpath.size() - 1; l++) {
                            weightLightpath += getWeight(kpath.get(l), kpath.get(l + 1));
                        }
                        if(weightLightpath > maxWeight){
                            maxWeight = weightLightpath;
                        }
                    }
                }
            }
        }
        return maxWeight;
    }
    
    private int[] route(ArrayList<Integer>[] kpaths, int k) {
        if (kpaths[k] != null) {
            int[] path = new int[kpaths[k].size()];
            for (int i = 0; i < path.length; i++) {
                path[i] = kpaths[k].get(i);
            }
            return path;
        } else {
            return null;
        }
    }
}
