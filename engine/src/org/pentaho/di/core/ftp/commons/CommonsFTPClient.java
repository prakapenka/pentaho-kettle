package org.pentaho.di.core.ftp.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.net.SocketFactory;

import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ftp.FTPCommonClient;
import org.pentaho.di.core.ftp.FTPCommonException;
import org.pentaho.di.core.ftp.FTPCommonFile;
import org.pentaho.di.core.ftp.FTPConnectionProperites;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.ftp.JobEntryFTP;

/**
 * Apache-commons-net compliant implementation. Requires availability of apache-commons-net version 3.3+
 *
 */
public class CommonsFTPClient implements FTPCommonClient {

  private static Class<?> PKG = JobEntryFTP.class;

  private FTPClient ftpClient;

  public CommonsFTPClient( FTPClient ftpClient ) {
    this.ftpClient = ftpClient;
  }

  @Override
  public void delete( String fileName ) throws FTPCommonException {
    try {
      ftpClient.dele( fileName );
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
      return this.ftpClient.getSystemType();
    } catch ( IOException e ) {
      throw new FTPCommonException( e );
    }
  }

  @Override
  public boolean connected() {
    return ftpClient.isConnected();
  }

  @Override
  public String[] dir() throws FTPCommonException {
    try {
      return ftpClient.listNames();
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public void chdir( String dir ) throws FTPCommonException {
    try {
      ftpClient.changeWorkingDirectory( dir );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public String pwd() throws FTPCommonException {
    try {
      return Integer.toString( ftpClient.pwd() );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public void mkdir( String dir ) throws FTPCommonException {
    try {
      ftpClient.mkd( dir );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public FTPCommonFile[] getDirContent( String targetDir ) throws FTPCommonException {
    FTPFile[] files = null;
    try {
      if ( targetDir == null ) {
        files = ftpClient.listFiles();
      } else {
        files = ftpClient.listFiles( targetDir );
      }
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
    FTPCommonFile[] ret = new FTPCommonFile[files == null || files.length == 0 ? 0 : files.length];
    for ( int i = 0; i < ret.length; i++ ) {
      ret[i] = new FTPHTTPFile( files[i] );
    }
    return ret;
  }

  @Override
  public void quit( LogChannelInterface log ) {
    try {
      ftpClient.logout();
    } catch ( IOException ex ) {
      // network is down but server keeps connection open
      logError( log, "Logout failed", ex );
    } finally {
      try {
        ftpClient.disconnect();
      } catch ( Exception ex ) {
        // some how we may leave opened sockets...
        logError( log, "Disconnection failed", ex );
      }
    }
  }

  @Override
  public void get( OutputStream os, String remoteFileName ) throws FTPCommonException {
    try {
      ftpClient.retrieveFile( remoteFileName, os );
    } catch ( Exception ex ) {
      // this is the case when target directory not found
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

  /**
   * This is convenient implementation with FTPEdt. Generally speaking we don't know is it a directory or a file.
   * 
   * So First we will check SIZE, then MDMT, finally iterate through files to find by name.
   * 
   * Exists do search in current directory only.
   * 
   */
  @Override
  public boolean exists( final String fileName ) throws FTPCommonException {
    try {
      boolean features = ftpClient.features();

      // so we can use SIZE?
      if ( features && ftpClient.hasFeature( "SIZE" ) ) {
        int replay = ftpClient.sendCommand( "SIZE", "fileName" );
        return FTPReply.isPositiveCompletion( replay );
      }

      // can we use MDMT?
      if ( features && ftpClient.hasFeature( "MDMT" ) ) {
        int replay = ftpClient.sendCommand( "MDMT", "fileName" );
        return FTPReply.isPositiveCompletion( replay );
      }

      // ok: LIST is our last resort.
      FTPListParseEngine engine = ftpClient.initiateListParsing( "." );
      FTPFileFilter filter = new FTPFileFilter() {
        @Override
        public boolean accept( FTPFile file ) {
          return file.getName().equals( fileName );
        }
      };
      FTPFile[] files = engine.getFiles( filter );
      return files.length > 0;
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    }
  }

  @Override
  public void put( String localFilename, String remoteFileName ) throws FTPCommonException {
    InputStream input = null;
    try {
      input = new FileInputStream( localFilename );
      ftpClient.storeFile( remoteFileName, input );
    } catch ( Exception ex ) {
      throw new FTPCommonException( ex );
    } finally {
      if ( input != null ) {
        try {
          input.close();
        } catch ( Exception ex ) {
          // oops!
        }
      }
    }
  }

  public static CommonsFTPClient getConnectedClient( FTPConnectionProperites info, LogChannelInterface log )
    throws FTPCommonException {

    // we assume to have one or anther implementation depending on
    // do we need some http proxy available.
    FTPClient ftp = null;

    // we do not support connection to http proxy over socks proxy.
    if ( info.hasProxy() && info.hasSOCKSProxy() ) {
      logError( log, "Connection to http through socks is not supported", null );
      return null;
    }

    if ( info.hasProxy() ) {
      if ( !Const.isEmpty( info.getProxyUsername() ) ) {
        ftp =
            new FTPHTTPClient( info.getProxyHost(), info.getProxyPort(), info.getProxyUsername(), info
                .getProxyPassword() );
      } else {
        ftp = new FTPHTTPClient( info.getProxyHost(), info.getProxyPort() );
      }
      logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.OpenedProxyConnectionOn", info.getProxyHost() ) );
    } else {
      ftp = new FTPClient();
    }

    if ( log.isRowLevel() ) {
      ProtocolCommandListener local = new RowLevelProtocolCommandListener( log );
      ftp.addProtocolCommandListener( local );
    }

    // Sock proxy section
    Proxy proxy = null;
    if ( info.hasSOCKSProxy() ) {
      SocketAddress sock = new InetSocketAddress( info.getSocksProxyHost(), info.getSocksProxyPort() );
      proxy = new Proxy( Proxy.Type.SOCKS, sock );
      if ( !Const.isEmpty( info.getSocksProxyUsername() ) ) {
        // we need to provide authentication.
        // this is thread isolated code, see implementation
        LocalAuthentificator auth = LocalAuthentificator.getInstance();
        auth.setCredentials( info.getSocksProxyUsername(), info.getSocksProxyPassword() );
        Authenticator.setDefault( auth );
      }
    }

    ftp.setConnectTimeout( info.getTimeout() );

    // show all to be compatible with previous
    ftp.setListHiddenFiles( true );
    // socket factory: we use same connection timeout.
    ftp.setSocketFactory( proxy == null ? new LocalSocketFactory( info.getTimeout() ) : new LocalSocketFactory( proxy,
        info.getTimeout() ) );
    logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.SetTimeout", String.valueOf( info.getTimeout() ) ) );

    try {
      if ( info.getPort() <= 0 ) {
        ftp.connect( info.getServerName() );
      } else {
        ftp.connect( info.getServerName(), info.getPort() );
      }

      // active or passive?
      if ( info.hasProxy() || info.hasSOCKSProxy() ) {
        // for proxy connections only passive
        ftp.enterLocalPassiveMode();
        logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.SetPassive" ) );
      } else {
        if ( info.isActiveConnection() ) {
          ftp.enterLocalActiveMode();
          logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.SetActive" ) );
        } else {
          ftp.enterLocalPassiveMode();
          logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.SetPassive" ) );
        }
      }

      int reply = ftp.getReplyCode();
      if ( !FTPReply.isPositiveCompletion( reply ) ) {
        // usually it is failed login
        throw new FTPCommonException( "Can't connect to: " + info.getServerName() );
      }

      // active or passive?
      if ( info.hasProxy() || info.hasSOCKSProxy() ) {
        // this is workaround after success connection
        ftp.enterLocalPassiveMode();
      }

      ftp.setControlEncoding( info.getControlEncoding() );

      ftp.setFileType( info.isBinaryMode() ? FTP.BINARY_FILE_TYPE : FTP.ASCII_FILE_TYPE );
      logDetailed( log, info.isBinaryMode() ? BaseMessages.getString( PKG, "JobEntryFTP.SetBinary" ) : BaseMessages
          .getString( PKG, "JobEntryFTP.SetAscii" ) );

      ftp.login( info.getUserName(), info.getPassword() );

      logDetailed( log, BaseMessages.getString( PKG, "JobEntryFTP.LoggedIn", info.getUserName() ) );
    } catch ( Exception e ) {
      // for example we have passed proxy but failed login to real server
      if ( ftp != null ) {
        try {
          ftp.disconnect();
        } catch ( IOException io ) {
          // we don't care for this case
        }
      }
      throw new FTPCommonException( e );
    }
    CommonsFTPClient cClient = new CommonsFTPClient( ftp );
    return cClient;
  }

  static class RowLevelProtocolCommandListener implements ProtocolCommandListener {
    private LogChannelInterface log;

    RowLevelProtocolCommandListener( LogChannelInterface log ) {
      this.log = log;
    }

    @Override
    public void protocolCommandSent( ProtocolCommandEvent event ) {
      log.logRowlevel( "---> " + event.getCommand() );
    }

    @Override
    public void protocolReplyReceived( ProtocolCommandEvent event ) {
      log.logRowlevel( event.getMessage() );
    }
  }

  /**
   * Local socket factory support timeouts do not hang out for calls to unaccessible servers.
   * 
   */
  static class LocalSocketFactory extends SocketFactory {
    private final Proxy connProxy;
    private int timeout;

    public LocalSocketFactory( int timeout ) {
      this( null, timeout );
    }

    public LocalSocketFactory( Proxy proxy, int timeout ) {
      connProxy = proxy;
      this.timeout = timeout;
    }

    @Override
    public Socket createSocket() throws IOException {
      Socket sc = null;
      if ( connProxy != null ) {
        sc = new Socket( connProxy );
      } else {
        sc = new Socket();
      }
      sc.setSoTimeout( timeout );
      return sc;
    }

    @Override
    public Socket createSocket( String host, int port ) throws UnknownHostException, IOException {
      if ( connProxy != null ) {
        Socket s = new Socket( connProxy );
        s.connect( new InetSocketAddress( host, port ) );
        return s;
      }
      Socket sc = new Socket( host, port );
      sc.setSoTimeout( timeout );
      return sc;
    }

    @Override
    public Socket createSocket( InetAddress address, int port ) throws IOException {
      if ( connProxy != null ) {
        Socket s = new Socket( connProxy );
        s.connect( new InetSocketAddress( address, port ) );
        return s;
      }
      Socket sc = new Socket( address, port );
      sc.setSoTimeout( timeout );
      return sc;
    }

    @Override
    public Socket createSocket( String host, int port, InetAddress localAddr, int localPort )
      throws UnknownHostException, IOException {
      if ( connProxy != null ) {
        Socket s = new Socket( connProxy );
        s.bind( new InetSocketAddress( localAddr, localPort ) );
        s.connect( new InetSocketAddress( host, port ) );
        return s;
      }
      Socket sc = new Socket( host, port, localAddr, localPort );
      sc.setSoTimeout( timeout );
      return sc;
    }

    @Override
    public Socket createSocket( InetAddress address, int port, InetAddress localAddr, int localPort )
      throws IOException {
      if ( connProxy != null ) {
        Socket s = new Socket( connProxy );
        s.bind( new InetSocketAddress( localAddr, localPort ) );
        s.connect( new InetSocketAddress( address, port ) );
        return s;
      }
      Socket sc = new Socket( address, port, localAddr, localPort );
      sc.setSoTimeout( timeout );
      return sc;
    }

    public ServerSocket createServerSocket( int port ) throws IOException {
      ServerSocket sc = new ServerSocket( port );
      sc.setSoTimeout( timeout );
      return sc;
    }

    public ServerSocket createServerSocket( int port, int backlog ) throws IOException {
      ServerSocket sc = new ServerSocket( port, backlog );
      sc.setSoTimeout( timeout );
      return sc;
    }

    public ServerSocket createServerSocket( int port, int backlog, InetAddress bindAddr ) throws IOException {
      ServerSocket sc = new ServerSocket( port, backlog, bindAddr );
      sc.setSoTimeout( timeout );
      return sc;
    }
  }

  public static class FTPHTTPFile implements FTPCommonFile {
    private FTPFile file;

    public FTPHTTPFile( FTPFile file ) {
      this.file = file;
    }

    @Override
    public boolean isDir() {
      return file.isDirectory();
    }

    @Override
    public boolean isFile() {
      return file.isFile();
    }

    @Override
    public boolean isLink() {
      return file.isSymbolicLink();
    }

    @Override
    public String getName() {
      return file.getName();
    }

    @Override
    public Date getDateCreated() {
      return file.getTimestamp().getTime();
    }

    @Override
    public Date getDateModified() {
      return file.getTimestamp().getTime();
    }
  }

  private static void logDetailed( LogChannelInterface log, String string ) {
    if ( log != null && log.isDetailed() ) {
      log.logDetailed( string );
    }
  }

  private static void logError( LogChannelInterface log, String string, Throwable e ) {
    if ( log != null ) {
      if ( e != null ) {
        log.logError( string, e );
      } else {
        log.logError( string );
      }
    }
  }
}
