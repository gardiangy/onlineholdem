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
        validateTextField($gameName);
        validatePlayerNumField($maxPlayerNumber);
        validateStackSizeField($startingStackSize);
        validateStartTime($startTime);

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
        }).done(function(){
                $("#game-table").trigger("reload");
                $("#new-game-overlay").hide();
                $("#new-game-content").removeClass("slideInDown").addClass("slideOutUp");
                clearForm();
            });
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

        } else if (field.val().length < 4) {
            removeError(field);
            showError(field, "must be longer than 3 characters");

        } else {
            removeError(field);
        }
    }

    function validatePlayerNumField(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");

        } else if (!$.isNumeric(field.val())) {
            removeError(field);
            showError(field, "must be a number");

        } else if (field.val() < 2) {
            removeError(field);
            showError(field, "cannot be smaller than 2");

        } else if (field.val() > 9) {
            removeError(field);
            showError(field, "cannot be bigger than 9");

        } else {
            removeError(field);
        }
    }

    function validateStackSizeField(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");

        } else if (!$.isNumeric(field.val())) {
            removeError(field);
            showError(field, "must be a number");

        } else if (field.val() < 0) {
            removeError(field);
            showError(field, "cannot be smaller than 0");

        } else {
            removeError(field);
        }
    }

    function validateStartTime(field) {
        if (field.val().length == 0) {
            removeError(field);
            showError(field, "required field");

        } else {
            removeError(field);
        }
    }
});
