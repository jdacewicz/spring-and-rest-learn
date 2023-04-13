package com.sharememories.sharememories.controller;

import com.sharememories.sharememories.domain.Message;
import com.sharememories.sharememories.domain.User;
import com.sharememories.sharememories.service.MessageService;
import com.sharememories.sharememories.service.SecurityUserDetailsService;
import com.sharememories.sharememories.util.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class MessageControllerTest {

    @InjectMocks
    private MessageController controller;
    @Mock
    private MessageService messageService;
    @Mock
    private SecurityUserDetailsService detailsService;
    private static MockedStatic<FileUtils> fileUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeAll
    static void init() {
        fileUtils = Mockito.mockStatic(FileUtils.class);
    }

    @AfterAll
    static void close() {
        fileUtils.close();
    }

    @Test
    void Given_Id_When_GettingMessageByProperIdByAPI_Then_ReturnedResponseOkWithMessage() {
        //Given
        long id = 1;
        //When
        Message message = new Message();
        Mockito.when(messageService.getMessage(id)).thenReturn(Optional.of(message));

        ResponseEntity response = controller.getMessage(id);
        //Then
        assertEquals(ResponseEntity.ok(message), response);
    }

    @Test
    void Given_Id_When_GettingMessageByWrongIdByAPI_Then_ReturnedResponseNotFound() {
        //Given
        long id = 1;
        //When
        ResponseEntity response = controller.getMessage(id);
        //Then
        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), response.getStatusCode());
    }

    @Test
    void Given_ContactUserId_When_GettingAllMessagesWithUserByProperUserIdByAPI_Then_ReturnedResponseOkWithListOfMessages() {
        //Given
        long contactId = 1;
        //When
        User loggedUser = new User("user");
        User contact = new User("contact");
        Message message = new Message();
        List<Message> messageList = List.of(message);

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));
        Mockito.when(detailsService.getUserById(contactId)).thenReturn(Optional.of(contact));

        Mockito.when(messageService.getAllMessagesBySenderAndReceiver(loggedUser, contact)).thenReturn(messageList);

        ResponseEntity response = controller.getAllMessagesWithUser(contactId);
        //Then
        assertEquals(ResponseEntity.ok(messageList), response);
    }

    @Test
    void Given_ContactUserId_When_GettingAllMessagesWithUserByProperUserIdByAPI_Then_ReturnedResponseNoContentIfListIsEmpty() {
        //Given
        long contactId = 1;
        //When
        User loggedUser = new User("user");
        User contact = new User("contact");

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));
        Mockito.when(detailsService.getUserById(contactId)).thenReturn(Optional.of(contact));

        ResponseEntity response = controller.getAllMessagesWithUser(contactId);
        //Then
        assertEquals(ResponseEntity.noContent().build(), response);
    }

    @Test
    void Given_ContactUserId_When_GettingAllMessagesWithUserByWrongUserIdByAPI_Then_ReturnedResponseNotFound() {
        //Given
        long contactId = 1;
        //When
        User loggedUser = new User("user");

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));

        ResponseEntity response = controller.getAllMessagesWithUser(contactId);
        //Then
        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), response.getStatusCode());
    }

    @Test
    void Given__When_GettingMessagesNotificationsCounts_Then_ReturnedResponseOkWithMapOfCounts() {
        //Given
        //When
        User loggedUser = new User("user");
        long keyAndValue = 1;
        Map<Long, Long> notifyCounts = Map.of(keyAndValue, keyAndValue);

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));
        Mockito.when(messageService.getNotificationsCount(loggedUser)).thenReturn(notifyCounts);

        ResponseEntity response = controller.getNotificationsCount();
        //Then
        assertEquals(ResponseEntity.ok(notifyCounts), response);
    }

    @Test
    void Given__When_GettingMessagesNotificationsCounts_Then_ReturnedResponseNoContentIfMapIsEmpty() {
        //Given
        //When
        User loggedUser = new User("user");

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));

        ResponseEntity response = controller.getNotificationsCount();
        //Then
        assertEquals(ResponseEntity.noContent().build(), response);
    }

    @Test
    void Given_ContactUserIdAndContent_When_SendingMessageToUserByProperUserIdByAPI_Then_ReturnedResponseOkWithMessage() {
        //Given
        long contactId = 1;
        String content = "test";
        MockMultipartFile emptyFile = new MockMultipartFile("name", null, null, new byte[0]);
        //When
        User loggedUser = new User("user");
        User contact = new User("user2");
        Message message = new Message();

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));
        Mockito.when(detailsService.getUserById(contactId)).thenReturn(Optional.of(contact));
        Mockito.when(messageService.createMessage(any(Message.class))).thenReturn(message);

        ResponseEntity response = controller.sendMessageToUser(contactId, content, emptyFile);
        //Then
        assertEquals(ResponseEntity.ok(message), response);
    }

    @Test
    void Given_ContactUserIdAndContent_When_SendingMessageToUserByWrongUserIdByAPI_Then_ReturnedResponseNotFound() {
        //Given
        long contactId = 1;
        String content = "test";
        MockMultipartFile emptyFile = new MockMultipartFile("name", null, null, new byte[0]);
        //When
        User loggedUser = new User("user");

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));

        ResponseEntity response = controller.sendMessageToUser(contactId, content, emptyFile);
        //Then
        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), response.getStatusCode());
    }

    @Test
    void Given_ContactUserIdAndContentAndImage_When_SendingMessageByProperUserIdByAPI_Then_ReturnedResponseOkWithMessage() {
        //Given
        long contactId = 1;
        String content = "test";
        MockMultipartFile file = new MockMultipartFile("image.png", "content".getBytes());
        //When
        User loggedUser = new User("user");
        User contact = new User("user2");
        Message message = new Message();

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));
        Mockito.when(detailsService.getUserById(contactId)).thenReturn(Optional.of(contact));
        Mockito.when(messageService.createMessage(any(Message.class))).thenReturn(message);

        ResponseEntity response = controller.sendMessageToUser(contactId, content, file);
        //Then
        assertEquals(ResponseEntity.ok(message), response);
    }

    @Test
    void Given_ContactUserIdAndContentAndImage_When_ErrorWhileSendingMessageByProperUserIdByAPI_Then_ReturnedResponseInternalServerError() {
        //Given
        long contactId = 1;
        String content = "test";
        MockMultipartFile file = new MockMultipartFile("image.png", "content".getBytes());
        //When
        User loggedUser = new User("user");
        User contact = new User("user2");

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));
        Mockito.when(detailsService.getUserById(contactId)).thenReturn(Optional.of(contact));
        fileUtils.when(() -> FileUtils.generateUniqueName(file.getOriginalFilename())).thenReturn(file.getOriginalFilename());
        fileUtils.when(() -> FileUtils.saveFile(Message.IMAGES_DIRECTORY_PATH, file.getOriginalFilename(), file)).thenThrow(IOException.class);

        ResponseEntity response = controller.sendMessageToUser(contactId, content, file);
        //Then
        assertEquals(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build().getStatusCode(), response.getStatusCode());
    }

    @Test
    void Given_ContactUserId_When_SettingMessagesSeenByProperUserIdByAPI_Then_ReturnedResponseOk() {
        //Given
        long contactId = 1;
        //When
        User loggedUser = new User("user");
        User contact = new User("user2");

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));
        Mockito.when(detailsService.getUserById(contactId)).thenReturn(Optional.of(contact));

        ResponseEntity response = controller.setMessagesSeen(contactId);
        //Then
        assertEquals(ResponseEntity.ok().build(), response);
    }

    @Test
    void Given_ContactUserId_When_SettingMessagesSeenByWrongUserIdByAPI_Then_ReturnedResponseNotFound() {
        //Given
        long contactId = 1;
        //When
        User loggedUser = new User("user");

        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getName()).thenReturn(loggedUser.getUsername());
        Mockito.when(detailsService.getUserByUsername(any(String.class))).thenReturn(Optional.of(loggedUser));

        ResponseEntity response = controller.setMessagesSeen(contactId);
        //Then
        assertEquals(ResponseEntity.status(HttpStatus.NOT_FOUND).build().getStatusCode(), response.getStatusCode());
    }
}