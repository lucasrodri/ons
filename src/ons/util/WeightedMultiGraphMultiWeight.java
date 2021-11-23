/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author lucasrc
 */
public class WeightedMultiGraphMultiWeight {

    private int id = 1;
    private int numNodes;
    private final Map<Integer, TreeSet<MultiWeightEdge>> edgeList;

    private static class EdgeSort implements Comparator<MultiWeightEdge> {

        @Override
        public int compare(MultiWeightEdge e1, MultiWeightEdge e2) {
            if (e1.getId() < e2.getId()) {
                return -1;
            }
            if (e1.getId() > e2.getId()) {
                return 1;
            }
            return 0;
        }
    }

    public WeightedMultiGraphMultiWeight(int n) {
        numNodes = n;
        edgeList = new HashMap<>(numNodes);
    }

    public WeightedMultiGraphMultiWeight(WeightedGraph g) {
        numNodes = g.size();
        edgeList = new HashMap<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (g.isEdge(i, j)) {
                    if (!edgeList.containsKey(i)) {
                        edgeList.put(i, new TreeSet<>(new EdgeSort()));
                    }
                    edgeList.get(i).add(new MultiWeightEdge(id++, i, j, g.getWeight(i, j)));
                }
            }
        }
    }

    public WeightedMultiGraphMultiWeight(WeightedMultiGraphMultiWeight g) {
        numNodes = g.size();
        edgeList = new HashMap<>(numNodes);
        for (int v : g.edgeList.keySet()) {
            edgeList.put(v, new TreeSet<>(new EdgeSort()));
            for (MultiWeightEdge e : g.edgeList.get(v)) {
                edgeList.get(v).add(new MultiWeightEdge(e.getId(),e.getSrc(),e.getDst(),e.getWeights()));
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
        if (!edgeList.containsKey(source)) {
            edgeList.put(source, new TreeSet<>(new EdgeSort()));
        }
        edgeList.get(source).add(new MultiWeightEdge(id++, source, target, w));
    }

    public void removeEdge(int source, long id) {
        MultiWeightEdge ed = null;
        for (MultiWeightEdge e : edgeList.get(source)) {
            if (e.getId() == id) {
                ed = e;
                break;
            }
        }
        edgeList.get(source).remove(ed);
    }

    public void removeEdgesUntil(int source, long id) {
        MultiWeightEdge ed = null;
        while (!edgeList.get(source).isEmpty()) {
            MultiWeightEdge e = edgeList.get(source).pollFirst();
            if (e.getId() == id) {
                break;
            }
        }
    }

    public MultiWeightEdge getFirstEdge(int source) {
        if (edgeList.get(source) == null) {
            return null;
        }
        if (edgeList.get(source).isEmpty()) {
            return null;
        }
        return edgeList.get(source).first();
    }

    public Iterator<MultiWeightEdge> getEdgeIterator(int source) {
        if (edgeList.get(source) == null) {
            return null;
        }
        if (edgeList.get(source).isEmpty()) {
            return null;
        }
        return edgeList.get(source).iterator();
    }

    public MultiWeightEdge getFirstEdge(int source, int target) {

        for (MultiWeightEdge e : edgeList.get(source)) {
            if (e.getDst() == target) {
                return e;
            }
        }
        return null;
    }

    public TreeSet<MultiWeightEdge> getEdges(int source) {
        return edgeList.get(source);
    }
    
    /**
     * Says whether or not a given pair of nodes has an edge between them.
     * 
     * @param source the source node
     * @param target the destination node
     * @return true if the edge exists, or false otherwise
     */
    public boolean isEdge(int source, int target) {
        if (edgeList.containsKey(source)) {
            for (MultiWeightEdge e : edgeList.get(source)) {
                if (e.getDst() == target) {
                    return true;
                }
            }
        }
        return false;
    }

    public double getWeight(int source, int target, long id, int index) {
        double w = -1;
        if (edgeList.containsKey(source)) {
            for (MultiWeightEdge e : edgeList.get(source)) {
                if (e.getId() == id) {
                    return e.getWeight(index);
                }
            }
        }
        return w;
    }
    
    public double getWeight(int source, int target, int index) {
        double w = -1;
        if (edgeList.containsKey(source)) {
            for (MultiWeightEdge e : edgeList.get(source)) {
                if (e.getDst() == target) {//pode haver multiplas arestas que ligam o src com o dst
                    return e.getWeight(index);//ele ira retornar a de menor id
                }
            }
        }
        return w;
    }

    public void setWeight(int source, int target, long id, double w, int index) {
        if (edgeList.containsKey(source)) {
            for (MultiWeightEdge e : edgeList.get(source)) {
                if (e.getId() == id) {
                    e.setWeight(index, w);
                }
            }
        }
    }
    
    public int[] neighbors(int vertex) {
        int count = 0;
        int[] answer = new int[0];
        if (edgeList.containsKey(vertex)) {
            answer = new int[edgeList.get(vertex).size()];
            count = 0;
            for (MultiWeightEdge e : edgeList.get(vertex)) {
                answer[count++] = e.getDst();
            }
        }
        return answer;
    }
    
    @Override
    public String toString() {
        String s = "";
        for (Integer v : edgeList.keySet()) {
            s += v + ": ";
            for (MultiWeightEdge e : edgeList.get(v)) {
                s += Integer.toString(e.getDst()) + "(" + Long.toString(e.getId()) + ", " + e.getWeights().toString() + ") ";
            }
            s += "\n";
        }
        return s;
    }
    
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
    
    public void removeNode(int node) {
        //removing edges from this node
        removeNodeEdge(node);
        //remove node from the graph
        edgeList.remove(node);
        numNodes--;
    }
    
    public void addNode(){
        if (!edgeList.containsKey(numNodes)) {
            edgeList.put(numNodes, new TreeSet<>(new EdgeSort()));
        }
        numNodes++;
    }
    
    /**
     * Retrieves the set of the Edges linking this pair
     * @param source the source node
     * @param target the destination node
     * @return the the set of the Edges linking this pair, if the edge doesnt existing, returns null set
     */
    public TreeSet<MultiWeightEdge> getEdge(int source, int target) {
        TreeSet<MultiWeightEdge> edges = new TreeSet<>();
        if (edgeList.containsKey(source)) {
            for (MultiWeightEdge e : edgeList.get(source)) {
                if (e.getDst() == target) {
                    edges.add(e);
                }
            }
        }
        return edges;
    }
    
    /**
     * Removes a given edge from the graph.
     * This method removes all the edges linking this pair
     * 
     * @param source the edge's source node
     * @param target the edge's destination node
     */
    public void removeEdge(int source, int target) {
        //This method removes all the edges linking this pair
        if (!edgeList.containsKey(source)) {
            throw (new IllegalArgumentException());
        }
        edgeList.get(source).removeAll(getEdge(source, target));
    }
    
    public double getWeight(int source, int target) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * Retrieves the diameter of this graph in this index.
     * @param index the edge index
     * @return the longest of all the calculated shortest paths in a network
     */
    public double getGraphDiameter(int index) {
        double max = 0, min = 0;
        int[] nodes;
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (i != j) {
                    nodes = Dijkstra.getShortestPath(this, i, j, index);
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
}
