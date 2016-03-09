var express = require('express');
var app = express();
var fs = require("fs");

app.get('/:cur1/:cur2/:number', function (req, res) {
	fs.readFile( __dirname + "/" + "rates.json", 'utf8', function (err, data) {
		curs = JSON.parse( data );
		
		/* get two currency */
		var cur1 = curs[req.params.cur1];
		var cur2 = curs[req.params.cur2];
		
		/* calculator */
		var result = req.params.number * cur1.rate / cur2.rate;
		res.write(result.toString());
       	res.end();
   });
})

var server = app.listen(8081, function () {

  var host = server.address().address
  var port = server.address().port

  console.log("App listening at http://%s:%s", host, port)
})