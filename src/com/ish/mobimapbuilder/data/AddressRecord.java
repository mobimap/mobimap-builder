/*****************************************************************************/
/*                      M O B I M A P   B U I L D E R                        */
/*                            (c) 2006-2008 ISh                              */
/*****************************************************************************/
package com.ish.mobimapbuilder.data;

import java.text.*;

import com.ish.isrt.core.data.*;
import com.ish.mobimapbuilder.util.*;

public class AddressRecord extends AbstractRecord implements Comparable {
    private double x, y;
    private String streetId;
    private String streetName;
    private String houseNumber;
    private String streetType;
    private String streetVillage;
    private tref ref;

    public AddressRecord (double x, double y,
                          String streetId, String streetName,
                          String streetType, String streetVillage, String number) {
        this.x = x;
        this.y = y;
        this.streetId = streetId;
        this.streetName = streetName == null ? "" : streetName.trim ();
        this.streetType = streetType == null ? "" : streetType.trim ();
        this.houseNumber = number;
    }

    public int compareTo (Object oj) {
        AddressRecord r = (AddressRecord) oj;
        Collator myCollator = Collator.getInstance ();
        myCollator.setStrength (Collator.IDENTICAL);
        myCollator.setDecomposition (Collator.CANONICAL_DECOMPOSITION);
        return myCollator.compare (houseNumber, r.houseNumber);
    }

    public static String formFullName (String number, String letter, String building) {
        if (StringUtils.isNullOrEmptyOrZero (number)) {
            return null;
        }

        String full = number;
        if (!StringUtils.isNullOrEmpty (letter)) {
            full += letter;
        }
        if (!StringUtils.isNullOrEmptyOrZero (building)) {
            full += "/" + building;
        }

        return full;
    }

    public double getY () {
        return y;
    }

    public double getX () {
        return x;
    }

    public String getStreetName () {
        return streetName;
    }

    public void setHouseNumber (String houseNumber) {

        this.houseNumber = houseNumber;
    }

    public void setY (double y) {
        this.y = y;
    }

    public void setX (double x) {
        this.x = x;
    }

    public void setStreetName (String streetName) {
        this.streetName = streetName;
    }

    public void setStreetType (String streetType) {
        this.streetType = streetType;
    }

    public void setStreetVillage (String streetVillage) {
        this.streetVillage = streetVillage;
    }

    public void setRef (tref ref) {
        this.ref = ref;
    }

    public void setStreetId (String streetId) {
        this.streetId = streetId;
    }

    public String getHouseNumber () {

        return houseNumber;
    }

    public String getStreetType () {
        return streetType;
    }

    public String getStreetVillage () {
        return streetVillage;
    }

    public tref getRef () {
        return ref;
    }

    public String getStreetId () {
        return streetId;
    }
}
