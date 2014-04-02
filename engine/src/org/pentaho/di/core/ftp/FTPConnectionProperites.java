package org.pentaho.di.core.ftp;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.variables.VariableSpace;

/**
 * FTP connection info bean to be used with connection factory.
 * 
 * 
 */
public class FTPConnectionProperites {

  private VariableSpace variableSpace;

  private boolean binaryMode = false;
  private boolean activeConnection = false;
  private String controlEncoding = "US-ASCII";

  // is auto by default.
  private FTPImplementations implementation = FTPImplementations.AUTO;

  private String _serverName = "";
  private String _port = "21";

  private int timeout = 10000;

  private String _userName = "";
  private String _password = "";

  private String _proxyHost = "";
  private String _proxyPassword = "";
  private String _proxyPort = "8080";
  private String _proxyUsername = "";

  private String _socksProxyPassword = "";
  private String _socksProxyUsername = "";
  private String _socksProxyHost = "";
  private String _socksProxyPort = "1080";

  public FTPImplementations getImplementation() {
    return implementation;
  }

  public void setImplementation( FTPImplementations implementation ) {
    this.implementation = implementation;
  }

  public boolean hasProxy() {
    return !Const.isEmpty( _proxyHost );
  }

  public boolean hasSOCKSProxy() {
    return !Const.isEmpty( _socksProxyHost );
  }

  /**
   * @return the binaryMode
   */
  public boolean isBinaryMode() {
    return binaryMode;
  }

  /**
   * @return the serverName
   */
  public String getServerName() {
    return environmentSubstitute( _serverName );
  }

  /**
   * @return the port
   */
  public int getPort() {
    return Const.toInt( environmentSubstitute( _port ), 21 );
  }

  /**
   * @return the timeout
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * @return the userName
   */
  public String getUserName() {
    return environmentSubstitute( _userName );
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( _password ) );
  }

  /**
   * @return the proxyHost
   */
  public String getProxyHost() {
    return environmentSubstitute( _proxyHost );
  }

  /**
   * @return the proxyPassword
   */
  public String getProxyPassword() {
    return Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( _proxyPassword ) );
  }

  /**
   * @return the proxyPort
   */
  public int getProxyPort() {
    return Const.toInt( environmentSubstitute( _proxyPort ), 21 );
  }

  /**
   * @return the proxyUsername
   */
  public String getProxyUsername() {
    return environmentSubstitute( _proxyUsername );
  }

  /**
   * @return the socksProxyPassword
   */
  public String getSocksProxyPassword() {
    return Encr.decryptPasswordOptionallyEncrypted( environmentSubstitute( _socksProxyPassword ) );
  }

  /**
   * @return the socksProxyUsername
   */
  public String getSocksProxyUsername() {
    return environmentSubstitute( _socksProxyUsername );
  }

  /**
   * @return the socksProxyHost
   */
  public String getSocksProxyHost() {
    return environmentSubstitute( _socksProxyHost );
  }

  /**
   * @return the socksProxyPort
   */
  public int getSocksProxyPort() {
    return Const.toInt( environmentSubstitute( _socksProxyPort ), 1080 );
  }

  /**
   * @param binaryMode
   *          the binaryMode to set
   */
  public void setBinaryMode( boolean binaryMode ) {
    this.binaryMode = binaryMode;
  }

  /**
   * @param serverName
   *          the serverName to set
   */
  public void setServerName( String serverName ) {
    this._serverName = serverName;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort( String port ) {
    this._port = port;
  }

  /**
   * @param timeout
   *          the timeout to set
   */
  public void setTimeout( int timeout ) {
    this.timeout = timeout;
  }

  /**
   * @param userName
   *          the userName to set
   */
  public void setUserName( String userName ) {
    this._userName = userName;
  }

  /**
   * @param password
   *          the password to set
   */
  public void setPassword( String password ) {
    this._password = password;
  }

  /**
   * @param proxyHost
   *          the proxyHost to set
   */
  public void setProxyHost( String proxyHost ) {
    this._proxyHost = proxyHost;
  }

  /**
   * @param proxyPassword
   *          the proxyPassword to set
   */
  public void setProxyPassword( String proxyPassword ) {
    this._proxyPassword = proxyPassword;
  }

  /**
   * @param proxyPort
   *          the proxyPort to set
   */
  public void setProxyPort( String proxyPort ) {
    this._proxyPort = proxyPort;
  }

  /**
   * @param proxyUsername
   *          the proxyUsername to set
   */
  public void setProxyUsername( String proxyUsername ) {
    this._proxyUsername = proxyUsername;
  }

  /**
   * @param socksProxyPassword
   *          the socksProxyPassword to set
   */
  public void setSocksProxyPassword( String socksProxyPassword ) {
    this._socksProxyPassword = socksProxyPassword;
  }

  /**
   * @param socksProxyUsername
   *          the socksProxyUsername to set
   */
  public void setSocksProxyUsername( String socksProxyUsername ) {
    this._socksProxyUsername = socksProxyUsername;
  }

  /**
   * @param socksProxyHost
   *          the socksProxyHost to set
   */
  public void setSocksProxyHost( String socksProxyHost ) {
    this._socksProxyHost = socksProxyHost;
  }

  /**
   * @param socksProxyPort
   *          the socksProxyPort to set
   */
  public void setSocksProxyPort( String socksProxyPort ) {
    this._socksProxyPort = socksProxyPort;
  }

  /**
   * @return the activeConnection
   */
  public boolean isActiveConnection() {
    return activeConnection;
  }

  /**
   * @param activeConnection
   *          the activeConnection to set
   */
  public void setActiveConnection( boolean activeConnection ) {
    this.activeConnection = activeConnection;
  }

  /**
   * @return the controlEncoding
   */
  public String getControlEncoding() {
    return controlEncoding;
  }

  /**
   * @param controlEncoding
   *          the controlEncoding to set
   */
  public void setControlEncoding( String controlEncoding ) {
    this.controlEncoding = controlEncoding;
  }

  String environmentSubstitute( String value ) {
    return variableSpace == null ? value : variableSpace.environmentSubstitute( value );
  }

  /**
   * @return the variableSpace
   */
  public VariableSpace getVariableSpace() {
    return variableSpace;
  }

  /**
   * @param variableSpace
   *          the variableSpace to set
   */
  public void setVariableSpace( VariableSpace variableSpace ) {
    this.variableSpace = variableSpace;
  }
}
