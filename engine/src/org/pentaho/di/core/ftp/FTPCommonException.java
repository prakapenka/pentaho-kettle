package org.pentaho.di.core.ftp;

public class FTPCommonException extends Exception {

  private static final long serialVersionUID = 1L;

  public FTPCommonException() {
  }

  public FTPCommonException( String message ) {
    super( message );
  }

  public FTPCommonException( Throwable cause ) {
    super( cause );
  }

}
