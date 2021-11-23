/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import java.io.Serializable;
import ons.LightPath;

/**
 * A weighted graph associates a label (weight) with every edge in the graph.
 * If a pair of nodes has weight equal to zero, it means the edge between them
 * doesn't exist.
 * 
 * @author lucasrc
 */
public class WeightedGraphLPCandidates extends WeightedGraph implements Serializable{
    
    private LightPath[][] lpEdges;  

    public WeightedGraphLPCandidates(int n) {
        super(n);
        lpEdges = new LightPath[n][n];
    }

    public WeightedGraphLPCandidates(WeightedGraph g) {
        super(g);
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                lpEdges[i][j] = null;
            }
        }
    }
    
    public WeightedGraphLPCandidates(WeightedGraphLPCandidates g) {
        super(g.numNodes);
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                edges[i][j] = g.getWeight(i, j);
                lpEdges[i][j] = g.getCandidate(i, j);
            }
        }
    }
    
    public LightPath getCandidate(int source, int target) {
        return lpEdges[source][target];
    }
    
    public void setCandidate(int source, int target, LightPath lp) {
        lpEdges[source][target] = lp;
    }
    
}
