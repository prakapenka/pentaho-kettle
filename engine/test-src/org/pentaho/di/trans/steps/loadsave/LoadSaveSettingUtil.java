package org.pentaho.di.trans.steps.loadsave;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

public class LoadSaveSettingUtil {
  private Map<String, String> getterMap;
  private Map<String, String> setterMap;
  private Map<String, FieldLoadSaveValidator<?>> attrValidatorMap;
  private Map<String, FieldLoadSaveValidator<?>> typeValidatorMap;
  private List<String> commonAttributes;
  private List<String> xmlAttributes;
  private List<String> repoAttributes;

  public LoadSaveSettingUtil( List<String> attributes ) {
    getterMap = new HashMap<String, String>();
    setterMap = new HashMap<String, String>();
    attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    commonAttributes = attributes;
    xmlAttributes = new ArrayList<String>();
    repoAttributes = new ArrayList<String>();
  }

  /**
   * shortcut to add setter/getter pair
   * 
   * @param field
   * @param getter
   * @param setter
   * @return
   */
  public String addSetGetPair( String field, String getter, String setter ) {
    String prevGet = getterMap.put( field, getter );
    String preSet = setterMap.put( field, setter );
    return prevGet + preSet;
  }

  /**
   * Setter/getter pair for boolean value (is/set)
   * 
   * @param field
   * @return
   */
  public String addBooleanSetGetPair( String field ) {
    char ch = field.charAt( 0 );
    ch = Character.toUpperCase( ch );
    field = ch + field.substring( 1 );
    String prevGet = getterMap.put( field, "is" + field );
    String preSet = setterMap.put( field, "set" + field );
    return prevGet + preSet;
  }

  public Map<String, String> getGetterMap() {
    return getterMap;
  }

  public Map<String, String> getSetterMap() {
    return setterMap;
  }

  public Map<String, FieldLoadSaveValidator<?>> getAttrValidatorMap() {
    return attrValidatorMap;
  }

  public Map<String, FieldLoadSaveValidator<?>> getTypeValidatorMap() {
    return typeValidatorMap;
  }

  public List<String> getCommonAttributes() {
    return commonAttributes;
  }

  public List<String> getXmlAttributes() {
    return xmlAttributes;
  }

  public List<String> getRepoAttributes() {
    return repoAttributes;
  }

}
