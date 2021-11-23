package ons;

/**
 * Methods to treat the incoming of a BulkData object.
 *
 */
public class BulkDataArrivalEvent extends Event {

    private final BulkData bulk;

    /**
     * Creates a new BulkDataArrivalEvent object.
     *
     * @param bd the arriving Bulk Data request
     */
    public BulkDataArrivalEvent(BulkData bd) {
        this.bulk = bd;
    }

    /**
     * Retrives the bdUnicast attribute of the BulkDataArrivalEvent object.
     *
     * @return the BulkDataArrivalEvent's bd request attribute
     */
    public BulkData getBulkData() {
        return this.bulk;

    }

    /**
     * Prints all information related to the arriving BulkData request.
     *
     * @return string containing all the values of the BulkData request's
     * parameters
     */
    @Override
    public String toString() {
        return "Arrival: " + bulk.toString();
    }

}
