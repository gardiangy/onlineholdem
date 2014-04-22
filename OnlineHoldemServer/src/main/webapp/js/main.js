$(function () {

    var $table = $('#game-table');

    var table = $table.dataTable({
        "bProcessing": true,
        "bServerSide": true,
        "sDom": 'frtlpi',

        "aoColumnDefs": [
            { "sClass": "firstCol", "aTargets": [ 1 ] },
            { "sClass": "alignCenter", "aTargets": [ 2, 3, 4 ] },
            { "bVisible": false, "aTargets": [ 0 ] }
        ],
        "sAjaxSource": 'rest/game/table'
    });

    var $menu = $("#menu");
    $("#menu a").click(function () {
        var $clickedMenu = $(this);
        var $contentToLoad = $('#' + $clickedMenu.attr("id") + 'content');
        var $activeContent = $('.content:visible');

        $activeContent.removeClass("slideInRight");
        $activeContent.addClass("slideOutRight");
        $activeContent.one("animationend webkitAnimationEnd oAnimationEnd MSAnimationEnd", function () {
            $activeContent.hide();
            $contentToLoad.show();
            $contentToLoad.removeClass("slideOutRight");
            $contentToLoad.addClass("slideInRight");
            $contentToLoad.trigger("showPage");
        });
        $("#menu").find(".activeMenu").removeClass("activeMenu");
        $clickedMenu.addClass("activeMenu");
        return false;

    });
    var selectedRow = null;

    $table.on("click","tbody tr", function(){
       $table.find(".selectedRow").removeClass("selectedRow");
       $(this).addClass("selectedRow");
        selectedRow = this;
    });

    $table.on("reload", function(){
       table.fnReloadAjax();
    });

    $("#btn-join-game").on("click", function () {
        if (undefined == sessionStorage.userId) {
            showLoginForm();
            return;
        }
        if(null == selectedRow){
            var n = noty({text: "Please select a game!",
                layout: 'top',
                type: 'warning'});
            return;
        }
        var DTO = { 'gameId': table.fnGetData(selectedRow)[0],
            'userId': sessionStorage.userId
        };
        $.ajax({
            type: 'POST',
            url: '/rest/game/join',
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(DTO)
        }).done(function (data) {
            if (data.responseType == 'OK') {
                var n = noty({text: 'Joined to game: ' + data.responseObject.gameName,
                    layout: 'top',
                    type: 'success'});
                loadJoinedGame();
                $("#joined-game").show();
            } else {
                var n = noty({text: "Could not join to game! \n Please try again.",
                    layout: 'top',
                    type: 'error'});
            }
        });


    });

    $("#btn-leave-game").on("click", function () {
        var DTO = { 'gameId': $("#joined-game-id").html(),
            'userId': sessionStorage.userId
        };
        $.ajax({
            type: 'POST',
            url: '/rest/game/leave',
            contentType: "application/json",
            dataType: "json",
            data: JSON.stringify(DTO)
        }).done(function (data) {
            if (data.responseType == 'OK') {
                var n = noty({text: 'Leaved game: ' + data.responseObject.gameName,
                    layout: 'top',
                    type: 'success'});
                $("#joined-game").hide();
                $("#btn-leave-game").hide();
                $("#btn-join-game").show();
            } else {
                var n = noty({text: "Could not leave game! \n Please try again.",
                    layout: 'top',
                    type: 'error'});
            }
        });


    });
    $("#joined-game").hide();

    if(undefined != sessionStorage.userId){
        $("#btn-login").hide();
        $("#username").html(sessionStorage.userName);
        $("#login-inf").show();
        loadJoinedGame();
    }


});

function loadJoinedGame(){
    $.ajax({
        type: 'GET',
        url: '/rest/game/user/' + sessionStorage.userId,
        contentType: "application/json",
        dataType: "json"
    }).done(function (data) {
        if (data.responseType == 'OK') {
            if(null != data.responseObject){
                $("#joined-game").show();
                $("#joined-game-id").html(data.responseObject.gameId);
                $("#joined-game-name").html(data.responseObject.gameName);
                $("#joined-game-max-player-num").html(data.responseObject.maxPlayerNumber + " / "
                    + data.responseObject.players.length);
                var starTime = new Date(data.responseObject.startTime);
                $("#joined-game-start-time").html(starTime.getFullYear() + "-" + (starTime.getMonth() + 1) + "-"
                          + starTime.getDay() + " " + starTime.getHours() + ":" + starTime.getMinutes());
                $("#btn-leave-game").show();
                $("#btn-join-game").hide();
            }
        }
    });
}


function showError(input, errorMessage) {
    $(input).addClass("errorInput").parent().siblings("span").html(errorMessage);
}

function removeError(input) {
    $(input).removeClass("errorInput").parent().siblings("span").html("");
}

