package ru.digitalhabbits.homework2;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Exchanger;

import static java.lang.Thread.currentThread;
import static org.slf4j.LoggerFactory.getLogger;

public class FileWriter
        implements Runnable {
    private static final Logger logger = getLogger(FileWriter.class);

    Exchanger<String> exchanger;
    File resultFile;

    public FileWriter(Exchanger<String> exchanger, String resultFileName) {
        this.exchanger = exchanger;
        this.resultFile = new File(resultFileName);
    }

    @Override
    public void run() {
        logger.info("Started writer thread {}", currentThread().getName());
        while (!Thread.currentThread().isInterrupted()){
            try {
                String exchangeString = exchanger.exchange(null) + System.lineSeparator();
                Files.writeString(resultFile.toPath(), exchangeString, StandardOpenOption.APPEND);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        logger.info("Finish writer thread {}", currentThread().getName());
    }
}
