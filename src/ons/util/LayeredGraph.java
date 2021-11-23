/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import java.util.ArrayList;

/**
 * The "layered graph" is constructed from some weighted graphs your edges is 
 * constructed between layers (weighted graphs).
 * 
 * @author lucas
 */
public class LayeredGraph {
    
    /**
     * The edges between layers in LayeredGraph.
     */
    public class LayeredGraphEdges{
        int sourceLayer;
        int destinationLayer;
        int sourceNode;
        int destinationNode;
        double weight;
        
        public LayeredGraphEdges(int sourceLayer, int destinationLayer, int sourceNode, int destinationNode, double weight) {
            this.sourceLayer = sourceLayer;
            this.destinationLayer = destinationLayer;
            this.sourceNode = sourceNode;
            this.destinationNode = destinationNode;
            this.weight = weight;
        }
    }

    private int numLayers;
    private ArrayList<LayeredGraphEdges> edges; //edges connecting layers
    private ArrayList<WeightedGraph> LayerGraph;
    private ArrayList<Long> LayerGraphTag;
    
    /**
     * Creates a new LayeredGraph object with no edges in any layer,
     * 
     * @param l number of layers the new graph will have
     * @param n number of nodes the each graph will have
     */
    public LayeredGraph(int l, int n) {
        numLayers = l;
        edges = new ArrayList<>();
        LayerGraph = new ArrayList<>();
        LayerGraphTag = new ArrayList<>();
        for(int i = 0; i < l; i++){
            LayerGraph.add(new WeightedGraph(n));
            LayerGraphTag.add(0L);
        }
    }
    
    /**
     * Creates a new LayeredGraph object, based on an already existing
     * weighteds graphs.
     * 
     * @param graphs the graph that will be copied into the new one
     */
    public LayeredGraph(ArrayList<WeightedGraph> graphs) {
        numLayers = graphs.size();
        edges = new ArrayList<>();
        LayerGraph = new ArrayList<>();
        LayerGraphTag = new ArrayList<>();
        LayerGraph.addAll(graphs);
        for (int i = 0; i < LayerGraph.size(); i++) {
            LayerGraphTag.add(0L);
        }
    }
    
    /**
     * Creates a new LayeredGraph object, based on an already existing
     * weighteds graphs and put its tags.
     * 
     * @param graphs the graph that will be copied into the new one
     * @param tags the tags of each layer
     */
    public LayeredGraph(ArrayList<WeightedGraph> graphs, ArrayList<Long> tags) {
        numLayers = graphs.size();
        edges = new ArrayList<>();
        LayerGraph = new ArrayList<>();
        LayerGraphTag = new ArrayList<>();
        LayerGraph.addAll(graphs);
        for (int i = 0; i < LayerGraph.size(); i++) {
            LayerGraphTag.add(tags.get(i));
        }
    }
    
    /**
     * Creates a new LayeredGraph object, based on an already existing
     * weighteds graphs.
     * The layered graph with 3 layers: Eletrical layer, Sub-transponder layer and Transponder layer.
     * 
     * @param eletricalLayer the Eletrical layer
     * @param subTransponderLayer the Sub-transponder layer
     * @param transponderLayer the Transponder layer
     */
    public LayeredGraph(WeightedGraph eletricalLayer, WeightedGraph subTransponderLayer, WeightedGraph transponderLayer) {
        numLayers = 3;
        edges = new ArrayList<>();
        LayerGraph = new ArrayList<>();
        LayerGraphTag = new ArrayList<>();
        LayerGraph.add(eletricalLayer);
        LayerGraph.add(subTransponderLayer);
        LayerGraph.add(transponderLayer);
        LayerGraphTag.add(0L);
        LayerGraphTag.add(0L);
        LayerGraphTag.add(0L);
    }

    /**
     * Retrieves the tag of this layer.
     * @param layer the layer
     * @return the tag of this layer.
     */
    public long getLayerGraphTag(int layer) {
        return LayerGraphTag.get(layer);
    }

    /**
     * Set the tag in this specific layer.
     * @param layer the layer where will be set the tag
     * @param tag the tag that will be set
     */
    public void setLayerGraphTag(int layer, long tag) {
        this.LayerGraphTag.set(layer, tag);
    }
    
    /**
     * Retrieves the size of the graph, i.e., the amount of vertexes it has.
     * 
     * @return integer with the quantity of nodes in the all graphs
     */
    public int size() {
        int nodes = 0;
        for (WeightedGraph LayerGraph1 : LayerGraph) {
            nodes = nodes + LayerGraph1.size();
        }
        return nodes;
    }
    
    /**
     * Retrieves the size of the graph in layer, i.e., the amount of vertexes it has.
     * 
     * @param l the layer
     * @return integer with the quantity of nodes in the layer
     */
    public int size(int l) {
        return LayerGraph.get(l).size();
    }
    
    /**
     * Creates a new layer in LayeredGraph.
     * 
     * @param l the layer
     */
    public void addLayer(WeightedGraph l) {
        numLayers++;
        LayerGraph.add(l);
    }
    
    /**
     * Retrieves the number of layers of the LayeredGraph.
     * 
     * @return the numeber of layers
     */
    public int getNumberLayer() {
        return numLayers;
    }
    
    /**
     * Retrieves the layer of the LayeredGraph.
     * 
     * @param l the layer
     * @return the layer
     */
    public WeightedGraph getLayer(int l) {
        return LayerGraph.get(l);
    }
    
    /**
     * Creates a new edge between layer, which requires two layers, two vertexes 
     * and its weight.
     *  
     * @param layerSource the layer source
     * @param layerTarget the layer destination
     * @param nodeSource the node source in WeightedGraph "layerSource"
     * @param nodeTarget the node destination in WeightedGraph "layerTarget"
     * @param w the value of the edge's weight
     */
    public void addEdge(int layerSource, int layerTarget, int nodeSource, int nodeTarget, double w) {
        edges.add(new LayeredGraphEdges(layerSource, layerTarget, nodeSource, nodeTarget, w));
    }
    
    /**
     * Says to exist or not an edge connecting a pair of layers.
     * 
     * @param layerSource the source layer
     * @param layerTarget the destination layer
     * @return true if the edge exists, or false otherwise
     */
    public boolean isEdge(int layerSource, int layerTarget) {
        for (LayeredGraphEdges edge : edges) {
            if (edge.sourceLayer == layerSource && edge.destinationLayer == layerTarget) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Says to exist or not an edge connecting a pair of nodes from different layers.
     * 
     * @param layerSource the source layer
     * @param layerTarget the destination layer
     * @param nodeSource the node source in WeightedGraph "layerSource"
     * @param nodeTarget the node destination in WeightedGraph "layerTarget"
     * @return true if the edge exists, or false otherwise
     */
    public boolean isEdge(int layerSource, int layerTarget, int nodeSource, int nodeTarget) {
        for (LayeredGraphEdges edge : edges) {
            if (edge.sourceLayer == layerSource && edge.destinationLayer == layerTarget &&
                    edge.sourceNode == nodeSource && edge.destinationNode == nodeTarget) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Says to exist or not an edge connecting a pair of nodes in this layer.
     * 
     * @param layer the layer
     * @param nodeSource the node source in WeightedGraph "layerSource"
     * @param nodeTarget the node destination in WeightedGraph "layerTarget"
     * @return true if the edge exists, or false otherwise
     */
    public boolean isEdge(int layer, int nodeSource, int nodeTarget) {
        return LayerGraph.get(layer).isEdge(nodeSource, nodeTarget);
    }
    
    /**
     * Get the edges connecting a pair of layers.
     * 
     * @param layerSource the source layer
     * @param layerTarget the destination layer
     * @return the edges
     */
    public ArrayList<LayeredGraphEdges> getEdges(int layerSource, int layerTarget) {
        ArrayList<LayeredGraphEdges> r_edges = new ArrayList<>();
        for (LayeredGraphEdges edge : edges) {
            if (edge.sourceLayer == layerSource && edge.destinationLayer == layerTarget) {
                r_edges.add(edge);
            }
        }
        return r_edges;
    }
    
    /**
     * Removes a given edge from the graph by simply attributing
     * 
     * @param layerSource the source layer
     * @param layerTarget the destination layer
     * @param nodeSource the node source in WeightedGraph "layerSource"
     * @param nodeTarget the node destination in WeightedGraph "layerTarget"
     */
    public void removeEdge(int layerSource, int layerTarget, int nodeSource, int nodeTarget) {
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).sourceLayer == layerSource && edges.get(i).destinationLayer == layerTarget &&
                    edges.get(i).sourceNode == nodeSource && edges.get(i).destinationNode == nodeTarget) {
                edges.remove(i);
                return;
            }
        }
    }
    
    /**
     * Removes a given edge from the graph by simply attributing
     * 
     * @param edge the edge
     */
    public void removeEdge(LayeredGraphEdges edge) {
        edges.remove(edge);
    }
    
    /**
     * Retrieves the weight of a given edge on the graph.
     * 
     * @param layerSource the source layer
     * @param layerTarget the destination layer
     * @param nodeSource the node source in WeightedGraph "layerSource"
     * @param nodeTarget the node destination in WeightedGraph "layerTarget"
     * @return the value of the edge's weight
     */
    public double getWeight(int layerSource, int layerTarget, int nodeSource, int nodeTarget) {
        for (LayeredGraphEdges edge : edges) {
            if (edge.sourceLayer == layerSource && edge.destinationLayer == layerTarget &&
                    edge.sourceNode == nodeSource && edge.destinationNode == nodeTarget) {
                return edge.weight;
            }
        }
        return -1; //if the edge doesn't exist
    }
    
    /**
     * Sets a determined weight to a given edge on the graph.
     * 
     * @param layerSource the source layer
     * @param layerTarget the destination layer
     * @param nodeSource the node source in WeightedGraph "layerSource"
     * @param nodeTarget the node destination in WeightedGraph "layerTarget"
     * @param w the value of the weight
     */
    public void setWeight(int layerSource, int layerTarget, int nodeSource, int nodeTarget, double w) {
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).sourceLayer == layerSource && edges.get(i).destinationLayer == layerTarget &&
                    edges.get(i).sourceNode == nodeSource && edges.get(i).destinationNode == nodeTarget) {
                edges.set(i, new LayeredGraphEdges(layerSource, layerTarget, nodeSource, nodeTarget, w));
                return;
            }
        }
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
        for(int i = 0; i < edges.size(); i++){
            s += Integer.toString(i);
            for (LayeredGraphEdges edge : edges) {
                if (edge.sourceLayer == i) {
                    s += " [" + Integer.toString(edge.destinationLayer) + ",";
                    s += Integer.toString(edge.sourceNode) + ",";
                    s += Integer.toString(edge.destinationNode) + "];";
                }
            }
            s += "\n";
        }
        return s;
    }
    
    /**
     * Remove all edges connecting this layer
     * 
     * @param l the layer 
     */
    public void removeEdgeLayer(int l) {
        for (LayeredGraphEdges edge : edges) {
            if (edge.sourceLayer == l || edge.destinationLayer == l) {
                removeEdge(edge);
            }
        }
    }
    
    /**
     * Remove all edges connecting this node in this layer
     * 
     * @param l the layer 
     * @param n the node
     */
    public void removeEdgeLayerNode(int l, int n) {
        for (LayeredGraphEdges edge : edges) {
            if ((edge.sourceLayer == l && edge.sourceNode == n) || (edge.destinationLayer == l && edge.destinationNode == n)) {
                removeEdge(edge);
            }
        }
    }
    
    /**
     * Remove layer in graph
     * 
     * @param layer the layer
     */
    public void removeLayer(int layer) {
        //removing edges from this layer
        removeEdgeLayer(layer);
        //remove layer from the graph
        
        ArrayList<LayeredGraphEdges> newedges = new ArrayList<>();
        for (LayeredGraphEdges edge : edges) {
            if (edge.sourceLayer > layer || edge.destinationLayer > layer) {
                if (edge.sourceLayer > layer) {
                    newedges.add(new LayeredGraphEdges(edge.sourceLayer - 1, edge.destinationLayer, edge.sourceNode, edge.destinationNode, edge.weight));
                } else {
                    newedges.add(new LayeredGraphEdges(edge.sourceLayer, edge.destinationLayer - 1, edge.sourceNode, edge.destinationNode, edge.weight));
                }
            } else {
                newedges.add(edge);
            }
        }
        numLayers--;
        LayerGraph.remove(layer);
        edges.clear();
        edges.addAll(newedges);
    }
    
    /**
     * Get WeightedGraph from the LayeredGraph.
     * 
     * @return the WeightedGraph from this LayeredGraph
     */
    public WeightedGraph getBigGraph() {
        WeightedGraph bigGraph = new WeightedGraph(size());
        int size = 0;
        //adding the edges of the layers
        for (int l = 0; l < numLayers; l++) {
            for (int i = 0; i < LayerGraph.get(l).size(); i++) {
                for (int j = 0; j < LayerGraph.get(l).size(); j++) {
                    if (LayerGraph.get(l).isEdge(i, j)) {
                        bigGraph.addEdge(i + size, j + size, LayerGraph.get(l).getWeight(i, j));
                    }
                }
            }
            size = size + LayerGraph.get(l).size();
        }
        //adding the inter-layer edges
        for (LayeredGraphEdges edge : edges) {
            bigGraph.addEdge(mapping(edge.sourceLayer, edge.sourceNode),
                    mapping(edge.destinationLayer, edge.destinationNode),
                    edge.weight);
        }
        return bigGraph;
    }
    
    public WeightedMultiGraph getBigGraph2() {
        WeightedMultiGraph bigGraph = new WeightedMultiGraph(size());
        int size = 0;
        //adding the edges of the layers
        for (int l = 0; l < numLayers; l++) {
            for (int i = 0; i < LayerGraph.get(l).size(); i++) {
                for (int j = 0; j < LayerGraph.get(l).size(); j++) {
                    if (LayerGraph.get(l).isEdge(i, j)) {
                        bigGraph.addEdge(i + size, j + size, LayerGraph.get(l).getWeight(i, j));
                    }
                }
            }
            size = size + LayerGraph.get(l).size();
        }
        //adding the inter-layer edges
        for (LayeredGraphEdges edge : edges) {
            bigGraph.addEdge(mapping(edge.sourceLayer, edge.sourceNode),
                    mapping(edge.destinationLayer, edge.destinationNode),
                    edge.weight);
        }
        return bigGraph;
    }
    
    /**
     * Get shortest Path from the layered graph using the Dijkstra and
     * it is assumed that the source and destination are in the first layer of the graph.
     * 
     * @param src
     * @param dst
     * @return 
     */
    public int[] getShortestPath(int src, int dst) {
        return Dijkstra.getShortestPath(getBigGraph(), src, dst);
    }
    
    /**
     * Maps the layer and node in LayeredGraph for bigGraaph.
     * 
     * @param layer the layer
     * @param node the node
     * @return the source/destination in "bigGraph"
     */
    private int mapping(int layer, int node){
        int size = 0;
        if(layer >= LayerGraph.size()){
            throw (new IllegalArgumentException());
        }
        for(int i = 0; i < layer; i++){
            size = size + LayerGraph.get(i).size();
        }
        return size + node;
    }
    
    /**
     * Maps the node in bigGraph for layer and node in LayeredGraph.
     * 
     * @param node the node
     * @return the array with 2 positions: the layer and the node
     */
    public int[] mapping(int node){
        int size = 0;
        int[] map = new int[2];
        if(node >= size()){
            throw (new IllegalArgumentException());
        }
        for(int i = 0; i < LayerGraph.size(); i++){
            if(node < LayerGraph.get(i).size() + size){
                map[0] = i;
                map[1] = node-size;
                return map;
            } else {
                size = size + LayerGraph.get(i).size();
            }
        }
        map[0] = -1;
        map[1] = -1;
        return map;
    }
}