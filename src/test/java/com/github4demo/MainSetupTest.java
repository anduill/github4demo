package com.github4demo;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by djchuy on 8/22/18.
 */
public class MainSetupTest {
    @Test
    public void testGrabbingReoLinesFromFile() throws IOException {
        List<String> expectedTokens = new ArrayList<>();
        expectedTokens.add("vuejs/vue");
        expectedTokens.add("daneden/animate.css");
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sampleRepoList.txt");
        List<String> actualTokens = Main.repoLinesFromInputStream(is);
        Assert.assertEquals(expectedTokens, actualTokens);
    }
    @Test
    public void testCreatingReposFromStrings() throws IOException {
        List<RepoRegistration> registrations = new ArrayList<>();
        registrations.add(new BasicRepoRegistration("vuejs", "vue"));
        registrations.add(new BasicRepoRegistration("daneden", "animate.css"));
        List<String> expected = registrations.stream().map((registration -> {return registration.asString();})).collect(Collectors.toList());
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("sampleRepoList.txt");
        List<String> actual = Main.repoRegsFromStrings(Main.repoLinesFromInputStream(is)).stream().map((registration -> {return registration.asString();})).collect(Collectors.toList());
        Assert.assertEquals(expected, actual);
    }
}
