package com.zebra.ai_multibarcodes_capture.filemanagement;

import androidx.annotation.NonNull;

public enum EExportMode {
    TEXT("TEXT"),
    CSV("CSV"),
    EXCEL("EXCEL");

    private String name = "";
    EExportMode(String name)
    {
        this.name = name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public String getExtension()
    {
        return switch (this) {
            case TEXT -> ".txt";
            case CSV -> ".csv";
            case EXCEL -> ".xlsx";
            default -> ".txt";
        };
    }

    public static EExportMode fromExtension(String value)
    {
        return switch (value) {
            case ".csv" -> CSV;
            case ".xlsx" -> EXCEL;
            default -> TEXT;
        };
    }
}
