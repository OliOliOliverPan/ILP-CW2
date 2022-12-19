package uk.ac.ed.inf;

public class JsonOrder {
    public String orderNo;

    public OrderOutcome orderOutcome;

    public int costInPence;

    //This constructor is for writing order data into JSON file
    public JsonOrder(String orderNo, OrderOutcome orderOutcome, int priceTotalInPence){
        this.orderNo = orderNo;
        this.orderOutcome = orderOutcome;
        this.costInPence = priceTotalInPence;
    }
}