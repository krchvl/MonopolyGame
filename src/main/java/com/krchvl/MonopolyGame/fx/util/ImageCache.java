package com.krchvl.MonopolyGame.fx.util;

import javafx.scene.image.Image;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ImageCache {
    private final Map<String, Image> cache = new ConcurrentHashMap<>();

    public Image get(String src) {
        if (src == null || src.isBlank()) return null;
        return ImageUtils.loadImage(src, cache);
    }
}