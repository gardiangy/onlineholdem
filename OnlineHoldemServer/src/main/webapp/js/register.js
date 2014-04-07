$(function () {

    var $registerFormInputs = $("#register-form").find('input[type!="button"]');

    var $userName = $("#inp-user-name");
    var $userPassword = $("#inp-password");
    var $userPasswordConfirm = $("#inp-password-confirm");
    var $userEmial = $("#inp-email");


    $("#btn-register").on("click", function () {
        var validFields = 0;
        $registerFormInputs.each(function () {
            validFields = validateField($(this)) ? validFields + 1 : validFields;
        });

        if(validFields == 4){
            validFields = validatePassword() ? validFields + 1 : validFields;
        }

        if(validFields == 5){
            var DTO = { 'userName': $userName.val(),
                'userPassword': $userPassword.val(),
                'userEmail': $userEmial.val()
            };
            $.ajax({
                type: 'POST',
                url: '/rest/login/register',
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(DTO)
            }).done(function(data){
                    if(data.responseType == 'OK'){
                        var n = noty({text: 'Successfully registered!',
                            layout: 'top',
                            type: 'success'});
                        clearForm();
                    }

                    if(data.responseType == 'ERROR'){
                        var n = noty({text: data.responseObject,
                            layout: 'top',
                            type: 'error'});
                    }

                });
        }


    });

    $("#menu2content").on("showPage",function(){
        clearForm();
    });

    $registerFormInputs.on("blur", function () {
        validateField($(this));
    });

    function clearForm(){
        $userName.val('');
        removeError($userName);
        $userPassword.val('');
        removeError($userPassword);
        $userPasswordConfirm.val('');
        removeError($userPasswordConfirm);
        $userEmial.val('');
        removeError($userEmial);
    }

    function validateField(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");
            return false;

        } else if (field.val().length < 4) {
            removeError(field);
            showError(field, "must be longer than 3 characters");
            return false;

        } else {
            removeError(field);
            return true;
        }
    }

    function validatePassword(){
        if($userPassword.val() != $userPasswordConfirm.val()){
            removeError($userPasswordConfirm);
            showError($userPasswordConfirm, "password does not match");
            return false;
        }
        return true;
    }


});


