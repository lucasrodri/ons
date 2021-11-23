/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import java.util.ArrayList;

/**
 *
 * @author lucas
 */
public class KSPOffline {
    
    private static KSPOffline singletonObject;
    private static int K;
    private static ArrayList<Integer>[][][] routes;
    private static WeightedGraph graph;
    
    /**
     * A private constructor that prevents any other class from instantiating.
     */
    private KSPOffline(WeightedGraph g, int k) {
        KSPOffline.K = k;
        KSPOffline.graph = g;
        KSPOffline.routes = new ArrayList[g.size()][g.size()][KSPOffline.K];
        initialize();
    }
    
    /**
     * Creates a new KSPOffline object, in case it does'n exist yet.
     *
     * @param g the weighted graph
     * @return the MyStatistics singletonObject
     */
    public static synchronized KSPOffline getKSPOfflineObject(WeightedGraph g, int k) {
        if (singletonObject == null) {
            singletonObject = new KSPOffline(g, k);
        }
        return singletonObject;
    }

    /**
     * Throws an exception to stop a cloned MyStatistics object from being
     * created.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
    
    private void initialize(){
        for (int i = 0; i < graph.size(); i++) {
            for (int j = 0; j < graph.size(); j++) {
                if(i != j){
                    routes[i][j] = YenKSP.kShortestPaths(graph, i, j, K);
                }
            }
        }
    }
    
    /**
     * Retrieves the K-Shortest Paths from the source and destination
     * @param source the source node
     * @param destination the destination node
     * @return the K-Shortest Paths
     */
    public ArrayList<Integer>[] getKShortestPaths(int source, int destination){
        return KSPOffline.routes[source][destination];
    }
    
    /**
     * Retrieves the K-Path of the K-Shortest Paths
     * @param source the source node
     * @param destination the destination node
     * @param k the k-path, starts from = 0
     * @return the K-Path, starts from = 0
     */
    public ArrayList<Integer> getKShortestPaths(int source, int destination, int k){
        return KSPOffline.routes[source][destination][k];
    }
}
