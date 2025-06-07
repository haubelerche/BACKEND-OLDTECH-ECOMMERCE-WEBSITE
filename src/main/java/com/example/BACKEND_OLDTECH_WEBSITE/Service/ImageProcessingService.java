package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@Service
public class ImageProcessingService {

    @Value("${app.image.max-width:1920}")
    private int maxWidth;

    @Value("${app.image.max-height:1080}")
    private int maxHeight;

    @Value("${app.image.thumbnail-size:300}")
    private int thumbnailSize;

    @Value("${app.image.quality:0.85}")
    private float quality;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Process and save high-resolution image with automatic resizing and optimization
     */
    public ProcessedImage processAndSaveImage(MultipartFile file, String subDirectory, boolean createThumbnail) throws IOException {
        validateImageFile(file);

        // Create directories
        Path uploadPath = Paths.get(uploadDir, subDirectory);
        Path thumbnailPath = createThumbnail ? Paths.get(uploadDir, subDirectory, "thumbnails") : null;
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        if (thumbnailPath != null && !Files.exists(thumbnailPath)) {
            Files.createDirectories(thumbnailPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String baseFilename = UUID.randomUUID().toString();
        String filename = baseFilename + fileExtension;

        // Read original image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Cannot read image file: " + originalFilename);
        }

        // Process main image (resize if needed while maintaining quality)
        BufferedImage processedImage = resizeImageHighQuality(originalImage, maxWidth, maxHeight);
        
        // Save main image with high quality
        Path mainImagePath = uploadPath.resolve(filename);
        saveImageWithQuality(processedImage, mainImagePath.toFile(), getImageFormat(fileExtension), quality);

        ProcessedImage result = new ProcessedImage();
        result.setMainImagePath(mainImagePath.toString());
        result.setFilename(filename);
        result.setOriginalWidth(originalImage.getWidth());
        result.setOriginalHeight(originalImage.getHeight());
        result.setProcessedWidth(processedImage.getWidth());
        result.setProcessedHeight(processedImage.getHeight());        // Create thumbnail if requested
        if (createThumbnail && thumbnailPath != null) {
            BufferedImage thumbnail = resizeImageHighQuality(originalImage, thumbnailSize, thumbnailSize);
            String thumbnailFilename = "thumb_" + filename;
            Path thumbnailFilePath = thumbnailPath.resolve(thumbnailFilename);
            saveImageWithQuality(thumbnail, thumbnailFilePath.toFile(), getImageFormat(fileExtension), quality);
            result.setThumbnailPath(thumbnailFilePath.toString());
            result.setThumbnailFilename(thumbnailFilename);
        }

        return result;
    }

    /**
     * Resize image maintaining aspect ratio and high quality
     */
    private BufferedImage resizeImageHighQuality(BufferedImage originalImage, int maxWidth, int maxHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // Calculate new dimensions maintaining aspect ratio
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        // If image is already smaller than max dimensions, keep original size
        if (ratio >= 1.0) {
            return originalImage;
        }

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // Create high-quality resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();

        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);

        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * Save image with specified quality
     */
    private void saveImageWithQuality(BufferedImage image, File outputFile, String format, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (!writers.hasNext()) {
            throw new IOException("No writer found for format: " + format);
        }

        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (FileOutputStream fos = new FileOutputStream(outputFile);
             ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {
            
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    /**
     * Validate uploaded image file
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IOException("Invalid image type. Only PNG, JPEG, JPG, and WEBP are allowed");
        }

        // Validate file size (25MB max for high resolution)
        if (file.getSize() > 25 * 1024 * 1024) {
            throw new IOException("File size exceeds maximum limit of 25MB");
        }
    }

    /**
     * Check if content type is valid image type
     */
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/png") || 
               contentType.equals("image/jpeg") || 
               contentType.equals("image/jpg") ||
               contentType.equals("image/webp");
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".jpg"; // default extension
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? ".jpg" : filename.substring(dotIndex);
    }

    /**
     * Get image format from file extension
     */
    private String getImageFormat(String extension) {
        switch (extension.toLowerCase()) {
            case ".png":
                return "png";
            case ".webp":
                return "webp";
            case ".jpg":
            case ".jpeg":
            default:
                return "jpeg";
        }
    }

    /**
     * Create square thumbnail for profile pictures
     */
    public ProcessedImage createSquareThumbnail(MultipartFile file, String subDirectory, int size) throws IOException {
        validateImageFile(file);

        Path uploadPath = Paths.get(uploadDir, subDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + fileExtension;

        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IOException("Cannot read image file: " + originalFilename);
        }

        // Create square thumbnail
        BufferedImage squareImage = createSquareImage(originalImage, size);
        
        Path imagePath = uploadPath.resolve(filename);
        saveImageWithQuality(squareImage, imagePath.toFile(), getImageFormat(fileExtension), quality);

        ProcessedImage result = new ProcessedImage();
        result.setMainImagePath(imagePath.toString());
        result.setFilename(filename);
        result.setOriginalWidth(originalImage.getWidth());
        result.setOriginalHeight(originalImage.getHeight());
        result.setProcessedWidth(size);
        result.setProcessedHeight(size);

        return result;
    }

    /**
     * Create square image by cropping center and resizing
     */
    private BufferedImage createSquareImage(BufferedImage originalImage, int size) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Determine the size of the square to crop
        int cropSize = Math.min(originalWidth, originalHeight);
        
        // Calculate crop coordinates (center crop)
        int x = (originalWidth - cropSize) / 2;
        int y = (originalHeight - cropSize) / 2;
        
        // Crop to square
        BufferedImage croppedImage = originalImage.getSubimage(x, y, cropSize, cropSize);
        
        // Resize to target size
        BufferedImage squareImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = squareImage.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(croppedImage, 0, 0, size, size, null);
        g2d.dispose();
        
        return squareImage;
    }

    /**
     * Data class for processed image information
     */
    public static class ProcessedImage {
        private String mainImagePath;
        private String thumbnailPath;
        private String filename;
        private String thumbnailFilename;
        private int originalWidth;
        private int originalHeight;
        private int processedWidth;
        private int processedHeight;

        // Getters and setters
        public String getMainImagePath() { return mainImagePath; }
        public void setMainImagePath(String mainImagePath) { this.mainImagePath = mainImagePath; }
        
        public String getThumbnailPath() { return thumbnailPath; }
        public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }
        
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        
        public String getThumbnailFilename() { return thumbnailFilename; }
        public void setThumbnailFilename(String thumbnailFilename) { this.thumbnailFilename = thumbnailFilename; }
        
        public int getOriginalWidth() { return originalWidth; }
        public void setOriginalWidth(int originalWidth) { this.originalWidth = originalWidth; }
        
        public int getOriginalHeight() { return originalHeight; }
        public void setOriginalHeight(int originalHeight) { this.originalHeight = originalHeight; }
        
        public int getProcessedWidth() { return processedWidth; }
        public void setProcessedWidth(int processedWidth) { this.processedWidth = processedWidth; }
        
        public int getProcessedHeight() { return processedHeight; }
        public void setProcessedHeight(int processedHeight) { this.processedHeight = processedHeight; }
    }
}
