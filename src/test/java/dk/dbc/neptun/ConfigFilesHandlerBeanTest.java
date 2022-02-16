package dk.dbc.neptun;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigFilesHandlerBeanTest {

    @Mock
    File mockedConfDir;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
    }

    private class MockConfigFilesHandlerBean extends ConfigFilesHandlerBean {
        public MockConfigFilesHandlerBean() {
            confDir = mockedConfDir;
        }
    }

    @Test
    void test_getClosestMatchExact() {
        ConfigFilesHandlerBean configFilesHandlerBean = new MockConfigFilesHandlerBean();
        int match = configFilesHandlerBean.getClosestMatchIndex(4, new int[]{1, 2, 4, 5});
        assertThat(match, is(2));
    }

    @Test
    void test_getClosetMatchLower() {
        ConfigFilesHandlerBean configFilesHandlerBean = new MockConfigFilesHandlerBean();
        int match = configFilesHandlerBean.getClosestMatchIndex(4, new int[]{1, 3, 5, 7});
        assertThat(match, is(1));
    }

    @Test
    void test_getClosestMatchHigher() {
        ConfigFilesHandlerBean configFilesHandlerBean = new MockConfigFilesHandlerBean();
        int match = configFilesHandlerBean.getClosestMatchIndex(4, new int[]{1, 3});
        assertThat(match, is(1));
    }

    @Test
    void test_getClosestMatchStartHigher() {
        ConfigFilesHandlerBean configFilesHandlerBean = new MockConfigFilesHandlerBean();
        int match = configFilesHandlerBean.getClosestMatchIndex(1, new int[]{3, 4});
        assertThat(match, is(0));
    }

    @Test
    void test_getClosestMatchValueAboveTarget() {
        ConfigFilesHandlerBean bean = new MockConfigFilesHandlerBean();
        int match = bean.getClosestMatchIndex(13, new int[]{1, 14});
        assertThat(match, is(0));
    }

    @Test
    void test_getConfigFiles() throws ConfigFilesHandlerException {
        final ConfigFilesHandlerBean configFilesHandlerBean = new MockConfigFilesHandlerBean();
        final File file1 = mock(File.class);
        final File file2 = mock(File.class);
        when(file1.getName()).thenReturn("1.zip");
        when(file2.getName()).thenReturn("14.zip");
        when(mockedConfDir.listFiles()).thenReturn(new File[]{file1, file2});
        final File match = configFilesHandlerBean.getConfigFiles(13);

        assertThat("filename", match.getName(), is("1.zip"));
    }

    @Test
    void test_getConfigFilesNonNumericFilename() throws ConfigFilesHandlerException {
        File file1 = mock(File.class);
        File file2 = mock(File.class);
        when(file1.getName()).thenReturn("blah.zip");
        when(file2.getName()).thenReturn("3.zip");
        when(mockedConfDir.listFiles()).thenReturn(new File[]{file1, file2});
        ConfigFilesHandlerBean configFilesHandlerBean = new MockConfigFilesHandlerBean();
        File match = configFilesHandlerBean.getConfigFiles(5);

        assertThat("filename", match.getName(), is("3.zip"));
    }

    @Test
    void test_getConfigFilesInvalidFileName() {
        File file = mock(File.class);
        ConfigFilesHandlerBean configFilesHandlerBean = new MockConfigFilesHandlerBean();
        when(file.getName()).thenReturn("blah");
        when(configFilesHandlerBean.confDir.listFiles()).thenReturn(new File[]{file});

        assertThrows(ConfigFilesHandlerException.class, () -> configFilesHandlerBean.getConfigFiles(0));
    }

    @Test
    void test_getConfigFilesListFilesNull() {
        when(mockedConfDir.listFiles()).thenReturn(null);
        ConfigFilesHandlerBean configFilesHandlerBean = new MockConfigFilesHandlerBean();

        assertThrows(ConfigFilesHandlerException.class, () -> configFilesHandlerBean.getConfigFiles(0));
    }

}
