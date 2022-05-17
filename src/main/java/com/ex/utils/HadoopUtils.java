package com.ex.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;

import java.io.IOException;

public class HadoopUtils {
    Configuration configuration;
    FileSystem fileSystem;
    String HADOOP_PATH = "hdfs://node1:9000";

    public HadoopUtils() {
        try {
            configuration = new Configuration();
            System.setProperty("HADOOP_USER_NAME", "root");
            configuration.set("fs.defaultFS", HADOOP_PATH);
            fileSystem = FileSystem.get(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HadoopUtils(String path) {
        
    }

}
