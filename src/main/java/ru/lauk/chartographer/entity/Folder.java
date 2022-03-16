package ru.lauk.chartographer.entity;

import ru.lauk.chartographer.helpers.FilesHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Folder {

    private final String            folderName;
    private final List<SimpleFile>  simpleFiles = new ArrayList<>(20);

    public Folder(int width, int height) {
        this.folderName = FilesHelper.getNewFileName(width, height);
        createSimpleFiles(width, height);
    }

    public Folder(File folder) {
        this.folderName = folder.getName();
        createSimpleFiles(folder);
    }

    public Folder(Folder folder) throws IOException {
        this.folderName = "temp_" + folder.getFolderName();
        Files.createDirectory(Path.of(getAbsolutePath()));
    }

    public String getFolderName() {
        return folderName;
    }

    public List<SimpleFile> getSimpleFiles() {
        return simpleFiles;
    }

    public void addSimpleFile(SimpleFile simpleFile) {
        this.simpleFiles.add(simpleFile);
    }

    public void createSimpleFiles(int width, int height) {
        int sumHeight = 0;
        int partNumber = 0;

        while (height > sumHeight) {
            int writeHeight = Math.min(1000, height - sumHeight);
            simpleFiles.add(
                    new SimpleFile(width, writeHeight, partNumber++, this));
            sumHeight += writeHeight;
        }
    }

    public void createSimpleFiles(File folder) {
        List<File> fileList =
                FilesHelper.findAllFiles(folder.getAbsolutePath());

        if (fileList == null) {
            return;
        }

        fileList.forEach(file -> simpleFiles.add(new SimpleFile(file, this)));
        simpleFiles.sort(Comparator.comparingInt(SimpleFile::getPartNumber));
    }

    public String getAbsolutePath() {
        return FilesHelper.getFolderPath(folderName);
    }
}
