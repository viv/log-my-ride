package uk.me.viv.logmyride;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

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
    private static final String TMP_GIT_DIR = "/tmp/tmpGitDir";

    private static final Logger LOGGER = Logger.getLogger(GitSite.class.getName());

    private final String user;
    private final String email;
    private final String token;
    private final String remoteURL;
    private final Git git;

    public GitSite(String user, String email, String token, String remoteURL) throws IOException {
        this.user = user;
        this.email = email;
        this.token = token;
        this.remoteURL = remoteURL;

        tmpCloneDir = new File(TMP_GIT_DIR);
        try {
            this.git = cloneToLocalFilesystem();
        } catch (GitAPIException ex) {
            throw new IIOException("Failed to clone remote repository", ex);
        }
    }

    public void update() {
        try {
            LOGGER.info("Creating file");
            File targetFile = new File(git.getRepository().getDirectory().getParent(), "newfile");
            InputStream contents = IOUtils.toInputStream("Sample file", "UTF-8");
            FileUtils.copyInputStreamToFile(contents, targetFile);

            add(targetFile);
            commit("Added testfile");
            push();
            close();

        }   catch (GitAPIException | IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
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

    public void add(File targetFile) throws IOException, GitAPIException {
        LOGGER.info("Adding file " + targetFile.getPath());
        git.add()
                .addFilepattern(targetFile.getName())
                .call();
    }

    private Git cloneToLocalFilesystem() throws IOException, GitAPIException {
        LOGGER.info("Preparing destination folder for cloned repository");
        LOGGER.info("Cloning " + this.remoteURL + " into " + this.tmpCloneDir);
        Git result = Git.cloneRepository()
                .setURI(this.remoteURL)
                .setDirectory(this.tmpCloneDir)
                .call();
        result.close();
        Repository repository = result.getRepository();
        Git git = new Git(repository);
        return git;
    }
    private final File tmpCloneDir;
}