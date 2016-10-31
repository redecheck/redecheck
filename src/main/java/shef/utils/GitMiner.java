package shef.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;

/**
 * Created by thomaswalsh on 31/05/2016.
 */
public class GitMiner {
    static String dataDirectory = "/Users/thomaswalsh/Documents/PhD/redecheck/cloned-repos/";
    String[] keywords = new String[] {"responsive", "fault", "layout", "error", "bug"};
    public GitMiner() {
        mine("https://github.com/twbs/bootstrap");
    }

    private void mine(String filePath) {
        System.out.println("----- MINING -----");
        Git git = null;
        try {
            String[] splits = filePath.split(".com/");
            String repoName = splits[splits.length-1];
            try {
                cloneGitRepository(filePath, git, repoName);
            } catch (JGitInternalException e ) {
                System.out.println("Repository already cloned");
            }

            File gitWorkDir = new File(dataDirectory + repoName);
            git = Git.open(gitWorkDir);
            Repository repository = git.getRepository();

            Iterable<RevCommit> logs = git.log().all().call();
            int count = 0;
            for (RevCommit rev : logs) {
                int numCont = messageContainsKeywords(rev.getFullMessage());
                if (numCont > 1) {
                    System.out.println(rev.getShortMessage());
                    System.out.println(numCont + " " + rev.getId());
                    System.out.println();
                    count++;
                }
            }
            System.out.println("Had " + count + " commits overall on current branch");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException apiE) {
            apiE.printStackTrace();
        } catch (NullPointerException npe) {
            System.out.println("Failed when trying to retrieve logs");
        }
    }

    private int messageContainsKeywords(String fullMessage) {
        int count = 0;
        for (String s : keywords) {
            if (fullMessage.toLowerCase().contains(s)) {
                count++;
            }
        }
        return count;
    }

    public void cloneGitRepository(String filePath, Git git, String repoName) {
        System.out.println("----- CLONING -----");
        try {
            git = Git.cloneRepository().setURI(filePath).setDirectory(new File(dataDirectory+repoName)).call();
            System.out.println(git);
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

    }

}
