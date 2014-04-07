$(function () {

    var $gameName = $("#inp-game-name");
    var $maxPlayerNumber = $("#inp-max-player-number");
    var $startingStackSize = $("#inp-starting-stack-size");
    var $startTime = $("#inp-start-time");

    $startTime.datepicker();

    $("#btn-new-game").on("click", function(){
         $("#overlay").show();
         $("#new-game-content").show().removeClass("slideOutUp").addClass("slideInDown");


    });

    $("#btn-new-game-close").on("click", function(e){
        e.preventDefault();
        $("#overlay").hide();
        $("#new-game-content").removeClass("slideInDown").addClass("slideOutUp");
        clearForm();
    });




    $("#btn-add-new-game").on("click", function () {
        var validFields = 0;
        validFields = validateTextField($gameName) ? validFields + 1 : validFields;
        validFields = validatePlayerNumField($maxPlayerNumber) ? validFields + 1 : validFields;
        validFields = validateStackSizeField($startingStackSize) ? validFields + 1 : validFields;
        validFields = validateStartTime($startTime) ? validFields + 1 : validFields;

        if(validFields == 4){
            var DTO = { 'gameName': $gameName.val(),
                'maxPlayerNumber': $maxPlayerNumber.val(),
                'startingStackSize': $startingStackSize.val(),
                'startTime': $startTime.datepicker("getDate")
            };
            $.ajax({
                type: 'POST',
                url: '/rest/game/create',
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(DTO)
            }).done(function(data){
                    if(data.responseType == 'OK'){
                        $("#game-table").trigger("reload");
                        $("#new-game-overlay").hide();
                        $("#new-game-content").removeClass("slideInDown").addClass("slideOutUp");
                        var n = noty({text: 'Game has been created!',
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

    $gameName.on("blur", function () {
        validateTextField($(this));
    });

    $maxPlayerNumber.on("blur", function () {
        validatePlayerNumField($(this));
    });

    $startingStackSize.on("blur", function () {
        validateStackSizeField($(this));
    });

    function clearForm(){
        $gameName.val('');
        removeError($gameName);
        $maxPlayerNumber.val('');
        removeError($maxPlayerNumber);
        $startingStackSize.val('');
        removeError($startingStackSize);
        $startTime.val('');
        removeError($startTime);
    }


    function validateTextField(field) {
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

    function validatePlayerNumField(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");
            return false;

        } else if (!$.isNumeric(field.val())) {
            removeError(field);
            showError(field, "must be a number");
            return false;

        } else if (field.val() < 2) {
            removeError(field);
            showError(field, "cannot be smaller than 2");
            return false;

        } else if (field.val() > 9) {
            removeError(field);
            showError(field, "cannot be bigger than 9");
            return false;

        } else {
            removeError(field);
            return true;
        }
    }

    function validateStackSizeField(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");
            return false;

        } else if (!$.isNumeric(field.val())) {
            removeError(field);
            showError(field, "must be a number");
            return false;

        } else if (field.val() < 0) {
            removeError(field);
            showError(field, "cannot be smaller than 0");
            return false;

        } else {
            removeError(field);
            return true;
        }
    }

    function validateStartTime(field) {
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
