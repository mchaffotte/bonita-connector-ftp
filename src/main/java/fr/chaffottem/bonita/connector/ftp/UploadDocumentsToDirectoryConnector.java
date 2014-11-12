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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.connector.ConnectorException;

/**
 * @author Matthieu Chaffotte
 */
public class UploadDocumentsToDirectoryConnector extends FTPClientConnector {

    public static final String DIRECTORY_PATH = "directoryPath";

    public static final String DOCUMENTS = "documents";

    public static final String STATUS = "status";

    @SuppressWarnings("unchecked")
    @Override
    protected void executeFTPTask() throws IOException, ConnectorException {
        final List<String> documentNames = (List<String>) getInputParameter(DOCUMENTS, new HashMap<String, String>());
        final Map<String, Boolean> status = new HashMap<String, Boolean>();
        final String directory = (String) getInputParameter(DIRECTORY_PATH);
        getFTPClient().changeWorkingDirectory(directory);
        for (final String documentName : documentNames) {
            final boolean done = uploadDocument(documentName);
            status.put(documentName, done);
        }
        setOutputParameter(STATUS, status);
    }

    private boolean uploadDocument(final String documentName) throws ConnectorException, IOException {
        final Document document = getDocument(documentName);
        final InputStream inputStream = getDocumentInputStream(document);
        try {
            final String fileName = getFileName(document);
            return getFTPClient().storeFile(fileName, inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private String getFileName(final Document document) {
        String fileName;
        final String contentFileName = document.getContentFileName();
        if (contentFileName == null || "".equals(contentFileName.trim())) {
            fileName = document.getName();
        } else {
            fileName = contentFileName;
        }
        return fileName;
    }

    private Document getDocument(final String documentName) throws ConnectorException {
        final ProcessAPI processAPI = getAPIAccessor().getProcessAPI();
        final long processInstanceId = getExecutionContext().getProcessInstanceId();
        try {
            return processAPI.getLastDocument(processInstanceId, documentName);
        } catch (final DocumentNotFoundException dnfe) {
            throw new ConnectorException(dnfe);
        }
    }

    private InputStream getDocumentInputStream(final Document document) throws ConnectorException {
        final ProcessAPI processAPI = getAPIAccessor().getProcessAPI();
        try {
            if (document.hasContent()) {
                return new ByteArrayInputStream(processAPI.getDocumentContent(document.getContentStorageId()));
            } else {
                return new ByteArrayInputStream(new byte[0]);
            }
        } catch (final DocumentNotFoundException dnfe) {
            throw new ConnectorException(dnfe);
        }
    }

}
