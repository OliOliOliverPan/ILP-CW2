package uk.ac.ed.inf;


/**
 * Class representing the order
 */

public class Order {


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
        if(orderItems.length == 0){
            throw new IllegalArgumentException("The order is invalid since it contains no item");
        }

        else if(orderItems.length > 4){
            throw new InvalidPizzaCombinationException("The order is invalid since it exceeds the maximum number of pizzas");
        }



        int totalCost = 0;

        //restaurant will be assigned with the value of the restaurant of the first ordered item in the following for loop,
        //currentRestaurant records the restaurant of each ordered item in each iteration of the for loop,
        //if these two variables are not equal in any iteration,
        //it means items from different restaurants are in the input order
        Restaurant restaurant = null;
        Restaurant currentRestaurant = null;


        // For each ordered item, find which restaurant and which menu in that restaurant it belongs to,
        // then add its cost to the total cost
        for(String s: orderItems){

            // determine if an item exists in any restaurant from the input restaurant array
            boolean foundInRestaurant = false;

            for(Restaurant r: restaurants){
                for(Menu m: r.getMenu()){
                    if(m.getName().equals((s))){
                        totalCost += m.getPriceInPence();
                        foundInRestaurant = true;
                        currentRestaurant = r;

                        if(restaurant == null){
                            restaurant = r;
                        }
                    }
                }
            }

            if(!foundInRestaurant){
                throw new IllegalArgumentException("The item cannot be found in any restaurant");
            }
            else if((restaurant != null) && (!currentRestaurant.equals(restaurant))) {
                throw new InvalidPizzaCombinationException("The items in the order list cannot be delivered from the same restaurant");
            }
        }

        // Add the delivery cost
        return totalCost + 100;


    }

}
