package com.barnizexpress.application;

/** A quote request that breaks one of the business rules. Reported to the client as 400. */
public class InvalidQuoteException extends RuntimeException {

    public InvalidQuoteException(String message) {
        super(message);
    }
}