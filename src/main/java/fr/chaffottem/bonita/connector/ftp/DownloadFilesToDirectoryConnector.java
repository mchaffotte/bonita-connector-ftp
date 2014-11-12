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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.connector.ConnectorException;

/**
 * @author Matthieu Chaffotte
 */
public class DownloadFilesToDirectoryConnector extends FTPClientConnector {

    public static final String FILE_PATHS = "filePaths";

    public static final String DIRECTORY_PATH = "directoryPath";

    @SuppressWarnings("unchecked")
    @Override
    protected void executeFTPTask() throws IOException, ConnectorException {
        final List<String> files = (List<String>) getInputParameter(FILE_PATHS, new HashMap<String, String>());
        final String directoryPath = (String) getInputParameter(DIRECTORY_PATH);
        final File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        for (final String file : files) {
            downloadFile(directory, file);
        }
    }

    private void downloadFile(final File directory, final String file) throws FileNotFoundException, IOException {
        final String fileName = getFileName(file);
        final File localFile = new File(directory, fileName);
        final FileOutputStream fos = new FileOutputStream(localFile);
        try {
            getFTPClient().retrieveFile(file, fos);
        } finally {
            fos.close();
        }
    }

    private String getFileName(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

}
