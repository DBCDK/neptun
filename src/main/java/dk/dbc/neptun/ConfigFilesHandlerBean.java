/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.neptun;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import java.io.File;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Stateless
public class ConfigFilesHandlerBean {
    private String CONFIG_DIR = System.getenv().getOrDefault("CONFIG_DIR", "CONFIG_DIR environment variable not set");

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
     * greater than version (ie. version 13 does not get 14.zip).
     *
     * @param targetVersion version of dbckat
     * @return zip of config files
     * @throws ConfigFilesHandlerException on error when matching version
     *                                     to a filename
     */
    public File getConfigFiles(int targetVersion) throws ConfigFilesHandlerException {
        final File[] files = confDir.listFiles();
        if (files != null) {
            final int[] versions = Stream.of(files).mapToInt(file -> {
                int extPos = file.getName().lastIndexOf(".zip");
                if (extPos == -1) return -1;
                try {
                    return Integer.valueOf(file.getName().substring(0, extPos));
                } catch (NumberFormatException e) {
                    return -1;
                }
            }).toArray();
            if (IntStream.of(versions).noneMatch(i -> i >= 0)) {
                throw new ConfigFilesHandlerException(String.format(
                        "no zip files found in %s", confDir.getAbsolutePath()));
            }
            int match = getClosestMatchIndex(targetVersion, versions);
            if (match >= files.length) {
                throw new ConfigFilesHandlerException("index of matched file " +
                        "exceeds length of file list");
            }
            return files[match];
        } else {
            throw new ConfigFilesHandlerException(String.format(
                    "couldn't list files in %s", confDir.getAbsolutePath()));
        }
    }

    protected int getClosestMatchIndex(int target, int[] versions) {
        int index = 0;
        int diff = Math.abs(target - versions[0]);
        for (int i = 1; i < versions.length; i++) {
            if (target < versions[i]) continue;
            int newDiff = Math.abs(target - versions[i]);
            if (newDiff < diff) {
                diff = newDiff;
                index = i;
            }
        }
        return index;
    }
}
