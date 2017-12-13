/*
 * Copyright Dansk Bibliotekscenter a/s. Licensed under GPLv3
 * See license text in LICENSE.txt or at https://opensource.dbc.dk/licenses/gpl-3.0/
 */

package dk.dbc.neptun;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigFilesHandlerBeanTest {
    private File confDir = mock(File.class);

    @Test
    public void test_getClosestsMatchExact() {
        ConfigFilesHandlerBean configFilesHandlerBean =
            getConfigFilesHandlerBean();
        int match = configFilesHandlerBean.getClosestMatchIndex(4,
            new int[]{1, 2, 4, 5});
        assertThat(match, is(2));
    }

    @Test
    public void test_getClosetsMatchLower() {
        ConfigFilesHandlerBean configFilesHandlerBean =
            getConfigFilesHandlerBean();
        int match = configFilesHandlerBean.getClosestMatchIndex(4,
            new int[]{1, 3, 5, 7});
        assertThat(match, is(1));
    }

    @Test
    public void test_getClosestMatchHigher() {
        ConfigFilesHandlerBean configFilesHandlerBean =
            getConfigFilesHandlerBean();
        int match = configFilesHandlerBean.getClosestMatchIndex(4,
            new int[]{1, 3});
        assertThat(match, is(1));
    }

    @Test
    public void test_getClosestMatchStartHigher() {
        ConfigFilesHandlerBean configFilesHandlerBean =
            getConfigFilesHandlerBean();
        int match = configFilesHandlerBean.getClosestMatchIndex(1,
            new int[]{3, 4});
        assertThat(match, is(0));
    }

    @Test
    public void test_getClosestMatchValueAboveTarget() {
        ConfigFilesHandlerBean bean = getConfigFilesHandlerBean();
        int match = bean.getClosestMatchIndex(13, new int[]{1, 14});
        assertThat(match, is(0));
    }

    @Test
    public void test_getConfigFiles() throws ConfigFilesHandlerException {
        File file1 = mock(File.class);
        File file2 = mock(File.class);
        when(file1.getName()).thenReturn("1.zip");
        when(file2.getName()).thenReturn("14.zip");
        when(confDir.listFiles()).thenReturn(new File[]{file1, file2});
        ConfigFilesHandlerBean configFilesHandlerBean =
            getConfigFilesHandlerBean();
        File match = configFilesHandlerBean.getConfigFiles(13);

        assertThat("filename", match.getName(), is("1.zip"));
    }

    @Test
    public void test_getConfigFilesNonNumericFilename() throws ConfigFilesHandlerException {
        File file1 = mock(File.class);
        File file2 = mock(File.class);
        when(file1.getName()).thenReturn("blah.zip");
        when(file2.getName()).thenReturn("3.zip");
        when(confDir.listFiles()).thenReturn(new File[]{file1, file2});
        ConfigFilesHandlerBean configFilesHandlerBean =
            getConfigFilesHandlerBean();
        File match = configFilesHandlerBean.getConfigFiles(5);

        assertThat("filename", match.getName(), is("3.zip"));
    }

    @Test
    public void test_getConfigFilesInvalidFileName() {
        File file = mock(File.class);
        when(file.getName()).thenReturn("blah");
        when(confDir.listFiles()).thenReturn(new File[]{file});
        ConfigFilesHandlerBean configFilesHandlerBean =
            getConfigFilesHandlerBean();

        assertThrows(ConfigFilesHandlerException.class,
            () -> configFilesHandlerBean.getConfigFiles(0));
    }

    @Test
    public void test_getConfigFilesListFilesNull() {
        when(confDir.listFiles()).thenReturn(null);
        ConfigFilesHandlerBean configFilesHandlerBean =
            getConfigFilesHandlerBean();

        assertThrows(ConfigFilesHandlerException.class,
            () -> configFilesHandlerBean.getConfigFiles(0));
    }

    private ConfigFilesHandlerBean getConfigFilesHandlerBean() {
        ConfigFilesHandlerBean configFilesHandlerBean =
            new ConfigFilesHandlerBean();
        configFilesHandlerBean.confDir = confDir;
        return configFilesHandlerBean;
    }
}
