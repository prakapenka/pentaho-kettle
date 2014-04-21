package org.pentaho.di.core.ftp;

public enum FTPImplementations {
  /**
   * <p>
   * AUTO is default implementation for backward compatibility, when customer transformation does not have detailed
   * specified.
   * </p>
   */
  AUTO( "auto" ),
  /**
   * <p>
   * This implementation was default in previous releases
   * </p>
   */
  FTPEDT( "edtFTPj (Deprecated)" ),
  /**
   * <p>
   * This implementation was introduced to support proxy, see PDI-7415 for details
   * </p>
   */
  APACHE_CN( "Apache Commons Net" );

  private String name;

  FTPImplementations( String name ) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  /**
   * Used for ui to get selected text from combo and return enum code to store into xml/db transformation.
   * 
   * @param value
   *          <u>localized</u> text from ui combo
   * @return
   */
  public static FTPImplementations getByValue( String value ) {
    FTPImplementations[] values = FTPImplementations.values();
    for ( int i = 0; i < values.length; i++ ) {
      if ( values[i].getName().equals( value ) ) {
        return values[i];
      }
    }
    // this is seems to be unrecognized value :(
    return FTPImplementations.AUTO;
  }

  /**
   * Used for loadXml/loadRepository code to get a real enum from text representation.
   * 
   * @param code
   *          - <u>toString()</u> enum representation
   * @return
   */
  public static FTPImplementations getByCode( String code ) {
    FTPImplementations[] values = FTPImplementations.values();
    for ( int i = 0; i < values.length; i++ ) {
      if ( values[i].toString().equals( code.toUpperCase() ) ) {
        return values[i];
      }
    }
    // this is seems to be unrecognized code :(
    return FTPImplementations.AUTO;
  }
}
