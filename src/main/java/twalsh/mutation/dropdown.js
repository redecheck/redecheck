/**
 * 
 */
window.scrollTo(2000, 2000);
//var anchors = document.getElementsByTagName("a");
//for (i=0; i<anchors.length; i++) {
//	var anc = anchors[i];
//	var href = anc.getAttribute("href");
//	var toggle = anc.getAttribute("data-toggle");
//	var target = anc.getAttribute("data-target");
//	var onclick = anc.getAttribute("onclick");
//	if (href != null) {
//		if ((href.indexOf("#") != -1) && (href.length == 1)) {
//			anc.click();
//		}
//	}
//	
//	if ((toggle != null) || (target != null) || (onclick != null)) {
//		anc.click();
//	}
//}

var buttons = document.getElementsByTagName("button");
for (i=0; i<buttons.length; i++) {
	var btn = buttons[i];
	var href = btn.getAttribute("href");
	var toggle = btn.getAttribute("data-toggle");
//	alert(toggle);
	var target = btn.getAttribute("data-target");
	if (href != null) {
		if ((href.indexOf("#") != -1) && (href.length == 1)) {
			btn.click();
		}
	}
	
	if ((toggle == "collapse")) {
		btn.click();
	}
}

window.scrollTo(0, 0);