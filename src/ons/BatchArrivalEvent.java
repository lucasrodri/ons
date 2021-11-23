package ons;

/**
 *
 * Methods to treat the incoming of a Batch object.
 */
public class BatchArrivalEvent extends Event {

    private final Batch batch;

    /**
     * Create a new BatchArrivalEvent object.
     * @param batch
     */
    public BatchArrivalEvent(Batch batch) {
        this.batch = batch;
    }

    /**
     * Retrives the Batch attribute of the BatchArrivalEvent object.
     *
     * @return the BatchArrivalEvent's Batch attribute
     */
    public Batch getBatch() {
        return this.batch;
    }

    /**
     * Retrives the size of Batch or amount of flow inside one.
     *
     * @return the size of BatchArrivalEvent's Batch attribute
     */

    public int getBatchSize() {
        return this.batch.getSize();
    }

    /**
     * Prints all information related to the arriving Batch.
     *
     * @return string containing all the values of the batch's parameters
     */
    @Override
    public String toString() {
        return "BatchArrival: " + batch.toString();
    }
}
