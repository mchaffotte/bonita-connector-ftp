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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTPFile;

/**
 * @author Matthieu Chaffotte
 */
public class RemoveDirectoriesConnector extends FTPClientConnector {

    public static final String PATHNAMES = "pathnames";

    public static final String STATUS = "status";

    @SuppressWarnings("unchecked")
    @Override
    protected void executeFTPTask() throws IOException {
        final List<String> pathnames = (List<String>) getInputParameter(PATHNAMES);
        final Map<String, Boolean> status = new HashMap<String, Boolean>();
        if (pathnames != null) {
            for (final String pathname : pathnames) {
                final boolean directoryDeleted = removeDirectory(pathname);
                status.put(pathname, directoryDeleted);
            }
        }
        setOutputParameter(STATUS, status);
    }

    private boolean removeDirectory(final String pathDir) throws IOException {
        final FTPFile[] files = getFTPClient().listFiles(pathDir);
        for (final FTPFile file : files) {
            final StringBuilder pathBuilder = new StringBuilder(pathDir);
            pathBuilder.append("/").append(file.getName());
            if (file.isDirectory()) {
                removeDirectory(pathBuilder.toString());
            } else {
                getFTPClient().deleteFile(pathBuilder.toString());
            }
        }
        return getFTPClient().removeDirectory(pathDir);
    }

}
