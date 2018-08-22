package com.github4demo;

/**
 * Class is pretty straight forward.  It's for holding repo name and owner as well as providing a convenience method for encoding.
 */
public class BasicRepoRegistration implements RepoRegistration{
    private String theRepoOwner;
    private String theRepoName;
    public BasicRepoRegistration(String repoOwner, String repoName){
        theRepoName = repoName;
        theRepoOwner = repoOwner;
    }
    @Override
    public String repoOwner() {
        return theRepoOwner;
    }

    @Override
    public String repoName() {
        return theRepoName;
    }

    @Override
    public String asString() {
        return repoOwner() + "/" + repoName();
    }
}
