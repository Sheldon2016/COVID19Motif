var cy;

function btn_click() {
  var modal = document.getElementById("graphMSelector");
  modal.style.display = "block";

  cy = window.cy = cytoscape({
    container: document.getElementById('canvasHolder'),

    zoomingEnabled : true,
    panningEnabled : true,
    userZoomingEnabled : false,

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
        style: {

        }
      }
    ],
    layout: {
      name: 'breadthfirst'
    },
  });

  var selectAllOfTheSameType = function(ele) {
    cy.elements().unselect();
    if(ele.isNode()) {
      cy.nodes().select();
    }
    else if(ele.isEdge()) {
      cy.edges().select();
    }
  };
  var context_options ={
    menuItems: [
      {
        id: 'remove',
        content: 'remove',
        tooltipText: 'remove',
        image: {src : "img/remove.svg", width : 12, height : 12, x : 6, y : 4},
        selector: 'node, edge',
        onClickFunction: function (event) {
          var target = event.target || event.cyTarget;
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
          var target = event.target || event.cyTarget;
          target.hide();
        },
        disabled: false
      },
      {
        id: 'remove-selected',
        content: 'remove selected',
        tooltipText: 'remove selected',
        image: {src : "img/remove.svg", width : 12, height : 12, x : 6, y : 6},
        coreAsWell: true,
        onClickFunction: function (event) {
          cy.$(':selected').remove();
        }
      },
      {
        id: 'select-all-nodes',
        content: 'select all nodes',
        tooltipText: 'select all nodes',
        selector: 'node',
        onClickFunction: function (event) {
          selectAllOfTheSameType(event.target || event.cyTarget);
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
        image: {src : "img/add.svg", width : 12, height : 12, x : 5, y : 7},

        selector: 'node:selected',
        onClickFunction: function (event) {
          var target=cy.$('node:selected');
          if(target.length!=2)
          alert("Need to select 2 nodes");
          else{
            var s=target[0]['_private']['data']['id'];
            var t=target[1]['_private']['data']['id'];
            cy.add([ {
              group : "edges",
              data : {
                source : s,
                target : t,
                class : "edge"
              }
            } ]);
          }
        },
        hasTrailingDivider: true
      }
    ]};
    // demo your core ext
    var context_menu_instance = cy.contextMenus(context_options);
    host = 'neo4j://'+document.getElementById("host").value;
    port = parseInt(document.getElementById("port").value);
    user = document.getElementById("user").value;
    password = document.getElementById("password").value;
    driver = neo4j.driver(host, neo4j.auth.basic(user, password));
    setConnectionManagementStatus("success", "Connected" + " to " + host + " on port " + port + " as user " + user);
    setConnected(true);
    var session10 = driver.session();
    var i = 0;
    session10.run("MATCH (n) RETURN DISTINCT LABELS(n)")  .subscribe({
      onNext: record => {
        i+=1;
        console.log(i);
        context_menu_instance.appendMenuItem(
          { id : i,
            content: record.get(0)[0],
            coreAsWell: true,
            onClickFunction: function (event) {
              var data = {
                group: 'nodes',
                name: record.get(0)[0],
              };

              var pos = event.position || event.cyPosition;

              cy.add({
                data: data,
                position: {
                  x: pos.x,
                  y: pos.y
                }
              });
            }


          }
        );
      },

    });
	
  }

  // When the user clicks on <span> (x), close the modal
  var sources = [];
  var targets = [];
  function span_click() {
    var modal = document.getElementById("graphMSelector");
    modal.style.display = "none";
	var allEdges = cy.edges();
	//var res = "Confirm motif edges:\n";
    // alert(all.length);
	
    for (var i = 0; i < allEdges.length; i++) {
      the_edge = allEdges[i];
	  var source = cy.getElementById(the_edge['_private']['data']['source']);
      var target = cy.getElementById(the_edge['_private']['data']['target']);
	  var sourceName = source['_private']['data'];//['id'];
	  var targetName = target['_private']['data'];//['id'];
	  sources.push (sourceName);
	  targets.push (targetName);
	  //res+="source:"+sources[i]+" target:"+targets[i]+"\n";
    }

    var modal = document.getElementById("graphMSelector");
    modal.style.display = "none";
	//alert(res);
  }
  function clean_up_motif(){
    cy.remove(cy.elements("node"));
}
