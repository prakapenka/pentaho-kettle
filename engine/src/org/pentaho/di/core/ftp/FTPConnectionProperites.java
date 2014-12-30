package org.pentaho.di.core.ftp;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.variables.VariableSpace;

/**
 * FTP connection info bean to be used with connection factory.
 * 
 * 
 */
public final class FTPConnectionProperites {

  private VariableSpace variableSpace;

  private boolean binaryMode = false;
  private boolean activeConnection = false;
  private String controlEncoding = "US-ASCII";

  // is auto by default.
  private FTPImplementations implementation = FTPImplementations.FTPEDT;

  private String _serverName = "";
  private String _port = "21";

  private int timeout = 10000;

  private String _userName = "anonymous";
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append( "FTPConnectionProperites [implementation=" );
    builder.append( implementation );
    builder.append( ", serverName=" );
    builder.append( _serverName );
    builder.append( ", port=" );
    builder.append( _port );
    builder.append( ", userName=" );
    builder.append( _userName );
    if ( !_proxyHost.isEmpty() ) {
      builder.append( ", proxyHost=" );
      builder.append( _proxyHost );
      builder.append( ", _proxyUsername=" );
      builder.append( _proxyUsername );
    }
    if ( !_socksProxyHost.isEmpty() ) {
      builder.append( ", socksProxyUsername=" );
      builder.append( _socksProxyUsername );
      builder.append( ", socksProxyHost=" );
      builder.append( _socksProxyHost );
    }
    builder.append( "]" );
    return builder.toString();
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( _password == null ) ? 0 : _password.hashCode() );
    result = prime * result + ( ( _port == null ) ? 0 : _port.hashCode() );
    result = prime * result + ( ( _proxyHost == null ) ? 0 : _proxyHost.hashCode() );
    result = prime * result + ( ( _proxyPassword == null ) ? 0 : _proxyPassword.hashCode() );
    result = prime * result + ( ( _proxyPort == null ) ? 0 : _proxyPort.hashCode() );
    result = prime * result + ( ( _proxyUsername == null ) ? 0 : _proxyUsername.hashCode() );
    result = prime * result + ( ( _serverName == null ) ? 0 : _serverName.hashCode() );
    result = prime * result + ( ( _socksProxyHost == null ) ? 0 : _socksProxyHost.hashCode() );
    result = prime * result + ( ( _socksProxyPassword == null ) ? 0 : _socksProxyPassword.hashCode() );
    result = prime * result + ( ( _socksProxyPort == null ) ? 0 : _socksProxyPort.hashCode() );
    result = prime * result + ( ( _socksProxyUsername == null ) ? 0 : _socksProxyUsername.hashCode() );
    result = prime * result + ( ( _userName == null ) ? 0 : _userName.hashCode() );
    result = prime * result + ( activeConnection ? 1231 : 1237 );
    result = prime * result + ( binaryMode ? 1231 : 1237 );
    result = prime * result + ( ( controlEncoding == null ) ? 0 : controlEncoding.hashCode() );
    result = prime * result + ( ( implementation == null ) ? 0 : implementation.hashCode() );
    result = prime * result + timeout;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    FTPConnectionProperites other = (FTPConnectionProperites) obj;
    if ( _password == null ) {
      if ( other._password != null ) {
        return false;
      }
    } else if ( !_password.equals( other._password ) ) {
      return false;
    }
    if ( _port == null ) {
      if ( other._port != null ) {
        return false;
      }
    } else if ( !_port.equals( other._port ) ) {
      return false;
    }
    if ( _proxyHost == null ) {
      if ( other._proxyHost != null ) {
        return false;
      }
    } else if ( !_proxyHost.equals( other._proxyHost ) ) {
      return false;
    }
    if ( _proxyPassword == null ) {
      if ( other._proxyPassword != null ) {
        return false;
      }
    } else if ( !_proxyPassword.equals( other._proxyPassword ) ) {
      return false;
    }
    if ( _proxyPort == null ) {
      if ( other._proxyPort != null ) {
        return false;
      }
    } else if ( !_proxyPort.equals( other._proxyPort ) ) {
      return false;
    }
    if ( _proxyUsername == null ) {
      if ( other._proxyUsername != null ) {
        return false;
      }
    } else if ( !_proxyUsername.equals( other._proxyUsername ) ) {
      return false;
    }
    if ( _serverName == null ) {
      if ( other._serverName != null ) {
        return false;
      }
    } else if ( !_serverName.equals( other._serverName ) ) {
      return false;
    }
    if ( _socksProxyHost == null ) {
      if ( other._socksProxyHost != null ) {
        return false;
      }
    } else if ( !_socksProxyHost.equals( other._socksProxyHost ) ) {
      return false;
    }
    if ( _socksProxyPassword == null ) {
      if ( other._socksProxyPassword != null ) {
        return false;
      }
    } else if ( !_socksProxyPassword.equals( other._socksProxyPassword ) ) {
      return false;
    }
    if ( _socksProxyPort == null ) {
      if ( other._socksProxyPort != null ) {
        return false;
      }
    } else if ( !_socksProxyPort.equals( other._socksProxyPort ) ) {
      return false;
    }
    if ( _socksProxyUsername == null ) {
      if ( other._socksProxyUsername != null ) {
        return false;
      }
    } else if ( !_socksProxyUsername.equals( other._socksProxyUsername ) ) {
      return false;
    }
    if ( _userName == null ) {
      if ( other._userName != null ) {
        return false;
      }
    } else if ( !_userName.equals( other._userName ) ) {
      return false;
    }
    if ( activeConnection != other.activeConnection ) {
      return false;
    }
    if ( binaryMode != other.binaryMode ) {
      return false;
    }
    if ( controlEncoding == null ) {
      if ( other.controlEncoding != null ) {
        return false;
      }
    } else if ( !controlEncoding.equals( other.controlEncoding ) ) {
      return false;
    }
    if ( implementation != other.implementation ) {
      return false;
    }
    if ( timeout != other.timeout ) {
      return false;
    }
    return true;
  }
}
