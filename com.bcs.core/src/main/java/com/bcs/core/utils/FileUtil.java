package com.bcs.core.utils;

import com.bcs.core.db.entity.ContentResource;
import com.bcs.core.db.entity.ContentResourceFile;
import com.bcs.core.db.service.ContentResourceFileService;
import com.bcs.core.enums.CONFIG_STR;
import com.bcs.core.exception.BcsNoticeException;
import com.bcs.core.resource.CoreConfigReader;
import com.bcs.core.spring.ApplicationContextProvider;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jcodec.api.awt.FrameGrab;
import org.jcodec.common.FileChannelWrapper;
import org.jcodec.common.NIOUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;

public class FileUtil {
    public static final String RESIZE_PRE = "RESIZE_";

    /**
     * Logger
     */
    private static Logger logger = Logger.getLogger(FileUtil.class);

    public static ContentResource uploadFile(MultipartFile filePart, String fileUrl, String resourceType, String modifyUser) throws Exception {
        String resourceId = null;
        String resourceTitle = null;
        Long resourceSize = 0L;
        String contentType = null;
        URL url = null;
        URLConnection connection = null;

        if (fileUrl == null) {
            resourceTitle = filePart.getOriginalFilename();
            resourceSize = filePart.getSize();
            contentType = filePart.getContentType();

            resourceId = UUID.randomUUID().toString().toLowerCase();
        } else {
            String proxyUrl = CoreConfigReader.getString(CONFIG_STR.TAISHIN_PROXY_URL.toString(), true);

            url = new URL(fileUrl);
            connection = (proxyUrl == null) ? url.openConnection() : url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl, 80)));

            resourceTitle = url.getFile().substring(url.getFile().lastIndexOf('/') + 1);
            resourceSize = Long.parseLong(connection.getHeaderField("Content-Length"));
            contentType = connection.getHeaderField("Content-Type");

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(fileUrl.split("://")[1].getBytes());

            resourceId = new BigInteger(1, md.digest()).toString(16);
        }

        logger.info("resourceId: " + resourceId);
        logger.info("resourceTitle: " + resourceTitle);
        logger.info("resourceSize: " + resourceSize);
        logger.info("contentType: " + contentType);

        String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + resourceType;

        logger.info("uploadFile:" + filePath + System.getProperty("file.separator") + resourceId);

        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        String errorMsg = "";
        boolean isBcsNoticeException = false;
        try {
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File genfile = new File(filePath + System.getProperty("file.separator") + resourceId);

            if (!genfile.exists()) {
                genfile.createNewFile();
            }
            out = new BufferedOutputStream(new FileOutputStream(genfile));
            in = (fileUrl == null) ? new BufferedInputStream(filePart.getInputStream()) : new BufferedInputStream(connection.getInputStream());

            return uploadFile(filePath, out, in, genfile, resourceId, resourceTitle, resourceSize, resourceType, modifyUser, contentType, true, true);
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                isBcsNoticeException = true;
            }
            errorMsg = e.getMessage();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            logger.debug("finally");
        }

        if (isBcsNoticeException) {
            throw new BcsNoticeException(errorMsg);
        } else {
            throw new Exception(errorMsg);
        }
    }

    private static void saveFileToDB(String filePath, String resourceId, Date date, String modifyUser) throws Exception {

        File file = new File(filePath + System.getProperty("file.separator") + resourceId);

        ContentResourceFile contentResourceFile = new ContentResourceFile(resourceId);

        byte[] bFile = new byte[(int) file.length()];
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            fileInputStream.read(bFile);
        }

        contentResourceFile.setFileData(bFile);
        contentResourceFile.setModifyTime(date);
        contentResourceFile.setModifyUser(modifyUser);

        ApplicationContextProvider.getApplicationContext().getBean(ContentResourceFileService.class).save(contentResourceFile);
    }

    private static void saveFileToDB(BufferedInputStream in, String resourceId, Date date, String modifyUser) throws Exception {

//		File file = new File(filePath + System.getProperty("file.separator") + resourceId);

        ContentResourceFile contentResourceFile = new ContentResourceFile(resourceId);

//        byte[] bFile = new byte[(int) file.length()];
        byte[] bFile = IOUtils.toByteArray(in);
//        FileInputStream fileInputStream = new FileInputStream(file);
//        in.read(bFile);
        in.close();

        contentResourceFile.setFileData(bFile);
        contentResourceFile.setModifyTime(date);
        contentResourceFile.setModifyUser(modifyUser);

        ApplicationContextProvider.getApplicationContext().getBean(ContentResourceFileService.class).save(contentResourceFile);
    }

    public static ContentResource uploadFile(InputStream inputStream,
                                             String resourceTitle,
                                             Long resourceSize,
                                             String contentType,
                                             String resourceType,
                                             String modifyUser) throws Exception {
        return uploadFile(inputStream, resourceTitle, resourceSize, contentType, resourceType, modifyUser, null, true, false);
    }

    private static ContentResource uploadFile(String filePath, BufferedOutputStream out, BufferedInputStream in, File genfile,
                                              String resourceId, String resourceTitle, Long resourceSize, String resourceType,
                                              String modifyUser, String contentType,
                                              boolean saveToDb,
                                              boolean saveToDisk) throws Exception {

        if (saveToDisk) {
            org.apache.commons.io.IOUtils.copy(in, out);

            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }

        Date date = new Date();

        // Create Bean Data
        ContentResource resource = new ContentResource();
        resource.setResourceId(resourceId);
        resource.setResourceTitle(resourceTitle);
        resource.setResourceSize(resourceSize);
        resource.setResourceType(resourceType);
        resource.setModifyUser(modifyUser);
        resource.setModifyTime(date);
        resource.setContentType(contentType);
        resource.setUseFlag(false);

        if (ContentResource.RESOURCE_TYPE_AUDIO.equals(resourceType)) {
            resource.setResourceLength(1000);
        } else if (ContentResource.RESOURCE_TYPE_VIDEO.equals(resourceType)) {
            File file = new File(filePath + System.getProperty("file.separator") + resourceId);

            File previewfolder = new File(filePath + System.getProperty("file.separator") + ContentResource.RESOURCE_TYPE_IMAGE + System.getProperty("file.separator"));
            if (!previewfolder.exists()) {
                previewfolder.mkdirs();
            }

            String previewPath = filePath + System.getProperty("file.separator") + ContentResource.RESOURCE_TYPE_IMAGE + System.getProperty("file.separator") + resourceId;
            logger.info("uploadFile previewPath:" + previewPath);

            createVideoPreview(file, new File(previewPath));

            resource.setResourcePreview(ContentResource.RESOURCE_TYPE_IMAGE);
        } else if (ContentResource.RESOURCE_TYPE_IMAGE.equals(resourceType)) {

            updateImageResource(genfile, resource);

            if (resource.getResourceHeight() != null) {

//				Long height = resource.getResourceHeight();

//				if(height > 2080){
//					throw new BcsNoticeException("高度不能大於 2080");
//				}
            }

            try {

                createPreviewImage(filePath, resourceId);
                createResizeImage(filePath, resourceId, "240");
                createResizeImage(filePath, resourceId, "300");
                createResizeImage(filePath, resourceId, "460");
                createResizeImage(filePath, resourceId, "700");
                createResizeImage(filePath, resourceId, "1040");

                resource.setResourcePreview(ContentResource.RESOURCE_TYPE_IMAGE);
            } catch (IOException e) {
                logger.error(ErrorRecord.recordError(e));
            }
        } else if (ContentResource.RESOURCE_TYPE_RECEIVEIMAGE.equals(resourceType)) {

//			updateImageResource(genfile, resource);
        }

        if (!saveToDisk) {

            if (saveToDb) {
                saveFileToDB(in, resourceId, date, modifyUser);
            }
        } else {
            if (saveToDb) {
                saveFileToDB(filePath, resourceId, date, modifyUser);
            }
        }

        return resource;
    }

    public static ContentResource uploadFile(InputStream inputStream,
                                             String resourceTitle,
                                             Long resourceSize,
                                             String contentType,
                                             String resourceType,
                                             String modifyUser,
                                             String resourceId,
                                             boolean saveToDb,
                                             boolean saveToDisk) throws Exception {

        if (StringUtils.isBlank(resourceId)) {
            resourceId = UUID.randomUUID().toString().toLowerCase();
        }

        String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + resourceType;

        logger.info("resourceTitle:" + resourceTitle);
        logger.info("uploadFile:" + filePath + System.getProperty("file.separator") + resourceId);

        BufferedOutputStream out = null;
        BufferedInputStream in = null;

        String errorMsg = "";
        boolean isBcsNoticeException = false;
        try {
            if (saveToDisk) {
                File folder = new File(filePath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                File genfile = new File(filePath + System.getProperty("file.separator") + resourceId);

                if (!genfile.exists()) {
                    genfile.createNewFile();
                }
                out = new BufferedOutputStream(new FileOutputStream(genfile));
                in = new BufferedInputStream(inputStream);

                return uploadFile(filePath, out, in, genfile, resourceId, resourceTitle, resourceSize, resourceType, modifyUser, contentType, saveToDb, saveToDisk);
            } else {
                in = new BufferedInputStream(inputStream);

                return uploadFile(filePath, out, in, null, resourceId, resourceTitle, resourceSize, resourceType, modifyUser, contentType, saveToDb, saveToDisk);
            }
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            if (e instanceof BcsNoticeException) {
                isBcsNoticeException = true;
            }
            errorMsg = e.getMessage();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            logger.debug("finally");
        }

        if (isBcsNoticeException) {
            throw new BcsNoticeException(errorMsg);
        } else {
            throw new Exception(errorMsg);
        }
    }

    private static void updateImageResource(File file, ContentResource resource) {
        try {
            BufferedImage originalImage = ImageIO.read(file);
            Long height = Long.valueOf(originalImage.getHeight());
            logger.debug("height:" + height);
            Long width = Long.valueOf(originalImage.getWidth());
            logger.debug("width:" + width);

            resource.setResourceHeight(height);
            resource.setResourceWidth(width);
        } catch (IOException e) {
            logger.error(ErrorRecord.recordError(e));
        }
    }

    private static void createResizeImage(String filePath, String resourceId, String resize) throws Exception {

        FileOutputStream out = null;
        FileInputStream in = null;

        try {
            File folder = new File(filePath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File fileResourceId = new File(filePath + System.getProperty("file.separator") + RESIZE_PRE + resourceId);
            if (!fileResourceId.exists()) {
                fileResourceId.mkdirs();
            }

            File genfile = new File(filePath + System.getProperty("file.separator") + RESIZE_PRE + resourceId + System.getProperty("file.separator") + resize);

            if (!genfile.exists()) {
                genfile.createNewFile();
            }

            File file = new File(filePath + System.getProperty("file.separator") + resourceId);
            out = new FileOutputStream(genfile);
            in = new FileInputStream(file);
            org.apache.commons.io.IOUtils.copy(in, out);

            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }

        } catch (IOException e) {
            logger.error(ErrorRecord.recordError(e));
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            logger.debug("finally");
        }
    }

    private static void createPreviewImage(String filePath, String resourceId) throws IOException {

        File file = new File(filePath + System.getProperty("file.separator") + resourceId);
        BufferedImage resizeImageJpg = resizeImageForPreview(file);

        File previewfolder = new File(filePath + System.getProperty("file.separator") + ContentResource.RESOURCE_TYPE_IMAGE + System.getProperty("file.separator"));
        if (!previewfolder.exists()) {
            previewfolder.mkdirs();
        }
        String previewPath = filePath + System.getProperty("file.separator") + ContentResource.RESOURCE_TYPE_IMAGE + System.getProperty("file.separator") + resourceId;

        logger.info("uploadFile previewPath:" + previewPath);
        ImageIO.write(resizeImageJpg, "jpg", new File(previewPath));
    }

    private static BufferedImage resizeImageForPreview(File file) {
        logger.debug("resizeImageForPreview");

        try {
            BufferedImage originalImage = ImageIO.read(file);
            int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

            int height = originalImage.getHeight();
            logger.debug("height:" + height);
            int width = originalImage.getWidth();
            logger.debug("width:" + width);

            int toHeight = 240;
            int toWidth = 240;
            if (height >= width) {
                toHeight = 240;
                logger.debug(new BigDecimal((width + 0.0) / (height + 0.0)));
                toWidth = new BigDecimal((width + 0.0) / (height + 0.0)).multiply(new BigDecimal(240)).intValue();
            } else {
                toWidth = 240;
                logger.debug(new BigDecimal((height + 0.0) / (width + 0.0)));
                toHeight = new BigDecimal((height + 0.0) / (width + 0.0)).multiply(new BigDecimal(240)).intValue();
            }

            logger.debug("toWidth:" + toWidth);
            logger.debug("toHeight:" + toHeight);
            BufferedImage resizedImage = new BufferedImage(toWidth, toHeight, type);

            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, toWidth, toHeight, null);
            g.dispose();

            return resizedImage;
        } catch (IOException e) {
            logger.error(ErrorRecord.recordError(e));
        }

        return null;
    }

    private static void createVideoPreview(File file, File outFile) throws Exception {
        logger.debug("createVideoPreview");

        try {
            FileChannelWrapper ch = NIOUtils.readableFileChannel(file);
            BufferedImage frameImage = ((FrameGrab) new FrameGrab(ch).seekToFramePrecise(0)).getFrame();
            NIOUtils.closeQuietly(ch);

            ImageIO.write(frameImage, "jpg", outFile);

            return;
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));

            throw new BcsNoticeException("檔案錯誤：非MP4");
        }
    }

    /**
     * @param response
     * @param resource
     * @throws Exception
     */
    public static void getFile(HttpServletResponse response, ContentResource resource) throws Exception {
        getFile(response, resource, false);
    }

    /**
     * @param response
     * @param resource
     * @throws Exception
     */
    public static ByteArrayInputStream getFile(ContentResource resource) throws Exception {
        return getFile(resource, false);
    }

    /**
     * @param response
     * @param resource
     * @param getPreview
     * @throws Exception
     */
    public static void getFile(HttpServletResponse response, ContentResource resource, boolean getPreview) throws Exception {

        String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + resource.getResourceType();

        if (getPreview) {
            if (resource.getResourcePreview() != null) {
                filePath += System.getProperty("file.separator") + resource.getResourcePreview();
            } else {
                throw new Exception("Resource No Preview");
            }
        }

        File file = new File(filePath + System.getProperty("file.separator"), resource.getResourceId());

        if (!file.exists()) {
            // File Missing Load From DB
            ContentResourceFile resourceFile = ApplicationContextProvider.getApplicationContext().getBean(ContentResourceFileService.class).findOne(resource.getResourceId());

            if (resourceFile != null) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(resourceFile.getFileData());
                uploadFile(inputStream, resource.getResourceTitle(), resource.getResourceSize(),
                        resource.getContentType(), resource.getResourceType(), resource.getModifyUser(),
                        resource.getResourceId(), false, true);
            }
        }

        getFile(response, file, resource.getResourceTitle(), resource.getContentType());
    }

    public static Boolean loadFromDB(ContentResource resource) throws Exception {

        if (resource != null) {
            String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + resource.getResourceType();

            File file = new File(filePath + System.getProperty("file.separator"), resource.getResourceId());

            if (!file.exists()) {
                // File Missing Load From DB
                ContentResourceFile resourceFile = ApplicationContextProvider.getApplicationContext().getBean(ContentResourceFileService.class).findOne(resource.getResourceId());

                if (resourceFile != null) {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(resourceFile.getFileData());
                    uploadFile(inputStream, resource.getResourceTitle(), resource.getResourceSize(),
                            resource.getContentType(), resource.getResourceType(), resource.getModifyUser(),
                            resource.getResourceId(), false, true);
                    logger.info("loadFromDB success:" + resource);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param response
     * @param resource
     * @param getPreview
     * @throws Exception
     */
    public static ByteArrayInputStream getFile(ContentResource resource, boolean getPreview) throws Exception {

        String filePath = CoreConfigReader.getString(CONFIG_STR.FilePath) + System.getProperty("file.separator") + resource.getResourceType();

        if (getPreview) {
            if (resource.getResourcePreview() != null) {
                filePath += System.getProperty("file.separator") + resource.getResourcePreview();
            } else {
                throw new Exception("Resource No Preview");
            }
        }

        File file = new File(filePath + System.getProperty("file.separator"), resource.getResourceId());

        if (!file.exists()) {
            // File Missing Load From DB
            ContentResourceFile resourceFile = ApplicationContextProvider.getApplicationContext().getBean(ContentResourceFileService.class).findOne(resource.getResourceId());

            if (resourceFile != null) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(resourceFile.getFileData());
                return inputStream;
//		    	uploadFile(inputStream, resource.getResourceTitle(), resource.getResourceSize(),
//		    			resource.getContentType(), resource.getResourceType(), resource.getModifyUser(),
//		    			resource.getResourceId(), false);
            }
        }

        return null;
    }

    public static void getFile(HttpServletResponse response, String filePath, String resourceTitle, String contentType) throws Exception {

        File file = new File(filePath);

        getFile(response, file, resourceTitle, contentType);
    }

    public static void getFile(HttpServletResponse response, File file, String resourceTitle, String contentType) throws Exception {

        OutputStream out = null;
        FileInputStream in = null;

        String errorMsg = "";

        try {
            logger.info("[getFile]");
            in = new FileInputStream(file);
            out = response.getOutputStream();
            if (contentType != null) {
                response.setHeader("Content-Type", contentType);
            }
            response.setHeader("Content-Length", String.valueOf(file.length()));

            org.apache.commons.io.IOUtils.copy(in, out);
            response.flushBuffer();

            return;
        } catch (Exception e) {
            logger.error(ErrorRecord.recordError(e));
            errorMsg = e.getMessage();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            logger.debug("finally");
        }

        throw new Exception(errorMsg);
    }
}
