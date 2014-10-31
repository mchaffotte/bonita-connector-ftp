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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ListFilesConnectorTest.class,
    DeleteFilesConnectorTest.class,
    RemoveDirectoriesConnectorTest.class,
    CreateDirectoriesConnectorTest.class,
    UploadDocumentsToDirectoryConnectorTest.class,
    DownloadFilesToDocumentsConnectorTest.class,
    DownloadFilesToDirectoryConnectorTest.class,
    FTPSIT.class
})
public class FTPConnectorTests {

}
