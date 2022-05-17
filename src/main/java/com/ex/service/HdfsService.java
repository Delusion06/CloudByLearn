package com.ex.service;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.BlockLocation;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface HdfsService {

    public Map<String, String> getConfigurationInfoAsMap() throws Exception;

    /**
     * 获取HDFS配置信息
     */
    public Configuration getConfiguration() throws Exception;

    /**
     * 获取HDFS文件系统对象
     */
    public FileSystem getFileSystem() throws Exception;

    /**
     * 创建文件夹
     */
    public boolean mkdir(String path) throws Exception;

    /**
     * 判断文件是否存在
     */
    public boolean existFile(String path) throws Exception;

    /**
     * 读取HDFS目录信息
     */
    public List<Map<String, String>> readPathInfo(String path) throws Exception;

    /**
     * 在HDFS创建文件
     */
    public void createFile(String path, MultipartFile file) throws Exception;

    /**
     * 读取HDFS文件内容
     */
    public String readFile(String path) throws Exception;

    /**
     * 搜索整个磁盘中所有的某种类型的文件
     */
    public List<Map<String, String>> listStatus(int type) throws Exception;

    /**
     * 获取某个文件的信息
     */
    public Map<String, String> getFileInfo(String path) throws Exception;

    /**
     * 将fileStatus转为一个Map
     */
    public Map<String, String> fileStatusToMap(FileStatus file) throws Exception;

    public List<Map<String, String>> listStatus(String path) throws Exception;

    /**
     * 读取HDFS文件列表
     */
    public List<Map<String, String>> listFile(String path) throws Exception;

    /**
     * HDFS重命名文件
     */
    public boolean renameFile(String oldName,String newName) throws Exception;

    /**
     * 删除HDFS文件
     */
    public boolean deleteFile(String path) throws Exception;

    /**
     * 上传HDFS文件
     */
    public void uploadFile(String path,String uploadPath) throws Exception;

    /**
     * 下载HDFS文件
     */
    public ResponseEntity<InputStreamResource> downloadFile(String path, String fileName) throws Exception;

    /**
     * 压缩打包目录
     */
    public ResponseEntity<byte[]> downloadDirectory(String path,String fileName) throws Exception;

    /**
     * HDFS文件复制
     */
    public void copyFile(String sourcePath,String targetPath) throws Exception;

    /**
     * 打开HDFS上的文件并返回byte数组
     */
    public byte[] openFileToBytes(String path) throws Exception;

    /**
     * 获取某个文件在HDFS的集群位置
     */
    public BlockLocation[] getBlockLocation(String path) throws Exception;

}
