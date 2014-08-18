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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
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

        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }
    }

    @Override
    public void connect() throws ConnectorException {
        ftpClient = new FTPClient();
        try {
            final String hostname = (String) getInputParameter(HOSTNAME);
            final Integer port = (Integer) getInputParameter(PORT);
            ftpClient.connect(hostname, port);
        } catch (final IOException ioe) {
            throw new ConnectorException(ioe);
        }
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
                    executeFTPTask();
                }
            } else {
                throw new ConnectorException("Login fails due to a wrong user name/password");
            }
        } catch (final IOException ioe) {
            throw new ConnectorException(ioe);
        }
    }

    protected abstract void executeFTPTask() throws IOException;

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
