package com.zebra.ai_multibarcodes_capture.helpers;

import com.zebra.ai_multibarcodes_capture.filemanagement.EExportMode;

import androidx.annotation.NonNull;

public enum  EBarcodesSymbologies {
    UNKNOWN("UNKNOWN", -1, false, "LABEL-TYPE-UNKNOWN"),
    EAN_8("EAN 8", 0, true, "LABEL-TYPE-EAN8"),
    EAN_13("EAN 13", 1, true, "LABEL-TYPE-EAN13"),
    UPC_A("UPC A", 2, true, "LABEL-TYPE-UPCA"),
    UPC_E("UPC E", 3, true, "LABEL-TYPE-UPCE"),
    AZTEC("AZTEC", 4, true, "LABEL-TYPE-AZTEC"),
    CODABAR("CODABAR", 5, true, "LABEL-TYPE-CODABAR"),
    CODE128("CODE128", 6, true, "LABEL-TYPE-CODE128"),
    CODE39("CODE39", 7, true, "LABEL-TYPE-CODE39"),
    I2OF5("I2OF5", 8, false, "LABEL-TYPE-I2OF5"),
    GS1_DATABAR("GS1 DATABAR", 9, true, "LABEL-TYPE-GS1DATABAR"),
    DATAMATRIX("DATAMATRIX", 10, true, "LABEL-TYPE-DATAMATRIX"),
    GS1_DATABAR_EXPANDED("GS1 DATABAR EXPANDED", 11, true, "LABEL-TYPE-GS1DATABAREXPANDED"),
    MAILMARK("MAILMARK", 12, true, "LABEL-TYPE-MAILMARK"),
    MAXICODE("MAXICODE", 13, true, "LABEL-TYPE-MAXICODE"),
    PDF417("PDF417", 14, true, "LABEL-TYPE-PDF417"),
    QRCODE("QRCODE", 15, true, "LABEL-TYPE-QRCODE"),
    DOTCODE("DOTCODE", 16, false, "LABEL-TYPE-DOTCODE"),
    GRID_MATRIX("GRID MATRIX", 17, false, "LABEL-TYPE-GRIDMATRIX"),
    GS1_DATAMATRIX("GS1 DATAMATRIX", 18, false, "LABEL-TYPE-GS1DATAMATRIX"),
    GS1_QRCODE("GS1 QRCODE", 19, false, "LABEL-TYPE-GS1QRCODE"),
    MICROQR("MICROQR", 20, false, "LABEL-TYPE-MICROQR"),
    MICROPDF("MICROPDF", 21, false, "LABEL-TYPE-MICROPDF"),
    USPOSTNET("USPOSTNET", 22, false, "LABEL-TYPE-USPOSTNET"),
    USPLANET("USPLANET", 23, false, "LABEL-TYPE-USPLANET"),
    UK_POSTAL("UK POSTAL", 24, false, "LABEL-TYPE-UKPOSTAL"),
    JAPANESE_POSTAL("JAPANESE POSTAL", 25, false, "LABEL-TYPE-JAPANESEPOSTAL"),
    AUSTRALIAN_POSTAL("AUSTRALIAN POSTAL", 26, false, "LABEL-TYPE-AUSPOSTAL"),
    CANADIAN_POSTAL("CANADIAN POSTAL", 27, false, "LABEL-TYPE-CANPOSTAL"),
    DUTCH_POSTAL("DUTCH POSTAL", 28, false, "LABEL-TYPE-DUTCHPOSTAL"),
    US4STATE("US4STATE", 29, false, "LABEL-TYPE-US4STATE"),
    US4STATE_FICS("US4STATE FICS", 30, false, "LABEL-TYPE-US4STATEFICS"),
    MSI("MSI", 31, false, "LABEL-TYPE-MSI"),
    CODE93("CODE93", 32, false, "LABEL-TYPE-CODE93"),
    TRIOPTIC39("TRIOPTIC39", 33, false, "LABEL-TYPE-TRIOPTIC39"),
    D2OF5("D2OF5", 34, false, "LABEL-TYPE-D2OF5"),
    CHINESE_2OF5("CHINESE 2OF5", 35, false, "LABEL-TYPE-CHINESE2OF5"),
    KOREAN_3OF5("KOREAN 3OF5", 36, false, "LABEL-TYPE-KOREAN3OF5"),
    CODE11("CODE11", 37, false, "LABEL-TYPE-CODE11"),
    TLC39("TLC39", 38, false, "LABEL-TYPE-TLC39"),
    HANXIN("HANXIN", 39, false, "LABEL-TYPE-HANXIN"),
    MATRIX_2OF5("MATRIX 2OF5", 40, false, "LABEL-TYPE-MATRIX2OF5"),
    UPCE1("UPCE1", 41, false, "LABEL-TYPE-UPCE1"),
    GS1_DATABAR_LIM("GS1 DATABAR LIM", 42, false, "LABEL-TYPE-GS1DATABARLIM"),
    FINNISH_POSTAL_4S("FINNISH POSTAL 4S", 43, false, "LABEL-TYPE-FINNISHPOSTAL4S"),
    COMPOSITE_AB("COMPOSITE AB", 44, false, "LABEL-TYPE-COMPOSITEAB"),
    COMPOSITE_C("COMPOSITE C", 45, false, "LABEL-TYPE-COMPOSITEC");

    private String name = "";
    private int value = -1;
    private boolean defaultStatus = false;
    private String labelType = "";

    EBarcodesSymbologies(String name, int value, boolean defaultStatus, String labelType)
    {
        this.name = name;
        this.value = value;
        this.defaultStatus = defaultStatus;
        this.labelType = labelType;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public int getIntValue() {
        return value;
    }

    @NonNull
    public boolean getDefaultStatus() {
        return defaultStatus;
    }

    @NonNull
    public String getLabelType() {
        return labelType;
    }

    public static EBarcodesSymbologies fromName(String name)
    {
        return switch (name) {
            case "AUSTRALIAN POSTAL" -> AUSTRALIAN_POSTAL;
            case "AZTEC" -> AZTEC;
            case "CANADIAN POSTAL" -> CANADIAN_POSTAL;
            case "CHINESE 2OF5" -> CHINESE_2OF5;
            case "CODABAR" -> CODABAR;
            case "CODE11" -> CODE11;
            case "CODE39" -> CODE39;
            case "CODE93" -> CODE93;
            case "CODE128" -> CODE128;
            case "COMPOSITE AB" -> COMPOSITE_AB;
            case "COMPOSITE C" -> COMPOSITE_C;
            case "D2OF5" -> D2OF5;
            case "DATAMATRIX" -> DATAMATRIX;
            case "DOTCODE" -> DOTCODE;
            case "DUTCH POSTAL" -> DUTCH_POSTAL;
            case "EAN 8" -> EAN_8;
            case "EAN 13" -> EAN_13;
            case "FINNISH POSTAL 4S" -> FINNISH_POSTAL_4S;
            case "GRID MATRIX" -> GRID_MATRIX;
            case "GS1 DATABAR" -> GS1_DATABAR;
            case "GS1 DATABAR EXPANDED" -> GS1_DATABAR_EXPANDED;
            case "GS1 DATABAR LIM" -> GS1_DATABAR_LIM;
            case "GS1 DATAMATRIX" -> GS1_DATAMATRIX;
            case "GS1 QRCODE" -> GS1_QRCODE;
            case "HANXIN" -> HANXIN;
            case "I2OF5" -> I2OF5;
            case "JAPANESE POSTAL" -> JAPANESE_POSTAL;
            case "KOREAN 3OF5" -> KOREAN_3OF5;
            case "MAILMARK" -> MAILMARK;
            case "MATRIX 2OF5" -> MATRIX_2OF5;
            case "MAXICODE" -> MAXICODE;
            case "MICROPDF" -> MICROPDF;
            case "MICROQR" -> MICROQR;
            case "MSI" -> MSI;
            case "PDF417" -> PDF417;
            case "QRCODE" -> QRCODE;
            case "TLC39" -> TLC39;
            case "TRIOPTIC39" -> TRIOPTIC39;
            case "UK POSTAL" -> UK_POSTAL;
            case "UPC A" -> UPC_A;
            case "UPC E" -> UPC_E;
            case "UPCE1" -> UPCE1;
            case "USPLANET" -> USPLANET;
            case "USPOSTNET" -> USPOSTNET;
            case "US4STATE" -> US4STATE;
            case "US4STATE FICS" -> US4STATE_FICS;
            default -> UNKNOWN;
        };
    }

    public static EBarcodesSymbologies fromInt(int value)
    {
        return switch (value) {
            case 0 -> EAN_8;
            case 1 -> EAN_13;
            case 2 -> UPC_A;
            case 3 -> UPC_E;
            case 4 -> AZTEC;
            case 5 -> CODABAR;
            case 6 -> CODE128;
            case 7 -> CODE39;
            case 8 -> I2OF5;
            case 9 -> GS1_DATABAR;
            case 10 -> DATAMATRIX;
            case 11 -> GS1_DATABAR_EXPANDED;
            case 12 -> MAILMARK;
            case 13 -> MAXICODE;
            case 14 -> PDF417;
            case 15 -> QRCODE;
            case 16 -> DOTCODE;
            case 17 -> GRID_MATRIX;
            case 18 -> GS1_DATAMATRIX;
            case 19 -> GS1_QRCODE;
            case 20 -> MICROQR;
            case 21 -> MICROPDF;
            case 22 -> USPOSTNET;
            case 23 -> USPLANET;
            case 24 -> UK_POSTAL;
            case 25 -> JAPANESE_POSTAL;
            case 26 -> AUSTRALIAN_POSTAL;
            case 27 -> CANADIAN_POSTAL;
            case 28 -> DUTCH_POSTAL;
            case 29 -> US4STATE;
            case 30 -> US4STATE_FICS;
            case 31 -> MSI;
            case 32 -> CODE93;
            case 33 -> TRIOPTIC39;
            case 34 -> D2OF5;
            case 35 -> CHINESE_2OF5;
            case 36 -> KOREAN_3OF5;
            case 37 -> CODE11;
            case 38 -> TLC39;
            case 39 -> HANXIN;
            case 40 -> MATRIX_2OF5;
            case 41 -> UPCE1;
            case 42 -> GS1_DATABAR_LIM;
            case 43 -> FINNISH_POSTAL_4S;
            case 44 -> COMPOSITE_AB;
            case 45 -> COMPOSITE_C;
            default -> UNKNOWN;
        };
    }


    public static EBarcodesSymbologies fromLabelType(String labelType)
    {
        return switch (labelType) {
            case "LABEL-TYPE-AUSPOSTAL" -> AUSTRALIAN_POSTAL;
            case "LABEL-TYPE-AZTEC" -> AZTEC;
            case "LABEL-TYPE-CANPOSTAL" -> CANADIAN_POSTAL;
            case "LABEL-TYPE-CHINESE2OF5" -> CHINESE_2OF5;
            case "LABEL-TYPE-CODABAR" -> CODABAR;
            case "LABEL-TYPE-CODE11" -> CODE11;
            case "LABEL-TYPE-CODE39" -> CODE39;
            case "LABEL-TYPE-CODE93" -> CODE93;
            case "LABEL-TYPE-CODE128" -> CODE128;
            case "LABEL-TYPE-COMPOSITEAB" -> COMPOSITE_AB;
            case "LABEL-TYPE-COMPOSITEC" -> COMPOSITE_C;
            case "LABEL-TYPE-D2OF5" -> D2OF5;
            case "LABEL-TYPE-DATAMATRIX" -> DATAMATRIX;
            case "LABEL-TYPE-DOTCODE" -> DOTCODE;
            case "LABEL-TYPE-DUTCHPOSTAL" -> DUTCH_POSTAL;
            case "LABEL-TYPE-EAN8" -> EAN_8;
            case "LABEL-TYPE-EAN13" -> EAN_13;
            case "LABEL-TYPE-FINNISHPOSTAL4S" -> FINNISH_POSTAL_4S;
            case "LABEL-TYPE-GRIDMATRIX" -> GRID_MATRIX;
            case "LABEL-TYPE-GS1DATABAR" -> GS1_DATABAR;
            case "LABEL-TYPE-GS1DATABAREXPANDED" -> GS1_DATABAR_EXPANDED;
            case "LABEL-TYPE-GS1DATABARLIM" -> GS1_DATABAR_LIM;
            case "LABEL-TYPE-GS1DATAMATRIX" -> GS1_DATAMATRIX;
            case "LABEL-TYPE-GS1QRCODE" -> GS1_QRCODE;
            case "LABEL-TYPE-HANXIN" -> HANXIN;
            case "LABEL-TYPE-I2OF5" -> I2OF5;
            case "LABEL-TYPE-JAPANESEPOSTAL" -> JAPANESE_POSTAL;
            case "LABEL-TYPE-KOREAN3OF5" -> KOREAN_3OF5;
            case "LABEL-TYPE-MAILMARK" -> MAILMARK;
            case "LABEL-TYPE-MATRIX2OF5" -> MATRIX_2OF5;
            case "LABEL-TYPE-MAXICODE" -> MAXICODE;
            case "LABEL-TYPE-MICROPDF" -> MICROPDF;
            case "LABEL-TYPE-MICROQR" -> MICROQR;
            case "LABEL-TYPE-MSI" -> MSI;
            case "LABEL-TYPE-PDF417" -> PDF417;
            case "LABEL-TYPE-QRCODE" -> QRCODE;
            case "LABEL-TYPE-TLC39" -> TLC39;
            case "LABEL-TYPE-TRIOPTIC39" -> TRIOPTIC39;
            case "LABEL-TYPE-UKPOSTAL" -> UK_POSTAL;
            case "LABEL-TYPE-UPCA" -> UPC_A;
            case "LABEL-TYPE-UPCE" -> UPC_E;
            case "LABEL-TYPE-UPCE1" -> UPCE1;
            case "LABEL-TYPE-USPLANET" -> USPLANET;
            case "LABEL-TYPE-USPOSTNET" -> USPOSTNET;
            case "LABEL-TYPE-US4STATE" -> US4STATE;
            case "LABEL-TYPE-US4STATEFICS" -> US4STATE_FICS;
            default -> UNKNOWN;
        };
    }

}
