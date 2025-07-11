package org.example.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.ResourceResolver;
import org.springframework.web.servlet.resource.ResourceResolverChain;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedHeaders("*").allowedOrigins("*").allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH");
    }

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//
//        registry.addResourceHandler("/uploads/**")
//                .addResourceLocations("file:uploads/");
//
//        registry.addResourceHandler("/**")
//                .addResourceLocations("classpath:/static/")
//                //                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
//                .resourceChain(false)
//                .addResolver(new PushStateResourceResolver());
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // frontend uchun statik resurslar
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(false)
                .addResolver(new PushStateResourceResolver());

        // uploads papkani ochish
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }

    private static class PushStateResourceResolver implements ResourceResolver {
        private final Resource index = new ClassPathResource("/static/index.html");
        private final List<String> handledExtensions = Arrays.asList("html", "js", "json", "csv", "css", "png", "svg", "eot", "ttf", "otf", "woff", "appcache", "jpg", "jpeg", "gif", "ico");
        private final List<String> ignoredPaths = List.of("api");

        @Override
        public Resource resolveResource(HttpServletRequest request, @NotNull String requestPath, @NotNull List<? extends Resource> locations, @NotNull ResourceResolverChain chain) {
            return resolve(requestPath, locations);
        }

        @Override
        public String resolveUrlPath(@NotNull String resourcePath, @NotNull List<? extends Resource> locations, @NotNull ResourceResolverChain chain) {
            Resource resolvedResource = resolve(resourcePath, locations);
            if (resolvedResource == null) {
                return null;
            }
            try {
                return resolvedResource.getURL().toString();
            } catch (IOException e) {
                return resolvedResource.getFilename();
            }
        }

        private Resource resolve(String requestPath, List<? extends Resource> locations) {
            if (isIgnored(requestPath)) {
                return null;
            }
            if (isHandled(requestPath)) {
                return locations.stream()
                        .map(loc -> createRelative(loc, requestPath))
                        .filter(resource -> resource != null && resource.exists())
                        .findFirst()
                        .orElseGet(null);
            }
            return index;
        }

        private Resource createRelative(Resource resource, String relativePath) {
            try {
                return resource.createRelative(relativePath);
            } catch (IOException e) {
                return null;
            }
        }

        private boolean isIgnored(String path) {
            return ignoredPaths.contains(path);
        }

        private boolean isHandled(String path) {
            String extension = StringUtils.getFilenameExtension(path);
            return handledExtensions.stream().anyMatch(ext -> ext.equals(extension));
        }
    }

}