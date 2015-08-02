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
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Matthew Vivian <matthew@viv.me.uk>
 */
public class KMLFile {
    public static final String EXTENSION = "kml";
    private final String kml;
    private final String filename;

    KMLFile(String filename, String content) {
        this.filename = filename;
        this.kml = content;
    }

    static boolean isKML(String path) {
        return path.toLowerCase().endsWith(EXTENSION);
    }

    public String getFilename() {
        return this.filename;
    }

    /**
     * @todo push this down into a MotionX specific KML class
     * @return
     */
    public String getDescription() {

        String description = "";

        String[] descriptionProperties = {
            "Date",
            "Distance",
            "Elapsed Time",
            "Avg Speed",
            "Max Speed",
            "Avg Pace",
            "Min Altitude",
            "Max Altitude",
            "Start Time",
            "Finish Time",
        };

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(this.kml));
            try {
                Document doc = builder.parse(is);
                XPathFactory xPathfactory = XPathFactory.newInstance();
                XPath xpath = xPathfactory.newXPath();

                for (String property : descriptionProperties) {
                    description += property + " = " + xpath.evaluate("//td[text()=\"" + property + ":\"]/following::td[1]/text()", doc) + "\n";
                }
            } catch (SAXException ex) {
                Logger.getLogger(KMLFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(KMLFile.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(KMLFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(KMLFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        return description;
    }

    public String saveAsKmz(String path) throws IOException {
        Logger.getLogger(KMLFile.class.getName()).log(
                Level.INFO, "SAVING " + getFilename() + " to " + path);
        final String fullPath = path + getFilename();
        Kml toSave = Kml.unmarshal(this.kml);
        toSave.marshalAsKmz(fullPath);
        // TODO - verify file is fully written before returning
        return fullPath;
    }

    @Override
    public String toString() {
        return this.kml;
    }
}
