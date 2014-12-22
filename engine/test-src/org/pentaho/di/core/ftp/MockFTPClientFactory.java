package org.pentaho.di.core.ftp;

public class MockFTPClientFactory extends FTPClientFactory {

  FTPCommonClient client = new MockFTPClient();

  public MockFTPClientFactory() {
    super( null );
  }

  @Override
  public FTPCommonClient getFtpClientConnected( FTPConnectionProperites info ) throws FTPCommonException {
    return client;
  }

  @Override
  public FTPCommonClient getFtpClientInitialized( FTPConnectionProperites info ) throws FTPCommonException {
    return client;
  }

  public void setFTPCommonClient( FTPCommonClient client ) {
    this.client = client;
  }
}
