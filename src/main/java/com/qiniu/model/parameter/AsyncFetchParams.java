package com.qiniu.model.parameter;

import com.qiniu.service.interfaces.IEntryParam;

import java.io.IOException;

public class AsyncFetchParams extends QossParams {

    private String targetBucket;
    private String domain;
    private String https;
    private String needSign;
    private String keepKey;
    private String keyPrefix;
    private String urlIndex;
    private String md5Index;
    private String host;
    private String callbackUrl;
    private String callbackBody;
    private String callbackBodyType;
    private String callbackHost;
    private String fileType;
    private String ignoreSameKey;

    public AsyncFetchParams(IEntryParam entryParam) throws Exception {
        super(entryParam);
        this.targetBucket = entryParam.getParamValue("to-bucket");
        this.domain = entryParam.getParamValue("domain");
        try { this.https = entryParam.getParamValue("https"); } catch (Exception e) { https = ""; }
        try { this.needSign = entryParam.getParamValue("private"); } catch (Exception e) { needSign = ""; }
        try { this.keepKey = entryParam.getParamValue("keep-key"); } catch (Exception e) { keepKey = ""; }
        try { this.keyPrefix = entryParam.getParamValue("add-prefix"); } catch (Exception e) { keyPrefix = ""; }
        try { this.urlIndex = entryParam.getParamValue("url-index"); } catch (Exception e) { urlIndex = ""; }
        try { this.md5Index = entryParam.getParamValue("md5-index"); } catch (Exception e) { md5Index = ""; }
        try { this.host = entryParam.getParamValue("host"); } catch (Exception e) {}
        try { this.callbackUrl = entryParam.getParamValue("callback-url"); } catch (Exception e) {}
        try { this.callbackBody = entryParam.getParamValue("callback-body"); } catch (Exception e) {}
        try { this.callbackBodyType = entryParam.getParamValue("callback-body-type"); } catch (Exception e) {}
        try { this.callbackHost = entryParam.getParamValue("callback-host"); } catch (Exception e) {}
        try { this.fileType = entryParam.getParamValue("file-type"); } catch (Exception e) { fileType = ""; }
        try { this.ignoreSameKey = entryParam.getParamValue("ignore-same-key"); } catch (Exception e) { ignoreSameKey = ""; }
    }

    public String getTargetBucket() {
        return targetBucket;
    }

    public String getDomain() {
        return domain;
    }

    public String getProtocol() throws IOException {
        if ("".equals(https) || https.matches("false")) {
            return "http";
        } else if (https.matches("true")) {
            return "https";
        } else {
            throw new IOException("please set https as true/false.");
        }
    }

    public boolean getNeedSign() {
        if (needSign.matches("(true|false)")) {
            return Boolean.valueOf(needSign);
        } else {
            return false;
        }
    }

    public boolean getKeepKey() {
        if (keepKey.matches("(true|false)")) {
            return Boolean.valueOf(keepKey);
        } else {
            return true;
        }
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public String getUrlIndex() throws IOException {
        if ("json".equals(getParseType())) {
            if ("".equals(urlIndex)) {
                throw new IOException("no incorrect json key index for avinfo's url.");
            } else {
                return urlIndex;
            }
        } else if ("table".equals(getParseType())) {
            if ("".equals(urlIndex)) {
                return "0";
            } else if (urlIndex.matches("\\d")) {
                return urlIndex;
            } else {
                throw new IOException("no incorrect url index, it should be a number.");
            }
        } else {
            throw new IOException("no incorrect object key index for avinfo's url.");
        }
    }

    public String getMd5Index() throws IOException {
        if ("json".equals(getParseType())) {
            if ("".equals(md5Index)) {
                throw new IOException("no incorrect json key index for asyncfetch's md5.");
            } else {
                return md5Index;
            }
        } else if ("table".equals(getParseType())) {
            if ("".equals(md5Index)) {
                return "8";
            } else if (md5Index.matches("\\d")) {
                return md5Index;
            } else {
                throw new IOException("no incorrect md5 index, it should be a number.");
            }
        } else {
            throw new IOException("no incorrect object key index for asyncfetch's md5.");
        }
    }

    public String getHost() {
        return host;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getCallbackBody() {
        return callbackBody;
    }

    public String getCallbackBodyType() {
        return callbackBodyType;
    }

    public String getCallbackHost() {
        return callbackHost;
    }

    public int getFileType() throws IOException {
        if (fileType.matches("([01])")) {
            return Short.valueOf(fileType);
        } else {
            throw new IOException("no incorrect file-type, please set it 0 or 1.");
        }
    }

    public boolean getIgnoreSameKey() {
        if (ignoreSameKey.matches("(true|false)")) {
            return Boolean.valueOf(ignoreSameKey);
        } else {
            return false;
        }
    }

    public boolean hasCustomArgs() {
        return (host != null || callbackUrl != null || callbackBody != null || callbackBodyType != null
                || callbackHost != null || "1".equals(fileType) || "true".equals(ignoreSameKey));
    }
}
