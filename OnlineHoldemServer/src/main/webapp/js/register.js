$(function () {
    var $registerFormInputs = $("#register-form").find("input");


    $("#btn-register").on("click", function () {
        $registerFormInputs.each(function () {
            validateField($(this));
        });
    });

    $registerFormInputs.on("blur", function () {
        validateField($(this));
    });

    function validateField(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");

        } else if (field.val().length < 4) {
            removeError(field);
            showError(field, "must be longer than 3 characters");

        } else {
            removeError(field);
        }
    }


});


