var express = require('express');
var path = require('path');
var fs = require('fs');

var app = express();

// Start Server
var bodyParser = require('body-parser');
//app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ limit: '5mb', extended: true }));
app.use(express.static(__dirname));

app.listen(process.env.PORT || '3000', function () {
  console.log('Server started on port: ' + this.address().port);
});

app.get('/', function(req, res) {
  res.send('Hello World');
});

var multer = require('multer');
var upload = multer( { dest: 'uploads/' } );

/*app.post('/api/upload/', upload.single('file'), function( req, res ) {
  console.log(req);
  //var name = req.file.originalname;
  //var path = req.file.path;
  //res.status( 200 ).send("ALL GOOD DAWG");
});*/


app.post('/api/upload/', function( req, res ) {
  //console.log(req.body);

  var buff = new Buffer(req.body.image, 'base64');
  fs.writeFile('uploads/test1.png', buff, function(err) { 
    console.log("WRITTen");
    if (err) console.log(err);
  });

  res.json({ message: "SUCCESS" });
});

/*
var logger = function(req, res, next) {
    console.log("GOT REQUEST !");
    next(); // Passing the request to the next handler in the stack.
};
app.use(logger);*/