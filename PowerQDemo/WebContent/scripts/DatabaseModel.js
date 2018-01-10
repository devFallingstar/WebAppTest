jQuery.fn.pop = [].pop;
jQuery.fn.shift = [].shift;

var graph = new joint.dia.Graph;
var paper = new joint.dia.Paper({
    el: $('#divPaper'),
    width: 10000,
    height: 2000,
    gridSize: 1,
    model: graph
});

$('.table-add-el').click(function () {
    var $table = $('#tblEl');
    var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
    var $rowlist = $table.find('tr:not(.hide)');
    var rowid = $rowlist.length;
    $clone.find('td.row-id').html(rowid);
    $table.append($clone);
    $rowlist = $table.find('tr:not(.hide)');
    addSelectRef(rowid);
});

$('.table-add-lk').click(function () {
    var $elrow = $('#tblEl').find('tr:not(.hide)');
    $elrow.shift();
    var $table = $('#tblLk');
    var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
    var lknum = $table.find('tr:not(.hide)').length;
    $clone.find('td.row-id').html(lknum);
    $clone.find('select.select-ref').find('option').remove();

    $elrow.each(function(i){
        $clone.find('select.select-ref').append($("<option></option>").attr("value", i).text(i + 1));
    });
    $table.append($clone);    
});

$('.table-remove-el').click(function () {
    var $row = $(this).parent().parent('tr');
    var $table = $row.parent();
    var rowid = $row.find('td.row-id').html();
    $row.detach();
    var $rowlist = $table.find('tr:not(.hide)');
    $rowlist.each(function(i){
        $(this).find('td.row-id').html(i);
    });
    removeSelectRef(rowid);
});

$('.table-remove-lk').click(function () {
    var $row = $(this).parent().parent('tr');
    var $table = $row.parent();
    $row.detach();
    $table.find('tr:not(.hide)').each(function(i){
        $(this).find('td.row-id').html(i);
    });
});

$('.table-up-el').click(function () {
    var $row = $(this).parent().parent('tr');
    if ($row.index() === 2)
        return;
    var $prev = $row.prev();
    $prev.before($row.get(0));
    var $previd = $prev.find('td.row-id');
    var $rowid = $row.find('td.row-id');
    var id1 = $previd.html();
    var id2 = $rowid.html();
    $previd.html(id2);
    $rowid.html(id1);
    updateSelectRef(id1, id2);
});

$('.table-up-lk').click(function () {
    var $row = $(this).parent().parent('tr');
    if ($row.index() === 2)
        return;
    var $prev = $row.prev();
    $prev.before($row.get(0));
    var $previd = $prev.find('td.row-id');
    var $rowid = $row.find('td.row-id');
    var id1 = $previd.html();
    var id2 = $rowid.html();
    $previd.html(id2);
    $rowid.html(id1);
});

$('.table-down-el').click(function () {
    var $row = $(this).parent().parent('tr');
    var $next = $row.next();
    if($next.length === 0)
        return;
    $next.after($row.get(0));
    var $rowid = $row.find('td.row-id');
    var $nextid = $next.find('td.row-id');
    var id1 = $rowid.html();
    var id2 = $nextid.html();
    $rowid.html(id2);
    $nextid.html(id1);
    updateSelectRef(id1, id2);
});

$('.table-down-lk').click(function () {
    var $row = $(this).parent().parent('tr');
    var $next = $row.next();
    if($next.length === 0)
        return;    
    $next.after($row.get(0));
    var $rowid = $row.find('td.row-id');
    var $nextid = $next.find('td.row-id');
    var id1 = $rowid.html();
    var id2 = $nextid.html();
    $rowid.html(id2);
    $nextid.html(id1);
});

$('.select-select').change(function() {
    var $select = $(this);
    var $row = $(this).parent().parent('tr');
    var $update = $row.find('.select-update');
    $update.find('option').remove();
    $update.append($('<option></option>').attr('value', 'N.A.').text('N.A.'));
    $.each(db, function(i, rel){
        if(db[i]['Table'] == $select.val())
        {
            var colList = db[i]['Column'];
            $.each(colList, function(j, col){
                $update.append($('<option></option>').attr('value', col).text(col));
            });
        }
    });
});

var element = [];
var link = [];

$('#btnDraw').click(function () {
    element = parseTable('#tblEl');
    link = parseTable('#tblLk');
    drawERDiagram(element, link);
    attachInputVal();
});

function addSelectRef(rowid) {
    var index = rowid - 1;
    $('select.select-ref').each(function(i){
        $(this).append($("<option></option>").attr("value", index).text(rowid));
    });
}

function removeSelectRef(rowid) {
    var index = rowid - 1;
    $('#tblLk').find('tr:not(.hide)').has('option:selected[value=' + index + ']').detach();
    $('#tblLk').find('tr:not(.hide)').each(function(i){
        $(this).find('td.row-id').html(i);
    });
    
    $('select.select-ref').each(function(i){
        var $selectOpt = $(this).find('option:selected');
        var selectid = $selectOpt.text();
        if(Number(selectid) > rowid)
        {
            $selectOpt.prop('selected', false);
            $selectOpt.prev().prop('selected', true);
        }        
        $(this).find('option:last').remove();
    });
}

function updateSelectRef(id1, id2) {
    var index1 = id1 - 1;
    var index2 = id2 - 1;
    var $opt1 = $('select.select-ref').has('option:selected[value=' + index1 + ']');
    var $opt2 = $('select.select-ref').has('option:selected[value=' + index2 + ']');
    //$opt1.val(index2);
    //$opt2.val(index1);
    $opt1.each(function(i){
        $(this).find('option:eq(' + index1 + ')').prop('selected', false);
        $(this).find('option:eq(' + index2 + ')').prop('selected', true);
    });
    $opt2.each(function(i){
        $(this).find('option:eq(' + index2 + ')').prop('selected', false);
        $(this).find('option:eq(' + index1 + ')').prop('selected', true);
    });    
}

function attachInputVal() {
    $('input[name=\'element\']').val(JSON.stringify(element));
    $('input[name=\'link\']').val(JSON.stringify(link));
}

function parseTable(tbl) {
    var table = [];
    var $rows = $(tbl).find('tr:not(:hidden)');
    var headers = [];
    $($rows.shift()).find('th:not(:empty)').each(function () {
        headers.push($(this).text());
    });
    $rows.each(function(){
        var $tdlist = $(this).find('td');
        var data = {};
        $.each(headers, function(i, header){
            var $td = $tdlist.eq(i);
            var $select = $td.find('select');
            if($select.length > 0)
            {
                data[header] = $select.val();
            }
            else
            {
                data[header] = $td.text();
            }
        });
        table.push(data);
    });
    return table;   
}

function drawERDiagram(elList, lkList) {
    var erd = joint.shapes.erd;
    var directedgraph = joint.layout.DirectedGraph;

    var element = function(elm, label) {
        var cell = new elm({attrs: { text: { text: label }}});
        graph.addCell(cell);
        return cell;
    };

    var link = function(elm1, elm2) {
        var myLink = new erd.Line({ source: { id: elm1.id }, target: { id: elm2.id }});
        graph.addCell(myLink);
        return myLink;
    };
    var node = [];
    var edge = [];
    graph.clear();
    $.each(elList, function(i, el){
        switch(el["Element"]){
            case "Entity":
                node.push(element(erd.Entity, el["Name"]));
                break;
            case "Relationship":
                node.push(element(erd.Relationship, el["Name"]));
                break;
            case "Key":
                node.push(element(erd.Key, el["Name"]));
                break;
            case "Normal":
                node.push(element(erd.Normal, el["Name"]));
                break;
            case "Multivalued":
                node.push(element(erd.Multivalued, el["Name"]));
                break;
        }
    });

    $.each(lkList, function(i, lk){
        if(lk["Cardinality"] == "N.A.")
        {
            edge.push(link(node[lk["Element1"]], node[lk["Element2"]]));
        }
        else
        {
            edge.push(link(node[lk["Element1"]], node[lk["Element2"]]).cardinality(lk["Cardinality"]));
        }
    });

    directedgraph.layout(graph, { setLinkVertices: false });    

    //var bbox = paper.getContentBBox();
    //var width = bbox.width > containerWidth ? bbox.width : containerWidth;
    //var height = bbox.height > containerHeight ? bbox.height : containerHeight;
    //paper.setOrigin(-bbox.x, -bbox.y);
    //paper.setDimensions(width, height);
}

(function initial() {
    element = parseTable('#tblEl');
    link = parseTable('#tblLk');
    drawERDiagram(element, link);
    attachInputVal();
})();