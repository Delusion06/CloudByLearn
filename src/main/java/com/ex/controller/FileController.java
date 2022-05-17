package com.ex.controller;

import com.ex.service.HdfsService;
import com.ex.vo.JsonModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author Exception
 * @create 2021-06-07-21:27
 */
@Controller
@RequestMapping("/back/hdfs")
public class FileController {
    @Autowired
    private HdfsService hdfsService;

    @RequestMapping(value = "/uploadData.action",method = RequestMethod.POST)
    @ResponseBody
    public JsonModel uploadData(MultipartFile file, @RequestParam("currentPath") String currentPath, HttpServletRequest req,JsonModel jm) throws Exception{
        try {
            this.hdfsService.createFile(currentPath,file);
            jm.setCode(1);
            List<Map<String, String>> list = hdfsService.listStatus(currentPath);
            jm.setObj(list);
        }catch (Exception e){
            e.printStackTrace();
            jm.setCode(0);
            jm.setMsg(e.getMessage());
        }
        return jm;
    }

    @RequestMapping(value = "/downLoadDirectory.action",method = RequestMethod.GET)
    public ResponseEntity<byte[]> downLoadDirectory(@RequestParam("path") String path, @RequestParam("fileName")String fileName) throws Exception{
        ResponseEntity<byte[]> result = null;
        try {
            result = this.hdfsService.downloadDirectory(path,fileName);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/downLoadFile.action",method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downLoadFile(@RequestParam("path") String path, @RequestParam("fileName")String fileName) throws Exception{
        ResponseEntity<InputStreamResource> result = null;
        try {
            result = this.hdfsService.downloadFile(path,fileName);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
