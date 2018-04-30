$('.chart-box ul.doughnut-legend').hide();

/* -- [ pass fail count ] -- */
var passesCnt = $('#test-collection li.test.displayed.has-leaf.pass').length;
var failsCnt = $('#test-collection li.test.displayed.has-leaf.fail').length;
var skipsCnt = $('#test-collection li.test.displayed.has-leaf.skip').length;

/* -- [ update counts ] -- */
$('#charts-row div.card-panel.nm-v:first div.block.text-small:first span.strong:first').html(passesCnt);
$('#charts-row div.card-panel.nm-v:first div.block.text-small:eq(1) span.strong:first').html(failsCnt);
$('#charts-row div.card-panel.nm-v:first div.block.text-small:eq(1) span.strong:first + span').html(skipsCnt);

/* -- [ parent chart ] -- */
function drawParentChart() {
    var data = [
        { value: passesCnt, color: '#00af00', highlight: '#32bf32', label: 'Pass' },
        { value: failsCnt, color:'#F7464A', highlight: '#FF5A5E', label: 'Fail' },
        { value: statusGroup.fatalParent, color:'#8b0000', highlight: '#a23232', label: 'Fatal' },
        { value: statusGroup.errorParent, color:'#ff6347', highlight: '#ff826b', label: 'Error' },
        { value: statusGroup.warningParent, color: '#FDB45C', highlight: '#FFC870', label: 'Warning' },
        { value: skipsCnt, color: '#1e90ff', highlight: '#4aa6ff', label: 'Skip' }
    ];

    var ctx = $('#parent-analysis').get(0).getContext('2d');
    testChart = new Chart(ctx).Doughnut(data, options);
    drawLegend(testChart, 'parent-analysis');
}; drawParentChart();

/* -- [ calculate count of passes fails and skips for CHILD tests ] -- */
var passChild = $('div.test-content ul.collapsible.node-list[data-collapsible=accordion] li.node.level-1.leaf.pass[status=pass]').length;
var failChild = $('div.test-content ul.collapsible.node-list[data-collapsible=accordion] li.node.level-1.leaf.fail').length;
var skipChild = $('div.test-content ul.collapsible.node-list[data-collapsible=accordion] li.node.level-1.leaf.skip').length;


/* -- [ updating the displayed value of counts of tests ] -- */
$('#charts-row div.card-panel.nm-v:eq(1) div.block.text-small:first span.strong:first').html(passChild);
$('#charts-row div.card-panel.nm-v:eq(1) div.block.text-small:eq(1) span.strong:first').html(failChild);
$('#charts-row div.card-panel.nm-v:eq(1) div.block.text-small:eq(1) span.strong:first + span').html(skipChild);

/* -- [ children chart ] -- */
function drawChildChart() {
    var data = [
        {value: passChild, color: '#00af00', highlight: '#32bf32', label: 'Pass'},
        {value: failChild, color: '#F7464A', highlight: '#FF5A5E', label: 'Fail'},
        {value: statusGroup.fatalChild, color: '#8b0000', highlight: '#a23232', label: 'Fatal'},
        {value: statusGroup.errorChild, color: '#ff6347', highlight: '#ff826b', label: 'Error'},
        {value: statusGroup.warningChild, color: '#FDB45C', highlight: '#FFC870', label: 'Warning'},
        {value: skipChild, color: '#ff8c00', highlight: '#ff8c01', label: 'Skip'},
    ];

    if ($('#child-analysis').length > 0) {
        var ctx = $('#child-analysis').get(0).getContext('2d');
        stepChart = new Chart(ctx).Doughnut(data, options);
        drawLegend(stepChart, 'child-analysis');
    };
};drawChildChart();