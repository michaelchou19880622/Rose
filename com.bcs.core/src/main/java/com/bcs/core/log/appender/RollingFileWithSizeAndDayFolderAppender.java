package com.bcs.core.log.appender;

import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RollingFileWithSizeAndDayFolderAppender extends RollingFileAppender {

    /**
     * File date pattern
     */
    private String fileDatePattern;
    /**
     * Folder date pattern
     */
    private String folderDatePattern;

    private long nextRollover = 0;
    private SimpleDateFormat df;

    @Override
    public void rollOver() {
        File target;
        File file;

        if (qw != null) {
            long size = ((CountingQuietWriter) qw).getCount();
            nextRollover = size + maxFileSize;
        }
        LogLog.debug("maxBackupIndex=" + maxBackupIndex);

        boolean renameSucceeded = true;
        if (maxBackupIndex > 0) {
            // File index increase
            for (int i = maxBackupIndex - 1; i >= 1 && renameSucceeded; i--) {
                file = new File(genFileName(fileName, i));
                if (file.exists()) {
                    target = new File(genFileName(fileName, i + 1));
                    renameSucceeded = file.renameTo(target);
                }
            }

            if (renameSucceeded) {
                target = new File(genFileName(fileName, 1));
                this.closeFile();
                file = new File(fileName);
                renameSucceeded = file.renameTo(target);

                if (!renameSucceeded) {
                    try {
                        this.setFile(fileName, true, bufferedIO, bufferSize);
                    } catch (InterruptedIOException ie) {
                        Thread.currentThread().interrupt();
                        LogLog.error("setFile(" + fileName + ", true) call failed.", ie);
                    } catch (IOException e) {
                        LogLog.error("setFile(" + fileName + ", true) call failed.", e);
                    }
                }
            }
        }
        if (renameSucceeded) {
            try {
                this.setFile(fileName, false, bufferedIO, bufferSize);
                nextRollover = 0;
            } catch (InterruptedIOException ie) {
                Thread.currentThread().interrupt();
                LogLog.error("setFile(" + fileName + ", false) call failed.", ie);
            } catch (IOException e) {
                LogLog.error("setFile(" + fileName + ", false) call failed.", e);
            }
        }
    }

    @Override
    protected void subAppend(LoggingEvent event) {
        super.subAppend(event);
        if (fileName != null && qw != null) {
            long size = ((CountingQuietWriter) qw).getCount();
            if (size >= maxFileSize && size >= nextRollover) {
                rollOver();
            }
            String tempName = fileName.substring(0, fileName.lastIndexOf('/'));

            df = new SimpleDateFormat(this.folderDatePattern);
            /* Append log to subfolder */
            fileName = tempName.substring(0, tempName.lastIndexOf('/')) +
                    '/' +
                    df.format(new Date()) +
                    fileName.substring(fileName.lastIndexOf('/'));
            File file = new File(genFileName(fileName, 0));
            if (!file.exists()) {
                rollOver();
            }
        }
    }

    /**
     * Set File Path in Configuration file(XML, Properties)
     *
     * @param file file path
     */
    @Override
    public void setFile(String file) {
        String val = file.trim();
        df = new SimpleDateFormat(this.folderDatePattern);
        fileName = val.substring(0, val.lastIndexOf('/')) +
                '/' +
                df.format(new Date()) +
                val.substring(val.lastIndexOf('/'));
    }

    /**
     * Generate log file name with date time and index
     *
     * @param name  file name
     * @param index backup index
     * @return file name
     */
    private String genFileName(String name, int index) {
        df = new SimpleDateFormat(this.fileDatePattern);
        String dateStrSuffix = '_' + df.format(new Date()) + '_';
        String fileName = "";
        if (index > 0) {
            /* If index less 10 then add '0' else use origin index */
            String num = index < 10 ? "0" + index : String.valueOf(index);
            /* Always replace .log and add .log suffix */
            fileName = name.replace(".log", "") + dateStrSuffix + num + ".log";
        } else {
            fileName = name;
        }
        return fileName;
    }

    /**
     * Set Date Pattern
     *
     * @param pattern pattern
     */
    public void setFileDatePattern(String pattern) {
        if (null != pattern && !"".equals(pattern)) {
            this.fileDatePattern = pattern;
        } else {
            this.fileDatePattern = "yyyy-MM-dd-HH";
        }
    }

    public String getFileDatePattern() {
        return this.fileDatePattern == null ? "yyyy-MM-dd-HH" : this.fileDatePattern;
    }

    /**
     * Set Date Pattern
     *
     * @param pattern pattern
     */
    public void setFolderDatePattern(String pattern) {
        if (null != pattern && !"".equals(pattern)) {
            this.folderDatePattern = pattern;
        } else {
            this.folderDatePattern = "yyyyMMdd";
        }
    }

    public String getFolderDatePattern() {
        return this.folderDatePattern == null ? "yyyyMMdd" : this.folderDatePattern;
    }

//
//    log4j配置如下
//
//### 输出到日志文件 ###
//
//    log4j.appender.bc=com.bingchuangapi.common.base.Log4jRollingFileAppender
//
//    log4j.appender.bc.File=${catalina.home}/logs/appapidebug.log
//
//    log4j.appender.bc.MaxFileSize=102400KB
//
//    log4j.appender.bc.Append=true
//
//    log4j.appender.bc.Threshold = debug
//
//    log4j.appender.bc.Encoding=UTF-8
//
//    log4j.appender.bc.MaxBackupIndex=100
//
//    log4j.appender.bc.layout=org.apache.log4j.PatternLayout
//
//    log4j.appender.bc.layout.ConversionPattern=%d{yyyy/MM/dd HH:mm:ss,SSS} %-5p %-20.20X{SessionID} %-20.20X{RequestID} %-10.10X{UserID} %-26.26c{1} %m%n
}
