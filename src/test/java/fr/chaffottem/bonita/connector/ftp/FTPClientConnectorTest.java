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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public abstract class FTPClientConnectorTest {

    public static final String HOSTNAME = "localhost";

    public static final String USER_NAME = "matti";

    public static final String PASSWORD = "ftp";

    private FakeFtpServer fakeFtpServer;

    @Before
    public void setUp() throws Exception {
        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(0);

        final FileSystem fileSystem = new WindowsFakeFileSystem();
        for (final String directory : getServerDirecotries()) {
            fileSystem.add(new DirectoryEntry(directory));
        }
        for (final Entry<String, String> file : getServerFiles().entrySet()) {
            fileSystem.add(new FileEntry(file.getKey(), file.getValue()));
        }
        fakeFtpServer.setFileSystem(fileSystem);

        final UserAccount userAccount = new UserAccount(USER_NAME, PASSWORD, getUserDirectory());
        fakeFtpServer.addUserAccount(userAccount);

        fakeFtpServer.start();
    }

    @After
    public void tearDown() throws Exception {
        fakeFtpServer.stop();
    }

    public Integer getListeningPort() {
        return fakeFtpServer.getServerControlPort();
    }

    public void validate(final Map<String, Object> paramaters) throws ConnectorValidationException {
        validateAndGetConnector(paramaters);
    }

    public Map<String, Object> execute(final Map<String, Object> paramaters) throws ConnectorValidationException, ConnectorException {
        final FTPClientConnector connector = validateAndGetConnector(paramaters);
        try {
            connector.connect();
            return connector.execute();
        } finally {
            connector.disconnect();
        }
    }

    private FTPClientConnector validateAndGetConnector(final Map<String, Object> paramaters) throws ConnectorValidationException {
        final FTPClientConnector connector = getFTPClientConnector();
        connector.setInputParameters(paramaters);
        connector.validateInputParameters();
        return connector;
    }

    public FileEntry getFile(final String path) {
        return (FileEntry) fakeFtpServer.getFileSystem().getEntry(path);
    }

    public DirectoryEntry getDirectory(final String path) {
        return (DirectoryEntry) fakeFtpServer.getFileSystem().getEntry(path);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsBecauseHostnameIsMissing() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.PORT, getListeningPort());

        validate(paramaters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsBecauseHostnameIsEmpty() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, "");
        paramaters.put(FTPClientConnector.PORT, getListeningPort());

        validate(paramaters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsBecausePortNumberIsMissing() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);

        validate(paramaters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToATooSmallPortNumber() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, -1);

        validate(paramaters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToAnOversizedPortNumber() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, 65536);

        validate(paramaters);
    }

    @Test
    public void validationIsOkWithASCIITransferType() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, 21);
        paramaters.put(FTPClientConnector.TRANSFER_TYPE, "ASCII");

        validate(paramaters);
    }

    @Test
    public void validationIsOkWithBinaryTransferType() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, 21);
        paramaters.put(FTPClientConnector.TRANSFER_TYPE, "binary");

        validate(paramaters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToAnUnknownTransferType() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, 21);
        paramaters.put(FTPClientConnector.TRANSFER_TYPE, "unknown");

        validate(paramaters);
    }

    @Test
    public void validationIsOkWithActiveTransferMode() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, 21);
        paramaters.put(FTPClientConnector.TRANSFER_MODE, "Active");

        validate(paramaters);
    }

    @Test
    public void validationIsOkWithPassiveTransferMode() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, 21);
        paramaters.put(FTPClientConnector.TRANSFER_MODE, "Passive");

        validate(paramaters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToAnUnknownTransfermode() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, 21);
        paramaters.put(FTPClientConnector.TRANSFER_MODE, "unknown");

        validate(paramaters);
    }

    @Test(expected = ConnectorException.class)
    public void connectionFailsDueToAnUnknownHost() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, "somewhere");
        paramaters.put(FTPClientConnector.PORT, getListeningPort());

        execute(paramaters);
    }

    @Test(expected = ConnectorException.class)
    public void authenticationFailsDueToAWrongUserName() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, "matt");
        paramaters.put(FTPClientConnector.PASSWORD, PASSWORD);

        execute(paramaters);
    }

    @Test(expected = ConnectorException.class)
    public void authenticationFailsDueToAWrongPassword() throws Exception {
        final Map<String, Object> paramaters = new HashMap<String, Object>();
        paramaters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        paramaters.put(FTPClientConnector.PORT, getListeningPort());
        paramaters.put(FTPClientConnector.USER_NAME, USER_NAME);
        paramaters.put(FTPClientConnector.PASSWORD, "pass");

        execute(paramaters);
    }

    public abstract Map<String, String> getServerFiles();

    public abstract List<String> getServerDirecotries();

    public abstract String getUserDirectory();

    public abstract FTPClientConnector getFTPClientConnector();

}
