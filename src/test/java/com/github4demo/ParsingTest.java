package com.github4demo;

//import org.json.simple.JSONValue;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
/**
 * Created by djchuy on 8/19/18.
 */
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParsingTest {
    private String testJsonExtractString = "{\n" +
            "    \"data\": {\n" +
            "        \"repository\": {\n" +
            "            \"issues\": {\n" +
            "                \"edges\": [\n" +
            "                    {\n" +
            "                        \"cursor\": \"Y3Vyc29yOnYyOpHOBuikPg==\",\n" +
            "                        \"node\": {\n" +
            "                            \"createdAt\": \"2015-11-09T16:13:29Z\",\n" +
            "                            \"title\": \"Add v-model 'options' parameter back for select form items.\",\n" +
            "                            \"state\": \"CLOSED\",\n" +
            "                            \"id\": \"MDU6SXNzdWUxMTU5MDk2OTQ=\"\n" +
            "                        }\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"cursor\": \"Y3Vyc29yOnYyOpHOBui6nQ==\",\n" +
            "                        \"node\": {\n" +
            "                            \"createdAt\": \"2015-11-09T16:40:30Z\",\n" +
            "                            \"title\": \"Incorrect warning about two-way bind on v-for alias with filters not working.\",\n" +
            "                            \"state\": \"CLOSED\",\n" +
            "                            \"id\": \"MDU6SXNzdWUxMTU5MTU0MjE=\"\n" +
            "                        }\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"cursor\": \"Y3Vyc29yOnYyOpHOBulCYA==\",\n" +
            "                        \"node\": {\n" +
            "                            \"createdAt\": \"2015-11-09T19:42:53Z\",\n" +
            "                            \"title\": \"v-ref inside component that appears multiple times\",\n" +
            "                            \"state\": \"CLOSED\",\n" +
            "                            \"id\": \"MDU6SXNzdWUxMTU5NTAxNzY=\"\n" +
            "                        }\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"cursor\": \"Y3Vyc29yOnYyOpHOBupvBQ==\",\n" +
            "                        \"node\": {\n" +
            "                            \"createdAt\": \"2015-11-10T04:59:22Z\",\n" +
            "                            \"title\": \"a problem about table components\",\n" +
            "                            \"state\": \"CLOSED\",\n" +
            "                            \"id\": \"MDU6SXNzdWUxMTYwMjcxNDE=\"\n" +
            "                        }\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"cursor\": \"Y3Vyc29yOnYyOpHOBurgbw==\",\n" +
            "                        \"node\": {\n" +
            "                            \"createdAt\": \"2015-11-10T07:54:51Z\",\n" +
            "                            \"title\": \"The 'afterEnter' hook dose not 'wait for the transition to finish'\",\n" +
            "                            \"state\": \"CLOSED\",\n" +
            "                            \"id\": \"MDU6SXNzdWUxMTYwNTYxNzU=\"\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

    @Test
    public void testJsonExtract() throws Exception{
        JsonNode node = new JsonNode(testJsonExtractString);
        List<org.json.JSONObject> extractResults = GithubV4IssueFetcher.extractRawIssueData(node);
        List<Issue> issues = extractResults.stream().map((jsonObject -> {
            return GithubV4IssueFetcher.extractIssue(jsonObject, new BasicRepoRegistration("vuejs", "vue"));
        })).collect(Collectors.toList());
        issues.forEach((issue -> {
            Assert.assertEquals(issue.state(), "CLOSED");
        }));
    }
    @Test
    public void testQueryTemplate() throws Exception{
        GithubV4IssueFetcher fetcher = new GithubV4IssueFetcher(10, "https://api.github.com/graphql", "token");
        String expectedCursor = "{\"variables\":{\"repoOwner\":\"tensorflow\",\"repoName\":\"tensorflow\"},\"query\":\"query($repoName: String!, $repoOwner: String!) {repository(name: $repoName, owner: $repoOwner){issues(first: 10, after: \\\"ACURSOR\\\"){edges{cursor node{createdAt title state id}}}}}\"}";
        String expectedBeginning = "{\"variables\":{\"repoOwner\":\"tensorflow\",\"repoName\":\"tensorflow\"},\"query\":\"query($repoName: String!, $repoOwner: String!) {repository(name: $repoName, owner: $repoOwner){issues(first: 10){edges{cursor node{createdAt title state id}}}}}\"}";
        String actualCursor = fetcher.continueGraphQLQueryBody("tensorflow", "tensorflow", "ACURSOR");
        String actualBeginning = fetcher.startingGraphQLQueryBody("tensorflow", "tensorflow");
        Assert.assertEquals(expectedCursor, actualCursor);
        Assert.assertEquals(expectedBeginning, actualBeginning);
    }
}
