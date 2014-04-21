package org.pentaho.di.core.ftp;

import org.pentaho.di.core.ftp.commons.CommonsFTPClient;
import org.pentaho.di.core.ftp.ftpedt.EdtFTPClient;
import org.pentaho.di.core.logging.LogChannelInterface;

public class FTPClientFactory {

  LogChannelInterface log;
  Class<?> PKG;

  public FTPClientFactory( LogChannelInterface log ) {
    this.log = log;
  }

  // usually check connection method
  public FTPCommonClient getFtpClientConnected( FTPConnectionProperites info )
    throws FTPCommonException {
    FTPImplementations imp = info.getImplementation();
    switch ( imp ) {
      case AUTO :
      case FTPEDT: {
        FTPCommonClient cl = EdtFTPClient.getConnectedClient( info, log );
        return cl;
      }
      case APACHE_CN: {
        FTPCommonClient cl = CommonsFTPClient.getConnectedClient( info, log );
        return cl;
      }
      default:
        throw new FTPCommonException( "not supported" );
    }

  }

  public FTPCommonClient getFtpClientInitialized( FTPConnectionProperites info )
    throws FTPCommonException {
    FTPImplementations imp = info.getImplementation();
    switch ( imp ) {
      case AUTO:
      case FTPEDT: {
        EdtFTPClient cl =  (EdtFTPClient) getFtpClientConnected( info );
        cl.init( log, info.getVariableSpace() );
        return cl;
      }
      case APACHE_CN: {
        FTPCommonClient cl = CommonsFTPClient.getConnectedClient( info, log );
        return cl;
      }
      default:
        throw new FTPCommonException( "not supported" );
    }
  }
}
