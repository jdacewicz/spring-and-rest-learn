$(function () {
    $("#create-post-form").submit(function (e) {
        e.preventDefault();
        let frm = $('#create-post-form');
        let data = new FormData($('#create-post-form')[0]);

        saveMultipartRequestedData(frm, data)
    });

    $("#create-reaction-form").submit(function (e) {
        e.preventDefault();
        var frm = $("#create-reaction-form");
        var data = {};
        $.each(this, function (i, v) {
            var input = $(v);
            data[input.attr("name")] = input.val();
            delete data["undefined"];
        });
        saveRequestedData(frm, data);
    });
});

function saveRequestedData(frm, data) {
    $.ajax({
        contentType: "application/json; charset=utf-8",
        url: frm.attr("action"),
        type: frm.attr("method"),
        dataType: "json",
        data: JSON.stringify(data),
        success: function () {
            location.reload();
        }
    });
}

function saveMultipartRequestedData(frm, data) {
    $.ajax({
        enctype : 'multipart/form-data',
        url: frm.attr("action"),
        type: frm.attr("method"),
        data : data,
        processData : false,
        contentType : false,
        success : function() {
            location.reload();
        }
    });
}