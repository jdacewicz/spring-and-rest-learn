package com.sharememories.sharememories.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sharememories.sharememories.util.TimeUtils;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@Getter @Setter
public class Post {

    @Transient
    public static final String IMAGES_DIRECTORY_PATH = "uploads/pictures/posts";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "post_Id")
    private long id;

    @NotBlank
    @Size(max = 255)
    private String content;

    private String image;
    private LocalTime creationTime = LocalTime.now();
    private LocalDate creationDate = LocalDate.now();

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User creator;

    @ManyToMany
    @JsonIgnore
    @JoinTable(
            name = "reactions_posts",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "reaction_id")
    )

    @OrderBy("id ASC")
    private List<Reaction> reactions = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "group_id")
    private PostGroup group;

    public Post(List<Reaction> reactions) {
        this.reactions = reactions;
    }

    public Post(String content, User creator) {
        this.content = content;
        this.creator = creator;
    }

    public Post(String content, String image, User creator) {
        this.content = content;
        this.image = image;
        this.creator = creator;
    }

    public void addReaction(Reaction reaction) {
        this.reactions.add(reaction);
        reaction.getPosts().add(this);
    }

//    public void removeReaction(Reaction reaction) {
//        this.reactions.remove(reaction);
//        reaction.getPosts().remove(this);
//    }

    public Map<Integer, Long> getReactionsCounts() {
        return reactions.stream()
                .collect(Collectors.groupingBy(Reaction::getId, Collectors.counting()));
    }

    @SuppressWarnings("unused")
    public String getElapsedCreationTimeMessage() {
        LocalDateTime creationDateTime = LocalDateTime.of(creationDate, creationTime);

        return TimeUtils.getElapsedTimeMessage(creationDateTime, LocalDateTime.now());
    }

    @SuppressWarnings("unused")
    public String getImagePath() {
        if (image == null) return null;

        return "/" + IMAGES_DIRECTORY_PATH + "/" + image;
    }
}
