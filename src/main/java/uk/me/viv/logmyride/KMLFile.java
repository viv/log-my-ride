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

import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matthew Vivian <matthew@viv.me.uk>
 */
public class KMLFile {
    public static final String EXTENSION = "kml";
    private final Kml kml;
    private final String filename;

    KMLFile(String filename, InputStream content) throws IOException {
        this(filename, Kml.unmarshal(content));
    }

    KMLFile(String filename, Kml content) {
        this.filename = filename;
        this.kml = content;
    }

    static boolean isKML(String path) {
        return path.toLowerCase().endsWith(EXTENSION);
    }

    public Kml getKml() {
        return kml;
    }

    public String getFilename() {
        return this.filename;
    }

    public String saveAsKmz(String path) throws IOException {
        Logger.getLogger(KMLFile.class.getName()).log(
                Level.INFO, "SAVING " + getFilename() + " to " + path);
        final String fullPath = path + getFilename();
        this.kml.marshalAsKmz(fullPath);
        return fullPath;
    }
}
