package com.ex.service;

import com.ex.utils.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class HdfsServiceImpl implements HdfsService {
    @Value("${hdfs.path}")
    private String path;
    @Value("${hdfs.username}")
    private String username;

    private final int butterSize = 1024 * 1024 * 64;

    /**
     * 获取HDFS配置信息
     *
     * @return
     * @throws Exception
     */
    @Override
    public Configuration getConfiguration() {
        Configuration configuration = new Configuration();
        configuration.set("fs.defaultFS", path);
        //其它参数
        return configuration;
    }

    /**
     * 获取HDFS文件系统对象
     *
     * @return
     * @throws Exception
     */
    @Override
    public FileSystem getFileSystem() throws Exception {
        //客户端去操作hdfs时是有一个用户身份的,默认情况下hdfs客户端api会从jvm中获取一个参数作为自己的用户身份
        //DHADOOP_USER_NAME=hadoop
        //也可以在构造客户端fs对象时,通过参数传递进去
        FileSystem fileSystem = FileSystem.get(new URI(path), getConfiguration(), username);
        return fileSystem;
    }

    @Override
    public Map<String, String> getConfigurationInfoAsMap() throws Exception {
        FileSystem fs = getFileSystem();
        Configuration conf = fs.getConf();
        Iterator<Map.Entry<String, String>> ite = conf.iterator();
        Map<String, String> map = new HashMap<>();
        while (ite.hasNext()){
            Map.Entry<String, String> entry = ite.next();
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * 在HDFS创建文件夹
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public boolean mkdir(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        if (existFile(path)) {
            return true;
        }
        FileSystem fs = getFileSystem();
        //目标路径
        Path srcPath = new Path(path);
        boolean isOk = fs.mkdirs(srcPath);

        fs.close();
        return isOk;
    }

    /**
     * 判断HDFS文件是否存在
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public boolean existFile(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        FileSystem fs = getFileSystem();
        Path srcPath = new Path(path);
        boolean isExists = fs.exists(srcPath);
        return isExists;
    }

    /**
     * 读取HDFS目录信息
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, String>> readPathInfo(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        if (!existFile(path)) {
            return null;
        }
        FileSystem fs = getFileSystem();
        //目标路径
        Path newPath = new Path(path);
        FileStatus[] statusList = fs.listStatus(newPath);
        List<Map<String, String>> returnList = new ArrayList<>();
        for (FileStatus file : statusList) {
            Map<String, String> map = fileStatusToMap(file);
            returnList.add(map);
        }
        fs.close();
        return returnList;
    }

    /**
     * HDFS创建文件
     *
     * @param path
     * @param file
     * @throws Exception
     */
    @Override
    public void createFile(String path, MultipartFile file) throws Exception {
        if (StringUtils.isEmpty(path) || null == file.getBytes()) {
            return;
        }
        String fileName = file.getOriginalFilename();
        FileSystem fs = getFileSystem();
        //上传时默认当前目录,后面自动拼接文件的目录
        Path newPath = null;
        if ("/".equals(path)) {
            newPath = new Path(path + fileName);
        } else {
            newPath = new Path(path + "/" + fileName);
        }
        //打开一个输出流
        FSDataOutputStream outputStream = fs.create(newPath);
        outputStream.write(file.getBytes());
        outputStream.close();
        fs.close();
    }

    /**
     * 读取HDFS文件内容
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public String readFile(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        if (!existFile(path)) {
            return null;
        }
        FileSystem fs = getFileSystem();
        //目标路径
        Path srcPath = new Path(path);
        FSDataInputStream inputStream = null;
        try {
            inputStream = fs.open(srcPath);
            //防止中文乱码
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String lineTxt = "";
            StringBuffer sb = new StringBuffer();
            while ((lineTxt = reader.readLine()) != null) {
                sb.append(lineTxt);
            }
            return sb.toString();
        } finally {
            inputStream.close();
            fs.close();
        }
    }

    @Override
    public List<Map<String, String>> listStatus(int type) throws Exception {
        String path = "/";//查找全盘所有的文件
        //目标路径
        Path srcPath = new Path(path);
        List<Map<String, String>> returnList = new ArrayList<>();
        String reg = null;
        if (type == 1) {
            reg = "\\.+(.jpeg|.jpg|.png|.bmp|.gif)$";
        } else if (type == 2) {
            reg = "\\.+(.txt|.rtf|.doc|.docx|.xls|.xlsx|.html|.xml)$";
        } else if (type == 3) {
            reg = "\\.+(.mp4|.avi|.wmv)$";
        } else if (type == 4) {
            reg = "\\.+(.mp3|.wav)$";
        } else if (type == 5) {
            reg = "^\\S+\\.*$";
        }
        Pattern pattern = Pattern.compile(reg, Pattern.CASE_INSENSITIVE);
        search(srcPath, returnList, pattern);
        return returnList;
    }

    private void search(Path srcPath, List<Map<String, String>> list, Pattern pattern) throws Exception {
        FileSystem fs = getFileSystem();
        FileStatus[] fileStatuses = fs.listStatus(srcPath);
        if (fileStatuses != null && fileStatuses.length > 0) {
            for (FileStatus file : fileStatuses) {
                boolean result = file.isFile();
                if (!result) {
                    //是目录,则递归
                    search(file.getPath(), list, pattern);
                } else {
                    //是文件,则判断类型
                    boolean b = pattern.matcher(file.getPath().getName()).find();
                    if (b) {
                        Map<String, String> map = this.fileStatusToMap(file);
                        list.add(map);
                    }
                }
            }
        }
    }

    /**
     * 获取某个文件的信息
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> getFileInfo(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        if (!existFile(path)) {
            return null;
        }
        FileSystem fs = getFileSystem();
        //目标路径
        Path srcPath = new Path(path);
        FileStatus fileStatus = fs.getFileStatus(srcPath);
        return fileStatusToMap(fileStatus);
    }

    /**
     * 将fileStatus转为一个Map
     *
     * @param file
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> fileStatusToMap(FileStatus file) throws Exception {
        Map<String, String> map = new HashMap<>();
        Path p = file.getPath();
        map.put("fileName", p.getName());
        String filePath = p.toUri().toString();
        map.put("filePath", filePath);// hdfs://node1:9000/idea/a.txt
        String relativePath = filePath.substring(this.path.length());// /idea/a.txt
//      if ("/".equalsIgnoreCase(path)){
        map.put("relativePath", relativePath);// / + text
//      }else {
//          map.put("relativePath",srcPath + "/" + p.getName());//  /test + / + a
//      }
        map.put("parentPath", p.getParent().toUri().toString().substring(this.path.length()));// /idea
        map.put("owner", file.getOwner());
        map.put("group", file.getGroup());
        map.put("isFile", file.isFile() + "");
        map.put("duplicates", file.getReplication() + "");
        map.put("size", FileUtils.formatFileSize(file.getLen()));
        map.put("rights", file.getPermission().toString());
        map.put("modifyTime", FileUtils.formatTime(file.getModificationTime()));
        return map;
    }

    @Override
    public List<Map<String, String>> listStatus(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        if (!existFile(path)) {
            return null;
        }
        FileSystem fs = getFileSystem();
        //目标路径
        Path srcPath = new Path(path);
        FileStatus[] fileStatuses = fs.listStatus(srcPath);//   调用listStatus方法
        if (fileStatuses == null || fileStatuses.length <= 0) {
            return null;
        }
        List<Map<String, String>> returnList = new ArrayList<>();
        for (FileStatus file : fileStatuses) {
            Map<String, String> map = fileStatusToMap(file);
            returnList.add(map);
        }
        fs.close();
        return returnList;
    }

    /**
     * 读取HDFS文件列表
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public List<Map<String, String>> listFile(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        if (!existFile(path)) {
            return null;
        }

        FileSystem fs = getFileSystem();
        //目标路径
        Path srcPath = new Path(path);
        //递归找到所有文件
        RemoteIterator<LocatedFileStatus> filesList = fs.listFiles(srcPath, true);
        List<Map<String, String>> returnList = new ArrayList<>();
        while (filesList.hasNext()) {
            LocatedFileStatus next = filesList.next();
            String fileName = next.getPath().getName();
            Path filePath = next.getPath();
            Map<String, String> map = new HashMap<>();
            map.put("fileName", fileName);
            map.put("filePath", filePath.toString());
            returnList.add(map);
        }
        fs.close();
        return returnList;
    }

    /**
     * HDFS重命名文件
     *
     * @param oldName
     * @param newName
     * @return
     * @throws Exception
     */
    @Override
    public boolean renameFile(String oldName, String newName) throws Exception {
        if (StringUtils.isEmpty(oldName) || StringUtils.isEmpty(newName)) {
            return false;
        }
        FileSystem fs = getFileSystem();
        //源文件目标路径
        Path oldPath = new Path(oldName);
        //重命名目标路径
        Path newPath = new Path(newName);
        boolean isOk = fs.rename(oldPath, newPath);
        fs.close();
        return isOk;
    }

    /**
     * 删除HDFS文件
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteFile(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return false;
        }
        if (!existFile(path)) {
            return false;
        }
        FileSystem fs = getFileSystem();
        Path srcPath = new Path(path);
        boolean isOk = fs.delete(srcPath, true);//   是否递归删除目录
        fs.close();
        return isOk;
    }

    /**
     * 上传HDFS文件
     *
     * @param path
     * @param uploadPath
     * @throws Exception
     */
    @Override
    public void uploadFile(String path, String uploadPath) throws Exception {
        if (StringUtils.isEmpty(path) || StringUtils.isEmpty(uploadPath)) {
            return;
        }
        FileSystem fs = getFileSystem();
        //上传路径
        Path clientPath = new Path(path);
        //目标路径
        Path serverPath = new Path(uploadPath);

        //调用文件系统的文件复制方法,第一个参数是否删除源文件,true为删除,默认为false
        fs.copyFromLocalFile(false, clientPath, serverPath);
        fs.close();
    }

    /**
     * 下载HDFS文件
     *
     * @param path
     * @param fileName
     * @return
     * @throws Exception
     */
    @Override
    public ResponseEntity<InputStreamResource> downloadFile(String path, String fileName) throws Exception {
        FileSystem fs = this.getFileSystem();
        Path p = new Path(path);
        FSDataInputStream inputStream = fs.open(p);
        return FileUtils.downloadFile(inputStream, fileName);
    }

    @Override
    public ResponseEntity<byte[]> downloadDirectory(String path, String fileName) throws Exception {
        //  1、获取对象
        ByteArrayOutputStream out = null;// 字节输出流(内存)
        try {
            FileSystem fs = this.getFileSystem();
            out = new ByteArrayOutputStream();
            ZipOutputStream zos = new ZipOutputStream(out);//   压缩流
            compress(path, zos, fs);
            zos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        byte[] bs = out.toByteArray();
        out.close();
        return FileUtils.downloadDirectory(bs, FileUtils.genFileName(fileName));
    }

    //  压缩                     HDFS中等待下载的目录         压缩流
    public void compress(String baseDir, ZipOutputStream zipOutputStream, FileSystem fs) throws Exception {
        try {
            FileStatus[] fileStatulist = fs.listStatus(new Path(baseDir));
            String[] strs = baseDir.split("/");//   /a/b    /a.txt
            //  lastName代表路径最后的单词
            String lastName = strs[strs.length - 1];
            for (int i = 0; i < fileStatulist.length; i++) {
                String name = fileStatulist[i].getPath().toString();
                name = name.substring(name.indexOf("/" + lastName));//  子目录名或子文件名
                if (fileStatulist[i].isFile()) {
                    //  如果baseDir下的一个文件是File,以流读取
                    Path path = fileStatulist[i].getPath();
                    FSDataInputStream inputStream = fs.open(path);
                    zipOutputStream.putNextEntry(new ZipEntry(name.substring(1)));
                    IOUtils.copyBytes(inputStream, zipOutputStream, this.butterSize);
                    inputStream.close();
                } else {
                    zipOutputStream.putNextEntry(new ZipEntry(fileStatulist[i].getPath().getName() + "/"));
                    compress(fileStatulist[i].getPath().toString(), zipOutputStream, fs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * HDFS文件复制
     *
     * @param sourcePath
     * @param targetPath
     * @throws Exception
     */
    @Override
    public void copyFile(String sourcePath, String targetPath) throws Exception {
        if (StringUtils.isEmpty(sourcePath) || StringUtils.isEmpty(targetPath)) {
            return;
        }
        FileSystem fs = getFileSystem();
        //上传路径
        Path oldPath = new Path(sourcePath);
        //目标路径
        Path newPath = new Path(targetPath);

        FSDataInputStream inputStream = null;
        FSDataOutputStream outputStream = null;
        try {
            inputStream = fs.open(oldPath);
            outputStream = fs.create(newPath);
            IOUtils.copyBytes(inputStream, outputStream, butterSize, false);
        } finally {
            inputStream.close();
            outputStream.close();
            fs.close();
        }
    }

    /**
     * 打开HDFS上的文件并返回byte数组
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public byte[] openFileToBytes(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        if (!existFile(path)) {
            return null;
        }
        byte[] result = null;
        FileSystem fs = getFileSystem();
        //目标路径
        Path srcPath = new Path(path);
        try {
            FSDataInputStream inputStream = fs.open(srcPath);
            InputStream iis = inputStream.getWrappedStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] bs = new byte[10 * 1024];
            int length = 0;
            while ((length = iis.read(bs, 0, bs.length)) != -1) {
                baos.write(bs, 0, length);
            }
            baos.flush();
            result = baos.toByteArray();
        } finally {
            fs.close();
        }
        return result;
    }

    /**
     * 读取某个文件在HDFS的集群位置
     *
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public BlockLocation[] getBlockLocation(String path) throws Exception {
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        if (!existFile(path)) {
            return null;
        }
        FileSystem fs = getFileSystem();
        //目标路径
        Path srcPath = new Path(path);
        FileStatus fileStatus = fs.getFileStatus(srcPath);
        return fs.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
    }


}
