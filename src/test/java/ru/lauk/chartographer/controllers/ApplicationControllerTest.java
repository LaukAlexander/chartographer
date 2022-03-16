package ru.lauk.chartographer.controllers;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.lauk.chartographer.helpers.FilesHelper;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApplicationControllerTest extends Assertions {

    @Autowired
    private ApplicationController controller;

    private File testFragment;
    private final String simpleChartaName = "Charta_1_5000_4999.bmp";
    private String compositeChartaName = "Charta_2_20000_50000";
    private InputStreamResource testFragmentStream;


    @BeforeEach
    void setUp() {
        String filePath;
        String absolutePath;
        String testFragmentName = "TestFragment.bmp";
        URL url = this.getClass().getResource("/" + testFragmentName);
        InputStream inputStream = this.getClass().getResourceAsStream("/" + testFragmentName);

        assert inputStream != null;
        testFragmentStream = new InputStreamResource(inputStream);
        assert url != null;
        testFragment = new File(url.getFile());

        absolutePath = testFragment.getAbsolutePath();
        filePath = absolutePath
                .substring(0, (absolutePath.indexOf(testFragmentName)));
        FilesHelper.setFilePath(filePath);
    }

    @Test
    @Order(1)
    void checkingCreationErrors() {
        ResponseEntity response;
        HttpStatus badRequestStatus = HttpStatus.BAD_REQUEST;

        response = controller.createNewCharta(0, 0);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.createNewCharta(0, 500);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.createNewCharta(500, 0);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.createNewCharta(-1, -1);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.createNewCharta(-1, 500);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.createNewCharta(500, -1);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.createNewCharta(20001, 50001);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.createNewCharta(20001, 500);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.createNewCharta(500, 50001);
        assertSame(response.getStatusCode(), badRequestStatus);
    }

    @Test
    @Order(2)
    void checkingRestorationErrors() {
        ResponseEntity response;
        HttpStatus badRequestStatus = HttpStatus.BAD_REQUEST;
        String fakeName = "Fake_Charta_20001_50001.bmp";

        response = controller.saveFragment(fakeName, -1, -1, 500, 500, null);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.saveFragment(fakeName, -1, 0, 500, 500, null);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.saveFragment(fakeName, 0, -1, 500, 500, null);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.saveFragment(fakeName, 0, 0, -1, -1, null);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.saveFragment(fakeName, 0, 0, -1, 500, null);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.saveFragment(fakeName, 0, 0, 500, -1, null);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.saveFragment(fakeName, 0, 0, 0, 0, null);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.saveFragment(fakeName, 0, 0, 0, 500, null);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.saveFragment(fakeName, 0, 0, 500, 0, null);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.saveFragment(fakeName, 0, 0, 5001, 5001, null);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.saveFragment(fakeName, 0, 0, 5001, 500, null);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.saveFragment(fakeName, 0, 0, 500, 5001, null);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.saveFragment(fakeName, 0, 0, 500, 500, null);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.saveFragment(
                fakeName, 0, 0, 500, 500, testFragmentStream);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.saveFragment(
                fakeName, 0, 0, 5000, 5000, testFragmentStream);
        assertSame(response.getStatusCode(), badRequestStatus);
    }

    @Test
    @Order(3)
    void checkingGettingErrors() {
        ResponseEntity response;
        HttpStatus badRequestStatus = HttpStatus.BAD_REQUEST;
        String fakeName = "Fake_Charta_20001_50001.bmp";

        response = controller.getFragment(fakeName, -1, -1, 500, 500);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.getFragment(fakeName, 0, -1, 500, 500);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.getFragment(fakeName, -1, 0, 500, 500);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.getFragment(fakeName, 0, 0, -1, -1);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.getFragment(fakeName, 0, 0, -1, 500);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.getFragment(fakeName, 0, 0, 500, -1);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.getFragment(fakeName, 0, 0, 0, 0);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.getFragment(fakeName, 0, 0, 500, 0);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.getFragment(fakeName, 0, 0, 0, 500);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.getFragment(fakeName, 0, 0, 5001, 5001);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.getFragment(fakeName, 0, 0, 500, 5001);
        assertSame(response.getStatusCode(), badRequestStatus);
        response = controller.getFragment(fakeName, 0, 0, 5001, 500);
        assertSame(response.getStatusCode(), badRequestStatus);

        response = controller.getFragment(fakeName, 0, 0, 500, 500);
        assertSame(response.getStatusCode(), badRequestStatus);
    }

    @Test
    @Order(4)
    void checkingDeletionErrors() {
        ResponseEntity response;
        HttpStatus badRequestStatus = HttpStatus.BAD_REQUEST;
        String fakeName = "Fake_Charta_20001_50001.bmp";

        response = controller.deleteFragment(fakeName);
        assertSame(response.getStatusCode(), badRequestStatus);
    }

    @Test
    @Order(5)
    void workingWithSimpleCharta() {
        ResponseEntity response;
        String expectedName = controller.createNewCharta(5000, 4999).getBody();

        assertEquals(simpleChartaName, expectedName);

        response = controller.saveFragment(
                simpleChartaName, 1000, 1000, 5000, 5000, testFragmentStream);
        assertEquals(HttpStatus.OK, response.getStatusCode());

         response = controller.getFragment(
                 simpleChartaName, 1000, 1000, 5000, 5000);
        assertEquals(HttpStatus.OK, response.getStatusCode());

         response = controller.deleteFragment(simpleChartaName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(6)
    void workingWithCompositeCharta() {
        ResponseEntity response;
        String expectedName = controller.createNewCharta(20000, 50000).getBody();

        assertEquals(compositeChartaName, expectedName);

        response = controller.saveFragment(
                compositeChartaName, 1000, 1000, 5000, 5000, testFragmentStream);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        response = controller.getFragment(
                compositeChartaName, 500, 500, 5000, 5000);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        response = controller.deleteFragment(compositeChartaName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}