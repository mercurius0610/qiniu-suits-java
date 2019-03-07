package com.qiniu.model.parameter;

import com.qiniu.config.CommandArgs;
import com.qiniu.config.FileProperties;
import com.qiniu.service.interfaces.IEntryParam;

import java.io.IOException;
import java.util.*;

public class CommonParams {

    protected IEntryParam entryParam;
    private String path;
    private String source;
    private String parse;
    private String separator;
    private Map<String, String> indexMap;
    private int unitLen;
    private int threads;
    private int retryCount;
    private boolean saveTotal;
    private String savePath;
    private String saveFormat;
    private String saveSeparator;
    private List<String> rmFields;
    private String process;
    private List<String> needUrlIndex = new ArrayList<String>(){{
        add("asyncfetch");
        add("privateurl");
        add("qhash");
        add("avinfo");
    }};
    private List<String> needMd5Index = new ArrayList<String>(){{
        add("asyncfetch");
    }};
    private List<String> needNewKeyIndex = new ArrayList<String>(){{
        add("rename");
        add("copy");
    }};
    private List<String> needFopsIndex = new ArrayList<String>(){{
        add("pfop");
    }};
    private List<String> needPidIndex = new ArrayList<String>(){{
        add("pfopresult");
    }};
    private List<String> needAvinfoIndex = new ArrayList<String>(){{
        add("pfopcmd");
    }};

    public CommonParams(IEntryParam entryParam) throws IOException {
        this.entryParam = entryParam;
        path = entryParam.getValue("path", null);
        setSource(path);
        parse = checked(entryParam.getValue("parse", "table"), "parse", "(csv|table|json)");
        separator = entryParam.getValue("separator", "\t");
        setUnitLen(entryParam.getValue("unit-len", "10000"));
        setThreads(entryParam.getValue("threads", "30"));
        setRetryCount(entryParam.getValue("retry-times", "3"));
        // list 操作时默认保存全部原始文件
        setSaveTotal(entryParam.getValue("save-total", String.valueOf("list".equals(getSource()))));
        savePath = entryParam.getValue("save-path", "result");
        saveFormat = entryParam.getValue("save-format", "table");
        // 校验设置的 format 参数
        saveFormat = checked(saveFormat, "save-format", "(csv|table|json)");
        saveSeparator = entryParam.getValue("save-separator", "\t");
        rmFields = Arrays.asList(entryParam.getValue("rm-fields", "").split(","));
        process = entryParam.getValue("process", null);
    }

    public CommonParams(String[] args) throws IOException {
        this(new CommandArgs(args));
    }

    public CommonParams(String configFileName) throws IOException {
        this(new FileProperties(configFileName));
    }

    public String getParamByKey(String key) throws IOException {
        return entryParam.getValue(key);
    }

    private void setSource(String path) throws IOException {
        try {
            source = entryParam.getValue("source-type");
        } catch (IOException e1) {
            try {
                source = entryParam.getValue("source");
            } catch (IOException e2) {
                if (path == null || path.startsWith("qiniu://")) source = "list";
                else source = "file";
            }
        }
        if (source.matches("(list|file)")) {
            throw new IOException("please set the \"source\" conform to regex:" +
                    " (list|file)");
        }
    }

    private void setUnitLen(String unitLen) throws IOException {
        this.unitLen = Integer.valueOf(checked(unitLen, "unit-len", "\\d+"));
    }

    private void setThreads(String threads) throws IOException {
        this.threads = Integer.valueOf(checked(threads, "threads", "[1-9]\\d*"));
    }

    private void setRetryCount(String retryCount) throws IOException {
        this.retryCount = Integer.valueOf(checked(retryCount, "retry-times", "\\d+"));
    }

    private void setSaveTotal(String saveTotal) throws IOException {
        this.saveTotal = Boolean.valueOf(checked(saveTotal, "save-total", "(true|false)"));
    }

    public String checked(String param, String name, String conditionReg) throws IOException {
        if (param == null || !param.matches(conditionReg))
            throw new IOException("no correct \"" + name + "\", please set the it conform to regex: " + conditionReg);
        else return param;
    }

    private void setIndex(String index, String indexName, List<String> needList) throws IOException {
        if (index != null && needList.contains(getProcess())) {
            if (indexMap.containsValue(index)) {
                throw new IOException("the value: " + index + "is already in map: " + indexMap);
            }
            if ("json".equals(getParse())) {
                indexMap.put(indexName, index);
            } else if ("table".equals(getParse())) {
                if (index.matches("\\d")) {
                    indexMap.put(indexName, index);
                } else {
                    throw new IOException("no incorrect " + indexName + "-index, it should be a number.");
                }
            } else {
                // 其他情况暂且忽略该索引
            }
        }
    }

    public void setIndexMap() throws IOException {
        indexMap = new HashMap<>();
        String indexes = entryParam.getValue("indexes", "");
        List<String> keys = Arrays.asList("key", "hash", "fsize", "putTime", "mimeType", "type", "status", "endUser");
        if ("table".equals(getParse())) {
            if ("".equals(indexes)) {
                indexMap.put("0", keys.get(0));
            } else if (indexes.matches("(\\d+,)*\\d")) {
                List<String> indexList = Arrays.asList(indexes.split(","));
                if (indexList.size() > 8) {
                    throw new IOException("the file info's index length is too long.");
                } else {
                    for (int i = 0; i < indexList.size(); i++) {
                        if (indexList.get(i).matches("\\d+") && Integer.valueOf(indexList.get(i)) > -1)
                            indexMap.put(indexList.get(i), keys.get(i));
                    }
                }
            } else {
                throw new IOException("the index pattern is not supported.");
            }
        } else {
            List<String> indexList = Arrays.asList(indexes.split(","));
            if (indexList.size() == 0) {
                indexMap.put("key", keys.get(0));
            } else if (indexList.size() > 8) {
                throw new IOException("the file info's index length is too long.");
            } else {
                for (int i = 0; i < indexList.size(); i++) { indexMap.put(indexList.get(i), keys.get(i)); }
            }
        }

        setIndex(entryParam.getValue("url-index", null), "url", needUrlIndex);
        setIndex(entryParam.getValue("md5-index", null), "md5", needMd5Index);
        setIndex(entryParam.getValue("newKey-index", null), "newKey", needNewKeyIndex);
        setIndex(entryParam.getValue("fops-index", null), "fops", needFopsIndex);
        setIndex(entryParam.getValue("persistentId-index", null), "persistentId", needPidIndex);
        setIndex(entryParam.getValue("avinfo-index", null), "avinfo", needAvinfoIndex);
    }

    public String getPath() {
        return path;
    }

    public String getSource() {
        return source;
    }

    public String getParse() {
        return parse;
    }

    public String getSeparator() {
        return separator;
    }

    public Map<String, String> getIndexMap() {
        return indexMap;
    }

    public int getUnitLen() {
        return unitLen;
    }

    public int getThreads() {
        return threads;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Boolean getSaveTotal() {
        return saveTotal;
    }

    public String getSavePath() {
        return savePath;
    }

    public String getSaveFormat() {
        return saveFormat;
    }

    public String getSaveSeparator() {
        return saveSeparator;
    }

    public List<String> getRmFields() {
        return rmFields;
    }

    public String getProcess() {
        return process;
    }
}
