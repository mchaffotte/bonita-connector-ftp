/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

public class DeleteFilesConnectorTest extends FTPClientConnectorTest {

    @Override
    public FTPClientConnector getFTPClientConnector() {
        return new DeleteFilesConnector();
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
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(FTPClientConnector.TRANSFER_MODE, "Active");
        parameters.put(DeleteFilesConnector.PATHNAMES, Arrays.asList("run.exe"));

        final Map<String, Object> result = execute(parameters);

        assertThat(getFile("c:\\share\\run.exe")).isNull();
        assertThat(getStatusOfFile(result, "run.exe")).isTrue();
    }

    @Test
    public void deleteFiles() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(DeleteFilesConnector.PATHNAMES, Arrays.asList("run.exe", "docs/file1.txt"));

        final Map<String, Object> result = execute(parameters);

        assertThat(getFile("c:\\share\\run.exe")).isNull();
        assertThat(getFile("c:\\share\\docs\\file1.txt")).isNull();
        assertThat(getStatusOfFile(result, "run.exe")).isTrue();
        assertThat(getStatusOfFile(result, "docs/file1.txt")).isTrue();
    }

    @Test
    public void deleteADirectoryDoesNothing() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(DeleteFilesConnector.PATHNAMES, Arrays.asList("docs"));

        final Map<String, Object> result = execute(parameters);

        assertThat(getFile("c:\\share\\docs\\file1.txt")).isNotNull();
        assertThat(getStatusOfFile(result, "docs")).isFalse();
    }

    @Test
    public void deleteNothing() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);

        final Map<String, Object> result = execute(parameters);

        assertThat(getFile("c:\\share\\run.exe")).isNotNull();
        assertThat(getFile("c:\\share\\docs\\file1.txt")).isNotNull();
        assertThat(getStatus(result)).isEmpty();
    }

}
