var visulizationQuery;

function nodeValue(container, id, labels, properties) {
  var card = document.createElement("div"),
  cardHeader = document.createElement("h5"),
  cardBody = document.createElement("div");
  card.setAttribute("class", "card");
  cardHeader.setAttribute("class", "card-header");
  cardHeader.appendChild(document.createTextNode("#" + id + " " + labels));
  cardBody.setAttribute("class", "card-body");
  cardBody.appendChild(document.createTextNode(JSON.stringify(properties)));
  card.appendChild(cardHeader);
  card.appendChild(cardBody);
  return card;
}

function getcons(queryarr){
	var cons = "";
	if(queryarr.includes("WHERE")){
	  var cypherarr = queryarr.split("WHERE m.");
      var consarr = cypherarr[1];
	  cons += "WHERE "
      cons += consarr.split(" RETURN")[0];
	  cons += "\n";
	  //alert(cons);
	  var tem = cons.split("m.");
	  var con2 = "";
	  for(var i=0;i<tem.length;i++){
		  con2 += tem[i];
	  }
	  }else{
		  return cons;
	  }
	return con2;
}

function run(cypher) {
	visulizationQuery = cypher;
  let map = new Map();
  var res = "Confirm motif M:\n", resQ="", tail="", resQ2="", tail2="";
  var res2 = "Nodes in M:\n";
  var nid = 1;
  //alert(sources.length);
  for (var i = 0; i < sources.length; i++) {
	 
	 var slabel = sources[i].name.toLowerCase()+":"+sources[i].name;
	 var tlabel = targets[i].name.toLowerCase()+":"+targets[i].name;
	 
	 //var slabel="", tlabel="";
	 if(map.has(sources[i].id)){
	   //slabel = "n"+map.get(sources[i].id)+":"+sources[i].name;
	 }else{
	   map.set(sources[i].id, nid);
	   res2 += "n"+nid+":"+sources[i].name+"\n";
	   //slabel = "n"+nid+":"+sources[i].name;
	   //tail+="n"+nid+".label As m_"+sources[i].name+", ";
	   //tail2 += "n"+nid+", ";
	   tail+=sources[i].name.toLowerCase()+".label As m_"+sources[i].name+", ";
	   tail2 += sources[i].name.toLowerCase()+", ";
	   nid += 1;
	 }
	 if(map.has(targets[i].id)){
	   //tlabel = "n"+map.get(targets[i].id)+":"+targets[i].name;
	 }else{
	   map.set(targets[i].id, nid);
	   res2 += "n"+nid+":"+targets[i].name+"\n";
	   //tlabel = "n"+nid+":"+targets[i].name;
	   //tail+="n"+nid+".label As m_"+targets[i].name+", ";
	   //tail2+="n"+nid+", ";
	   tail+=targets[i].name.toLowerCase()+".label As m_"+targets[i].name+", ";
	   tail2 += targets[i].name.toLowerCase()+", ";
	   nid += 1;
	 }
	 
	 res+="Edge "+(i+1)+" ["+slabel+"--"+tlabel+"]\n";
	 resQ += "MATCH ("+slabel+")--("+tlabel+")\n";
  }
  tail = tail.substring(0,tail.length-2);
  tail2 = tail2.substring(0,tail2.length-2);
  //alert(res2);
  
  resQ += getcons(cypher);
  resQ2 = resQ;

  if(cypher.includes("COUNT"))
	resQ += "RETURN COUNT(*)+''";
  else
    resQ += "RETURN "+tail;
  //alert(resQ);
  resQ2 += "RETURN "+tail2;

  //m-cypher parser
  var cq = cypher;

  var q1="(m:M)";
  if(cypher.includes(q1)){
    //cq = "MATCH p=(:Location)<-[:from_location]-(:Strain)-[:mutate_from_branch]->(:Branch) RETURN COUNT (p)+''";
	cq = resQ;
	visulizationQuery = resQ2;
    return runonce(cq);
  }

  var q2="[m:M*]";
  if (cypher.includes(q2)) {
    var cypherarr = cypher.split("\"")
    var country1 = cypherarr[1]
    var country2 = cypherarr[3]
	
    cq = "match (p1:Location{label:\""+country1+
    "\"})--(s1:Strain)--(b1:Branch)--(b2:Branch)--(s2:Strain)--(p2:Location{label:\""+country2+
    "\"})";
	tail = " return p1.label as m1_Location, b1.label as m1_Branch1, s1.label as m1_Strain,  s2.label as m2_Strain, p2.label as m2_Country, b2.label as m2_Branch";
	tail2 = " return p1, b1, s1,  s2, p2, b2";
	visulizationQuery = cq + tail2;
	cq += tail;
    return runonce(cq);
  }
  return runonce(cq)
}

function runonce(cypher){
	alert(visulizationQuery);
  alert(cypher);
  // Clear any existing result
  var head = document.getElementById("result-head"),
  body = document.getElementById("result-body"),
  foot = document.getElementById("result-foot");
  while (head.firstChild) head.removeChild(head.firstChild);
  while (body.firstChild) body.removeChild(body.firstChild);
  while (foot.firstChild) foot.removeChild(foot.firstChild);
  var fields = [];
  var t0 = new Date();

  var icount = 1;

  $("#result-form").show();

  var session1 = driver.session();
  session1.run(cypher).subscribe({
    onKeys: keys => {
      var tr = document.createElement("tr");
      for (var j = 0; j < keys.length; j++) {
        var key = keys[j];
        fields.push(key);
        var th = document.createElement("th");
        th.appendChild(document.createTextNode(key));
        tr.appendChild(th);
        //document.getElementById("demo"+j).innerHTML = key;
        //console.log(keys)
      }
      head.appendChild(tr);
    },
    onCompleted: () => {
      session1.close() // returns a Promise
    },
    onError: error => {
      console.log(error)
    }
  });

  var count = 0;
  var session = driver.session();
  session.run(cypher).then(result => {
    result.records.forEach(record => {
      var str = "";
      count = count + 1;
      //document.getElementById("demo"+icount).innerHTML = count;
      icount = icount + 1;
      var tr = document.createElement("tr");
      for (var j = 0; j < record.length; j++) {
        var value = record.get(j);
        if (value === null) {
          content = undefined;
        }

        var td = document.createElement("td");
        var div = document.createElement("div");
        //var value = fields[i], content;
        if (value === null) {
          content = undefined;
        }else if (typeof value === "string") {
          content = document.createTextNode(value);
        }else if (typeof value === "object" && !Array.isArray(value)) {
          var type = value.constructor.name;
          switch (type) {
            case "Node":
            content = nodeValue(div, value.id, value.labels, value.properties);
            break;
            default:
            div.setAttribute("style", "white-space: pre");
            content = document.createTextNode(type + "(" + JSON.stringify(value, null, "  ") + ")");
          }
        }else {
          content = document.createTextNode(JSON.stringify(value));
        }
        if (content !== undefined)
        div.appendChild(content);
        td.appendChild(div);
        tr.appendChild(td);

        str = str + value + "-";

      }
      body.appendChild(tr);
      //document.getElementById("demo"+i).innerHTML = tr;
      //i = i + 1;
    });
    var time = new Date() - t0;
    //var tr = document.createElement("tr");
    //var th = document.createElement("th");
    //th.setAttribute("colspan", "" + fields.length);
    var server = user + "@" + host + ":" + port;
    //document.getElementById("demo0").innerHTML = 100;
    var counter = count;
    //th.appendChild(document.createTextNode(counter  + " record" + (counter === 1 ? "" : "s") + " received from " + server + " in " + time + "ms"));
    //tr.appendChild(th);
    //foot.appendChild(tr);
    setConnectionManagementStatus("success", counter  + " record" + (counter === 1 ? "" : "s") + " received from " + server + " in " + time + "ms");//console.log(str);

  }
).catch(error => {console.log(error)}).then(() => session.close());
}
