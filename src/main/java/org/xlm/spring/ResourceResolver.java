package org.xlm.spring;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xlm.spring.myresource.Resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class ResourceResolver {

    Logger logger = LoggerFactory.getLogger(getClass());

    private final String basePackage;

    public ResourceResolver(String basePackage) {
        this.basePackage = basePackage;
    }

    ClassLoader getContextClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        return cl;
    }

    public <T> List<T> scan(Function<Resource, T> mapper) throws Exception {

        String basePackagePath = this.basePackage.replace(".", "/");
        List<T> collector = Lists.newArrayList();
        scan0(basePackagePath, collector, mapper);
        return collector;
    }

    private <T> void scan0(String basePackage, List<T> collector, Function<Resource, T> mapper) throws IOException, URISyntaxException {
        logger.info("san path: {}", basePackage);
        ClassLoader contextClassLoader = getContextClassLoader();
        Enumeration<URL> resources = contextClassLoader.getResources(basePackage);

        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            URI uri = url.toURI();
            String uriStr = removeTrailingSlash(uriToString(uri));
            String uriBaseStr = uriStr.substring(0, uriStr.length() - basePackage.length());
            if (uriBaseStr.startsWith("file:")) {
                uriBaseStr = uriBaseStr.substring("file:".length());
            }
            if (uriStr.startsWith("jar:")) {
                scanFile(true, uriBaseStr, jarUriToPath(basePackage, uri), collector, mapper);
            } else {
                scanFile(false, uriBaseStr, Paths.get(uri), collector, mapper);
            }
        }
    }

    Path jarUriToPath(String basePackagePath, URI jarUri) throws IOException {
        FileSystem fileSystem = FileSystems.newFileSystem(jarUri, Maps.newHashMap());
            return fileSystem.getPath(basePackagePath);
    }

    <T> void scanFile(boolean isJar, String base, Path root, List<T> collector, Function<Resource, T> mapper) throws IOException, URISyntaxException {
        String baseDir = removeTrailingSlash(base);
        try (Stream<Path> walk = Files.walk(root)) {
            walk.filter(Files::isRegularFile).forEach(file -> {
                Resource resource;
                if (isJar) {
                    resource = new Resource(baseDir, removeLeadingSlash(file.toString()));
                } else {
                    String path = file.toString();
                    String name = removeLeadingSlash(path.substring(baseDir.length()));
                    resource = new Resource("file:" + path, name);
                }
                logger.info("found resource: {}", resource);
                T apply = mapper.apply(resource);
                if (apply != null) {
                    collector.add(apply);
                }
            });
        }
    }

    private String uriToString(URI uri) throws UnsupportedEncodingException {
        return URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8.displayName());
    }

    String removeLeadingSlash(String uri) {
        if (uri.startsWith("/") || uri.startsWith("\\")) {
            return uri.substring(1);
        }
        return uri;
    }

    String removeTrailingSlash(String uri) {
        if (uri.endsWith("/") || uri.endsWith("\\")) {
            return uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

}
