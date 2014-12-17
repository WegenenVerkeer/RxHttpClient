var http = require('http');

/**
 * A ridiculously simplistic mock-service against which to test GETTING a chunked HTTP response.
 *
 * TODO: replace this with an extension to WireMock
 */
http.createServer(function (request, response) {
    response.setHeader('Content-Type', 'text/plain; charset=UTF-8');
    response.setHeader('Transfer-Encoding', 'chunked');

 
    response.write("START EVENTS:");
    var size = 1000;
    var counter = 1;

     function emitEvent() {
	if (counter == size) {
		response.end("FINISHED");
	} else {
		response.write("Event emitted: " + counter++ + "\n");
		setTimeout( emitEvent, 1000);
	}
     }
 
   emitEvent(response, 1, 100);
 
 
}).listen(process.env.VMC_APP_PORT || 9000, null);
