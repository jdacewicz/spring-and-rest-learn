package com.sharememories.sharememories.service;

import com.sharememories.sharememories.domain.Comment;
import com.sharememories.sharememories.domain.Post;
import com.sharememories.sharememories.domain.Reaction;
import com.sharememories.sharememories.repository.CommentRepository;
import com.sharememories.sharememories.repository.PostRepository;
import com.sharememories.sharememories.repository.ReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ReactionRepository reactionRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, PostRepository postRepository, ReactionRepository reactionRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.reactionRepository = reactionRepository;
    }

    public Optional<Comment> getComment(Long id) {
        return commentRepository.findById(id);
    }

    public Optional<String> getCommentImageName(long id) {
        return commentRepository.findById(id)
                .map(Comment::getImage);
    }

    public void deletePostComment(long postId, long commentId) {
        postRepository.findById(postId).map(post -> {
            Optional<Comment> comment = commentRepository.findById(commentId);
            comment.ifPresent(value -> post.getComments().remove(value));
            return postRepository.save(post);
        });
        commentRepository.deleteById(commentId);
    }

    public Optional<Comment> reactToComment(int reactionId, long commentId) {
        return commentRepository.findById(commentId).map(comment -> {
            Optional<Reaction> reaction = reactionRepository.findById(reactionId);
            if (reaction.isPresent()) {
                comment.addReaction(reaction.get());
                return commentRepository.save(comment);
            }
            return null;
        });
    }

    public Optional<Comment> commentPost(long postId, Comment comment) {
        Optional<Post> post = postRepository.findById(postId);
        if(post.isPresent()) {
            comment.setPost(post.get());
            return Optional.of(commentRepository.save(comment));
        }
        return Optional.empty();
    }
}
