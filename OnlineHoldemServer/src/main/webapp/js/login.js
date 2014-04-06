$(function () {

    var $userName = $("#inp-login-username");
    var $password = $("#inp-login-password");

    $("#btn-login").on("click", function(e){
        e.preventDefault();
         $("#overlay").show();
         $("#login-content").show().removeClass("slideOutUp").addClass("slideInDown");


    });

    $("#btn-login-close").on("click", function(e){
        e.preventDefault();
        $("#overlay").hide();
        $("#login-content").removeClass("slideInDown").addClass("slideOutUp");
        clearForm();
    });




    $("#btn-login-ok").on("click", function () {
        validateTextField($userName);
        validateTextField($password);

//        var DTO = { 'gameName': $gameName.val(),
//                    'maxPlayerNumber': $maxPlayerNumber.val(),
//                    'startingStackSize': $startingStackSize.val(),
//                    'startTime': $startTime.datepicker("getDate")
//        };
//        $.ajax({
//            type: 'POST',
//            url: '/rest/game/create',
//            contentType: "application/json",
//            dataType: "json",
//            data: JSON.stringify(DTO)
//        }).done(function(){
//                $("#game-table").trigger("reload");
//                $("#new-game-overlay").hide();
//                $("#new-game-content").removeClass("slideInDown").addClass("slideOutUp");
//                clearForm();
//            });
    });


    function clearForm(){
        $userName.val('');
        removeError($userName);
        $password.val('');
        removeError($password);
    }


    function validateTextField(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");

        } else {
            removeError(field);
        }
    }

});
