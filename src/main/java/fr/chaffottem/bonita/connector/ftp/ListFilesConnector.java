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
import java.util.Arrays;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilters;

/**
 * @author Matthieu Chaffotte
 */
public class ListFilesConnector extends FTPClientConnector {

    public static final String PATHNAME = "pathname";

    public static final String FILES = "files";

    public static final String DIRECTORIES = "directories";

    public static final String FTP_FILES = "ftpFiles";

    @Override
    protected void executeFTPTask() throws IOException {
        final String pathname = (String) getInputParameter(PATHNAME);
        FTPFile[] files;
        if (pathname == null) {
            files = getFTPClient().listFiles();
        } else {
            files = getFTPClient().listFiles(pathname, FTPFileFilters.ALL);
        }
        setOutputParameter(FTP_FILES, Arrays.asList(files));
    }

}
