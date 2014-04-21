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

package org.pentaho.di.job.entries.ftp;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.ftp.FTPClientFactory;
import org.pentaho.di.core.ftp.FTPCommonClient;
import org.pentaho.di.core.ftp.FTPCommonException;
import org.pentaho.di.core.ftp.FTPCommonFile;
import org.pentaho.di.core.ftp.FTPConnectionProperites;
import org.pentaho.di.core.ftp.FTPImplementations;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
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
 * This defines an FTP job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 * 
 */

public class JobEntryFTP extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryFTP.class; // for i18n purposes, needed by Translator2!!

  private FTPConnectionProperites connectionProperties;
  private FTPClientFactory ftpFactory;
  private String ftpDirectory;

  private String targetDirectory;

  private String wildcard;

  private boolean remove;

  private boolean onlyGettingNewFiles; /* Don't overwrite files */

  /**
   * Implicit encoding used before PDI v2.4.1
   */
  private static String LEGACY_CONTROL_ENCODING = "US-ASCII";

  /**
   * Default encoding when making a new ftp job entry instance.
   */
  private static String DEFAULT_CONTROL_ENCODING = "ISO-8859-1";

  private boolean movefiles;

  private String movetodirectory;

  private boolean adddate;

  private boolean addtime;

  private boolean SpecifyFormat;

  private String date_time_format;

  private boolean AddDateBeforeExtension;

  private boolean isaddresult;

  private boolean createmovefolder;

  public int ifFileExistsSkip = 0;

  public String SifFileExistsSkip = "ifFileExistsSkip";

  public int ifFileExistsCreateUniq = 1;

  public String SifFileExistsCreateUniq = "ifFileExistsCreateUniq";

  public int ifFileExistsFail = 2;

  public String SifFileExistsFail = "ifFileExistsFail";

  public int ifFileExists;

  public String SifFileExists;

  public String SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED = "success_when_at_least";

  public String SUCCESS_IF_ERRORS_LESS = "success_if_errors_less";

  public String SUCCESS_IF_NO_ERRORS = "success_if_no_errors";

  private String nr_limit;

  private String success_condition;

  long NrErrors = 0;

  long NrfilesRetrieved = 0;

  boolean successConditionBroken = false;

  int limitFiles = 0;

  String targetFilename = null;

  static String FILE_SEPARATOR = "/";

  public JobEntryFTP( String n ) {
    super( n, "" );
    connectionProperties = new FTPConnectionProperites();
    nr_limit = "10";
    success_condition = SUCCESS_IF_NO_ERRORS;
    ifFileExists = ifFileExistsSkip;
    SifFileExists = SifFileExistsSkip;
    movefiles = false;
    movetodirectory = null;
    adddate = false;
    addtime = false;
    SpecifyFormat = false;
    AddDateBeforeExtension = false;
    isaddresult = true;
    createmovefolder = false;
    connectionProperties.setControlEncoding( DEFAULT_CONTROL_ENCODING );
  }

  public JobEntryFTP() {
    this( "" );
  }

  public Object clone() {
    JobEntryFTP je = (JobEntryFTP) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 128 );

    VariableSpace var = connectionProperties.getVariableSpace();
    connectionProperties.setVariableSpace( null );

    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "library",
          connectionProperties.getImplementation().toString() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", connectionProperties.getPort() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", connectionProperties.getServerName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", connectionProperties.getUserName() ) );
    retval.append( "      " ).append(
        XMLHandler.addTagValue( "password",
            Encr.encryptPasswordIfNotUsingVariables( connectionProperties.getPassword() ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "ftpdirectory", ftpDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "targetdirectory", targetDirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "wildcard", wildcard ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "binary", connectionProperties.isBinaryMode() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "timeout", connectionProperties.getTimeout() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "remove", remove ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "only_new", onlyGettingNewFiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "active", connectionProperties.isActiveConnection() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "control_encoding",
        connectionProperties.getControlEncoding() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "movefiles", movefiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "movetodirectory", movetodirectory ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "adddate", adddate ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "addtime", addtime ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "SpecifyFormat", SpecifyFormat ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "date_time_format", date_time_format ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "AddDateBeforeExtension", AddDateBeforeExtension ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "isaddresult", isaddresult ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "createmovefolder", createmovefolder ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_host", connectionProperties.getProxyHost() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_port", connectionProperties.getProxyPort() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxy_username",
        connectionProperties.getProxyUsername() ) );
    retval.append( "      " ).append(
        XMLHandler.addTagValue( "proxy_password",
            Encr.encryptPasswordIfNotUsingVariables( connectionProperties.getProxyPassword() ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_host",
        connectionProperties.getSocksProxyHost() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_port",
        connectionProperties.getSocksProxyPort() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "socksproxy_username",
        connectionProperties.getSocksProxyUsername() ) );
    retval.append( "      " ).append(
        XMLHandler.addTagValue( "socksproxy_password", Encr.encryptPasswordIfNotUsingVariables(
            connectionProperties.getSocksProxyPassword() ) ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "ifFileExists", SifFileExists ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "nr_limit", nr_limit ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "success_condition", success_condition ) );

    connectionProperties.setVariableSpace( var );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep,
      IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      String implementation = XMLHandler.getTagValue( entrynode, "library" );
      connectionProperties.setImplementation( implementation == null
          ? FTPImplementations.FTPEDT : FTPImplementations.getByCode( implementation ) );
      connectionProperties.setPort( XMLHandler.getTagValue( entrynode, "port" ) );
      connectionProperties.setServerName( XMLHandler.getTagValue( entrynode, "servername" ) );
      connectionProperties.setUserName( XMLHandler.getTagValue( entrynode, "username" ) );
      connectionProperties.setPassword(
           Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "password" ) ) );
      ftpDirectory = XMLHandler.getTagValue( entrynode, "ftpdirectory" );
      targetDirectory = XMLHandler.getTagValue( entrynode, "targetdirectory" );
      wildcard = XMLHandler.getTagValue( entrynode, "wildcard" );
      connectionProperties.setBinaryMode( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "binary" ) ) );
      connectionProperties.setTimeout( Const.toInt( XMLHandler.getTagValue( entrynode, "timeout" ), 10000 ) );
      remove = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "remove" ) );
      onlyGettingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "only_new" ) );
      connectionProperties.setActiveConnection( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "active" ) ) );
      String controlEncoding = XMLHandler.getTagValue( entrynode, "control_encoding" );
      // if we couldn't retrieve an encoding, assume it's an old instance and
      // put in the the encoding used before v 2.4.0
      connectionProperties.setControlEncoding( controlEncoding == null ? LEGACY_CONTROL_ENCODING : controlEncoding );
      movefiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "movefiles" ) );
      movetodirectory = XMLHandler.getTagValue( entrynode, "movetodirectory" );

      adddate = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "adddate" ) );
      addtime = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "addtime" ) );
      SpecifyFormat = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "SpecifyFormat" ) );
      date_time_format = XMLHandler.getTagValue( entrynode, "date_time_format" );
      AddDateBeforeExtension = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "AddDateBeforeExtension" ) );

      String addresult = XMLHandler.getTagValue( entrynode, "isaddresult" );

      if ( Const.isEmpty( addresult ) ) {
        isaddresult = true;
      } else {
        isaddresult = "Y".equalsIgnoreCase( addresult );
      }

      createmovefolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "createmovefolder" ) );

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
      SifFileExists = XMLHandler.getTagValue( entrynode, "ifFileExists" );
      if ( Const.isEmpty( SifFileExists ) ) {
        ifFileExists = ifFileExistsSkip;
      } else {
        if ( SifFileExists.equals( SifFileExistsCreateUniq ) ) {
          ifFileExists = ifFileExistsCreateUniq;
        } else if ( SifFileExists.equals( SifFileExistsFail ) ) {
          ifFileExists = ifFileExistsFail;
        } else {
          ifFileExists = ifFileExistsSkip;
        }

      }
      nr_limit = XMLHandler.getTagValue( entrynode, "nr_limit" );
      success_condition = Const.NVL( XMLHandler.getTagValue( entrynode, "success_condition" ), SUCCESS_IF_NO_ERRORS );

    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'ftp' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
      List<SlaveServer> slaveServers ) throws KettleException {
    try {
      String implementation = rep.getJobEntryAttributeString( id_jobentry, "library" );
      connectionProperties.setImplementation( implementation == null
          ? FTPImplementations.FTPEDT : FTPImplementations.getByCode( implementation ) );

      connectionProperties.setPort( rep.getJobEntryAttributeString( id_jobentry, "port" ) );
      connectionProperties.setServerName( rep.getJobEntryAttributeString( id_jobentry, "servername" ) );
      connectionProperties.setUserName( rep.getJobEntryAttributeString( id_jobentry, "username" ) );
      connectionProperties.setPassword(
          Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) ) );
      ftpDirectory = rep.getJobEntryAttributeString( id_jobentry, "ftpdirectory" );
      targetDirectory = rep.getJobEntryAttributeString( id_jobentry, "targetdirectory" );
      wildcard = rep.getJobEntryAttributeString( id_jobentry, "wildcard" );
      connectionProperties.setBinaryMode( rep.getJobEntryAttributeBoolean( id_jobentry, "binary" ) );
      connectionProperties.setTimeout( (int) rep.getJobEntryAttributeInteger( id_jobentry, "timeout" ) );
      remove = rep.getJobEntryAttributeBoolean( id_jobentry, "remove" );
      onlyGettingNewFiles = rep.getJobEntryAttributeBoolean( id_jobentry, "only_new" );
      connectionProperties.setActiveConnection( rep.getJobEntryAttributeBoolean( id_jobentry, "active" ) );
      String controlEncoding = rep.getJobEntryAttributeString( id_jobentry, "control_encoding" );
      // if we couldn't retrieve an encoding, assume it's an old instance and
      // put in the the encoding used before v 2.4.0
      connectionProperties.setControlEncoding( controlEncoding == null ? LEGACY_CONTROL_ENCODING : controlEncoding );

      movefiles = rep.getJobEntryAttributeBoolean( id_jobentry, "movefiles" );
      movetodirectory = rep.getJobEntryAttributeString( id_jobentry, "movetodirectory" );

      adddate = rep.getJobEntryAttributeBoolean( id_jobentry, "adddate" );
      addtime = rep.getJobEntryAttributeBoolean( id_jobentry, "addtime" );
      SpecifyFormat = rep.getJobEntryAttributeBoolean( id_jobentry, "SpecifyFormat" );
      date_time_format = rep.getJobEntryAttributeString( id_jobentry, "date_time_format" );
      AddDateBeforeExtension = rep.getJobEntryAttributeBoolean( id_jobentry, "AddDateBeforeExtension" );

      isaddresult = rep.getJobEntryAttributeBoolean( id_jobentry, "add_to_result_filenames" );

      createmovefolder = rep.getJobEntryAttributeBoolean( id_jobentry, "createmovefolder" );

      connectionProperties.setProxyHost( rep.getJobEntryAttributeString( id_jobentry, "proxy_host" ) );
      connectionProperties.setProxyPort( rep.getJobEntryAttributeString( id_jobentry, "proxy_port" ) );
      connectionProperties.setProxyUsername( rep.getJobEntryAttributeString( id_jobentry, "proxy_username" ) );
      connectionProperties.setProxyPassword(
          Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "proxy_password" ) ) );
      connectionProperties.setSocksProxyHost( rep.getJobEntryAttributeString( id_jobentry, "socksproxy_host" ) );
      connectionProperties.setSocksProxyPort( rep.getJobEntryAttributeString( id_jobentry, "socksproxy_port" ) );
      connectionProperties.setSocksProxyUsername( rep.getJobEntryAttributeString( id_jobentry,
            "socksproxy_username" ) );
      connectionProperties.setSocksProxyPassword(
          Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry,
              "socksproxy_password" ) ) );
      SifFileExists = rep.getJobEntryAttributeString( id_jobentry, "ifFileExists" );
      if ( Const.isEmpty( SifFileExists ) ) {
        ifFileExists = ifFileExistsSkip;
      } else {
        if ( SifFileExists.equals( SifFileExistsCreateUniq ) ) {
          ifFileExists = ifFileExistsCreateUniq;
        } else if ( SifFileExists.equals( SifFileExistsFail ) ) {
          ifFileExists = ifFileExistsFail;
        } else {
          ifFileExists = ifFileExistsSkip;
        }
      }
      nr_limit = rep.getJobEntryAttributeString( id_jobentry, "nr_limit" );
      success_condition =
          Const.NVL( rep.getJobEntryAttributeString( id_jobentry, "success_condition" ), SUCCESS_IF_NO_ERRORS );

    } catch ( KettleException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'ftp' from the repository for id_jobentry="
          + id_jobentry, dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    VariableSpace var = connectionProperties.getVariableSpace();
    connectionProperties.setVariableSpace( null );
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "library",
          connectionProperties.getImplementation().toString() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", connectionProperties.getPort() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", connectionProperties.getServerName() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "username", connectionProperties.getUserName() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password",
          Encr.encryptPasswordIfNotUsingVariables( connectionProperties.getPassword() ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "ftpdirectory", ftpDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "targetdirectory", targetDirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "wildcard", wildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "binary", connectionProperties.isBinaryMode() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "timeout", connectionProperties.getTimeout() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "remove", remove );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "only_new", onlyGettingNewFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "active", connectionProperties.isActiveConnection() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "control_encoding", connectionProperties.getControlEncoding() );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "add_to_result_filenames", isaddresult );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "movefiles", movefiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "movetodirectory", movetodirectory );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "addtime", addtime );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "adddate", adddate );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "SpecifyFormat", SpecifyFormat );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "date_time_format", date_time_format );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "AddDateBeforeExtension", AddDateBeforeExtension );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "isaddresult", isaddresult );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "createmovefolder", createmovefolder );

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
      rep.saveJobEntryAttribute( id_job, getObjectId(), "ifFileExists", SifFileExists );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "nr_limit", nr_limit );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "success_condition", success_condition );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'ftp' to the repository for id_job=" + id_job, dbe );
    } finally {
      connectionProperties.setVariableSpace( var );
    }
  }

  public void setLimit( String nr_limitin ) {
    this.nr_limit = nr_limitin;
  }

  /**
   * used to limit get files, or number errors depending on settings.
   * @return
   */
  public String getLimit() {
    return nr_limit;
  }

  public void setSuccessCondition( String success_condition ) {
    this.success_condition = success_condition;
  }

  public String getSuccessCondition() {
    return success_condition;
  }

  public void setCreateMoveFolder( boolean createmovefolderin ) {
    this.createmovefolder = createmovefolderin;
  }

  public boolean isCreateMoveFolder() {
    return createmovefolder;
  }

  public void setAddDateBeforeExtension( boolean AddDateBeforeExtension ) {
    this.AddDateBeforeExtension = AddDateBeforeExtension;
  }

  public boolean isAddDateBeforeExtension() {
    return AddDateBeforeExtension;
  }

  public void setAddToResult( boolean isaddresultin ) {
    this.isaddresult = isaddresultin;
  }

  public boolean isAddToResult() {
    return isaddresult;
  }

  public void setDateInFilename( boolean adddate ) {
    this.adddate = adddate;
  }

  public boolean isDateInFilename() {
    return adddate;
  }

  public void setTimeInFilename( boolean addtime ) {
    this.addtime = addtime;
  }

  public boolean isTimeInFilename() {
    return addtime;
  }

  public boolean isSpecifyFormat() {
    return SpecifyFormat;
  }

  public void setSpecifyFormat( boolean SpecifyFormat ) {
    this.SpecifyFormat = SpecifyFormat;
  }

  public String getDateTimeFormat() {
    return date_time_format;
  }

  public void setDateTimeFormat( String date_time_format ) {
    this.date_time_format = date_time_format;
  }

  public boolean isMoveFiles() {
    return movefiles;
  }

  public void setMoveFiles( boolean movefilesin ) {
    this.movefiles = movefilesin;
  }

  public String getMoveToDirectory() {
    return movetodirectory;
  }

  public void setMoveToDirectory( String movetoin ) {
    this.movetodirectory = movetoin;
  }

  public String getFtpDirectory() {
    return ftpDirectory;
  }

  public void setFtpDirectory( String directory ) {
    this.ftpDirectory = directory;
  }

  public String getWildcard() {
    return wildcard;
  }

  public void setWildcard( String wildcard ) {
    this.wildcard = wildcard;
  }

  public String getTargetDirectory() {
    return targetDirectory;
  }

  public void setTargetDirectory( String targetDirectory ) {
    this.targetDirectory = targetDirectory;
  }

  public void setRemove( boolean remove ) {
    this.remove = remove;
  }

  public boolean getRemove() {
    return remove;
  }

  public boolean isOnlyGettingNewFiles() {
    return onlyGettingNewFiles;
  }

  public void setOnlyGettingNewFiles( boolean onlyGettingNewFilesin ) {
    this.onlyGettingNewFiles = onlyGettingNewFilesin;
  }

  // mostly for junit to have ability to set mock factory
  FTPClientFactory getFtpFactory() {
    if ( ftpFactory == null ) {
      ftpFactory = new FTPClientFactory( log );
    }
    return ftpFactory;
  }

  // for junit use only
  void setFtpFactory( FTPClientFactory ftpFactory ) {
    this.ftpFactory = ftpFactory;
  }


  public Result execute( Result previousResult, int nr ) {
    log.logBasic( BaseMessages.getString( PKG, "JobEntryFTP.Started", connectionProperties.getServerName() ) );

    Result result = previousResult;
    result.setNrErrors( 1 );
    result.setResult( false );
    NrErrors = 0;
    NrfilesRetrieved = 0;
    successConditionBroken = false;
    boolean exitjobentry = false;
    limitFiles = Const.toInt( environmentSubstitute( getLimit() ), 10 );

    // Here let's put some controls before stating the job
    if ( movefiles && Const.isEmpty( movetodirectory ) ) {
      logError( BaseMessages.getString( PKG, "JobEntryFTP.MoveToFolderEmpty" ) );
      return result;
    }

    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.Start" ) );
    }

    FTPCommonClient ftpclient = null;
    String realMoveToFolder = null;
    connectionProperties.setVariableSpace( this );
    try {
      // connect to ftp
      ftpclient = getFtpFactory().getFtpClientInitialized( connectionProperties );
      if ( ftpclient == null ) {
        throw new Exception( "Unable to connet" );
      }

      // move to spool dir ...
      if ( !Const.isEmpty( ftpDirectory ) ) {
        String realFtpDirectory = environmentSubstitute( ftpDirectory );
        realFtpDirectory = normalizePath( realFtpDirectory );
        ftpclient.chdir( realFtpDirectory );
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.ChangedDir", realFtpDirectory ) );
        }
      }

      // Create move to folder if necessary
      if ( movefiles && !Const.isEmpty( movetodirectory ) ) {
        realMoveToFolder = environmentSubstitute( movetodirectory );
        realMoveToFolder = normalizePath( realMoveToFolder );
        // Folder exists?
        boolean folderExist = true;
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.CheckMoveToFolder", realMoveToFolder ) );
        }
        String originalLocation = ftpclient.pwd();
        try {
          // does not work for folders, see PDI-2567: folderExist=ftpclient.exists(realMoveToFolder);
          // try switching to the 'move to' folder.
          ftpclient.chdir( realMoveToFolder );
          // Switch back to the previous location.
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.CheckMoveToFolderSwitchBack", originalLocation ) );
          }
          ftpclient.chdir( originalLocation );
        } catch ( Exception e ) {
          folderExist = false;
          // Assume folder does not exist !!
        }

        if ( !folderExist ) {
          if ( createmovefolder ) {
            ftpclient.mkdir( realMoveToFolder );
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.MoveToFolderCreated", realMoveToFolder ) );
            }
          } else {
            logError( BaseMessages.getString( PKG, "JobEntryFTP.MoveToFolderNotExist" ) );
            exitjobentry = true;
            NrErrors++;
          }
        }
      }

      if ( !exitjobentry ) {
        // Get all the files in the current directory...
        FTPCommonFile[] ftpFiles = ftpclient.getDirContent( null );

        // if(isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobEntryFTP.FoundNFiles",
        // String.valueOf(filelist.length)));
        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.FoundNFiles", String.valueOf( ftpFiles.length ) ) );
        }
        // Some FTP servers return a message saying no files found as a string in the filenlist
        // e.g. Solaris 8
        // CHECK THIS !!!

        if ( ftpFiles.length == 1 ) {
          String translatedWildcard = environmentSubstitute( wildcard );
          if ( !Const.isEmpty( translatedWildcard ) ) {
            if ( ftpFiles[0].getName().startsWith( translatedWildcard ) ) {
              throw new FTPCommonException( ftpFiles[0].getName() );
            }
          }
        }

        Pattern pattern = null;
        if ( !Const.isEmpty( wildcard ) ) {
          String realWildcard = environmentSubstitute( wildcard );
          pattern = Pattern.compile( realWildcard );
        }

        if ( !getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) ) {
          limitFiles = Const.toInt( environmentSubstitute( getLimit() ), 10 );
        }

        // Get the files in the list...
        for ( FTPCommonFile ftpFile : ftpFiles ) {

          if ( parentJob.isStopped() ) {
            exitjobentry = true;
            throw new Exception( BaseMessages.getString( PKG, "JobEntryFTP.JobStopped" ) );
          }

          if ( successConditionBroken ) {
            throw new Exception( BaseMessages.getString( PKG, "JobEntryFTP.SuccesConditionBroken", "" + NrErrors ) );
          }

          boolean getIt = true;

          String filename = ftpFile.getName();
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "JobEntryFTP.AnalysingFile", filename ) );
          }

          // We get only files
          if ( ftpFile.isDir() ) {
            // not a file..so let's skip it!
            getIt = false;
            if ( isDebug() ) {
              logDebug( BaseMessages.getString( PKG, "JobEntryFTP.SkippingNotAFile", filename ) );
            }
          }
          if ( getIt ) {
            try {
              // See if the file matches the regular expression!
              if ( pattern != null ) {
                Matcher matcher = pattern.matcher( filename );
                getIt = matcher.matches();
              }
              if ( getIt ) {
                downloadFile( ftpclient, filename, realMoveToFolder, parentJob, result );
              }
            } catch ( Exception e ) {
              // Update errors number
              updateErrors();
              logError( BaseMessages.getString( PKG, "JobFTP.UnexpectedError", e.toString() ) );
            }
          }
        } // end for
      }
    } catch ( Exception e ) {
      if ( !successConditionBroken && !exitjobentry ) {
        updateErrors();
      }
      logError( BaseMessages.getString( PKG, "JobEntryFTP.ErrorGetting", e.getMessage() ) );
    } finally {
      if ( ftpclient != null ) {
        ftpclient.quit( log );
      }
    }

    result.setNrErrors( NrErrors );
    result.setNrFilesRetrieved( NrfilesRetrieved );
    if ( getSuccessStatus() ) {
      result.setResult( true );
    }
    if ( exitjobentry ) {
      result.setResult( false );
    }
    displayResults();
    return result;
  }

  private void downloadFile( FTPCommonClient ftpclient, String filename, String realMoveToFolder, Job parentJob,
      Result result ) throws Exception {

    String localFilename = filename;
    targetFilename = KettleVFS.getFilename( KettleVFS.getFileObject( returnTargetFilename( localFilename ) ) );

    if ( ( !onlyGettingNewFiles ) || ( onlyGettingNewFiles && needsDownload( targetFilename ) ) ) {
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.GettingFile", filename,
            environmentSubstitute( targetDirectory ) ) );
      }
      ftpclient.get( targetFilename, filename );

      // Update retrieved files
      updateRetrievedFiles();
      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.GotFile", filename ) );
      }

      // Add filename to result filenames
      addFilenameToResultFilenames( result, parentJob, targetFilename );

      // Delete the file if this is needed!
      if ( remove ) {
        ftpclient.delete( filename );
        if ( isDetailed() ) {
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.DeletedFile", filename ) );
          }
        }
      } else {
        if ( movefiles ) {
          // Try to move file to destination folder ...
          ftpclient.rename( filename, realMoveToFolder + FILE_SEPARATOR + filename );

          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.MovedFile", filename, realMoveToFolder ) );
          }
        }
      }
    }
  }

  /**
   * normalize / to \ and remove trailing slashes from a path
   * 
   * @param path
   * @return normalized path
   * @throws Exception
   */
  public String normalizePath( String path ) throws Exception {

    String normalizedPath = path.replaceAll( "\\\\", FILE_SEPARATOR );
    while ( normalizedPath.endsWith( "\\" ) || normalizedPath.endsWith( FILE_SEPARATOR ) ) {
      normalizedPath = normalizedPath.substring( 0, normalizedPath.length() - 1 );
    }

    return normalizedPath;
  }

  private void addFilenameToResultFilenames( Result result, Job parentJob, String filename ) throws KettleException {
    if ( isaddresult ) {
      FileObject targetFile = null;
      try {
        targetFile = KettleVFS.getFileObject( filename, this );

        // Add to the result files...
        ResultFile resultFile =
            new ResultFile( ResultFile.FILE_TYPE_GENERAL, targetFile, parentJob.getJobname(), toString() );
        resultFile.setComment( BaseMessages.getString( PKG, "JobEntryFTP.Downloaded",
            connectionProperties.getServerName() ) );
        result.getResultFiles().put( resultFile.getFile().toString(), resultFile );

        if ( isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.FileAddedToResult", filename ) );
        }
      } catch ( Exception e ) {
        throw new KettleException( e );
      } finally {
        try {
          targetFile.close();
          targetFile = null;
        } catch ( Exception e ) {
          // Ignore close errors
        }
      }
    }
  }

  private void displayResults() {
    if ( isDetailed() ) {
      logDetailed( "=======================================" );
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.Log.Info.FilesInError", "" + NrErrors ) );
      logDetailed( BaseMessages.getString( PKG, "JobEntryFTP.Log.Info.FilesRetrieved", "" + NrfilesRetrieved ) );
      logDetailed( "=======================================" );
    }
  }

  private boolean getSuccessStatus() {
    boolean retval = false;

    if ( ( NrErrors == 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
        || ( NrfilesRetrieved >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_AT_LEAST_X_FILES_DOWNLOADED ) )
        || ( NrErrors <= limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }

    return retval;
  }

  private void updateErrors() {
    NrErrors++;
    if ( checkIfSuccessConditionBroken() ) {
      // Success condition was broken
      successConditionBroken = true;
    }
  }

  private boolean checkIfSuccessConditionBroken() {
    boolean retval = false;
    if ( ( NrErrors > 0 && getSuccessCondition().equals( SUCCESS_IF_NO_ERRORS ) )
        || ( NrErrors >= limitFiles && getSuccessCondition().equals( SUCCESS_IF_ERRORS_LESS ) ) ) {
      retval = true;
    }
    return retval;
  }

  private void updateRetrievedFiles() {
    NrfilesRetrieved++;
  }

  /**
   * @param string
   *          the filename from the FTP server
   * 
   * @return the calculated target filename
   */
  private String returnTargetFilename( String filename ) {
    String retval = null;
    // Replace possible environment variables...
    if ( filename != null ) {
      retval = filename;
    } else {
      return null;
    }

    int lenstring = retval.length();
    int lastindexOfDot = retval.lastIndexOf( "." );
    if ( lastindexOfDot == -1 ) {
      lastindexOfDot = lenstring;
    }

    if ( isAddDateBeforeExtension() ) {
      retval = retval.substring( 0, lastindexOfDot );
    }

    SimpleDateFormat daf = new SimpleDateFormat();
    Date now = new Date();

    if ( SpecifyFormat && !Const.isEmpty( date_time_format ) ) {
      daf.applyPattern( date_time_format );
      String dt = daf.format( now );
      retval += dt;
    } else {
      if ( adddate ) {
        daf.applyPattern( "yyyyMMdd" );
        String d = daf.format( now );
        retval += "_" + d;
      }
      if ( addtime ) {
        daf.applyPattern( "HHmmssSSS" );
        String t = daf.format( now );
        retval += "_" + t;
      }
    }

    if ( isAddDateBeforeExtension() ) {
      retval += retval.substring( lastindexOfDot, lenstring );
    }

    // Add foldername to filename
    retval = environmentSubstitute( targetDirectory ) + Const.FILE_SEPARATOR + retval;
    return retval;
  }

  public boolean evaluates() {
    return true;
  }

  /**
   * See if the filename on the FTP server needs downloading. The default is to check the presence of the file in the
   * target directory. If you need other functionality, extend this class and build it into a plugin.
   * 
   * @param filename
   *          The local filename to check
   * @param remoteFileSize
   *          The size of the remote file
   * @return true if the file needs downloading
   */
  protected boolean needsDownload( String filename ) {
    boolean retval = false;

    File file = new File( filename );

    if ( !file.exists() ) {
      // Local file not exists!
      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "JobEntryFTP.LocalFileNotExists" ), filename );
      }
      return true;
    } else {

      // Local file exists!
      if ( ifFileExists == ifFileExistsCreateUniq ) {
        if ( isDebug() ) {
          logDebug( toString(), BaseMessages.getString( PKG, "JobEntryFTP.LocalFileExists" ), filename );
          // Create file with unique name
        }

        int lenstring = targetFilename.length();
        int lastindexOfDot = targetFilename.lastIndexOf( '.' );
        if ( lastindexOfDot == -1 ) {
          lastindexOfDot = lenstring;
        }

        targetFilename =
            targetFilename.substring( 0, lastindexOfDot ) + StringUtil.getFormattedDateTimeNow( true )
                + targetFilename.substring( lastindexOfDot, lenstring );

        return true;
      } else if ( ifFileExists == ifFileExistsFail ) {
        log.logError( BaseMessages.getString( PKG, "JobEntryFTP.LocalFileExists" ), filename );
        updateErrors();
      } else {
        if ( isDebug() ) {
          logDebug( toString(), BaseMessages.getString( PKG, "JobEntryFTP.LocalFileExists" ), filename );
        }
      }
    }

    return retval;
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    andValidator().validate( this, "serverName", remarks, putValidators( notBlankValidator() ) );
    andValidator().validate( this, "targetDirectory", remarks,
        putValidators( notBlankValidator(), fileExistsValidator() ) );
    andValidator().validate( this, "userName", remarks, putValidators( notBlankValidator() ) );
    andValidator().validate( this, "password", remarks, putValidators( notNullValidator() ) );
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    VariableSpace var = connectionProperties.getVariableSpace();
    connectionProperties.setVariableSpace( jobMeta );

    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Const.isEmpty( connectionProperties.getServerName() ) ) {
      String realServername = jobMeta.environmentSubstitute( connectionProperties.getServerName() );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realServername, ResourceType.SERVER ) );
      references.add( reference );
    }
    connectionProperties.setVariableSpace( var );
    return references;
  }

  /**
   * @return the connectionProperties
   */
  public FTPConnectionProperites getConnectionProperties() {
    return connectionProperties;
  }

  /**
   * For junit test usage only.
   * @param prop
   */
  public void setConnectionProperties( FTPConnectionProperites connectionProperties ) {
    this.connectionProperties = connectionProperties;
  }

  /**
   * Use {@link #isActiveConnection()}
   * @return the activeConnection
   */
  @Deprecated
  public boolean isActiveConnection() {
    return this.getConnectionProperties().isActiveConnection();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param activeConnection
   *          the activeConnection to set
   */
  @Deprecated
  public void setActiveConnection( boolean passive ) {
    this.connectionProperties.setActiveConnection( passive );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the password.
   */
  @Deprecated
  public String getPassword() {
    return connectionProperties.getPassword();
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
   * Use {@link #getConnectionProperties()}
   * @return Returns the port.
   */
  @Deprecated
  public String getPort() {
    return String.valueOf( connectionProperties.getPort() );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param port
   *          The port to set.
   */
  @Deprecated
  public void setPort( String port ) {
    this.connectionProperties.setPort( port );
  }

  /**
   * Use {@link #getConnectionProperties()}
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
   * Use {@link #getConnectionProperties()}
   * @param timeout
   *          The timeout to set.
   */
  @Deprecated
  public void setTimeout( int timeout ) {
    this.connectionProperties.setTimeout( timeout );
  }

  /**
   * Use {@link #getTimeout()}
   * @return Returns the timeout.
   */
  @Deprecated
  public int getTimeout() {
    return this.connectionProperties.getTimeout();
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
    this.connectionProperties.setBinaryMode( binaryMode );
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
   * @param proxyPassword
   *          The password which is used to authenticate at the socks proxy.
   */
  @Deprecated
  public void setProxyPassword( String proxyPassword ) {
    this.connectionProperties.setProxyPassword( proxyPassword );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return Returns the password which is used to authenticate at the proxy.
   */
  public String getProxyPassword() {
    return connectionProperties.getProxyPassword();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param proxyPassword
   *          The password which is used to authenticate at the proxy.
   */
  @Deprecated
  public void setSocksProxyPassword( String socksProxyPassword ) {
    this.connectionProperties.setSocksProxyPassword( socksProxyPassword );
  }

  /**
   * Use {@link #getProxyPassword()}
   * @return Returns the password which is used to authenticate at the socks proxy.
   */
  @Deprecated
  public String getSocksProxyPassword() {
    return this.connectionProperties.getSocksProxyPassword();
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
   * @return Returns the port of the ftp-proxy.
   */
  @Deprecated
  public String getProxyPort() {
    return String.valueOf( connectionProperties.getProxyPort() );
  }

  /**
   * Use {@link #getProxyUsername()}
   * @return Returns the username which is used to authenticate at the proxy.
   */
  @Deprecated
  public String getProxyUsername() {
    return this.connectionProperties.getProxyUsername();
  }

  /**
   * Use {@link #setProxyUsername(String)}
   * @param proxyUsername
   *          The username which is used to authenticate at the proxy.
   */
  @Deprecated
  public void setProxyUsername( String proxyUsername ) {
    this.connectionProperties.setProxyUsername( proxyUsername );
  }

  /**
   * Use {@link #getProxyUsername()}
   * @return Returns the username which is used to authenticate at the socks proxy.
   */
  @Deprecated
  public String getSocksProxyUsername() {
    return this.connectionProperties.getProxyUsername();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param proxyUsername
   *          The username which is used to authenticate at the socks proxy.
   */
  @Deprecated
  public void setSocksProxyUsername( String socksProxyUsername ) {
    this.connectionProperties.setSocksProxyUsername( socksProxyUsername );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param socksProxyHost
   *          The host name of the socks proxy host
   */
  @Deprecated
  public void setSocksProxyHost( String socksProxyHost ) {
    this.connectionProperties.setSocksProxyHost( socksProxyHost );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return The host name of the socks proxy host
   */
  @Deprecated
  public String getSocksProxyHost() {
    return this.connectionProperties.getSocksProxyHost();
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @param socksProxyPort
   *          The port number the socks proxy host is using
   */
  @Deprecated
  public void setSocksProxyPort( String socksProxyPort ) {
    this.connectionProperties.setSocksProxyPort( socksProxyPort );
  }

  /**
   * Use {@link #getConnectionProperties()}
   * @return The port number the socks proxy host is using
   */
  @Deprecated
  public String getSocksProxyPort() {
    return String.valueOf( connectionProperties.getSocksProxyPort() );
  }
}
