package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Date;
import java.util.Random;

public class IntegerLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
  Date date = new Date();
  private final Integer value = new Random( date.getTime() ).nextInt();

  @Override
  public Integer getTestObject() {
    return value;
  }

  @Override
  public boolean validateTestObject( Integer testObject, Object actual ) {
    return testObject.equals( actual );
  }
}
