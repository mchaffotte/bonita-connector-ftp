package fr.chaffottem.bonita.connector.ftp;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockftpserver.fake.filesystem.FileEntry;

public class DownloadFilesToDirectoryConnectorTest extends FTPClientConnectorTest {

    @Override
    public List<String> getServerDirecotries() {
        return Arrays.asList("c:\\share", "c:\\share\\images", "c:\\share\\docs");
    }

    @Override
    public Map<String, String> getServerFiles() {
        final Map<String, String> files = new HashMap<String, String>();
        files.put("c:\\share\\run.exe", "");
        files.put("c:\\share\\docs\\file1.txt", "qsfojsdfmljsgih");
        return files;
    }

    @Override
    public String getUserDirectory() {
        return "c:\\share";
    }

    @Override
    public FTPClientConnector getFTPClientConnector() {
        return new DownloadFilesToDirectoryConnector();
    }

    private byte[] getFileContent(final FileEntry file) throws IOException {
        final InputStream inputStream = file.createInputStream();
        try {
            return IOUtils.toByteArray(inputStream);
        } finally {
            inputStream.close();
        }
    }

    @Test
    public void downloadFiles() throws Exception {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(FTPClientConnector.HOSTNAME, HOSTNAME);
        parameters.put(FTPClientConnector.PORT, getListeningPort());
        parameters.put(FTPClientConnector.USER_NAME, USER_NAME);
        parameters.put(FTPClientConnector.PASSWORD, PASSWORD);
        parameters.put(FTPClientConnector.TRANSFER_TYPE, "ascii");
        final List<String> filePaths = new ArrayList<String>();
        filePaths.add("docs/file1.txt");
        filePaths.add("run.exe");
        parameters.put(DownloadFilesToDirectoryConnector.FILE_PATHS, filePaths);
        parameters.put(DownloadFilesToDirectoryConnector.DIRECTORY_PATH, "./target/local");

        execute(parameters);

        final File file = new File("./target/local/file1.txt");
        assertThat(file).exists();
        final File exeFile = new File("./target/local/run.exe");
        assertThat(exeFile).exists();
    }

}
