
package edu.ucla.library.lambda.kakadu.converter;

import static edu.ucla.library.lambda.kakadu.converter.Constants.MESSAGES;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import info.freelibrary.util.IOUtils;
import info.freelibrary.util.Logger;
import info.freelibrary.util.LoggerFactory;
import info.freelibrary.util.LoggerMarker;

/**
 * The Kakadu TIFF to JP2 image converter.
 */
public class Kakadu {

    public static final String KAKADU_HOME = "KAKADU_HOME";

    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");

    private static final Logger LOGGER = LoggerFactory.getLogger(Kakadu.class, MESSAGES);

    private static final String KAKADU_COMMAND = "kdu_compress";

    /** The JPEG 2000 extension */
    private static final String JP2_EXT = ".jpx";

    private static final List<String> BASE_OPTIONS = Arrays.asList("Clevels=6", "Clayers=6",
            "Cprecincts={256,256},{256,256},{128,128}", "Stiles={512,512}", "Corder=RPCL", "ORGgen_plt=yes",
            "ORGtparts=R", "Cblk={64,64}", "Cuse_sop=yes", "Cuse_eph=yes", "-flush_period", "1024");

    private static final String LOSSLESS_RATE = "-";

    @SuppressWarnings({ "checkstyle:multiplestringliterals" })
    private static final List<String> LOSSLESS_OPTIONS = Arrays.asList("Creversible=yes", "-rate", LOSSLESS_RATE);

    @SuppressWarnings({ "checkstyle:multiplestringliterals" })
    private static final List<String> LOSSY_OPTIONS = Arrays.asList("-rate");

    // private static final List<String> ALPHA_OPTION = Arrays.asList("-jp2_alpha");

    /**
     * Converts a source image to JP2.
     *
     * @param aID An ID for the image to be converted
     * @param aTIFF A TIFF image to be converted
     * @param aCompressionRate A type of conversion (e.g. lossy, lossless)
     * @return The JP2 file
     * @throws IOException If there is trouble reading the source image or writing the JP2
     * @throws InterruptedException If the process gets interrupted
     * @throws IllegalArgumentException If the compression rate isn't dash or a float
     */
    public File convert(final String aID, final File aTIFF, final String aCompressionRate) throws IOException,
            InterruptedException, IllegalArgumentException {
        final String id = aID.contains("/") ? URLEncoder.encode(aID, StandardCharsets.UTF_8.toString()) : aID;
        final File jp2 = new File(TMP_DIR, id + JP2_EXT);
        final List<String> command = new ArrayList<>();

        command.addAll(Arrays.asList(KAKADU_COMMAND, "-i", getPath(aTIFF), "-o", getPath(jp2)));
        command.addAll(BASE_OPTIONS);

        if (aCompressionRate.equals(LOSSLESS_RATE)) {
            command.addAll(LOSSLESS_OPTIONS);
        } else {
            final List<String> options = new ArrayList<>(LOSSY_OPTIONS);

            // Confirm our lossy compression rate value is a positive float
            try {
                if (Float.parseFloat(aCompressionRate) > 0F) {
                    options.add(aCompressionRate);
                } else {
                    throw new IllegalArgumentException(LOGGER.getMessage(MessageCodes.LKC_119, aCompressionRate));
                }
            } catch (final NumberFormatException details) {
                throw new IllegalArgumentException(LOGGER.getMessage(MessageCodes.LKC_119, aCompressionRate));
            }

            command.addAll(options);
        }

        // Run the TIFF -> JP2 conversion
        run(new ProcessBuilder(command), aID, LOGGER);

        // If conversion was successful, return the resulting JP2 file
        return jp2;
    }

    /**
     * Checks whether kakadu is available on the system and in the system $PATH.
     *
     * @return True if kakadu is found; else, false
     */
    public boolean isInstalled() {
        try {
            return Runtime.getRuntime().exec("kdu_compress -v").waitFor() == 0;
        } catch (final InterruptedException | IOException details) {
            LOGGER.error(details, details.getMessage());
            return false;
        }
    }

    /**
     * Gets the absolute path of the supplied file.
     *
     * @param aFile An image file
     * @return The path of the supplied image file
     * @throws IOException If there is trouble checking the file
     */
    public String getPath(final File aFile) throws IOException {
        if (!aFile.exists() && !aFile.getParentFile().canWrite()) {
            throw new IOException(LOGGER.getMessage(MessageCodes.LKC_007, aFile));
        }

        return aFile.getAbsolutePath();
    }

    /**
     * Run the conversion process.
     *
     * @param aProcessBuilder A process builder that has the process to run
     * @param aID The ID for the image that's being converted
     * @throws IOException If the process has trouble reading or writing
     * @throws InterruptedException If the process has been interrupted
     */
    private void run(final ProcessBuilder aProcessBuilder, final String aID, final Logger aLogger) throws IOException,
            InterruptedException {
        final Marker marker = MarkerFactory.getMarker(LoggerMarker.EOL_TO_SPACE);
        final Process process = aProcessBuilder.start();

        if (process.waitFor() != 0) {
            aLogger.error(marker, MessageCodes.LKC_112, getMessage(process.getErrorStream()));
            throw new IOException(aLogger.getMessage(MessageCodes.LKC_006, aID));
        } else if (aLogger.isDebugEnabled()) {
            aLogger.debug(marker, MessageCodes.LKC_112, getMessage(process.getInputStream()));
        }
    }

    /**
     * A convenience method to simplify String reading.
     *
     * @param aInputStream A buffered reader from which to read
     * @return The string that was read
     * @throws IOException If there is trouble reading from the supplied reader
     */
    private String getMessage(final InputStream aInputStream) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(aInputStream));
        final StringBuilder buffer = new StringBuilder();

        // Read in one line at a time, adding EOL characters for readability
        String line;

        while ((line = reader.readLine()) != null) {
            buffer.append(line).append(System.lineSeparator());
        }

        // Clean up the reader without caring if it throws exceptions on close
        IOUtils.closeQuietly(reader);

        return buffer.toString();
    }
}
