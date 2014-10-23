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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.junit.After;
import org.junit.Test;

public class FTPSIT {

    private static final String USER_NAME = "matti";

    private static final String PASSWORD = "secret";

    private FtpServer server;

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private void createAndStartServer(final boolean isImplicit) throws FtpException {
        server = createFtpsServer(isImplicit);
        server.start();
    }

    private FtpServer createFtpsServer(final boolean isImplicit) throws FtpException {
        final ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(2221);

        final SslConfigurationFactory sslConfigurationFactory = new SslConfigurationFactory();
        sslConfigurationFactory.setKeystoreFile(new File("src/test/resources/ftpserver.jks"));
        sslConfigurationFactory.setKeystorePassword("supermdp");
        listenerFactory.setSslConfiguration(sslConfigurationFactory.createSslConfiguration());
        listenerFactory.setImplicitSsl(isImplicit);

        final PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        final UserManager um = userManagerFactory.createUserManager();
        final BaseUser user = new BaseUser();
        user.setName(USER_NAME);
        user.setPassword(PASSWORD);
        user.setHomeDirectory("src/test/resources");
        final List<Authority> authorities = new ArrayList<Authority>();
        authorities.add(new WritePermission());
        authorities.add(new ConcurrentLoginPermission(0, 0));
        authorities.add(new TransferRatePermission(0, 0));
        user.setAuthorities(authorities);
        um.save(user);

        final FtpServerFactory ftpServerFactory = new FtpServerFactory();
        ftpServerFactory.addListener("default", listenerFactory.createListener());
        ftpServerFactory.setUserManager(um);
        return ftpServerFactory.createServer();
    }

    @Test
    public void listFiles_should_return_a_single_file_using_implicit_ftps() throws Exception {
        verifyTheContentOfRemoteDirectory(true);
    }

    @Test
    public void listFiles_should_return_a_single_file_using_explicit_ftps() throws Exception {
        verifyTheContentOfRemoteDirectory(false);
    }

    private void verifyTheContentOfRemoteDirectory(final boolean isImplicit) throws Exception {
        final String securityMode = isImplicit ? "implicit" : "explicit";
        createAndStartServer(isImplicit);

        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, "localhost");
        parameters.put(FTPClientConnector.PORT, 2221);
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(FTPClientConnector.FTPS, true);
        parameters.put(FTPClientConnector.SECURITY_MODE, securityMode);
        parameters.put(FTPClientConnector.SECURITY_PROTOCOL, "TLS");
        parameters.put(ListFilesConnector.PATHNAME, "");

        final ListFilesConnector connector = new ListFilesConnector();
        connector.setInputParameters(parameters);
        connector.connect();
        final Map<String, Object> result = connector.execute();
        final List<FtpFile> ftpFiles = (List<FtpFile>) result.get(ListFilesConnector.FTP_FILES);
        connector.disconnect();
        assertThat(ftpFiles).hasSize(1).extracting("name", String.class).containsOnly("ftpserver.jks");
    }

}
