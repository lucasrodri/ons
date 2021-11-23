/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.ra;

import ons.EONPhysicalTopology;
import ons.Flow;
import ons.LightPath;
import ons.Modulation;
import ons.util.Dijkstra;
import ons.util.PseudoControlPlane;
import ons.util.WeightedGraph;
import ons.util.WeightedGraphMultiWeight;
import ons.util.YenKSP;
import java.util.ArrayList;

/**
 * This class is a adaptive modulation model, that uses some RA class in your implementation. 
 * This adaptive modulation model was created by Lucas R. Costa to resolve the RMLSA problem.
 * @author lucas
 */
public class AMMS_FPA implements RA {
    
    private ControlPlaneForRA cp;
    private ControlPlaneForRA pseudoCP;
    private WeightedGraph graph;
    
    //Here we defined the RSA class:
    private final RA rsa = new FPA();
    //Here the variables of this scheme
    int maxM = EONPhysicalTopology.getMaxModulation();
    int numModulation = maxM+1;
    double T;
    double factor = 0.25;
    int C;
    int K = 3;
    ArrayList<Integer>[][][][] routes;
    WeightedGraphMultiWeight P;
    
    @Override
    public void simulationInterface(ControlPlaneForRA cp) {
        this.cp = cp;
        this.graph = cp.getPT().getWeightedGraph();
        this.pseudoCP = new PseudoControlPlane(cp, rsa);
        this.rsa.simulationInterface(pseudoCP);
        this.T = graph.getGraphDiameter() * this.factor;
        this.C = (int) Math.ceil(T/Modulation.getModulationReach(maxM));//maior numero de saltos
        this.P = new WeightedGraphMultiWeight(graph.size(), numModulation);//because the first modulation is 0
        initializeVTM();
        initializeRoutes();
        //System.out.println("Fim da Criação das rotas!!!");
        /*
        System.out.println("Vai a rota agora");
        System.out.println(routes[4][18][2][0] + " -> " + pathLength(routes[4][18][2][0], P, 2));
        System.out.println(routes[4][18][2][1] + " -> " + pathLength(routes[4][18][2][1], P, 2));
        System.out.println(routes[4][18][2][2] + " -> " + pathLength(routes[4][18][2][2], P, 2));
        System.out.println("stop");
        System.exit(0);
        */
    }

    @Override
    public void simulationEnd() {   
    }
    
    private void initializeVTM(){
        int[] path;
        for (int i = 0; i < graph.size(); i++) {
            for (int j = 0; j < graph.size(); j++) {
                if(i != j){
                    path = Dijkstra.getShortestPath(graph, i, j);
                    modulationPath(i, j, path);
                }
            }
        }
        //grafo P ta pronto
        
    }
    
    private void initializeRoutes() {
        routes = new ArrayList[graph.size()][graph.size()][numModulation][K];
        for (int s = 0; s < graph.size(); s++) {
            for (int d = 0; d < graph.size(); d++) {
                for (int m = 0; m < numModulation; m++) {
                    for (int k = 0; k < K; k++) {
                        //routes[s][d][m] = kShortestPaths(convertGraph(P, m), s, d, K);
                        routes[s][d][m] = YenKSP.kShortestPaths(convertGraph(P, m), s, d, K);
                    }
                }
            }
        }
    }
    
    private void modulationPath(int src, int dst, int[] path) {
        double pathLength = 0;
        for (int i = 0; i < path.length - 1; i++) {
            pathLength += graph.getWeight(path[i], path[i + 1]);
        }
        for (int i = 0; i < maxM + 1; i++) {//por que a primeira modulacao e 0
            if(pathLength <= (double) Modulation.getModulationReach(i)) {
                P.addEdge(src, dst, pathLength, i);
            }
        }
    }
    
    @Override
    public void setModulation(int modulation) {
    }
    
    @Override
    public void flowArrival(Flow flow) {
        int modulation = maxM;
        int ksp = 0;
        boolean flag;
        
        while (true) {
            ArrayList<Flow> disposal = path(flow, ksp, modulation);
            if (disposal.isEmpty() || disposal.size() > C) {
                ksp++;
                if (ksp == K) {//arrived in maxK
                    modulation--;
                    if (modulation < 0) {
                        // Block the call
                        pseudoCP.blockFlow(flow.getID());
                        cp.blockFlow(flow.getID());
                        return;
                    } else {
                        ksp = 0;
                    }
                } 
            } else {
                flag = false;
                rsa.setModulation(modulation);
                for (Flow f : disposal) {
                    rsa.flowArrival(f);
                    if (pseudoCP.getPath(f) == null) { //checks that was not blocked
                        flag = true; //its blocked
                        break;
                    }
                }
                if (flag) {
                    ksp++;
                    if (ksp == K) {//arrived in maxK
                        modulation--;
                        if (modulation < 0) {
                            // Block the call
                            cp.blockFlow(flow.getID());
                            return;
                        } else {
                            ksp = 0;
                        }
                    }
                } else {
                    // Accept the call
                    // we have to do remove bw in lps before, because they was added by pseudoCP
                    for (LightPath lightpath : pseudoCP.getPath(flow).getLightpaths()) {
                            cp.getPT().removeRate(flow.getRate(), lightpath);
                    }
                    cp.acceptFlow(flow.getID(), pseudoCP.getPath(flow).getLightpaths());
                    //System.err.println(conta++);
                    return;
                }
            }
        }
    }

    @Override
    public void flowDeparture(long id) {
        rsa.flowDeparture(id);
    }

    private ArrayList<Flow> path(Flow flow, int k, int modulation) {
        ArrayList<Flow> disposal = new ArrayList<>();
        int[] nodes = arrayListToArray(routes[flow.getSource()][flow.getDestination()][modulation][k]);
        if (nodes.length == 0 || nodes == null) {
            return disposal;
        }
        // Creating a disposal flow set
        for (int i = 0; i < nodes.length - 1; i++) {
            disposal.add(new Flow(flow.getID(), nodes[i], nodes[i + 1], flow.getRate(), flow.getDuration(), flow.getCOS()));
        }
        return disposal;
    }

    /**
     * This method transforms the WeightedGraphMultiWeight objetct to WeightedGraph object
     * @param P the WeightedGraphMultiWeight object
     * @param level the level considered
     * @return the WeightedGraph object
     */
    private WeightedGraph convertGraph(WeightedGraphMultiWeight P, int level) {
        WeightedGraph g = new WeightedGraph(P.size());
        for (int i = 0; i < g.size(); i++) {
            for (int j = 0; j < g.size(); j++) {
                if (i != j){
                    if(P.isEdge(i, j, level)){
                        g.addEdge(i, j, P.getWeight(i, j, level));
                    }
                }
            }
        }
        return g;
    }
    
    private double pathLength(ArrayList<Integer> path, WeightedGraphMultiWeight P, int level) {
        double pathLength = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            pathLength += P.getWeight(path.get(i), path.get(i + 1), level);
        }
        return pathLength;
    }
    
    private double pathLength(WeightedGraph graph, int[] path) {
        double pathLength = 0;
        for (int i = 0; i < path.length - 1; i++) {
            pathLength += graph.getWeight(path[i], path[i + 1]);
        }
        return pathLength;
    }

    private ArrayList<Integer>[] kShortestPaths(WeightedGraph graph, int src, int dst, int k) {
        ArrayList<Integer>[] paths = new ArrayList[k];
        for (int i = 0; i < paths.length; i++){
            paths[i] = new ArrayList<>();
        }
        int count = 0, index = 0, indexPath = 0, pathVector[];
        double pathLengthAuxOld, pathLengthAuxNew;
        pathVector = YenKSP.kShortestPathsIndex(graph, src, dst, index);
        index++;
        pathLengthAuxOld = pathLength(graph, pathVector);
        addVectorInArray(paths, pathVector, indexPath);
        indexPath++;
        count++;
        while(count < k){
            pathVector = YenKSP.kShortestPathsIndex(graph, src, dst, index);
            if(pathVector == null || pathVector.length == 0){
                break;
            }
            index++;
            pathLengthAuxNew = pathLength(graph, pathVector);
            if(pathLengthAuxNew > pathLengthAuxOld){
                pathLengthAuxOld = pathLengthAuxNew;
                addVectorInArray(paths, pathVector, indexPath);
                indexPath++;
                count++;
            }
        }
        return paths;
    }

    private void addVectorInArray(ArrayList<Integer>[] paths, int[] pathVector, int indexPath) {
        for (int i = 0; i < pathVector.length; i++) {
            paths[indexPath].add(pathVector[i]);
        }
    }

    private int[] arrayListToArray(ArrayList<Integer> a) {
        int[] array = new int[a.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = a.get(i);
        }
        return array;
    }
}
