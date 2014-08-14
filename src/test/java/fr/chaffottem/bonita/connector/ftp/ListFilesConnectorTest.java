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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPFile;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.WindowsFakeFileSystem;

/**
 * @author Matthieu Chaffotte
 */
public class ListFilesConnectorTest {

    private FakeFtpServer fakeFtpServer;

    @Before
    public void setUp() throws Exception {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(0);

        final FileSystem fileSystem = new WindowsFakeFileSystem();
        fileSystem.add(new DirectoryEntry("c:\\share"));
        fileSystem.add(new FileEntry("c:\\share\\run.exe"));
        fileSystem.add(new DirectoryEntry("c:\\share\\images"));
        fileSystem.add(new DirectoryEntry("c:\\share\\docs"));
        fileSystem.add(new FileEntry("c:\\share\\docs\\file1.txt", "abcdef 1234567890"));
        fakeFtpServer.setFileSystem(fileSystem);

        final UserAccount userAccount = new UserAccount("matti", "bpm", "c:\\share");
        fakeFtpServer.addUserAccount(userAccount);

        fakeFtpServer.start();
    }

    @After
    public void tearDown() throws Exception {
        fakeFtpServer.stop();
    }

    private Map<String, Object> execute(final Map<String, Object> paramaters) throws ConnectorValidationException, ConnectorException {
        final ListFilesConnector connector = new ListFilesConnector();
        connector.setInputParameters(paramaters);
        connector.validateInputParameters();
        connector.connect();
        final Map<String, Object> result = connector.execute();
        connector.disconnect();
        return result;
    }

    @Test
    public void getFilesListReturnsFilesAndDirecotries() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, "localhost");
        paramaters.put(FTPClientConnector.PORT, fakeFtpServer.getServerControlPort());
        paramaters.put(FTPClientConnector.USER_NAME, "matti");
        paramaters.put(FTPClientConnector.PASSWORD, "bpm");
        paramaters.put(ListFilesConnector.PATHNAME, "c:\\share");
        final Map<String, Object> result = execute(paramaters);

        final List<FTPFile> files = (List<FTPFile>) result.get(ListFilesConnector.FTP_FILES);
        assertThat(files).hasSize(3);
        assertThat(files.get(0).getName()).isEqualTo("run.exe");
        assertThat(files.get(1).getName()).isEqualTo("docs");
        assertThat(files.get(2).getName()).isEqualTo("images");
    }

    @Test(expected = ConnectorException.class)
    public void authenticationFailsDueToAWrongPassword() throws Exception {
        final ListFilesConnector connector = new ListFilesConnector();
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, "localhost");
        paramaters.put(FTPClientConnector.PORT, fakeFtpServer.getServerControlPort());
        paramaters.put(FTPClientConnector.USER_NAME, "matti");
        paramaters.put(FTPClientConnector.PASSWORD, "bpm2");
        connector.setInputParameters(paramaters);
        connector.validateInputParameters();
        connector.connect();
        connector.execute();
    }

    @Test
    public void getFilesListReturnsNothingWhenUserHasNoRightOnFolder() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, "localhost");
        paramaters.put(FTPClientConnector.PORT, fakeFtpServer.getServerControlPort());
        paramaters.put(FTPClientConnector.USER_NAME, "matti");
        paramaters.put(FTPClientConnector.PASSWORD, "bpm");
        paramaters.put(ListFilesConnector.PATHNAME, "c:\\private");
        final Map<String, Object> result = execute(paramaters);

        final List<FTPFile> files = (List<FTPFile>) result.get(ListFilesConnector.FTP_FILES);
        assertThat(files).hasSize(0);
    }

}
