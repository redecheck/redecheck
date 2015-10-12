var links = document.getElementsByTagName("link");
var host = window.location.host;
files = Array();
for (i=0; i<links.length; i++) {
	var link = links[i];
	var rel = link.getAttribute("rel");
	if (rel == "stylesheet") {
		files.push(link.getAttribute("href"));
//		link.click();
		
//		if (link.getAttribute("href").indexOf("http") == -1) {
//			if (link.getAttribute("href").charAt(0) == '/') {
//				files.push(window.location.protocol + "//" + window.location.host + link.getAttribute("href"));
//			} else if (link.getAttribute("href").indexOf("../") != -1) {
//				files.push(window.location.protocol + "//" + window.location.host + link.getAttribute("href").replace("..", ""));
//			} else {
//				alert("Pushing first");
//				files.push(window.location.protocol + "//" + window.location.host + "/" + link.getAttribute("href"));
//			}
//		} else {
//			alert("Pushing second");
//			files.push(link.getAttribute("href"));
//		}
//		window.history.back();
	}
}
return files;