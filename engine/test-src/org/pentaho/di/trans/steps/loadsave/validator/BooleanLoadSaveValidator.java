package org.pentaho.di.trans.steps.loadsave.validator;

import java.util.Random;

public class BooleanLoadSaveValidator implements FieldLoadSaveValidator<Boolean> {
  // by default boolean type usually initialized as false
  // so we may double-check Booleans for false/true assignment
  // otherwise if somehow default value is initialized as false
  // and random returns false it is not guaranteed it is really loaded properly?
  private final Boolean value = new Random().nextBoolean();

  @Override
  public Boolean getTestObject() {
    return value;
  }

  @Override
  public boolean validateTestObject( Boolean original, Object actual ) {
    return original.equals( actual );
  }
}
