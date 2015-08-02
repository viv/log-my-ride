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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Matthew Vivian <matthew@viv.me.uk>
 */
public class YamlTest {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        testRead();
        testWrite();
    }

    private static void testRead() {
        InputStream input = YamlTest.class.getResourceAsStream("/Rides.yml");
        Yaml yaml = new Yaml();
        List<Map<String, String>> object = (List<Map<String, String>>) yaml.load(input);

        System.out.println("RAW AFTER READ");
        System.out.println(object);

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yaml = new Yaml(options);
        String output = yaml.dump(object);
        System.out.println("DUMPED AFTER READ");
        System.out.println(output);
    }

    private static void testWrite() {
        List<Map> list = new ArrayList();
        HashMap<String, String> mMap = new LinkedHashMap<>();
        mMap.put("name", "Today");
        mMap.put("date","0845");
        mMap.put("distance","0845");
        mMap.put("duration","0845");
        mMap.put("averageSpeed","0845");
        mMap.put("maximumSpeed","0845");
        mMap.put("averagePace","0845");
        mMap.put("minimumAltitude","0845");
        mMap.put("maximumAltitude","0845");
        mMap.put("startTime","0845");
        mMap.put("finishTime","0845");
        mMap.put("filename","0845");
        mMap.put("id","0845");
        list.add(mMap);
        mMap = new HashMap<>();
        mMap.put("Date", "Yesterday");
        mMap.put("Start Time","0945");
        list.add(mMap);

        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml yaml = new Yaml(options);
        String output = yaml.dump(list);
        System.out.println("DUMPED AFTER CREATION");
        System.out.println(output);
    }
}
