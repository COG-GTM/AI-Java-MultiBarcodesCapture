package com.zebra.ai_multibarcodes_capture.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class EBarcodesSymbologiesTest {

    @Test
    public void toStringReturnsHumanReadableName() {
        assertEquals("EAN 8", EBarcodesSymbologies.EAN_8.toString());
        assertEquals("GS1 DATABAR EXPANDED", EBarcodesSymbologies.GS1_DATABAR_EXPANDED.toString());
        assertEquals("UNKNOWN", EBarcodesSymbologies.UNKNOWN.toString());
    }

    @Test
    public void unknownUsesNegativeValueAndIsNotEnabledByDefault() {
        assertEquals(-1, EBarcodesSymbologies.UNKNOWN.getIntValue());
        assertFalse(EBarcodesSymbologies.UNKNOWN.getDefaultStatus());
        assertEquals("LABEL-TYPE-UNKNOWN", EBarcodesSymbologies.UNKNOWN.getLabelType());
    }

    @Test
    public void intValuesAreUniqueAndContiguousFromZero() {
        Set<Integer> values = new HashSet<>();
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            assertTrue("duplicate value for " + symbology.name(), values.add(symbology.getIntValue()));
        }
        for (int i = 0; i <= EBarcodesSymbologies.COMPOSITE_C.getIntValue(); i++) {
            assertTrue("missing value " + i, values.contains(i));
        }
    }

    @Test
    public void labelTypesAreUnique() {
        Set<String> labelTypes = new HashSet<>();
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            assertTrue("duplicate label type for " + symbology.name(), labelTypes.add(symbology.getLabelType()));
        }
    }

    @Test
    public void fromIntResolvesEveryDeclaredValue() {
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            assertEquals(symbology, EBarcodesSymbologies.fromInt(symbology.getIntValue()));
        }
    }

    @Test
    public void fromIntReturnsUnknownForOutOfRangeValues() {
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromInt(-1));
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromInt(46));
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromInt(Integer.MAX_VALUE));
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromInt(Integer.MIN_VALUE));
    }

    @Test
    public void fromNameResolvesEveryDeclaredName() {
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            assertEquals(symbology, EBarcodesSymbologies.fromName(symbology.getName()));
        }
    }

    @Test
    public void fromNameReturnsUnknownForUnsupportedName() {
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromName(""));
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromName("code128"));
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromName("NOT A SYMBOLOGY"));
    }

    @Test
    public void fromNameThrowsOnNullInput() {
        assertThrows(NullPointerException.class, () -> EBarcodesSymbologies.fromName(null));
    }

    @Test
    public void fromLabelTypeResolvesEveryDeclaredLabelType() {
        for (EBarcodesSymbologies symbology : EBarcodesSymbologies.values()) {
            assertEquals(symbology, EBarcodesSymbologies.fromLabelType(symbology.getLabelType()));
        }
    }

    @Test
    public void fromLabelTypeReturnsUnknownForUnsupportedLabelType() {
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromLabelType(""));
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromLabelType("LABEL-TYPE-NOPE"));
        assertEquals(EBarcodesSymbologies.UNKNOWN, EBarcodesSymbologies.fromLabelType("label-type-qrcode"));
    }

    @Test
    public void fromLabelTypeThrowsOnNullInput() {
        assertThrows(NullPointerException.class, () -> EBarcodesSymbologies.fromLabelType(null));
    }

    @Test
    public void commonRetailSymbologiesAreEnabledByDefault() {
        assertTrue(EBarcodesSymbologies.EAN_13.getDefaultStatus());
        assertTrue(EBarcodesSymbologies.UPC_A.getDefaultStatus());
        assertTrue(EBarcodesSymbologies.CODE128.getDefaultStatus());
        assertTrue(EBarcodesSymbologies.QRCODE.getDefaultStatus());
        assertFalse(EBarcodesSymbologies.MSI.getDefaultStatus());
        assertFalse(EBarcodesSymbologies.I2OF5.getDefaultStatus());
    }
}
