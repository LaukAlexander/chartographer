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
public class ChartaCreationAsyncService {

    private final int MAX_PIXELS_FOR_SIMPLE_CHARTA = 25_000_000;

    @Async
    public Future<String> createImage(int width, int height) {
        if (width * height < MAX_PIXELS_FOR_SIMPLE_CHARTA) {
            return new AsyncResult<>(createSimpleImage(width, height));
        } else {
            return new AsyncResult<>(createCompositeImage(width, height));
        }
    }

    private String createSimpleImage(int width, int height) {
        SimpleFile simpleFile = new SimpleFile(width, height);

        if (FilesHelper.getFile(simpleFile.getFileName(), false) != null) {
            return createSimpleImage(width, height);
        }

        try {
            return saveImage(simpleFile);
        } catch (IOException e) {
            e.printStackTrace();
            FilesHelper.deleteFile(simpleFile.getAbsolutePath());
            return null;
        }
    }

    private String createCompositeImage(int width, int height) {
        Folder folder = new Folder(width, height);

        if (FilesHelper.getFile(folder.getFolderName(), true) != null) {
            return createCompositeImage(width, height);
        }

        try {
            List<SimpleFile> simpleFiles;
            Files.createDirectory(Path.of(folder.getAbsolutePath()));
            simpleFiles = folder.getSimpleFiles();
            for (SimpleFile simpleFile : simpleFiles) {
                saveImage(simpleFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            FilesHelper.deleteFile(folder.getAbsolutePath());
            return null;
        }

        return folder.getFolderName();
    }

    private String saveImage(SimpleFile simpleFile) throws IOException {
        String absolutePath = simpleFile.getAbsolutePath();
        File resultFile = Files.createFile(Path.of(absolutePath)).toFile();
        BufferedImage image =
                new BufferedImage(simpleFile.getWidth(),
                        simpleFile.getHeight(), BufferedImage.TYPE_INT_RGB);

        ImageIO.write(image, "bmp", resultFile);

        return resultFile.getName();
    }
}
