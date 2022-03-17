package ru.lauk.chartographer.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.lauk.chartographer.entity.Folder;
import ru.lauk.chartographer.entity.SimpleFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FilesHelper {
    private static final String FILE_NAME = "Charta_";
    private static final String FILE_FORMAT = ".bmp";
    private static final String PART = "Part_";
    private static long         id = 0;
    private static String       filePath;

    public static void setFilePath(String filePath) {
        FilesHelper.filePath = filePath;
    }

    public synchronized static String getNewFileName(int width, int height) {
        return FILE_NAME + ++id + "_" + width + "_" + height;
    }

    public static String getNewFileName(int width,
                                        int height,
                                        int partNumber) {
        return PART + String.format("%02d", partNumber) +
                "_" + width + "_" + height;
    }

    public static String getAbsolutePath(String fileName) {
        return filePath + "/" + fileName + FILE_FORMAT;
    }

    public static String getAbsolutePath(String folderPath, String fileName) {
        return folderPath + "/" + fileName + FILE_FORMAT;
    }

    public static String getFolderPath(String folderName) {
        return filePath + "/" + folderName;
    }

    public static File getFile(String fileName) {
        String absolutePath = getFolderPath(fileName);

        if (!Files.exists(Path.of(absolutePath))) {
            return null;
        }
        return new File(absolutePath);
    }

    public static File getFile(String fileName, boolean isFolder) {
        String absolutePath;

        if (isFolder) {
            absolutePath = getFolderPath(fileName);
        } else {
            absolutePath = getAbsolutePath(fileName);
        }

        if (!Files.exists(Path.of(absolutePath))) {
            return null;
        }

        return new File(absolutePath);
    }

    public static void deleteFile(Folder folder) {
        FilesHelper.deleteFile(new File(folder.getAbsolutePath()));
    }

    public static boolean deleteFile(File file) {
        if (file == null) {
            return false;
        }

        if (file.isDirectory()) {
            List<File> fileList =
                    FilesHelper.findAllFiles(file.getAbsolutePath());
            if (fileList == null) {
                return false;
            }
            if (!fileList.isEmpty()) {
                for (File simpleFile : fileList) {
                    if (!FilesHelper.deleteFile(
                            simpleFile.getAbsolutePath())) {
                        return false;
                    }
                }
            }
        }

        return FilesHelper.deleteFile(file.getAbsolutePath());
    }

    public static boolean deleteFile(String absolutePath) {
        if (absolutePath == null) {
            return false;
        }

        try {
            Files.delete(Path.of(absolutePath));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<File> findAllFiles(String folderPath) {
        try (Stream<Path> streamPath = Files.list(Path.of(folderPath))) {
            return streamPath.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void restoreFolderAfterIOException(Folder folder,
                                                     Folder copyFolder) {
        List<SimpleFile> copiesSimpleFile = copyFolder.getSimpleFiles();
        List<SimpleFile> simpleFiles = folder.getSimpleFiles();

        for (SimpleFile copy : copiesSimpleFile) {
            String fileName = copy.getFileName();
            SimpleFile originalFile = simpleFiles.stream()
                    .filter(file -> file.getFileName() == fileName)
                    .findAny().orElseGet(null);

            try {
                Path originalFilePath =
                        Path.of(originalFile.getAbsolutePath());
                Files.deleteIfExists(originalFilePath);
                Files.copy(Path.of(copy.getAbsolutePath()), originalFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String withoutFormat(String fileName) {
        return fileName.substring(0, fileName.indexOf(FILE_FORMAT));
    }
}
