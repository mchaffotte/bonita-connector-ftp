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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.net.ftp.FTP;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.connector.ConnectorException;

/**
 * @author Matthieu Chaffotte
 */
public class UploadFilesConnector extends FTPClientConnector {

    public static final String DOCUMENTS = "documents";

    public static final String STATUS = "status";

    @SuppressWarnings("unchecked")
    @Override
    protected void executeFTPTask() throws IOException, ConnectorException {
        getFTPClient().setFileType(FTP.BINARY_FILE_TYPE);
        final Map<String, String> documents = (Map<String, String>) getInputParameter(DOCUMENTS, new HashMap<String, String>());
        final Map<String, Boolean> status = new HashMap<String, Boolean>();
        for (final Entry<String, String> document : documents.entrySet()) {
            final boolean done = uploadDocument(document);
            status.put(document.getKey(), done);
        }
        setOutputParameter(STATUS, status);
    }

    private ByteArrayInputStream getDocumentContent(final String documentName) throws ConnectorException {
        final ProcessAPI processAPI = getAPIAccessor().getProcessAPI();
        final long processInstanceId = getExecutionContext().getProcessInstanceId();
        try {
            final Document document = processAPI.getLastDocument(processInstanceId, documentName);
            if (document.hasContent()) {
                return new ByteArrayInputStream(processAPI.getDocumentContent(document.getContentStorageId()));
            } else {
                return new ByteArrayInputStream(new byte[0]);
            }
        } catch (final DocumentNotFoundException dnfe) {
            throw new ConnectorException(dnfe);
        }
    }

    private boolean uploadDocument(final Entry<String, String> document) throws ConnectorException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = getDocumentContent(document.getKey());
            return getFTPClient().storeFile(document.getValue(), inputStream);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

}