package uk.me.viv.logmyride;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


public class GeoFence {

    private static final String KMZ_WATCH_DIR = "/tmp";
    private final Fence fence;

    public GeoFence(Fence fence) {
        this.fence = fence;
        fence();
    }

    public void fence() {
        List<File> filesToFence = this.getFilesToFence();

        for (File file : filesToFence) {
            Logger.getLogger(LogMyRide.class.getName()).info("TO FENCE: " + file.getAbsolutePath());

            try {
                final ZipInputStream zis = new ZipInputStream(new FileInputStream(file));

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) !=  null) {
                    if (entry.getName().toLowerCase().endsWith(KMLFile.EXTENSION)) {
                       parseKML(zis);
                    }
                }
                zis.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private List<File> getFilesToFence() {
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


    private void parseKML(InputStream kmlStream) throws FileNotFoundException, IOException {
        InputStream fixedKMLStream = fixNamespace(kmlStream);

        final Kml kml = Kml.unmarshal(fixedKMLStream);
//        kml.marshal(System.out);

        final Document doc = (Document) kml.getFeature();

        final List<Feature> placemarks = doc.getFeature();
        for (Feature placemark : placemarks) {
            Placemark p = (Placemark) placemark;

            Geometry geometry = p.getGeometry();
            if (geometry instanceof MultiGeometry) {
                System.out.println("MultiGeometry: " + p.getName());

                List<Geometry> geometries = ((MultiGeometry)geometry).getGeometry();
                LineString ls = (LineString) geometries.get(0);
                List<Coordinate> coordinates = ls.getCoordinates();
                for (Coordinate coordinate : coordinates) {
//                    System.out.println(coordinate.getLatitude());
//                    System.out.println(coordinate.getLongitude());
//                    System.out.println(coordinate.getAltitude());
                }
            } else {
                List<Coordinate> coordinates = ((Point)geometry).getCoordinates();
                for (Coordinate coordinate : coordinates) {


                    if (isInsideFence(coordinate)) {
                        System.out.println("Removing node: " + p.getName());
                        System.out.println(" ------- " + coordinate.getLongitude());
                        System.out.println(" ------- " + coordinate.getLatitude());
                    } else {
                        System.out.println("Leave node as is: " + p.getName());
                        System.out.println(" ------- " + coordinate.getLongitude());
                        System.out.println(" ------- " + coordinate.getLatitude());
                    }
                }
            }
        }
    }

    private boolean isInsideFence(Coordinate coordinate) {
        return coordinate.getLongitude() < this.fence.getRight()
                && coordinate.getLongitude() > this.fence.getLeft()
                && coordinate.getLatitude() < this.fence.getTop()
                && coordinate.getLatitude() > this.fence.getBottom();
    }

    private InputStream fixNamespace(InputStream kmlStream) throws IOException, UnsupportedEncodingException {
        String str = IOUtils.toString( kmlStream );
        str = StringUtils.replace( str, "xmlns=\"http://earth.google.com/kml/2.2\"", "xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"" );
        ByteArrayInputStream fixedKMLStream = new ByteArrayInputStream( str.getBytes( "UTF-8" ) );
        return fixedKMLStream;
    }

//    private void unmarshalFromKMZ() {
//        final Kml[] kml;
//        try {
//            kml = Kml.unmarshalFromKmz(new File(Thread.currentThread().getContextClassLoader().getResource("LlandowLoop.kmz").toURI()));
//            System.out.println(Arrays.toString(kml));
//        } catch (URISyntaxException ex) {
//            Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

//    private void writeKML() {
//            // Write KML to temp file
//            zis.getNextEntry();
//
//            try {
//                File outFile = File.createTempFile("kmltest", ".kml");
//                System.out.format("The temporary file has been created: %s%n", outFile);
//                OutputStream outputStream = new FileOutputStream(outFile);
//                IOUtils.copy(zis, outputStream);
//                outputStream.close();
//
//                final Kml kml = Kml.unmarshal(outFile, false);
//                System.out.println(kml.toString());
//
//            } catch (IOException x) {
//                System.err.format("IOException: %s%n", x);
//            }
//    }
}
