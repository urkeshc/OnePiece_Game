package mvc.controller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/*
Place all .png image assets in this directory src/main/resources/imgs or its subdirectories.
All raster images loaded in static context prior to runtime.
 */
public class ImageLoader {

    private static Map<String, BufferedImage> IMAGE_MAP = null;

    static {
        try {
            // Resolve root directory relative to the classpath
            Path rootDirectory = Paths.get(ImageLoader.class.getResource("/imgs").toURI());
            IMAGE_MAP = loadPngImages(rootDirectory);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            IMAGE_MAP = new HashMap<>(); // Initialize an empty map in case of an error
        }
    }

    /*
     Walks the directory and sub-directories at root src/main/resources/imgs and returns a Map<String, BufferedImage>
     of images in that file hierarchy.
     */
    private static Map<String, BufferedImage> loadPngImages(Path rootDirectory) throws IOException {
        Map<String, BufferedImage> images = new HashMap<>();
        Files.walkFileTree(rootDirectory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String fileName = file.toString().toLowerCase();
                if (fileName.endsWith(".png")
                        && !fileName.contains("do_not_load.png")) {
                    System.out.println("Discovered PNG image: " + fileName);
                    try {
                        BufferedImage image = ImageIO.read(file.toFile());
                        if (image != null) {
                            String key = rootDirectory.relativize(file).toString().replace("\\", "/");
                            System.out.println("⇒ Storing image with key: " + key);
                            images.put(key, image);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return images;
    }

    // Fetch the image from existing static map
    public static BufferedImage getImage(String imagePath) {
        if (IMAGE_MAP == null) {
            throw new IllegalStateException("Images not loaded properly.");
        }
        String lookupKey = imagePath.toLowerCase();
        System.out.println("⇒ Asking for image with key: " + lookupKey);
        return IMAGE_MAP.get(lookupKey);
    }
}
