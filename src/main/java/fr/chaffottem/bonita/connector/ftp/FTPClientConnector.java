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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * @author Matthieu Chaffotte
 */
public abstract class FTPClientConnector extends AbstractConnector {

    public static final String HOSTNAME = "hostname";

    public static final String PORT = "port";

    public static final String USER_NAME = "userName";

    public static final String PASSWORD = "password";

    public static final String TRANSFER_TYPE = "transferType";

    public static final String TRANSFER_MODE = "transferMode";

    public static final String FTPS = "ftps";

    public static final String SECURITY_PROTOCOL = "securityProtocol";

    public static final String SECURITY_MODE = "securityMode";

    private FTPClient ftpClient;

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final List<String> errors = new ArrayList<String>();
        final String hostname = (String) getInputParameter(HOSTNAME);
        if (hostname == null) {
            errors.add("The hostname is not defined");
        } else if ("".equals(hostname.trim())) {
            errors.add("The hostname is empty");
        }
        final Integer port = (Integer) getInputParameter(PORT);
        if (port == null) {
            errors.add("The port is not defined");
        } else if (port < 0) {
            errors.add("The port is less than 0");
        } else if (port > 65535) {
            errors.add("The port is greater than 65535");
        }
        final String transfertType = (String) getInputParameter(TRANSFER_TYPE);
        if (transfertType != null) {
            if (!("ASCII".equalsIgnoreCase(transfertType) || "binary".equalsIgnoreCase(transfertType))) {
                errors.add("Only ASCII and binary are supported as transfer types");
            }
        }
        final String transfertMode = (String) getInputParameter(TRANSFER_MODE);
        if (transfertMode != null) {
            if (!("active".equalsIgnoreCase(transfertMode) || "passive".equalsIgnoreCase(transfertMode))) {
                errors.add("Only active and passive are supported as transfer modes");
            }
        }
        final String securityMode = (String) getInputParameter(SECURITY_MODE);
        if (securityMode != null) {
            if (!("implicit".equalsIgnoreCase(securityMode) || "explicit".equalsIgnoreCase(securityMode))) {
                errors.add("Only explicit and implicit are supported as security modes");
            }
        }

        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }
    }

    @Override
    public void connect() throws ConnectorException {
        ftpClient = build();
        try {
            final String hostname = (String) getInputParameter(HOSTNAME);
            final Integer port = (Integer) getInputParameter(PORT);
            ftpClient.connect(hostname, port);
        } catch (final IOException ioe) {
            throw new ConnectorException(ioe);
        }
    }

    protected FTPClient build() {
        final Boolean ftps = (Boolean) getInputParameter(FTPS, false);
        if (!ftps) {
            return new FTPClient();
        } else {
            final String securityProtocol = (String) getInputParameter(SECURITY_PROTOCOL, "TLS");
            final boolean isImplicit = isImplicit();
            return new FTPSClient(securityProtocol, isImplicit);
        }
    }

    protected boolean isImplicit() {
        final String securityMode = (String) getInputParameter(SECURITY_MODE, "implicit");
        boolean isImplicit;
        if ("implicit".equalsIgnoreCase(securityMode)) {
            isImplicit = true;
        } else {
            isImplicit = false;
        }
        return isImplicit;
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        try {
            boolean login = true;
            final String userName = (String) getInputParameter(USER_NAME);
            if (userName != null) {
                final String password = (String) getInputParameter(PASSWORD);
                login = ftpClient.login(userName, password);
            }
            if (login) {
                final int reply = ftpClient.getReplyCode();
                if (FTPReply.isPositiveCompletion(reply)) {
                    configureClient();
                    executeFTPTask();
                }
            } else {
                throw new ConnectorException("Login fails due to a wrong user name/password");
            }
        } catch (final IOException ioe) {
            throw new ConnectorException(ioe);
        }
    }

    private void configureClient() throws IOException {
        final String transferType = (String) getInputParameter(TRANSFER_TYPE, "binary");
        if ("ascii".equalsIgnoreCase(transferType)) {
            ftpClient.setFileType(FTP.ASCII_FILE_TYPE);
        } else {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        }
        final String transferMode = (String) getInputParameter(TRANSFER_MODE, "passive");
        if ("active".equalsIgnoreCase(transferMode)) {
            ftpClient.enterLocalActiveMode();
        } else {
            ftpClient.enterLocalPassiveMode();
        }
    }

    protected abstract void executeFTPTask() throws IOException, ConnectorException;

    @Override
    public void disconnect() throws ConnectorException {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (final IOException ioe) {
                throw new ConnectorException(ioe);
            }
        }
    }

    protected FTPClient getFTPClient() {
        return ftpClient;
    }

}
