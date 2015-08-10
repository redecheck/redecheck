function initialize() {
  var map_options = {
    center: new google.maps.LatLng(21.308848, -157.861526),
    zoom: 14,
    mapTypeId: google.maps.MapTypeId.ROADMAP,
    scrollwheel: false,
    styles: [
      {
        "elementType": "labels.text.fill",
        "stylers": [
          { "saturation": -100 },
          { "lightness": 50 },
          { "color": "#484c4f" }
        ]
      },
      {
        "featureType": "road",
        "elementType": "geometry",
        "stylers": [
          {
            "visibility": "on"
          },
          {
            "color": "#ffffff"
          }
        ]
      },
      {
        "featureType": "poi",
        "elementType": "geometry",
        "stylers": [
          {
            "visibility": "off"
          }
        ]
      },
      {
        "featureType": "landscape",
        "elementType": "geometry",
        "stylers": [
          {
            "color": "#f9f5ed"
          }
        ]
      },
      {
        "featureType": "water",
        "stylers": [
          {
            "lightness": 50
          },
          {
            "color": "#d6ede9"
          }
        ]
      },
      {
        "featureType": "road",
        "elementType": "labels",
        "stylers": [
          {
            "visibility": "off"
          }
        ]
      },
      {
        "featureType": "transit",
        "stylers": [
          {
            "visibility": "off"
          }
        ]
      },
      {
        "featureType": "administrative",
        "elementType": "geometry",
        "stylers": [
          {
            "lightness": 40
          }
        ]
      }
    ]
  };
  var map_canvas = document.getElementById('map_canvas');
  var map = new google.maps.Map(map_canvas, map_options)        
}
google.maps.event.addDomListener(window, 'load', initialize);
