// JavaScript Picture Switcher

$(document).ready(function(){


//don't want to use the slide show? See all designs
$("#all_button").click(function(){

//how do we do this?

});


//list image names without extension
var myImg= new Array(7)
  myImg[0]="comp1.jpg";
  myImg[1]="comp2.jpg";
  myImg[2]="comp3.jpg";
  myImg[3]="comp4.jpg";
  myImg[4]="comp5.png";
  myImg[5]="comp6.jpg";
  myImg[6]="comp7.jpg";
  myImg[7]="comp8.jpg";


//What folder are the IMGs in?
var myImgSrc = "img/web/small/";

var position = 0;
var picnum = 7; //done this way for the array. Total number of pics is this number + 1
var oldLocation = position;
var loadGifSwitch;

//function for "loading" animated GIF
//depending on the value of 'loadGifSwitch', fades in or out the GIF
function loadingGif(){

var myPicCase = document.getElementById("current_pic_case");

  if (loadGifSwitch=="off")
  {
	myPicCase.className = "loading";
	loadGifSwitch = "on";
  }

  else {
	myPicCase.className = "";
	loadGifSwitch = "off";
  }
}


function descOut() //this fades out the old description and removes the class "show"
{
var jobInfoDiv = document.getElementById("job_info");	

$("#comp" + oldLocation).fadeOut('slow', function(){
  
  var lastDesc = document.getElementById("comp" + oldLocation);
  lastDesc.className = "";
  
  jobInfoDiv.className = "off";

  }); //end new info fadeOut

}

function descIn() //this fades in the new description
{
var jobInfoDiv = document.getElementById("job_info");
  
  $("#comp" + position).fadeIn('slow', function(){
	
	jobInfoDiv.className = "on";
	
	var targetDesc = document.getElementById("comp" + position);
	targetDesc.className = "show";
	
  }); //end new info fadeIn

}


//changeBlock function
//this removes the class "ur_here" on previous location, and adds it to the new one
function changeBlock()
{
var lastBlock = document.getElementById("block" + oldLocation);
lastBlock.className = "";

var targetBlock = document.getElementById("block" + position);
targetBlock.className = "ur_here";

descOut();

oldLocation = position;
}


function picIn() // fades the pic and description back in
{
  $("#current_pic").load(function(){
	$("#current_pic").fadeIn('slow');
	descIn();
  });
}

/**** below are the jQuery functions which make reference to the above code ****/

//#prev_button
$("#prev_button").click(function(){

  if (position==0) {
   position = picnum;
   var target = position;}

  else {
   var target = position-=1;}

  changeBlock();

  $("#current_pic").fadeOut('slow', function(){
  document.getElementById("current_pic").src = myImgSrc + myImg[target];
  });

  picIn();

});

//#next_button
$("#next_button").click(function(){

  if (position == picnum) {
  position = 0;
  var target = position;}

  else {
   var target = position+=1;}

  changeBlock();
  
  $("#current_pic").fadeOut('slow', function(){
  document.getElementById("current_pic").src = myImgSrc + myImg[target];
  });

  picIn();

});

// Click on IMG
$("#current_pic").click(function(){

 if (position == picnum) {
  position = 0;
  var target = position;}

 else {
  var target = position+=1;}

 changeBlock();

  $("#current_pic").fadeOut('slow', function(){
  document.getElementById("current_pic").src = myImgSrc + myImg[target];  
  });// end function that fades IMG

 picIn();

});// end IMG click function

})(jQuery);





