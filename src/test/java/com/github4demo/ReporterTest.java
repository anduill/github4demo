package com.github4demo;

import com.mashape.unirest.http.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by djchuy on 8/22/18.
 */
public class ReporterTest {
    private String testInversionCorrectionString = "{\n" +
            "  \"data\": {\n" +
            "    \"repository\": {\n" +
            "      \"issues\": {\n" +
            "        \"edges\": [\n" +
            "          {\n" +
            "            \"cursor\": \"Y3Vyc29yOnYyOpHOBuikPg==\",\n" +
            "            \"node\": {\n" +
            "              \"createdAt\": \"2015-11-11T16:13:29Z\",\n" +
            "              \"title\": \"Add v-model 'options' parameter back for select form items.\",\n" +
            "              \"state\": \"CLOSED\",\n" +
            "              \"id\": \"MDU6SXNzdWUxMTU5MDk2OTQ=\"\n" +
            "            }\n" +
            "          },\n" +
            "          {\n" +
            "            \"cursor\": \"Y3Vyc29yOnYyOpHOBui6nQ==\",\n" +
            "            \"node\": {\n" +
            "              \"createdAt\": \"2015-11-09T16:40:30Z\",\n" +
            "              \"title\": \"Incorrect warning about two-way bind on v-for alias with filters not working.\",\n" +
            "              \"state\": \"CLOSED\",\n" +
            "              \"id\": \"MDU6SXNzdWUxMTU5MTU0MjE=\"\n" +
            "            }\n" +
            "          },\n" +
            "          {\n" +
            "            \"cursor\": \"Y3Vyc29yOnYyOpHOBulCYA==\",\n" +
            "            \"node\": {\n" +
            "              \"createdAt\": \"2015-11-09T19:42:53Z\",\n" +
            "              \"title\": \"v-ref inside component that appears multiple times\",\n" +
            "              \"state\": \"CLOSED\",\n" +
            "              \"id\": \"MDU6SXNzdWUxMTU5NTAxNzY=\"\n" +
            "            }\n" +
            "          },\n" +
            "          {\n" +
            "            \"cursor\": \"Y3Vyc29yOnYyOpHOBupvBQ==\",\n" +
            "            \"node\": {\n" +
            "              \"createdAt\": \"2015-11-10T04:59:22Z\",\n" +
            "              \"title\": \"a problem about table components\",\n" +
            "              \"state\": \"CLOSED\",\n" +
            "              \"id\": \"MDU6SXNzdWUxMTYwMjcxNDE=\"\n" +
            "            }\n" +
            "          },\n" +
            "          {\n" +
            "            \"cursor\": \"Y3Vyc29yOnYyOpHOBurgbw==\",\n" +
            "            \"node\": {\n" +
            "              \"createdAt\": \"2015-11-10T07:54:51Z\",\n" +
            "              \"title\": \"The 'afterEnter' hook dose not 'wait for the transition to finish'\",\n" +
            "              \"state\": \"CLOSED\",\n" +
            "              \"id\": \"MDU6SXNzdWUxMTYwNTYxNzU=\"\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    private static String singleResponse = "{\n" +
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
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";

    @Test
    public void testOrderingAndGrouping(){
        JsonNode node = new JsonNode(testInversionCorrectionString);
        List<JSONObject> extractResults = GithubV4IssueFetcher.extractRawIssueData(node);
        List<Issue> issues = extractResults.stream().map((jsonObject -> {
            return GithubV4IssueFetcher.extractIssue(jsonObject, new BasicRepoRegistration("vuejs", "vue"));
        })).collect(Collectors.toList());
        Set<String> allRepos = new HashSet<>();
        allRepos.add("vuejs/vue");
        Reporter reporter = new BasicReporter();
        List<Map.Entry<String, List<Issue>>> dayGrouping = BasicReporter.groupAndSortIssues(issues.stream());
        Assert.assertEquals(3, dayGrouping.get(0).getValue().size());
        Assert.assertEquals(1, dayGrouping.get(1).getValue().size());
        Assert.assertEquals(1, dayGrouping.get(2).getValue().size());
        String jsonResult = reporter.generateReport(issues.stream(), allRepos);
        JsonNode outputNode = new JsonNode(jsonResult);
        JSONArray actualIssues = outputNode.getObject().getJSONArray("issues");
        JSONObject firstRecord = actualIssues.getJSONObject(0);
        Assert.assertEquals("2015-11-09T16:40:30Z", firstRecord.getString("createdAt"));
    }

    @Test
    public void testGroupingByRepositories(){
        JsonNode node = new JsonNode(testInversionCorrectionString);
        List<JSONObject> extractResults = GithubV4IssueFetcher.extractRawIssueData(node);
        List<Issue> issues = new ArrayList<>();
        issues.add(GithubV4IssueFetcher.extractIssue(extractResults.get(0), new BasicRepoRegistration("vuejs", "vue")));
        issues.add(GithubV4IssueFetcher.extractIssue(extractResults.get(1), new BasicRepoRegistration("max", "repo")));
        issues.add(GithubV4IssueFetcher.extractIssue(extractResults.get(2), new BasicRepoRegistration("vuejs", "vue")));
        Set<String> allRepos = new HashSet<>();
        allRepos.add("vuejs/vue");
        allRepos.add("max/repo");
        Map<String, Integer> actualRepoCount = BasicReporter.groupByRepository(issues, allRepos);
        Map<String, Integer> expectedRepoCount = new HashMap<>();
        expectedRepoCount.put("max/repo", 1);
        expectedRepoCount.put("vuejs/vue", 2);
        Assert.assertEquals(expectedRepoCount, actualRepoCount);
    }
    @Test
    public void testBadRepos(){
        JsonNode node = new JsonNode(testInversionCorrectionString);
        List<JSONObject> extractResults = GithubV4IssueFetcher.extractRawIssueData(node);
        List<Issue> issues = new ArrayList<>();
        issues.add(GithubV4IssueFetcher.extractIssue(extractResults.get(0), new BasicRepoRegistration("vuejs", "vue")));
        issues.add(GithubV4IssueFetcher.extractIssue(extractResults.get(1), new BasicRepoRegistration("max", "repo")));//This entry is bad and should cause and error
        issues.add(GithubV4IssueFetcher.extractIssue(extractResults.get(2), new BasicRepoRegistration("vuejs", "vue")));
        Set<String> allRepos = new HashSet<>();
        allRepos.add("vuejs/vue");
        try{
            Map<String, Integer> actualRepoCount = BasicReporter.groupByRepository(issues, allRepos);
            Assert.assertTrue(false);
        } catch (Exception e){
            Assert.assertTrue(true);
        }

    }
    @Test
    public void testReportFormat(){
        JsonNode node = new JsonNode(singleResponse);
        List<JSONObject> extractResults = GithubV4IssueFetcher.extractRawIssueData(node);
        List<Issue> issues = extractResults.stream().map((jsonObject -> {
            return GithubV4IssueFetcher.extractIssue(jsonObject, new BasicRepoRegistration("vuejs", "vue"));
        })).collect(Collectors.toList());
        Set<String> allRepos = new HashSet<>();
        allRepos.add("vuejs/vue");
        Reporter reporter = new BasicReporter();
        String jsonResult = reporter.generateReport(issues.stream(), allRepos);
        JsonNode expectedResult = new JsonNode("{\n" +
                "  \"top_day\": {\n" +
                "    \"occurrences\": {\"vuejs/vue\": 1},\n" +
                "    \"day\": \"2015-11-09\"\n" +
                "  },\n" +
                "  \"issues\": [{\n" +
                "    \"createdAt\": \"2015-11-09T16:13:29Z\",\n" +
                "    \"id\": \"MDU6SXNzdWUxMTU5MDk2OTQ=\",\n" +
                "    \"state\": \"CLOSED\",\n" +
                "    \"title\": \"Add v-model 'options' parameter back for select form items.\",\n" +
                "    \"repository\": \"vuejs/vue\"\n" +
                "  }]\n" +
                "}");
        JsonNode actualResult = new JsonNode(jsonResult);
        Assert.assertEquals(expectedResult.getObject().toString(), actualResult.getObject().toString());
    }
}
