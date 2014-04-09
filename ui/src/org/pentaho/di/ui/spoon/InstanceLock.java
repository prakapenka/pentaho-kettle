package org.pentaho.di.ui.spoon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

public class InstanceLock {
  private static Class<?> PKG = InstanceLock.class;
  public static final String lockName = "/lock";

  private File file;
  private FileLock fl;
  private RandomAccessFile rfile;

  private InstanceLock() {
  }

  private static InstanceLock instance = new InstanceLock();

  public static InstanceLock getInstance() {
    return instance;
  }

  public boolean tryLock( LogChannelInterface log ) {
    String path = Const.getKettleDirectory();
    path += lockName;

    String cpath = null;

    file = new File( path );
    // resolve canonical path to see how it is going.
    try {
      cpath = file.getCanonicalPath();
    } catch ( IOException io ) {
      // somehow we got incorrect file path - we will not be able to deal with it.
      // IDS02-J ;) BaseMessages.getString( PKG, "InstanceLock.PathResolveError" )
      log.logError( BaseMessages.getString( PKG, "InstanceLock.PathResolveError", file.getPath() ) );
      return true;
    }

    if ( !file.exists() ) {
      try {
        file.createNewFile();
      } catch ( IOException e ) {
        // we don't have write access or it is SecurityManager
        // or something we can't deal with it once again.
        log.logError( BaseMessages.getString( PKG, "InstanceLock.WriteError", cpath ) );
        return true;
      }
    }

    // ok, now lock it down
    try {
      rfile = new RandomAccessFile( file, "rws" );
      FileChannel channel = rfile.getChannel();

      try {
        fl = channel.tryLock();
      } catch ( OverlappingFileLockException e ) {
        // somebody attempt to lock in same JVM?
        log.logError( "API error. This cahnnel for 'lock' already locked! Kettle home: " + cpath );
        return false;
      }
      if ( fl == null ) {
        // somebody IN ANOTHER JVM has lock already
        return false;
      }

    } catch ( FileNotFoundException e ) {
      // someone already had delete it? can't get RanfomAccessFile
    } catch ( IOException e ) {
      // can't obtain FileChannel? Something wrong with io system.
    }
    return true;
  }

  public void unlock() {
    if ( fl != null ) {
      try {
        fl.release();
      } catch ( IOException e ) {
        // JVM shut down will release it finally
      }
    }
    if ( rfile != null ) {
      try {
        rfile.close();
      } catch ( IOException e ) {
        // no problems with it
      }
    }
    file.delete();
  }
}
