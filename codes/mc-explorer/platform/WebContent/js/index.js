function startAll(){
    app_name = $("body").attr("data-appname");
	groupBy();
	setGraphResult();
	setGraphG();
	setGraphM();
	setQueryButton();
	emptyingGraphG();
    if (app_name === 'medicine') {
        // use drugbank + disgenet + geneontology + refseq
        setData5();
        setTimeout(function(){
            $("#configButton").click();
		    $("#config-modal").css("display", "none");
        }, 1000);
    }
	console.log("done startAll");
}

