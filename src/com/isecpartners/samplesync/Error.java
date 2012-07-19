package com.isecpartners.samplesync;

/* superclass of all of our custom exceptions */
public abstract class Error extends Exception { 
    public Error(String msg) { super(msg); }

    /* a human-readable description of the error */
    public abstract String descr();
}

