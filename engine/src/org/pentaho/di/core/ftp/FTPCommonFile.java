package org.pentaho.di.core.ftp;

import java.util.Date;

public interface FTPCommonFile {

  public boolean isDir();

  public boolean isFile();

  public boolean isLink();

  public String getName();

  public Date getDateCreated();

  public Date getDateModified();

}
