# BonitaBPM connectors: FTP

Enable BonitaBPM processes to interact with an FTP server.

## Build
Run the given command to build the project:

    mvn clean package

## Installation
Get zip files from the target folder and import them in the studio.

## Tests

###### Certificate creation
If the ftpserver.jks is out-of-date:

	keytool -genkey -alias ftptest -keyalg RSA -keystore ftpserver.jks -keysize 4096