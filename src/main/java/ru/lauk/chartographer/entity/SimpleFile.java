package ru.lauk.chartographer.entity;

import ru.lauk.chartographer.helpers.FilesHelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleFile {

    private final int       width;
    private final int       height;
    private final int       partNumber;
    private final Folder    folder;
    private final String    fileName;
    private File            fileObj;

    public SimpleFile(File simpleFile, Folder folder) {
        String name = FilesHelper.withoutFormat(simpleFile.getName());
        String[] nameArr = name.split("_");
        this.fileObj = simpleFile;
        this.folder = folder;
        this.fileName = name;
        this.partNumber = Integer.parseInt(nameArr[1]);
        this.width = Integer.parseInt(nameArr[2]);
        this.height = Integer.parseInt(nameArr[3]);
    }

    public SimpleFile(int width, int height) {
        this.width = width;
        this.height = height;
        this.partNumber = 0;
        this.folder = null;
        this.fileName = FilesHelper.getNewFileName(width, height);
    }

    public SimpleFile(int width, int height, int partNumber, Folder folder) {
        this.folder = folder;
        this.width = width;
        this.height = height;
        this.partNumber = partNumber;
        this.fileName = FilesHelper.getNewFileName(width, height, partNumber);
    }

    public SimpleFile(SimpleFile simpleFile, Folder folder) {
        this.width = simpleFile.getWidth();
        this.height = simpleFile.getHeight();
        this.partNumber = simpleFile.getPartNumber();
        this.folder = folder;
        this.fileName = simpleFile.getFileName();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public Folder getFolder() {
        return folder;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFileObj() {
        return fileObj;
    }

    public String getAbsolutePath() {
        if (folder == null) {
            return FilesHelper.getAbsolutePath(fileName);
        } else {
            return FilesHelper.getAbsolutePath(
                    folder.getAbsolutePath(), fileName);
        }
    }

    public SimpleFile createCopy(Folder folder) {
        SimpleFile copySimpleFile = new SimpleFile(this, folder);

        try {
            Files.copy(Path.of(getAbsolutePath()),
                    Path.of(copySimpleFile.getAbsolutePath()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return copySimpleFile;
    }
}
