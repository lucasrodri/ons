/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.util;

import ons.Batch;
import ons.BulkData;

/**
 * This class find all combinations of Batch calls of Batch with k calls
 *
 */
public class CombinationsBatch {
    
    private static int line;
    
    public static BulkData[][] getCombinations(Batch batch, int combinations){
        BulkData[][] bulks = new BulkData[totalCombinations(batch.getSize(), combinations)][combinations];
        int vector[] = new int[combinations];
        line = 0;
        combinations(batch, bulks, vector, 0, combinations - 1);
        return bulks;
    }
    
    private static void combinations(Batch batch, BulkData[][] bulks, int index[], int i, int k) {
        if(i == 0) {
            for (index[i] = 0; index[i] < batch.getSize() - k; index[i]++) {
                combinations(batch, bulks, index, i+1, k-1);
            }
        } else {
            for (index[i] = index[i-1] + 1; index[i] < batch.getSize() - k; index[i]++) {
                if(k > 0){
                    combinations(batch, bulks, index, i+1, k-1);
                } else {
                    for (int j = 0; j < index.length; j++) {
                        bulks[line][j] = batch.getBulks().get(index[j]);        
                    }
                    line++;
                }
            }
        }
    }

    private static int totalCombinations(int n, int k) {
        if(n < k){
            throw new IllegalArgumentException();
        } else {
            if(n == k){
                return 1;
            } 
        }
        return fatorial(n) / ( fatorial(k) * fatorial(n - k) );
    }

    private static int fatorial(int n) {
        if(n > 1){
            return n*fatorial(n-1);
        } else {
            return n;
        }
    }
}
