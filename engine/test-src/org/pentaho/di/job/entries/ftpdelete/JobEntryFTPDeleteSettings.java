package org.pentaho.di.job.entries.ftpdelete;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.ftp.FTPConnectionPropValidator;
import org.pentaho.di.core.ftp.FTPImplementations;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class JobEntryFTPDeleteSettings extends JobEntryLoadSaveTestSupport<JobEntryFTPDelete> {

  // init password encoder/decoder since we test password get/set meth
  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
        Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Override
  protected Class<JobEntryFTPDelete> getJobEntryClass() {
    return JobEntryFTPDelete.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    List<String> attributes =
        Arrays.asList( "serverName", "port", "userName", "password", "useProxy", "proxyHost", "proxyPort",
            "proxyUsername", "proxyPassword", "socksProxyHost", "socksProxyPort", "socksProxyUsername",
            "socksProxyPassword", "timeout", "activeConnection", "ftpDirectory", "wildcard", "limitSuccess",
            "successCondition", "copyPrevious", "usePublicKey", "keyFilename", "keyFilePass", "connectionProperties" );
    return attributes;
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> map = new HashMap<String, FieldLoadSaveValidator<?>>();
    FTPConnectionPropValidator val = new FTPConnectionPropValidator();
    // for now FTP delete does not obtain protocol from CP.
    val.LIB = FTPImplementations.FTPEDT;
    val.CENC = "US-ASCII";
    val.BMODE = false;
    map.put( "connectionProperties", val );
    return map;
  }
}
