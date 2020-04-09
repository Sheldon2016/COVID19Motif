var stopSearching = false;
var search_limit = 1000;
var query_combine_type = -1;
var query_weight_lower_bound = 0;
var descCategory = null;
var currentNodes = [];
// nodes on the current graph and are specified by search box on side panel
var currentSearchedNodes = [];
var highlightedCategory = {};

// when using sliding bar, some edges will be removed
// the removed result will be stored in removedEdges
// so that they can be easily recovered
var removedNodes = null;
var removedEdges = null;

// right click on a node to hide it
var hiddenNodes = {};
var hiddenEdges = {};

var motifCount = null;

// sortingCriteria:
// 0: First Found (Default)
// 1: Sort by number of nodes in result (descending)
// 2: Sort by percentage of nodes with same subtype (descending)
var sorting_criteria = 0;

var app_name;

var selected_clique_type = 0; // 0 for motif-clique, 1 for traditional clique

// 1 for disease subtyping; 2 for drug mechanism investigation; 3 for drug repurposing
var medicine_mode = 1;

var is_searching = false;

var file_name;

var cola_params = {
	      name: "cola",
	      nodeSpacing: 30,
	      edgeLengthVal: 45,
	      animate: true,
	      randomize: false,
	      avoidOverlap: true,
	      maxSimulationTime: 1500,
	      edgeLength: function(e) {
	  		return 40;
	  	  }
	};

var spread_params = {
    name: 'concentric',
    minNodeSpacing: 30
}

function groupBy() {
	Array.prototype.groupBy = function(prop) {
		return this.reduce(function(groups, item) {
			const
			val = item[prop]
			groups[val] = groups[val] || []
			groups[val].push(item)
			return groups
		}, {})
	};
}

function showLoading(isEnabled) {
	if (isEnabled)
		$("#loader").css("display", "inline-block");
	else
		$("#loader").css("display", "none");
}

function enableAllButton(isEnable) {
	$("#preset_previewButton").prop('disabled', !isEnable);
	$("#preset_setButton").prop('disabled', !isEnable);
	$("#upload_uploadButton").prop('disabled', !isEnable);
	$("#upload_previewButton").prop('disabled', !isEnable);
	$("#maximallCliquesButton").prop('disabled', !isEnable);
	$("#nextButton").prop('disabled', !isEnable);
	$("#previousButton").prop('disabled', !isEnable);
}

function putCy(location, isResizable) {
	return cytoscape({

		style : cytoscape.stylesheet().selector("edge").css({

			"curve-style" : "bezier",
			"width" : 1,
			"line-color" : "#999999",
		}).selector("node").css({
			"content" : "data(label)",
			"color" : "#999999",
			"font-size" : "12px",
			"font-weight" : "bold"
		}).selector(".node0").css({
			"background-color" : "#02fc87",
			"color" : "#1b4f72",
		}).selector(".node1").css({
			"background-color" : "#ff8c1a",
			"color" : "#1b4f72"
		}).selector(".node2").css({
			"background-color" : "#1a8cff",
			"color" : "#1b4f72"
		}).selector(".node3").css({
			"background-color" : "#c61aff",
			"color" : "#1b4f72"
		}).selector(".node4").css({
			"background-color" : "#ff1a8c",
			"color" : "#1b4f72"
		}).selector("node:selected").css({
			"overlay-color" : "#bfbfbf",
			"overlay-opacity" : 0.5,
			"overlay-padding" : 10
		}).selector("node.nolabel").css({
			"content" : ""
		}).selector(".node-highlight").css({
			"background-color": "red",
			"color": "red"
		}).selector(".node-non-highlight").css({
			"background-color": "black",
			"color": "black"
		}).selector(".node-search-highlight").css({
			"shape": "star"
		})

		,
		container : $(location), // container to render in
		zoomingEnabled : isResizable,
		panningEnabled : true,
		userZoomingEnabled : false
	});
}

function addEdge(cy, s, t, c, weight) {
	if (!weight) {
		w = 1;
	} else {
		w = weight;
	}
	cy.add([ {
		group : "edges",
		data : {
			id : "e" + c,
			source : s,
			target : t,
			weight : w,
			class : "edge"
		}
	} ]);
	if (weight) {
		var t = makeTippy(cy.$("#e" + c), weight);
		cy.$("#e" + c).on('mouseover', function() {
			t.show();
		});
		cy.$("#e" + c).on('mouseout', function() {
			t.hide();
		});
	}
}

function addNode(cy, type, x, y, c, label, tooltip, tooltipExtra, summary, identifier) {
    var numberOfEdgesInMotif = graphM_adjList['n' + type] ? graphM_adjList['n' + type].length : 0;
	cy.add({
		group : "nodes",
		data : {
			id : "n" + c,
			label : label,
			type: type,
			identifier : identifier,
			motifEdges: numberOfEdgesInMotif,
		},
		position : {
			x : x,
			y : y
		}
	});

	if (tooltipExtra) {
		tooltip += '[' + tooltipExtra + ']';
	}
	if (tooltip != null) {
		var t = makeTippy(cy.$("#n" + c), tooltip);

		cy.$("#n" + c).on('mouseover', function() {
			t.show();
		})
		cy.$("#n" + c).on('mouseout', function() {
			t.hide();
		})

	}
	
	if (summary) {
		var t2 = makeTippy(cy.$("#n" + c), summary);
		
		cy.$("#n" + c).on('mousedown', function() {
			t2.show();
			if (t) {
			    t.hide();
			}
		})
		cy.$("#n" + c).on('mouseup', function() {
			t2.hide();
		})
	}

	cy.$("#n" + c).addClass("node" + type)
}

function rerenderNode(cy, nodeId, type, oldType) {
	if (oldType) {
		removeNodeClass(cy, nodeId, oldType);
	}
	cy.$("#n" + nodeId).addClass("node-" + type);
}

function removeNodeClass(cy, nodeId, type) {
	cy.$("#n" + nodeId).removeClass("node-" + type);
}

function tidyingGraph(cy) {
	var spread_layout = cy.layout(spread_params);
	spread_layout.run();
//	var cola_layout = cy.layout(cola_params);
//	cola_layout.run();
}

function makeTippy(node, text) {
	return tippy(node.popperRef(), {
		html : (function() {
			var div = document.createElement('div');
			div.innerHTML = text;
			return div;
		})(),
		trigger : 'manual',
		arrow : true,
		placement : 'right',
		hideOnClick : false,
		multiple : false,
		sticky : true,
		interactive: true,
		maxWidth: '600px'
	}).tooltips[0];
};

function printdiv(printpage) {
	var newstr = printpage.innerHTML;
	var oldstr = document.body.innerHTML;
	document.body.innerHTML = newstr;
	window.print();
	document.body.innerHTML = oldstr;
	return false;
}

function removeGraphTitle() {
	$("#graphTitle").html("");
}

function setGraphTitle(action) {
	if (action == "add") {
		if (graphResult != null) {

			var theTh = "th";
			var numberShown = result_choosenGraph + 1;
			var remainder = numberShown % 100;
			if (remainder < 11 || remainder > 20) {
				if (remainder % 10 == 1)
					theTh = "st";
				else if (remainder % 10 == 2)
					theTh = "nd";
				else if (remainder % 10 == 3)
					theTh = "rd";
			}
			var str = "<div contenteditable id='editable-graph-number' style='display:inline'>"
			        + numberShown + "</div>"
			        + theTh + " out of "
					+ graphResult.length;
			$("#graphTitle").html(str);
			document.getElementById("editable-graph-number").addEventListener("input", function() {
			    number = parseInt($("#editable-graph-number").text());
			    // parseInt returns null if input is not valid number
			    if (number) {
			        result_choosenGraph = number - 1;
			        drawGraphResult();
			    }
            }, false);
		}
	} else if (action == "setGraph") {
		$("#graphTitle").html(graphG_label);
	} else
		$("#graphTitle").html("");
}