package org.example.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;

import java.io.*;

@RestController
@RequestMapping("/file")
public class FileController {
    @GetMapping("/get")
    public void getFile(HttpServletResponse response, @RequestParam String path) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        InputStream inputStream = new FileInputStream(
                path
        );
        FileCopyUtils.copy(inputStream,outputStream);
    }

    @GetMapping("/getVideo")
    public void getVideo(HttpServletResponse response, HttpServletRequest request, @RequestParam String path) throws IOException {
        File videoFile = new File(path);
        if (!videoFile.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String range = request.getHeader("Range");
        if (range == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        long fileLength = videoFile.length();
        long start = 0;
        long end = fileLength - 1;

        String[] ranges = range.split("=")[1].split("-");
        try {
            if (ranges.length > 0 && !ranges[0].isEmpty()) {
                start = Long.parseLong(ranges[0]);
            }
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        if (start > end || start >= fileLength) {
            response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }
        long contentLength = end - start + 1;
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setContentType("video/mp4");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        response.setHeader("Content-Length", String.valueOf(contentLength));

        try (RandomAccessFile raf = new RandomAccessFile(videoFile, "r")) {
            raf.seek(start);
            byte[] buffer = new byte[4096];
            long bytesToRead = contentLength;
            OutputStream os = response.getOutputStream();
            while (bytesToRead > 0) {
                int bytesRead = raf.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead));
                if (bytesRead == -1) {
                    break;
                }
                os.write(buffer, 0, bytesRead);
                bytesToRead -= bytesRead;
            }
            os.flush();
        }

    }

}
