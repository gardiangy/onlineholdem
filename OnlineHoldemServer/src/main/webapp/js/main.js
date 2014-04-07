$(function () {

    var $table = $('#game-table');

    var table = $table.dataTable({
        "bProcessing": true,
        "bServerSide": true,
        "sDom": 'frtlpi',

        "aoColumnDefs": [
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

    $table.on("reload", function(){
       table.fnReloadAjax();
    });


});


function showError(input, errorMessage) {
    $(input).addClass("errorInput").parent().siblings("span").html(errorMessage);
}

function removeError(input) {
    $(input).removeClass("errorInput").parent().siblings("span").html("");
}

