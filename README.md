
Gah!!  Ok, I'm an idiot.  I seemed to pass over this comment (in the Main Method) for whatever reason.  This program is
designed to take the repo lists in a line delimited file.  I would chalk this up to a misunderstanding of the
requirements and I should have asked more questions.  I would iterate to fix this interface.

@param args String array with Github repositories with the format
"owner/repository"
  
However, the above comments not withstanding, credentials for the api need to somehow be injected into the program.
Hard coding these is obviously not the right choice.  So, I would question the stated interface and try to
nail it down (unless configuraiton is supposed to come in some other way (i.e. Spring, env-vars, typeconfig...etc)).

Performance Discussion:
There are definitely some performance issues with this implementation- -Specifically memory.  The output of the program
is a single Json-object report (i.e. not line delimited strings).  Most default behavior of existing JSON libraries retains the
entire object in memory.  So, since we are putting ALL the issues from ALL the repos into a single report, all the
respective data would be in memory by default.  I'm sure with enough investigation, I could find a simple way to adjust this...
or the report format could change to make it more memory efficient.


Sample invocation of the program:
1. mvn clean install
2. java -cp target/githubissues-1.0-SNAPSHOT-jar-with-dependencies.jar com.github4demo.Main -p 100 -r src/test/resources/sampleRepoList.txt -t <github_token> -u https://api.github.com/graphql