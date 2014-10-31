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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.tika.Tika;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.connector.ConnectorException;

/**
 * @author Matthieu Chaffotte
 */
public class DownloadFilesToDocumentsConnector extends FTPClientConnector {

    public static final String FILE_PATHS = "filePaths";

    public static final String DOCUMENT_VALUES = "documentValues";

    private final Tika tika;

    public DownloadFilesToDocumentsConnector() {
        tika = new Tika();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeFTPTask() throws IOException, ConnectorException {
        final List<String> files = (List<String>) getInputParameter(FILE_PATHS, new HashMap<String, String>());
        final List<DocumentValue> documentValues = new ArrayList<DocumentValue>();
        for (final String file : files) {
            final byte[] content = downloadFile(file);
            final String mimeType = tika.detect(content);
            final String fileName = getFileName(file);
            final DocumentValue docValue = new DocumentValue(content, mimeType, fileName);
            documentValues.add(docValue);
        }
        setOutputParameter(DOCUMENT_VALUES, documentValues);
    }

    private String getFileName(final String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private byte[] downloadFile(final String remotePath) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            getFTPClient().retrieveFile(remotePath, output);
            return output.toByteArray();
        } finally {
            output.close();
        }
    }

}
