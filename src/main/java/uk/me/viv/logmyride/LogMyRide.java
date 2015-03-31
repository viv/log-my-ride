/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.me.viv.logmyride;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author matthewvivian
 */
public class LogMyRide {

    private static final String KMZ_WATCH_DIR = "/tmp";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Starting...");

        // INJECT CONFIG
        //      From a config properties file?
        // GEO FENCE
        //      Detect KMZ
        //      Get KML from KMZ
        //      Geo Filter
        //      Write KML to KMZ
        // YML RIDE INFO
        //      Read ride info from KML
        //      Generate ride YML
        // GIT INTEGRATION
        //      Upload KMZ to github
        //      Append ride YML to file on github

        // While true
        //  check for MotionX mail
        //  if HaveMail
        //      GeoFence ride data
        //      Create ride info
        //      Update website
        //

        Properties settings = LogMyRide.getProperties();

//        MailWatch mail = new MailWatch(
//                settings.getProperty("mail.protocol"),
//                settings.getProperty("mail.host"),
//                settings.getProperty("mail.port"),
//                settings.getProperty("mail.username"),
//                settings.getProperty("mail.password"));
//        mail.getNewEmails();

        Fence fence = new Fence(
                settings.getProperty("fence.top"),
                settings.getProperty("fence.bottom"),
                settings.getProperty("fence.left"),
                settings.getProperty("fence.right"));

        GeoFence geoFence = new GeoFence(fence);

        List<File> filesToFence = LogMyRide.getFilesToFence();

        for (File file : filesToFence) {
            System.out.println("");
            Logger.getLogger(LogMyRide.class.getName()).info("TO FENCE: " + file.getAbsolutePath());

            try {
                final ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) !=  null) {
                    if (entry.getName().toLowerCase().endsWith(KMLFile.EXTENSION)) {
                       Kml kml = geoFence.fence(zis);
                       kml.marshalAsKmz("/Users/matthewvivian/NetBeansProjects/LogMyRide/samples/" + file.getName());
                    }
                }
                zis.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


//        GitSite site = new GitSite(
//                settings.getProperty("github.user"),
//                settings.getProperty("github.email"),
//                settings.getProperty("github.token"),
//                settings.getProperty("github.remoteURL"));
//        site.update();
    }

    private static List<File> getFilesToFence() {
        File dir = new File(KMZ_WATCH_DIR);
        File[] directoryListing = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(KMZFile.EXTENSION);
            }
        });

        List<File> filesToFence = new ArrayList<File>();

        if (directoryListing != null) {
            for (File child : directoryListing) {
                filesToFence.add(child);
            }
        } else {
            Logger.getLogger(LogMyRide.class.getName()).warning("Error reading directory " + KMZ_WATCH_DIR);
        }

        return filesToFence;
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