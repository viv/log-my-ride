/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.me.viv.logmyride;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author matthewvivian
 */
public class LogMyRide {

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

        MailWatch mail = new MailWatch(
                settings.getProperty("mail.protocol"),
                settings.getProperty("mail.host"),
                settings.getProperty("mail.port"),
                settings.getProperty("mail.username"),
                settings.getProperty("mail.password"));
        mail.getNewEmails();

        Fence fence = new Fence(
                settings.getProperty("fence.top"),
                settings.getProperty("fence.bottom"),
                settings.getProperty("fence.left"),
                settings.getProperty("fence.right"));

        GeoFence geoFence = new GeoFence(fence);

        GitSite site = new GitSite(
                settings.getProperty("github.user"),
                settings.getProperty("github.email"),
                settings.getProperty("github.token"),
                settings.getProperty("github.remoteURL"));
        site.update();
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