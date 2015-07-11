package uk.me.viv.logmyride;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;


public class GitSite {

    private static final String BLANK_PASSWORD = "";
    private static final Logger LOGGER = Logger.getLogger(GitSite.class.getName());

    private final String user;
    private final String email;
    private final String token;
    private final String remoteURL;
    private final File tmpCloneDir;
    private final Git git;

    public GitSite(String user, String email, String token, String remoteURL, String cloneToDir) throws IOException {
        this.user = user;
        this.email = email;
        this.token = token;
        this.remoteURL = remoteURL;
        this.tmpCloneDir = new File(cloneToDir);

        try {
            this.git = cloneToLocalFilesystem();
        } catch (GitAPIException ex) {
            throw new IOException("Failed to clone remote repository", ex);
        }
    }

    public void close() throws IOException {
        git.close();
        LOGGER.info("Removing local repository");
        FileUtils.deleteDirectory(this.tmpCloneDir);
    }

    public void push() throws GitAPIException {
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
    }

    public void commit(String message) throws GitAPIException {
        git.commit()
                .setCommitter(this.user, this.email)
                .setMessage(message)
                .call();
        LOGGER.info("Committed to repository at " + git.getRepository().getDirectory());
    }

    public void add(String fileName) throws IOException, GitAPIException {
        LOGGER.info("Adding file " + fileName);
        git.add()
                .addFilepattern(fileName)
                .call();
    }

    private Git cloneToLocalFilesystem() throws IOException, GitAPIException {
        LOGGER.info("Preparing destination folder for cloned repository");

        Git git;

        File existingRepo = RepositoryCache.FileKey.resolve(this.tmpCloneDir, FS.DETECTED);
        if (existingRepo == null) {
            LOGGER.info("Cloning " + this.remoteURL + " into " + this.tmpCloneDir);
            Git result = Git.cloneRepository()
                    .setURI(this.remoteURL)
                    .setDirectory(this.tmpCloneDir)
                    .call();
            result.close();
            Repository repository = result.getRepository();
            git = new Git(repository);
        } else {
            LOGGER.info("Already cloned, using existing repository.");
            git = Git.open(tmpCloneDir);
        }

        return git;
    }
}