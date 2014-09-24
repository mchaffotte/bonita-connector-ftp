package fr.chaffottem.bonita.connector.ftp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DownloadFilesConnectorTest extends FTPClientConnectorTest {

    @Mock
    private APIAccessor apiAccessor;

    @Mock
    private ProcessAPI processAPI;

    private EngineExecutionContext executionContext;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        executionContext = new EngineExecutionContext();
        executionContext.setProcessInstanceId(46887L);

        when(apiAccessor.getProcessAPI()).thenReturn(processAPI);
    }

    @Override
    public List<String> getServerDirecotries() {
        return Arrays.asList("c:\\share", "c:\\share\\images", "c:\\share\\docs");
    }

    @Override
    public Map<String, String> getServerFiles() {
        final Map<String, String> files = new HashMap<String, String>();
        files.put("c:\\share\\run.exe", "");
        files.put("c:\\share\\docs\\file1.txt", "qsfojsdfmljsgih");
        return files;
    }

    @Override
    public String getUserDirectory() {
        return "c:\\share";
    }

    @Override
    public FTPClientConnector getFTPClientConnector() {
        final DownloadFilesConnector connector = new DownloadFilesConnector();
        connector.setAPIAccessor(apiAccessor);
        connector.setExecutionContext(executionContext);
        return connector;
    }

    private byte[] getFileContent(final FileEntry file) throws IOException {
        final InputStream inputStream = file.createInputStream();
        try {
            return IOUtils.toByteArray(inputStream);
        } finally {
            inputStream.close();
        }
    }

    @Test
    public void downloadADocument() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);
        final List<String> filePaths = new ArrayList<String>();
        filePaths.add("c:\\share\\docs\\file1.txt");
        paramaters.put(DownloadFilesConnector.FILE_PATHS, filePaths);
        final FileEntry file = getFile("c:\\share\\docs\\file1.txt");
        final byte[] fileContent = getFileContent(file);
        final DocumentValue expected = new DocumentValue(fileContent, "text/plain", "file1.txt");

        final Map<String, Object> execute = execute(paramaters);

        final List<DocumentValue> docs = (List<DocumentValue>) execute.get(DownloadFilesConnector.DOCUMENT_VALUES);
        assertThat(docs.get(0)).isEqualTo(expected);
    }

}
