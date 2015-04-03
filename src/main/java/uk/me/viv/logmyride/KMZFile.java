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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Matthew Vivian <matthew@viv.me.uk>
 */
public class KMZFile {
    public static final String EXTENSION = "kmz";

    private final File kmz;

    public KMZFile(File kmz) {
        this.kmz = kmz;
    }

    public KMLFile getFirstKML() {
        KMLFile kml = null;
        try {
            final ZipInputStream zis = new ZipInputStream(new FileInputStream(kmz));
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (KMLFile.isKML(entry.getName())) {
                    kml = new KMLFile(zis);
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

    static boolean isKMZ(Path path) {
        return KMZFile.isKMZ(path.toString());
    }

    static boolean isKMZ(String path) {
        return path.toLowerCase().endsWith(EXTENSION);
    }
}
