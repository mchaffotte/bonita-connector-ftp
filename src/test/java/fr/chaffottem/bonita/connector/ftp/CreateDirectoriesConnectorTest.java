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
public class CreateDirectoriesConnectorTest extends FTPClientConnectorTest {

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
        return new CreateDirectoriesConnector();
    }

    private Boolean getStatusOfEntry(final Map<String, Object> result, final String pathname) {
        final Map<String, Boolean> status = getStatus(result);
        return status.get(pathname);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Boolean> getStatus(final Map<String, Object> result) {
        return (Map<String, Boolean>) result.get(CreateDirectoriesConnector.STATUS);
    }

    @Test
    public void makeADirectory() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);
        paramaters.put(CreateDirectoriesConnector.PATHNAMES, Arrays.asList("c:\\share\\music"));

        final Map<String, Object> result = execute(paramaters);

        assertThat(getDirectory("c:\\share\\music")).isNotNull();
        assertThat(getStatusOfEntry(result, "c:\\share\\music")).isTrue();
    }

    @Test
    public void makeAnExistingDirectory() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);
        paramaters.put(CreateDirectoriesConnector.PATHNAMES, Arrays.asList("c:\\share\\images"));

        final Map<String, Object> result = execute(paramaters);

        assertThat(getDirectory("c:\\share\\images")).isNotNull();
        assertThat(getStatusOfEntry(result, "c:\\share\\images")).isFalse();
    }

    @Test
    public void makeNothing() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);

        final Map<String, Object> result = execute(paramaters);
        assertThat(getStatus(result)).isEmpty();
    }

    @Test
    public void makeASubDirectory() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);
        paramaters.put(CreateDirectoriesConnector.PATHNAMES, Arrays.asList("c:\\share\\music", "c:\\share\\music\\albums"));

        final Map<String, Object> result = execute(paramaters);

        assertThat(getDirectory("c:\\share\\music")).isNotNull();
        assertThat(getDirectory("c:\\share\\music\\albums")).isNotNull();
        assertThat(getStatusOfEntry(result, "c:\\share\\music\\albums")).isTrue();
    }

}
