package com.stream.spring_stream_backend.services.impl;

import com.stream.spring_stream_backend.entiities.Video;
import com.stream.spring_stream_backend.repositories.VideoRepository;
import com.stream.spring_stream_backend.services.VideoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoRepository videoRepository;

    @Value("${files.video}")
    String DIR;

    @Value("${file.videos.hsl}")
    String HSL_DIR;

    @Override
    public Video get(String id) {
        Video video = videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
        return video;
    }

    @PostConstruct
    public void init() {
        File file = new File(DIR);

        File file1 = new File(HSL_DIR);
        if (!file1.exists()) {
            file1.mkdir();
        }

        if (!file.exists()) {
            file.mkdir();
            System.out.println("Folder created");
        } else {
            System.out.println("Alrady folder is created");
        }
    }

    @Override
    public Video save(Video video, MultipartFile file) {

        try {
            String fileName = file.getOriginalFilename();
            String contentType = file.getContentType();
            InputStream inputStream = file.getInputStream();

            //folder path
            String cleanFilePAth = StringUtils.cleanPath(DIR);

            //file name
            String cleanFileName = StringUtils.cleanPath(fileName);

            //folder path with file name
            Path path = Paths.get(cleanFilePAth, cleanFileName);

            System.out.println(contentType);
            System.out.println(path);

            //copying video to folder path
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            video.setContentType(contentType);
            video.setPath(path.toString());


            Video savedVideo = videoRepository.save(video);
            //processing video
            processVideo(savedVideo.getVideoId());



            return savedVideo;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error in processing video ");
        }

    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return videoRepository.findAll();
    }

    @Override
    public String processVideo(String videoId) {

        Video video = this.get(videoId);
        String filePath = video.getPath();

        //path where to store data
        Path videoPath = Paths.get(filePath);


        try {
            Path outputPath = Paths.get(HSL_DIR, videoId);
            Files.createDirectories(outputPath);
            String ffmpegCmd = String.format(
                    "ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\"  \"%s/master.m3u8\" ",
                    videoPath, outputPath, outputPath
            );

            System.out.println(ffmpegCmd);

            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", ffmpegCmd);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("video processing failed!!");
            }

            return videoId;

        }
        catch (IOException e) {
            throw new RuntimeException("Video processing fail");
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}
