var visulizationQuery = "";

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
	if(queryarr.includes("WHERE m.")){
	  var cypherarr = queryarr.split("WHERE m.");
      var consarr = cypherarr[1];
	  cons += "WHERE "
      cons += consarr.split("RETURN")[0];
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

function returnDeal(cypher){
	var tem = cypher.split("RETURN ");
	var r = tem[1];
	var res = tem[0]+"RETURN ";
	var rtem = r.split(",");
	for(var i=0;i<rtem.length;i++){
		if(rtem[i].includes(".")){
			var stem = rtem[i].split(".");
			if(i<rtem.length-1)
				res += stem[0] +", ";
			else
				res += stem[0];
		}
	  }
	return res;
}
function run(cypher) {
  if(visulizationQuery.includes("RETURN "))
	visulizationQuery = returnDeal(cypher);
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
	   //tail+="n"+nid+".name As m_"+sources[i].name+", ";
	   //tail2 += "n"+nid+", ";
	   tail+=sources[i].name.toLowerCase()+".name As m_"+sources[i].name+", ";
	   tail2 += sources[i].name.toLowerCase()+", ";
	   nid += 1;
	 }
	 if(map.has(targets[i].id)){
	   //tlabel = "n"+map.get(targets[i].id)+":"+targets[i].name;
	 }else{
	   map.set(targets[i].id, nid);
	   res2 += "n"+nid+":"+targets[i].name+"\n";
	   //tlabel = "n"+nid+":"+targets[i].name;
	   //tail+="n"+nid+".name As m_"+targets[i].name+", ";
	   //tail2+="n"+nid+", ";
	   tail+=targets[i].name.toLowerCase()+".name As m_"+targets[i].name+", ";
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
    var location1 = cypherarr[1]
    //var location2 = cypherarr[3]
	
    cq = "match r=(p1:Location{name:\""+location1+"\"})--(s1:Strain)-[:mutate_from*..3]-(s2:Strain)--(s3:Strain)--(p2:Location)";
	tail = " return distinct p2.name as Location_name";
	tail2 = " return p1,s1,s2,s3,p2";
	visulizationQuery = cq + tail2;
	cq += tail;
    return runonce(cq);
  }
  
  var q3="MCLQ";
  if(cypher.includes(q3)){
	  cq = "match (a:Virus)--(b:HostProtein)--(c:HostProtein)--(d:Symptom)--(b) match (a)--(c) where a.label contains \"SARS\"";
	  visulizationQuery = cq+"return a,b,c,d";
	  return runonce(cq+"return collect(distinct a.name) as label_A, collect(distinct b.name) as label_B, collect(distinct d.name) as label_C")
  }
  
  var q4="MPPR";
  if(cypher.includes(q4)){
	  cq = "MATCH (v:Virus) Where v.label contains \"SARS-CoV-2\" CALL gds.pageRank.stream(\'covid19udppr\', \{  maxIterations: 2000000,  dampingFactor: 0.85,  sourceNodes: [v]\}) YIELD nodeId, score with gds.util.asNode(nodeId) as node, score as score_raw with labels(node) as ls, node.name as p, round(score_raw*10000)/10000 as score with ls[0] as l, p, score where l = \"Drug\" and score > 0 RETURN p AS name, score ORDER BY score DESC, name ASC";
	  //console.log(cq);
	  return runonce(cq);
  }
  
  var q5="MDIS";
  if(cypher.includes(q5)){
	  var sname = "";
	  var slabel = "";
	  if(cypher.includes("WHERE")){
		  var temw = cypher.split("WHERE");
		  var temn = temw[1].split("\"");
		  sname = temn[1];
		  var teml = temw[0].split(":");
		  slabel = teml[1].split(")")[0];
	  }
	  var tem = cypher.split("MDIS");
	  var tem2 = tem[1].split("\"");
	  var Ls = [];
	  Ls.push(tem2[1]);
	  Ls.push(tem2[3]);
	  Ls.push(tem2[5]);
	  Ls.push(tem2[7]);
	  //var Ls = ["Drug","Virus","VirusProtein","HostProtein"];//["Drug","Virus","Disease","Symptom"];
	  if(sname.length>0){
		  var svariant = ["a","b","c","d"];
		  for(var i=0;i<Ls.length;i++){
			  if(Ls[i] === slabel){
				  Ls[i] = Ls[i]+"{name:\""+sname+"\"}"
			  }
		  }
		}
		cq = "match r=(a:"+Ls[0]+")--(b:"+Ls[1]+") \n return labels(a)[0]+\"-\"+labels(b)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[0]+")--(b:"+Ls[2]+")  \n return labels(a)[0]+\"-\"+labels(b)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[0]+")--(b:"+Ls[3]+")  \n return labels(a)[0]+\"-\"+labels(b)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[1]+")--(b:"+Ls[2]+")  \n return labels(a)[0]+\"-\"+labels(b)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[1]+")--(b:"+Ls[3]+")  \n return labels(a)[0]+\"-\"+labels(b)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[2]+")--(b:"+Ls[3]+")  \n return labels(a)[0]+\"-\"+labels(b)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[0]+")--(b:"+Ls[1]+")--(c:"+Ls[2]+")  \n  return labels(a)[0]+\"-\"+labels(b)[0]+\"-\"+labels(c)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[0]+")--(b:"+Ls[1]+")--(c:"+Ls[3]+")  \n  return labels(a)[0]+\"-\"+labels(b)[0]+\"-\"+labels(c)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[1]+")--(b:"+Ls[2]+")--(c:"+Ls[3]+")  \n  return labels(a)[0]+\"-\"+labels(b)[0]+\"-\"+labels(c)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[1]+")--(b:"+Ls[2]+")--(c:"+Ls[3]+")  \n  return labels(a)[0]+\"-\"+labels(b)[0]+\"-\"+labels(c)[0] as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[0]+")--(b:"+Ls[1]+")--(c:"+Ls[2]+")--(a)  \n return labels(a)[0]+\"-\"+labels(b)[0]+\"-\"+labels(c)[0]+\"-\" as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[0]+")--(b:"+Ls[1]+")--(c:"+Ls[3]+")--(a)  \n   return labels(a)[0]+\"-\"+labels(b)[0]+\"-\"+labels(c)[0]+\"-\" as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[1]+")--(b:"+Ls[2]+")--(c:"+Ls[3]+")--(a)  \n   return labels(a)[0]+\"-\"+labels(b)[0]+\"-\"+labels(c)[0]+\"-\" as motif , count(r)+\"\" as frequency  \n union  \n match r=(a:"+Ls[1]+")--(b:"+Ls[2]+")--(c:"+Ls[3]+")--(a)  \n   return labels(a)[0]+\"-\"+labels(b)[0]+\"-\"+labels(c)[0]+\"-\" as motif , count(r)+\"\" as frequency  \n";
	  return runonce(cq);
  }
  
  var q6="MFV";
  if(cypher.includes(q6)){
	  //var sname = "SARS";
	  //var tname = "Sofosbuvir";
	  var tem = cypher.split("\"");
	  var sname = tem[1];
	  var tname = tem[3];
	  cq = "match r=(a:Drug)--(b)--(c:Virus) where c.label contains \""+sname+"\" and a.label contains \""+tname+"\" return count (r)+\"\" as MFV union match r=(a:Drug)--(b)--(d)--(c:Virus) where c.label contains \""+sname+"\" and a.label contains \""+tname+"\" return count (r)+\"\" as MFV union match r=(a:Drug)--(b)--(d)--(e)--(c:Virus) where c.label contains \""+sname+"\" and a.label contains \""+tname+"\" return count (r)+\"\" as MFV";
	  return runonce(cq);
  }
  
  return runonce(cq);
}

function runonce(cypher){
  //alert(visulizationQuery);
  //alert(cypher);
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
