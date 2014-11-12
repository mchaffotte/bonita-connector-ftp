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

import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;

public class ListFilesConnectorTest extends FTPClientConnectorTest {

    @Override
    public FTPClientConnector getFTPClientConnector() {
        return new ListFilesConnector();
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

    @Test
    public void getFilesListReturnsFilesAndDirectories() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(ListFilesConnector.PATHNAME, "docs");
        final Map<String, Object> result = execute(parameters);

        final List<FTPFile> files = (List<FTPFile>) result.get(ListFilesConnector.FTP_FILES);
        assertThat(files).hasSize(1);
        assertThat(files).extracting("name", String.class).containsOnly("file1.txt");
    }

    @Test
    public void getFilesListReturnsFilesAndDirectoriesUsingRootPath() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(ListFilesConnector.PATHNAME, "");
        final Map<String, Object> result = execute(parameters);

        final List<FTPFile> files = (List<FTPFile>) result.get(ListFilesConnector.FTP_FILES);
        assertThat(files).hasSize(3);
        assertThat(files).extracting("name", String.class).containsOnly("run.exe", "docs", "images");
    }

    @Test
    public void getFilesListReturnsFilesAndDirectoriesFromTheCurrentDirectoryWhenNotDefiningThePath() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        final Map<String, Object> result = execute(parameters);

        final List<FTPFile> files = (List<FTPFile>) result.get(ListFilesConnector.FTP_FILES);
        assertThat(files).hasSize(3);
        assertThat(files).extracting("name", String.class).containsOnly("run.exe", "docs", "images");
    }

    @Test
    public void getFilesListReturnsNothingWhenUserHasNoRightOnFolder() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(ListFilesConnector.PATHNAME, "private");
        final Map<String, Object> result = execute(parameters);

        final List<FTPFile> files = (List<FTPFile>) result.get(ListFilesConnector.FTP_FILES);
        assertThat(files).hasSize(0);
    }

    @Test
    public void getNoFilesListReturnsFilesAndDirectories() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(ListFilesConnector.PATHNAME, "images");
        final Map<String, Object> result = execute(parameters);

        final List<FTPFile> files = (List<FTPFile>) result.get(ListFilesConnector.FTP_FILES);
        assertThat(files).hasSize(0);
    }

}
