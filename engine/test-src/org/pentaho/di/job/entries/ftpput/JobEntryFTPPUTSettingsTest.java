package org.pentaho.di.job.entries.ftpput;

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

public class JobEntryFTPPUTSettingsTest extends JobEntryLoadSaveTestSupport<JobEntryFTPPUT> {

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
  protected Class<JobEntryFTPPUT> getJobEntryClass() {
    return JobEntryFTPPUT.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    List<String> attributes =
        Arrays.asList( "serverName", "serverPort", "userName", "password", "remoteDirectory", "localDirectory",
            "wildcard", "binaryMode", "timeout", "remove", "onlyPuttingNewFiles", "activeConnection",
            "control_encoding", "proxy_host", "proxy_port", "proxy_username", "proxy_password", "socksProxyHost",
            "socksProxyPort", "socksProxyUsername", "socksProxyPassword", "connectionProperties" );
    return attributes;
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
