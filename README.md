# Problem Description
Create a program that generates a report about the the Issues belonging to a
list of github repositories ordered by creation time, and information about
the day when most Issues were created.

## Specified Input
List of 1 to n Strings with Github repositories references with
the format "owner/repository"
### Modified Input
I needed a way to pass several configuration arguments to the program (i.e. github API url, github API token...etc)
I used a simple command line parser instead and put the Github repositories into a file.  Please
see "sampleRepoList.txt" under test/resources as an example.
```bash
usage: Github Issue Analyzer
 -p,--page_size <arg>           Page size for API requests.  100 appears
                                to be the upper limit
 -r,--repositories_file <arg>   file with newline delimited
                                owner/repository designators:
                                owner1/repo1
                                owner2/repo2
                                ...
 -t,--github_token <arg>        github token for API access
 -u,--github_url <arg>          github API URL version 4
```
 
## Specified Output
String representation of a Json dictionary with the following content:
 
### Issues
List containing all the Issues related to all the repositories
provided. The list should be ordered by the Issue "created_at" field (From
oldest to newest) Each entry of the list will be a dictionary with basic
Issue information: "id", "state", "title", "repository" and "created_at"
fields. Issue entry example:
```json
{
  "id": 1,
  "state": "open",
  "title": "Found a bug",
  "repository": "owner1/repository1",
  "created_at": "2011-04-22T13:33:48Z"
}
```
### top_day 
Dictionary with the information of the day when most Issues were
created. It will contain the day and the number of Issues that were created
on each repository this day If there are more than one "top_day", the latest
one should be used. example:
```json
{
  "day": "2011-04-22",
  "occurrences": {
    "owner1/repository1": 8,
    "owner2/repository2": 0,
    "owner3/repository3": 2
  }
}
```
### Complete Output Example
```json
{
  "issues": [
    {
      "id": 38,
      "state": "open",
      "title": "Found a bug",
      "repository": "owner1/repository1",
      "created_at": "2011-04-22T13:33:48Z"
    },
    {
      "id": 23,
      "state": "open",
      "title": "Found a bug 2",
      "repository": "owner1/repository1",
      "created_at": "2011-04-22T18:24:32Z"
    },
    {
      "id": 24,
      "state": "closed",
      "title": "Feature request",
      "repository": "owner2/repository2",
      "created_at": "2011-05-08T09:15:20Z"
    }
  ],
  "top_day": {
    "day": "2011-04-22",
    "occurrences": {
      "owner1/repository1": 2,
      "owner2/repository2": 0
    }
  }
}
``` 



## Performance Discussion
There are definitely some performance issues with this implementation- -Specifically memory.

### Single JSON Report
The output of the program is a single Json-object report (i.e. not line delimited strings).  Most default behavior of existing JSON libraries retains the
entire object in memory.  So, since we are putting ALL the issues from ALL the repos into a single report, all the
respective data would be in memory by default.  I'm sure with enough investigation, I could find a simple way to adjust this...
or the report format could change to make it more memory efficient.

### Sorting All the Issues
All issues from all the repositories need to be sorted; this is most easily done keeping them in memory.
If the number of issues is huge, then this type of task would most easily be done using some type of distributed framework like Hadoop. 

## Sample Invocation of the Program:
1. mvn clean install
2. java -cp target/githubissues-1.0-SNAPSHOT-jar-with-dependencies.jar com.github4demo.Main -p 100 -r src/test/resources/sampleRepoList.txt -t <github_token> -u https://api.github.com/graphql