<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ page import="main.Website"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<!--<script src="pkg/cytoscape.min.js"></script>-->
<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
<script src="https://unpkg.com/webcola/WebCola/cola.min.js"></script>
<script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<link
	href="https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-bootstrap/0.5pre/css/custom-theme/jquery-ui-1.10.0.custom.css"
	rel="stylesheet" />

<title>MC-Explorer - motif based graph analysis platform</title>
<script>
	var now = Date.now();
	document
			.write('<script type="text/javascript" src="pkg/jquery-3.3.1.min.js?t="'
					+ now + '"> <\/script>');
	document.write('<script type="text/javascript" src="pkg/cytoscape.js?t="'+ now + '"> <\/script>');
	document
			.write('<script type="text/javascript" src="pkg/bootstrap.bundle.js?t="'
					+ now + '"> <\/script>');
	document
			.write('<script type="text/javascript" src="pkg/cytoscape-cola.js?t="'
					+ now + '"> <\/script>');
	document
			.write('<script type="text/javascript" src="pkg/cytoscape-context-menus.js?t="'
					+ now + '"> <\/script>');
	document.write('<script type="text/javascript" src="pkg/popper.min.js?t="'
			+ now + '"> <\/script>');
	document.write('<script type="text/javascript" src="pkg/tippy.all.js?t="'
			+ now + '"> <\/script>');
	document
			.write('<script type="text/javascript" src="pkg/cytoscape-popper.js?t="'
					+ now + '"> <\/script>');
	document.write('<script type="text/javascript" src="pkg/FileSaver.js?t="'
			+ now + '"> <\/script>');
	//			document.write('<script type="text/javascript" src="pkg/Blob.js?t="'+ now + '"> <\/script>');
	document
			.write('<script type="text/javascript" src="pkg/base64toblob.js?t="'
					+ now + '"> <\/script>');
	document
			.write('<script type="text/javascript" src="pkg/jquery-ui.min.js?t="'
	+ now + '"> <\/script>');
	document
			.write('<script type="text/javascript" src="pkg/weaver.min.js?t="'
					+ now + '"> <\/script>');
	document
			.write('<script type="text/javascript" src="pkg/cytoscape-spread.js?t="'
					+ now + '"> <\/script>');
	//		document.write('<script type="text/javascript" src="pkg/canvas-toBlob.js?t="'+ now + '"> <\/script>');
	document.write('<script type="text/javascript" src="js/index.js?t="' + now
			+ '"> <\/script>');
	document.write('<script type="text/javascript" src="js/graphG.js?t="' + now
			+ '"> <\/script>');
	document.write('<script type="text/javascript" src="js/graphM.js?t="' + now
			+ '"> <\/script>');
	document.write('<script type="text/javascript" src="js/graphResult.js?t="'
			+ now + '"> <\/script>');
	document.write('<script type="text/javascript" src="js/query.js?t="' + now
			+ '"> <\/script>');

	document.write('<script type="text/javascript" src="js/utilities.js?t="'
			+ now + '"> <\/script>');
	document
			.write('<link rel="stylesheet" type="text/css" href="pkg/bootstrap.css?t='
					+ now + '"\/>');
	document
			.write('<link rel="stylesheet" type="text/css" href="pkg/cytoscape-context-menus.css?t='
					+ now + '"\/>');
	document
			.write('<link rel="stylesheet" type="text/css" href="css/index.css?t='
					+ now + '"\/>');
</script>
</head>
<body data-appname='<%= request.getParameter("app") %>'>

	<%
		Website website = new Website();
	%>
	<div id="config-modal" class="modal">
 		<div class="modal-content">
			<span class="close" id="config-close">&times;</span>
			<p>Enter the limit of cliques you want to find</p>
			<input class="form-control autocomplete" type="number" id="query_search_limit"
				   style="margin-bottom: 10px" value="1000"/>
			<p>Enter the lower bound for weighted edges (edge with lower weight will be filtered out)</p>
			<input class="form-control autocomplete" type="number" id="query_weight_lower_bound"
				   style="margin-bottom: 10px" value="0" min="0" max="1" step="0.1"/>
			<p>Enter the name of node you want the result to INCLUDE (optional)</p>
			<div class="form-group">
				<div class="input-group mb-2">
					<div class="input-group-prepend">
						<div class="input-group-text">
							<input type="checkbox"
								aria-label="Checkbox for following text input"
								id="query_isContain">
						</div>
					</div>
					<input class="form-control autocomplete" id="query_contain"
						placeholder="Enter the node's name..." />
				</div>
			</div>
			<p>Enter the name of node you want the result to EXCLUDE (optional)</p>
			<div class="form-group">
				<div class="input-group mb-2">
					<div class="input-group-prepend">
						<div class="input-group-text">
							<input type="checkbox"
								aria-label="Checkbox for following text input"
								id="query_isNotContain">
						</div>
					</div>
					<input class="form-control autocomplete" id="query_not_contain"
						placeholder="Enter the node's name..." />
				</div>
			</div>
			<div id="combine_container">
				<p>Select the type to combine m-cliques sharing a same node with a certain type (optional)</p>
				<div class="form-group">
					<div class="input-group mb-2">
						<div class="input-group-prepend">
							<div class="input-group-text">
								<input type="checkbox"
									aria-label="Checkbox for following text input"
									id="query_isCombine">
							</div>
						</div>
						<div id="combine_type_holder" style="flex: 1 1 auto; position:relative; width:1%"></div>
					</div>
				</div>
			</div>
			<div id="range_holder"></div>
			<div id="display_order_container">
				<p>Choose results display sorting criteria</p>
				<select id="query_display_order" style="width:100%; height:100%">
					<option value="0">First Found (Default)</option>
					<option value="1">Sort by number of nodes in result (descending)</option>
					<!--<option value="2" disabled>Sort by percentage of nodes with same subtype (descending) (not supported)</option>-->
				</select>
			</div>
		</div>
	</div>

	<div id="graphGSelector" class="modal">
 		<div class="modal-content">
			<span class="close" id="graphG-close">&times;</span>
            <ul class="nav nav-tabs nav-fill" id="myTab" role="tablist">
    	    	<li class="nav-item"><a class="nav-link active" id="home-tab"
    	    		data-toggle="tab" href="#preset" role="tab" aria-controls="preset"
    	    		aria-selected="true">Preset Graph</a></li>
    	    	<li class="nav-item"><a class="nav-link" id="upload-tab"
    	    		data-toggle="tab" href="#upload" role="tab" aria-controls="upload"
    	    		aria-selected="false">Upload</a></li>
    	    </ul>
    	    <div class="tab-content" id="myTabContent">
    	    	<div class="tab-pane fade show active" id="preset" role="tabpanel"
    	    		aria-labelledby="preset-tab">
    	    		<select id="preset_inputSet" class="form-control">
    	    			<option value=0>Select Dataset</option>
    	    			<option value=1>DBLP (4 areas)</option>
    	    			<option value=2>Amazon</option>
    	    			<option value=3>MovieLens</option>
    	    			<option value=4>DrugBank(1)</option>
    	    			<option value=5>DrugBank(2)</option>
    	    		</select>
    	    		<button type="button" class="btn btn-primary" id="preset_setButton">Set</button>
    	    		<button type="button" class="btn btn-secondary previewButton"
    	    			id="preset_previewButton">Preview graph</button>
    	    	</div>
    	    	<div class="tab-pane fade" id="upload" role="tabpanel"
    	    		aria-labelledby="upload-tab">
    	    		<form id="upload_uploadForm">
    	    			<div class="form-group">
    	    			    <p>
    	    				    <input type="file" class="form-control-file"
    	    					    id="upload_browseFile" accept=".txt">
    	    					<a href="file/readme.txt" target="_blank"><i class="fa fa-question-circle input-icon"></i></a>
    	    			    </p>
    	    				<button type="button" class="btn btn-primary"
    	    					id="upload_uploadButton">Upload</button>
    	    				<button type="button" type="submit" class="btn btn-secondary previewButton"
    	    					id="upload_previewButton">Preview graph</button>
    	    			</div>
    	    		</form>
    	    	</div>
    	    </div>
    	</div>
    </div>


	<div id="graphMSelector" class="modal">
 		<div class="modal-content">
			<span class="close" id="graphM-close">&times;</span>
		    <div id="canvasHolder"></div>
		    <div class="float-left">
		        <button class="btn btn-primary dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                  Motif Discovery
                </button>
                <div class="dropdown-menu" aria-labelledby="dropdownMenuButton" x-placement="top-start">
                  <a class="dropdown-item" href="#" id="graphMSelector_find_frequent_motif">Find motif with highest frequency</a>
                  <a class="dropdown-item" href="#" id="graphMSelector_find_label_motif">Find motif with label constraints</a>
                </div>
		    	<button type="button" class="btn btn-secondary btn"
		    		id="graphMSelector_arrangeButton">Arrange</button>
		    	<button type="button" class="btn btn-secondary btn"
		    		id="graphMSelector_cleanButton">Clean</button>
		    </div>
		    <!--
		    <div class="btn-group float-right  btn-group-justified" role="group"
		    	aria-label="Basic example">
		    	<button type="button" class="btn btn-primary" id="zoomoutButton">-</button>
		    	<button type="button" class="btn btn-primary" id="zoominButton">+</button>
		    </div>
		    -->
		</div>
	</div>


	<div id="statisticsPanel" class="auto-modal">
 		<div class="auto-modal-content">
			<span class="close" id="stats-close">&times;</span>
            <ul class="nav nav-tabs nav-fill" id="myTab" role="tablist">
    	    	<li class="nav-item"><a class="nav-link active"
    	    		data-toggle="tab" href="#overview-tab" role="tab"
    	    		aria-selected="true">Graph Statistics</a></li>
    	    	<li class="nav-item"><a class="nav-link"
    	    		data-toggle="tab" href="#node-tab" role="tab"
    	    		aria-selected="false">Node Distribution</a></li>
    	    	<li class="nav-item"><a class="nav-link"
    	    		data-toggle="tab" href="#edge-tab" role="tab"
    	    		aria-selected="false">Edge Distribution</a></li>
    	    </ul>
    	    <div class="tab-content" id="myTabContent">
    	    	<div class="tab-pane fade show active" id="overview-tab" role="tabpanel">
    	    	    <div id="textbox" style="margin: 5px 5px;">
                      <p class="alignleft" style="float: left;" id="stats1"></p>
                      <p class="alignright" style="float: right;" id="stats2"></p>
                      <div style="clear: both;"></div>
                    </div>
     	    	    <div id="textbox" style="margin: 5px 5px;">
                      <p class="alignleft" style="float: left;" id="stats3"></p>
                      <p class="alignright" style="float: right;" id="stats4"></p>
                      <div style="clear: both;"></div>
                    </div>
    	    	</div>
    	    	<div class="tab-pane fade" id="node-tab" role="tabpanel">
    	    	    <div id="node-stats-plot"></div>
    	    	</div>
    	    	<div class="tab-pane fade" id="edge-tab" role="tabpanel">
    	    	    <div id="edge-stats-plot"></div>
    	    	</div>
   	    	    </div>
    	    </div>
    	</div>
    </div>


	<div id="main">
        <nav class="navbar navbar-expand-lg navbar-light bg-light">
          <a class="navbar-brand" href="#">MC-Explorer</a>
          <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>

          <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav mr-auto">

<c:choose>
    <c:when test="${param.app == 'medicine'}">
              <li class="nav-item dropdown">
                <a class="nav-link dropdown-toggle" href="#" style="color:black" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                  Mode
                </a>
                <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                  <a class="dropdown-item" id="mode-option-one" href="#">Disease Subtyping</a>
                  <a class="dropdown-item" id="mode-option-two" href="#">Drug Mechanism Investigation</a>
                  <a class="dropdown-item" id="mode-option-three" href="#">Drug Repurposing</a>
                </div>
              </li>
              <li class="nav-item" id="configButton">
                <a class="nav-link" style="color:black" href="#">Config</a>
              </li>
    </c:when>
    <c:otherwise>
              <li class="nav-item" id="graphGButton">
                <a class="nav-link" style="color:black" href="#">Step 1: Select Dataset</a>
              </li>
              <li class="nav-item" id="graphMButton">
                <a class="nav-link" style="color:black" href="#">Step 2: Enter Motif</a>
              </li>
              <li class="nav-item" id="configButton">
                <a class="nav-link" style="color:black" href="#">Step 3: Set Config</a>
              </li>
    </c:otherwise>
</c:choose>
            </ul>
            <ul class="navbar-nav my-2 my-sm-0">
              <li class="nav-item dropdown my-2 my-sm-0">
                <a class="nav-link dropdown-toggle" href="#" id="clique-selected-option" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                  Motif-Clique
                </a>
                <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                  <a class="dropdown-item" id="clique-alternative-option" href="#">Traditional clique</a>
                </div>
              </li>
            </ul>
            <button class="btn btn-outline-success my-2 my-sm-0" id="toggle_search" type="submit">Search</button>
          </div>
        </nav>

    	<div id="graphContainer">
    		<div id="graphHolder">
    			<div id="graphTitle"></div>
    			<div id="graphBoard">
    				<div id="graphCanvas"></div>
    				<div id="graphSidePanel">
    					<p><center>Side Panel</center></p>
    					<div style="display:flex">
    						<input class="form-control" id="result_search_node" style="flex:3; font-size:11px"
    							placeholder="Search node by name.."/>
    						<button style="margin:0px 1px; border-radius:5px" id="result_search_node_confirm"
    							onClick="searchNode();">
    							<i class="fa fa-search"></i>
    						</button>
    						<button style="margin:0px 1px; border-radius:5px" id="result_search_node_reset"
    							onClick="resetSearchNode();">
    							<i class="fa fa-eraser"></i>
    						</button>
    					</div>

    					<label class="label label-default" for="edge_weight_threshold_slider" style="margin-top: 5px; font-size: 12px">
    						Minimum Edge Weight: <div id="edge_weight_threshold_number" style="display:inline">0</div>
    					</label>
    					<input id="edge_weight_threshold_slider" type="range" min="0" max="1" step="0.01" value="0" style="width:100%"
    						oninput="EdgeWeightThresholdOnChange(this.value)">

    					<div id="graphCategoryList"></div>
    				</div>
    			</div>
    			<div id="graphNavBar">
    				<div class="float-left cursorHand" id="resetButton">Reset</div>
    				<div class="float-left cursorHand margin_left_20" id="showHideNameButton">Hide
    					Names</div>
    				<div class="float-left cursorHand margin_left_20" id="previousButton">
    				    Previous</div>
    				<div class="float-left cursorHand margin_left_20" id="nextButton">
    				    Next</div>
    				<div class="float-left cursorHand margin_left_20" id="arrangeButton">
    				    Refresh
    					<div id="graph-refresh-button" class="notification-dot"></div>
    				</div>
    				<div class="float-left cursorHand margin_left_20" id="statisticsButton">
    				    Statistics</div>
    	            <div id="loader"></div>
    				<!--   					<div class="float-right"><img class="navBarButton cursorHand" src="icon/print.png" id="printButton"></div>    -->
    				<div class="float-right">
    					<input type="file" class="form-control-file"
    								id="upload_mclique" style="display:none;" accept=".txt">
    					<img class="navBarButton cursorHand" src="icon/upload.png"
    						id="importButton">
    				</div>
    				<div class="float-right">
    					<img class="navBarButton cursorHand" src="icon/export.png"
    						id="exportButton">
    				</div>
    				<div class="float-right">
    					<img class="navBarButton cursorHand" src="icon/save.png"
    						id="saveButton">
    				</div>
    				<div class="float-right">
    					<img class="navBarButton cursorHand" src="icon/large.png"
    						id="largeButton">
    				</div>
    				<div class="float-right">
    					<img class="navBarButton cursorHand" src="icon/small.png"
    						id="smallButton">
    				</div>
    			</div>
    		</div>
    	</div>
	    <p>©2019 Department of Computer Science, The University of Hong Kong. All Right Reserved.</p>
	</div>
	<script>
		startAll();
	</script>
</body>
</html>