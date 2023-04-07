package uk.ac.ed.inf;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;



/**
 * Class defining each order and its required methods
 */

public class Order implements Comparable<Order>{

    //fields or an order extracted from REST server
    private String orderNo;
    private String orderDate;
    private String customer;
    private String creditCardNumber;
    private String creditCardExpiry;
    private String cvv;
    // price of the order extracted from REST
    private int priceTotalInPence;
    //food items in the order
    private ArrayList<String> orderItems;



    //the restaurant for items of the order and its coordinate (assume the order is valid)
    private Restaurant correspondingRestaurant;
    private LngLat correspondingRestaurantCoordinate;


    // calculated price of the order in pence using helper method,
    //which will be compared with the one from REST server
    private int calculatedOrderCost;

    // the order outcome
    private OrderOutcome orderStatus;



    /**
     * Constructor of an order
     *
     * @param orderNo           the order number
     * @param orderDate         time of the order (in the format year-month-date)
     * @param customer          name of the order's customer
     * @param creditCardNumber  credit card number for the order's payment
     * @param creditCardExpiry  expiry time of the payment credit card (in the format of month/year)
     * @param cvv               cvv of the payment credit card
     * @param priceTotalInPence total cost of the order in pence
     * @param orderItems        food items in the order
     */
    public Order(@JsonProperty("orderNo")String orderNo, @JsonProperty("orderDate")String orderDate, @JsonProperty("customer")String customer, @JsonProperty("creditCardNumber")String creditCardNumber,
                 @JsonProperty("creditCardExpiry")String creditCardExpiry, @JsonProperty("cvv")String cvv, @JsonProperty("priceTotalInPence")int priceTotalInPence, @JsonProperty("orderItems")ArrayList<String> orderItems) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.customer = customer;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.priceTotalInPence = priceTotalInPence;
        this.orderItems = orderItems;

        this.correspondingRestaurant = null;
        this.correspondingRestaurantCoordinate = null;
        this.calculatedOrderCost = 100; // the delivery cost if (initially assume the order is valid)
    }



    /**
     * Determine if the coordinate of a point is inside the central area
     *
     * @param baseUrl  REST server base address
     * @param year     year specified to extract order data for
     * @param month    month specified to extract order data for
     * @param date     date specified to extract order data for
     *
     * @return an array of specified orders on the required time,
     *         with all valid orders followed by all invalid orders on that day,
     *         and all valid orders are sorted based on the distance between
     *         their corresponding restaurants and Appleton Tower
     *
     */

    public static ArrayList<Order> getOrdersFromRestServer(String baseUrl, String year, String month, String date) {

        if(! baseUrl.endsWith("/")){
            baseUrl += "/";
        }


        Order[] orderList = null;

        //URL to access orders of the specified time on REST server
        String orderURL = baseUrl + "orders/";
        orderURL = orderURL + "20" + year + "-" + month + "-" + date;

        try {
            orderList = new ObjectMapper().readValue(new URL(orderURL), Order[].class);
        } catch (StreamReadException e){
            e.printStackTrace();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }


        //classify valid and invalid orders

        ArrayList<Order> allOrders = new ArrayList<>();
        ArrayList<Order> invalidOrders = new ArrayList<>();


        for(Order o: orderList){


            //check each value of order details,
            //if an order passes all these criteria,
            //it is considered valid
            OrderOutcome result;

            if(((o.getCreditCardNumber().length() != 16)) || (!o.isValidCreditCardNumber())) {
                result = OrderOutcome.InvalidCardNumber;
            }
            else if(Integer.parseInt(o.getCreditCardExpiry().substring(0,2)) > Integer.parseInt(month)
                    && (Integer.parseInt(o.getCreditCardExpiry().substring(3,5)) <= Integer.parseInt(year))) {
                result = OrderOutcome.InvalidExpiryDate;
            }
            else if(o.getCvv().length() != 3){
                result = OrderOutcome.InvalidCvv;
            }

            else if ((o.getOrderNo().length() != 8) || (o.getCustomer().length() < 1)){
                result = OrderOutcome.Invalid;
            }

            else{
                result = Order.validRestaurantItems(baseUrl,o);
            }

            o.orderStatus = result;

            if(result == OrderOutcome.ValidButNotDelivered){

                allOrders.add(o);
            }
            else{
                invalidOrders.add(o);
            }

        }

        //sort all valid orders based on the distance of their restaurants to Appleton Tower in ascending order
        Collections.sort(allOrders);

        //place invalid orders after all valid ones
        allOrders.addAll(invalidOrders);

        return allOrders;
    }



    /**
     * Helper function for determining if the detail of an order is valid
     * regarding details of its restaurant and food items
     *
     * @param  baseUrl  REST server base address
     * @param  order    order to be determined validity
     *
     * @return the order outcome of the order
     *
     */
    private static OrderOutcome validRestaurantItems(String baseUrl, Order order) {

        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(baseUrl);

        ArrayList<String> orderItems= order.getOrderItems();

        // the returned value initialized to valid, and will be changed
        // only when any invalid order criterion is satisfied
        OrderOutcome result = OrderOutcome.ValidButNotDelivered;

        if((orderItems.size() == 0) || (orderItems.size() > 4)){
            result = OrderOutcome.InvalidPizzaCount;
        }


        //restaurant will be assigned with the value of the restaurant of the first ordered item in the following for loop,
        //currentRestaurant records the restaurant of each ordered item in each iteration of the for loop,
        //if these two variables are not equal in any iteration,
        //it means items from different restaurants are in the input order
        Restaurant restaurant = null;
        Restaurant currentRestaurant = null;



        // For each ordered item, find which restaurant and which menu in that restaurant it belongs to,
        // then add its cost to the total cost
        for (String s : orderItems) {

            // determine if an item exists in any restaurant from the input restaurant array
            boolean foundInRestaurant = false;

            for (Restaurant r : restaurants) {
                for (Menu m : r.getMenu()) {
                    if (m.getName().equals((s))) {
                        foundInRestaurant = true;
                        currentRestaurant = r;
                        order.correspondingRestaurant = r;
                        order.correspondingRestaurantCoordinate = r.getCoordinate();
                        order.calculatedOrderCost += m.getPriceInPence();

                        if (restaurant == null) {
                            restaurant = r;

                        }
                    }
                }
            }

            if (!foundInRestaurant) {
                result = OrderOutcome.InvalidPizzaNotDefined;
                break;
            } else if ((restaurant != null) && (!currentRestaurant.equals(restaurant))) {
                result = OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
                break;
            }

        }

        if(order.calculatedOrderCost != order.priceTotalInPence){
            result = OrderOutcome.InvalidTotal;
        }

        return result;
    }



    /**
     * Helper function for determining if order's credit card number is valid using Luhn's algorithm,
     * which is an algorithm checking the validity of credit card number based on the standard of credit car number
     *
     * @return true if order's credit card number fits the standard,
     *         false otherwise
     *
     */
    private boolean isValidCreditCardNumber(){
        String cardNumber = this.getCreditCardNumber();

        // convert each card number digit to int type
        int [] cardIntArray = new int[cardNumber.length()];

        for(int i = 0; i<cardNumber.length(); i ++){
            char c = cardNumber.charAt(i);
            cardIntArray[i] = Integer.parseInt("" + c);

        }

        for(int i = cardIntArray.length - 2; i >= 0; i = i-2){


            int num = cardIntArray[i] ;
            num = num * 2;

            //if the value of any digit after being doubled is two digits, save the sum of each digit
            if(num > 9){
                num = num%10 + (int) Math.floor(num / 10);
            }
            cardIntArray[i] = num;
        }

        int sum = Arrays.stream(cardIntArray).sum();

        return sum % 10 == 0;
    }





    //sort the order based on the distance between their corresponding restaurant and Appleton Tower
    @Override
    public int compareTo(Order o) {

            if(Drone.START_POSITION.distanceTo(this.correspondingRestaurantCoordinate) < Drone.START_POSITION.distanceTo(o.correspondingRestaurantCoordinate)){
                return -1;
            }
            else if(Drone.START_POSITION.distanceTo(this.correspondingRestaurantCoordinate) > Drone.START_POSITION.distanceTo(o.correspondingRestaurantCoordinate)){
                return 1;
            }
            else{
                return 0;
            }

    }


    //getters and setters for private fields of Order class
    public ArrayList<String> getOrderItems() {
        return this.orderItems;
    }


    public String getCreditCardNumber() {
        return this.creditCardNumber;
    }

    public String getCreditCardExpiry() {
        return this.creditCardExpiry;
    }

    public String getCvv() {
        return this.cvv;
    }

    public int getPriceTotalInPence() {
        return this.priceTotalInPence;
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public String getCustomer() {
        return this.customer;
    }


    public LngLat getCorrespondingRestaurantCoordinate() {
        return correspondingRestaurantCoordinate;
    }
    public OrderOutcome getOrderStatus(){return orderStatus;}

    public void setOrderStatus(OrderOutcome orderStatus){ this.orderStatus = orderStatus; }


}
