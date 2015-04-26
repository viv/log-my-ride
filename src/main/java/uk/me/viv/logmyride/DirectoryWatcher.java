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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Matthew Vivian <matthew@viv.me.uk>
 */
public class DirectoryWatcher implements Runnable {

    private Path watched;
    private final BlockingQueue queue;

    public DirectoryWatcher(String watchDir, BlockingQueue queue) {
        this.watched = Paths.get(watchDir);
        this.queue = queue;
    }

    /**
     *
     * Created with help from http://andreinc.net/2013/12/06/java-7-nio-2-tutorial-writing-a-simple-filefolder-monitor-using-the-watch-service-api/
     */
    @Override
    public void run() {
        if (!watched.toFile().isDirectory()) {
            throw new IllegalArgumentException("Path: " + watched + " is not a folder");
        }

        System.out.println("Watching path: " + watched);

        FileSystem fs = watched.getFileSystem();

        try (WatchService service = fs.newWatchService()) {

            watched.register(service, ENTRY_CREATE);

            WatchKey key = null;
            while (true) {
                key = service.take();

                Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    kind = watchEvent.kind();
                    if (OVERFLOW == kind) {
                        continue;
                    } else if (ENTRY_CREATE == kind) {
                        Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        System.out.println("New path created: " + watched.resolve(newPath));
                        if (KMZFile.isKMZ(newPath)) {
                            addToQueue(newPath);
                        }
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }
        } catch (IOException | InterruptedException ioe) {
            ioe.printStackTrace();
        }
    }

    private void addToQueue(Path newPath) throws IOException {
        InputStream is = Files.newInputStream(watched.resolve(newPath));
        KMZFile kmz = new KMZFile(is, newPath.getFileName().toString());
        this.queue.add(kmz);
    }
}