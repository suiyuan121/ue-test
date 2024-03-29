package com.baidu.ueditor.hunter;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import ls.demon.xx.BaseModel;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.ueditor.PathFormat;
import com.baidu.ueditor.define.AppInfo;
import com.baidu.ueditor.define.BaseState;
import com.baidu.ueditor.define.MultiState;
import com.baidu.ueditor.define.State;

public class FileManager extends BaseModel {
    /**  */
    private static final long   serialVersionUID = 4275085241016731099L;

    /**
    * Logger for this class
    */
    private static final Logger logger           = LoggerFactory.getLogger(FileManager.class);

    private String              dir              = null;
    private String              rootPath         = null;
    private String[]            allowFiles       = null;
    private int                 count            = 0;

    public FileManager(Map<String, Object> conf) {
        logger.info("{}", conf);

        this.rootPath = (String) conf.get("rootPath");
        this.dir = this.rootPath + (String) conf.get("dir");
        this.allowFiles = this.getAllowFiles(conf.get("allowFiles"));
        this.count = (Integer) conf.get("count");

        logger.info("{}", this);
    }

    public State listFile(int index) {
        logger.info("listFile index={}", index);

        File dir = new File(this.dir);
        State state = null;

        if (!dir.exists()) {
            return new BaseState(false, AppInfo.NOT_EXIST);
        }

        if (!dir.isDirectory()) {
            return new BaseState(false, AppInfo.NOT_DIRECTORY);
        }

        Collection<File> list = FileUtils.listFiles(dir, this.allowFiles, true);

        if (index < 0 || index > list.size()) {
            state = new MultiState(true);
        } else {
            Object[] fileList = Arrays.copyOfRange(list.toArray(), index, index + this.count);
            state = this.getState(fileList);
        }

        state.putInfo("start", index);
        state.putInfo("total", list.size());

        logger.info("state = {}", state);
        return state;

    }

    private State getState(Object[] files) {

        MultiState state = new MultiState(true);
        BaseState fileState = null;

        File file = null;

        for (Object obj : files) {
            if (obj == null) {
                break;
            }
            file = (File) obj;
            fileState = new BaseState(true);
            String url = PathFormat.format(this.getPath(file));

            logger.info("file={}, url={}", file, url);
            fileState.putInfo("url", url);
            state.addState(fileState);
        }

        return state;

    }

    private String getPath(File file) {
        String path = PathFormat.format(file.getAbsolutePath());
        return path.replace(this.rootPath, "/");
    }

    private String[] getAllowFiles(Object fileExt) {

        String[] exts = null;
        String ext = null;

        if (fileExt == null) {
            return new String[0];
        }

        exts = (String[]) fileExt;

        for (int i = 0, len = exts.length; i < len; i++) {

            ext = exts[i];
            exts[i] = ext.replace(".", "");

        }

        return exts;

    }

}
