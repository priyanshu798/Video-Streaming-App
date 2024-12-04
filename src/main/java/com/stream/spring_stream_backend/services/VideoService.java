package com.stream.spring_stream_backend.services;

import com.stream.spring_stream_backend.entiities.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {

    Video get(String id);

    Video save(Video video, MultipartFile file);

    Video getByTitle(String title);

    List<Video> getAll();

}
