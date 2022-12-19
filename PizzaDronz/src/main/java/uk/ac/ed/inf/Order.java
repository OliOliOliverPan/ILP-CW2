package uk.ac.ed.inf;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;



/**
 * Class representing the order
 */

public class Order implements Comparable<Order>{

    private String orderNo;
    private String orderDate;
    private String customer;
    private String creditCardNumber;
    private String creditCardExpiry;
    private String cvv;
    private int priceTotalInPence; // price of the order extracted from REST
    private ArrayList<String> orderItems;

    private Restaurant correspondingRestaurant;
    private LngLat correspondingRestaurantCoordinate;
    private int calculatedOrderCost; // calculated price of the order

    private OrderOutcome orderStatus;



    //This constructor is for extracting order details from REST
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





    //parse the order of a given day and classify valid and invalid orders
    public static ArrayList<Order> getOrdersFromRestServer(String month, String date) throws MalformedURLException {

        //for month and date, if they are less than 10 and do not start with 0, a 0 will be automatically added before them
        if(month.length() == 1){
            month = "0" + month;
        }

        if(date.length() == 1){
            date = "0" + date;
        }


        Order[] orderList = null;

        String orderURL = "https://ilp-rest.azurewebsites.net/orders/";
        orderURL = orderURL + "2023" + "-" + month + "-" + date;

        try {
            orderList = new ObjectMapper().readValue(new URL(orderURL), Order[].class);
        } catch (StreamReadException e){
            e.printStackTrace();
        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }


        ArrayList<Order> validOrders = new ArrayList<>();
        ArrayList<Order> invalidOrders = new ArrayList<>();


        for(Order o: orderList){

            OrderOutcome result;

            if(((o.getCreditCardNumber().length() != 16)) || (!o.isValidCreditCardNumber())) {
                result = OrderOutcome.InvalidCardNumber;
            }
            else if(Integer.parseInt(o.getCreditCardExpiry().substring(0,2)) > Integer.parseInt(month)
                    && (Integer.parseInt(o.getCreditCardExpiry().substring(3,5)) <= 23)) {
                result = OrderOutcome.InvalidExpiryDate;
            }
            else if(o.getCvv().length() != 3){
                result = OrderOutcome.InvalidCvv;
            }
//            else if((getDeliveryCost(o) != -1) && (getDeliveryCost(o) != o.getPriceTotalInPence())){
//                result = OrderOutcome.InvalidTotal;
//            }

            else if ((o.getOrderNo().length() != 8) || (o.getCustomer().length() < 1)){
                result = OrderOutcome.Invalid;
            }
            else{
                result = Order.isValidOrder(o);
            }



            if(result == OrderOutcome.ValidButNotDelivered){

                validOrders.add(o);
            }
            else{
                invalidOrders.add(o);
            }

        }

        Collections.sort(validOrders);

        return validOrders;
    }




    // helper function for determining if the detail of an order is valid
    private static OrderOutcome isValidOrder(Order order) throws MalformedURLException {

        Restaurant[] restaurants = Restaurant.getINSTANCE();

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


        order.orderStatus = result;

        return result;
    }



    // Apply Luhn's Algorithm to determine if a credit card number is valid or not
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


    public ArrayList<String> getOrderItems() {
        return this.orderItems;
    }

    public String getOrderDate() {
        return this.orderDate;
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

    public Restaurant getCorrespondingRestaurant() { return correspondingRestaurant;}

    public LngLat getCorrespondingRestaurantCoordinate() {
        return correspondingRestaurantCoordinate;
    }
    public OrderOutcome getOrderStatus(){return orderStatus;}

    public void setOrderStatus(OrderOutcome orderStatus){ this.orderStatus = orderStatus; }







    public static void main(String[] args) throws MalformedURLException {

        ArrayList<Order> result = Order.getOrdersFromRestServer("04","15");

        ArrayList<Restaurant> correspondingRestaurants = new ArrayList<>();
        ArrayList<LngLat> correspondingRestaurantsCoordinate = new ArrayList<>();

        for(int i = 0 ; i < result.size(); i ++) {
            correspondingRestaurants.add(result.get(i).correspondingRestaurant);
            correspondingRestaurantsCoordinate.add(result.get(i).correspondingRestaurantCoordinate);
        }


        for(Order o: result) {
            System.out.println(o.getCorrespondingRestaurant().getName());
        }
        for (Restaurant r: correspondingRestaurants){
            System.out.println(r.getName());

        }



        //System.out.println(isValidCreditCardNumber("5102312041208609"));
    }


}






/*
        Menu[] civerinosSliceMenu = new Menu[] {new Menu("Margarita", 1000), new Menu("Calzone", 1400)};
        Restaurant civerinosSlice = new Restaurant("Civerinos Slice",  civerinosSliceMenu);

        Menu[] soraLellaVeganMenu = new Menu[] {new Menu("Meat Lover", 1400), new Menu("Vegan Delight", 1100)};
        Restaurant soraLellaVegan = new Restaurant("Sora Lella Vegan",  soraLellaVeganMenu);

        Menu[] dominosMenu = new Menu[] {new Menu("Super Cheese", 1400), new Menu("All Shrooms", 900)};
        Restaurant dominos = new Restaurant("Domino's Pizza - Edinburgh - Southside",  dominosMenu);

        Menu[] sodebergPavillionMenu = new Menu[] {new Menu("Proper Pizza", 1400), new Menu("Pineapple & Ham & Cheese", 900)};
        Restaurant sodebergPavillion = new Restaurant("Sodeberg Pavillion",  sodebergPavillionMenu);
        Restaurant[] restaurants = new Restaurant[]{civerinosSlice, soraLellaVegan, dominos, sodebergPavillion};
        System.out.println(getDeliveryCost(restaurants,"Super Cheese","Super Cheese","Super Cheese","Super Cheese", "Proper Pizza"));
        System.out.println(getDeliveryCost(restaurants,"Super Cheese","Super Cheese", "All Shrooms"));

 */