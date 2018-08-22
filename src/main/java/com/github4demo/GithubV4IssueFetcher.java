package com.github4demo;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.json.simple.JSONObject;
import java.util.stream.Collectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by djchuy on 8/21/18.
 */
public class GithubV4IssueFetcher extends IssueFetcher {
    private static String ID = "id";
    private static String STATE = "state";
    private static String CREATED_AT = "createdAt";
    private static String TITLE = "title";
    private static String DATA = "data";
    private static String REPO = "repository";
    private static String ISSUES = "issues";
    private static String EDGES = "edges";
    private static String CURSOR = "cursor";
    private static String NODE = "node";

    private Integer pageSize;
    private String githubURL;
    private String theToken;
    public static String beginningTemplate = "query($repoName: String!, $repoOwner: String!) {repository(name: $repoName, owner: $repoOwner){issues(first: ${pageSize}){edges{cursor node{createdAt title state id}}}}}";
    public static String cursorQueryTemplate = "query($repoName: String!, $repoOwner: String!) {repository(name: $repoName, owner: $repoOwner){issues(first: ${pageSize}, after: \"${cursor}\"){edges{cursor node{createdAt title state id}}}}}";
    public GithubV4IssueFetcher(Integer ps, String url, String token){
        pageSize = ps;
        githubURL = url;
        theToken = token;
    }

    public String startingGraphQLQueryBody(String repoOwner, String repoName){
        Map<String,String> valuesMap = new HashMap<String, String>();
        valuesMap.put("pageSize", "" + pageSize);
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String queryString = sub.replace(beginningTemplate);
        JSONObject result = new JSONObject();
        JSONObject variablesJson = new JSONObject();
        variablesJson.put("repoName", repoName);
        variablesJson.put("repoOwner", repoOwner);
        result.put("query", queryString);
        result.put("variables", variablesJson);
        return result.toJSONString();
    }
    public String continueGraphQLQueryBody(String repoOwner, String repoName, String cursor){
        Map<String,String> valuesMap = new HashMap<String, String>();
        valuesMap.put("cursor", cursor);
        valuesMap.put("pageSize", "" + pageSize);
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String queryString = sub.replace(cursorQueryTemplate);
        JSONObject result = new JSONObject();
        JSONObject variablesJson = new JSONObject();
        variablesJson.put("repoName", repoName);
        variablesJson.put("repoOwner", repoOwner);
        result.put("query", queryString);
        result.put("variables", variablesJson);
        return result.toJSONString();
    }

    @Override
    public Stream<Issue> fetchIssuesForRepo(RepoRegistration registration) {
        try {
            HttpResponse<JsonNode> beginningResponse = Unirest.post(githubURL)
                    .header("Authorization", "Bearer " + theToken)
                    .header("Content-Type", "application/json")
                    .body(new JsonNode(startingGraphQLQueryBody(registration.repoOwner(), registration.repoName())))
                    .asJson();

            List<Issue> newIssues = extractRawIssueData(beginningResponse.getBody()).stream().map(new Function<org.json.JSONObject, Issue>() {
                @Override
                public Issue apply(org.json.JSONObject jsonObject) {
                    return extractIssue(jsonObject, registration);
                }
            }).collect(Collectors.toList());
            Stream<Issue> result = new ArrayList<Issue>().stream();
            result = Stream.concat(result, newIssues.stream());
            while(newIssues.size() >= pageSize){
                HttpResponse<JsonNode> cursorResponse = Unirest.post(githubURL)
                        .header("Authorization", "Bearer " + theToken)
                        .header("Content-Type", "application/json")
                        .body(new JsonNode(continueGraphQLQueryBody(registration.repoOwner(), registration.repoName(), newIssues.get(newIssues.size()-1).cursor())))
                        .asJson();
                newIssues = extractRawIssueData(cursorResponse.getBody()).stream().map(new Function<org.json.JSONObject, Issue>() {
                    @Override
                    public Issue apply(org.json.JSONObject jsonObject) {
                        return extractIssue(jsonObject, registration);
                    }
                }).collect(Collectors.toList());
                result = Stream.concat(result, newIssues.stream());
            }
            return result;

        } catch (Exception e){
            throw new RuntimeException("Error hitting github URL or Processing Error: " + e.getMessage(), e);
        }
    }
    public static List<org.json.JSONObject> extractRawIssueData(JsonNode node){
        List<org.json.JSONObject> result = new ArrayList<>();
        Iterator<Object> objects = node.getObject().getJSONObject(DATA).getJSONObject(REPO).getJSONObject(ISSUES).getJSONArray(EDGES).iterator();
        while(objects.hasNext()){
            result.add((org.json.JSONObject) objects.next());
        }
        return result;
    }
    public static Issue extractIssue(org.json.JSONObject obj, RepoRegistration registration){
        org.json.JSONObject inner = obj.getJSONObject(NODE);
        return new BasicIssue(inner.getString(ID), inner.getString(STATE), inner.getString(TITLE), registration.asString(), inner.getString(CREATED_AT), obj.getString(CURSOR));
    }
}
