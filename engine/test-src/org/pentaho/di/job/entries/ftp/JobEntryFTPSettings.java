package org.pentaho.di.job.entries.ftp;

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
import org.pentaho.di.core.ftp.FTPConnectionProperites;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class JobEntryFTPSettings extends JobEntryLoadSaveTestSupport<JobEntryFTP> {

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
  protected Class<JobEntryFTP> getJobEntryClass() {
    return JobEntryFTP.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    List<String> attributes =
        Arrays.asList( "ftpDirectory", "targetDirectory", "wildcard", "remove", "onlyGettingNewFiles", "moveFiles",
            "moveToDirectory", "adddate", "addtime", "SpecifyFormat", "date_time_format", "AddDateBeforeExtension",
            "isaddresult", "createmovefolder", "limit",
            "success_condition",
            // old getter/setters is replaced with connectionProperties bean
            "serverName", "port", "userName", "password", "timeout", "binaryMode", "controlEncoding", "proxyHost",
            "proxyPort", "proxyUsername", "proxyPassword", "socksProxyHost", "socksProxyPort", "socksProxyUsername",
            "socksProxyPassword" , "connectionProperties" );
    return attributes;
  }

  @Override
  protected Map<String, String> createGettersMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put( "adddate", "isDateInFilename" );
    map.put( "addtime", "isTimeInFilename" );
    map.put( "isaddresult", "isAddToResult" );
    map.put( "createmovefolder", "isCreateMoveFolder" );
    return map;
  }

  @Override
  protected Map<String, String> createSettersMap() {
    Map<String, String> map = new HashMap<String, String>();
    map.put( "adddate", "setDateInFilename" );
    map.put( "addtime", "setTimeInFilename" );
    map.put( "isaddresult", "setAddToResult" );
    map.put( "createmovefolder", "setCreateMoveFolder" );
    return map;
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> map =
        new HashMap<String, FieldLoadSaveValidator<?>>();
    FieldLoadSaveValidator<FTPConnectionProperites> val = new FTPConnectionPropValidator();
    map.put( "connectionProperties", val );
    return map;
  }
}
