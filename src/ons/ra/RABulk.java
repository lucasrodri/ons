/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ons.ra;

import ons.Batch;
import ons.BulkData;

/**
 *
 * @author lucas
 */
public interface RABulk extends RA {
    
    public void bulkDataArrival(BulkData bulk);
    
    public void batchArrival(Batch batch);
    
    public void bulkDeparture(long id);
    
    public void batchDeparture(long id);
    
}
