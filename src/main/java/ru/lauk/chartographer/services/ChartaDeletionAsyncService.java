package ru.lauk.chartographer.services;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import ru.lauk.chartographer.helpers.FilesHelper;

import java.io.File;
import java.util.concurrent.Future;

@Service
public class ChartaDeletionAsyncService {

    @Async
    public Future<Boolean> deleteCharta(File file) {
        return new AsyncResult<>(FilesHelper.deleteFile(file));
    }
}
