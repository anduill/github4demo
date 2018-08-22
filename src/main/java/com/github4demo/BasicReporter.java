package com.github4demo;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by djchuy on 8/21/18.
 */
public class BasicReporter implements Reporter {
    @Override
    public String generateReport(Stream<Issue> issues, Set<String> allRepositories) {
        //We need to ensure that the issues are sorted before generating the report
        List<Issue> copy = issues.sorted(new Comparator<Issue>() {
            @Override
            public int compare(Issue o1, Issue o2) {
                return o1.createdAt().compareTo(o2.createdAt());
            }
        }).collect(Collectors.toList());
        if(copy.isEmpty()){
            return "{\"issues\":[], \"top_day\":{}}";
        }
        List<Map.Entry<String, List<Issue>>> groupedAndSorted = groupAndSortIssues(copy.stream());
        List<JSONObject> jsonObjects = copy.stream().map((issue -> {
            JSONObject result = new JSONObject();
            result.put("id", issue.id());
            result.put("state", issue.state());
            result.put("title", issue.title());
            result.put("repository", issue.repository());
            result.put("createdAt", issue.createdAt());
            return result;
        })).collect(Collectors.toList());
        JSONArray issueArray = new JSONArray(jsonObjects);
        Map.Entry<String, List<Issue>> mostIssues = groupedAndSorted.get(0);
        Map<String, Integer> groupedByRepo = groupByRepository(mostIssues.getValue(), allRepositories);
        JSONObject occurrences = new JSONObject(groupedByRepo);
        JSONObject topDay = new JSONObject();
        topDay.put("day", mostIssues.getKey());
        topDay.put("occurrences", occurrences);
        JSONObject result = new JSONObject();
        result.put("issues", issueArray);
        result.put("top_day", topDay);
        return result.toString(2);
    }
    public static Map<String, Integer> groupByRepository(List<Issue> issues, Set<String> allRepositories){
        Map<String, Integer> result = new HashMap<>();
        allRepositories.forEach((repo -> {
            result.put(repo, 0);
        }));
        issues.forEach((issue -> {
            if(!result.containsKey(issue.repository())){
                throw new RuntimeException("Issue is from an unregistered Repository: " + issue.repository());
            }
            result.put(issue.repository(), result.get(issue.repository()) + 1);
        }));
        return result;
    }
    public static List<Map.Entry<String, List<Issue>>> groupAndSortIssues(Stream<Issue> issues){
        Map<String, List<Issue>> groupedIssues = new HashMap<>();
        issues.forEach((issue -> {
            String dateString = (new DateTime(issue.createdAt())).toDateTimeISO().toString("YYYY-MM-dd");
            if(!groupedIssues.containsKey(dateString)){
                groupedIssues.put(dateString, new ArrayList<Issue>());
            }
            groupedIssues.get(dateString).add(issue);
        }));
        List<Map.Entry<String, List<Issue>>> sortedIssues = groupedIssues.entrySet().stream().sorted(new Comparator<Map.Entry<String, List<Issue>>>() {
            @Override
            public int compare(Map.Entry<String, List<Issue>> o1, Map.Entry<String, List<Issue>> o2) {
                if(o1.getValue().size() == o2.getValue().size()){
                    return -o1.getKey().compareTo(o2.getKey());
                }
                return o1.getValue().size() > o2.getValue().size() ? -1 : (o1.getValue().size() < o2.getValue().size() ? 1 : 0);
            }
        }).collect(Collectors.toList());
        return sortedIssues;
    }
}
