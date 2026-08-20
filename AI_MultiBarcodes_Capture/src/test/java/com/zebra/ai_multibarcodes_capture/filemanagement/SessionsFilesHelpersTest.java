package com.zebra.ai_multibarcodes_capture.filemanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import com.zebra.ai_multibarcodes_capture.helpers.EBarcodesSymbologies;
import com.zebra.ai_multibarcodes_capture.helpers.SessionData;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.rules.TemporaryFolder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Date;

@RunWith(RobolectricTestRunner.class)
public class SessionsFilesHelpersTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Context context;

    @Before
    public void setUp() {
        context = RuntimeEnvironment.getApplication();
    }

    private static Date todayAt(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private static SessionData sessionData(String value, EBarcodesSymbologies symbology, int quantity, Date date) {
        SessionData sessionData = new SessionData();
        sessionData.barcodeValuesMap.put(0, value);
        sessionData.barcodeSymbologyMap.put(0, symbology.getIntValue());
        sessionData.barcodeQuantityMap.put(0, quantity);
        sessionData.barcodeDateMap.put(0, date);
        return sessionData;
    }

    private static void assertSameTimeOfDay(Date expected, Date actual) {
        Calendar expectedCalendar = Calendar.getInstance();
        expectedCalendar.setTime(expected);
        Calendar actualCalendar = Calendar.getInstance();
        actualCalendar.setTime(actual);
        assertEquals(expectedCalendar.get(Calendar.HOUR_OF_DAY), actualCalendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(expectedCalendar.get(Calendar.MINUTE), actualCalendar.get(Calendar.MINUTE));
        assertEquals(expectedCalendar.get(Calendar.SECOND), actualCalendar.get(Calendar.SECOND));
    }

    private String contentOf(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    // ----------------------------------------------------------------- TXT

    @Test
    public void saveDataTXTWritesHeaderAndBarcodeEntry() throws IOException {
        File file = temporaryFolder.newFile("session.txt");

        assertTrue(SessionsFilesHelpers.saveDataTXT(context, file,
                sessionData("1234567890", EBarcodesSymbologies.CODE128, 3, todayAt(10, 20, 30))));

        String content = contentOf(file);
        assertTrue(content.contains("Capture file:session.txt"));
        assertTrue(content.contains("Value:1234567890"));
        assertTrue(content.contains("Symbology:CODE128"));
        assertTrue(content.contains("Quantity:3"));
        assertTrue(content.contains("Capture Date:"));
    }

    @Test
    public void saveDataTXTAppendsWithoutDuplicatingHeader() throws IOException {
        File file = temporaryFolder.newFile("session.txt");
        Date date = todayAt(8, 0, 0);

        SessionsFilesHelpers.saveDataTXT(context, file, sessionData("AAA", EBarcodesSymbologies.QRCODE, 1, date));
        SessionsFilesHelpers.saveDataTXT(context, file, sessionData("BBB", EBarcodesSymbologies.QRCODE, 1, date));

        String content = contentOf(file);
        assertEquals(1, content.split("Capture file:", -1).length - 1);
        assertTrue(content.contains("Value:AAA"));
        assertTrue(content.contains("Value:BBB"));
    }

    @Test
    public void saveDataTXTSkipsEmptyBarcodeValues() throws IOException {
        File file = temporaryFolder.newFile("session.txt");
        SessionData sessionData = sessionData("", EBarcodesSymbologies.CODE39, 1, todayAt(9, 9, 9));

        assertTrue(SessionsFilesHelpers.saveDataTXT(context, file, sessionData));

        assertFalse(contentOf(file).contains("Value:"));
    }

    @Test
    public void saveDataTXTUsesUnknownSymbologyWhenMappingIsMissing() throws IOException {
        File file = temporaryFolder.newFile("session.txt");
        SessionData sessionData = new SessionData();
        sessionData.barcodeValuesMap.put(0, "NOSYMBOLOGY");

        assertTrue(SessionsFilesHelpers.saveDataTXT(context, file, sessionData));

        String content = contentOf(file);
        assertTrue(content.contains("Symbology:UNKNOWN"));
        assertTrue(content.contains("Quantity:1"));
    }

    @Test
    public void txtRoundTripPreservesValueSymbologyAndQuantity() throws IOException {
        File file = temporaryFolder.newFile("session.txt");
        SessionsFilesHelpers.saveDataTXT(context, file,
                sessionData("0123456789012", EBarcodesSymbologies.EAN_13, 5, todayAt(14, 30, 45)));

        SessionData loaded = SessionsFilesHelpers.loadDataTXT(context, file);

        assertEquals(1, loaded.barcodeValuesMap.size());
        assertEquals("0123456789012", loaded.barcodeValuesMap.get(0));
        assertEquals(Integer.valueOf(EBarcodesSymbologies.EAN_13.getIntValue()), loaded.barcodeSymbologyMap.get(0));
        assertEquals(Integer.valueOf(5), loaded.barcodeQuantityMap.get(0));
        assertNotNull(loaded.barcodeDateMap.get(0));
    }

    @Test
    public void loadDataTXTIndexesEntriesInFileOrder() throws IOException {
        File file = temporaryFolder.newFile("session.txt");
        Files.write(file.toPath(), ("-----------------------------------------\n"
                + "Capture file:session.txt\n"
                + "Created the:whatever\n"
                + "-----------------------------------------\n"
                + "Value:FIRST\nSymbology:CODE128\nQuantity:2\nCapture Date:2025-01-21 14:30:45\n"
                + "-----------------------------------------\n"
                + "Value:SECOND\nSymbology:QRCODE\nQuantity:7\nCapture Date:2025-01-22 15:31:46\n"
                + "-----------------------------------------\n").getBytes(StandardCharsets.UTF_8));

        SessionData loaded = SessionsFilesHelpers.loadDataTXT(context, file);

        assertEquals(2, loaded.barcodeValuesMap.size());
        assertEquals("FIRST", loaded.barcodeValuesMap.get(0));
        assertEquals("SECOND", loaded.barcodeValuesMap.get(1));
        assertEquals(Integer.valueOf(2), loaded.barcodeQuantityMap.get(0));
        assertEquals(Integer.valueOf(7), loaded.barcodeQuantityMap.get(1));
        assertEquals(Integer.valueOf(EBarcodesSymbologies.CODE128.getIntValue()), loaded.barcodeSymbologyMap.get(0));
        assertEquals(Integer.valueOf(EBarcodesSymbologies.QRCODE.getIntValue()), loaded.barcodeSymbologyMap.get(1));
    }

    @Test
    public void loadDataTXTFallsBackToQuantityOneOnInvalidQuantityAndUnknownOnInvalidSymbology() throws IOException {
        File file = temporaryFolder.newFile("session.txt");
        Files.write(file.toPath(), ("Value:BROKEN\nSymbology:NOT A SYMBOLOGY\nQuantity:many\n"
                + "Capture Date:2025-01-21 14:30:45\n").getBytes(StandardCharsets.UTF_8));

        SessionData loaded = SessionsFilesHelpers.loadDataTXT(context, file);

        assertEquals(Integer.valueOf(1), loaded.barcodeQuantityMap.get(0));
        assertEquals(Integer.valueOf(EBarcodesSymbologies.UNKNOWN.getIntValue()), loaded.barcodeSymbologyMap.get(0));
    }

    @Test
    public void loadDataTXTReturnsEmptySessionForEmptyOrMissingFile() throws IOException {
        SessionData fromEmpty = SessionsFilesHelpers.loadDataTXT(context, temporaryFolder.newFile("empty.txt"));
        SessionData fromMissing = SessionsFilesHelpers.loadDataTXT(context,
                new File(temporaryFolder.getRoot(), "missing.txt"));

        assertTrue(fromEmpty.barcodeValuesMap.isEmpty());
        assertTrue(fromMissing.barcodeValuesMap.isEmpty());
    }

    // ----------------------------------------------------------------- CSV

    @Test
    public void saveDataCSVWritesHeaderAndSemicolonSeparatedRow() throws IOException {
        File file = temporaryFolder.newFile("session.csv");

        assertTrue(SessionsFilesHelpers.saveDataCSV(context, file,
                sessionData("ABC123", EBarcodesSymbologies.CODE39, 4, todayAt(11, 22, 33))));

        String[] lines = contentOf(file).split("\n");
        assertEquals("Date;Symbology;Data;Quantitiy", lines[0]);
        assertEquals("11:22:33;CODE39;ABC123;4", lines[1]);
    }

    @Test
    public void csvRoundTripPreservesValueSymbologyQuantityAndTime() throws IOException {
        File file = temporaryFolder.newFile("session.csv");
        Date date = todayAt(16, 5, 9);
        SessionsFilesHelpers.saveDataCSV(context, file,
                sessionData("XYZ", EBarcodesSymbologies.DATAMATRIX, 2, date));

        SessionData loaded = SessionsFilesHelpers.loadDataCSV(context, file);

        assertEquals("XYZ", loaded.barcodeValuesMap.get(0));
        assertEquals(Integer.valueOf(EBarcodesSymbologies.DATAMATRIX.getIntValue()), loaded.barcodeSymbologyMap.get(0));
        assertEquals(Integer.valueOf(2), loaded.barcodeQuantityMap.get(0));
        assertSameTimeOfDay(date, loaded.barcodeDateMap.get(0));
    }

    @Test
    public void loadDataCSVSkipsMalformedLinesAndEmptyValues() throws IOException {
        File file = temporaryFolder.newFile("session.csv");
        Files.write(file.toPath(), ("Date;Symbology;Data;Quantitiy\n"
                + "10:00:00;CODE128;GOOD;1\n"
                + "not;enough\n"
                + "10:00:02;CODE128;;3\n"
                + "10:00:03;CODE128;ALSOGOOD;notanumber\n").getBytes(StandardCharsets.UTF_8));

        SessionData loaded = SessionsFilesHelpers.loadDataCSV(context, file);

        assertTrue(loaded.barcodeValuesMap.containsValue("GOOD"));
        assertTrue(loaded.barcodeValuesMap.containsValue("ALSOGOOD"));
        assertEquals(2, loaded.barcodeValuesMap.size());
        assertFalse(loaded.barcodeValuesMap.containsValue(""));
    }

    @Test
    public void loadDataCSVWithoutHeaderTreatsFirstLineAsData() throws IOException {
        File file = temporaryFolder.newFile("session.csv");
        Files.write(file.toPath(), "09:08:07;QRCODE;FIRSTLINE;1\n".getBytes(StandardCharsets.UTF_8));

        SessionData loaded = SessionsFilesHelpers.loadDataCSV(context, file);

        assertEquals("FIRSTLINE", loaded.barcodeValuesMap.get(0));
    }

    @Test
    public void loadDataCSVReturnsEmptySessionForEmptyFile() throws IOException {
        SessionData loaded = SessionsFilesHelpers.loadDataCSV(context, temporaryFolder.newFile("empty.csv"));

        assertTrue(loaded.barcodeValuesMap.isEmpty());
    }

    // ---------------------------------------------------------------- XLSX

    @Test
    public void saveDataXSLXCreatesDataSheetWithHeaderRow() throws IOException {
        File file = temporaryFolder.newFile("session.xlsx");

        assertTrue(SessionsFilesHelpers.saveDataXSLX(context, file,
                sessionData("BARCODE-1", EBarcodesSymbologies.PDF417, 6, todayAt(7, 6, 5))));

        try (FileInputStream fis = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Data", sheet.getSheetName());
            Row header = sheet.getRow(0);
            assertEquals("Date", header.getCell(0).getStringCellValue());
            assertEquals("Symbology", header.getCell(1).getStringCellValue());
            assertEquals("Data", header.getCell(2).getStringCellValue());
            assertEquals("Quantity", header.getCell(3).getStringCellValue());

            Row row = sheet.getRow(1);
            assertEquals("07:06:05", row.getCell(0).getStringCellValue());
            assertEquals("PDF417", row.getCell(1).getStringCellValue());
            assertEquals("BARCODE-1", row.getCell(2).getStringCellValue());
            assertEquals(6, (int) row.getCell(3).getNumericCellValue());
        }
    }

    @Test
    public void saveDataXSLXAppendsRowsToAnExistingWorkbook() throws IOException {
        File file = temporaryFolder.newFile("session.xlsx");
        Date date = todayAt(12, 0, 0);
        SessionsFilesHelpers.saveDataXSLX(context, file, sessionData("FIRST", EBarcodesSymbologies.QRCODE, 1, date));
        SessionsFilesHelpers.saveDataXSLX(context, file, sessionData("SECOND", EBarcodesSymbologies.QRCODE, 1, date));

        SessionData loaded = SessionsFilesHelpers.loadDataXSLX(context, file);

        assertEquals(2, loaded.barcodeValuesMap.size());
        assertEquals("FIRST", loaded.barcodeValuesMap.get(0));
        assertEquals("SECOND", loaded.barcodeValuesMap.get(1));
    }

    @Test
    public void xlsxRoundTripPreservesValueSymbologyQuantityAndTime() throws IOException {
        File file = temporaryFolder.newFile("session.xlsx");
        Date date = todayAt(18, 45, 1);
        SessionsFilesHelpers.saveDataXSLX(context, file,
                sessionData("SN-42", EBarcodesSymbologies.AZTEC, 9, date));

        SessionData loaded = SessionsFilesHelpers.loadDataXSLX(context, file);

        assertEquals("SN-42", loaded.barcodeValuesMap.get(0));
        assertEquals(Integer.valueOf(EBarcodesSymbologies.AZTEC.getIntValue()), loaded.barcodeSymbologyMap.get(0));
        assertEquals(Integer.valueOf(9), loaded.barcodeQuantityMap.get(0));
        assertSameTimeOfDay(date, loaded.barcodeDateMap.get(0));
    }

    @Test
    public void loadDataXSLXSkipsRowsWithoutBarcodeValue() throws IOException {
        File file = temporaryFolder.newFile("session.xlsx");
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            Row emptyValueRow = sheet.createRow(1);
            emptyValueRow.createCell(0).setCellValue("10:00:00");
            emptyValueRow.createCell(1).setCellValue("CODE128");
            emptyValueRow.createCell(2).setCellValue("   ");
            emptyValueRow.createCell(3).setCellValue(1);
            Row validRow = sheet.createRow(3);
            validRow.createCell(0).setCellValue("10:00:01");
            validRow.createCell(1).setCellValue("CODE128");
            validRow.createCell(2).setCellValue("KEEPME");
            validRow.createCell(3).setCellValue(2);
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                workbook.write(fos);
            }
        }

        SessionData loaded = SessionsFilesHelpers.loadDataXSLX(context, file);

        assertEquals(1, loaded.barcodeValuesMap.size());
        assertEquals("KEEPME", loaded.barcodeValuesMap.get(0));
        assertEquals(Integer.valueOf(2), loaded.barcodeQuantityMap.get(0));
    }

    @Test
    public void loadDataXSLXReturnsEmptySessionForEmptyFile() throws IOException {
        SessionData loaded = SessionsFilesHelpers.loadDataXSLX(context, temporaryFolder.newFile("empty.xlsx"));

        assertTrue(loaded.barcodeValuesMap.isEmpty());
    }

    // ------------------------------------------------------------ dispatch

    @Test
    public void saveDataAndLoadDataDispatchOnFileExtension() throws IOException {
        File folder = temporaryFolder.newFolder("sessions");
        Date date = todayAt(13, 14, 15);

        for (EExportMode exportMode : EExportMode.values()) {
            File file = new File(folder, "dispatch" + exportMode.getExtension());
            assertTrue(exportMode.toString(), SessionsFilesHelpers.saveData(context, file.getPath(),
                    sessionData("DISPATCH", EBarcodesSymbologies.CODE128, 1, date)));
            assertTrue(exportMode.toString(), file.exists());

            SessionData loaded = SessionsFilesHelpers.loadData(context, file.getPath());
            assertNotNull(exportMode.toString(), loaded);
            assertEquals(exportMode.toString(), "DISPATCH", loaded.barcodeValuesMap.get(0));
        }
    }

    @Test
    public void loadDataReturnsNullWhenFileDoesNotExist() {
        assertNull(SessionsFilesHelpers.loadData(context,
                new File(temporaryFolder.getRoot(), "missing.txt").getPath()));
    }

    @Test
    public void saveDataCreatesTheFileWhenItDoesNotExistYet() {
        File file = new File(temporaryFolder.getRoot(), "created.txt");

        assertTrue(SessionsFilesHelpers.saveData(context, file.getPath(),
                sessionData("NEW", EBarcodesSymbologies.CODE128, 1, todayAt(1, 2, 3))));
        assertTrue(file.exists());
    }
}
