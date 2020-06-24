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
        // container: document.getElementById('motifVisualOutput'),

        zoomingEnabled: true,
        panningEnabled: true,
        userZoomingEnabled: true,

        style: [
            {
                selector: 'node',
                style: {
                    'content': 'data(name)'
                }
            },

            {
                selector: 'edge',
                style: {
                    'target-arrow-shape': 'triangle'
                }
            },

            {
                selector: ':selected',
                style: {}
            }
        ],
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

    // get all the edges and add them to cy object
    var init_session = driver.session();
    init_session.run("match (a)-->(b) return a,b").then(result => {
        result.records.forEach(record => {
            // console.log(record);
            var source = record.get(0);
            var source_node, target_node;
            let s_flag = true;
            let t_flag = true;
            if (!node_ids.includes(source['properties']['id']['low'])) {
                source_node = {
                    group: 'nodes',
                    data: {
                        id: source['properties']['id']['low'],
                        name: source['properties']['label'],
                    },
                    classes: [source['labels'][0]],

                };
                node_ids.push(source['properties']['id']['low']);
            } else {
                // console.log("source redundant -->" + source['properties']['id']['low'] + ",  " + source['properties']['label']);
                s_flag = false;

            }
            var target = record.get(1);
            if (!node_ids.includes(target['properties']['id']['low'])) {
                target_node = {
                    group: 'nodes',
                    data: {
                        id: target['properties']['id']['low'],
                        name: target['properties']['label'],
                    },
                    classes: [target['labels'][0]],

                };
                node_ids.push(target['properties']['id']['low']);
            } else {
                // console.log("target redundant -->" + target['properties']['id']['low'] + ",  " + target['properties']['label']);
                t_flag = false;

            }
            var edge = {
                group: "edges",
                data: {
                    // inferred as an edge because `source` and `target` are specified:
                    source: source['properties']['id']['low'], // the source node id (edge comes from this node)
                    target: target['properties']['id']['low']  // the target node id (edge goes to this node)
                    // (`source` and `target` can be effectively changed by `eles.move()`)
                },

                pannable: true // whether dragging on the edge causes panning
            };

            if (s_flag) neo4j_cy.add(source_node);
            if (t_flag) neo4j_cy.add(target_node);
            neo4j_cy.add(edge);


        });
    });

    layout = neo4j_cy.layout({
        name: 'random'
    });


    // neo4j_cy.on('tap', function (event) {
    //     // target holds a reference to the originator
    //     // of the event (core or element)
    //     var evtTarget = event.target;
    //
    //     if (evtTarget === neo4j_cy) {
    //         console.log('tap on background');
    //         layout.run();
    //     } else {
    //         console.log('tap on some element');
    //     }
    // });
    // neo4j_cy.center();
    // console.log(neo4j_cy);
    console.log("cy init done...")


}


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
                'border-width': 4,
                'border-opacity': 1
            })
            .selector('node:selected')
            .css({
                'border-color': '#e700ff',
            })
            .selector('edge')
            .css({
                'curve-style': 'bezier',
                'width': 3,
                'target-arrow-shape': 'triangle',
                'line-color': '#000',
                'target-arrow-color': '#000'
            }),
        layout: {
            name: 'breadthfirst',
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

                        function getRandomColor() {
                            var letters = '0123456789ABCDEF';
                            var color = '#';
                            for (var i = 0; i < 6; i++) {
                                color += letters[Math.floor(Math.random() * 16)];
                            }
                            return color;
                        }

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

function submit_button() {
    var all = cy.edges();
    // alert(all.length);
    for (i = 0; i < all.length; i++) {
        the_edge = all[i];
        var target = cy.getElementById(the_edge['_private']['data']['target']);
        var source = cy.getElementById(the_edge['_private']['data']['source']);

        // alert(source['_private']['data']['name']+"--> "+target['_private']['data']['name']);
    }
    var modal = document.getElementById("graphMSelector");
    modal.style.display = "none";

}


function draw_query_result() {
    var modal = document.getElementById("queryVisualResult");
    modal.style.display = "block";
}

function filter_results(cypher) {
    var result_nodes = [];
    var result_edges = [];
    var result_node_ids = [];
    var result_edges_ids = [];

    var session = driver.session();

    session.run(cypher).then(result => {
            result.records.forEach(record => {
                console.log(record);
                for (var j = 0; j < record.length; j++) {
                    var value = record.get(j);
                    if (value === null) {
                        content = undefined;
                    }

                    if (value === null) {
                        content = undefined;
                    } else if (typeof value === "string") {
                        alert("return type must be object")
                    } else if (typeof value === "object" && !Array.isArray(value)) {
                        var type = value.constructor.name;
                        switch (type) {
                            case "Node":
                                // content = nodeValue(div, value.id, value.labels, value.properties);
                                var node_id = value['properties']['id']['low'];
                                // console.log("node id:" + node_id + "  label: " + value['labels']);
                                // console.log(neo4j_cy.$id(node_id));
                                result_nodes.push(neo4j_cy.$id(node_id));
                                // console.log(result_nodes);
                                // console.log("+_+_+_+_+_+_+++_+_+_+_+_+_+_");
                                break;

                            case "Edge":
                                alert("edge detected!");
                                break;

                        }
                    } else {
                        alert("something is wrong!")
                    }


                }
                // console.log('----------------------');

            });
        }
    ).catch(error => {
        console.log(error)
    }).then(() => {
        // console.log("++++>>" + result_nodes['length']);
        // console.log(result_nodes);
        var final_result_nodes = [];
        for (var i1 = 0; i1 < result_nodes.length; i1++) {
            var source_node = {
                group: 'nodes',
                data: {
                    id: result_nodes[i1]['_private']['data']['id'],
                    name: result_nodes[i1]['_private']['data']['name'],
                },
                classes: result_nodes[i1]['_private']['classes'],

            };
            if (!result_node_ids.includes(source_node['data']['id'])) {
                final_result_nodes.push(source_node);
                result_node_ids.push(source_node['data']['id']);
            }


            console.log("source:");
            console.log(source_node);
            console.log("edges:");
            // console.log(" in for " + i1);
            var edges = result_nodes[i1]['_private']['edges'];
            for (var j2 = 0; j2 < edges.length; j2++) {
                // console.log("new Edge! -->>" + edges[j2]);
                var target_id = edges[j2]['_private']['data']['target'];
                var target_node = {
                    group: 'nodes',
                    data: {
                        id: target_id,
                        name: neo4j_cy.$id(target_id)['_private']['data']['name'],
                    },
                    classes: neo4j_cy.$id(target_id)['_private']['classes'],

                };
                console.log(target_node);

                if (!result_node_ids.includes(target_node['data']['id'])) {
                    final_result_nodes.push(target_node);
                    result_node_ids.push(target_node['data']['id']);
                }
                if (source_node['data']['id'] !== target_node['data']['id']) {
                    var new_edge = {
                        group: 'edges',
                        data: {
                            source: source_node['data']['id'],
                            target: target_node['data']['id'],
                        },

                    };
                    console.log("creating edge: " + source_node['data']['id'] + " ----> " + target_node['data']['id']);
                    if (!result_edges_ids.includes([new_edge['data']['source'], new_edge['data']['id']])) {
                        result_edges.push(new_edge);
                        result_edges_ids.push([new_edge['data']['source'], new_edge['data']['id']]);

                    }
                }


            }

        }
        console.log("building cyto_vis");
        vis_cy = cytoscape({
            container: document.getElementById('motifVisualOutput'),

            zoomingEnabled: true,
            panningEnabled: true,
            userZoomingEnabled: true,

            style: [
                {
                    selector: 'node',
                    style: {
                        'content': 'data(name)'
                    }
                },

                {
                    selector: 'edge',
                    style: {
                        'target-arrow-shape': 'triangle'
                    }
                },

                {
                    selector: ':selected',
                    style: {}
                }
            ],
            layout: {
                name: 'random'
            },
            ready: function () {
                window.cy = this;
            }
        });
        // console.log("<><><><>><><><><><<><><><><><><><><><><><><><><><><><><><>");
        // console.log(result_edges);
        // console.log("res nodes:" + result_nodes);

        vis_cy.add(final_result_nodes);
        vis_cy.add(result_edges);

        console.log(vis_cy);
        vis_cy.center();
        layout = vis_cy.layout({
            name: 'random'
        });


        vis_cy.on('tap', function (event) {
            // target holds a reference to the originator
            // of the event (core or element)
            var evtTarget = event.target;

            if (evtTarget === vis_cy) {
                console.log('tap on background');
                layout.run();
            } else {
                console.log('tap on some element');
            }
        });


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

