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

    public void validate(final Map<String, Object> parameters) throws ConnectorValidationException {
        validateAndGetConnector(parameters);
    }

    public Map<String, Object> execute(final Map<String, Object> parameters) throws ConnectorValidationException, ConnectorException {
        final FTPClientConnector connector = validateAndGetConnector(parameters);
        try {
            connector.connect();
            return connector.execute();
        } finally {
            connector.disconnect();
        }
    }

    private FTPClientConnector validateAndGetConnector(final Map<String, Object> parameters) throws ConnectorValidationException {
        final FTPClientConnector connector = getFTPClientConnector();
        connector.setInputParameters(parameters);
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
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.PORT, getListeningPort());

        validate(parameters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsBecauseHostnameIsEmpty() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, "");
        parameters.put(FTPClientConnector.PORT, getListeningPort());

        validate(parameters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsBecausePortNumberIsMissing() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);

        validate(parameters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToATooSmallPortNumber() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, -1);

        validate(parameters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToAnOversizedPortNumber() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 65536);

        validate(parameters);
    }

    @Test
    public void validationIsOkWithASCIITransferType() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.TRANSFER_TYPE, "ASCII");

        validate(parameters);
    }

    @Test
    public void validationIsOkWithBinaryTransferType() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.TRANSFER_TYPE, "binary");

        validate(parameters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToAnUnknownTransferType() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.TRANSFER_TYPE, "unknown");

        validate(parameters);
    }

    @Test
    public void validationIsOkWithActiveTransferMode() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.TRANSFER_MODE, "Active");

        validate(parameters);
    }

    @Test
    public void validationIsOkWithPassiveTransferMode() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.TRANSFER_MODE, "Passive");

        validate(parameters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToAnUnknownTransferMode() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.TRANSFER_MODE, "unknown");

        validate(parameters);
    }

    @Test
    public void validationIsOkWithExplicitSecurityMode() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.SECURITY_MODE, "Explicit");

        validate(parameters);
    }

    @Test
    public void validationIsOkWithImplicitSecurityMode() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.SECURITY_MODE, "Implicit");

        validate(parameters);
    }

    @Test(expected = ConnectorValidationException.class)
    public void validationFailsDueToAnUnknownSecurityMode() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, 21);
        parameters.put(FTPClientConnector.SECURITY_MODE, "unknown");

        validate(parameters);
    }

    @Test(expected = ConnectorException.class)
    public void connectionFailsDueToAnUnknownHost() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, "somewhere");
        parameters.put(FTPClientConnector.PORT, getListeningPort());

        execute(parameters);
    }

    @Test(expected = ConnectorException.class)
    public void authenticationFailsDueToAWrongUserName() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, "matt");
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);

        execute(parameters);
    }

    @Test(expected = ConnectorException.class)
    public void authenticationFailsDueToAWrongPassword() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, "pass");

        execute(parameters);
    }

    public abstract Map<String, String> getServerFiles();

    public abstract List<String> getServerDirecotries();

    public abstract String getUserDirectory();

    public abstract FTPClientConnector getFTPClientConnector();

}
