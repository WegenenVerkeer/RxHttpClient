var http = require('http');
var url = require('url');

/**
 * A ridiculously simplistic mock-service against which to test GETTING a chunked HTTP response.
 *
 * TODO: replace this with an extension to WireMock
 */
http.createServer(function (request, response) {
    response.setHeader('Content-Type', 'text/plain; charset=UTF-8');
    response.setHeader('Transfer-Encoding', 'chunked');
    var cancelled = false;

    request.on('close', function() {
	console.log("Connection closed!");
	cancelled = true;	
	});

    var reqNum= url.parse(request.url).pathname;
    var size = parseInt(reqNum.substr(1));

    if (isNaN(size)){
	console.log("Can't interpret size, or not specified. Using default.");
	size = process.env.TEST_APP_DEFAULT || 100;
    }

    console.log("Requested number of events: " + size);
 
    var counter = 1;
    var waitTime = 10;
    function emitEvent() {
	if (cancelled) {
		console.log("Connection closed by client, so cancelling...");
		response.end("Cancelled.");
	}else if (counter > size) {
		console.log("Ending connection");
		response.end();
	} else {
		console.log("Emitting event" + counter);
		response.write("Event emitted: " + counter++ + "\n");
		setTimeout( emitEvent, waitTime);
	}
     }
 
   emitEvent();
 
 
}).listen(process.env.TEST_APP_PORT || 9000, null);
