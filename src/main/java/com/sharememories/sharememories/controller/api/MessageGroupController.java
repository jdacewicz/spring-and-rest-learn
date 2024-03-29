package com.sharememories.sharememories.controller.api;

import com.sharememories.sharememories.domain.MessageGroup;
import com.sharememories.sharememories.domain.User;
import com.sharememories.sharememories.service.MessageGroupService;
import com.sharememories.sharememories.service.SecurityUserDetailsService;
import com.sharememories.sharememories.util.UserUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/messages/groups", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class MessageGroupController {

    private final MessageGroupService groupService;
    private final SecurityUserDetailsService userDetailsService;


    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable long id) {
        Optional<MessageGroup> group = groupService.getGroup(id);
        if (group.isEmpty()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("status", HttpStatus.NOT_FOUND.value());
            map.put("message", "Message group not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        } else {
            return ResponseEntity.ok(group.get());
        }
    }

    @GetMapping("/random")
    public ResponseEntity<?> getRandomGroups() {
        Set<MessageGroup> groups = groupService.getRandomGroups();
        if (groups.isEmpty())
            return ResponseEntity.noContent().build();
        else
            return ResponseEntity.ok(groups);
    }

    @PostMapping()
    public ResponseEntity<?> createGroup(@RequestPart("name") String name,
                                         @RequestPart("members") long[] ids) {
                                         // @RequestPart(value = "image", required = false) MultipartFile file) {
        User user = UserUtils.getLoggedUser(userDetailsService);
        Set<User> members = userDetailsService.getUsers(ids);
        if (members.isEmpty()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("status", HttpStatus.NOT_FOUND.value());
            map.put("message", "Could not find users with given ids.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        } else {
            MessageGroup group = groupService.createGroup(name, user, members);
            return ResponseEntity.status(HttpStatus.CREATED).body(group);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok().build();
    }
}
