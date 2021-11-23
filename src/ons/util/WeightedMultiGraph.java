/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 *
 * @author andred
 */
public class WeightedMultiGraph {
    
    private int id = 1;
    private int numNodes;
    private final Map<Integer, TreeSet<Edge>> edgeList;

    private static class EdgeSort implements Comparator<Edge> {

        @Override
        public int compare(Edge e1, Edge e2) {
            if (e1.getId() < e2.getId()) {
                return -1;
            }
            if (e1.getId() > e2.getId()) {
                return 1;
            }
            return 0;
        }
    }

    public WeightedMultiGraph(int n) {
        numNodes = n;
        edgeList = new HashMap<>(numNodes);
    }

    public WeightedMultiGraph(WeightedGraph g) {
        numNodes = g.size();
        edgeList = new HashMap<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            for (int j = 0; j < numNodes; j++) {
                if (g.isEdge(i, j)) {
                    if (!edgeList.containsKey(i)) {
                        edgeList.put(i, new TreeSet<>(new EdgeSort()));
                    }
                    edgeList.get(i).add(new Edge(id++, i, j, g.getWeight(i, j)));
                }
            }
        }
    }

    public WeightedMultiGraph(WeightedMultiGraph g) {
        numNodes = g.size();
        edgeList = new HashMap<>(numNodes);
        for (int v : g.edgeList.keySet()) {
            edgeList.put(v, new TreeSet<>(new EdgeSort()));
            for (Edge e : g.edgeList.get(v)) {
                edgeList.get(v).add(new Edge(e.getId(),e.getSrc(),e.getDst(),e.getWeight()));
            }

        }
    }

    public int size() {
        return numNodes;
    }

    public void addEdge(int source, int target, double weight) {
        if (!edgeList.containsKey(source)) {
            edgeList.put(source, new TreeSet<>(new EdgeSort()));
        }
        edgeList.get(source).add(new Edge(id++, source, target, weight));
    }

    public void removeEdge(int source, long id) {
        Edge ed = null;
        for (Edge e : edgeList.get(source)) {
            if (e.getId() == id) {
                ed = e;
                break;
            }
        }
        edgeList.get(source).remove(ed);
    }

    public void removeEdgesUntil(int source, long id) {
        Edge ed = null;
        while (!edgeList.get(source).isEmpty()) {
            Edge e = edgeList.get(source).pollFirst();
            if (e.getId() == id) {
                break;
            }
        }
    }

    public Edge getFirstEdge(int source) {
        if (edgeList.get(source) == null) {
            return null;
        }
        if (edgeList.get(source).isEmpty()) {
            return null;
        }
        return edgeList.get(source).first();
    }

    public Iterator<Edge> getEdgeIterator(int source) {
        if (edgeList.get(source) == null) {
            return null;
        }
        if (edgeList.get(source).isEmpty()) {
            return null;
        }
        return edgeList.get(source).iterator();
    }

    public Edge getFirstEdge(int source, int target) {

        for (Edge e : edgeList.get(source)) {
            if (e.getDst() == target) {
                return e;
            }
        }
        return null;
    }

    public TreeSet<Edge> getEdges(int source) {
        return edgeList.get(source);
    }

    public boolean isEdge(int source, int target) {
        if (edgeList.containsKey(source)) {
            for (Edge e : edgeList.get(source)) {
                if (e.getDst() == target) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Retrieves the set of the Edges linking this pair
     * @param source the source node
     * @param target the destination node
     * @return the the set of the Edges linking this pair, if the edge doesnt existing, returns null set
     */
    public ArrayList<Edge> getEdges(int source, int target) {
        //Revise: I change this return TreeSet to ArrayList
        ArrayList<Edge> edges = new ArrayList<>();
        if (edgeList.containsKey(source)) {
            for (Edge e : edgeList.get(source)) {
                if (e.getDst() == target) {
                    edges.add(e);
                }
            }
        }
        return edges;
    }

    public double getWeight(int source, int target, long id) {
        double w = -1;
        if (edgeList.containsKey(source)) {
            for (Edge e : edgeList.get(source)) {
                if (e.getId() == id) {
                    return e.getWeight();
                }
            }
        }
        return w;
    }
    
    public double getWeight(int source, int target) {
        double w = -1;
        if (edgeList.containsKey(source)) {
            for (Edge e : edgeList.get(source)) {
                if (e.getDst() == target) {//pode haver multiplas arestas que ligam o src com o dst
                    return e.getWeight();//ele ira retornar a de menor id
                }
            }
        }
        return w;
    }

    public void setWeight(int source, int target, long id, double w) {
        if (edgeList.containsKey(source)) {
            for (Edge e : edgeList.get(source)) {
                if (e.getId() == id) {
                    e.setWeight(w);
                }
            }
        }
    }

    public void setWeight(int source, int target, double w) {
        if (edgeList.containsKey(source)) {
            for (Edge e : edgeList.get(source)) {
                e.setWeight(w);
            }
        }
    }
    
    public int[] neighbors(int vertex) {
        int count = 0;
        int[] answer = new int[0];
        if (edgeList.containsKey(vertex)) {
            answer = new int[edgeList.get(vertex).size()];
            count = 0;
            for (Edge e : edgeList.get(vertex)) {
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
            for (Edge e : edgeList.get(v)) {
                s += Integer.toString(e.getDst()) + "(" + Long.toString(e.getId()) + ", " + Double.toString(e.getWeight()) + ") ";
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
    
    public void removeEdge(int source, int target) {
        //This method removes all the edges linking this pair
        if (!edgeList.containsKey(source)) {
            throw (new IllegalArgumentException());
        }
        edgeList.get(source).removeAll(getEdges(source, target));
    }
    
    /**
     * Retrieves the diameter of this graph.
     * @return the longest of all the calculated shortest paths in a network
     */
    public double getGraphDiameter() {
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
}
