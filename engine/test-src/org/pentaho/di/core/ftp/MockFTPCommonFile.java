package org.pentaho.di.core.ftp;

import java.util.Date;

public class MockFTPCommonFile implements FTPCommonFile {
  private boolean isDir;
  private boolean isFile;
  private boolean isLink;
  private String name;
  private Date dateCreated;
  private Date dateModified;

  public MockFTPCommonFile( boolean isDir, boolean isFile, boolean isLink, String name, Date dateCreated,
      Date dateModified ) {
    this.isDir = isDir;
    this.isFile = isFile;
    this.isLink = isLink;
    this.name = name;
    this.dateCreated = dateCreated;
    this.dateModified = dateModified;
  }

  public MockFTPCommonFile( String fileName ) {
    this( false, true, false, fileName, new Date(), new Date() );
  }

  @Override
  public boolean isDir() {
    return isDir;
  }

  @Override
  public boolean isFile() {
    return isFile;
  }

  @Override
  public boolean isLink() {
    return isLink;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Date getDateCreated() {
    return dateCreated;
  }

  @Override
  public Date getDateModified() {
    return dateModified;
  }
}
