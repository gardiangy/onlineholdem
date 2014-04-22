$(function () {

    var $userName = $("#inp-login-username");
    var $password = $("#inp-login-password");

    $("#btn-login").on("click", function (e) {
        e.preventDefault();
        showLoginForm();

    });

    $("#btn-login-close").on("click", function (e) {
        e.preventDefault();
        $("#overlay").hide();
        $("#login-content").removeClass("slideInDown").addClass("slideOutUp");
        clearForm();
    });

    $("#btn-logout").on("click", function (e) {
        e.preventDefault();
        $("#login-inf").hide();
        $("#username").html('');
        $("#btn-login").show();
        sessionStorage.clear();
        $("#joined-game").hide();
        $("#btn-leave-game").hide();
        $("#btn-join-game").show();

    });


    $("#btn-login-ok").on("click", function () {
        var validFields = 0;
        validFields = validateTextField($userName) ? validFields + 1 : validFields;
        validFields = validateTextField($password) ? validFields + 1 : validFields;

        if (validFields == 2) {
            var DTO = { 'userName': $userName.val(),
                'userPassword': $password.val()
            };
            $.ajax({
                type: 'POST',
                url: '/rest/login',
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(DTO)
            }).done(function (data) {
                if (data.responseType == 'OK') {
                    $("#overlay").hide();
                    $("#login-content").removeClass("slideInDown").addClass("slideOutUp");
                    clearForm();
                    $("#btn-login").hide();
                    $("#username").html(data.responseObject.userName);
                    $("#login-inf").show();
                    sessionStorage.userId = data.responseObject.userId;
                    sessionStorage.userName = data.responseObject.userName;
                    loadJoinedGame();

                }
                if (data.responseType == 'ERROR') {
                    var n = noty({text: data.errorMessage,
                        layout: 'top',
                        type: 'error'});
                }

            });
        }

    });


    function clearForm() {
        $userName.val('');
        removeError($userName);
        $password.val('');
        removeError($password);
    }


    function validateTextField(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");
            return false;

        } else {
            removeError(field);
            return true;
        }
    }

});

function showLoginForm(){
    $("#overlay").show();
    $("#login-content").show().removeClass("slideOutUp").addClass("slideInDown");
}