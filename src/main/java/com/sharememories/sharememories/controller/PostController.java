package com.sharememories.sharememories.controller;

import com.sharememories.sharememories.domain.Post;
import com.sharememories.sharememories.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {

    private PostService service;

    @Autowired
    public PostController(PostService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Optional<Post> get(@PathVariable Long id) {
        return service.getPost(id);
    }

    @PostMapping()
    public Post create(@RequestBody Post newPost) {
        return service.createPost(newPost);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deletePost(id);
    }
}