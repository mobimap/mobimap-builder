/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.model;

import java.io.*;

public abstract class DataStorage
   implements
     Serializable
  {

  /**
   * Number of fields that loaded from the source (server side or
   * shapefile). The actual number of fields may be more because of
   * calculated (custom) fields. To get actual number of fields you
   * have to use <code>getFieldCount</code>.
   *
   * @see Layer#getFieldCount
   * @see RecordField
   */
  protected int fieldOriginalCount = 0;

 // protected FieldDef[] fieldDefs = null;

  /**
   * Returns a value that indicates the type of geometric shape
   * associated with a Layer.<br>
   *  <table>
   *  <tr><td>POINT</td><td>     1  </td><td>Point features</td></tr>
   *  <tr><td>LINE</td><td>      2  </td><td>Line features</td></tr>
   *  <tr><td>POLYGON</td><td>   3  </td><td>Polygon features</td></tr>
   *  <tr><td>BOUND_BOX</td><td> 4  </td><td>Rectangle features</td></tr>
   *  </table>
   *
   * @see Const
   */
  private int objectType = -1;


//--------- MAIN ROUTINES ------------------------------------------

  /*
   *
   */
  public DataStorage() {}

  /**
   * Performs things necessary after deserialization such as
   * loading images (which are not serializable), etc.
   * TODO: implement better machanism for calling objects that need
   * to perform some things after deserialization.
   */
  public void afterDeserialization() {}

  abstract public int size();

//--------- SAVE ROUTINES ------------------------------------------

  /**
   * Saves the record in the datastorage.
   * Saves positions, coordinates and attributes
   */
  abstract public void saveDataStorageRec(Record rec, boolean isBoth)
                                                 throws IOException;

  /**
   * Opens the datastorage to save.
   *
   * isTempIndex - determines whether the index is stored
   * in temporary storage (if the final size is unknown) or
   * directly in the index arrays
   */
  abstract public void openDataStorageSave(boolean isTempIndex);

  /*
   * Opens dataStorage for saving coordinates only
   */
  abstract public void openDataStorageSaveCoordinates();

  /**
   * Finalizes the save. Converts the streams into byte arrays.
   */
  abstract public void closeDataStorageSave();

//---------------- READ ROUTINES -----------------------------------

  /**
   * Opens the datasorage to read
   */
  abstract public void openDataStorage(boolean withAttributes);

  /**
   * Closes the datastorage after reading is completed
   */
  abstract public void closeDataStorage();

  /**
   * Skips the reading of attribute data for the given index
   */
  abstract public void skipAttribute(int index) throws IOException;

  abstract protected void skipAttribute2(int index1, int index2) throws IOException;

  /**
   * Skips the reading of spatial(cordinates) data for the given index
   */
  abstract protected void skipSpatial(int index) throws IOException;

  abstract protected void skipSpatial2(int index1, int index2) throws IOException;

  /**
   * Read attribute data for the current position in the datastorage
   */
  abstract public void readRecAttribute(Record rec)
   throws IOException;

  /*
   * Read next record Spatial Data from opened DataStorage.
   * Returns <code>true</code> if record is within given
   * <code>extent</code>
   */
  abstract public boolean readRecCoords(Record rec,  Rectangle2 extent)
   throws IOException;

  /**
   * Reads the record with the given
   * index. It opens and closes the Datastorage automatically.
   *
   * whatNeed
   * 0 - both coordinates and attributes
   * 1 - attributes only
   * 2 - coordinates only
   */
  abstract public Record getRecord(int ind, int whatNeed);


  /**
   * Returns the empty record with given index and not filled array
   * of attributes
   */
  abstract public Record newRecord(int index);

  /**
   *
   */
  abstract public void clear();

  /**
   * Creates a clone of this object.
   * This method is utilized to copy data for Projection operation
   * (to store the original projection)
   */
  abstract public DataStorage copy();

//---------------- EDIT ROUTINES -----------------------------------
  /*
   * Delete record from storage
   */
  abstract public void deleteRecord(Record rec);

  static public final int _EDIT_ALL  = 0;
  static public final int _EDIT_ATR  = 1;
  static public final int _EDIT_CRD  = 2;

  /**
   * Change/Add record
   *
   * 0 - both coordinates and attributes
   * 1 - attributes only
   * 2 - coordinates only
   */
  abstract public void changeRecord(Record rec, int whatNeed);

  /**
   * Union storages - to add new porion of data on server download
   */
  abstract protected void unionStorages(DataStorage ds2);

//---------------- COMMON ROUTINES ---------------------------------

  /**
   * Creates new empty arrays of fields definitions.
   *
   * @see DataStorage#fieldOriginalCount
   * @see FieldDef
   */
  /*public FieldDef[] initFields(int count){
     fieldOriginalCount = count;
     if(count>0) fieldDefs = new FieldDef[count];
     return fieldDefs;
  }*/

  /**
   * Returns fields definitions array.
   *
   * @see FieldDef
   */
/*  public FieldDef[] getFields(){
     return fieldDefs;
  }*/

  /**
   * Assign array of field definitions to this layer.
   *
   * @param defs - array of fields definitions
   * @see FieldDef
   */
/*  public void assignFields(FieldDef[] defs){
     fieldDefs = defs;
  }*/

   /**
   * Sets a value that indicates the type of geometric shape
   * associated with a Layer.
   *
   * @see getObjectType
   */
  public void setObjectType(int value){
     objectType = value;
  }

  /**
   * Returns a value that indicates the type of geometric shape
   * associated with a Layer.<br>
   *  <table>
   *  <tr><td>POINT</td><td>     1  </td><td>Point features</td></tr>
   *  <tr><td>LINE</td><td>      2  </td><td>Line features</td></tr>
   *  <tr><td>POLYGON</td><td>   3  </td><td>Polygon features</td></tr>
   *  <tr><td>BOUND_BOX</td><td> 4  </td><td>Rectangle features</td></tr>
   *  </table>
   *
   * @see Const
   */
  public int getObjectType(){
     return objectType;
  }

}
