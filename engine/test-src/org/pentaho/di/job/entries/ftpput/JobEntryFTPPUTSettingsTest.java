package org.pentaho.di.job.entries.ftpput;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.ftp.FTPConnectionPropValidator;
import org.pentaho.di.core.ftp.FTPConnectionProperites;
import org.pentaho.di.job.entry.loadSave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.LoadSaveSettingUtil;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidatorFactory;

public class JobEntryFTPPUTSettingsTest {
  private LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    List<String> attributes =
        Arrays.asList( "serverName", "serverPort", "userName", "password", "remoteDirectory", "localDirectory",
            "wildcard", "binaryMode", "timeout", "remove", "onlyPuttingNewFiles", "activeConnection",
            "control_encoding", "proxy_host", "proxy_port", "proxy_username", "proxy_password", "socksProxyHost",
            "socksProxyPort", "socksProxyUsername", "socksProxyPassword", "connectionProperties" );
    LoadSaveSettingUtil util = new LoadSaveSettingUtil( attributes );
    util.addBooleanSetGetPair( "binaryMode" );
    util.addBooleanSetGetPair( "activeConnection" );

    this.loadSaveTester = new LoadSaveTester( JobEntryFTPPUT.class, util );
    FieldLoadSaveValidatorFactory validatorFactory = loadSaveTester.getFieldLoadSaveValidatorFactory();
    FieldLoadSaveValidator<FTPConnectionProperites> targetValidator = new FTPConnectionPropValidator();
    validatorFactory.registerValidator( validatorFactory.getName( FTPConnectionProperites.class ), targetValidator );
  }

  @Test
  public void loadSaveRepTest() throws KettleException {
    loadSaveTester.testRepoRoundTrip();
  }

  @Test
  public void loadSaveXmlTest() throws KettleException {
    loadSaveTester.testXmlRoundTrip();
  }

}
