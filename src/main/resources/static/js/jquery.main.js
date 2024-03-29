$(document).ready(function () {
    let mainContent = $("#main-content");
    let adminPanel = $("#admin-panel");
    let panels = $("#panels");
    let adminReactionsList = $("#admin-reactions-list");

    loadUnreadMessagesCount();

    $("#show-contact-form").click(function () {
        if (mainContent.is(":visible")) {
            mainContent.fadeOut("fast", function () {
                adminPanel.hide();
                $("#contact-panel").show();
                panels.fadeIn("fast");
            });
        } else {
            panels.fadeOut("fast", function () {
                adminPanel.hide();
                $("#contact-panel").show();
                panels.fadeIn("fast");
            });
        }
    });

    $("#show-admin-users-panel").click(function () {
        if (mainContent.is(":visible")) {
            mainContent.fadeOut("fast", function () {
                $("#contact-panel").hide();
                adminPanel.show();
                $("#admin-users-panel").show();
                panels.fadeIn("fast");
            });
        } else {
            panels.fadeOut("fast", function () {
                $("#contact-panel").hide();
                adminPanel.show();
                adminPanel.children().hide();
                $("#admin-users-panel").fadeIn("fast");
                panels.fadeIn("fast");
            });
        }
    });

    $("#show-admin-reactions-panel").click(function () {
        if (mainContent.is(":visible")) {
            $("#admin-reactions-list tbody").empty();
            loadAdminPanelReactions();

            mainContent.fadeOut("fast", function () {
                $("#contact-panel").hide();
                adminPanel.show();
                $("#admin-reactions-panel").show();
                panels.fadeIn("fast");
            });
        } else {
            $("#admin-reactions-list tbody").empty();
            loadAdminPanelReactions();

            panels.fadeOut("fast", function () {
                $("#contact-panel").hide();
                adminPanel.show();
                adminPanel.children().hide();
                $("#admin-reactions-panel").fadeIn("fast");
                panels.fadeIn("fast");
            });
        }
    });

    $("#admin-user-search").submit(function (e) {
        e.preventDefault();
        let userId = $("#admin-user-search input").val();

        $("#admin-user-search-results").fadeIn("fast");
        loadUser(userId);
    });

    $("#admin-user-search-results").on("click", ".delete-user", function () {
        if (confirm('Are you sure you want to delete this user?')) {
            let userId = getIdFromSquareBracket($(this).closest("tr[class^='user[']").attr("class"));

            deleteObject("/api/users/" + userId);
            $("#admin-user-search-results tr[class^='user[" + userId + "]']").remove();
        }
    });

    $("#admin-search-results-clear").click(function () {
        $("#admin-user-search-results table tbody").empty();
    });

    adminReactionsList.on("click", ".delete-reaction", function () {
        if (confirm('Are you sure you want to delete this reaction?')) {
            let reactionId = getIdFromSquareBracket($(this).closest("tr[class^='reaction[']").attr("class"));

            deleteObject("/api/reactions/" + reactionId);
            $("#admin-reactions-list tr[class^='reaction[" + reactionId + "]']").remove();
        }
    });

    adminReactionsList.on("click", ".edit-reaction", function () {
        let reaction = $(this).closest("tr[class^='reaction[']");

        $("#edit-reaction-form").fadeOut("fast", function () {
            $("#edit-reaction-id").val(getIdFromSquareBracket(reaction.attr("class")));
            $("#edit-reaction-name").val(reaction.find(".reaction-name").text());
            $("#edit-reaction-image-preview").attr("src", reaction.find("img[class^='reaction-image']").attr("src"));

            $("#edit-reaction-form").fadeIn("fast");
        });
    });

    $("#edit-reaction-form").submit(function (e) {
        e.preventDefault();

        let data = new FormData($(this)[0])
        let reactionId = $("#edit-reaction-id").val();

        updateReaction("/api/reactions/" + reactionId, data);

        let reaction = $("#admin-reactions-list tr[class^='reaction[" + reactionId + "]']");
        reaction.find("img[class^='reaction-image']").attr("src", $("#edit-reaction-image-preview").attr("src"));
        reaction.find(".reaction-name").text($("#edit-reaction-name").val());

        $(this).fadeOut("fast");
    });

    $("#edit-reaction-file").click(function (){
        $(this).on("change", function () {
            let file = $(this).get(0).files[0];
            let reader = new FileReader();

            reader.onload = function () {
                $("#edit-reaction-image-preview").attr("src", reader.result).show();
            }
            reader.readAsDataURL(file);
        })
    });

    $("#full-screen-notifications-box").click(function () {
        $(this).fadeOut("fast");
    });

    $(".close-chat-box").click(function () {
        $("#chat-box").hide();
    });

    $("#right-panels").on("click", "button[name^='contact[']" ,function () {
        $("#chat-box-messages").empty();

        let userId = getIdFromSquareBracket($(this).attr("name"));

        $("#chat-box-sender").attr("href", "/profile/" + userId);
        $("#chat-box-sender img").attr("src", $(this).children("img").attr("src"));
        $("#chat-box-sender span").text($(this).children(".name").text());
        $("#send-message-form").children("input[name='id']").val(userId);

        loadMessagesWithUser(userId);
        setMessagesSeen(userId);
        let notification = $(this).children("span[class^='notification']");

        if ($(this).parent("#unknown-contacts-list").length) {
            let unknownNotifications = $("#contacts button[name='unknown-contacts'] span[class^='notification']");
            let subtract = parseInt(unknownNotifications.text()) - parseInt(notification.text());

            if (subtract === 0) {
                unknownNotifications.parent().remove();
            } else {
                unknownNotifications.text(subtract);
            }
        }
        notification.remove();

        $("#chat-box").show();
    });

    $("#contacts").on("click", "button[name='unknown-contacts']", function () {
        $("#unknown-contacts-list").empty();

        loadUnknownContacts();

        $("#unknown-contacts").show();
    });

    $("#send-message-form").submit(function (e) {
        e.preventDefault();

        let userId = $(this).children("input[name='id']").val();
        let data = new FormData($(this)[0])

        sendMessage(userId, data);
        $(this).find("input[type='file'], textarea").val('');
        $(this).find("img[class^='image-preview']").attr("src", '').hide();
    });

    $("#close-unknown-contacts-box").click(function () {
       $("#unknown-contacts").hide();
    });
})

function sendMessage(userId, data) {
    $.ajax({
        enctype : 'multipart/form-data',
        url: "/api/messages/user/" + userId,
        type: "POST",
        data : data,
        dataType: "JSON",
        processData : false,
        contentType : false,
        success: function (message) {
            appendMessageToChatBox(message, true);
        }
    });
}

function updateReaction(url, data) {
    $.ajax({
        enctype : 'multipart/form-data',
        url: url,
        type: "PUT",
        data : data,
        dataType: "JSON",
        processData : false,
        contentType : false
    });
}

function setMessagesSeen(userId) {
    $.ajax({
        url: "/api/messages/user/" + userId + "/mark-seen",
        type: "PUT",
        dataType: "JSON",
        processData : false,
        contentType : false
    });
}

function loadUser(id) {
    $.ajax({
        enctype : 'multipart/form-data',
        url: "/api/users/" + id,
        type: "GET",
        dataType: "JSON",
        processData : false,
        contentType : false,
        success: function (user) {
            appendUserToAdminPanel(user);
        }
    });
}

function loadAdminPanelReactions() {
    $.ajax({
        enctype : 'multipart/form-data',
        url: "/api/reactions",
        type: "GET",
        dataType: "JSON",
        processData : false,
        contentType : false,
        success: function (reactions) {
            reactions.forEach(function (reaction) {
                appendReactionToAdminPanel(reaction);
            });
        }
    });
}

function loadMessagesWithUser(userId) {
    $.ajax({
        type: "GET",
        url: "/api/messages/user/" + userId,
        dataType: "JSON",
        success: function (data) {
            if (data == null) {
                return;
            }
            data.forEach(function (message) {
                if (message.sender.id === parseInt(userId)) {
                    appendMessageToChatBox(message, false);
                } else {
                    appendMessageToChatBox(message, true)
                }
            });
        }
    });
}

function loadUnreadMessagesCount() {
    $.ajax({
        type: "GET",
        url: "/api/messages/notify",
        dataType: "JSON",
        success: function (data) {
            for (let key in data) {
                let notification = '<span class="notification px-2 ml-2 bg-red-500 text-white rounded-xl">' + data[key] + '</span>';

                if (parseInt(key) !== -1) {
                    let contact = $("button[name='contact[" + key + "]']");

                    contact.append(notification);
                    contact.prependTo("#contacts");
                } else if (parseInt(key) === -1 && data[key] > 0) {
                    $("#contacts").prepend(
                        '<button type="button" name="unknown-contacts" class="w-full flex justify-start items-center p-2 text-sm font-medium hover:bg-gray-100">' +
                            '<span class="name">Unknown contacts</span>' +
                            notification +
                        '</button>'
                    );
                }
            }
        }
    });
}

function loadUnknownContacts() {
    $.ajax({
        type: "GET",
        url: "/api/users/contacts/unknown",
        dataType: "JSON",
        success: function (contacts) {
            if (contacts == null) {
                return;
            }

            $.ajax({
                type: "GET",
                url: "/api/messages/notify/unknown",
                dataType: "JSON",
                success: function (notifies) {
                    if (notifies == null) {
                        return;
                    }

                    contacts.forEach(function (contact) {
                        appendUnknownContact(contact, notifies[contact.id]);
                    });
                }
            });
        }
    });
}

function deleteObject(url) {
    $.ajax({
        enctype : 'multipart/form-data',
        url: url,
        type: "DELETE",
        dataType: "JSON",
        processData : false,
        contentType : false
    });
}

function getIdFromSquareBracket(str) {
    return str.substring(str.indexOf('[') + 1, str.indexOf(']'));
}

function appendUserToAdminPanel(user) {
    if ($("#admin-user-search-results tr[class^='user[" + user.id + "]']").length > 0) {
        return;
    }

    $("#admin-user-search-results tbody").append(
        '<tr class="user[' + user.id +'] border-b">' +
            '<th scope="row" class="px-4 py-2">' +
                '<a href="/profile/' + user.id + '" class="flex justify-center items-center font-medium hover:underline">' +
                    '<img src="' + user.imagePath + '" class="w-6 rounded-lg mr-2" alt="user profile picture">' +
                    '<span>' + user.capitalizedFirstAndLastName + '</span>' +
                '</a>' +
            '</th>' +
            '<td class="px-4 py-2">' +
                '<div class="flex justify-center items-center">' +
                    '<button class="delete-user" type="button">' +
                        '<img src="/images/icons/close-icon.svg" class="w-8" alt="delete icon">' +
                    '</button>' +
                '</div>' +
            '</td>' +
        '</tr>'
    );
}

function appendReactionToAdminPanel(reaction) {
    $("#admin-reactions-list tbody").append(
        '<tr class="reaction[' + reaction.id +'] border-b">' +
            '<td class="px-4 py-3">' +
                '<div class="flex justify-center items-center">' +
                    '<img src="' + reaction.imagePath + '" class="reaction-image w-10" alt="reaction image">' +
                '</div>' +
            '</td>' +
            '<td class="px-4 py-3">' +
                '<div class="flex justify-center items-center text-sm">' +
                    '<span class="reaction-name">' + reaction.name + '</span>' +
                '</div>' +
            '</td>' +
            '<td class="px-4 py-2">' +
                '<div class="flex justify-center items-center">' +
                    '<button class="delete-reaction" type="button">' +
                        '<img src="/images/icons/close-icon.svg" class="w-8" alt="delete icon">' +
                    '</button>' +
                    '<button class="edit-reaction" type="button">' +
                        '<img src="/images/icons/edit-icon.svg" class="w-8" alt="edit icon">' +
                    '</button>' +
                '</div>' +
            '</td>' +
        '</tr>'
    );
}

function appendMessageToChatBox(message, moveToRight) {
    let messageImage = (message.imagePath == null) ? "" :
        '<img src="' + message.imagePath + '" class="w-28 pt-2" alt="message image">';

    let sender = '<img src="' + message.sender.imagePath + '" class="w-8 rounded-xl mx-2 border" alt="sender profile picture">';
    let content = '<div class="border bg-pink-500 rounded-xl p-2 shadow">' +
                    '<span class="block">' + message.content + '</span>' +
                    messageImage +
                  '</div>';

    if (moveToRight) {
        $("#chat-box-messages").append(
            '<div class="block flex items-center mb-2 justify-end">' +
                content +
                sender +
            '</div>'
        );
    } else {
        $("#chat-box-messages").append(
            '<div class="block flex items-center mb-2 justify-start">' +
                sender +
                content +
            '</div>'
        );
    }
}

function appendUnknownContact(user, notifyCount) {
    $("#unknown-contacts-list").append(
        '<button type="button" name="contact[' + user.id +']" class="w-full flex justify-start items-center p-2 text-sm font-medium hover:bg-gray-100">' +
            '<img src="' + user.imagePath + '" class="w-8 rounded-xl mx-2 border" alt="unknown contact profile picture">' +
            '<span class="name">' + user.capitalizedFirstAndLastName + '</span>' +
            '<span class="notification px-2 ml-2 bg-red-500 text-white rounded-xl">' + notifyCount + '</span>' +
        '</button>'
    );
}