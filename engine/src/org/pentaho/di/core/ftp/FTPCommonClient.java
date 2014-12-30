package org.pentaho.di.core.ftp;

import java.io.OutputStream;

import org.pentaho.di.core.logging.LogChannelInterface;

/**
 * <p>Common interface to hide current FTP client implementation. All this methods 
 * should throw exception in case of not success result of any command execution.</p>
 * 
 * <p>This means for example if attempt to create remote directory is failed - implementation
 * must throw FTPCommonException instead of skip fail replay and void.</p>
 *
 */
public interface FTPCommonClient {

  /**
   * Delete file on FTP server.
   * 
   * @param fileName file name to delete
   * @throws FTPCommonException
   */
  void delete( String fileName ) throws FTPCommonException;

  /**
   * Renames a remote file.
   * 
   * @param oldName The name of the remote file to rename.
   * @param newName The new name of the remote file. 
   * @throws FTPCommonException
   */
  void rename( String oldName, String newName ) throws FTPCommonException;

  /**
   * convenient method to obtain system type from server.
   * 
   * @return
   * @throws FTPCommonException
   */
  String system() throws FTPCommonException;

  /**
   * Check if ftp client is connected to a server.
   * 
   * @return
   */
  boolean connected();

  /**
   * Obtain a list of filenames in the current working directory
   * 
   * @return
   * @throws FTPCommonException
   */
  String[] dir() throws FTPCommonException;

  /**
   * Change the current working directory of the FTP session.
   * 
   * @param dir
   * @throws FTPCommonException
   */
  void chdir( String dir ) throws FTPCommonException;

  /**
   * A convenience method to send the FTP PWD command to the server, receive the reply, and return the reply code.
   * (usually tests is client is connected)
   * 
   * @return
   * @throws FTPCommonException
   */
  String pwd() throws FTPCommonException;

  /**
   * 
   * @param dir
   * @throws FTPCommonException
   */
  void mkdir( String dir ) throws FTPCommonException;

  /**
   * Lists files in ftp directory. Returns array of files or empty array.
   * 
   * @param targetDir
   * @return
   * @throws FTPCommonException
   */
  FTPCommonFile[] getDirContent( String targetDir ) throws FTPCommonException;

  /**
   * Logout of the FTP server by sending the QUIT command, 
   * closes the connection to the FTP server, releases resources.
   * 
   * After call quite object can't be reused. 
   * NewFTP client can be obtained again only through factory methods.
   * 
   * @param log
   */
  void quit( LogChannelInterface log );

  /**
   * <p>Retrieves a named file from the server and writes it to the given OutputStream. 
   * This method implementation guarantee to <u>CLOSE the given OutputStream</u> in any case.</p>
   * 
   * @param out
   * @param remoteFileName
   * @throws FTPCommonException
   */
  void get( OutputStream out, String remoteFileName ) throws FTPCommonException;

  /**
   * Check is file exists. Different implementations can use different mechanisms. 
   * 
   * @param fileName
   * @return
   * @throws FTPCommonException
   */
  boolean exists( String fileName ) throws FTPCommonException;

  /**
   * Stores a file on the server using the given name and taking input from the given input file
   * 
   * @param localFilename the system-dependent local file name
   * @param remoteFileName
   * @throws FTPCommonException
   */
  void put( String localFilename, String remoteFileName ) throws FTPCommonException;

}
