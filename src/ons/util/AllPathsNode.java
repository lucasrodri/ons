/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author lucas
 */
public class AllPathsNode implements Serializable{
    
    private String topologia;
    private ArrayList<ArrayList<Integer>>[][] routes;
    private WeightedGraph graph;

    public AllPathsNode(WeightedGraph g, String topologia) {
        this.graph = g;
        this.routes = new ArrayList[graph.size()][graph.size()];
        for (int i = 0; i < graph.size(); i++) {
            for (int j = 0; j < graph.size(); j++) {
                routes[i][j] = new ArrayList<>();
            }
        }
        this.topologia = topologia;
        initialize();
    }
    
    private void initialize() {
        ArrayList<Integer> linePath;
        ArrayList<ArrayList<Integer>> paths;
        
        for (int i = 0; i < graph.size(); i++) {
            for (int j = 0; j < graph.size(); j++) {
                if(i != j){
                    try {
                        FileReader arq = new FileReader("txt/"+topologia+i+"_"+j+".txt");
                        BufferedReader lerArq = new BufferedReader(arq);
                        String linha = lerArq.readLine();
                        paths = new ArrayList<>();
                        while (linha != null) {
                            String[] splitedLine = linha.split(",");
                            linePath = new ArrayList<>();
                            for (int k = 1; k < splitedLine.length; k++) {
                                if(k == splitedLine.length - 1){
                                    linePath.add(Integer.parseInt(splitedLine[k].substring(0, splitedLine[k].indexOf("."))));
                                } else {
                                    linePath.add(Integer.parseInt(splitedLine[k]));
                                }
                            }
                            paths.add(linePath);
                            linha = lerArq.readLine();
                        }
                        routes[i][j] = new ArrayList<>(paths);
                        arq.close();
                    } catch (IOException e) {
                        System.err.printf("Erro na abertura do arquivo: %s.\n", e.getMessage());
                    }
                }
            }   
        }
    }
    
    /**
     * Retrieves the all Paths without node repetition from the source and destination.
     * @param source the source node
     * @param destination the destination node
     * @param diameter
     * @return the K-Shortest Paths
     */
    public ArrayList<Integer>[] getPaths(int source, int destination, int diameter){
        ArrayList<ArrayList<Integer>> shortestPaths = new ArrayList();
        for (int i = 0; i < routes[source][destination].size(); i++) {
            if(routes[source][destination].get(i).size() <= diameter + 1){
                shortestPaths.add(routes[source][destination].get(i));
            }
        }
        ArrayList<Integer> [] saida = new ArrayList[shortestPaths.size()];
        for (int i = 0; i < saida.length; i++) {
            saida[i] = shortestPaths.get(i);
        }
        return saida;
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

