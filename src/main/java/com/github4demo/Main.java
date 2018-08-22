package com.github4demo;

import org.apache.commons.cli.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GitHub Issues -------------
 *
 * Create a program that generates a report about the the Issues belonging to a
 * list of github repositories ordered by creation time, and information about
 * the day when most Issues were created.
 *
 * Input: ----- List of 1 to n Strings with Github repositories references with
 * the format "owner/repository"
 *
 *
 * Output: ------ String representation of a Json dictionary with the following
 * content:
 *
 * - "issues": List containing all the Issues related to all the repositories
 * provided. The list should be ordered by the Issue "created_at" field (From
 * oldest to newest) Each entry of the list will be a dictionary with basic
 * Issue information: "id", "state", "title", "repository" and "created_at"
 * fields. Issue entry example: { "id": 1, "state": "open", "title": "Found a
 * bug", "repository": "owner1/repository1", "created_at":
 * "2011-04-22T13:33:48Z" }
 *
 * - "top_day": Dictionary with the information of the day when most Issues were
 * created. It will contain the day and the number of Issues that were created
 * on each repository this day If there are more than one "top_day", the latest
 * one should be used. example: { "day": "2011-04-22", "occurrences": {
 * "owner1/repository1": 8, "owner2/repository2": 0, "owner3/repository3": 2 } }
 *
 *
 * Output example: --------------
 *
 * {
 * "issues": [ { "id": 38, "state": "open", "title": "Found a bug",
 * "repository": "owner1/repository1", "created_at": "2011-04-22T13:33:48Z" }, {
 * "id": 23, "state": "open", "title": "Found a bug 2", "repository":
 * "owner1/repository1", "created_at": "2011-04-22T18:24:32Z" }, { "id": 24,
 * "state": "closed", "title": "Feature request", "repository":
 * "owner2/repository2", "created_at": "2011-05-08T09:15:20Z" } ], "top_day": {
 * "day": "2011-04-22", "occurrences": { "owner1/repository1": 2,
 * "owner2/repository2": 0 } } }
 *
 * --------------------------------------------------------
 *
 * You can create the classes and methods you consider. You can use any library
 * you need. Good modularization, error control and code style will be taken
 * into account. Memory usage and execution time will be taken into account.
 *
 * Good Luck!
 */
public class Main {

    /**
     *
     * Gah!!  Ok, I'm an idiot.  I seemed to pass over this comment for whatever reason.  This program is
     * designed to take the repo lists in a line delimited file.  I would chalk this up to a misunderstanding of the
     * requirements and I should have asked more questions.  I would iterate to fix this interface.*
     * @param args String array with Github repositories with the format
     * "owner/repository"
     *
     * However, the above comments not withstanding, credentials for the api need to somehow be injected into the program.
     *             Hard coding these is obviously not the right choice.  So, I would iterate on the initial interface and try to
     *             nail it down.
     *
     */
    public static void main(String[] args) {
        Options options = new Options();
        Option repositoriesFile = new Option("r", "repositories_file", true, "file with newline delimited owner/repository designators: \nowner1/repo1\nowner2/repo2\n...");
        repositoriesFile.setRequired(true);
        options.addOption(repositoriesFile);
        Option gitAPIURL = new Option("u", "github_url", true, "github API URL version 4");
        gitAPIURL.setRequired(true);
        options.addOption(gitAPIURL);
        Option paging = new Option("p", "page_size", true, "Page size for API requests.  100 appears to be the upper limit");
        paging.setRequired(true);
        options.addOption(paging);
        Option token = new Option("t", "github_token", true, "github token for API access");
        token.setRequired(true);
        options.addOption(token);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String repoFileName = cmd.getOptionValue("repositories_file");
            String githubURL = cmd.getOptionValue("github_url");
            String githubToken = cmd.getOptionValue("github_token");
            Integer pageSize = Integer.parseInt(cmd.getOptionValue("page_size"));

            InputStream is = new FileInputStream(repoFileName);
            List<RepoRegistration> repoRegistrations = repoRegsFromStrings(repoLinesFromInputStream(is));
            IssueFetcher fetcher = new GithubV4IssueFetcher(pageSize, githubURL, githubToken);
            Stream<Issue> issues = fetcher.fetchIssuesForRepos(repoRegistrations);
            Set<String> repoSet = repoRegistrations.stream().map((repo -> {return repo.asString();})).collect(Collectors.toSet());
            Reporter reporter = new BasicReporter();
            String report = reporter.generateReport(issues, repoSet);
            System.out.println(report);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Github Issue Analyzer", options);
            System.exit(1);
        } catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }
    public static List<String> repoLinesFromInputStream(InputStream is) throws IOException{
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        List<String> result = new ArrayList<>();
        String line = reader.readLine();
        while (line != null){
            result.add(line);
            line = reader.readLine();
        }
        return result;
    }
    public static List<RepoRegistration> repoRegsFromStrings(List<String> registrationStrings){
        return registrationStrings.stream().map((string -> {
            String[] tokens = string.split("/");
            return new BasicRepoRegistration(tokens[0], tokens[1]);
        })).collect(Collectors.toList());
    }
}
