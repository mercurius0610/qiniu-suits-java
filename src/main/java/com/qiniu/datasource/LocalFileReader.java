package com.qiniu.datasource;

import com.qiniu.interfaces.IReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalFileReader implements IReader<BufferedReader> {

    private String name;
    private BufferedReader bufferedReader;
    private String startLine;
    private int limit;
    private String line;
    private List<String> lineList;
    private long count;

    public LocalFileReader(File sourceFile, String startLine, int limit) throws IOException {
        FileReader fileReader;
        try {
            fileReader = new FileReader(sourceFile);
        } catch (IOException e) {
            throw new IOException("file-path parameter may be incorrect, " + e.getMessage());
        }
        name = sourceFile.getPath();
        bufferedReader = new BufferedReader(fileReader);
        this.startLine = startLine == null ? "" : startLine;
        this.limit = limit;
        this.line = bufferedReader.readLine();
        this.lineList = new ArrayList<String>(){{ add(line); }};
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BufferedReader getRealReader() {
        return bufferedReader;
    }

    @Override
    public List<String> readLines() throws IOException {
        List<String> srcList = new ArrayList<>(lineList);
        lineList.clear();
        while (true) {
            if (srcList.size() >= limit) break;
            try {
                // 避免文件过大，行数过多，使用 lines() 的 stream 方式直接转换可能会导致内存泄漏，故使用 readLine() 的方式
                line = bufferedReader.readLine();
            } catch (IOException e) {
                lineList = srcList;
                throw e;
            }
            if (line == null) {
                break;
            } else if (line.compareTo(startLine) > 0) {
                srcList.add(line);
            }
        }
        count += srcList.size();
        return srcList;
    }

    @Override
    public String lastLine() {
        return line;
    }

    @Override
    public long count() {
        return count;
    }

    @Override
    public void close() {
        try {
            bufferedReader.close();
        } catch (IOException e) {
            bufferedReader = null;
        }
    }
}
