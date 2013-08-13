$( document ).ready(function() {
	
	var rootURL = "http://localhost:8080";

    function saveMessage() {
        var DTO = { 'value' : newMessage };
        $.ajax({
            type : 'POST',
            url : rootURL + '/rest/message',
            contentType: "application/json",
            dataType: "json",
            data:  JSON.stringify(DTO)
        });
    }

    var newMessage = undefined;


    $('#newBtn').click(function() {
        newMessage = $("#newInput").val();
        saveMessage();

    });
	
});

