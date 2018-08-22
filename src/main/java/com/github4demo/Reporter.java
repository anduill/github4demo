package com.github4demo;

import java.util.Set;
import java.util.stream.Stream;


public interface Reporter {
    public String generateReport(Stream<Issue> issues, Set<String> allRepositories);
}
