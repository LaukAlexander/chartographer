package ru.lauk.chartographer.controllers;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.lauk.chartographer.helpers.FilesHelper;
import ru.lauk.chartographer.services.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/chartas")
public class ApplicationController {

    @Autowired
    private ChartaCreationAsyncService creationService;
    @Autowired
    private ChartaRestorationAsyncService restorationService;
    @Autowired
    private ChartaGettingFragmentAsyncService gettingFragmentService;
    @Autowired
    private ChartaDeletionAsyncService deletionService;

    @PostMapping("")
    public ResponseEntity<String> createNewCharta(
            @RequestParam("width") int width,
            @RequestParam("height") int height) {
        String fileName;

        if (width < 1 || width > 20_000 || height < 1 || height > 50_000) {
            return new ResponseEntity(
                    returningBadRequestMessage(20_000, 50_000),
                    HttpStatus.BAD_REQUEST);
        }
        try {
            fileName = creationService.createImage(width, height).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            fileName = null;
        }
        if (fileName == null) {
            return new ResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            return new ResponseEntity(fileName, HttpStatus.CREATED);
        }
    }

    @PostMapping(value = "/{id}")
    public ResponseEntity<String> saveFragment(
            @PathVariable("id") String id,
            @RequestParam("x") int x,
            @RequestParam("y") int y,
            @RequestParam("width") int width,
            @RequestParam("height") int height,
            @RequestBody InputStreamResource stream) {
        BufferedImage fragment;
        File file;
        Boolean result;
        InputStream fragmentInputStream = null;

        if (x < 0 || y < 0) {
            return new ResponseEntity(
                    returningBadRequestMessage(),
                    HttpStatus.BAD_REQUEST);
        }

        if (width < 1 || width > 5_000 || height < 1 || height > 5_000) {
            return new ResponseEntity(
                    returningBadRequestMessage(5_000, 5_000),
                    HttpStatus.BAD_REQUEST);
        }

        if (stream == null) {
            return new ResponseEntity(
                    "Не передан фрагмент для вставки",
                    HttpStatus.BAD_REQUEST);
        }
        try {
            file = FilesHelper.getFile(id);
            if (file == null) {
                return new ResponseEntity(
                        "Файла с id: " + id + ", не существует",
                        HttpStatus.BAD_REQUEST);
            }

            fragmentInputStream = stream.getInputStream();
            fragment = ImageIO.read(fragmentInputStream);
            if (fragment.getType() != BufferedImage.TYPE_INT_RGB &&
                    fragment.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                return new ResponseEntity(
                        "", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            }
            if (fragment.getWidth() != width ||
                    fragment.getHeight() != height) {
                return new ResponseEntity<>(
                        "Неверно переданы значения width и height",
                        HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity("", HttpStatus.UNPROCESSABLE_ENTITY);
        } finally {
            if (fragmentInputStream != null) {
                closeStream(fragmentInputStream);
            }
        }

        try {
            result = restorationService
                    .restoreFragment(file, x, y, height, fragment).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            result = false;
        }

        if (result) {
            return ResponseEntity.ok("");
        } else {
            return new ResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/{id}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity getFragment(@PathVariable("id") String id,
                                      @RequestParam("x") int x,
                                      @RequestParam("y") int y,
                                      @RequestParam("width") int width,
                                      @RequestParam("height") int height) {
        File file;
        BufferedImage image;

        if (x < 0 || y < 0) {
            return new ResponseEntity(
                    returningBadRequestMessage(),
                    HttpStatus.BAD_REQUEST);
        }

        if (width < 1 ||width > 5_000 || height < 1 || height > 5_000) {
            return new ResponseEntity(
                    returningBadRequestMessage(5_000, 5_000),
                    HttpStatus.BAD_REQUEST);
        }

        file = FilesHelper.getFile(id);
        if (file == null) {
            return new ResponseEntity(
                    "Файла с id: " + id + ", не существует",
                    HttpStatus.BAD_REQUEST);
        }
         try {
             image = gettingFragmentService
                     .getFragment(file, x, y, width, height).get();
         } catch (ExecutionException | InterruptedException e) {
             e.printStackTrace();
             return null;
         }
        if (image == null) {
            return new ResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "bmp", out);
                return ResponseEntity.ok(out.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity(
                        "", HttpStatus.INTERNAL_SERVER_ERROR);
            } finally {
                closeStream(out);
            }
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFragment(
            @PathVariable("id") String id) {
        File file = FilesHelper.getFile(id);
        Boolean result;

        if (file == null) {
            return new ResponseEntity(
                    "Файла с id: " + id + ", не существует",
                    HttpStatus.BAD_REQUEST);
        }
         try {
             result = deletionService.deleteCharta(file).get();
         } catch (ExecutionException | InterruptedException e) {
             e.printStackTrace();
             result = false;
         }
        if (result) {
            return ResponseEntity.ok("");
        } else {
            return new ResponseEntity("", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void closeStream(Closeable stream) {
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String returningBadRequestMessage() {
        return "Недопустимые значения \"x\" и/или \"y\".\n" +
                "\"x\" и \"y\" должны быть положительные числа.";
    }

    private String returningBadRequestMessage(int width, int height) {
        return "Недопустимые значения \"width\" и/или \"height\".\n" +
                "\"width\" должно быть от 1 до " + width + ".\n" +
                "\"height\" должно быть от 1 до " + height + ".";
    }
}
