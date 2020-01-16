package com.bcs.core.log.appender;

import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.helpers.CountingQuietWriter;
import org.apache.log4j.helpers.LogLog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Alan
 * @apiNote use "com.bcs.core.log.appender.RollingWithTimeAndSizeAppender"
 */
public class RollingWithTimeAndSizeAppender extends RollingFileAppender {
    /**
     * Date pattern
     */
    private String datePattern;
    /**
     * Suffix date time string
     */
    private String dateStr = "";
    /**
     * Log expired day
     */
    private String expiredDays = "1";
    /**
     * Is clean log
     */
    private String isCleanLog = "true";
    /**
     * Log max count index
     */
    private String maxIndex = "100";
    /**
     * Root direction path
     */
    private File rootDir;


    /**
     * Do roll
     */
    @Override
    public void rollOver() {
        dateStr = new SimpleDateFormat(this.datePattern).format(new Date(System.currentTimeMillis()));
        File target;
        File file;
        if (qw != null) {
            long size = ((CountingQuietWriter) this.qw).getCount();
            LogLog.debug("Rolling over count=" + size);
        }
        LogLog.debug("Max Backup Index=" + this.maxBackupIndex);
        //如果maxIndex<=0則不需命名
        if (maxIndex != null && Integer.parseInt(maxIndex) > 0) {
            //刪除舊文件
            file = new File(this.fileName + '.' + dateStr + '.' + Integer.parseInt(this.maxIndex) + ".log");
            //如果當天日誌達到最大設置數量，則刪除當天第一個日誌，其他日誌爲尾號減一
            if (file.exists() && !reLogNum()) {
                LogLog.debug("日誌滾動重命名失敗！");
            }
        }
        //獲取當天日期文件個數
        int count = cleanLog();
        //生成新文件
        target = new File(fileName + "." + dateStr + "." + (count + 1) + ".log");
        this.closeFile();
        file = new File(fileName);
        LogLog.debug("Renaming file" + file + "to" + target);
        boolean isRenamed = file.renameTo(target);
        LogLog.debug("Is rename success? " + isRenamed);
        try {
            setFile(this.fileName, false, this.bufferedIO, this.bufferSize);
        } catch (IOException e) {
            LogLog.error("setFile(" + this.fileName + ",false)call failed.", e);
        }
    }

    /**
     * Clean expired log
     *
     * @return Cleaned log count
     */
    public int cleanLog() {
        //記錄當天文件個數
        int count = 0;
        if (Boolean.parseBoolean(isCleanLog)) {
            File f = new File(fileName);
            rootDir = f.getParentFile();
            File[] listFiles = rootDir.listFiles();
            if (listFiles == null) {
                return 0;
            }
            for (File file : listFiles) {
                if (file.getName().contains(dateStr)) {
                    count = count + 1;
                } else {
                    if (Boolean.parseBoolean(isCleanLog)) {
                        // Do clean expired log
                        String[] split = file.getName().split("\\\\")[0].split("\\.");
                        // Fetch date string in file name and check expired time
                        if (split.length == 4 && isExpTime(split[2])) {
                            boolean isDeleted = file.delete();
                            LogLog.debug("is deleted? " + isDeleted);
                        }
                    }
                }
            }
        }
        return count;
    }

    /**
     * is Expire
     *
     * @param time time
     * @return Is expired
     */
    public boolean isExpTime(String time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date logTime = format.parse(time);
            Date nowTime = format.parse(format.format(new Date()));
            //算出日誌與當前日期相差幾天
            int days = (int) (nowTime.getTime() - logTime.getTime()) / (1000 * 3600 * 24);
            return Math.abs(days) >= Integer.parseInt(expiredDays);
        } catch (Exception e) {
            LogLog.error(e.toString());
            return false;
        }
    }

    /**
     * if 當天日誌達到最大設置數量
     * then 每次刪除尾號爲1的日誌
     * and 其他日誌編號依次減去1，重命名
     *
     * @return Is rename
     */
    public boolean reLogNum() {
        boolean renameTo = false;
        File startFile = new File(this.fileName + '.' + dateStr + '.' + "1");
        if (startFile.exists() && startFile.delete()) {
            for (int i = 2; i <= Integer.parseInt(maxIndex); i++) {
                File target = new File(this.fileName + '.' + dateStr + '.' + (i - 1));
                this.closeFile();
                File file = new File(this.fileName + '.' + dateStr + '.' + i);
                renameTo = file.renameTo(target);
            }
        }
        return renameTo;
    }

    /**
     * Set Date Pattern
     *
     * @param pattern pattern
     */
    public void setDatePattern(String pattern) {
        if (null != pattern && !"".equals(pattern)) {
            this.datePattern = pattern;
        }
    }

    public String getDatePattern() {
        return this.datePattern;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getExpiredDays() {
        return expiredDays;
    }

    public void setExpiredDays(String expiredDays) {
        this.expiredDays = expiredDays;
    }

    public String getIsCleanLog() {
        return isCleanLog;
    }

    public void setIsCleanLog(String isCleanLog) {
        this.isCleanLog = isCleanLog;
    }

    public String getMaxIndex() {
        return maxIndex;
    }

    public void setMaxIndex(String maxIndex) {
        this.maxIndex = maxIndex;
    }

}
