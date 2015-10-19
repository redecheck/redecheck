var input = arguments[0];
var firstBox = document.getElementById("minified-css-orig");
firstBox.value = input;
var button = document.getElementById("minified-css-button");
button.click();
var result = document.getElementById("minified-css-new");
return result.value;