package uk.ac.ed.inf;

/**
 * Helper class for writing information of daily orders into json file
 */
public class JsonOrder {
    public String orderNo;

    public OrderOutcome orderOutcome;

    public int costInPence;

    /**
     * Constructor of JsonOrder class
     *
     * @param orderNo           the order's order number
     * @param orderOutcome      the final status of the order after drone has finished deliveries of the day
     * @param priceTotalInPence total price of the order in pence
     */
    public JsonOrder(String orderNo, OrderOutcome orderOutcome, int priceTotalInPence){
        this.orderNo = orderNo;
        this.orderOutcome = orderOutcome;
        this.costInPence = priceTotalInPence;
    }
}
