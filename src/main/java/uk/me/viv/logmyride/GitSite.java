package uk.me.viv.logmyride;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;


public class GitSite {

    private static final String BLANK_PASSWORD = "";

    private static final Logger LOGGER = Logger.getLogger(GitSite.class.getName());

    private final String user;
    private final String email;
    private final String token;
    private final String remoteURL;

    public GitSite(String user, String email, String token, String remoteURL) {
        this.user = user;
        this.email = email;
        this.token = token;
        this.remoteURL = remoteURL;
    }

    public void update() {
        try {
            LOGGER.info("Preparing destination folder for cloned repository");
            File localPath = File.createTempFile("TestGitRepository", "");
            localPath.delete();

            LOGGER.info("Cloning " + this.remoteURL + " into " + localPath);
            Git result = Git.cloneRepository()
                    .setURI(this.remoteURL)
                    .setDirectory(localPath)
                    .call();
            result.close();

            Repository repository = result.getRepository();
            Git git = new Git(repository);

            LOGGER.info("Creating test file");
            File myfile = new File(repository.getDirectory().getParent(), "testfile");
            myfile.createNewFile();

            LOGGER.info("Adding test file");
            git.add()
                    .addFilepattern("testfile")
                    .call();

            git.commit()
                    .setCommitter(this.user, this.email)
                    .setMessage("Added testfile")
                    .call();

            LOGGER.info("Committed file " + myfile + " to repository at " + repository.getDirectory());

            LOGGER.info("Pushing to remote");
            CredentialsProvider cp = new UsernamePasswordCredentialsProvider(this.token, BLANK_PASSWORD);            
            PushCommand pc = git.push();
            pc.setCredentialsProvider(cp)
                    .setPushAll();
            try {
                Iterator<PushResult> it = pc.call().iterator();
                if(it.hasNext()){
                    PushResult pr = it.next();                    
                    for (RemoteRefUpdate ru : pr.getRemoteUpdates() ) {
                        LOGGER.info("Push Status: " + ru.getStatus());
                        LOGGER.info("Updated: " + ru.getTrackingRefUpdate().getLocalName());
                    }
                }
            } catch (InvalidRemoteException e) {
                e.printStackTrace();
            }

            repository.close();
            LOGGER.info("Removing local repository");
            FileUtils.deleteDirectory(localPath);

        }   catch (GitAPIException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
}