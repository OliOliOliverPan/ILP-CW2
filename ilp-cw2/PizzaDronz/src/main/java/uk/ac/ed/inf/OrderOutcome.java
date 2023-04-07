package uk.ac.ed.inf;

/**
 * Enum containing all possible outcomes of an order on a day
 */
public enum OrderOutcome {
    //an order is valid and has been delivered on the given day
    Delivered,

    //an order is valid but not delivered on the given day
    ValidButNotDelivered,

    //the payment credit card number of the order is invalid
    InvalidCardNumber,

    //the expiry date of order's payment credit card number is before the given time of delivery
    InvalidExpiryDate,

    //the cvv of order's payment credit card number is invalid
    InvalidCvv,

    //the price total in pence of the order is wrong
    InvalidTotal,

    //an item in the order that can not be found in any restaurant
    InvalidPizzaNotDefined,
    //the number of items in the order exceeds the drone's maximum capacity
    InvalidPizzaCount,

    //the order contains items from different restaurants
    InvalidPizzaCombinationMultipleSuppliers,

    //the customer name of the order is null or the order number is not 8 digits long
    Invalid
}
