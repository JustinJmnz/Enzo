package com.enzo.enzo;

/**
 * Created by Justin on 11/7/2016.
 */

public class UserContact {
    private String name;
    private String number;

    public UserContact() {
        this.name = "No Name";
        this.number = "No Number";
    }
    public UserContact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public String getName() {
        return this.name;
    }

    public String getNumber() {
        return this.number;
    }
}
