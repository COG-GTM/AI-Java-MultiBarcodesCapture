package com.zebra.ai_multibarcodes_capture.filemanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FileUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private File writeFile(String name, String content) throws IOException {
        File file = temporaryFolder.newFile(name);
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    @Test
    public void getFileNameWithoutExtensionStripsLastExtensionOnly() {
        assertEquals("session", FileUtil.getFileNameWithoutExtension(new File("/tmp/session.txt")));
        assertEquals("session.backup", FileUtil.getFileNameWithoutExtension(new File("/tmp/session.backup.xlsx")));
    }

    @Test
    public void getFileNameWithoutExtensionReturnsFullNameWhenNoExtension() {
        assertEquals("session", FileUtil.getFileNameWithoutExtension(new File("/tmp/session")));
    }

    @Test
    public void getFileNameWithoutExtensionReturnsNullForNullFile() {
        assertNull(FileUtil.getFileNameWithoutExtension(null));
    }

    @Test
    public void getFileExtensionIncludesTheDot() {
        assertEquals(".txt", FileUtil.getFileExtension(new File("/tmp/session.txt")));
        assertEquals(".xlsx", FileUtil.getFileExtension(new File("/tmp/session.backup.xlsx")));
    }

    @Test
    public void getFileExtensionReturnsEmptyStringWhenMissingOrTrailingDot() {
        assertEquals("", FileUtil.getFileExtension(new File("/tmp/session")));
        assertEquals("", FileUtil.getFileExtension(new File("/tmp/session.")));
    }

    @Test
    public void getFileExtensionReturnsNullForNullFile() {
        assertNull(FileUtil.getFileExtension(null));
    }

    @Test
    public void getFileExtensionFeedsExportModeResolution() {
        assertEquals(EExportMode.EXCEL, EExportMode.fromExtension(FileUtil.getFileExtension(new File("s.xlsx"))));
        assertEquals(EExportMode.CSV, EExportMode.fromExtension(FileUtil.getFileExtension(new File("s.csv"))));
        assertEquals(EExportMode.TEXT, EExportMode.fromExtension(FileUtil.getFileExtension(new File("s.txt"))));
    }

    @Test
    public void readFileToStringKeepsAllLines() throws IOException {
        File file = writeFile("session.txt", "first\nsecond\nthird\n");

        String content = FileUtil.readFileToString(file);

        List<String> lines = Arrays.asList(content.split(Pattern.quote(System.lineSeparator())));
        assertEquals(Arrays.asList("first", "second", "third"), lines);
    }

    @Test
    public void readFileToStringReturnsEmptyStringForEmptyFile() throws IOException {
        assertEquals("", FileUtil.readFileToString(temporaryFolder.newFile("empty.txt")));
    }

    @Test(expected = IOException.class)
    public void readFileToStringThrowsWhenFileMissing() throws IOException {
        FileUtil.readFileToString(new File(temporaryFolder.getRoot(), "missing.txt"));
    }

    @Test
    public void removeLastLineDropsOnlyTheLastLine() throws IOException {
        File file = writeFile("session.txt", "first\nsecond\nthird\n");

        FileUtil.removeLastLine(file);

        assertEquals(Arrays.asList("first", "second"), Files.readAllLines(file.toPath()));
    }

    @Test
    public void removeLastLineOnSingleLineFileEmptiesIt() throws IOException {
        File file = writeFile("session.txt", "only\n");

        FileUtil.removeLastLine(file);

        assertEquals(0, Files.readAllLines(file.toPath()).size());
    }

    @Test
    public void removeLastLineOnEmptyFileKeepsItEmpty() throws IOException {
        File file = temporaryFolder.newFile("empty.txt");

        FileUtil.removeLastLine(file);

        assertEquals(0, file.length());
    }

    @Test
    public void getLatestModifiedFileReturnsMostRecentMatchingExtension() throws IOException {
        File older = writeFile("older.txt", "older");
        File newer = writeFile("newer.txt", "newer");
        File otherExtension = writeFile("newest.csv", "csv");
        assertTrue(older.setLastModified(1_000_000L));
        assertTrue(newer.setLastModified(2_000_000L));
        assertTrue(otherExtension.setLastModified(3_000_000L));

        assertEquals(newer, FileUtil.getLatestModifiedFile(temporaryFolder.getRoot().getPath(), ".txt"));
    }

    @Test
    public void getLatestModifiedFileReturnsNullWhenNothingMatches() throws IOException {
        writeFile("session.csv", "csv");

        assertNull(FileUtil.getLatestModifiedFile(temporaryFolder.getRoot().getPath(), ".txt"));
    }

    @Test
    public void getLatestModifiedFileReturnsNullForMissingDirectory() {
        File missing = new File(temporaryFolder.getRoot(), "missing");

        assertNull(FileUtil.getLatestModifiedFile(missing.getPath(), ".txt"));
    }

    @Test
    public void copyToFolderCopiesContentAndOverwritesExistingFile() throws IOException {
        File source = writeFile("session.txt", "payload");
        File destinationFolder = temporaryFolder.newFolder("destination");
        File preExisting = new File(destinationFolder, "session.txt");
        Files.write(preExisting.toPath(), "stale".getBytes(StandardCharsets.UTF_8));

        File copy = FileUtil.copyToFolder(source, destinationFolder);

        assertNotNull(copy);
        assertEquals(destinationFolder, copy.getParentFile());
        assertEquals("payload", new String(Files.readAllBytes(copy.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void copyToFolderReturnsNullWhenSourceIsNull() {
        assertNull(FileUtil.copyToFolder(null, temporaryFolder.getRoot()));
    }

    @Test
    public void deleteAllFilesRemovesFilesButKeepsFolders() throws IOException {
        File nested = temporaryFolder.newFolder("nested");
        File nestedFile = new File(nested, "nested.txt");
        assertTrue(nestedFile.createNewFile());
        File rootFile = writeFile("root.txt", "root");

        FileUtil.deleteAllFiles(temporaryFolder.getRoot());

        assertFalse(rootFile.exists());
        assertFalse(nestedFile.exists());
        assertTrue(nested.exists());
    }

    @Test
    public void deleteAllFilesDeletesASingleFile() throws IOException {
        File file = writeFile("session.txt", "payload");

        FileUtil.deleteAllFiles(file);

        assertFalse(file.exists());
    }

    @Test
    public void deleteFolderRecursivelyRemovesTheWholeTree() throws IOException {
        File folder = temporaryFolder.newFolder("tree");
        File subFolder = new File(folder, "sub");
        assertTrue(subFolder.mkdirs());
        assertTrue(new File(subFolder, "leaf.txt").createNewFile());

        assertTrue(FileUtil.deleteFolderRecursively(folder));
        assertFalse(folder.exists());
    }

    @Test
    public void deleteFolderRecursivelyReturnsFalseForMissingFolder() {
        assertFalse(FileUtil.deleteFolderRecursively(new File(temporaryFolder.getRoot(), "missing")));
    }

    @Test
    public void createNewFileNameAppendsTimeStampToPrefix() {
        String fileName = FileUtil.createNewFileName("MySession_");

        assertTrue(fileName, fileName.matches("MySession_\\d{2}_\\d{2}_\\d{2}"));
    }

    @Test
    public void createNewFileCreatesFileWithExportModeExtension() throws IOException {
        File folder = temporaryFolder.newFolder("sessions");

        String fileName = FileUtil.createNewFile(folder, "MySession_", EExportMode.EXCEL);

        assertTrue(new File(folder, fileName + ".xlsx").exists());
    }

    @Test
    public void createNewFileAndReturnFullPathReturnsPathOfCreatedFile() throws IOException {
        File folder = temporaryFolder.newFolder("sessions");

        String path = FileUtil.createNewFileAndReturnFullPath(folder, "MySession_", EExportMode.CSV);

        File created = new File(path);
        assertTrue(created.exists());
        assertTrue(path.endsWith(".csv"));
        assertEquals(folder, created.getParentFile());
    }

    @Test
    public void getTodayDateStringIsNotEmpty() {
        assertFalse(FileUtil.getTodayDateString().isEmpty());
    }
}
