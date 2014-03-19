package org.pentaho.di.core.logging;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;

public class LoggingRegistryUnitTest {

  LoggingRegistry instance = LoggingRegistry.getInstance();

  @BeforeClass
  public static void beforClass() throws KettleException {
    // KettleEnvironment.init();
  }

  /**
   * Test that registered object can be safely removed.
   */
  @Test
  public void testUnregisterLoggingObject() {
    SomeLoggingObject obj = new SomeLoggingObject();
    String logChannelId = instance.registerLoggingSource( obj );
    instance.removeIncludingChildren( logChannelId );

    Assert.assertTrue( "Registered logging objectm properly unregistered", !instance.dumpItems().containsKey(
        logChannelId ) );
  }

  /**
   * Test that after logging channel is created object can be found.
   */
  @Test
  public void testObjectRegisteredAndFound() {
    LoggingObjectInterface obj = new SomeLoggingObject();

    @SuppressWarnings( "unused" )
    LogChannel logCH = new LogChannel( obj );

    LoggingObjectInterface found = instance.findExistingLoggingSource( obj );

    Assert.assertNotNull( "Object can be found", found );
  }

  /**
   * Test when creating objects hierarchy it can be properly discovered
   */
  @Test
  public void testRegisterLoggingObjectWithParent() {
    SimpleLoggingObject parent = new SimpleLoggingObject( "parent", LoggingObjectType.GENERAL, null );
    SimpleLoggingObject child1 = new SimpleLoggingObject( "child1", LoggingObjectType.GENERAL, parent );
    SimpleLoggingObject child2 = new SimpleLoggingObject( "child2", LoggingObjectType.GENERAL, parent );

    LogChannel parentCh = new LogChannel( parent );
    String id = parentCh.getLogChannelId();
    LogChannel childCh1 = new LogChannel( child1, parent );
    LogChannel childCh2 = new LogChannel( child2, parent );

    Map<String, List<String>> map = instance.dumpChildren();
    Assert.assertNotNull( "Parent has registered child relashionships", map.get( id ) );
    Assert.assertEquals( "parent has 2 childs", 2, map.get( parentCh.getLogChannelId() ).size() );

    Assert.assertNull( "No child for child 1", map.get( childCh1.getLogChannelId() ) );
    Assert.assertNull( "No child for child 2", map.get( childCh2.getLogChannelId() ) );
  }

  /**
   * Attempt to register any object already registered should not lead to creation new item (memory leaks)
   */
  @Test
  public void testRegisterLoggingSourceLeaks() {
    SomeObject object = new SomeObject();
    String uid1 = instance.registerLoggingSource( object );
    String uid2 = instance.registerLoggingSource( object );
    Assert.assertEquals( "same uid for same object:", uid1, uid2 );
  }

  /**
   * Attempt to register object, already registered in registry should not create new logging channel (memory leaks)
   */
  @Test
  public void testTransMetaCreation() {
    SomeLoggingObject meta = new SomeLoggingObject();
    String logIdExp = meta.getLogChannelId();
    String actual = instance.registerLoggingSource( meta );
    Assert.assertEquals( "Same log channel ID", logIdExp, actual );
  }

  class SomeObject {
    // this will be used as identificator for logging object
    @Override
    public String toString() {
      return "HereIam";
    }
  }

  /**
   * Dumb implementation of LoggingObjectInterface to simulate parent-child relationship
   */
  class SomeLoggingObject implements LoggingObjectInterface {

    String objectName = UUID.randomUUID().toString();
    String fileName = UUID.randomUUID().toString();
    RepositoryDirectoryInterface repoInt = Mockito.mock( RepositoryDirectoryInterface.class );
    ObjectId obId = new StringObjectId( UUID.randomUUID().toString() );
    ObjectRevision rev;

    // obtained from registry
    String logChId = null;
    LoggingObjectType logType = LoggingObjectType.JOB;
    String objCopy;
    LogLevel level = LogLevel.ERROR;
    String contObjId = UUID.randomUUID().toString();
    Date regDate = new Date();
    boolean gatheringMetrics = true;
    boolean forcingSeparateLogging = false;
    LoggingObjectInterface parent;

    SomeLoggingObject( LoggingObjectInterface parent ) {
      this();
      this.parent = parent;
    }

    SomeLoggingObject() {
      rev = new ObjectRevision() {
        @Override
        public String getName() {
          return UUID.randomUUID().toString();
        }

        @Override
        public Date getCreationDate() {
          return new Date( 0 );
        }

        @Override
        public String getComment() {
          return UUID.randomUUID().toString();
        }

        @Override
        public String getLogin() {
          return UUID.randomUUID().toString();
        }
      };
    }

    @Override
    public String getObjectName() {
      return objectName;
    }

    @Override
    public RepositoryDirectoryInterface getRepositoryDirectory() {
      return repoInt;
    }

    @Override
    public String getFilename() {
      return fileName;
    }

    @Override
    public ObjectId getObjectId() {
      return obId;
    }

    @Override
    public ObjectRevision getObjectRevision() {
      return rev;
    }

    @Override
    public String getLogChannelId() {
      return logChId;
    }

    @Override
    public LoggingObjectInterface getParent() {
      return parent;
    }

    @Override
    public LoggingObjectType getObjectType() {
      return this.logType;
    }

    @Override
    public String getObjectCopy() {
      return objCopy;
    }

    @Override
    public LogLevel getLogLevel() {
      return this.level;
    }

    @Override
    public String getContainerObjectId() {
      return contObjId;
    }

    @Override
    public Date getRegistrationDate() {
      return regDate;
    }

    @Override
    public boolean isGatheringMetrics() {
      return gatheringMetrics;
    }

    @Override
    public void setGatheringMetrics( boolean gatheringMetrics ) {
      this.gatheringMetrics = gatheringMetrics;
    }

    @Override
    public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
      this.forcingSeparateLogging = forcingSeparateLogging;
    }

    @Override
    public boolean isForcingSeparateLogging() {
      return forcingSeparateLogging;
    }
  }
}
