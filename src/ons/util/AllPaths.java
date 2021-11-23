/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

/**
 *
 * @author lucas
 */
public class AllPaths {
    
    private static AllPaths singletonObject;
    private static double diameter;
    private static ArrayList<ArrayList<Integer>>[][] routes;
    private static WeightedGraph graph;
    
    public AllPaths(WeightedGraph g, double diameter){
        AllPaths.diameter = diameter;
        AllPaths.graph = g;
        AllPaths.routes = new ArrayList[g.size()][g.size()];
        initialize();
    }
    
    /**
     * Creates a new KSPOffline object, in case it does'n exist yet.
     *
     * @param g the weighted graph
     * @param diameter the routes size can't be more than this value
     * @return the MyStatistics singletonObject
     */
    public static synchronized AllPaths getAllPathsObject(WeightedGraph g, double diameter) {
        if (singletonObject == null) {
            singletonObject = new AllPaths(g, diameter);
        }
        return singletonObject;
    }

    private void initialize() {
        for (int i = 0; i < graph.size(); i++) {
            for (int j = 0; j < graph.size(); j++) {
                if(i != j){
                    initialize(i,j);
                }
            }
        }
    }

    private void initialize(int src, int dst) {
        int k = 1;
        ArrayList<Integer>[] aux = YenKSP.kShortestPaths(graph, src, dst, k);
        ArrayList<Integer>[] ksp = aux;
        while(!repetition(aux) && lessThan(aux)) {
            ksp = aux;
            aux = YenKSP.kShortestPaths(graph, src, dst, ++k);
        }
        ArrayList<ArrayList<Integer>> paths = new ArrayList();
        paths.addAll(Arrays.asList(ksp));
        routes[src][dst] = paths;
    }

    private boolean repetition(ArrayList<Integer>[] ksp) {
        TreeSet<Integer> nodes = new TreeSet<>();
        int lastSize = 0;
        for (int node : ksp[ksp.length-1]) {
            nodes.add(node);
            if(nodes.size() == lastSize){
                return true;
            } else {
                lastSize = nodes.size();
            }
        }
        return false;
    }

    private boolean lessThan(ArrayList<Integer>[] ksp) {
        ArrayList<Integer> path = ksp[ksp.length-1];
        double weight = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            weight += graph.getWeight(path.get(i), path.get(i + 1));
        }
        return weight <= diameter;
    }
    
    /**
     * Retrieves the all Paths without node repetition from the source and destination.
     * @param source the source node
     * @param destination the destination node
     * @return the K-Shortest Paths
     */
    public ArrayList<Integer>[] getPaths(int source, int destination){
        ArrayList<Integer>[] shortestPaths = new ArrayList[routes[source][destination].size()];
        for (int i = 0; i < shortestPaths.length; i++) {
            shortestPaths[i] = routes[source][destination].get(i);
        }
        return shortestPaths;
    }
    
    /**
     * Retrieves the K-Path of the all Paths without node repetition from the source and destination.
     * @param source the source node
     * @param destination the destination node
     * @param k the k-path, starts from = 0
     * @return the K-Path, starts from = 0
     */
    public ArrayList<Integer> getKShortestPath(int source, int destination, int k){
        return routes[source][destination].get(k);
    }
    
}
