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
import java.util.Iterator;
import java.util.List;


public class GeoFence {

    private final Fence fence;

    public GeoFence(Fence fence) {
        this.fence = fence;
    }

    public KMLFile fence(KMLFile toFence) {

        Kml kml = toFence.getKml();
        final Document doc = (Document) kml.getFeature();

        final List<Feature> placemarks = doc.getFeature();
        for (Feature placemark : placemarks) {
            Placemark p = (Placemark) placemark;
            System.out.println("Fencing " + p.getName());
            Geometry geometry = p.getGeometry();
            if (geometry instanceof MultiGeometry) {
                List<Geometry> geometries = ((MultiGeometry)geometry).getGeometry();
                final Geometry firstGeometry = geometries.get(0);
                if (firstGeometry instanceof LineString) {
                    LineString ls = (LineString) firstGeometry;
                    ls.setCoordinates(removeFencedCoordinates(ls.getCoordinates()));
                } else {
                    System.out.println("First geometry is not a LineString: " + firstGeometry.getClass());
                }
            } else if (geometry instanceof Point) {
                Point point = (Point) geometry;
                point.setCoordinates(removeFencedCoordinates(point.getCoordinates()));
            } else {
                System.out.println("Unable to handle Geometry of type: " + geometry.getClass());
            }
        }
        return new KMLFile(toFence.getFilename(), kml);
    }

    private boolean isInsideFence(Coordinate coordinate) {
        return coordinate.getLongitude() < this.fence.getRight()
                && coordinate.getLongitude() > this.fence.getLeft()
                && coordinate.getLatitude() < this.fence.getTop()
                && coordinate.getLatitude() > this.fence.getBottom();
    }

    private List<Coordinate> removeFencedCoordinates(List<Coordinate> coordinates) {
        Iterator<Coordinate> iterator = coordinates.iterator();
        while (iterator.hasNext()) {
            Coordinate coordinate = iterator.next();

            if (isInsideFence(coordinate)) {
                System.out.println(" - Removing coordinate "
                        + coordinate.getLongitude()
                        + ", " + coordinate.getLatitude());
                iterator.remove();
            }
        }

        return coordinates;
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
