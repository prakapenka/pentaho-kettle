package org.pentaho.di.job.entries.ftpdelete;

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

public class JobEntryFTPDeleteSettings {

  private LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    // except "fTPSConnectionType" which is uses non-standard setter/getter method

    List<String> attributes =
        Arrays.asList( "serverName", "port", "userName", "password", "useProxy", "proxyHost", "proxyPort",
            "proxyUsername", "proxyPassword", "socksProxyHost", "socksProxyPort", "socksProxyUsername",
            "socksProxyPassword", "timeout", "activeConnection", "ftpDirectory", "wildcard", "limitSuccess",
            "successCondition", "copyPrevious", "usePublicKey", "keyFilename", "keyFilePass", "connectionProperties" );
    LoadSaveSettingUtil util = new LoadSaveSettingUtil( attributes );
    util.addBooleanSetGetPair( "activeConnection" );
    util.addBooleanSetGetPair( "copyPrevious" );
    util.addBooleanSetGetPair( "usePublicKey" );
    this.loadSaveTester = new LoadSaveTester( JobEntryFTPDelete.class, util );
    // connectionProperties handling
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
