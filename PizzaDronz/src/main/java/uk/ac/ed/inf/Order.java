package uk.ac.ed.inf;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class representing the order
 */

public class Order {

    private String orderNo;
    private String orderDate;
    private String customer;
    private String creditCardNumber;
    private String creditCardExpiry;
    private String cvv;
    private int priceTotalInPence;
    private ArrayList<String> orderItems;


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
    }


    //parse the order of a given day and classify valid and invalid orders
    public static ArrayList<Order> getOrdersFromRestServer(String month, String date) throws MalformedURLException, InvalidPizzaCombinationException {

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


        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL(Restaurant.restaurantUrl));

        ArrayList<Order> validOrders = new ArrayList<>();
        ArrayList<Order> invalidOrders = new ArrayList<>();

//        for (Restaurant r : restaurants) {
//            for (Menu m : r.getMenu()) {
//                System.out.println(m.getName());
//                    }
//                }



        for(Order o: orderList){

            OrderOutcome result;

            if(o.creditCardNumber.length() != 16){ // sum of the credit card also has rule
                result = OrderOutcome.InvalidCardNumber;
            }
            else if(Integer.parseInt(o.creditCardExpiry.substring(0,2)) > Integer.parseInt(month)
                    && (Integer.parseInt(o.creditCardExpiry.substring(3,5)) <= 23)) {
                result = OrderOutcome.InvalidExpiryDate;
            }
            else if(o.cvv.length() != 3){
                result = OrderOutcome.InvalidCvv;
            }
            else if(o.priceTotalInPence % 100 != 0){ // what if using deliveryCost obtain different answer as o.priceTotalInPence ?
                result = OrderOutcome.InvalidTotal;
            }

            else if ((o.orderNo.length() != 8) || (o.customer.length() < 1)){
                result = OrderOutcome.Invalid;
            }
            else{
                result = Order.isValidItems(restaurants, o.orderItems);
            }



            if(result == OrderOutcome.ValidButNotDelivered){
                validOrders.add(o);
            }
            else{
                invalidOrders.add(o);
            }

        }


        return validOrders;
    }




    // helper function for determining if an order is valid
    public static OrderOutcome isValidItems(Restaurant[] restaurants, List<String> orderItems) {

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

                        if (restaurant == null) {
                            restaurant = r;
                        }
                    }
                }
            }

            if (!foundInRestaurant) {
                result = OrderOutcome.InvalidPizzaNotDefined;
            } else if ((restaurant != null) && (!currentRestaurant.equals(restaurant))) {
                result = OrderOutcome.InvalidPizzaCombinationMultipleSuppliers;
            }
        }

        return result;
    }







    /**
     * Count the total cost of a given order in pence, including an extra delivery cost of 100p
     *
     * @param restaurants an array of restaurants whose menus are used to match ordered items with
     * @param orderItems a list of items to be calculated the total cost with
     *
     * @return the total cost of the input order plus 100p delivery cost
     *
     * @throws IllegalArgumentException if there is no item in the input order
     * @throws IllegalArgumentException if the number of pizzas in the order exceeds the maximum capacity of the drone (4)
     * @throws IllegalArgumentException if an item in the order cannot be found in any of the given restaurant
     * @throws InvalidPizzaCombinationException if the order contains items that cannot be delivered from the same restaurant
     */
    public static int getDeliveryCost(Restaurant[] restaurants, String... orderItems) throws InvalidPizzaCombinationException {

        List<String> orderItemList = Arrays.stream(orderItems).toList();

        if(Order.isValidItems(restaurants, orderItemList) == OrderOutcome.InvalidPizzaCount){
            throw new IllegalArgumentException("The order is invalid since the number of items is either 0 or exceeds the maximum capacity of drone");
        }

        else if(Order.isValidItems(restaurants, orderItemList) == OrderOutcome.InvalidPizzaNotDefined){
            throw new IllegalArgumentException("There's item cannot be found in any restaurant");
        }

        else if(Order.isValidItems(restaurants, orderItemList) == OrderOutcome.InvalidPizzaCombinationMultipleSuppliers){
            throw new InvalidPizzaCombinationException("The items in the order list cannot be delivered from the same restaurant");
        }

        //if above exceptions are not thrown, the order is valid, and we will find the total cost

        int totalCost = 0;

        // For each ordered item, find its menu among all restaurants and its price in pence,
        // then add its cost to the total cost
        for(String s: orderItems){
            for(Restaurant r: restaurants){
                for(Menu m: r.getMenu()){
                    if(m.getName().equals((s))){
                        totalCost += m.getPriceInPence();
                    }
                }
            }


        }

        // Add the delivery cost
        return totalCost + 100;


    }


    public static ArrayList<Restaurant> findRestaurant(Restaurant[] restaurants, String month, String date) throws InvalidPizzaCombinationException, MalformedURLException {
        if(month.length() == 1){
            month = "0" + month;
        }

        if(date.length() == 1){
            date = "0" + date;
        }

        ArrayList<Restaurant> correspondingRestaurants = new ArrayList<>();

        ArrayList<Order> validOrders = Order.getOrdersFromRestServer(month, date);

        for(Order o: validOrders){
            String first_menu = o.getOrderItems().get(0);
            for(Restaurant r: restaurants){
                Menu[] menu_list  = r.getMenu();
                for(Menu m: menu_list){
                    if(m.getName().equals(first_menu)){
                        correspondingRestaurants.add(r);
                    }
                }
            }
        }

        return correspondingRestaurants;
    }


    public ArrayList<String> getOrderItems() {
        return orderItems;
    }

    public static void main(String[] args) throws InvalidPizzaCombinationException, MalformedURLException {

        ArrayList<Order> result = Order.getOrdersFromRestServer("04","15");

        Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL(Restaurant.restaurantUrl));
        ArrayList<Restaurant> correspondingRestaurants = Order.findRestaurant(restaurants,"04       ","15");

        int count = 0;
        int count_2 = 0;
        for(Order o: result) {
            System.out.println(o.orderNo);
            count ++;
        }
        for (Restaurant r: correspondingRestaurants){
            System.out.println(r.getName());
            count_2 ++;
        }

        System.out.println(count);
        System.out.println(count_2);
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