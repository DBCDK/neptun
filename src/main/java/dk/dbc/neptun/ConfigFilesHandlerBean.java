package dk.dbc.neptun;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Stateless
public class ConfigFilesHandlerBean {
    private static final String CONFIG_DIR = System.getenv().getOrDefault("CONFIG_DIR", "CONFIG_DIR environment variable not set");
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFilesHandlerBean.class);

    protected File confDir;

    @PostConstruct
    public void init() {
        // to facilitate testing, the config directory is instantiated here
        confDir = new File(CONFIG_DIR);
    }

    /**
     * Finds a zip file corresponding to the supplied dbckat version.
     * <p>
     * Returns the closest match between version and filename so that
     * filename corresponds to [0-9]+\.zip and does not resolve to a number
     * greater than version (i.e. version 13 does not get 14.zip).
     *
     * @param targetVersion version of dbckat
     * @return zip of config files
     * @throws ConfigFilesHandlerException on error when matching version
     *                                     to a filename
     */
    public File getConfigFiles(int targetVersion) throws ConfigFilesHandlerException {
        LOGGER.info("Asking for {}", targetVersion);
        final File[] files = confDir.listFiles();
        if (files != null) {
            for (File file : files) {
                LOGGER.info("File {}", file.getName());
            }
            // Go through all files and check if they are zips, for those get the number - other return -1
            final int[] versions = Stream.of(files).mapToInt(file -> {
                int extPos = file.getName().lastIndexOf(".zip");
                if (extPos == -1) return -1;
                try {
                    return Integer.parseInt(file.getName().substring(0, extPos));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }).toArray();
            for (int version : versions) {
                LOGGER.info("Found version {}", version);
            }
            // Check if there are any non -1 occurrences
            if (IntStream.of(versions).noneMatch(i -> i >= 0)) {
                throw new ConfigFilesHandlerException(String.format(
                        "no zip files found in %s", confDir.getAbsolutePath()));
            }
            int match = getClosestMatchIndex(targetVersion, versions);
            // Hmm, this is kind of impossible
            if (match >= files.length) {
                throw new ConfigFilesHandlerException("index of matched file " +
                        "exceeds length of file list");
            }
            LOGGER.info("Got match {} name {}", match, files[match].getName());
            return files[match];
        } else {
            throw new ConfigFilesHandlerException(String.format(
                    "couldn't list files in %s", confDir.getAbsolutePath()));
        }
    }

    /*
     * Initialize with the abs value of target - value of first, then step through the rest of the versions list.
     * If target is less than the list, continue.
     * If not, then make a new abs diff - if it's less, then save it as diff and remember where.
     * Return the index which contains a value closest to target.
     */
    protected int getClosestMatchIndex(int target, int[] versions) {
        int index = 0;
        int diff = Math.abs(target - versions[0]);
        LOGGER.info("Target {} version {} diff {}", target, versions[0], diff);
        for (int i = 1; i < versions.length; i++) {
            LOGGER.info("Target {} index {} version {}", target, i, versions[i]);
            if (target < versions[i]) continue;
            int newDiff = Math.abs(target - versions[i]);
            LOGGER.info("Target {} index {} newDiff {} currentDiff {}", target, i, newDiff, diff);
            if (newDiff < diff) {
                diff = newDiff;
                index = i;
            }
        }
        return index;
    }
}
