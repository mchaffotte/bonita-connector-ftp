/**
 * Copyright (C) 2014 Matthieu Chaffotte
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 */
package fr.chaffottem.bonita.connector.ftp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UploadFilesConnectorTest extends FTPClientConnectorTest {

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
        executionContext.setProcessInstanceId(46887);

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
        final UploadFilesConnector connector = new UploadFilesConnector();
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

    private Boolean getStatusOfFile(final Map<String, Object> result, final String pathname) {
        final Map<String, Boolean> status = getStatus(result);
        return status.get(pathname);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getStatus(final Map<String, Object> result) {
        return (Map<String, Boolean>) result.get(DeleteFilesConnector.STATUS);
    }

    @Test
    public void uploadADocument() throws Exception {
        final byte[] content = new byte[] { 0, 1, 0, 1, 0, 0, 0, 0, 1, 0 };
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(UploadFilesConnector.DIRECTORY_PATH, "docs");
        parameters.put(UploadFilesConnector.DOCUMENTS, Arrays.asList("processFile"));
        final Document document = mock(Document.class);
        when(processAPI.getLastDocument(46887, "processFile")).thenReturn(document);
        when(document.hasContent()).thenReturn(true);
        when(document.getContentStorageId()).thenReturn("bcvxft");
        when(processAPI.getDocumentContent("bcvxft")).thenReturn(content);
        when(document.getContentFileName()).thenReturn("file2.bin");

        final Map<String, Object> result = execute(parameters);

        final FileEntry file = getFile("c:\\share\\docs\\file2.bin");
        assertThat(file).isNotNull();
        final byte[] fileContent = getFileContent(file);
        assertThat(fileContent).isEqualTo(content);
        assertThat(getStatusOfFile(result, "processFile")).isTrue();
    }

    @Test
    public void uploadAnEmptyDocument() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(UploadFilesConnector.DIRECTORY_PATH, "docs");
        parameters.put(UploadFilesConnector.DOCUMENTS, Arrays.asList("processFile"));
        final Document document = mock(Document.class);
        when(processAPI.getLastDocument(46887, "processFile")).thenReturn(document);
        when(document.getName()).thenReturn("processFile");
        when(document.hasContent()).thenReturn(false);
        when(document.getContentStorageId()).thenReturn("bcvxft");

        final Map<String, Object> result = execute(parameters);

        final FileEntry file = getFile("c:\\share\\docs\\processFile");
        assertThat(file).isNotNull();
        final byte[] fileContent = getFileContent(file);
        assertThat(fileContent).isEqualTo(new byte[0]);
        assertThat(getStatusOfFile(result, "processFile")).isTrue();
    }

    @Test(expected = ConnectorException.class)
    public void uploadADocumentThrowsAnExceptionWhenNotFound() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(UploadFilesConnector.DIRECTORY_PATH, "docs");
        parameters.put(UploadFilesConnector.DOCUMENTS, Arrays.asList("processFile"));
        when(processAPI.getLastDocument(46887, "processFile")).thenThrow(new DocumentNotFoundException(null));

        execute(parameters);
    }

}
