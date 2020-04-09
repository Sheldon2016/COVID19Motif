var graphM_cy;
var graphM_edge_counter=0;
var graphM_node_counter=0;
var graphM_nodes=[];
var graphM_adjList={};
var graphM_labels={};
var graphM_instance;
var graphM_motifs=[];
var graphM_motif_index=0;
var graphM_options = {
	    // List of initial menu items
	    menuItems: [
	      {
                id: 'remove-node',
                content: 'remove',
                tooltipText: 'remove',
                selector: 'node',
                image: {src : "icon/remove.svg", width : 12, height : 12, x : 5, y : 7},
                onClickFunction: function (event) {
                  var target = event.target || event.cyTarget;
                  graphM_removeNode(target["_private"]["data"]["id"]);
                },
                hasTrailingDivider: true
          },
          {
              id: 'remove-edge',
              content: 'remove',
              tooltipText: 'remove',
              selector: 'edge',
              image: {src : "icon/remove.svg", width : 12, height : 12, x : 5, y : 7},
              onClickFunction: function (event) {
                var target = event.target || event.cyTarget;
                graphM_removeEdge(target["_private"]["data"]["id"],target["_private"]["data"]["source"],target["_private"]["data"]["target"]);
              },
              hasTrailingDivider: true
        },
          {
              id: 'add-edge',
              content: 'add edge',
              tooltipText: 'add edge',
              image: {src : "icon/add.svg", width : 12, height : 12, x : 5, y : 7},

              selector: 'node:selected',
              onClickFunction: function (event) {
            	  var target=graphM_cy.$('node:selected');
            	  	if(target.length!=2)
            	  		alert("Need to select 2 nodes")
            	  	else{
            	  		var s=target[0]["_private"]["data"]["id"];
            	  		var t=target[1]["_private"]["data"]["id"];
            	  		graphM_adjList[s].push(t);
            	  		graphM_adjList[t].push(s);
            	  		addEdge(graphM_cy,s,t,graphM_edge_counter);
            	  		graphM_edge_counter++;
            	  	}
              },
              hasTrailingDivider: true
            }
	      
	    ],
	    menuItemClasses: ['custom-menu-item'],
        contextMenuClasses: ['custom-context-menu']
	};

function graphM_addItemsToInstance(item){
    if (app_name === 'medicine') return;
	graphM_instance.appendMenuItems(item);
}

function graphM_removeItemFromInstance(itemID){
    if (app_name === 'medicine') return;
	graphM_instance.removeMenuItem(itemID);
}

function emptyingGraphM(){
    if (app_name === 'medicine') return;
	//graphM = {"nodeNum":0, "edgeNum":0, "adjList":[], "labels":[], "edges":[]};
	graphM_nodes=[];
	graphM_adjList={};
	graphM_labels={};
	graphM_cy.remove(graphM_cy.elements("node"));
	graphM_edge_counter=0;
	graphM_node_counter=0;
	graphM_nodesCollection=[];
	graphM_cy.zoom(1);
	console.log("done emptying graphM");
}

function setGraphM(){
    if (app_name === 'medicine') return;
	graphM_cy=putCy("#canvasHolder",true);
	graphM_instance=graphM_cy.contextMenus(graphM_options);
	console.log(graphM_instance.isActive());
	$("#graphMSelector_find_frequent_motif").click(function (){discoverMotif(null)});
	$("#graphMSelector_find_label_motif").click(function (){
	    var msg = "Please enter motif constraint, e.g. '1,1,2' means that motif must contain at " +
            "least 2 nodes of label '1' and 1 node of label '2'\n" + JSON.stringify(graphG_labelToType);
        var motifConstraint = prompt(msg) || null;
        console.log("entered motifConstraint is:", motifConstraint);
        graphM_motifs = [];
	    discoverMotif(motifConstraint);
	});
	$("#graphMSelector_cleanButton").click(function (){emptyingGraphM()});
	$("#graphMSelector_arrangeButton").click(function (){tidyingGraph(graphM_cy);});
	var x = $("#canvasHolder").width();
	var y = $("#canvasHolder").height();
	var position = {x:x, y:y};
	console.log(position);
	$("#zoomoutButton").click(function(){
		var zoomLevel=graphM_cy.zoom()*0.8;
		graphM_cy.animate({zoom:zoomLevel,  center: position },{duration:500});
	});
	$("#zoominButton").click(function(){
		var zoomLevel=graphM_cy.zoom()*1.25;
		graphM_cy.animate({zoom:zoomLevel ,  center: position},{duration:500});
	});
	
}

function graphM_removeNode(node)
{
    if (app_name === 'medicine') return;
	var deleted=false;
	
	for(var i=0;!deleted&&i<graphM_nodes.length;i++)
	{
		if(node==graphM_nodes[i])
		{
			deleted=true;
			graphM_nodes.splice(i,1);
		}
	}

	for(var i=0;i<graphM_adjList[node].length;i++)
	{
		
		deleted=false;
		var target=graphM_adjList[node][i];

		for(var j=0;!deleted&&j<graphM_adjList[target].length;j++)
		{
			if(node==graphM_adjList[target][j])
			{	
				deleted=true;
				graphM_adjList[target].splice(j,1);
			}
		}
	}
	
	delete graphM_adjList[node];
	delete graphM_labels[node];
	graphM_cy.remove(graphM_cy.$id(node));
}

function graphM_removeEdge(id, source, target)
{
    if (app_name === 'medicine') return;
	var deleted=false;
	
	for(var i=0;!deleted&&i<graphM_adjList[source].length;i++)
	{
		if(target==graphM_adjList[source][i])
		{
			deleted=true;
			graphM_adjList[source].splice(i,1);
		}
	}
	
	deleted=false;
	
	for(var i=0;!deleted&&i<graphM_adjList[target].length;i++)
	{
		if(source==graphM_adjList[target][i])
		{
			deleted=true;
			graphM_adjList[target].splice(i,1);
		}
	}
	graphM_cy.remove(graphM_cy.$id(id));
}

function isGraphMConnected(){
	if(graphM_nodes.length<=1)
		return false;
	var visited={};
	for(var key in graphM_adjList)
	{
		list=graphM_adjList[key];
		if(graphM_adjList.length==0)
			return false;
		visited[key]=false;
	}
	
	var candidate=[];
	candidate.push(graphM_nodes[0]);
	while(candidate.length!=0)
	{
		var currentPos=candidate.shift();
		visited[currentPos]=true;
		var list=graphM_adjList[currentPos];
		for(var i=0;i<list.length;i++ )
		{
			if(!visited[list[i]])
				candidate.push(list[i]);
		}
	}
	for(var key in visited)
		if(!visited[key])
			return false;
	return true;
}

function displayMotif() {
    console.log("total discovered motifs: ", graphM_motifs.length, "current index: ", graphM_motif_index);
    if (graphM_motifs.length == 0) return;
    var motif = graphM_motifs[graphM_motif_index];
    console.log("display motif:", motif);
    emptyingGraphM();
    graphM_node_counter = motif.nodeNum;
    graphM_edge_counter = motif.edgeNum / 2;
    graphM_adjList = {};
    graphM_labels = {};

    for (var i = 0; i < motif.labels.length; i++) {
        addNode(graphM_cy, motif.labels[i], 0, 0, i+1, graphG_labelToType[motif.labels[i]], null);
        graphM_nodes.push("n" + (i+1));
        graphM_labels["n" + (i+1)] = motif.labels[i];
    }

    var edgeCount = 0;
    for (var i = 0; i < motif.adjList.length; i++) {
        graphM_adjList['n' + (i+1)] = [];
        for (var j = 0; j < motif.adjList[i].length; j++) {
            graphM_adjList['n' + (i+1)].push('n' + (motif.adjList[i][j] + 1));
            if (i < motif.adjList[i][j] + 1) {
                edgeCount++;
                addEdge(graphM_cy, 'n' + (i+1), 'n' + (motif.adjList[i][j] + 1), edgeCount);
            }
        }
    }

    tidyingGraph(graphM_cy);
    graphM_motif_index++;
    graphM_motif_index = graphM_motif_index % graphM_motifs.length;
}

