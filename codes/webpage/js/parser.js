var visulizationQuery = "";
var matchStr = ""; 

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

function replaceAll(str, str1, str2){
	var tem = str.split(str1);
	var str3 = "";
	for(i=0;i<tem.length;i++){
		if(i<tem.length-1)
			str3 += tem[i] + str2;
		else
			str3 += tem[i];
	}
	return str3;
}

function replaceLast(str, str1, str2){
	var tem = str.split(str1);
	var str3 = "";
	for(i=0;i<tem.length;i++){
		if(i<tem.length-1){
			if(i<tem.length-2){
				str3 += tem[i] + str1;
			}else{
				str3 += tem[i] + str2;
			}
			
		}
		else
			str3 += tem[i];
	}
	return str3;
}

function run(cypher) {
  matchStr = "";
  if(cypher.includes("SID")||cypher.includes("TID")){
	var tem = cypher.split("RETURN");
	matchStr = tem[0];
	matchStr = replaceAll(matchStr,".id",".nid+''");	
  }
  cypher = replaceLast(cypher,"SID","'SID'");
  cypher = replaceLast(cypher,"TID","'TID'");
	
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
  // RETURN MCLQ(M,'1','Drug')
  //return neo4jdriver.MCLQ('211','A,B,C','5','A')
  //unwind neo4jdriver.MCLQVis('211','A,B,C','5','A') as ms with ms match (a:A{nid:ms[0]}),(b:B{nid:ms[1]}),(c:C{nid:ms[2]}) return a,b,c
  if(cypher.includes(q3)){
	  var tem = cypher.split("'");
	  var nodeSStr = tem[1]
	  var labelSStr = tem[3];
	  cq = "return neo4jdriver.MCLQ";
	  visulizationQuery = "unwind neo4jdriver.MCLQVis";
	  var para = "('"+degVecStr + "','" + nodeLabelsArr +"','"+nodeSStr+"','"+labelSStr+"')";
	  cq += para;
	  visulizationQuery += para + " as ms with ms match ";
	  var retArr = "";
	  for(i = 0; i < nodeLabelsArr.length; i++){
		  visulizationQuery += "(n"+i+":"+nodeLabelsArr[i]+"{nid:ms["+i+"]})";
		  retArr += "n"+i;
		  if(i!=nodeLabelsArr.length-1){
			  visulizationQuery += ",";
			  retArr += ",";
		  }
		  
	  }
	  visulizationQuery += " return "+retArr;
	  return runonce(cq);	
  }
  
  var q4="MPPR";
  if(cypher.includes(q4)){
	  //RETURN MPPR(M,'2697049','Virus','Drug','10000','0.85')	  
	  //unwind neo4jdriver.MPPR('1122', 'A,B,C,D', '5', 'A', 'A', '10000', '0.85') as entry return toInteger(entry[0]) as nodeID, entry[1] as nodeName, toFloat(entry[2]) as MPPRscore order by MPPRscore DESC
	  var tem = cypher.split("'");
	  var nodeSStr = tem[1]
	  var labelSStr = tem[3];
	  var labelTStr = tem[5]
	  var interationStr = tem[7];
	  var dampingStr = tem[9];	  
	  cq = "return neo4jdriver.MPPR";
	  var para = "('"+degVecStr + "','" + nodeLabelsArr +"','"+nodeSStr+"','"+labelSStr+"','"+labelTStr+"','"+interationStr+"','"+dampingStr+"')";
	  cq += para + "as entry return toInteger(entry[0]) as nodeID, entry[1] as nodeName, toFloat(entry[2]) as MPPRscore order by MPPRscore DESC";
	  return runonce(cq);
  }
  
  var q5old="MDISOld";
  if(cypher.includes(q5old)){
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
  
  var q51 = "MDIS";
  //RETURN MDIS('A,B,C,D','4')
  //unwind neo4jdriver.MDIS('A,B,C,D','4') as tem with tem[0] as degvec, tem[1] as labelvec, toInteger(tem[2]) as countnum where countnum>0 return degvec, labelvec, countnum order by countnum desc
  var q52 = "MDISS";
  //RETURN MDISS('A,B,C,D','4','1','A')
  //unwind neo4jdriver.MDISS('A,B,C,D','4','5','A') as tem with tem[0] as degvec, tem[1] as labelvec, toInteger(tem[2]) as countnum where countnum>0 return degvec, labelvec, countnum order by countnum desc
  if(cypher.includes(q51)){
	  var tem = cypher.split("'");
	  var labelsStr = tem[1]
	  var kStr = tem[3];
	  if(cypher.includes(q52)){
		  var nodeSStr = tem[5]
	      var labelSStr = tem[7]; 
		  cq = "unwind neo4jdriver.MDISS('"+labelsStr+"','"+kStr+"','"+nodeSStr+"','"+labelSStr+"') as tem with tem[0] as degvec, tem[1] as labelvec, toInteger(tem[2]) as countnum where countnum>0 return degvec, labelvec, countnum order by countnum desc";
	  }else{
		  cq = "unwind neo4jdriver.MDIS('"+labelsStr+"','"+kStr+"') as tem with tem[0] as degvec, tem[1] as labelvec, toInteger(tem[2]) as countnum where countnum>0 return degvec, labelvec, countnum order by countnum desc";
	  }
	  return runonce(cq);
  }
  
  
  
  var q61 = "MFV";
  //RETURN MFV([M1,M2],'5','A','3','D')
  //return neo4jdriver.MFV('112|222','A,C,D|A,C,D','5','A','3','D')
  var q62 = "MFVN";
  //RETURN MFV([M1,M2],'5','A')
  //return neo4jdriver.MFVN('112|222','A,C,D|A,C,D','5','A')
  if(cypher.includes(q61)){
	  var tem = cypher.split("'");
	  var nodeSStr = tem[1]
	  var labelSStr = tem[3];
	  cq = "return neo4jdriver.";
	  var para = "'"+degVecStr + "','" + nodeLabelsArr + "','" + nodeSStr +"','"+ labelSStr +"'";
	  if(cypher.includes(q62)){
		  cq+="MFVN("+para+")";
	  }else{
		  var nodeTStr = tem[5]
	      var labelTStr = tem[7];
		  cq+="MFV("+para+","+nodeTStr+"','"+labelTStr+"')";
	  }
	  return runonce(cq);
  }
  
  var q71="MCOUNT";
  var q72="MENUMERATE";
  //return MCOUNT(M)
  //unwind neo4jdriver.mcount('211','A,B,C') as ms return count(ms)
  if(cypher.includes(q71)||cypher.includes(q72)){
	  cq = "unwind neo4jdriver.MCOUNT(";
	  cq += "'"+degVecStr + "','" + nodeLabelsArr + "') as ms";
	  visulizationQuery = cq + " with ms match ";
	  var retArr = "";
	  for(i = 0; i < nodeLabelsArr.length; i++){
		  visulizationQuery += "(n"+i+":"+nodeLabelsArr[i]+"{nid:ms["+i+"]})";
		  retArr += "n"+i;
		  if(i!=nodeLabelsArr.length-1){
			  visulizationQuery += ",";
			  retArr += ",";
		  }
		  
	  }
	  visulizationQuery += " return "+retArr;
	  if(cypher.includes(q71))
		  cq += " return count(ms)+'' as MotifCountNumber";
	  else
		  cq += " return ms";
	  return runonce(cq);
  }
  
  var q8="MCON";
  //RETURN MCON(M,'1,2,3','Drug,Drug,Drug')
  //return neo4jdriver.MCON('112','A,B,C','5,9,17,18,19','A,A,A,A,A')
  if(cypher.includes(q8)){
	  var tem = cypher.split("'");
	  var nodeSetStr = tem[1]
	  var labelSetStr = tem[3];
	  
	  cq = "return neo4jdriver.MCON(";
	  cq += "'"+degVecStr + "','" + nodeLabelsArr + "','" + nodeSetStr + "','" + labelSetStr + "')";
	  return runonce(cq);
  }  
  
  var q9="MGD";
  //RETURN MGD(M)
  if(cypher.includes(q9)){	  
	  cq = "return neo4jdriver.MGD(";
	  cq += "'"+degVecStr + "','" + nodeLabelsArr + "')";
	  return runonce(cq);
  }  

  var q10 = "SMPD";
  //RETURN SMPD(M,'1','Drug','10254','Virus')
  //return neo4jdriver.SMPD('112','A,B,C','5','A','17','A')
  //unwind neo4jdriver.SMPDVis('112','A,B,C','5','A','17','A') as ms with ms match (a:A{nid:ms[0]}),(b:B{nid:ms[1]}),(c:C{nid:ms[2]}) return a,b,c 
  if(cypher.includes(q10)){
	  var tem = cypher.split("'");
	  var nodeSStr = tem[1]
	  var labelSStr = tem[3];
	  var nodeTStr = tem[5]
	  var labelTStr = tem[7];
	  cq = "return neo4jdriver.SMPD";
	  visulizationQuery = "unwind neo4jdriver.SMPDVis";
	  var para = "('"+degVecStr + "','" + nodeLabelsArr +"','"+nodeSStr+"','"+labelSStr+"','"+nodeTStr+"','"+labelTStr+"')";
	  cq += para;
	  visulizationQuery += para + " as ms with ms match ";
	  var retArr = "";
	  for(i = 0; i < nodeLabelsArr.length; i++){
		  visulizationQuery += "(n"+i+":"+nodeLabelsArr[i]+"{nid:ms["+i+"]})";
		  retArr += "n"+i;
		  if(i!=nodeLabelsArr.length-1){
			  visulizationQuery += ",";
			  retArr += ",";
		  }
		  
	  }
	  visulizationQuery += " return "+retArr;
	  return runonce(cq);	  
	  
  }
  
  var q11 = "MAM";
  //RETURN MAM([M1,M2]);
  //unwind neo4jdriver.MAM('1122|112','A,B,C,D|A,B,C') as entry return toInteger(entry[0]) as node1, entry[1] as label1, toInteger(entry[2]) as node2, entry[3] as label2, toInteger(entry[4]) as weight
  if(cypher.includes(q11)){	  
	  cq = "unwind neo4jdriver.MAM('";
	  cq += degVecStr + "','" + nodeLabelsArr;
	  cq += "') as entry return toInteger(entry[0]) as node1, entry[1] as label1, toInteger(entry[2]) as node2, entry[3] as label2, toInteger(entry[4]) as weight";
	  return runonce(cq);
  }  
  
  var q12 = "MCC";
  //RETURN MCC(M,'1','Drug')
  //return neo4jdriver.MCC('112','A,B,C','5','A')
  //unwind neo4jdriver.MCCVis('112','A,B,C','5','A') as ms with ms match (a:A{nid:ms[0]}),(b:B{nid:ms[1]}),(c:C{nid:ms[2]}) return a,b,c
  if(cypher.includes(q12)){
	  var tem = cypher.split("'");
	  var nodeSStr = tem[1]
	  var labelSStr = tem[3];
	  cq = "return neo4jdriver.MCC";
	  visulizationQuery = "unwind neo4jdriver.MCCVis";
	  var para = "('"+degVecStr + "','" + nodeLabelsArr +"','"+nodeSStr+"','"+labelSStr+"')";
	  cq += para;
	  visulizationQuery += para + " as ms with ms match ";
	  var retArr = "";
	  for(i = 0; i < nodeLabelsArr.length; i++){
		  visulizationQuery += "(n"+i+":"+nodeLabelsArr[i]+"{nid:ms["+i+"]})";
		  retArr += "n"+i;
		  if(i!=nodeLabelsArr.length-1){
			  visulizationQuery += ",";
			  retArr += ",";
		  }
		  
	  }
	  visulizationQuery += " return "+retArr;
	  return runonce(cq);	  
	  
  }
  
  return runonce(cq);
}

function runonce(cypher){
  //alert(cypher);
  if(cypher.includes("'SID'")||cypher.includes("'TID'")){
	cypher = replaceAll(cypher,"'SID'","SID");
    cypher = replaceAll(cypher,"'TID'","TID");
	cypher = matchStr + cypher;		
  }
  //alert(cypher);
  //alert(visulizationQuery);  
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
