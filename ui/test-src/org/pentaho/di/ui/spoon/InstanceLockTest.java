package org.pentaho.di.ui.spoon;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LogChannelInterface;

public class InstanceLockTest {
  static InstanceLock instance = InstanceLock.getInstance();

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    instance.unlock();
  }

  /**
   * <p>
   * See PDI-3748.
   * </p>
   * <p>
   * This is very simple check. We will not run 2 separate JVM to check or a 2 separate Processes with same jar file
   * etc..
   * </p>
   */
  @Test
  public void testLockCanBeObtained() {
    LogChannelInterface log = Mockito.mock( LogChannelInterface.class );
    boolean locked = instance.tryLock( log );

    Assert.assertTrue( "Locked at first time: ", locked );
    Assert.assertFalse( "Lock can't be obtained", instance.tryLock( log ) );

    instance.unlock();
    Assert.assertTrue( "Lock can be obtained", instance.tryLock( log ) );
    // unlock in final tear down.
  }
}
