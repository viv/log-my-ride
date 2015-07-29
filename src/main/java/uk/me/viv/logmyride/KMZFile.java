/*
 * The MIT License
 *
 * Copyright 2015 Matthew Vivian <matthew@viv.me.uk>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package uk.me.viv.logmyride;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Matthew Vivian <matthew@viv.me.uk>
 */
public class KMZFile {
    public static final String EXTENSION = "kmz";

    private final InputStream kmz;
    private final String filename;

    public KMZFile(InputStream kmz, String filename) {
        this.kmz = kmz;
        this.filename = normalise(filename);
    }

    public KMLFile getFirstKML() {
        KMLFile kml = null;
        try {
            final ZipInputStream zis = new ZipInputStream(kmz);
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (KMLFile.isKML(entry.getName())) {
                    kml = new KMLFile(getFilename(), fixNamespace(IOUtils.toString(zis)));
                    break;
                }
            }
            zis.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LogMyRide.class.getName()).log(Level.SEVERE, null, ex);
        }
        return kml;
    }

    public String getFilename() {
        return this.filename;
    }

    private String normalise(String filename) {
        String normalised = filename.replaceAll(" +", "-").toLowerCase();
        System.out.println("Renaming " + filename + " to " + normalised);
        return normalised;
    }

    private String fixNamespace(String kml) throws IOException, UnsupportedEncodingException {
        return StringUtils.replace(kml, "xmlns=\"http://earth.google.com/kml/2.2\"", "xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\"" );
    }

    static boolean isKMZ(Path path) {
        return KMZFile.isKMZ(path.toString());
    }

    static boolean isKMZ(String path) {
        return path.toLowerCase().endsWith(EXTENSION);
    }

    /**
     * Close the KMZ InputStream
     */
    public void close() {
        try {
            this.kmz.close();
        } catch (IOException ex) {
            Logger.getLogger(KMZFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {
        return this.getFilename();
    }
}
