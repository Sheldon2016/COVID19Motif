function start_mclique_search(){
    var msg = "";
	var index = graphG_id2Name.indexOf($("#query_contain").val());
	if(app_name !== 'medicine' && !isGraphMConnected())
		msg += "The motif is not connected.\n";
	if($("#query_isContain").is(":checked") && index == -1) {
		msg += "The node's name is not valid.";
	}
	if(msg)
		alert(msg);
	else {
		if(!$("#query_isContain").is(":checked")) {
			runMClique(-1);
		}
		else {
			runMClique(index);
		}
	}
}

function start_traditional_clique_search(){
	if (graphG_mode != null){
		if($("#query_isContain").is(":checked")){
			var index = graphG_id2Name.indexOf($("#query_contain").val());
			if(index == -1) {
				alert("The node's name is not valid.");
			}
			else {
				runTClique(index);
			}
		}
		else {
			runTClique(-1);
		}
	}
}

function start_search() {
   if (selected_clique_type) {
       console.log('start searching traditional cliques');
       start_traditional_clique_search();
   } else {
       console.log('start searching motif cliques');
       start_mclique_search();
   }
}

function stop_search() {
	stopSearching = true;
	var txt= "command=maxClique&mode=" + graphG_mode + "&mustContain=-1";
	sendMCliqueRequest(txt);
	showLoading(false);
};


function setQueryButton(){
    $("#toggle_search").click(function(){
        if (!is_searching) {
            $("#toggle_search").text("Stop");
            start_search();
            is_searching = true;
        } else {
            $("#toggle_search").text("Start");
            stop_search();
            is_searching = false;
        }
    });

	$("#clique-alternative-option").click(function(){
	    selected_clique_type = !selected_clique_type;
	    $("#clique-alternative-option").text(selected_clique_type ? "Motif-clique" : "Traditional Clique");
	    $("#clique-selected-option").text(selected_clique_type ? "Traditional Clique" : "Motif-clique");
	});

	$("#graphGButton").click(function(){
	    $("#graphGSelector").css("display", "block");
	});

	$("#graphMButton").click(function(){
	    $("#graphMSelector").css("visibility", "visible");
	});

	$("#configButton").click(function(){
		$("#config-modal").css("display", "block");
		
		// display selectbox so that user can specify combine type
		if (Object.keys(graphG_labelToType).length) {
			$('#combine_container').show();
			var typeHolderStr = '';
			typeHolderStr += '<select id="query_combine_type" style="width:100%; height:100%">\n';
			var choicesStr = '';
			for (var i in graphG_labelToType) {
			    if (i === query_combine_type) {
				    choicesStr += '<option value="' + i + '" selected>' + graphG_labelToType[i] + '</option>\n';
			    } else {
				    choicesStr += '<option value="' + i + '">' + graphG_labelToType[i] + '</option>\n';
				}
			}
			typeHolderStr += choicesStr;
			typeHolderStr += '</select>\n';
			document.getElementById("combine_type_holder").innerHTML = typeHolderStr;
		} else {
			$('#combine_container').hide();
		}

	    if (!document.getElementById("range_holder").innerHTML) {
		    // let users specify upper bounds for number of nodes
		    var contentStr = '';
		    if (Object.keys(graphG_labelToType).length) {
		    	contentStr += '<p>Specify the range for number of nodes with certain type in the result (optional)</p>';
		    }
		    for (var i in graphG_labelToType) {
		    	contentStr += '<div class="form-group">\n';
		    	contentStr += '<div class="input-group mb-2" style="display:flex">\n';
		    	contentStr += '<p style="flex:1">' + graphG_labelToType[i] + '</p>\n';
		    	contentStr += '<input style="flex:1" id="query_lowerBound_' + i + '" class="form-control" type="number" min="0" value="0"/>';
		    	contentStr += '<input style="flex:1" id="query_upperBound_' + i + '" class="form-control" type="number" min="0"/>';
		    	contentStr += '</div>\n';
		    	contentStr += '</div>\n';
		    }
		    document.getElementById("range_holder").innerHTML = contentStr;
		}
	});

	$("#config-close").click(function(){
		$("#config-modal").css("display", "none");
		// when closing the modal, update search limit value
		search_limit = $('#query_search_limit').val();
		
		query_weight_lower_bound = $('#query_weight_lower_bound').val();
		
		var e = document.getElementById("query_combine_type");
		if (e) {
			query_combine_type = e.options[e.selectedIndex].value;
		}
		
		var e_order = document.getElementById("query_display_order");
		sorting_criteria = e_order.options[e_order.selectedIndex].value;
	});

	$("#graphG-close").click(function(){
		$("#graphGSelector").css("display", "none");
	});

	$("#graphM-close").click(function(){
		$("#graphMSelector").css("visibility", "hidden");
	});

	$("#stats-close").click(function(){
        $("#statisticsPanel").css("display", "none");
	});


	$("#resetButton").click(function(){
		emptyingGraphResult();
		stopSearching = true;
		var txt= "command=maxClique&mode=" + graphG_mode + "&mustContain=-1";
		sendMCliqueRequest(txt);
		graphResult = null;
		setGraphTitle(null);
		showLoading(false);
	});

	$("#previousButton").click(function(){
		if(graphResult != null) {
			console.log("previousButtonClicked");
			var size = graphResult.length;
			result_choosenGraph = (result_choosenGraph + size - 1) % size;
			drawGraphResult();
		}
	});

	$("#nextButton").click(function(){
		if(graphResult != null) {
			var size = graphResult.length;
			result_choosenGraph = (result_choosenGraph + 1 ) % size;
			drawGraphResult();
		}
	});


    // disease subtyping
	$("#mode-option-one").click(function(){
	    clear_medicine_mode();
	    medicine_mode = 1;
	    $("#query_isCombine").prop('checked', true);
	    query_combine_type = "1"; // combine disease
	    $("#mode-option-one").addClass("active");
	    $('#query_lowerBound_0').val(1); // drug
	    $('#query_upperBound_0').val(1); // drug
	    $('#query_lowerBound_1').val(1); // disease
	    $('#query_upperBound_1').val(1); // disease
	    $('#query_lowerBound_2').val(5); // gene
	});

    // drug mechanism
	$("#mode-option-two").click(function(){
	    clear_medicine_mode();
	    medicine_mode = 2;
	    $("#mode-option-two").addClass("active");
	    $('#query_lowerBound_0').val(1); // drug
	    $('#query_upperBound_0').val(1); // drug
	    $('#query_lowerBound_1').val(2); // disease
	    $('#query_upperBound_1').val(2); // disease
	    $('#query_lowerBound_2').val(5); // gene
	    $('#query_display_order').val(1);
	});

    // drug repurposing
	$("#mode-option-three").click(function(){
	    clear_medicine_mode();
	    medicine_mode = 3;
	    $("#mode-option-three").addClass("active");
	    $('#query_lowerBound_0').val(2); // drug
	    $('#query_upperBound_0').val(2); // drug
	    $('#query_lowerBound_1').val(1); // disease
	    $('#query_upperBound_1').val(1); // disease
	    $('#query_lowerBound_2').val(5); // gene
	    $('#query_display_order').val(1);
	});
}

function clear_medicine_mode() {
	$("#mode-option-one").removeClass("active");
	$("#mode-option-two").removeClass("active");
	$("#mode-option-three").removeClass("active");
	$("#query_isCombine").prop('checked', false);
	query_combine_type = -1;
}

function setQueryContain(data) {
	$("#query_contain").autocomplete({
		source: function(request, response) {
			var results = $.ui.autocomplete.filter(data, request.term);

			response(results.slice(0, 8));
		}
	});
	$("#query_not_contain")
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

function runMClique(mustContain)
{	
	enableAllButton(false);
	showLoading(true);

	var nodeToIndex = {};
	var data = {"nodeNum":graphM_nodes.length, "edgeNum":0, "adjList":[], "labels":[] };

    if (app_name === 'medicine') {
        console.log('App is medicine, use default motif data');
        data={"nodeNum":3,"edgeNum":4,"adjList":[[2],[2],[1,0]],"labels":[0,1,2]}
    } else {
	    for(var i = 0, size = graphM_nodes.length; i < size; i++) {
	    	nodeToIndex[graphM_nodes[i]] = i;
	    	data["labels"].push(graphM_labels[graphM_nodes[i]]);
	    }

	    for (key in graphM_adjList) {
	    	var list = graphM_adjList[key];
	    	var size = list.length;
	    	data["edgeNum"] += size;
	    	var tempList = [];
	    	for(var i = 0; i < size; i++) {
	    		tempList.push(nodeToIndex[list[i]]);
	    	}
	    	data["adjList"].push(tempList);
	    }
    }
	var string = encodeURIComponent(JSON.stringify(data));
	var txt= "command=maxClique&mode=" + graphG_mode +"&which="+graphG_chosen+"&mustContain=" + mustContain +
	"&data=" + string;

	if($("#query_isNotContain").is(":checked")) {
	    txt += "&mustNotContain=";
	    $("#query_not_contain").val().split("|").forEach(function(notContain) {
	        if (notContain) {
	            txt += graphG_id2Name.indexOf(notContain) + "|";
	        }
	    });
	}

	if (!$("#query_isCombine").is(":checked")) {
		query_combine_type = '-1';
	}
	console.log('query combine type =', query_combine_type);
	txt += "&combineType=" + query_combine_type;
	
	txt += '&upperBound=';
	for (var i in graphG_labelToType) {
		var upperBoundValue = $('#query_upperBound_' + i).val();
		console.log('upperbound value for', graphG_labelToType[i], 'is', upperBoundValue);
		if (upperBoundValue) {
			txt += i + ':' + upperBoundValue + ',';
		}
	}

	txt += '&lowerBound=';
	for (var i in graphG_labelToType) {
		var lowerBoundValue = $('#query_lowerBound_' + i).val();
		console.log('lowerBound value for', graphG_labelToType[i], 'is', lowerBoundValue);
		if (lowerBoundValue) {
			txt += i + ':' + lowerBoundValue + ',';
		}
	}

	txt += '&weightLowerBound=' + query_weight_lower_bound;
	
	txt += '&sortingCriteria=' + sorting_criteria;
		

	stopSearching = false;
	sendMCliqueRequest(txt, i=0, limit=search_limit);
	enableAllButton(true);
}

function sendMCliqueRequest(txt, i, limit) {
	var query;
	if (stopSearching) {
		// this is absolutely not the best way to stop searching,
		// and it is using a global variable,
		// but it works in this small code base
		query = txt + "&state=stop";
	} else if (i === 0) {
		query = txt + "&state=start";
	} else if (i < limit) {
		query = txt + "&state=continue";
	} else {
		query = txt + "&state=stop";
	}
	console.log('query parameters are', query);
	$.ajax({url:"query.jsp?" + query,success:function(result){
		var init=result.search("<body>");
		var end=result.search("</body>");
		var maxMotifResult=result.substring(init+11,end-1);
		if(maxMotifResult.search("null") == -1)
		{
			if (!stopSearching) {
				// do not parse results if searching is already interrupted by user
				// TODO: backend should tell the frontend whether data is updated, otherwise frontend
				// has no idea whether there is no more result, or exisiting result is updated
				var jsonResult = JSON.parse(maxMotifResult);
				graphResult = jsonResult['data'] || [];
				motifCount = jsonResult['motifCount'];
				$("#graph-refresh-button").css("display", "inline-block");
			}
			console.log('graph result length =', graphResult.length);
			if (i === 0) {
				result_choosenGraph=0;
				drawGraphResult();
			} else {
				setGraphTitle("add");
			}
			if (i >= limit) {
				// this is already a request which asks searching process to stop
				showLoading(false);
			} else {
				i = graphResult.length;
				// sleep 1 second
				if (!stopSearching) {
					setTimeout(function(){
						sendMCliqueRequest(txt, i, limit);
					}, 1000);
				}
			}
		}
		else
		{
			// no (more) result
			console.log("No Result");
			showLoading(false);
		}
	}});
}

function runTClique(mustContain)
{	
	enableAllButton(false);
	showLoading(true);
	var txt= "command=traditionalClique&mode=" + graphG_mode +"&which="+graphG_chosen+"&mustContain=" + mustContain;
	txt += "&limit=" + search_limit;
	console.log('query parameters are', txt);
	$.ajax({url:"query.jsp?" + txt,success:function(result){
		var init=result.search("<body>");
		var end=result.search("</body>");
		var maxMotifResult=result.substring(init+11,end-1);
		console.log("maxMotifResult", maxMotifResult);
		if(maxMotifResult.search("null") == -1)
		{	
			graphResult = JSON.parse(maxMotifResult)["data"];
			result_choosenGraph=0;
			drawGraphResult();
		}
		else
		{
			alert("No Result");
		}
		showLoading(false);
		enableAllButton(true);
	}});
}

function discoverMotifSilently() {
   console.log("discover motif (silent mode) invoked!");
   var txt = "command=discoverMotif&mode=" + graphG_mode + "&which=" + graphG_chosen + "&silent=true";
   $.ajax({url:"query.jsp?" + txt,success:function(result){ }});
}

function discoverMotif(motifConstraint) {
    console.log("graphM_motifs is:", graphM_motifs);
    if (!graphM_motifs || !graphM_motifs.length) {
        console.log("discover motif invoked!");
        enableAllButton(false);
        showLoading(true);
        var txt = "command=discoverMotif&mode=" + graphG_mode + "&which=" + graphG_chosen;
        if (motifConstraint != null) {
            txt += "&motifConstraint=" + motifConstraint;
        }
        $.ajax({url:"query.jsp?" + txt,success:function(result){
            showLoading(false);
            enableAllButton(true);
	    	var init=result.search("<body>");
	    	var end=result.search("</body>");
            graphM_motifs = JSON.parse(result.substring(init+11,end-1));
            console.log("result = ", result.substring(init+11,end-1));
            displayMotif();
        }});
    } else {
        console.log("change to next discovered motif");
        displayMotif();
    }
}