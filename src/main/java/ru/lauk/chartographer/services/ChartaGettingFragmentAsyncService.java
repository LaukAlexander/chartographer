package ru.lauk.chartographer.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import ru.lauk.chartographer.entity.Folder;
import ru.lauk.chartographer.entity.SimpleFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class ChartaGettingFragmentAsyncService {

    @Async
    public Future<BufferedImage> getFragment(File file, int x, int y,
                                            int width, int height) {
        if (Files.isDirectory(Path.of(file.getAbsolutePath()))) {
            return new AsyncResult<>(getFragment(
                    new Folder(file), x, y, width, height));
        } else {
            return new AsyncResult<>(getFragment(
                    new SimpleFile(file, null), x, y, width, height));
        }
    }

    private BufferedImage getFragment(Folder folder, int x, int y,
                                      int width, int height) {
        List<SimpleFile> simpleFiles = folder.getSimpleFiles();
        BufferedImage resultImage;
        int deltaY = y;
        int deltaHeight = height;
        int subY = 0;

        if (simpleFiles.isEmpty()) {
            return null;
        }

        resultImage =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (SimpleFile file : simpleFiles) {
            int fileHeight = file.getHeight();
            int subHeight;

            if (fileHeight <= deltaY) {
                deltaY -= fileHeight;
                continue;
            }

            subHeight = Math.min(fileHeight, deltaHeight);
            BufferedImage subFragment =
                    getFragment(file, x, deltaY, width, subHeight);

            if (subFragment == null) {
                return null;
            }

            fragmentJoin(resultImage, subY, subFragment);
            subY += fileHeight;
            deltaHeight -= fileHeight - deltaY;
            deltaY = 0;

            if (deltaHeight <= 0) {
                break;
            }
        }

        return resultImage;
    }

    private BufferedImage getFragment(SimpleFile file, int x, int y,
                                      int width, int height) {
        BufferedImage resultImage;

        try {
            BufferedImage localImage = ImageIO.read(file.getFileObj());

            resultImage = getFragment(localImage, x, y, width, height);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return resultImage;
    }

    private BufferedImage getFragment(BufferedImage image, int x, int y,
                                      int width, int height) {
        BufferedImage resultImage;

        int imageWidth;
        int imageHeight;

        resultImage =
                new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        imageWidth = image.getWidth();
        imageHeight = image.getHeight();

        if (x > imageWidth || y > imageHeight) {
            return resultImage;
        }
        for (int i = 0; i < width; i++) {
            if (i + x == imageWidth) {
                break;
            }
            for (int j = 0; j < height; j++) {
                if (j + y == imageHeight) {
                    break;
                }

                resultImage.setRGB(i, j, image.getRGB(i + x, j + y));
            }
        }

        return resultImage;
    }

    private BufferedImage fragmentJoin(BufferedImage restoreImage, int y,
                                       BufferedImage fragment) {
        int fragmentWidth = fragment.getWidth();
        int fragmentHeight = fragment.getHeight();
        int restoreImageWidth = restoreImage.getWidth();
        int restoreImageHeight = restoreImage.getHeight();

        if (y >= restoreImageHeight) {
            return restoreImage;
        }

        for (int i = 0; i < fragmentWidth; i++) {
            if (i == restoreImageWidth) {
                break;
            }
            for (int j = 0; j < fragmentHeight; j++) {
                if (j + y == restoreImageHeight) {
                    break;
                }

                restoreImage.setRGB(i, j + y, fragment.getRGB(i, j));
            }
        }

        return restoreImage;
    }
}
