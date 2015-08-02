/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.me.viv.logmyride;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 *
 * @author matthewvivian
 */
public class LogMyRide {

    private static final String OUTPUT_DIR = "/Users/matthewvivian/NetBeansProjects/LogMyRide/output/";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting " + LogMyRide.class.getName());

        // YML RIDE INFO
        //      Read ride info from KML
        //      Generate ride YML

        Properties settings = LogMyRide.getProperties();

        LinkedBlockingQueue<KMZFile> kmzQueue = new LinkedBlockingQueue<>();

        // TODO - can this be refactored into DirectoryWatcher.start()
        startDirectoryWatcher(settings.getProperty("processor.tmp.dir"), kmzQueue);

        System.out.println("Starting mail watcher");
        MailWatch mailCheck = new MailWatch(
                settings.getProperty("mail.protocol"),
                settings.getProperty("mail.host"),
                settings.getProperty("mail.port"),
                settings.getProperty("mail.username"),
                settings.getProperty("mail.password"),
                kmzQueue);

        // TODO - can these 2 be pushed into a MailWatch.start() method
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(mailCheck, 0, 10, MINUTES);

        // TODO - can I just pass in settings.getProperty("fence")?
        Fence fence = new Fence(
                settings.getProperty("fence.top"),
                settings.getProperty("fence.bottom"),
                settings.getProperty("fence.left"),
                settings.getProperty("fence.right"));

        GeoFence geoFence = new GeoFence(fence);

        try {
            GitSite site = new GitSite(
                    settings.getProperty("github.user"),
                    settings.getProperty("github.email"),
                    settings.getProperty("github.token"),
                    settings.getProperty("github.remoteURL"),
                    OUTPUT_DIR
            );

            int queueSize = 0;
            while (true) {
                if (queueSize != kmzQueue.size()) {
                    queueSize = kmzQueue.size();
                    System.out.println("Queue Size in now: " + queueSize);
                }

                KMZFile kmz = kmzQueue.take();
                Logger.getLogger(LogMyRide.class.getName()).log(Level.INFO, "TAKEN {0} FROM QUEUE", kmz.getFilename());

                KMLFile kml = kmz.getFirstKML();
                System.out.println(kml.getDescription());

                try {
                    kml = geoFence.fence(kml);
                    final String fencedKMZ = kml.saveAsKmz(OUTPUT_DIR);
                    Thread.sleep(100); // Give file time to save fully before adding to git
                    site.add(kml.getFilename());
                    site.commit("Added new ride: " + kml.getFilename());
                    site.push();
                    kmz.close();
                } catch (IOException ex) {
                    Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, "Failed to save fenced file", ex);
                } catch (GitAPIException ex) {
                    Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, "Failed to add fenced file to git", ex);
                }
            }
        } catch (IOException ex) { // GitSite construction
            Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, "Failed to initiate Git Site", ex);
        } finally {
        }

        // TODO - can this be pushed into a MailWatch.stop() method
        scheduledExecutorService.shutdown();
    }

    private static void startDirectoryWatcher(final String watchDirectory, BlockingQueue<KMZFile> kmzQueue) {
        System.out.println("Starting directory watcher");
        DirectoryWatcher watcher = new DirectoryWatcher(watchDirectory, kmzQueue);
        Thread directoryWatcher = new Thread(watcher);
        directoryWatcher.setDaemon(true); // important, otherwise JVM does not exit at end of main()
        directoryWatcher.setName("DirectoryWatcher");
        directoryWatcher.start();
    }

    private static Properties getProperties() {

    	Properties prop = new Properties();
    	InputStream input = null;

    	try {
            String filename = "LogMyRide.properties";
            input = LogMyRide.class.getClassLoader().getResourceAsStream(filename);
            if(input==null){
                System.out.println("Sorry, unable to find " + filename);
                return prop;
            }

            prop.load(input);

    	} catch (IOException ex) {
            ex.printStackTrace();
        } finally{
            if(input!=null){
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }
}