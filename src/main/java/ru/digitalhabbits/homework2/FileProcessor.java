package ru.digitalhabbits.homework2;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static java.nio.charset.Charset.defaultCharset;
import static org.slf4j.LoggerFactory.getLogger;

public class FileProcessor {
    private static final Logger logger = getLogger(FileProcessor.class);
    public static final int CHUNK_SIZE = 2 * getRuntime().availableProcessors();
    final ExecutorService executorService = Executors.newCachedThreadPool();
    Exchanger<String> exchanger = new Exchanger<>();

    public void process(@Nonnull String processingFileName, @Nonnull String resultFileName) {
        checkFileExists(processingFileName);

        final File file = new File(processingFileName);
        List<String> tmpList = new ArrayList<>();
        List<Callable<Pair<String, Integer>>> callableList = new ArrayList<>();
        List<Future<Pair<String, Integer>>> futureList = new ArrayList<>();

        Thread fileWriterThread = new Thread(new FileWriter(exchanger, resultFileName));
        fileWriterThread.start();

        try (final Scanner scanner = new Scanner(file, defaultCharset())) {
            while (scanner.hasNext()) {
                tmpList.add(scanner.nextLine());
                //Согласно условию в коллекции можно хранить не больше CHUNK_SIZE обрабатываемых строк
                if (tmpList.size() == CHUNK_SIZE){
                    for (String str : tmpList){
                        Callable<Pair<String, Integer>> callable = () -> new LineCounterProcessor().process(str);
                        callableList.add(callable);
                    }

                    futureList = executorService.invokeAll(callableList);
                    tmpList.clear();
                    callableList.clear();

                    for (Future<Pair<String, Integer>> future : futureList){
                        Pair<String, Integer> pair = future.get();
                        String text = pair.getLeft();
                        String countSymbols = pair.getRight().toString();
                        String resultString = text + " " + countSymbols;
                        exchanger.exchange(resultString);
                    }

                    futureList.clear();
                }
            }
        } catch (IOException | InterruptedException | ExecutionException exception) {
            logger.error("", exception);
        }

        executorService.shutdown();
        fileWriterThread.interrupt();

        logger.info("Finish main thread {}", Thread.currentThread().getName());
    }

    private void checkFileExists(@Nonnull String fileName) {
        final File file = new File(fileName);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException("File '" + fileName + "' not exists");
        }
    }
}
