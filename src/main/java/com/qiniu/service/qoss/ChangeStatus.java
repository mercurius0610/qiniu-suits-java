package com.qiniu.service.qoss;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager.*;
import com.qiniu.service.interfaces.ILineProcess;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;
import com.qiniu.util.HttpResponseUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChangeStatus extends OperationBase implements ILineProcess<Map<String, String>>, Cloneable {

    private int status;

    public ChangeStatus(Auth auth, Configuration configuration, String bucket, int status, String resultPath,
                        int resultIndex) throws IOException {
        super(auth, configuration, bucket, "status", resultPath, resultIndex);
        this.status = status;
    }

    public ChangeStatus(Auth auth, Configuration configuration, String bucket, int status, String resultPath)
            throws IOException {
        this(auth, configuration, bucket, status, resultPath, 0);
    }

    protected String processLine(Map<String, String> line) throws QiniuException {
        Response response = bucketManager.changeStatus(bucket, line.get("key"), status);
        return response.statusCode + "\t" + HttpResponseUtils.getResult(response);
    }

    synchronized protected BatchOperations getOperations(List<Map<String, String>> lineList){
        List<String> keyList = lineList.stream().map(line -> line.get("key")).collect(Collectors.toList());
        return batchOperations.addChangeStatusOps(bucket, status, keyList.toArray(new String[]{}));
    }
}
