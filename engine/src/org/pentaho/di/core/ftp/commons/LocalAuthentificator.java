package org.pentaho.di.core.ftp.commons;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Authentificator helper to support SOCK authentification with respect to java concurrency.
 *
 */
public class LocalAuthentificator extends Authenticator {
  private static LocalAuthentificator instance = new LocalAuthentificator();

  // ftp connection is local per thread
  private ThreadLocal<PasswordAuthentication> credentials = new ThreadLocal<PasswordAuthentication>();

  public static LocalAuthentificator getInstance() {
    return instance;
  }

  public void setCredentials( String user, String password ) {
    credentials.set( new PasswordAuthentication( user, password.toCharArray() ) );
  }

  @Override
  public PasswordAuthentication getPasswordAuthentication() {
    return credentials.get();
  }
}
