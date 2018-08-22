package com.github4demo;

/**
 * Created by djchuy on 8/21/18.
 */
public interface Issue {
    public String id();
    public String state();
    public String title();
    public String repository();
    public String createdAt();
    public String cursor();
}
