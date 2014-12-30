package org.pentaho.di.core.ftp.ftpedt;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ftp.FTPCommonClient;
import org.pentaho.di.core.ftp.FTPCommonException;
import org.pentaho.di.core.ftp.FTPCommonFile;
import org.pentaho.di.core.ftp.FTPConnectionProperites;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.ftp.JobEntryFTP;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPFile;
import com.enterprisedt.net.ftp.FTPFileFactory;
import com.enterprisedt.net.ftp.FTPFileParser;
import com.enterprisedt.net.ftp.FTPMessageListener;
import com.enterprisedt.net.ftp.FTPTransferType;

public class EdtFTPClient implements FTPCommonClient {

  private static Class<?> PKG = JobEntryFTP.class;

  /**
   * This is workaround for old ftp edt implementation to prevent leak of username and password into JVM system
   * properties.
   */
  public static final String USERNAME = "java.net.socks.username";
  public static final String PASSWORD = "java.net.socks.password";

  private FTPClient ftpClient = null;

  public EdtFTPClient( FTPClient client ) {
    ftpClient = client;
  }

  private static boolean sockProxyModified = false;

  @Override
  public void delete( String fileName ) throws FTPCommonException {
    try {
      ftpClient.delete( fileName );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public void rename( String oldName, String newName ) throws FTPCommonException {
    try {
      ftpClient.rename( oldName, newName );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public String system() throws FTPCommonException {
    try {
      return ftpClient.system();
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public boolean connected() {
    return ftpClient.connected();
  }

  @Override
  public void chdir( String dir ) throws FTPCommonException {
    try {
      ftpClient.chdir( dir );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public String pwd() throws FTPCommonException {
    try {
      return ftpClient.pwd();
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public void mkdir( String dir ) throws FTPCommonException {
    try {
      ftpClient.mkdir( dir );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public FTPCommonFile[] getDirContent( String dir ) throws FTPCommonException {
    try {
      FTPFile[] files = ftpClient.dirDetails( dir );

      return convertFilesToCommon( files );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public void quit( LogChannelInterface log ) {
    try {
      ftpClient.quit();
      if ( sockProxyModified ) {
        FTPClient.clearSOCKS();
        // also do not leak username and password in
        // java system properties ;)
        Properties prop = System.getProperties();
        prop.remove( USERNAME );
        prop.remove( PASSWORD );
        System.setProperties( prop );
      }
    } catch ( Exception ex ) {
      if ( log != null && log.isError() ) {
        log.logError( BaseMessages.getString( PKG, "JobEntryFTP.ErrorQuitting" ), ex );
      }
    }
  }

  @Override
  public void get( OutputStream os, String remoteFileName ) throws FTPCommonException {
    try {
      ftpClient.get( os, remoteFileName );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    } finally {
      if ( os != null ) {
        try {
          os.close();
        } catch ( IOException ex ) {
          // we don't care
        }
      }
    }
  }

  public FTPClient getFtpClient() {
    return ftpClient;
  }

  public static EdtFTPClient getConnectedClient( FTPConnectionProperites info, LogChannelInterface log )
    throws FTPCommonException {

    PDIFTPClient pdiCl = new PDIFTPClient( log );
    try {
      pdiCl.setRemoteAddr( InetAddress.getByName( info.getServerName() ) );
      pdiCl.setRemotePort( info.getPort() );

      if ( info.hasProxy() ) {
        throw new FTPCommonException( "HTTP proxy is not supported for FTPEdt implementation. Use "
            + "Apache commons instead." );
      }
      //
      if ( info.hasProxy() ) {
        // the only option for http proxy if we will support them anytime
        pdiCl.setConnectMode( FTPConnectMode.PASV );
        BaseMessages.getString( PKG, "JobEntryFTP.SetPassive" );
      } else {
        pdiCl.setConnectMode( info.isActiveConnection() ? FTPConnectMode.ACTIVE : FTPConnectMode.PASV );
        logDetailed( log, info.isActiveConnection() ? BaseMessages.getString( PKG, "JobEntryFTP.SetActive" )
            : BaseMessages.getString( PKG, "JobEntryFTP.SetPassive" ) );
      }

      pdiCl.setTimeout( info.getTimeout() );
      logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.SetTimeout", String.valueOf( info.getTimeout() ) ) );

      pdiCl.setControlEncoding( info.getControlEncoding() );
      logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.SetEncoding", info.getControlEncoding() ) );

      if ( info.hasSOCKSProxy() ) {
        // NOTE: for edtftp this is just setting environment properties for whole JVM
        // disregard to thread local
        sockProxyModified = true;
        FTPClient.initSOCKS( String.valueOf( info.getSocksProxyPort() ), info.getSocksProxyHost() );
        String sockUser = info.getSocksProxyUsername();
        String sockPassw = info.getSocksProxyPassword();
        if ( !Const.isEmpty( sockUser ) && !Const.isEmpty( sockPassw ) ) {
          FTPClient.initSOCKSAuthentication( sockUser, sockPassw );
        }
      }
      if ( log.isRowLevel() ) {
        pdiCl.setMessageListener( new LocalFTPMessageListener( log ) );
      }

      pdiCl.connect();

      String userName = info.getUserName();
      pdiCl.login( userName, info.getPassword() );
      logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.LoggedIn", userName ) );

      // set transfer type after client is connected
      pdiCl.setType( info.isBinaryMode() ? FTPTransferType.BINARY : FTPTransferType.ASCII );
      logDetailed( log, info.isBinaryMode() ? BaseMessages.getString( PKG, "JobEntryFTP.SetBinary" ) : BaseMessages
          .getString( PKG, "JobEntryFTP.SetAscii" ) );

    } catch ( Exception e ) {
      // Something goes wrong
      try {
        pdiCl.quit();
      } catch ( Exception ee ) {
        // we don't care for this one
      }
      // do not suppress root exception!
      throw new FTPCommonException( e );
    }
    EdtFTPClient client = new EdtFTPClient( pdiCl );
    return client;
  }

  static class LocalFTPMessageListener implements FTPMessageListener {
    private LogChannelInterface log;

    LocalFTPMessageListener( LogChannelInterface log ) {
      this.log = log;
    }

    @Override
    public void logCommand( String cmd ) {
      log.logRowlevel( cmd );
    }

    @Override
    public void logReply( String reply ) {
      log.logRowlevel( reply );
    }
  }

  private static void logDetailed( LogChannelInterface log, String string ) {
    if ( log != null && log.isDetailed() ) {
      log.logDetailed( string );
    }
  }

  public void init( LogChannelInterface log, VariableSpace vs ) throws FTPCommonException {
    try {
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Hooking.Parsers" ) );
      }
      String system = ftpClient.system();
      MVSFileParser parser = new MVSFileParser( log );
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Created.MVS.Parser" ) );
      }
      FTPFileFactory factory = new FTPFileFactory( system );
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Created.Factory" ) );
      }
      factory.addParser( parser );
      ftpClient.setFTPFileFactory( factory );
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Get.Variable.Space" ) );
      }
      if ( vs != null ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Getting.Other.Parsers" ) );
        }
        String otherParserNames = vs.getVariable( "ftp.file.parser.class.names" );
        if ( otherParserNames != null ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Creating.Parsers" ) );
          }
          String[] parserClasses = otherParserNames.split( "|" );
          String cName = null;
          Class<?> clazz = null;
          Object parserInstance = null;
          for ( int i = 0; i < parserClasses.length; i++ ) {
            cName = parserClasses[i].trim();
            if ( cName.length() > 0 ) {
              try {
                clazz = Class.forName( cName );
                parserInstance = clazz.newInstance();
                if ( parserInstance instanceof FTPFileParser ) {
                  if ( log.isDetailed() ) {
                    log.logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.DEBUG.Created.Other.Parser", cName ) );
                  }
                  factory.addParser( (FTPFileParser) parserInstance );
                }
              } catch ( Exception ignored ) {
                if ( log.isDebug() ) {
                  ignored.printStackTrace();
                  log.logError( BaseMessages.getString( PKG, "JobEntryFTP.ERROR.Creating.Parser", cName ) );
                }
              }
            }
          }
        }
      }
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public String[] dir() throws FTPCommonException {
    try {
      return ftpClient.dir();
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public boolean exists( String fileName ) throws FTPCommonException {
    try {
      return ftpClient.exists( fileName );
    } catch ( Exception e ) {
      throw new FTPCommonException( e );
    }
  }

  @Override
  public void put( String localFilename, String remoteFileName ) throws FTPCommonException {
    try {
      ftpClient.put( localFilename, remoteFileName );
    } catch ( Exception e ) {
      throw new FTPCommonException( e );
    }
  }

  protected FTPCommonFile[] convertFilesToCommon( FTPFile[] files ) {
    FTPCommonFile[] ret = new FTPCommonFile[( files == null || files.length == 0 ) ? 0 : files.length];
    if ( ret.length > 0 ) {
      for ( int i = 0; i < files.length; i++ ) {
        ret[i] = new FTPCommonFileAdapter( files[i] );
      }
    }
    return ret;
  }

  protected static class FTPCommonFileAdapter implements FTPCommonFile {
    private FTPFile file = null;

    public FTPCommonFileAdapter( FTPFile file ) {
      this.file = file;
    }

    @Override
    public boolean isDir() {
      return file.isDir();
    }

    @Override
    public boolean isFile() {
      return file.isFile();
    }

    @Override
    public boolean isLink() {
      return file.isLink();
    }

    @Override
    public String getName() {
      return file.getName();
    }

    @Override
    public Date getDateCreated() {
      return file.created();
    }

    @Override
    public Date getDateModified() {
      return file.lastModified();
    }
  }
}