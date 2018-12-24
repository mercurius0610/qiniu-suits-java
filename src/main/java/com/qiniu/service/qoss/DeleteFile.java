package com.qiniu.service.qoss;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager.BatchOperations;
import com.qiniu.service.interfaces.ILineProcess;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.HttpResponseUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeleteFile extends OperationBase implements ILineProcess<Map<String, String>>, Cloneable {

    public DeleteFile(Auth auth, Configuration configuration, String bucket, String resultPath,
                      int resultIndex) throws IOException {
        super(auth, configuration, bucket, "delete", resultPath, resultIndex);
    }

    public DeleteFile(Auth auth, Configuration configuration, String bucket, String resultFileDir) throws IOException {
        this(auth, configuration, bucket, resultFileDir, 0);
    }

    protected String processLine(Map<String, String> line) throws QiniuException {
        Response response = bucketManager.delete(bucket, line.get("key"));
        return response.statusCode + "\t" + HttpResponseUtils.getResult(response);
    }

    synchronized protected BatchOperations getOperations(List<Map<String, String>> lineList) throws QiniuException {

        List<String> keyList = lineList.stream().map(line -> line.get("key"))
                .filter(key -> key != null && !"".equals(key)).collect(Collectors.toList());
        if (keyList.size() == 0) throw new QiniuException(null, "there is no key in line.");
        batchOperations.addDeleteOp(bucket, keyList.toArray(new String[]{}));
        return batchOperations;
    }
}
