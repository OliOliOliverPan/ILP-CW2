package uk.ac.ed.inf;

/**
 * Class representing the custom exception fo InvalidPizzaCombinationException,
 * which will be thrown when an order contains types of pizzas that cannot be delivered from the same restaurant
 */
public class InvalidPizzaCombinationException extends Exception{

    public InvalidPizzaCombinationException(String s) {
        super(s);
    }
}
