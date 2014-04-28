package org.pentaho.di.core.ftp;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import org.mockito.Mockito;
import org.pentaho.di.core.logging.LogChannelInterface;

public class MockFTPClient implements FTPCommonClient {

  LinkedList<String> deleted = new LinkedList<String>();
  LinkedList<RenamePair> renamed = new LinkedList<RenamePair>();
  public boolean connected = false;
  LinkedList<String> chdir = new LinkedList<String>();
  public FTPCommonFile[] dirContent = new FTPCommonFile[0];
  public LinkedList<String> chDir = new LinkedList<String>();

  public String[] dir;

  @Override
  public void delete( String fileName ) throws FTPCommonException {
    deleted.add( fileName );
  }

  @Override
  public void rename( String oldName, String newName ) throws FTPCommonException {
    renamed.add( new RenamePair( oldName, newName ) );
  }

  @Override
  public String system() throws FTPCommonException {
    return "Sega Mega Drive 2";
  }
  @Override
  public boolean connected() {
    return false;
  }
  @Override
  public String[] dir() throws FTPCommonException {
    return dir;
  }

  @Override
  public void chdir( String dir ) throws FTPCommonException {
    chDir.add( dir );
  }

  @Override
  public String pwd() throws FTPCommonException {
    return null;
  }

  @Override
  public void mkdir( String dir ) throws FTPCommonException {
    // test
  }

  @Override
  public FTPCommonFile[] getDirContent( String targetDir ) throws FTPCommonException {
    return dirContent;
  }

  @Override
  public void quit( LogChannelInterface log ) {
    // test
  }

  // strategy?
  @Override
  public void get( String localFileName, String remoteFileName ) throws FTPCommonException {
    FileWriter fw = null;
    try {
      Random r = new Random();
      fw = new FileWriter( localFileName );
      for ( int i = 0; i < 100; i++ ) {
        fw.append( (char) ( r.nextInt( 83 ) + 32 ) );
      }
      fw.flush();
      fw.close();
    } catch ( IOException e ) {
      throw new FTPCommonException( e );
    } finally {
      if ( fw != null ) {
        try {
          fw.close();
        } catch ( IOException e ) {
          // don't care
        }
      }
    }
  }

  @Override
  public boolean exists( String fileName ) throws FTPCommonException {
    return false;
  }

  @Override
  public void put( String localFilename, String remoteFileName ) throws FTPCommonException {
    // test
  }

  static class RenamePair {
    private String oldName;
    private String newName;
    RenamePair( String oldName, String newName ) {
      this.oldName = oldName;
      this.newName = newName;
    }
    public String getOldName() {
      return oldName;
    }
    public String getNewName() {
      return newName;
    }
  }
  
  public static FTPCommonFile[] getFTPCommonFiles ( int count, String namePrefix ) {
    FTPCommonFile[] arr = new MockFTPCommonFile[count]; 
    for ( int i = 0 ; i < count ; i++ ) {
      arr[i] = new MockFTPCommonFile( namePrefix + "_" + i );
    }
    return arr;
  }
}
