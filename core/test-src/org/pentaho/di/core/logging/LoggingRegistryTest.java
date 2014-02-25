/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.core.logging;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.Const.KETTLE_LOG_MARK_MAPPINGS;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Tatsiana_Kasiankova
 * 
 */
public class LoggingRegistryTest {

  static LoggingObjectInterface simplelogObjectMock = mock( SimpleLoggingObject.class );

  @BeforeClass
  public static void beforeClass() {
    when( simplelogObjectMock.getObjectName() ).thenReturn( "SimpleLoggingObject" );
    when( simplelogObjectMock.getObjectType() ).thenReturn( LoggingObjectType.TRANS );

  }

  @AfterClass
  public static void afterClass() {
    LoggingRegistry.getInstance().getMap().clear();
  }

  @Test
  public void testNoChecksExistingLoggingSource_WhenLogMarkMappingTurnOn() throws Exception {
    turnOnLogMarkMapping();

    LoggingRegistry logRegSpy = spy( LoggingRegistry.getInstance() );
    String logChannelId = logRegSpy.registerLoggingSource( simplelogObjectMock );
    assertNotNull( logChannelId );
    verify( logRegSpy, never() ).findExistingLoggingSource( any( LoggingObject.class ) );
    assertTrue(
        "Logging Object [" + logChannelId + ", " + simplelogObjectMock.getObjectName() + "] is in registry map",
        logRegSpy.getMap().containsKey( logChannelId ) );
  }

  @Test
  public void testChecksExistingLoggingSource_WhenLogMarkMappingTurnOff() throws Exception {
    turnOffLogMarkMapping();

    LoggingRegistry logRegSpy = spy( LoggingRegistry.getInstance() );
    String logChannelId = logRegSpy.registerLoggingSource( simplelogObjectMock );
    assertNotNull( logChannelId );
    verify( logRegSpy, times( 1 ) ).findExistingLoggingSource( any( LoggingObject.class ) );
    assertTrue(
        "Logging Object [" + logChannelId + ", " + simplelogObjectMock.getObjectName() + "] is in registry map",
        logRegSpy.getMap().containsKey( logChannelId ) );
  }

  private void turnOnLogMarkMapping() {
    System.getProperties().put( KETTLE_LOG_MARK_MAPPINGS, "Y" );
  }

  private void turnOffLogMarkMapping() {
    System.getProperties().put( KETTLE_LOG_MARK_MAPPINGS, "N" );
  }

  private static LoggingObjectInterface getLoggingObjectWithOneParent() {
    LoggingObjectInterface rootLogObject = new SimpleLoggingObject( "ROOT_SUBJECT", LoggingObjectType.SPOON, null );
    LoggingObjectInterface transLogObject =
        new SimpleLoggingObject( "TRANS_SUBJECT", LoggingObjectType.TRANS, rootLogObject );
    return transLogObject;
  }

}
