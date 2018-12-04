package com.qiniu.service.datasource;

import com.qiniu.common.FileMap;
import com.qiniu.service.convert.FileLineToMap;
import com.qiniu.service.interfaces.ILineProcess;
import com.qiniu.service.interfaces.ITypeConvert;
import com.qiniu.util.ExecutorsUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Collectors;

public class FileInput {

    private String parserTye;
    private String separator;
    private int unitLen;

    public FileInput(String separator, int unitLen) {
        this.separator = separator;
        this.unitLen = unitLen;
    }

    public void traverseByReader(int finalI, BufferedReader bufferedReader, ILineProcess<Map<String, String>> processor) {

        ILineProcess<Map<String, String>> fileProcessor = null;
        ITypeConvert<String, Map<String, String>> typeConverter = new FileLineToMap(parserTye, separator);
        try {
            if (processor != null) fileProcessor = processor.getNewInstance(finalI + 1);
            List<String> lineList = bufferedReader.lines().parallel().collect(Collectors.toList());
            int size = lineList.size()/unitLen + 1;
            for (int j = 0; j < size; j++) {
                List<String> processList = lineList.subList(unitLen * j,
                        j == size - 1 ? lineList.size() : unitLen * (j + 1));
                if (fileProcessor != null) fileProcessor.processLine(typeConverter.convertToVList(processList));
            }
            bufferedReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (fileProcessor != null) fileProcessor.closeResource();
        }
    }

    public void process(int maxThreads, String filePath, ILineProcess<Map<String, String>> processor) {
        List<String> sourceKeys = new ArrayList<>();
        FileMap fileMap = new FileMap();
        File sourceFile = new File(filePath);
        try {
            if (sourceFile.isDirectory()) {
                File[] fs = sourceFile.listFiles();
                assert fs != null;
                for(File f : fs) {
                    if (!f.isDirectory()) {
                        sourceKeys.add(f.getName());
                        fileMap.initReader(sourceFile.getPath(), f.getName());
                    }
                }
            } else {
                sourceKeys.add(sourceFile.getName());
                fileMap.initReader(sourceFile.getParent(), sourceFile.getName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int listSize = sourceKeys.size();
        int runningThreads = listSize < maxThreads ? listSize : maxThreads;
        String info = "read files" + (processor == null ? "" : " and " + processor.getProcessName());
        System.out.println(info + " concurrently running with " + runningThreads + " threads ...");
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setUncaughtExceptionHandler((t, e) -> System.out.println(t.getName() + "\t" + e.getMessage()));
            return thread;
        };
        ExecutorService executorPool = Executors.newFixedThreadPool(runningThreads, threadFactory);
        List<BufferedReader> sourceReaders = sourceKeys.parallelStream()
                .map(fileMap::getReader)
                .collect(Collectors.toList());
        for (int i = 0; i < sourceReaders.size(); i++) {
            int finalI = i;
            executorPool.execute(() -> traverseByReader(finalI, sourceReaders.get(finalI), processor));
        }
        executorPool.shutdown();
        ExecutorsUtils.waitForShutdown(executorPool, info);
        fileMap.closeWriter();
    }
}
