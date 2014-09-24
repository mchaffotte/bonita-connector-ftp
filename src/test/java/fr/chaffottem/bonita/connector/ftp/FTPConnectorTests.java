package fr.chaffottem.bonita.connector.ftp;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ListFilesConnectorTest.class,
        DeleteFilesConnectorTest.class,
        RemoveDirectoriesConnectorTest.class,
        MakeDirectoriesConnectorTest.class,
        UploadFilesConnectorTest.class,
        DownloadFilesConnectorTest.class
})
public class FTPConnectorTests {

}
