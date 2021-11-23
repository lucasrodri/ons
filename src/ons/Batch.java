package ons;

/**
 * The Batch class defines an object that has a group flows
 *
 */
import java.util.ArrayList;

public class Batch {

    private final long id;
    private final long[] bulksIds;
    private final int[] sources;
    private final int dst;
    private final int[] dataAmounts;
    private final double[] deadline;
    private final int cos;
    private ArrayList<BulkData> bulks = new ArrayList<>();
    private final double arrivalTime;
    private double servedTime;
    private double departureTime;
    private final double deadlineTime;

    /**
     * Create a new Batch object.
     *
     * @param bulksIds uniques identifiers of Batch and its BulkDatas
     * @param grupoSrc sources nodes of BulkData calls
     * @param dstFinal destination node
     * @param arrayVariations data amout plus variations for each BulkData call
     * @param deadline maximum tolerable transfer time (seconds)
     *
     */
    Batch(long id, long[] bulksIds, int[] grupoSrc, int dstFinal, int[] arrayVariations, double[] deadline, int cos, double arrivalTime, double[] deadlineTime) {
        this.id = id;
        this.bulksIds = bulksIds;
        this.sources = grupoSrc;
        this.dst = dstFinal;
        this.dataAmounts = arrayVariations;
        this.deadline = deadline;
        this.cos = cos;
        int size = this.sources.length;
        this.arrivalTime = arrivalTime;
        double minTime = Double.MAX_VALUE;
        for (int i = 0; i < deadlineTime.length; i++) {
            if (deadlineTime[i] < minTime) {
                minTime = deadlineTime[i];
            }
        }
        this.deadlineTime = arrivalTime + minTime;
        for (int i = 0; i < size; i++) {
            bulks.add(new BulkData(this.bulksIds[i], this.sources[i], this.dst, this.dataAmounts[i], this.deadline[i], this.cos, arrivalTime, arrivalTime + deadlineTime[i]));
        }
    }

    /**
     * Retrieves the unique identifier for a given Batch.
     *
     * @return the value of the Batch's id attribute
     */
    public long getID() {
        return this.id;
    }

    /**
     * Retrieves the array of the unique identifiers of BulkDatas into Batch.
     *
     *
     * @return the value's array of the BulkData's id attribute
     */
    public long[] getIds() {
        return this.bulksIds;
    }

    public int[] getAllDataAmounts() {
        return this.dataAmounts;
    }

    public int getDataAmount(int bulkIndex) {
        return this.dataAmounts[bulkIndex];
    }

    public int getSumDataAmounts() {
        int allDataAmounts = 0;
        for (BulkData bulk : bulks) {
            allDataAmounts += bulk.getDataAmount();
        }
        return allDataAmounts;
    }

    /**
     * Retrieves the all sorces.
     *
     * @return the sorces's array of BulkData's calls
     */
    public int[] getSources() {
        return this.sources;
    }

    /**
     * Retrieves the destination Batch.
     *
     * @return the destination of BulkData's calls
     */
    public int getDestination() {
        return this.dst;
    }

    public double[] getDeadlines() {
        return this.deadline;
    }

    public ArrayList<BulkData> getBulks() {
        return bulks;
    }

    public int getCOS() {
        return this.cos;
    }

    /**
     * Retrives the size of Batch or amount of flow inside one.
     *
     * @return the size of BatchArrivalEvent's Batch attribute
     */
    public int getSize() {
        return bulks.size();
    }

    /**
     * Prints all information related to the arriving Batch.
     *
     * @return string containing all the values of the batch's parameters
     */
    public String toTrace() {
        String trace = Long.toString(id) + " bulk data:\n";
        for (BulkData bulk : bulks) {
            trace += bulk.toTrace() + " # ";
        }
        return trace;
    }

    @Override
    public String toString() {
        String string = Long.toString(id) + "-> ";
        for (BulkData bulk : bulks) {
            string += bulk.toString() + " # ";
        }
        return string;
    }

    /**
     * Verifies that the rate meets the all bulk's requirements in this batch
     *
     * @param rate the rate in Mb
     * @return if the rate meets the all bulk's requirements in this batch,
     * false otherwise
     */
    boolean verifyRate(int[] rate, int byzantine) {
        int count = 0;
        for (int i = 0; i < getSize(); i++) {
            if (rate[i] != 0) {
                if (this.bulks.get(i).verifyRate(rate[i])) {
                    count++;
                }
            }
        }
        return count >= byzantine;
    }

    /**
     * Retorna o indice relativo ao bulk do batch
     *
     * @param bulk
     * @return
     */
    public int getIndex(BulkData bulk) {
        int index = -1;
        for (int i = 0; i < bulks.size(); i++) {
            if (bulk.equals(bulks.get(i))) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int getMaxDataAmount() {
        int maxValue = Integer.MIN_VALUE;
        for (BulkData bulk : bulks) {
            if (bulk.getDataAmount() > maxValue) {
                maxValue = bulk.getDataAmount();
            }
        }
        return maxValue;
    }
    
    public int getMinDataAmount() {
        int minValue = Integer.MAX_VALUE;
        for (BulkData bulk : bulks) {
            if (bulk.getDataAmount() < minValue) {
                minValue = bulk.getDataAmount();
            }
        }
        return minValue;
    }

    public double getMinDeadline() {
        double minValue = Double.MAX_VALUE;
        for (BulkData bulk : bulks) {
            if (bulk.getDeadline() < minValue) {
                minValue = bulk.getDeadline();
            }
        }
        return minValue;
    }
    
    public double getMaxDeadline() {
        double maxValue = Double.MIN_VALUE;
        for (BulkData bulk : bulks) {
            if (bulk.getDeadline() > maxValue) {
                maxValue = bulk.getDeadline();
            }
        }
        return maxValue;
    }

    public double getDeadlineTime() {
        return deadlineTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getServedTime() {
        return servedTime;
    }

    protected void setServedTime(double servedTime) {
        this.servedTime = servedTime;
        for (BulkData bulk : bulks) {
            bulk.setServedTime(servedTime);
        }
    }

    public double getDepartureTime() {
        return departureTime;
    }

    protected void setDepartureTime(double departureTime, int[] rate, double currentTime) {
        this.departureTime = departureTime;
        for (int i = 0; i < bulks.size(); i++) {
            if (rate[i] != 0) {
                bulks.get(i).setDepartureTime(currentTime + ( (double) bulks.get(i).getDataAmount() / (double) rate[i]));
            }
        }
    }

    public double getMaxDeparture(int[] rate) {
        double maxTime = Double.MIN_VALUE;
        for (int i = 0; i < bulks.size(); i++) {
            if (rate[i] != 0) {
                double time = ((double) bulks.get(i).getDataAmount() / (double) rate[i]);
                if (maxTime < time) {
                    maxTime = time;
                }
            }
        }
        return maxTime;
    }
    
    public double getMinDeparture(int[] rate) {
        double minTime = Double.MAX_VALUE;
        for (int i = 0; i < bulks.size(); i++) {
            double time = (bulks.get(i).getDataAmount()/rate[i]);
            if(minTime > time) {
                minTime = time;
            }
        }
        return minTime;
    }

}
