package com.github4demo;


import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by djchuy on 8/21/18.
 */
public abstract class IssueFetcher {

    public abstract Stream<Issue> fetchIssuesForRepo(RepoRegistration registration);

    /**
     *
     * @param registrations repo registrations that we need to look up
     * @return a long stream that represents each repo's issues concatenated all together
     */
    public Stream<Issue> fetchIssuesForRepos(List<RepoRegistration> registrations){
        return registrations.stream().flatMap(new Function<RepoRegistration, Stream<Issue>>() {
            @Override
            public Stream<Issue> apply(RepoRegistration registration) {
                return fetchIssuesForRepo(registration);
            }
        });
    }
}
