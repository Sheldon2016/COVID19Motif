var motif_input_cy;
var neo4j_cy;
var driver;
var layout;
var node_ids = [];
var input_motifs = [];
var number_of_motifs = 1;
var node_selection_order = [];

function initCytoscape() {

    // build the cy object
    neo4j_cy = cytoscape({
        container: document.getElementById('motifVisualOutput'),

        zoomingEnabled: true,
        panningEnabled: true,
        userZoomingEnabled: true,

        style: cytoscape.stylesheet()
            .selector('node')
            .css({
                'content': 'data(name)',
                'height': 30,
                'width': 30,
                'background-color': '#543190',
                'border-color': '#000',
                'border-width': 1,
                'border-opacity': 1
            })
            .selector('node:selected')
            .css({
                'border-color': '#e700ff',
            })
            .selector('edge')
            .css({
                'curve-style': 'straight',
                'width': 1,
                'target-arrow-shape': 'triangle',
                'line-color': '#000',
                'target-arrow-color': '#000'
            }),
        layout: {
            name: 'random'
        },
        ready: function () {
            window.cy = this;
        }
    });


    //connect to Neo4j db
    host = 'neo4j://' + document.getElementById("host").value;
    port = parseInt(document.getElementById("port").value);
    user = document.getElementById("user").value;
    password = document.getElementById("password").value;
    driver = neo4j.driver(host, neo4j.auth.basic(user, password));

    console.log("cy init done...")


}

function getRandomColor() {
    var letters = '0123456789ABCDEF';
    var color = '#';
    for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

var sources = [];
var targets = [];

function motif_input_btn_click() {
    var modal = document.getElementById("graphMSelector");
    modal.style.display = "block";

    motif_input_cy = window.cy = cytoscape({
        container: document.getElementById('canvasHolder'),

        zoomingEnabled: true,
        panningEnabled: true,
        userZoomingEnabled: false,

        style: cytoscape.stylesheet()
            .selector('node')
            .css({
                'content': 'data(name)',
                'height': 30,
                'width': 30,
                'background-color': '#543190',
                'border-color': '#000',
                'border-width': 1,
                'border-opacity': 1
            })
            .selector('node:selected')
            .css({
                'border-color': '#e700ff',
            })
            .selector('edge')
            .css({
                'curve-style': 'bezier',
                'width': 1,
                'target-arrow-shape': 'triangle',
                'line-color': '#000',
                'target-arrow-color': '#000'
            }),
        layout: {
            name: 'concentric',
            directed: true
        },
    });

    motif_input_cy.on('tap', function (event) {
        // target holds a reference to the originator
        // of the event (core or element)
        var evtTarget = event.target;

        if (evtTarget === cy) {
            node_selection_order = [];

        } else if (evtTarget['_private']['group'] === "nodes") {
            node_selection_order.push(evtTarget['_private']['data']['id']);
        }
    });

    //motif_input_cy.remove(motif_input_cy.elements("node"));
    var selectAllOfTheSameType = function (ele) {
        motif_input_cy.elements().unselect();
        if (ele.isNode()) {
            motif_input_cy.nodes().select();
        } else if (ele.isEdge()) {
            motif_input_cy.edges().select();
        }
    };
    var context_options = {
        menuItems: [
            {
                id: 'remove',
                content: 'remove',
                tooltipText: 'remove',
                image: {src: "img/remove.svg", width: 12, height: 12, x: 6, y: 4},
                selector: 'node, edge',
                onClickFunction: function (event) {
                    var target = event.target;
                    target.remove();
                },
                hasTrailingDivider: true
            },
            {
                id: 'hide',
                content: 'hide',
                tooltipText: 'hide',
                selector: '*',
                onClickFunction: function (event) {
                    var target = event.target;
                    target.hide();
                },
                disabled: false
            },
            {
                id: 'remove-selected',
                content: 'remove selected',
                tooltipText: 'remove selected',
                image: {src: "img/remove.svg", width: 12, height: 12, x: 6, y: 6},
                coreAsWell: true,
                onClickFunction: function (event) {
                    motif_input_cy.$(':selected').remove();
                }
            },
            {
                id: 'select-all-nodes',
                content: 'select all nodes',
                tooltipText: 'select all nodes',
                selector: 'node',
                onClickFunction: function (event) {
                    selectAllOfTheSameType(event.target);
                }
            },
            {
                id: 'select-all-edges',
                content: 'select all edges',
                tooltipText: 'select all edges',
                selector: 'edge',
                onClickFunction: function (event) {
                    selectAllOfTheSameType(event.target || event.cyTarget);
                }
            },
            {
                id: 'add-edge',
                content: 'add edge',
                tooltipText: 'add edge',
                image: {src: "img/add.svg", width: 12, height: 12, x: 5, y: 7},

                selector: 'node:selected',
                onClickFunction: function (event) {
                    if (node_selection_order.length !== 2)
                        alert("Need to select 2 nodes");
                    else {
                        var s = node_selection_order[0];
                        var t = node_selection_order[1];
                        motif_input_cy.add([{
                            group: "edges",
                            data: {
                                source: s,
                                target: t,
                                class: "edge"
                            }
                        }]);
                    }
                },
                hasTrailingDivider: true
            }
        ]
    };

    var context_menu_instance = motif_input_cy.contextMenus(context_options);
    var type_reader_session = driver.session();
    var i = 0;
    node_types = [];
    type_reader_session.run("MATCH (n) RETURN DISTINCT LABELS(n)").subscribe({
        onNext: record => {
            i += 1;
            console.log(i);
            context_menu_instance.appendMenuItem(
                {
                    id: i,
                    content: record.get(0)[0],
                    coreAsWell: true,
                    onClickFunction: function (event) {
                        var data = {
                            group: 'nodes',
                            name: record.get(0)[0]
                        };

                        var pos = event.position;

                        if (!node_types.includes(record.get(0)[0])) {
                            console.log("new color for class:  " + record.get(0)[0]);
                            motif_input_cy.style().selector('.' + record.get(0)[0]).css({'background-color': getRandomColor()}).update();
                            // motif_input_cy.style().selector('Virus' ).css({'background-color': getRandomColor()});

                            node_types.push(record.get(0)[0]);
                        }

                        motif_input_cy.add({
                            data: data,
                            position: {
                                x: pos.x,
                                y: pos.y
                            },
                            classes: record.get(0)[0],
                        });
                    }


                }
            );
        },

    });
}

// When the user clicks on <span> (x), close the modal
function modal_close() {
    var modal = document.getElementById("graphMSelector");
    modal.style.display = "none";
    modal = document.getElementById("queryVisualResult");
    modal.style.display = "none";
}

function clean_up_motif() {
    motif_input_cy.remove(motif_input_cy.elements("node"));
    motif_input_cy.remove(motif_input_cy.elements("edge"));
}

var sources = [];
var targets = [];

function submit_button() {
    var all = cy.edges();
    // alert(all.length);
    for (i = 0; i < all.length; i++) {
        the_edge = all[i];
        var target = cy.getElementById(the_edge['_private']['data']['target']);
        var source = cy.getElementById(the_edge['_private']['data']['source']);

        var sourceName = source['_private']['data'];//['id'];
        var targetName = target['_private']['data'];//['id'];

        sources.push(sourceName);
        targets.push(targetName);
        // alert(source['_private']['data']['name']+"--> "+target['_private']['data']['name']);
    }
    var modal = document.getElementById("graphMSelector");
    modal.style.display = "none";

}


function draw_query_result() {
    var modal = document.getElementById("queryVisualResult");
    modal.style.display = "block";
}

function filter_results() {
    var result_nodes = [];
    var result_edges = [];
    var result_node_ids = [];
    var result_edges_ids = [];
    var node_types = [];

    var session = driver.session();
    var resEdgeNum = 0;
    session.run(visulizationQuery).then(result => {
            result.records.forEach(record => {
                console.log(record);

                for (var j = 0; j < record.length - 1; j++) {
                    var source = record.get(j);
                    var source_node, target_node;
                    let s_flag = true;
                    let t_flag = true;
                    if (!node_ids.includes(source['identity']['low'])) {
                        source_node = {
                            group: 'nodes',
                            data: {
                                id: source['identity']['low'],
                                name: source['properties']['name'],
                            },
                            classes: [source['labels'][0]],

                        };
                        node_ids.push(source['identity']['low']);
                    } else {
                        // console.log("source redundant -->" + source['identity']['low'] + ",  " + source['properties']['label']);
                        s_flag = false;

                    }
                    var target = record.get(j + 1);
                    if (!node_ids.includes(target['identity']['low'])) {
                        target_node = {
                            group: 'nodes',
                            data: {
                                id: target['identity']['low'],
                                name: target['properties']['name'],
                            },
                            classes: [target['labels'][0]],

                        };
                        node_ids.push(target['identity']['low']);
                    } else {
                        // console.log("target redundant -->" + target['identity']['low'] + ",  " + target['properties']['label']);
                        t_flag = false;

                    }
                    var edge = {
                        group: "edges",
                        data: {
                            // inferred as an edge because `source` and `target` are specified:
                            source: source['identity']['low'], // the source node id (edge comes from this node)
                            target: target['identity']['low']  // the target node id (edge goes to this node)
                            // (`source` and `target` can be effectively changed by `eles.move()`)
                        },

                        pannable: true // whether dragging on the edge causes panning
                    };

                    if (s_flag) {
                        if (!node_types.includes(source_node['classes'])) {
                            neo4j_cy.style().selector('.' + source_node['classes']).css({'background-color': getRandomColor()}).update();
                            node_types.push(source_node['classes'])
                        }

                        neo4j_cy.add(source_node);
                    }
                    if (t_flag) {
                        if (!node_types.includes(target_node['classes'])) {
                            neo4j_cy.style().selector('.' + target['classes']).css({'background-color': getRandomColor()}).update();
                            node_types.push(target_node['classes'])
                        }
                        neo4j_cy.add(target_node);
                    }
                    if (!result_edges.includes(edge)) {
                        neo4j_cy.add(edge);
                        resEdgeNum = resEdgeNum + 1;
                        result_edges.push(edge);
                    }
                    //alert(resEdgeNum+": "+edge['source']+" - "+edge['source']);
                }
                // console.log('----------------------');

            });
        }
    ).catch(error => {
        console.log(error)
    }).then(() => {
        layout = neo4j_cy.layout({
            name: 'concentric'
        });
        neo4j_cy.on('tap', function (event) {
            // target holds a reference to the originator
            // of the event (core or element)
            var evtTarget = event.target;

            if (evtTarget === neo4j_cy) {
                console.log('tap on background');
                layout.run();
            } else {
                console.log('tap on some element');
            }
        });

        neo4j_cy.center();


    });

    // neo4j_cy.center();


}

function motif_input_next_button() {
    number_of_motifs += 1;
    document.getElementById("motifNameDiv").innerHTML = "M" + number_of_motifs;
    var current_state = motif_input_cy.json();
    input_motifs.push(current_state);
    console.log(input_motifs.length, input_motifs[input_motifs.length - 1]);
    motif_input_cy.remove(motif_input_cy.elements("node"));
}

