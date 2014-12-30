package org.pentaho.di.core.ftp;

import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class FTPConnectionPropValidator implements FieldLoadSaveValidator<FTPConnectionProperites> {
  static final String UNAME = "uname";
  static final String PASSW = "passw";
  static final String SNAME = "sname";
  static final String PORT = "13";
  static final String PHOST = "phost";
  static final String PPORT = "14";
  static final String PUSER = "pUser";
  static final String PPASSW = "ppassw";
  static final String SPHOST = "spHost";
  static final String SPORT = "10002";
  static final String SUNAME = "sUname";
  static final String SPASSW = "sPassw";
  // disclosure for FTP delete which does not set encoding
  public String CENC = "cEnc";
  static final Integer TOUT = 120;
  static final boolean ACONN = true;
  public boolean BMODE = true;
  // temporary disclose to handle workaround for DeleteFTP multiple protocols
  public FTPImplementations LIB = FTPImplementations.APACHE_CN;

  @Override
  public FTPConnectionProperites getTestObject() {
    FTPConnectionProperites prop = new FTPConnectionProperites();
    prop.setImplementation( LIB );
    prop.setUserName( UNAME );
    prop.setPassword( PASSW );
    prop.setServerName( SNAME );
    prop.setPort( PORT );
    prop.setProxyHost( PHOST );
    prop.setProxyPort( PPORT );
    prop.setProxyUsername( PUSER );
    prop.setProxyPassword( PPASSW );
    prop.setSocksProxyHost( SPHOST );
    prop.setSocksProxyPort( SPORT );
    prop.setSocksProxyUsername( SUNAME );
    prop.setSocksProxyPassword( SPASSW );
    prop.setActiveConnection( ACONN );
    prop.setBinaryMode( BMODE );
    prop.setControlEncoding( CENC );
    prop.setTimeout( TOUT );
    return prop;
  }

  @Override
  public boolean validateTestObject( FTPConnectionProperites testObject, Object actual ) {
    return testObject.equals( actual );
  }
}
