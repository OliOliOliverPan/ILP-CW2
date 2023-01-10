package uk.ac.ed.inf;


import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class representing the menu
 */
public class Menu {



    /**
     * The name of the pizza and its price in pence
     */
    private String name;

    private int priceInPence;


    /**
     * Constructor of the menu,
     * values of its fields will be drawn from the REST server
     *
     * @param name the name of the pizza
     * @param priceInPence the price of the pizza in pence
     */
    public Menu(@JsonProperty("name")String name, @JsonProperty("priceInPence")int priceInPence) {
        this.name = name;
        this.priceInPence = priceInPence;
    }


    // Getter methods for private fields of menu class
    public String getName() {

        return this.name;
    }


    public int getPriceInPence() {

        return this.priceInPence;
    }



}
