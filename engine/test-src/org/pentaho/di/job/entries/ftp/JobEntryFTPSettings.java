package org.pentaho.di.job.entries.ftp;

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

public class JobEntryFTPSettings {
  private LoadSaveTester loadSaveTester;

  @Before
  public void before() {
    List<String> attributes =
        Arrays.asList( "ftpDirectory", "targetDirectory", "wildcard", "remove", "onlyGettingNewFiles", "moveFiles",
            "moveToDirectory", "adddate", "addtime", "SpecifyFormat", "date_time_format", "AddDateBeforeExtension",
            "isaddresult", "createmovefolder", "limit",
            "success_condition",
            // obsolete
            "serverName", "port", "userName", "password", "timeout", "binaryMode", "controlEncoding", "proxyHost",
            "proxyPort", "proxyUsername", "proxyPassword", "socksProxyHost", "socksProxyPort", "socksProxyUsername",
            "socksProxyPassword",
            // replacement for old getter/setters
            "connectionProperties" );
    LoadSaveSettingUtil util = new LoadSaveSettingUtil( attributes );
    util.addSetGetPair( "adddate", "isDateInFilename", "setDateInFilename" );
    util.addSetGetPair( "addtime", "isTimeInFilename", "setTimeInFilename" );
    util.addSetGetPair( "isaddresult", "isAddToResult", "setAddToResult" );
    util.addSetGetPair( "createmovefolder", "isCreateMoveFolder", "setCreateMoveFolder" );
    this.loadSaveTester = new LoadSaveTester( JobEntryFTP.class, util );
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
