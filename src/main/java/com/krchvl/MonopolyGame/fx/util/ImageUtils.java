package com.krchvl.MonopolyGame.fx.util;

import com.krchvl.MonopolyGame.core.tiles.Tile;
import javafx.scene.image.Image;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;

public final class ImageUtils {

    private ImageUtils() {}

    public static String getImgSrc(Tile tile) {
        if (tile == null) {
            return null;
        }
        try {
            Method method = tile.getClass().getMethod("getImgSrc");
            Object value = method.invoke(tile);
            if (value != null) {
                return String.valueOf(value);
            }
        } catch (Exception ignored) {}

        try {
            Field field = tile.getClass().getDeclaredField("imgSrc");
            field.setAccessible(true);
            Object value = field.get(tile);
            if (value != null) {
                return String.valueOf(value);
            }
        } catch (Exception ignored) {}
        return null;
    }

    public static Image loadImage(String src, Map<String, Image> imageCache) {
        if (src == null || src.isBlank()) {
            return null;
        }

        Image cachedImage = imageCache.get(src);
        if (cachedImage != null) {
            return cachedImage;
        }

        Image img = null;
        try {
            String trimmedSrc = src.trim();
            if (trimmedSrc.startsWith("http://") || trimmedSrc.startsWith("https://")
                    || trimmedSrc.startsWith("file:") || trimmedSrc.startsWith("jar:")
                    || trimmedSrc.startsWith("data:image")) {
                img = new Image(trimmedSrc, true);
            } else {
                URL url = ImageUtils.class.getResource(trimmedSrc);
                if (url == null) {
                    url = ImageUtils.class.getResource(trimmedSrc.startsWith("/") ? trimmedSrc : "/" + trimmedSrc);
                }
                if (url == null) {
                    url = Tile.class.getResource(trimmedSrc);
                }
                if (url == null) {
                    url = Tile.class.getResource(trimmedSrc.startsWith("/") ? trimmedSrc : "/" + trimmedSrc);
                }
                if (url == null) {
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    url = contextClassLoader.getResource(trimmedSrc.startsWith("/") ? trimmedSrc.substring(1) : trimmedSrc);
                }

                if (url != null) {
                    img = new Image(url.toExternalForm(), true);
                } else {
                    try {
                        img = new Image(new File(trimmedSrc).toURI().toString(), true);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        if (img != null && !img.isError()) {
            imageCache.put(src, img);
        }
        return img;
    }
}