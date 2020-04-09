var result_cy;
var result_edge_counter=0;
var graphResult = null;
var result_choosenGraph = -1;

var graphResult_options = {
	    // List of initial menu items
	    menuItems: [
	      {
                id: 'extend-graph',
                content: 'extend graph',
                tooltipText: 'extend graph',
                selector: 'node',
                onClickFunction: function (event) {
                  var target = event.target || event.cyTarget;
                  var clicked = target["_private"]["data"]["id"];
                  clicked = clicked.substr(1);
                  graphToResultExtension(graphG["adjList"], graphG["labels"], graphG_labelToType, graphG_id2Name, parseInt(clicked));
                },
          },
          {
              id: 'remove-leaf-neighboor',
              content: 'remove leaf neighboor',
              tooltipText: 'remove leaf neighboor',
              selector: 'node',
              onClickFunction: function (event) {
                var target = event.target || event.cyTarget;
                var id = target["_private"]["data"]["id"];
                var cy_node = result_cy.$id(id);
                cy_node.neighborhood().nodes().forEach(function( ele ){
                	if(ele.neighborhood().nodes().size() == 1) {
                		 ele.remove();
                	}
                });
                tidyingGraph(result_cy);
              },
          },
          {
              id: 'hide current node',
              content: 'hide current node',
              tooltipText: 'hide current node (use refresh to recover)',
              selector: 'node',
              onClickFunction: function (event) {
                var target = event.target || event.cyTarget;
                var id = target["_private"]["data"]["id"];
                var cy_node = result_cy.$id(id);
                if (!hiddenNodes[id]) {
                    hiddenNodes[id] = [];
                }
                if (!hiddenEdges[id]) {
                    hiddenEdges[id] = [];
                }
                hiddenNodes[id].push(cy_node);
                hiddenEdges[id].push(cy_node.connectedEdges());
                cy_node.remove();
                postHideNodes();
              },
          },
          {
              id: 'show only x neighbors per label',
              content: 'show only x neighbors per label',
              tooltipText: 'For each label, only display at most x neighbors',
              selector: 'node',
              onClickFunction: function (event) {
                var target = event.target || event.cyTarget;
                var id = target["_private"]["data"]["id"];
                var cy_node = result_cy.$id(id);
                var maxNeighbors = parseInt(prompt("Please enter x:", "5")) || 5;
                console.log('maxNeighbors', maxNeighbors);
                var retainedNeighbors = {};
                cy_node.neighborhood().nodes().forEach(function( ele ){
	                var label = ele._private.data.type; // type represents label in motif graph
	                if (!retainedNeighbors[label]) {
	                    retainedNeighbors[label] = 0;
	                }
	                if (retainedNeighbors[label] >= maxNeighbors) {
	                    if (!hiddenNodes[id]) {
	                        hiddenNodes[id] = [];
	                    }
	                    if (!hiddenEdges[id]) {
	                        hiddenEdges[id] = [];
	                    }
	                    hiddenNodes[id].push(ele);
                        hiddenEdges[id].push(ele.connectedEdges());
	                    ele.remove();
	                } else {
	                    retainedNeighbors[label] += 1;
	                }
                });
                postHideNodes();
              },
          },
          {
              id: 'recover all hidden neighbors',
              content: 'recover all hidden neighbors',
              tooltipText: 'recover all hidden neighbors',
              selector: 'node',
              onClickFunction: function (event) {
                var target = event.target || event.cyTarget;
                var id = target["_private"]["data"]["id"];
                var cy_node = result_cy.$id(id);
                if (hiddenNodes[id]) {
                    hiddenNodes[id].forEach(function(node) {
                        node.restore();
                    });
                    hiddenNodes[id] = [];
                }
                if (hiddenEdges[id]) {
                    console.log('hiddenEdges', hiddenEdges[id]);
                    hiddenEdges[id].forEach(function(edge) {
                        edge.restore();
                    });
                    hiddenEdges[id] = [];
                }
                postHideNodes();
              },
          }
	    ],
	    menuItemClasses: ['custom-menu-item'],
        contextMenuClasses: ['custom-context-menu']
	};

function postHideNodes(){
	var nodesWithoutEdgesLst = [];
	Object.keys(hiddenNodes).forEach(function(key) {
	    var nodes = hiddenNodes[key];
	    for (var i=0; i < nodes.length; i++) {
	    	var idStr = nodes[i]._private.data.id;
	    	nodesWithoutEdgesLst.push(parseInt(idStr.substring(1, idStr.length)));
	    }
	});
	updateDescCategory(nodesWithoutEdgesLst);
    tidyingGraph(result_cy);
}

function updateVisibilityOfNames(){
	if($("#showHideNameButton").html()=="Hide Names") {
		result_cy.elements("node").removeClass("nolabel");
	}
	else {
		result_cy.elements("node").addClass("nolabel");
	}
}

function download(filename, text) {
    var element = document.createElement('a');
    element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
    element.setAttribute('download', filename);
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
}

/**
 * On the side panel, user can search by node name
 * Corresponding node will be highlighted if it exists on result
 * @param data
 * @returns
 */
function setSearchNode() {
	var data = [];
	for (var i = 0; i < currentNodes.length; i++) {
		data.push(graphG_id2Name[currentNodes[i]]);
	}
	$("#result_search_node")
	.on( "keydown", function( event ) {
        if ( event.keyCode === $.ui.keyCode.TAB &&
            $( this ).autocomplete( "instance" ).menu.active ) {
          event.preventDefault();
        }
    })
	.autocomplete({
		source: function(request, response) {
			var terms = request.term.split('|')
			var results = $.ui.autocomplete.filter(data, terms[terms.length - 1]);
			response(results.slice(0, 8));
		},
		focus: function() {
	        // prevent value inserted on focus
	        return false;
	    },
	    select: function( event, ui ) {
	        var terms = this.value.split('|');
	        // remove the current input
	        terms.pop();
	        // add the selected item
	        terms.push( ui.item.value );
	        // add placeholder to get the | at the end
	        terms.push( "" );
	        this.value = terms.join( "|" );
	        return false;
	    }
	});
}

/**
 * On the side panel, use slide bar to change minimum edge weight
 * @param value: a number between [0, 1]
 * @returns
 */
function EdgeWeightThresholdOnChange(value) {
	document.getElementById("edge_weight_threshold_number").innerHTML = value;
	if (removedNodes) {
		removedNodes.restore();
	}
	if (removedEdges) {
		removedEdges.restore();
	}
	removedEdges = result_cy.remove('edge[weight < ' + value + ']');
	var nodesWithoutEdges;
	if (result_choosenGraph === -1) {
	    // in preview mode, simply hide nodes if they are not connected to any other node
	    nodesWithoutEdges = result_cy.nodes().filter(node => node.connectedEdges(":visible").size() === 0);
	} else {
	    // in mclique display mode, if an edge is removed, we need to remove nodes iteratively, until the remaining graph
	    // becomes a valid motif-clique.
	    // This is a difficult problem, and it will take much time to do the iterative removal and check, so I propose a
	    // simple and effective method (for simple motif). That is, when defining the motif, we record how many neighbours
	    // each node has.
	    // For example, the motif is drug - gene - disease
	    // then motifEdges === 2 for all gene nodes
	    // In the result graph, if the number of visible connected edges for a gene node is fewer than 2, we remove the node
	    // TODO: this method, however, is not sufficient enough. It works for simple motif (i.e. each label only appears once)
	    nodesWithoutEdges = result_cy.nodes().filter(node => node._private.data.motifEdges > 0 // When motif is not given, motifEdges === 0
	        && node.connectedEdges(":visible").size() < node._private.data.motifEdges);
	}
	removedNodes = result_cy.remove(nodesWithoutEdges);
	var nodesWithoutEdgesLst = [];
	for (var i=0; i < nodesWithoutEdges.length; i++) {
		var idStr = nodesWithoutEdges[i]._private.data.id;
		nodesWithoutEdgesLst.push(parseInt(idStr.substring(1, idStr.length)));
	}
	updateDescCategory(nodesWithoutEdgesLst);
}

/**
 * On the side panel, click on search icon to search and highlight node
 * @returns
 */
function searchNode() {
	// text can be a single name, or multiple names separated by \t
	// obviously, autocomplete feature only works when users type only one name
	removeSearchHighlights();
	currentSearchedNodes = [];
	var txt = $('#result_search_node').val().split('|');
	console.log('nodes to be highlighted based on search', txt);
	for (var i=0;i<currentNodes.length;i++) {
		var idx = txt.indexOf(graphG_id2Name[currentNodes[i]]);
		if (idx >= 0) {
			currentSearchedNodes.push(currentNodes[i]);
			rerenderNode(result_cy, currentNodes[i], 'search-highlight');
		}
	}
}

/**
 * On the side panel, click on reset icon to remove highlighting by search result
 * @returns
 */
function resetSearchNode() {
    $('#result_search_node').val('');
    removeSearchHighlights();
}

function removeSearchHighlights() {
	for (var i=0;i<currentSearchedNodes.length;i++) {
		removeNodeClass(result_cy, currentSearchedNodes[i], 'search-highlight');
	}
	currentSearchedNodes = [];
}

function setGraphResult(){
	result_cy=putCy("#graphCanvas",true);
	$("#showHideNameButton").click(function(){
		if($("#showHideNameButton").html()=="Hide Names") {
			result_cy.elements("node").addClass("nolabel");
			$("#showHideNameButton").html("Show Names");
		}
		else {
			result_cy.elements("node").removeClass("nolabel");
			$("#showHideNameButton").html("Hide Names");
		}
	});

	$("#statisticsButton").click(function() {
	    $("#statisticsPanel").css("display", "flex");
	    // fill in overview info
	    var nodeCount = graphG.nodeNum ? graphG.nodeNum : "unknown";
	    var edgeCount = graphG.edgeNum ? graphG.edgeNum / 2 : "unknown";
	    var motifNumber = !isNaN(motifCount) ? motifCount : "unknown";

	    $("#stats1").html("Graph nodes:" + nodeCount);
	    $("#stats2").html("Graph edges:" + edgeCount);
	    $("#stats3").html("Graph motifs:" + motifNumber);
	    $("#stats4").html("Maximum cliques:" + motifNumber * 2);

	    // plot node & edge statistics
	    var x1 = [];
        var x2 = [];
        if (graphResult != null) {
            for (var i = 0; i < graphResult.length; i++) {
                var nodeNum = graphResult[i].length;
                var edgeNum = 0;
                for (var j = 0; j < nodeNum; j++) {
                    for (var k = j+1; k < nodeNum; k++) {
                        if (graphG.adjList[graphResult[i][j]].indexOf(graphResult[i][k]) != -1) {
                            edgeNum++;
                        }
                    }
                }
                x1.push(nodeNum);
                x2.push(edgeNum);
            }
            var trace1 = {
              x: x1,
              type: "histogram",
              name: "node",
              opacity: 0.5,
              marker: {
                 color: 'green',
              },
            };
            var trace2 = {
              x: x2,
              type: "histogram",
              name: "edge",
              opacity: 0.6,
              marker: {
                 color: 'red',
              },
            };

            var data1 = [trace1];
            var data2 = [trace2];
            var layout1 = {
                xaxis: {title: "number of nodes"},
                yaxis: {title: "number of maximal m-cliques"}
            };
            var layout2 = {
                xaxis: {title: "number of edges"},
                yaxis: {title: "number of maximal m-cliques"}
            };
            Plotly.newPlot("node-stats-plot", data1, layout1);
            Plotly.newPlot("edge-stats-plot", data2, layout2);
        }
	});
	
	$("#importButton").click(function(){
		document.getElementById("upload_mclique").click();
		
		document.getElementById("upload_mclique").onchange = function() {
			var file=document.getElementById("upload_mclique").files[0];
			var reader = new FileReader();
			reader.readAsText(file);
			
			reader.onload = function(e) {
				  var text = reader.result;
				  // console.log('text', text);
				  var graph_lst = [];
				  var raw_lines = text.split("\n");
				  var nodes_size = parseInt(raw_lines[0].split(":")[1]);
				  if (isNaN(nodes_size)) {
				    nodes_size = raw_lines.length;
				  } else {
				    raw_lines.shift(); // remove first line (# of nodes: xx)
				  }
				  for (var i=0; i < nodes_size; i++) {
					  // label:node_id:node_name
					  var node_id = raw_lines[i].split(':')[1];
					  graph_lst.push(node_id);
				  }
				  if (!graphResult) {
					  graphResult = [];
				  }
				  graphResult.unshift(graph_lst);
				  result_choosenGraph = 0;
				  drawGraphResult();
			}
		}

	});

	$("#exportButton").click(function(){
		var exportResults = [];
		var nodes = currentNodes;
		// console.log('graph result', nodes);
		function node2Str(id) {
		    var node_name = graphG_id2Name[id];
		    var label = graphG_labelToType[graphG["labels"][id]];
		    return label + ':' + id + ':' + node_name;
		}
		// retrieve label and name of nodes
		for(var i = 0, size = nodes.length; i < size; i++){
			var node = nodes[i];
			exportResults.push(node2Str(node));
		}
		var text = "# of nodes:" + nodes.length + "\n";
		exportResults = exportResults.sort();
		for (var i=0, size=nodes.length; i<size; i++) {
			text += exportResults[i] + "\n";
		}
		// retrieve edge information (we don't need them for deserialization, but it would
		// be good to have edge information exported, so that the graph can be recovered without our system)
		var edges = [];
		for (var i = 0; i < nodes.length; i++) {
		    for (var j = 0; j < nodes.length; j++) {
		        node_i = parseInt(nodes[i]);
		        node_j = parseInt(nodes[j]);
		        if (graphG["adjList"][node_i].indexOf(node_j) >= 0) {
		            if (graphG["labels"][node_i] < graphG["labels"][node_j] ||
		                graphG["labels"][node_i] == graphG["labels"][node_j] && node_i < node_j) {
		                edges.push(node2Str(node_i) + '\t' + node2Str(node_j));
		            }
		        }
		    }
		}
        text += "# of edges:" + edges.length + "\n";
        edges = edges.sort();
        for (var i = 0; i < edges.length; i++) {
            text += edges[i] + "\n";
        }
	    var filename = "mclique_" + (result_choosenGraph + 1) + ".txt";
        filename = prompt("Please enter file name:", filename);
        if (filename) {
	        download(filename, text);
        }
	});

	$("#saveButton").click(function(){
		var png64 = result_cy.png();
		var b64key = 'base64,';
		var b64 = png64.substring( png64.indexOf(b64key) + b64key.length );
		var imgBlob = base64ToBlob( b64, 'image/png' );
	    var filename = "mclique_" + (result_choosenGraph + 1) + ".png";
        filename = prompt("Please enter image name:", filename);
        if (filename) {
		    saveAs(imgBlob, filename);
        }

	});
	
	$("#arrangeButton").click(function(){
	    drawGraphResult();
	});

	setZoomGraphResultButton()
}

function setZoomGraphResultButton() {
	$("#largeButton").off();
	$("#smallButton").off();
	
	var x = $("#graphHolder").width()/2.0;
	var y = $("#graphHolder").height()/2.0;
	var position = {x:x, y:y};
	
	$("#largeButton").click(function(){
		var zoomLevel=result_cy.zoom()*1.25;
		result_cy.animate({zoom:zoomLevel, center:position},{duration:500});
	});

	$("#smallButton").click(function(){
		var zoomLevel=result_cy.zoom()*0.8;
		result_cy.animate({zoom:zoomLevel, center:position},{duration:500});
	});
}



function drawGraphResult(){
    $("#graph-refresh-button").css("display", "none");
    if (!graphResult) {
        console.log('refresh in preview mode');
        $(".previewButton").click();
        return;
    }
	console.log("drawGraphResult start");
	document.getElementById("edge_weight_threshold_slider").value = 0;
	document.getElementById("edge_weight_threshold_number").innerHTML = '0';
	removedNodes = null;
	removedEdges = null;
	hiddenNodes = {};
	hiddenEdges = {};

	var nodes = graphResult[result_choosenGraph];
	var adjList = {}, weightAdjList = {};
	descCategory = null;
	currentNodes = [];
	highlightedCategory = [];
	for(var i = 0, size = nodes.length; i < size; i++) {
		var list = graphG["adjList"][nodes[i]];
		var temp = [];
		var weightTemp = [];
		for(var j = 0, size2 = list.length; j < size2; j++) {
			var index = -1;
			for (var k = 0; k < size && index == -1; k++) {
				if (list[j] == nodes[k])
					index = k	;
			}
			if (index != -1) {
				temp.push(nodes[index]);
				weightTemp.push(graphG.adjWeightList[nodes[i]][j]);
			}
		}
		adjList[nodes[i]]= temp;
		weightAdjList[nodes[i]] = weightTemp;
	}
	emptyingGraphResult();
    result_cy.contextMenus(graphResult_options);
	for(var i = 0, size = nodes.length; i < size; i++){
		var node = nodes[i];
		var label = graphG_id2Name[node].length >= 24 ? graphG_id2Name[node].substring(0, 18) + "..." : graphG_id2Name[node];
		descCategory = nodeToDescCategory(descCategory, node);
		currentNodes.push(node);
		addNode(result_cy, graphG["labels"][node],0,0,node,label,graphG_id2Name[node]+" ("+graphG_labelToType[graphG["labels"][node]]+")", graphG_id2Desc[node],
				/* node summary */ graphG['node2Summary'][node]);
	}
	
	for(var i = 0, size = nodes.length; i < size; i++){
		var node = nodes[i];
		for(var j = 0, size2 = adjList[node].length; j < size2; j++){
			if (result_cy.filter('edge[source = "n' + node + '"][target= "n' + adjList[node][j] + '"]').length == 0 &&
					result_cy.filter('edge[source = "n' + adjList[node][j] + '"][target= "n' + node + '"]').length == 0) {
				addEdge(result_cy,"n"+ node, "n"+adjList[node][j],result_edge_counter,weightAdjList[node][j]);
				result_edge_counter++;
			}
		}
	}
	displayDescCategory(descCategory);
	setSearchNode();
	tidyingGraph(result_cy);
	setGraphTitle("add");
}

function emptyingGraphResult(){
	displayDescCategory(null);
	setSearchNode();
	result_cy.remove(result_cy.elements("node"));
	$("#showHideNameButton").html("Hide Names");
	result_edge_counter=0;
	result_cy.contextMenus('get').destroy();
}

/**
 * Recalculate and display category info on the side panel when needed
 * This function is invoked when sliding bar is changed
 * 
 */
function updateDescCategory(nodesWithoutEdges) {
	var set = new Set(nodesWithoutEdges);
	var category = null;
	for (var i=0; i<currentNodes.length;i++) {
		if (!set.has(currentNodes[i])) {
			category = nodeToDescCategory(category, currentNodes[i]);
		}
	}
	displayDescCategory(category);
	descCategory = category;
}

/**
 * graphG_id2Desc is an object consisting of id -> description mappings
 * each description has several categories subdescriptions, splitted by '|' symbol
 * @param descCategory: an 2-d array where each row represents a list of (desc, [nodeId,]) pairs
 * @param nodeId: global nodeId
 * @returns a new descCategory
 */
function nodeToDescCategory(descCategory, nodeId)
{
	if (!graphG_id2Desc[nodeId]) {
		// some node does not have descriptions
		return descCategory;
	}
	var descriptions = graphG_id2Desc[nodeId].split('|');
	if (!descCategory) {
		descCategory = [];
		for (var i=0; i < descriptions.length; i++) {
			descCategory.push([]);
		}
	}
	for (var i=0; i < descriptions.length; i++) {
		var desc = descriptions[i].toLowerCase();
		if (desc) {
			// some node may miss certain category data
			var exist = false;
			for (var j=0; j < descCategory[i].length; j++) {
				if (descCategory[i][j][0] === desc) {
					descCategory[i][j][1].push(nodeId);
					exist = true;
				}
			}
			if (!exist) {
				descCategory[i].push([desc, [nodeId]]);
			}
			descCategory[i].sort((x, y) => y[1].length - x[1].length);
		}
	}
	return descCategory;
}

function displayDescCategory(descCategory) {
	if (!descCategory) {
		document.getElementById("graphCategoryList").innerHTML = '';
	} else {
		var contentStr = '';
		for (var i=0; i < descCategory.length; i++) {
			contentStr += '<div>';
			contentStr += '<p style="font-size: 14px"><b>Property: ' + graphG.categoryList[i] + '</b></p>';
			for (var j=0; j < descCategory[i].length; j++) {
				var desc = descCategory[i][j][0];
				var count = descCategory[i][j][1].length;
				var idStr = i + '|' + j;
				contentStr += '<div class="category-item" id="' + idStr + '" style="font-size: 12px;'
				if (highlightedCategory[desc]) {
				    contentStr += 'color: red;';
				}
				contentStr += '" onClick="highlightNodes(' + i + ',' + j + ')">';
				contentStr += '<div style="flex:1">' + desc + '</div>';
				contentStr += '<div style="align-self:end">' + count + '</div>'; 
				contentStr += '</div>';
			}
			contentStr += '</div>';
		}
		document.getElementById("graphCategoryList").innerHTML = contentStr;
	}
}

function highlightNodes(i, j) {
	var textElem = document.getElementById(i + '|' + j);
	var desc = descCategory[i][j][0];
	// Note that we must use description rather than i,j as the key
	// this is because when we hide or remove some nodes, i and j will change accordingly
	if (highlightedCategory[desc]) {
		textElem.style.color = 'black';
	} else {
		textElem.style.color = 'red';
	}
	var highlighted;
	if (highlightedCategory[desc]) {
		highlighted = false;
		highlightedCategory[desc] = null;
	} else {
		highlighted = true;
		highlightedCategory[desc] = true;
	}
	console.log('highlight', descCategory[i][j][1]);
	// we don't want to hightlight or de-highlight any node that does not have the same type as nodes in descCategory
	// for example, we may have 3 types: gene, disease, and drug. Gene nodes have subtypes (i.e. category), when
	// we select a category and highlight those nodes, we only want to de-highlight other gene nodes and 
	// we don't want disease and drug nodes get affected.
	// var nodes = [];
	// for (var k=0; k < currentNodes.length; k++) {
	//	if (graphG_id2Desc[currentNodes[k]]) {
			// if a node does not have category, graphG_id2Desc[nodeId] entry will be null
			// thus, this is not accurate if some nodes have categories while others not
			// in the previous example, if some gene nodes have category info while others not,
			// the rendering will not be accurate. It is strongly recommended if a certain type
			// of nodes have category info, then all nodes of that type should provide category
			// information.
	//		nodes.push(currentNodes[k]);
	//	}
	// }
	// console.log('hightlight and dehightlight nodes', nodes);
	if (highlighted) {
		// highlight selected nodes
		// for (var k = 0; k < nodes.length; k++) {
		//	rerenderNode(result_cy, nodes[k], 'non-highlight');
		// }
		for (var k = 0; k < descCategory[i][j][1].length; k++) {
			rerenderNode(result_cy, descCategory[i][j][1][k], 'highlight');
		}		
	} else {
		// remove highlighting
		for (var k = 0; k < descCategory[i][j][1].length; k++) {
			removeNodeClass(result_cy, descCategory[i][j][1][k], 'highlight');
			// removeNodeClass(result_cy, nodes[k], 'non-highlight');
		}
	}

}

function graphToResult(adjList, labels, labelToType, id2Name, selected)
{
	console.log('graphToResult starts');
	hiddenNodes = {};
	hiddenEdges = {};
	removedEdges = null;
	removedNodes = null;
	descCategory = null;
	highlightedCategory = [];
	currentNodes = [];
	if(adjList.length > 0) {
		emptyingGraphResult();
		result_cy.contextMenus(graphResult_options);
		var label = graphG_id2Name[selected].length >= 24 ? graphG_id2Name[selected].substring(0,18) + "..." : graphG_id2Name[selected];
		descCategory = nodeToDescCategory(descCategory, selected);
		currentNodes.push(selected);
		addNode(result_cy, labels[selected],0,0,selected, label, id2Name[selected]+" ("+labelToType[labels[selected]]+")", graphG_id2Desc[selected],
				/* node summary */ graphG['node2Summary'][selected]);
		for(var j=0, size = adjList[selected].length; j<size;j++){
			var otherNode = adjList[selected][j];
			if(result_cy.filter('node[id="n'+ otherNode + '"]').length == 0) {
				var label = graphG_id2Name[otherNode].length >= 24 ? graphG_id2Name[otherNode].substring(0,18) + "..." : graphG_id2Name[otherNode];
				currentNodes.push(otherNode);
				addNode(result_cy, labels[otherNode],0,0,otherNode, label, id2Name[otherNode]+" ("+labelToType[labels[otherNode]]+")", graphG_id2Desc[otherNode],
						/* node summary */ graphG['node2Summary'][otherNode]);
				descCategory = nodeToDescCategory(descCategory, otherNode);
			}
			if (result_cy.filter('edge[source = "n' + selected + '"][target= "n' + otherNode + '"]').length == 0 &&
					result_cy.filter('edge[source = "n' + otherNode + '"][target= "n' + selected + '"]').length == 0) {
				addEdge(result_cy,"n"+ selected, "n"+ otherNode,result_edge_counter,graphG.adjWeightList[selected][j]);// TODO: be careful about j! (this is correct only if adjList == graphG.adjList)
				result_edge_counter++;
			}
	
		}
		tidyingGraph(result_cy);
		setGraphTitle();
	}
	else {
		alert("the graph is empty")	
	}
	displayDescCategory(descCategory);
	setSearchNode();
}



function graphToResultExtension(adjList, labels, labelToType, id2Name, clicked) {
	console.log('graphToResultExtension starts');
	for(var j=0, size = adjList[clicked].length; j<size;j++){

		var otherNode = adjList[clicked][j];
		if(result_cy.filter('node[id="n'+ otherNode + '"]').length == 0) {
			var label = graphG_id2Name[otherNode].length >= 24 ? graphG_id2Name[otherNode].substring(0,18) + "..." : graphG_id2Name[otherNode];
			descCategory = nodeToDescCategory(descCategory, otherNode);
			currentNodes.push(otherNode);
			addNode(result_cy, labels[otherNode],0,0,otherNode, label, id2Name[otherNode]+" ("+labelToType[labels[otherNode]]+")", graphG_id2Desc[otherNode],
					/* node summary */ graphG['node2Summary'][otherNode]);
		}
		if (result_cy.filter('edge[source = "n' + clicked + '"][target= "n' + otherNode + '"]').length == 0 &&
				result_cy.filter('edge[source = "n' + otherNode + '"][target= "n' + clicked + '"]').length == 0) {
			addEdge(result_cy,"n"+ clicked, "n"+ otherNode,result_edge_counter, graphG.adjWeightList[clicked][j]);
			result_edge_counter++;
		}
	}
	updateVisibilityOfNames();
	tidyingGraph(result_cy);
	displayDescCategory(descCategory);
	setSearchNode();
}


/*
function printDiv(divToPrint) 
{
  var newWin=window.open('','Print-Window');

  newWin.document.open();

  newWin.document.write('<html><body onload="window.print()">'+divToPrint.innerHTML+'</body></html>');

  newWin.document.close();

  setTimeout(function(){newWin.close();},10);

}
 */

