$(function () {
    var $table = $('#rankings-table');

    var table = $table.dataTable({
        "bProcessing": true,
        "bServerSide": true,
        "sDom": 'frtlpi',

        "aoColumnDefs": [
            { "sClass": "firstCol", "aTargets": [ 1 ] },
            { "sClass": "alignCenter", "aTargets": [ 2, 3, 4] },
            { "bVisible": false, "aTargets": [ 0 ] }
        ],
        "sAjaxSource": 'rest/rankings/table'
    });
});
