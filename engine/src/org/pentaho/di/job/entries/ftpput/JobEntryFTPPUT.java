/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job.entries.ftpput;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.ftp.FTPClientFactory;
import org.pentaho.di.core.ftp.FTPCommonClient;
import org.pentaho.di.core.ftp.FTPConnectionProperites;
import org.pentaho.di.core.ftp.FTPImplementations;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines an FTP put job entry.
 * 
 * @author Samatar
 * @since 15-09-2007
 * 
 */

public class JobEntryFTPPUT extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryFTPPUT.class; // for i18n purposes, needed by Translator2!!

  private FTPConnectionProperites connectionProperties;
  private String remoteDirectory;
  private String localDirectory;
  private String wildcard;
  //TODO check all binary mode calls
  private boolean remove;
  private boolean onlyPuttingNewFiles; /* Don't overwrite files */

  /**
   * Implicit encoding used before PDI v2.4.1
   */
  private static String LEGACY_CONTROL_ENCODING = "US-ASCII";

  /**
   * Default encoding when making a new ftp job entry instance.
   */
  private static String DEFAULT_CONTROL_ENCODING = "ISO-8859-1";

  public JobEntryFTPPUT( String n ) {
    super( n, "" );
    connectionProperties = new FTPConnectionProperites();
    connectionProperties.setPort( "21" );
    connectionProperties.setSocksProxyPort( "1080" );
    remoteDirectory = null;
    localDirectory = null;
    connectionProperties.setControlEncoding( DEFAULT_CONTROL_ENCODING );
  }

  public JobEntryFTPPUT() {
    this( "" );
  }

  public Object clone() {
    JobEntryFTPPUT je = (JobEntryFTPPUT) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 200 );
    VariableSpace var = connectionProperties.getVariableSpace();
    connectionProperties.setVariableSpace( null );

    retval.append( super.getXML() );

    retval.append( "      " ).append( XMLHandler.addTagValue( "library", connectionProperties.getImplementation().toString() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", connectionProperties.getServerName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "serverport", connectionProperties.getPort() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", connectionProperties.getUserName() ) );
    retval.append( "      " ).append(
        XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables(
            connectionProperties.getPassword() ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "remoteDirectory", remoteDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "localDirectory", localDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wildcard", wildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "binary", connectionProperties.isBinaryMode() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", connectionProperties.getTimeout() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "remove", remove ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "only_new", onlyPuttingNewFiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "active", connectionProperties.isActiveConnection() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "control_encoding",
        connectionProperties.getControlEncoding() ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_host", connectionProperties.getProxyHost() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_port", connectionProperties.getProxyPort() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_username", connectionProperties.getProxyUsername() ) );
    retval.append( "      " ).append(
        XMLHandler.addTagValue( "proxy_password", Encr.encryptPasswordIfNotUsingVariables(
            connectionProperties.getProxyPassword() ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_host",
        connectionProperties.getSocksProxyHost() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_port",
        connectionProperties.getSocksProxyPort() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_username",
        connectionProperties.getSocksProxyUsername() ) );
    retval.append( "      " ).append(
        XMLHandler.addTagValue( "socksproxy_password", Encr.encryptPasswordIfNotUsingVariables(
            connectionProperties.getSocksProxyPassword() ) ) );

    connectionProperties.setVariableSpace( var );
    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );

      String implementation = XMLHandler.getTagValue( entrynode, "library" );
      connectionProperties.setImplementation( implementation == null ?
          FTPImplementations.FTPEDT : FTPImplementations.getByCode( implementation ) );
      connectionProperties.setServerName( XMLHandler.getTagValue( entrynode, "servername" ) );
      connectionProperties.setPort( XMLHandler.getTagValue( entrynode, "serverport" ) );
      connectionProperties.setUserName( XMLHandler.getTagValue( entrynode, "username" ) );
      connectionProperties.setPassword( Encr.decryptPasswordOptionallyEncrypted(
          XMLHandler.getTagValue( entrynode, "password" ) ) );
      remoteDirectory = XMLHandler.getTagValue( entrynode, "remoteDirectory" );
      localDirectory = XMLHandler.getTagValue( entrynode, "localDirectory" );
      wildcard = XMLHandler.getTagValue( entrynode, "wildcard" );
      connectionProperties.setBinaryMode( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "binary" ) ) );
      connectionProperties.setTimeout( Const.toInt( XMLHandler.getTagValue( entrynode, "timeout" ), 10000 ) );
      remove = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "remove" ) );
      onlyPuttingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "only_new" ) );
      connectionProperties.setActiveConnection( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "active" ) ) );
      connectionProperties.setControlEncoding( XMLHandler.getTagValue( entrynode, "control_encoding" ) );

      connectionProperties.setProxyHost( XMLHandler.getTagValue( entrynode, "proxy_host" ) );
      connectionProperties.setProxyPort( XMLHandler.getTagValue( entrynode, "proxy_port" ) );
      connectionProperties.setProxyUsername( XMLHandler.getTagValue( entrynode, "proxy_username" ) );
      connectionProperties.setProxyPassword(
            Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "proxy_password" ) ) );
      connectionProperties.setSocksProxyHost( XMLHandler.getTagValue( entrynode, "socksproxy_host" ) );
      connectionProperties.setSocksProxyPort( XMLHandler.getTagValue( entrynode, "socksproxy_port" ) );
      connectionProperties.setSocksProxyUsername( XMLHandler.getTagValue( entrynode, "socksproxy_username" ) );
      connectionProperties.setSocksProxyPassword(
          Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "socksproxy_password" ) ) );

      //TODO what happening with encoding?
      if ( connectionProperties.getControlEncoding() == null ) {
        // if we couldn't retrieve an encoding, assume it's an old instance and
        // put in the the encoding used before v 2.4.0
        connectionProperties.setControlEncoding( LEGACY_CONTROL_ENCODING );
      }
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobFTPPUT.Log.UnableToLoadFromXml" ), xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers ) throws KettleException {
    try {
      String implementation = rep.getJobEntryAttributeString( id_jobentry, "library" );
      connectionProperties.setImplementation( implementation == null ?
          FTPImplementations.FTPEDT : FTPImplementations.getByCode( implementation ) );      
      connectionProperties.setServerName( rep.getJobEntryAttributeString( id_jobentry, "servername" ) );
      // backward compatible.
      String value =  rep.getJobEntryAttributeString( id_jobentry, "serverport" );
      int intServerPort = (int) rep.getJobEntryAttributeInteger( id_jobentry, "serverport" );
      if ( intServerPort > 0 && Const.isEmpty( value ) ) {
        connectionProperties.setPort( Integer.toString( intServerPort ) );
      } else {
        connectionProperties.setPort( value );
      }
      connectionProperties.setUserName( rep.getJobEntryAttributeString( id_jobentry, "username" ) );
      connectionProperties.setPassword(
            Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) ) );
      remoteDirectory = rep.getJobEntryAttributeString( id_jobentry, "remoteDirectory" );
      localDirectory = rep.getJobEntryAttributeString( id_jobentry, "localDirectory" );
      wildcard = rep.getJobEntryAttributeString( id_jobentry, "wildcard" );
      connectionProperties.setBinaryMode( rep.getJobEntryAttributeBoolean( id_jobentry, "binary" ) );
      connectionProperties.setTimeout( (int) rep.getJobEntryAttributeInteger( id_jobentry, "timeout" ) );
      remove = rep.getJobEntryAttributeBoolean( id_jobentry, "remove" );
      onlyPuttingNewFiles = rep.getJobEntryAttributeBoolean( id_jobentry, "only_new" );
      connectionProperties.setActiveConnection( rep.getJobEntryAttributeBoolean( id_jobentry, "active" ) );
      value = rep.getJobEntryAttributeString( id_jobentry, "control_encoding" );
      // if we couldn't retrieve an encoding, assume it's an old instance and
      // put in the the encoding used before v 2.4.0
      connectionProperties.setControlEncoding( value == null ? LEGACY_CONTROL_ENCODING : value );

      connectionProperties.setProxyHost( rep.getJobEntryAttributeString( id_jobentry, "proxy_host" ) );
      connectionProperties.setProxyPort( rep.getJobEntryAttributeString( id_jobentry, "proxy_port" ) );
      connectionProperties.setProxyUsername( rep.getJobEntryAttributeString( id_jobentry, "proxy_username" ) );
      connectionProperties.setProxyPassword(
          Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "proxy_password" ) ) );
      connectionProperties.setSocksProxyHost( rep.getJobEntryAttributeString( id_jobentry, "socksproxy_host" ) );
      connectionProperties.setSocksProxyPort( rep.getJobEntryAttributeString( id_jobentry, "socksproxy_port" ) );
      connectionProperties.setSocksProxyUsername(
          rep.getJobEntryAttributeString( id_jobentry, "socksproxy_username" ) );
      connectionProperties.setSocksProxyPassword( Encr.decryptPasswordOptionallyEncrypted(
          rep.getJobEntryAttributeString( id_jobentry, "socksproxy_password" ) ) );
    } catch ( KettleException dbe ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobFTPPUT.UnableToLoadFromRepo", String
          .valueOf( id_jobentry ) ), dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    VariableSpace var = connectionProperties.getVariableSpace();
    connectionProperties.setVariableSpace( null );

    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "library", connectionProperties.getImplementation().toString() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", connectionProperties.getServerName() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "serverport", connectionProperties.getPort() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "username", connectionProperties.getUserName() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password",
          Encr.encryptPasswordIfNotUsingVariables( connectionProperties.getPassword() ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "remoteDirectory", remoteDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "localDirectory", localDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wildcard", wildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "binary", connectionProperties.isBinaryMode() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", connectionProperties.getTimeout() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "remove", remove );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "only_new", onlyPuttingNewFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "active", connectionProperties.isActiveConnection() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "control_encoding", connectionProperties.getControlEncoding() );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_host", connectionProperties.getProxyHost() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_port", connectionProperties.getProxyPort() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_username", connectionProperties.getProxyUsername() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxy_password", Encr
          .encryptPasswordIfNotUsingVariables( connectionProperties.getProxyPassword() ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_host", connectionProperties.getSocksProxyHost() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_port", connectionProperties.getSocksProxyPort() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_username",
          connectionProperties.getSocksProxyUsername() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "socksproxy_password", Encr
          .encryptPasswordIfNotUsingVariables( connectionProperties.getSocksProxyPassword() ) );

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
          BaseMessages.getString( PKG, "JobFTPPUT.UnableToSaveToRepo", String.valueOf( id_job ) ), dbe );
    } finally {
      connectionProperties.setVariableSpace( var );
    }
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the binaryMode.
   */
  @Deprecated
  public boolean isBinaryMode() {
    return this.connectionProperties.isBinaryMode();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param binaryMode
   *          The binaryMode to set.
   */
  @Deprecated
  public void setBinaryMode( boolean binaryMode ) {
    this.connectionProperties.setBinaryMode( binaryMode );;
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param timeout
   *          The timeout to set.
   */
  @Deprecated
  public void setTimeout( int timeout ) {
    this.connectionProperties.setTimeout( timeout );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the timeout.
   */
  @Deprecated
  public int getTimeout() {
    return this.connectionProperties.getTimeout();
  }

  /**
   * @return Returns the onlyGettingNewFiles.
   */
  public boolean isOnlyPuttingNewFiles() {
    return onlyPuttingNewFiles;
  }

  /**
   * @param onlyPuttingNewFiles
   *          Only transfer new files to the remote host
   */
  public void setOnlyPuttingNewFiles( boolean onlyPuttingNewFiles ) {
    this.onlyPuttingNewFiles = onlyPuttingNewFiles;
  }

  /**
   * Use {@link #getConnectionProperties()}
   * Get the control encoding to be used for ftp'ing
   * 
   * @return the used encoding
   */
  @Deprecated
  public String getControlEncoding() {
    return this.connectionProperties.getControlEncoding();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * Set the encoding to be used for ftp'ing. This determines how names are translated in dir e.g. It does impact the
   * contents of the files being ftp'ed.
   * 
   * @param encoding
   *          The encoding to be used.
   */
  @Deprecated
  public void setControlEncoding( String encoding ) {
    this.connectionProperties.setControlEncoding( encoding );
  }

  /**
   * @return Returns the remoteDirectory.
   */
  public String getRemoteDirectory() {
    return remoteDirectory;
  }

  /**
   * @param directory
   *          The remoteDirectory to set.
   */
  public void setRemoteDirectory( String directory ) {
    this.remoteDirectory = directory;
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the password.
   */
  @Deprecated
  public String getPassword() {
    return String.valueOf(  this.connectionProperties.getPassword() );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param password
   *          The password to set.
   */
  @Deprecated
  public void setPassword( String password ) {
    this.connectionProperties.setPassword( password );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the serverName.
   */
  @Deprecated
  public String getServerName() {
    return this.connectionProperties.getServerName();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param serverName
   *          The serverName to set.
   */
  @Deprecated
  public void setServerName( String serverName ) {
    this.connectionProperties.setServerName( serverName );
  }

  /**
   * @return Returns the userName.
   */
  @Deprecated
  public String getUserName() {
    return this.connectionProperties.getUserName();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param userName
   *          The userName to set.
   */
  @Deprecated
  public void setUserName( String userName ) {
    this.connectionProperties.setUserName( userName );
  }

  /**
   * @return Returns the wildcard.
   */
  public String getWildcard() {
    return wildcard;
  }

  /**
   * @param wildcard
   *          The wildcard to set.
   */
  public void setWildcard( String wildcard ) {
    this.wildcard = wildcard;
  }

  /**
   * @return Returns the localDirectory.
   */
  public String getLocalDirectory() {
    return localDirectory;
  }

  /**
   * @param directory
   *          The localDirectory to set.
   */
  public void setLocalDirectory( String directory ) {
    this.localDirectory = directory;
  }

  /**
   * @param remove
   *          The remove to set.
   */
  public void setRemove( boolean remove ) {
    this.remove = remove;
  }

  /**
   * @return Returns the remove.
   */
  public boolean getRemove() {
    return remove;
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return
   */
  @Deprecated
  public String getServerPort() {
    return String.valueOf( this.connectionProperties.getPort() );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param serverPort
   */
  @Deprecated
  public void setServerPort( String serverPort ) {
    this.connectionProperties.setPort( serverPort );
  }

  /**
   * @return the activeConnection
   */
  @Deprecated
  public boolean isActiveConnection() {
    return this.connectionProperties.isActiveConnection();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param activeConnection
   *          set to true to get an active FTP connection
   */
  @Deprecated
  public void setActiveConnection( boolean activeConnection ) {
    this.connectionProperties.setActiveConnection( activeConnection );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the hostname of the ftp-proxy.
   */
  @Deprecated
  public String getProxyHost() {
    return this.connectionProperties.getProxyHost();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param proxyHost
   *          The hostname of the proxy.
   */
  @Deprecated
  public void setProxyHost( String proxyHost ) {
    this.connectionProperties.setProxyHost( proxyHost );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the password which is used to authenticate at the proxy.
   */
  @Deprecated
  public String getProxyPassword() {
    return this.connectionProperties.getProxyPassword();
  }

  /**
   * @param proxyPassword
   *          The password which is used to authenticate at the proxy.
   */
  @Deprecated
  public void setProxyPassword( String proxyPassword ) {
    this.connectionProperties.setProxyPassword( proxyPassword );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the port of the ftp-proxy.
   */
  @Deprecated
  public String getProxyPort() {
    return String.valueOf( this.connectionProperties.getProxyPort() );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param proxyPort
   *          The port of the ftp-proxy.
   */
  @Deprecated
  public void setProxyPort( String proxyPort ) {
    this.connectionProperties.setProxyPort( proxyPort );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the username which is used to authenticate at the proxy.
   */
  @Deprecated
  public String getProxyUsername() {
    return this.connectionProperties.getProxyUsername();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param socksProxyHost
   *          The socks proxy host to set
   */
  @Deprecated
  public void setSocksProxyHost( String socksProxyHost ) {
    this.connectionProperties.setSocksProxyHost( socksProxyHost );
  }

  /**
   * 
   * @param socksProxyPort
   *          The socks proxy port to set
   */
  @Deprecated
  public void setSocksProxyPort( String socksProxyPort ) {
    this.connectionProperties.setSocksProxyPort( socksProxyPort );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param socksProxyUsername
   *          The socks proxy username to set
   */
  @Deprecated
  public void setSocksProxyUsername( String socksProxyUsername ) {
    this.connectionProperties.setSocksProxyUsername( socksProxyUsername );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param socksProxyPassword
   *          The socks proxy password to set
   */
  @Deprecated
  public void setSocksProxyPassword( String socksProxyPassword ) {
    this.connectionProperties.setSocksProxyPassword( socksProxyPassword );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return The sox proxy host name
   */
  @Deprecated
  public String getSocksProxyHost() {
    return this.connectionProperties.getSocksProxyHost();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return The socks proxy port
   */
  @Deprecated
  public String getSocksProxyPort() {
    return String.valueOf( this.connectionProperties.getSocksProxyPort() );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return The socks proxy username
   */
  @Deprecated
  public String getSocksProxyUsername() {
    return this.connectionProperties.getSocksProxyUsername();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return The socks proxy password
   */
  @Deprecated
  public String getSocksProxyPassword() {
    return this.connectionProperties.getSocksProxyPassword();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param proxyUsername
   *          The username which is used to authenticate at the proxy.
   */
  @Deprecated
  public void setProxyUsername( String proxyUsername ) {
    this.connectionProperties.setProxyUsername( proxyUsername );
  }

  public Result execute( Result previousResult, int nr ) {
    Result result = previousResult;
    result.setResult( false );
    long filesput = 0;

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.Starting" ) );
    }

    this.connectionProperties.setVariableSpace( this );
    // String substitution..
    String realRemoteDirectory = environmentSubstitute( remoteDirectory );
    String realWildcard = environmentSubstitute( wildcard );
    String realLocalDirectory = environmentSubstitute( localDirectory );

    FTPCommonClient ftpclient = null;

    try {
      FTPClientFactory factory = new FTPClientFactory( log, PKG );
      //TODO we don't need initialize parsers just to delete files
      ftpclient = factory.getFtpClientInitialized( connectionProperties );
      
      // move to spool dir ...
      if ( !Const.isEmpty( realRemoteDirectory ) ) {
        ftpclient.chdir( realRemoteDirectory );
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.ChangedDirectory", realRemoteDirectory ) );
        }
      }

      // Get all the files in the local directory...
      int x = 0;

      // Joerg: ..that's for Java5
      // ArrayList<String> myFileList = new ArrayList<String>();
      ArrayList<String> myFileList = new ArrayList<String>();

      File localFiles = new File( realLocalDirectory );
      File[] children = localFiles.listFiles();
      for ( int i = 0; i < children.length; i++ ) {
        // Get filename of file or directory
        if ( !children[i].isDirectory() ) {
          // myFileList.add(children[i].getAbsolutePath());
          myFileList.add( children[i].getName() );
          x = x + 1;

        }
      } // end for

      // Joerg: ..that's for Java5
      // String[] filelist = myFileList.toArray(new String[myFileList.size()]);

      String[] filelist = new String[myFileList.size()];
      myFileList.toArray( filelist );

      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.FoundFileLocalDirectory", "" + filelist.length,
            realLocalDirectory ) );
      }

      Pattern pattern = null;
      if ( !Const.isEmpty( realWildcard ) ) {
        pattern = Pattern.compile( realWildcard );

      } // end if

      // Get the files in the list and execute ftp.put() for each file
      for ( int i = 0; i < filelist.length && !parentJob.isStopped(); i++ ) {
        boolean getIt = true;

        // First see if the file matches the regular expression!
        if ( pattern != null ) {
          Matcher matcher = pattern.matcher( filelist[i] );
          getIt = matcher.matches();
        }

        if ( getIt ) {

          // File exists?
          boolean fileExist = false;
          try {
            fileExist = ftpclient.exists( filelist[i] );

          } catch ( Exception e ) {
            // Assume file does not exist !!
          }

          if ( log.isDebug() ) {
            if ( fileExist ) {
              logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.FileExists", filelist[i] ) );
            } else {
              logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.FileDoesNotExists", filelist[i] ) );
            }
          }

          if ( !fileExist || ( !onlyPuttingNewFiles && fileExist ) ) {
            if ( log.isDebug() ) {
              logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.PuttingFileToRemoteDirectory", filelist[i],
                  realRemoteDirectory ) );
            }

            String localFilename = realLocalDirectory + Const.FILE_SEPARATOR + filelist[i];
            ftpclient.put( localFilename, filelist[i] );

            filesput++;

            // Delete the file if this is needed!
            if ( remove ) {
              new File( localFilename ).delete();
              if ( log.isDetailed() ) {
                logDetailed( BaseMessages.getString( PKG, "JobFTPPUT.Log.DeletedFile", localFilename ) );
              }
            }
          }
        }
      }

      result.setResult( true );
      if ( log.isDetailed() ) {
        logDebug( BaseMessages.getString( PKG, "JobFTPPUT.Log.WeHavePut", "" + filesput ) );
      }
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      logError( BaseMessages.getString( PKG, "JobFTPPUT.Log.ErrorPuttingFiles", e.getMessage() ) );
      logError( Const.getStackTracker( e ) );
    } finally {
      if ( ftpclient != null && ftpclient.connected() ) {
        ftpclient.quit( log );
      }
    }

    return result;
  }

  public boolean evaluates() {
    return true;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    VariableSpace var = connectionProperties.getVariableSpace();
    connectionProperties.setVariableSpace( null );

    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Const.isEmpty( connectionProperties.getServerName() ) ) {
      String realServerName = jobMeta.environmentSubstitute( connectionProperties.getServerName() );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realServerName, ResourceType.SERVER ) );
      references.add( reference );
    }

    connectionProperties.setVariableSpace( var );
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space, Repository repository,
      IMetaStore metaStore ) {
    andValidator().validate( this, "serverName", remarks, putValidators( notBlankValidator() ) );
    andValidator().validate( this, "localDirectory", remarks,
        putValidators( notBlankValidator(), fileExistsValidator() ) );
    andValidator().validate( this, "userName", remarks, putValidators( notBlankValidator() ) );
    andValidator().validate( this, "password", remarks, putValidators( notNullValidator() ) );
    andValidator().validate( this, "serverPort", remarks, putValidators( integerValidator() ) );
  }

  /**
   * @return the connectionProperties
   */
  public FTPConnectionProperites getConnectionProperties() {
    return connectionProperties;
  }
}
