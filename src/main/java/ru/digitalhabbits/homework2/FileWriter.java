package ru.digitalhabbits.homework2;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Exchanger;

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

public class FileWriter
        implements Runnable {
    private static final Logger logger = getLogger(FileWriter.class);

    Exchanger<String> exchanger;
    Path resultFilePath;

    public FileWriter(Exchanger<String> exchanger, String resultFileName) {
        this.exchanger = exchanger;
        this.resultFilePath = createResultFilePath(resultFileName);
    }

    private Path createResultFilePath(String resultFileName){
        Path path = Path.of(resultFileName);
        if (Files.notExists(path)){
            try {
                return Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return path;
    }

    @Override
    public void run() {
        logger.info("Started writer thread {}", currentThread().getName());
        while (!Thread.currentThread().isInterrupted()){
            try {
                String exchangeString = exchanger.exchange(null) + System.lineSeparator();
                Files.writeString(resultFilePath, exchangeString, StandardOpenOption.APPEND);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
                currentThread().interrupt();
            }
        }

        logger.info("Finish writer thread {}", currentThread().getName());
    }
}
