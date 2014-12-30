package org.pentaho.di.core.ftp;

import java.util.Date;

/**
 * Interface for FTP file since different libraries uses different
 * implementations for this item.
 * 
 */
public interface FTPCommonFile {

  public boolean isDir();

  public boolean isFile();

  public boolean isLink();

  public String getName();

  public Date getDateCreated();

  public Date getDateModified();

}
