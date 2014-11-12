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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.connector.ConnectorException;

/**
 * @author Matthieu Chaffotte
 */
public class UploadFilesToDirectoryConnector extends FTPClientConnector {

    public static final String DIRECTORY_PATH = "directoryPath";

    public static final String FILE_PATHS = "filePaths";

    public static final String STATUS = "status";

    @SuppressWarnings("unchecked")
    @Override
    protected void executeFTPTask() throws IOException, ConnectorException {
        final List<String> filePaths = (List<String>) getInputParameter(FILE_PATHS, new HashMap<String, String>());
        final Map<String, Boolean> status = new HashMap<String, Boolean>();
        final String directory = (String) getInputParameter(DIRECTORY_PATH);
        getFTPClient().changeWorkingDirectory(directory);
        for (final String filePath : filePaths) {
            final boolean done = uploadFile(filePath);
            status.put(filePath, done);
        }
        setOutputParameter(STATUS, status);
    }

    private boolean uploadFile(final String filePath) throws IOException {
        final File file = new File(filePath);
        final FileInputStream fis = new FileInputStream(file);
        try {
            return getFTPClient().storeFile(file.getName(), fis);
        } finally {
            fis.close();
        }
    }

}
