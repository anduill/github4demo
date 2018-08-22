package com.github4demo;

/**
 * Created by djchuy on 8/21/18.
 */
public class BasicIssue implements Issue {
    private String theId;
    private String theState;
    private String theTitle;
    private String theRepository;
    private String theCreatedAt;
    private String theCursor;

    public BasicIssue(String id, String state, String title, String repository, String createdAt, String cursor){
        theId = id;
        theState = state;
        theTitle = title;
        theRepository = repository;
        theCreatedAt = createdAt;
        theCursor = cursor;
    }
    public String id() {
        return theId;
    }

    public String state() {
        return theState;
    }

    public String title() {
        return theTitle;
    }

    public String repository() {
        return theRepository;
    }

    public String createdAt() {
        return theCreatedAt;
    }

    public String cursor() {
        return theCursor;
    }
}
