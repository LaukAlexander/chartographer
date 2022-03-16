package ru.lauk.chartographer.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import ru.lauk.chartographer.entity.Folder;
import ru.lauk.chartographer.entity.SimpleFile;
import ru.lauk.chartographer.helpers.FilesHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class ChartaRestorationAsyncService {

    @Async
    public Future<Boolean> restoreFragment(File file, int x, int y, int height,
                            BufferedImage fragment) {
        synchronized (file.getName().intern()) {
            if (Files.isDirectory(Path.of(file.getAbsolutePath()))) {
                return new AsyncResult<>(restoreFragment(
                        new Folder(file), x, y, height, fragment));
            } else {
                return new AsyncResult<>(restoreFragment(
                        new SimpleFile(file, null), x, y, fragment));
            }
        }
    }

    private boolean restoreFragment(SimpleFile simpleFile, int x, int y,
                                    BufferedImage fragment) {
        boolean result = true;
        File workFile = simpleFile.getFileObj();

        try {
            BufferedImage localImage = ImageIO.read(workFile);
            int fragmentWidth = fragment.getWidth();
            int fragmentHeight = fragment.getHeight();
            int localImageWidth = localImage.getWidth();
            int localImageHeight = localImage.getHeight();

            if (x > localImageWidth || y > localImageHeight) {
                return true;
            }

            for (int i = 0; i < fragmentWidth; i++) {
                if (i + x == localImageWidth) {
                    break;
                }
                for (int j = 0; j < fragmentHeight ; j++) {
                    if (j + y == localImageHeight) {
                        break;
                    }

                    localImage.setRGB(i + x, j + y, fragment.getRGB(i, j));
                }
            }

            ImageIO.write(localImage, "bmp", workFile);
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    private boolean restoreFragment(Folder folder, int x, int y,
                                    int height, BufferedImage fragment) {
        List<SimpleFile> simpleFiles = folder.getSimpleFiles();
        Folder copyFolder;

        try {
            copyFolder = new Folder(folder);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        boolean result = true;
        int deltaY = y;
        int deltaHeight = height;
        int plusHeight = 0;

        if (simpleFiles.isEmpty()) {
            return false;
        }

        for (SimpleFile file : simpleFiles) {
            int fileHeight = file.getHeight();
            int subHeight;

            if (fileHeight <= deltaY) {
                deltaY -= fileHeight;
                continue;
            }

            SimpleFile copySimpleFile = file.createCopy(copyFolder);
            if (copySimpleFile != null) {
                copyFolder.addSimpleFile(copySimpleFile);
            }

            subHeight = Math.min(fileHeight, deltaHeight);
            result = restoreFragment(
                    file, x, deltaY, plusHeight, subHeight, fragment);
            deltaHeight -= fileHeight - deltaY;
            plusHeight += fileHeight - deltaY;
            deltaY = 0;

            if (!result) {
                FilesHelper.restoreFolderAfterIOException(folder, copyFolder);
                break;
            }

            if (deltaHeight <= 0) {
                break;
            }
        }
        FilesHelper.deleteFile(copyFolder);

        return result;
    }

    private boolean restoreFragment(SimpleFile simpleFile,
                                    int x, int y,
                                    int plusH, int height,
                                    BufferedImage fragment) {
        boolean result = true;
        File workFile = simpleFile.getFileObj();

        try {
            BufferedImage localImage = ImageIO.read(workFile);
            int fragmentWidth = fragment.getWidth();
            int fragmentHeight = fragment.getHeight();
            int localImageWidth = localImage.getWidth();
            int localImageHeight = localImage.getHeight();

            if (x > localImageWidth || y > localImageHeight) {
                return true;
            }

            for (int i = 0; i < fragmentWidth; i++) {
                if (i + x == localImageWidth) {
                    break;
                }
                for (int j = 0; j < height ; j++) {
                    if (j + y == localImageHeight ||
                            j + plusH > fragmentHeight) {
                        break;
                    }

                    localImage.setRGB(i + x, j + y,
                            fragment.getRGB(i, j + plusH));
                }
            }

            ImageIO.write(localImage, "bmp", workFile);
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }
}
