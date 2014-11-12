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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockftpserver.fake.filesystem.FileEntry;

public class DownloadFilesToDirectoryConnectorTest extends FTPClientConnectorTest {

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
        return new DownloadFilesToDirectoryConnector();
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
    public void downloadFiles() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(FTPClientConnector.TRANSFER_TYPE, "ascii");
        final List<String> filePaths = new ArrayList<String>();
        filePaths.add("docs/file1.txt");
        filePaths.add("run.exe");
        parameters.put(DownloadFilesToDirectoryConnector.FILE_PATHS, filePaths);
        parameters.put(DownloadFilesToDirectoryConnector.DIRECTORY_PATH, "./target/local");

        execute(parameters);

        final File file = new File("./target/local/file1.txt");
        assertThat(file).exists();
        final File exeFile = new File("./target/local/run.exe");
        assertThat(exeFile).exists();
    }

}
