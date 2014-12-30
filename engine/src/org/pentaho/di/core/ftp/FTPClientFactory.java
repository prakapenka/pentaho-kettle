package org.pentaho.di.core.ftp;

import org.pentaho.di.core.ftp.commons.CommonsFTPClient;
import org.pentaho.di.core.ftp.ftpedt.EdtFTPClient;
import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * <p>This is a common factory to retrieve FTP client depends on necessary implementation. List of known
 * implementations is maintained by enum FTPImplementations.</p>
 * <p>Historically there was 2 ways to deal with FTP clients:
 * <ol><li>check if client is connected according to credentials on UI
 * <li>working with client with job entry on execute() phase
 * </ol>
 * The difference is that for second case it is critical to have log outputs during execution.</p>
 * <p>
 * Every method will return connected/initialized client ready for action, or null.
 * So it is caller responsibility to check for null's - if returned client is null it means
 * it was unable to connect according to network or credentials reasons or any other.</p>
 * 
 * <p>In future it may be possible to force always track connection status in console. The question is
 * UI callers must supply correct logging interface.</p>
 * 
 * <p>Every implementation supports log for FTP commands for debug phase. That means log every FTP responses.</p>
 *
 */
public class FTPClientFactory {

  LogChannelInterface log;
  Class<?> PKG;

  public FTPClientFactory( LogChannelInterface log ) {
    this.log = log;
  }

  /**
   * This method is usually called when connection should be just checked using UI
   * functionality. For this case not necessary to redirect log output to console.
   * 
   * 
   * @param info
   * @return
   * @throws FTPCommonException
   */
  public FTPCommonClient getFtpClientConnected( FTPConnectionProperites info )
    throws FTPCommonException {
    FTPImplementations imp = info.getImplementation();
    switch ( imp ) {
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

  /**
   * This is convenient method to get FTP client ready to be used for job entries get/put operations.
   * The difference from connected call is that it is guaranteed that log output will be
   * redirected to console if necessary.
   * 
   * @param info
   * @return
   * @throws FTPCommonException
   */
  public FTPCommonClient getFtpClientInitialized( FTPConnectionProperites info )
    throws FTPCommonException {
    FTPImplementations imp = info.getImplementation();
    switch ( imp ) {
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
