package ru.lauk.chartographer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.lauk.chartographer.helpers.FilesHelper;

@SpringBootApplication
public class ChartographerApplication {

	public static void main(String[] args) {
		FilesHelper.setFilePath(args[0]);
		SpringApplication.run(ChartographerApplication.class, args);
	}

}
