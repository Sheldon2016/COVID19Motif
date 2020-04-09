var graphG_labelToType={};
var graphG_id2Name=[];
var graphG={};
var graphG_chosen=-1;
var graphG_mode = null;
var graphG_randomLabel_toOrdered = {};
var graphG_label = null;

function emptyingGraphG(){

	graphG={};
	graphG_id2Name=[];
	graphG_id2Desc=[];
	graphG_labelToType={};
	result_cy.remove(result_cy.elements("node"));
	result_edge_counter=0;
	result_node_counter=0;
    graphM_motifs = [];
    graphM_motif_index = 0;
	emptyingGraphM();
	emptyingGraphResult();
	$("#query_contain").prop('max', 0);
	$("#query_contain").val("");
	$('#query_isContain').prop('checked', false);
	$("#query_not_contain").prop('max', 0);
	$("#query_not_contain").val("");
	$('#query_isNotContain').prop('checked', false);
	graphG_mode = null;
	graphG_chosen = -1;
	graphG_label = null;
	setGraphTitle(null);
}

function setGraphFromFile(link){
	graphG_mode="default";
    discoverMotifSilently();
	$.get(link, function (data) {
		data = data.replace(/\r/g,'');
		var lines = data.split('\n');
		line = lines[0].split('\t');
		var counter = 1;
		var num_of_node = parseInt(line[0]);
		var num_of_edge = parseInt(line[1]);
		var num_of_label = parseInt(line[2]); 

		var edges = [];
		var adjList = [];
		var adjWeightList = [];
		var categoryList = [];
		var node2Summary = {};
		var summaryList = [];
		var labels = [];
		var items = [];

		for(var i=counter, size=counter + num_of_node; i<size;i++)
		{
			var line = lines[i].split('\t');
			adjList.push([]);
			adjWeightList.push([]);
			graphG_id2Name.push(line[1]);
			if (line.length >= 3) {
				graphG_id2Desc.push(line[2]);
			} else {
				graphG_id2Desc.push(null);
			}
		}


		counter += num_of_node;
		for(var i = counter, size = counter + num_of_edge; i<size;i++)
		{
			var line = lines[i].split('\t');
			var edge1 = [];
			edge1.push(parseInt(line[0]));
			edge1.push(parseInt(line[1]));

			var edge2 = [];
			edge2.push(parseInt(line[1]));
			edge2.push(parseInt(line[0]));


			edges.push(edge1);
			edges.push(edge2);

			adjList[parseInt(line[0])].push(parseInt(line[1]));
			adjList[parseInt(line[1])].push(parseInt(line[0]));
			
			if (line.length >= 3) {
				adjWeightList[parseInt(line[0])].push(parseFloat(line[2]));
				adjWeightList[parseInt(line[1])].push(parseFloat(line[2]));
			} else {
				adjWeightList[parseInt(line[0])].push(null);
				adjWeightList[parseInt(line[1])].push(null);
			}
		}

		counter += num_of_edge;
		var k = 0;
		for(var i = counter, size = counter + num_of_label; i<size; i++)
		{
			var line = lines[i].split('\t');
			graphG_labelToType[parseInt(line[0])]=line[1];
			graphG_randomLabel_toOrdered[parseInt(line[0])] = k;
			doAdd(parseInt(line[0]));
			k++;
		}

		counter += num_of_label;
		for(var i = counter, size = counter + num_of_node; i<size; i++)
		{
			labels.push(parseInt(lines[i]));
		}
		
		// if node has categorical information, we need to know category names
		// TODO: currently, we assume categories for all nodes are the same
		// this works fine if there is only one type of nodes that have categorical info
		// but is (almost always) not true if different types have categorical info
		counter += num_of_node;
		if (lines[counter]) {
			categoryList = lines[counter].split('|');
		}
		
		// if node have detailed description, or other info that want to display
		// on UI (say, click on the node and display info)
		counter += 1;
		if (lines[counter]) {
			num_of_summary = parseInt(lines[counter]);
			counter += 1;
			for (var i = counter, size = counter + num_of_summary; i < size; i++) {
				var node = lines[i].split('\t')[0];
				var summary = lines[i].split('\t')[1];
				node2Summary[node] = summary;
			}
		}
		
		function doAdd(val){
			items.push({
				id: graphG_randomLabel_toOrdered[val],
				content: 'add node #' + graphG_labelToType[val],
				tooltipText: graphG_labelToType[val],
				image: {src : "icon/"+graphG_randomLabel_toOrdered[val]+".svg", width : 12, height : 12, x : 5, y : 7},
				coreAsWell: true,
				onClickFunction:function (event) {
					var pos = event.position || event.cyPosition;
					addNode(graphM_cy, graphG_randomLabel_toOrdered[val], pos.x, pos.y, graphM_node_counter, graphG_labelToType[val],null);
					var s = "n"+graphM_node_counter;
					graphM_nodes.push(s);
					graphM_labels[s]=val;
					graphM_adjList[s]=[];
					graphM_node_counter++;
				}
			});
		}

		graphM_addItemsToInstance(items);
		//var graph = {};
		graphG["nodeNum"] = num_of_node;
		graphG["edgeNum"] = 2 * num_of_edge;
		graphG["adjList"] = adjList;
		graphG["adjWeightList"] = adjWeightList;
		graphG["edges"] = edges;
		graphG["labels"] = labels;
		graphG["categoryList"] = categoryList;
		graphG["node2Summary"] = node2Summary;
		var maxNodeId = num_of_node-1;
		setQueryContain(graphG_id2Name);
		console.log("done setGraph");
		enableAllButton(true);
		showLoading(false);
	}, 'text');
}

function setGraphFromString(data){
	data = data.replace(/\r/g,'');
	var lines = data.split('\n');
	line = lines[0].split('\t');
	var counter = 1;
	var num_of_node = parseInt(line[0]);
	var num_of_edge = parseInt(line[1]);
	var num_of_label = parseInt(line[2]); 

	var edges = [];
	var adjList = [];
	var adjWeightList = [];
	var categoryList = [];
	var node2Summary = {};
	var labels = [];
	var items = [];

	for(var i=counter, size=counter + num_of_node; i<size;i++)
	{
		var line = lines[i].split('\t');
		adjList.push([]);
		adjWeightList.push([]);
		graphG_id2Name.push(line[1]);
		if (line.length >= 3) {
			graphG_id2Desc.push(line[2]);
		} else {
			graphG_id2Desc.push(null);
		}
	}

	counter += num_of_node;
	for(var i = counter, size = counter + num_of_edge; i<size;i++)
	{
		var line = lines[i].split('\t');
		var edge1 = [];
		edge1.push(parseInt(line[0]));
		edge1.push(parseInt(line[1]));

		var edge2 = [];
		edge2.push(parseInt(line[1]));
		edge2.push(parseInt(line[0]));


		edges.push(edge1);
		edges.push(edge2);

		adjList[parseInt(line[0])].push(parseInt(line[1]));
		adjList[parseInt(line[1])].push(parseInt(line[0]));
		
		if (line.length >= 3) {
			adjWeightList[parseInt(line[0])].push(parseFloat(line[2]));
			adjWeightList[parseInt(line[1])].push(parseFloat(line[2]));
		} else {
			adjWeightList[parseInt(line[0])].push(null);
			adjWeightList[parseInt(line[1])].push(null);
		}
	}

	counter += num_of_edge;
	var k = 0;
	for(var i = counter, size = counter + num_of_label; i<size; i++)
	{
		var line = lines[i].split('\t');
		graphG_labelToType[parseInt(line[0])]=line[1];
		graphG_randomLabel_toOrdered[parseInt(line[0])] = k;
		doAdd(parseInt(line[0]));
		k++;
	}

	counter += num_of_label;
	for(var i = counter, size = counter + num_of_node; i<size; i++)
	{
		labels.push(parseInt(lines[i]));
	}
	
	// if node has categorical information, we need to know category names
	// TODO: currently, we assume categories for all nodes are the same
	// this works fine if there is only one type of nodes that have categorical info
	// but is (almost always) not true if different types have categorical info
	counter += num_of_node;
	if (lines[counter]) {
		categoryList = lines[counter].split('|');
	}
	
	// if node have detailed description, or other info that want to display
	// on UI (say, click on the node and display info)
	counter += 1;
	if (lines[counter]) {
		num_of_summary = parseInt(lines[counter]);
		counter += 1;
		for (var i = counter, size = counter + num_of_summary; i < size; i++) {
			var node = lines[i].split('\t')[0];
			var summary = lines[i].split('\t')[1];
			node2Summary[node] = summary;
		}
	}

	function doAdd(val){
		items.push({
			id: graphG_randomLabel_toOrdered[val],
			content: 'add node #' + graphG_labelToType[val],
			tooltipText: graphG_labelToType[val],
			image: {src : "icon/"+graphG_randomLabel_toOrdered[val]+".svg", width : 12, height : 12, x : 5, y : 7},
			coreAsWell: true,
			onClickFunction:function (event) {
				var pos = event.position || event.cyPosition;
				addNode(graphM_cy, graphG_randomLabel_toOrdered[val], pos.x, pos.y, graphM_node_counter, graphG_labelToType[val],null);
				var s = "n"+graphM_node_counter;
				graphM_nodes.push(s);
				graphM_labels[s]=val;
				graphM_adjList[s]=[];
				graphM_node_counter++;
			}
		});
	}

	graphM_addItemsToInstance(items);
	graphG["nodeNum"] = num_of_node;
	graphG["edgeNum"] = 2 * num_of_edge;
	graphG["adjList"] = adjList;
	graphG["adjWeightList"] = adjWeightList;
	graphG["categoryList"] = categoryList;
	graphG["node2Summary"] = node2Summary;
	graphG["edges"] = edges;
	graphG["labels"] = labels;
	var maxNodeId = num_of_node-1;
	setQueryContain(graphG_id2Name);
	console.log("done setGraph");
	graphG_mode="upload";
	enableAllButton(true);
	showLoading(false);
}

function setData1(){
	graphG_chosen=1;
	setGraphFromFile('file/input1-DBLP.txt');
	graphG_label = "Graph from DBLP (4 areas)";
}

function setData2(){
	graphG_chosen=2;
	setGraphFromFile('file/input2-amazon.txt');
	graphG_label = "Graph from Amazon";
}

function setData3(){
	graphG_chosen=3;
	setGraphFromFile('file/input3-movielens.txt');
	graphG_label = "Graph from MovieLens";
}

function setData4(){
	graphG_chosen=4;
	setGraphFromFile('file/input4-DrugBank(1).txt');
	graphG_label = "Graph from Drugbank + Disgenet + PantherDB + refseq";
}

function setData5(){
	graphG_chosen=5;
	setGraphFromFile('file/input5-DrugBank(2).txt');
	graphG_label = "Graph from Drugbank + Disgenet + GeneOntology + refseq";
}


function setGraphG(){
	$("#preset_setButton").click(function(){
		emptyingGraphG();
		emptyingGraphResult();
		setGraphTitle(null);
		graphResult=null;
		selected=$("#preset_inputSet").val();
		enableAllButton(false);
		showLoading(true);

		if(selected==1){
			setData1();
			
		}
		else if(selected==2){
			setData2();
		}
		else if(selected==3){
			setData3();
		}
		else if(selected==4) {
		    setData4();
		}
		else if(selected==5) {
		    setData5();
		}
	})

	$(".previewButton").click(function(){
		enableAllButton(false);
		var msg = "";
		var index = Object.values(graphG_id2Name).indexOf($("#query_contain").val());
		if (typeof graphG["adjList"] === 'undefined' || graphG["adjList"].length <= 0 ||
				typeof graphG["edges"] === 'undefined' && graphG["edges"].length <= 0 ||
				typeof graphG["labels"] === 'undefined' && graphG["labels"].length <= 0      ) {
			msg += "The graph G is empty\n";

		}
		if ($("#query_isContain").is(":checked"))
			if (index == -1)
				msg += "The node's name is not valid.";

		if (msg) {
			alert(msg);
		}
		else {
			
			if ($("#query_isContain").is(":checked")) {
				graphToResult(graphG["adjList"], graphG["labels"], graphG_labelToType, 
						graphG_id2Name, index);
			}
			else {
				graphToResult(graphG["adjList"], graphG["labels"], graphG_labelToType, 
						graphG_id2Name, Math.floor(Math.random() * graphG["nodeNum"]));
			}
		}
		enableAllButton(true);
		setGraphTitle("setGraph");
	})

	$("#upload_uploadButton").click(function(){
		enableAllButton(false);
		showLoading(true);

		var file=document.getElementById("upload_browseFile").files[0];

		file_name = file["name"].replace(".txt", "");
		var reader = new FileReader();
		reader.readAsText(file);
		
		reader.onload = function(e) {
			  var text = reader.result;
			  emptyingGraphG();
			  graphG_label="Graph from file upload";
			  setGraphFromString(text);
		}

		var fd = new FormData();
		fd.append("file", file);
		$.ajax({
			url: "uploadGraph.jsp",
			data: fd,
			processData: false,
			contentType: false,
			type: 'POST',
			success: function(data){
				console.log("upload graph success");
				// var init=data.search("<body>");
				// var end=data.search("</body>");
				// var graphGInString=data.substring(init+11,end-1);
				// emptyingGraphG();
				// graphG_label="Graph from file upload";
				// setGraphFromString(graphGInString);
			}
		});
	});

	console.log("done setGraphG");
}
