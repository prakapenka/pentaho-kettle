package org.pentaho.di.job.entries.ftp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.ftp.FTPConnectionProperites;
import org.pentaho.di.core.ftp.FTPImplementations;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.steps.loadsave.MemoryRepository;
import org.pentaho.di.utils.XMLXpathParser;
import org.xml.sax.SAXException;

public class JobEntryFTPSettings {

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
  static final String CENC = "cEnc";
  static final Integer TOUT = 120;
  static boolean ACONN = true;
  static boolean BMODE = true;
  static FTPImplementations LIB = FTPImplementations.APACHE_CN;
    
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }
  
  void initFTPConnectionProperites ( FTPConnectionProperites prop ){
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
  }
  
  @Test
  public void getXmlTest() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
    JobEntryFTP entry = new JobEntryFTP();        
    initFTPConnectionProperites ( entry.getConnectionProperties() );
    
    String xml = "<root>" + entry.getXML() + "</root>";    
    XMLXpathParser parser = new XMLXpathParser( xml );
    
    Assert.assertTrue( "At least we can have some xml output", !xml.isEmpty() );
    
    // based on xml tag values
    Assert.assertEquals( "Library (implementation) can be set properly",
        FTPImplementations.APACHE_CN.toString(), parser.queryTextContent( "//library" ).get( 0 ) );
    Assert.assertEquals("Username is saved correctly", UNAME , parser.queryTextContent( "//username" ).get( 0 )  );
    Assert.assertEquals( "Server is correct", SNAME, parser.queryTextContent( "//servername" ).get( 0 ) );
    Assert.assertEquals( "Port is correct", PORT, parser.queryTextContent( "//port" ).get( 0 ) );
    Assert.assertEquals( "Passw is correct (got encrypted actually)",
        Encr.encryptPasswordIfNotUsingVariables( PASSW ), parser.queryTextContent( "//password" ).get( 0 ) );
    
    Assert.assertEquals( "Proxy host is correct", PHOST, parser.queryTextContent( "//proxy_host" ).get( 0 ) );
    Assert.assertEquals("Proxy port is correct", PPORT, parser.queryTextContent( "//proxy_port" ).get( 0 ) );
    Assert.assertEquals( "Proxy user name is correct", PUSER, parser.queryTextContent( "//proxy_username" ).get( 0 ) );
    Assert.assertEquals( "Proxy password is correct (got ecnrypted)",
        Encr.encryptPasswordIfNotUsingVariables( PPASSW ), parser.queryTextContent( "//proxy_password" ).get( 0 ) );
    
    Assert.assertEquals( "Socks proxy host is correct", SPHOST, parser.queryTextContent( "//socksproxy_host" ).get( 0 ) );
    Assert.assertEquals( "Socks proxy port is correct", SPORT, parser.queryTextContent( "//socksproxy_port" ).get( 0 ) );
    Assert.assertEquals( "Sock proxy user name is correct", SUNAME, parser.queryTextContent( "//socksproxy_username" ).get( 0 ) );
    Assert.assertEquals( "Sock proxy password name is correct (got encrypted)",
        Encr.encryptPasswordIfNotUsingVariables( SPASSW ), parser.queryTextContent( "//socksproxy_password" ).get( 0 ) );
    
    Assert.assertEquals( "Binary mode is correct (converted to Y or N)", BMODE ? "Y" : "N", parser.queryTextContent( "//binary" ).get( 0 ) );
    Assert.assertEquals( "Control encoding is correct", CENC, parser.queryTextContent( "//control_encoding" ).get( 0 ) );
    Assert.assertEquals( "Timeout is correct", TOUT.toString(), parser.queryTextContent( "//timeout" ).get( 0 ) );   
  }

  @Test
  public void saveToRepTest() throws KettleException {
    JobEntryFTP entry = new JobEntryFTP();
    ObjectId id_job = new StringObjectId( UUID.randomUUID().toString() );
    entry.setObjectId( id_job );
    
    initFTPConnectionProperites ( entry.getConnectionProperties() );

    Repository rep = new MemoryRepository();

    //IMetaStore not used in this implementation
    entry.saveRep( rep, null, id_job );
    
    entry.loadRep( rep, null, id_job, null, null );
    
    FTPConnectionProperites conn = entry.getConnectionProperties();
    
    
  }

}
