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

    @Override
    public Video get(String id) {
        return null;
    }

    @PostConstruct
    public void init() {
        File file = new File(DIR);
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

            videoRepository.save(video);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return video;
    }

    @Override
    public Video getByTitle(String title) {
        return null;
    }

    @Override
    public List<Video> getAll() {
        return null;
    }
}
