package br.com.psousa.up.rest;

/**
 * Created by Paulo on 01/03/2016.
 */
public enum MIMETypes {
    APPLICATION_JSON("application/json");

    private final String name;

    private MIMETypes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
