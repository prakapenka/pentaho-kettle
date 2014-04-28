package org.pentaho.di.core.ftp;

import java.io.OutputStream;

import org.pentaho.di.core.logging.LogChannelInterface;


public interface FTPCommonClient {

  void delete( String fileName ) throws FTPCommonException;

  void rename( String oldName, String newName ) throws FTPCommonException;

  String system() throws FTPCommonException;

  boolean connected();

  String[] dir() throws FTPCommonException;

  void chdir( String dir ) throws FTPCommonException;

  String pwd() throws FTPCommonException;

  void mkdir( String dir ) throws FTPCommonException;

  FTPCommonFile[] getDirContent( String targetDir ) throws FTPCommonException;

  void quit( LogChannelInterface log );

  void get( OutputStream out, String remoteFileName ) throws FTPCommonException;

  boolean exists( String fileName ) throws FTPCommonException;

  void put( String localFilename, String remoteFileName ) throws FTPCommonException;

}
