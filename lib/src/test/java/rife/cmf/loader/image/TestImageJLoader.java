/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.cmf.loader.image;

import org.junit.jupiter.api.Test;
import rife.resources.ResourceFinderClasspath;
import rife.tools.FileUtils;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TestImageJLoader {
    @Test
    public void testIsBackendPresent() {
        var loader = new ImageJLoader();
        assertTrue(loader.isBackendPresent());
    }

    @Test
    public void testLoadSuccess()
    throws Exception {
        var loader = new ImageJLoader();
        Set<String> errors = new HashSet<>();
        var image_resource = ResourceFinderClasspath.instance().getResource("uwyn.tif");

        var image_bytes = FileUtils.readBytes(image_resource);
        var image = loader.load(image_bytes, false, errors);

        assertNotNull(image);
        assertEquals(0, errors.size());
    }

    @Test
    public void testLoadUnsupportedType() {
        var loader = new ImageJLoader();
        Set<String> errors = new HashSet<>();

        var image = loader.load(new Object(), false, errors);

        assertNull(image);
        assertEquals(0, errors.size());
    }

    @Test
    public void testLoadFromBytesSuccess()
    throws Exception {
        var loader = new ImageJLoader();
        Set<String> errors = new HashSet<>();
        var image_resource = ResourceFinderClasspath.instance().getResource("uwyn.tif");

        var image_bytes = FileUtils.readBytes(image_resource);
        var image = loader.loadFromBytes(image_bytes, errors);

        assertNotNull(image);
        assertEquals(0, errors.size());
    }

    @Test
    public void testLoadFromBytesError() {
        var loader = new ImageJLoader();
        Set<String> errors = new HashSet<>();

        var image_bytes = new byte[]{2, 9, 7, 12, 45}; // just random values
        var image = loader.loadFromBytes(image_bytes, errors);

        assertNull(image);
    }

    @Test
    public void testLoadFromBytesErrorNoList() {
        var loader = new ImageJLoader();

        var image_bytes = new byte[]{2, 9, 7, 12, 45}; // just random values
        var image = loader.loadFromBytes(image_bytes, null);

        assertNull(image);
    }
}
