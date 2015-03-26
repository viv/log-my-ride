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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


public class GeoFence {

    public GeoFence() {
        fence();
    }

    public void fence() {
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("LlandowLoop.kmz");
            final ZipInputStream zis = new ZipInputStream(is);
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) !=  null) {
                if (entry.getName().toLowerCase().endsWith(".kml")) {
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

    private void parseKML(InputStream kmlStream) throws FileNotFoundException, IOException {
        InputStream fixedKMLStream = fixNamespace(kmlStream);

        final Kml kml = Kml.unmarshal(fixedKMLStream);
        kml.marshal(System.out);
        
        final Document doc = (Document) kml.getFeature();
        
        final List<Feature> placemarks = doc.getFeature();
        for (Feature placemark : placemarks) {
            Placemark p = (Placemark) placemark;
            System.out.println(p.getName());
            Geometry geometry = p.getGeometry();
            if (geometry instanceof MultiGeometry) {
                List<Geometry> geometries = ((MultiGeometry)geometry).getGeometry();
                LineString ls = (LineString) geometries.get(0);
                List<Coordinate> coordinates = ls.getCoordinates();
                for (Coordinate coordinate : coordinates) {
                    System.out.println(coordinate.getLatitude());
                    System.out.println(coordinate.getLongitude());
                    System.out.println(coordinate.getAltitude());
                }
            } else {
                List<Coordinate> coordinates = ((Point)geometry).getCoordinates();
                for (Coordinate coordinate : coordinates) {
                    System.out.println(coordinate.getLatitude());
                    System.out.println(coordinate.getLongitude());
                    System.out.println(coordinate.getAltitude());
                }
            }
        }
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
