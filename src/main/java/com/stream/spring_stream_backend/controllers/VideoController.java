package com.stream.spring_stream_backend.controllers;

import com.stream.spring_stream_backend.AppConstants;
import com.stream.spring_stream_backend.entiities.Video;
import com.stream.spring_stream_backend.payload.CustomMessage;
import com.stream.spring_stream_backend.services.VideoService;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin(origins = "http://localhost:5173")
public class VideoController {

    @Autowired
    private VideoService videoService;


    //video uploader
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) {

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoId(UUID.randomUUID().toString());

        Video savedVideo = videoService.save(video, file);

        if (savedVideo != null) {
            return ResponseEntity.status(HttpStatus.OK).body(video);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CustomMessage.builder()
                            .message("Video Not uploaded")
                            .success(false).build());
        }


    }


    @GetMapping
    public List<Video> getAll() {

        return videoService.getAll();
    }

    @GetMapping("/stream/{videoId}")
    public ResponseEntity<Resource> stream(@PathVariable String videoId) {
        Video video = videoService.get(videoId);
        String contentType = video.getContentType();
        String path = video.getPath();

        Resource resource = new FileSystemResource(path);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }


        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
    }

    //stream vid in chucnks
    @GetMapping("/stream/range{videoId}")
    public ResponseEntity<Resource> streamVideoRange(@PathVariable
                                                             String videoId,
                                                     @RequestHeader(value = "Range", required = false) String range) {
        System.out.println(range);
        Video video = videoService.get(videoId);
        Path path = Paths.get(video.getPath());
        Resource resource = new FileSystemResource(path);
        String contentType = video.getContentType();

        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        //file length
        long fileLength = path.toFile().length();
        if (range == null) {
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);

        }
        long rangeStart;
        long rangeEnd;

        String[] split = range.replace("bytes", "").split("-");
        rangeStart = Long.parseLong(split[0]);

        rangeEnd = rangeStart + AppConstants.CHUNK_SIZE - 1;

//        if (split.length > 1) {
//            rangeEnd = Long.parseLong(split[1]);
//        }
//        else {
//            rangeEnd = fileLength - 1;
//        }


        if (rangeEnd > fileLength - 1) {
            rangeEnd =  fileLength - 1;
        }

        InputStream inputStream;
        try {
            inputStream = Files.newInputStream(path);
            inputStream.skip(rangeStart);

            long contentLength = rangeEnd - rangeStart + 1;

            byte[] data = new byte[(int)contentLength];
            inputStream.read(data, 0, data.length);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-range", "bytes "+rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");
            headers.add("X-Content-Type-Options", "nosniff");
            headers.setContentLength(contentLength);
            Resource inputResource = new InputStreamResource(inputStream);


            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(inputResource);
        }
        catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }




    }

    //server hls playlist
    //master.m2u8 file

    @Value("${file.videos.hsl}")
    private String HLS_DIR;

    @GetMapping("/{videoId}/master.m3eu8")
    public ResponseEntity<Resource> serverMasterFile(
            @PathVariable String videoId) {
        //creating path
        Path path = Paths.get(HLS_DIR, videoId, "master.m3u8");
        System.out.println(path);
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(path);

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "application/vid.apple.mpegurl"
                ).body(resource);
    }

    //serve the segments

    @GetMapping("/{videoId}/{segment}.ts")
    public ResponseEntity<Resource> serveSegments(
        @PathVariable String videoId,
        @PathVariable String segment
    ) {
        //creating path for segments
        Path path = Paths.get(HLS_DIR, videoId, segment + ".ts");
        if (!Files.exists(path)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_TYPE, "video/mp2t")
                .body(resource);
    }

}
