package com.qiniu.process.qiniu;

import com.qiniu.config.PropertiesFile;
import com.qiniu.process.qiniu.ImageCensor;
import com.qiniu.storage.Configuration;
import org.junit.Test;

import java.util.HashMap;

public class ImageCensorTest {

    @Test
    public void singleResult() throws Exception {
        PropertiesFile propertiesFile = new PropertiesFile("resources/.application.properties");
        String accessKey = propertiesFile.getValue("ak");
        String secretKey = propertiesFile.getValue("sk");
        ImageCensor imageCensor = new ImageCensor(accessKey, secretKey, new Configuration(), null, null,
                "url", "?imageView2/0/w/4999/h/4999", new String[]{"pulp", "ads"});
        String result = imageCensor.singleResult(new HashMap<String, String>(){{
            put("url", "http://xxx.cn/upload/24790f63-0936-44c4-8695-a6d6b1dd8d91.jpg");
            put("mime", "iii");
        }});
//        ImageCensor imageCensor = new ImageCensor(accessKey, secretKey, new Configuration(), null, null,
//                "url", "-w480", new String[]{"pulp"});
//        String result = imageCensor.singleResult(new HashMap<String, String>(){{
//            put("url", "https://xxx.com/58pic/35/20/38/34858PIC1uc642wj5HM73_PIC2018.png");
//        }});
        System.out.println(result);
    }
}