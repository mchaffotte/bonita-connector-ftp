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
    UploadFilesConnectorTest.class,
    DownloadFilesConnectorTest.class,
    FTPSIT.class
})
public class FTPConnectorTests {

}
