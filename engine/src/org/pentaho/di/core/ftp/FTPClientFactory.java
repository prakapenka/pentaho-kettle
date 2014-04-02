package org.pentaho.di.core.ftp;

import java.io.IOException;

import org.pentaho.di.core.ftp.commons.CommonsFTPClient;
import org.pentaho.di.core.ftp.ftpedt.EdtFTPClient;
import org.pentaho.di.core.logging.LogChannelInterface;

import com.enterprisedt.net.ftp.FTPException;

public class FTPClientFactory {

  LogChannelInterface log;
  Class<?> PKG;

  public FTPClientFactory( LogChannelInterface log, Class<?> PKG ) {
    this.log = log;
    this.PKG = PKG;
  }

  // usually check connection method
  public FTPCommonClient getFtpClientConnected( FTPConnectionProperites info )
    throws FTPCommonException, FTPException, IOException {
    FTPImplementations imp = info.getImplementation();
    switch ( imp ) {
      case AUTO :
      case FTPEDT: {
        FTPCommonClient cl = EdtFTPClient.getConnectedClient( info, log, PKG );
        return cl;
      }
      case APACHE_CN: {
        FTPCommonClient cl = CommonsFTPClient.getConnectedClient( info, log, PKG );
        return cl;
      }
      default:
        throw new FTPCommonException( "not supported" );
    }

  }

  public FTPCommonClient getFtpClientInitialized( FTPConnectionProperites info )
    throws FTPCommonException, FTPException, IOException {
    FTPImplementations imp = info.getImplementation();
    switch ( imp ) {
      case AUTO:
      case FTPEDT: {
        EdtFTPClient cl =  (EdtFTPClient) getFtpClientConnected( info );
        cl.init( log, PKG, info.getVariableSpace() );
        return cl;
      }
      case APACHE_CN: {
        FTPCommonClient cl = CommonsFTPClient.getConnectedClient( info, log, PKG );
        return cl;
      }
      default:
        throw new FTPCommonException( "not supported" );
    }
  }
}
