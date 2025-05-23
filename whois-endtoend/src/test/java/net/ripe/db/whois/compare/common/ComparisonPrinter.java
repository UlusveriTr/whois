package net.ripe.db.whois.compare.common;

import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import net.ripe.db.whois.common.domain.ResponseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ComparisonPrinter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComparisonPrinter.class);
    private static int filenameSuffix = 1;

    private ComparisonPrinter(){}

    public static void writeDifferences(final File targetDir,
                                        final String query,
                                        final List<ResponseObject> responseObjects1,
                                        final List<ResponseObject> responseObjects2,
                                        final List<AbstractDelta> deltas) throws IOException {

        final String filenameBase = String.format("%d_%%s.txt", filenameSuffix++);

        writeObjects(query, new File(targetDir, String.format(filenameBase, "1")), responseObjects1);
        writeObjects(query, new File(targetDir, String.format(filenameBase, "2")), responseObjects2);
        writeDeltas(query, new File(targetDir, String.format(filenameBase, "DELTA")), deltas);
    }

    private static void writeObjects(final String query, final File file, final List<ResponseObject> result) throws IOException {
        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));) {
            os.write(query.getBytes(StandardCharsets.UTF_8));
            os.write("\n\n".getBytes(StandardCharsets.UTF_8));

            for (final ResponseObject responseObject : result) {
                responseObject.writeTo(os);
                os.write("\n".getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private static void writeDeltas(final String query, final File file, final List<AbstractDelta> deltas) throws IOException {
        LOGGER.info("Creating {}", file.getAbsolutePath());
        try (final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
            os.write(query.getBytes(StandardCharsets.UTF_8));
            os.write("\n\n".getBytes(StandardCharsets.UTF_8));

            for (final AbstractDelta delta : deltas) {
                final Chunk original = delta.getSource();
                final Chunk revised = delta.getTarget();

                os.write(String.format("\n\n" +
                        "---------- 1 (position %d, size %d) ----------\n\n%s\n\n" +
                        "---------- 2 (position %d, size %d) ----------\n\n%s\n\n",
                        original.getPosition(),
                        original.size(),
                        original.getLines(),
                        revised.getPosition(),
                        revised.size(),
                        revised.getLines()
                ).getBytes(StandardCharsets.UTF_8));
            }
        }
    }
}
