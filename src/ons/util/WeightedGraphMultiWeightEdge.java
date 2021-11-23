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
public class WeightedGraphMultiWeightEdge {

    //begins with weaker modulation level (fewer bits per symbol)
    private ArrayList<Double> mlevels;
    //ends with stronger modulation level (more bits per symbol)

    public WeightedGraphMultiWeightEdge(int levels) {
        mlevels = new ArrayList<>();
        for (int i = 0; i < levels; i++) {
            mlevels.add(0.0);
        }
    }
    
    public double getWeightLevel(int level){
        return mlevels.get(level);
    }

    public void setWeightLevel(int level, double weight) {
        this.mlevels.set(level, weight);
    }
}
