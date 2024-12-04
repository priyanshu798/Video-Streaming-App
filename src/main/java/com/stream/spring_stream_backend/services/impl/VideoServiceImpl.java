package com.stream.spring_stream_backend.services.impl;

import com.stream.spring_stream_backend.entiities.Video;
import com.stream.spring_stream_backend.services.VideoService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class VideoServiceImpl implements VideoService {
    @Override
    public Video get(String id) {
        return null;
    }

    @Override
    public Video save(Video video, MultipartFile file) {
        return null;
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
