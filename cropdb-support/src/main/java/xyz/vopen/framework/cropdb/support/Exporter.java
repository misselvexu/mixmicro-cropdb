/*
 * Copyright (c) 2021-2022. CropDB author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.vopen.framework.cropdb.support;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.exceptions.CropIOException;

import java.io.*;


/**
 * Crop database export utility. It exports data to
 * a json file. Contents of a Crop database can be exported
 * using this tool.
 * <p>
 * [[app-listing]]
 * include::/src/docs/asciidoc/tools/data-format.adoc[]
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @since 1.0
 */
public class Exporter {
    private CropDB db;
    private JsonFactory jsonFactory;
    private ExportOptions options;

    private Exporter() {
    }

    /**
     * Creates a new {@link Exporter} instance.
     *
     * @param db the db
     * @return the exporter instance
     */
    public static Exporter of(CropDB db) {
        return of(db, createObjectMapper());
    }

    public static Exporter of(CropDB db, ObjectMapper objectMapper) {
        Exporter exporter = new Exporter();
        exporter.db = db;
        exporter.jsonFactory = objectMapper.getFactory();
        exporter.options = new ExportOptions();
        return exporter;
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(
            objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    /**
     * Sets {@link ExportOptions} to customize data export.
     *
     * @param options the options
     * @return the exporter
     */
    public Exporter withOptions(ExportOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Exports data to a file.
     *
     * @param file the file
     */
    public void exportTo(String file) {
        exportTo(new File(file));
    }

    /**
     * Exports data to a {@link File}.
     *
     * @param file the file
     * @throws CropIOException if there is any low-level I/O error.
     */
    public void exportTo(File file) {
        try {
            if (file.isDirectory()) {
                throw new IOException(file.getPath() + " is not a file");
            }

            File parent = file.getParentFile();
            // if parent dir does not exists, try to create it
            if (!parent.exists()) {
                boolean result = parent.mkdirs();
                if (!result) {
                    throw new IOException("Failed to create parent directory " + parent.getPath());
                }
            }
            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                exportTo(outputStream);
            }
        } catch (IOException ioe) {
            throw new CropIOException("I/O error while writing content to file " + file, ioe);
        }
    }

    /**
     * Exports data to an {@link OutputStream}.
     *
     * @param stream the stream
     */
    public void exportTo(OutputStream stream) throws IOException {
        try(OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            exportTo(writer);
        }
    }

    /**
     * Exports data to a {@link Writer}.
     *
     * @param writer the writer
     * @throws CropIOException if there is any error while writing the data.
     */
    public void exportTo(Writer writer) {
        JsonGenerator generator;
        try {
            generator = jsonFactory.createGenerator(writer);
            generator.setPrettyPrinter(new DefaultPrettyPrinter());
        } catch (IOException ioe) {
            throw new CropIOException("I/O error while writing data with writer", ioe);
        }

        CropJsonExporter jsonExporter = new CropJsonExporter(db);
        jsonExporter.setGenerator(generator);
        jsonExporter.setOptions(options);
        try {
            jsonExporter.exportData();
        } catch (IOException | ClassNotFoundException e) {
            throw new CropIOException("error while exporting data", e);
        }
    }
}
