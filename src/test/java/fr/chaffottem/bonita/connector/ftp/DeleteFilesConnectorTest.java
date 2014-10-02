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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class DeleteFilesConnectorTest extends FTPClientConnectorTest {

    private static final String DOC_DIRECTORY = "c:\\share\\docs";

    private static final String TEXT_FILE_PATHNAME = "c:\\share\\docs\\file1.txt";

    private static final String EXEC_FILE_PATHNAME = "c:\\share\\run.exe";

    @Override
    public FTPClientConnector getFTPClientConnector() {
        return new DeleteFilesConnector();
    }

    @Override
    public List<String> getServerDirecotries() {
        return Arrays.asList("c:\\share", "c:\\share\\images", DOC_DIRECTORY);
    }

    @Override
    public Map<String, String> getServerFiles() {
        final Map<String, String> files = new HashMap<String, String>();
        files.put(EXEC_FILE_PATHNAME, "");
        files.put(TEXT_FILE_PATHNAME, "qsfojsdfmljsgih");
        return files;
    }

    @Override
    public String getUserDirectory() {
        return "c:\\share";
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
    public void deleteAFile() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);
        paramaters.put(FTPClientConnector.TRANSFER_MODE, "Active");
        paramaters.put(DeleteFilesConnector.PATHNAMES, Arrays.asList(EXEC_FILE_PATHNAME));

        final Map<String, Object> result = execute(paramaters);

        assertThat(getFile(EXEC_FILE_PATHNAME)).isNull();
        assertThat(getStatusOfFile(result, EXEC_FILE_PATHNAME)).isTrue();
    }

    @Test
    public void deleteFiles() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);
        paramaters.put(DeleteFilesConnector.PATHNAMES, Arrays.asList(EXEC_FILE_PATHNAME, TEXT_FILE_PATHNAME));

        final Map<String, Object> result = execute(paramaters);

        assertThat(getFile(EXEC_FILE_PATHNAME)).isNull();
        assertThat(getFile(TEXT_FILE_PATHNAME)).isNull();
        assertThat(getStatusOfFile(result, EXEC_FILE_PATHNAME)).isTrue();
        assertThat(getStatusOfFile(result, TEXT_FILE_PATHNAME)).isTrue();
    }

    @Test
    public void deleteADirectoryDoesNothing() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);
        paramaters.put(DeleteFilesConnector.PATHNAMES, Arrays.asList(DOC_DIRECTORY));

        final Map<String, Object> result = execute(paramaters);

        assertThat(getFile(TEXT_FILE_PATHNAME)).isNotNull();
        assertThat(getStatusOfFile(result, DOC_DIRECTORY)).isFalse();
    }

    @Test
    public void deleteNothing() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);

        final Map<String, Object> result = execute(paramaters);

        assertThat(getFile(EXEC_FILE_PATHNAME)).isNotNull();
        assertThat(getFile(TEXT_FILE_PATHNAME)).isNotNull();
        assertThat(getStatus(result)).isEmpty();
    }

}
