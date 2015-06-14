 function init() {
  				var options = {
					minResolution: "auto",
					minExtent: new OpenLayers.Bounds(-1, -1, 1, 1),
					maxResolution: "auto",
					maxExtent: new OpenLayers.Bounds(-180, -90, 180, 90),
				};
				      var map = new OpenLayers.Map("canvas",options);


         var mapnik = new OpenLayers.Layer.OSM();
        map.addLayer(mapnik);
 			map.zoomToMaxExtent();
 			var markers = new OpenLayers.Layer.Markers("Markers");
map.addLayer(markers);
var marker = new OpenLayers.Marker(
    new OpenLayers.LonLat(139.76, 35.68)
        .transform(
            new OpenLayers.Projection("EPSG:4326"), 
            new OpenLayers.Projection("EPSG:900913")
        )
);
markers.addMarker(marker);
 /*        
        var lonLat = new OpenLayers.LonLat(139.76, 35.68)
            .transform(
                new OpenLayers.Projection("EPSG:4326"), 
                new OpenLayers.Projection("EPSG:900913")
            );
        map.setCenter(lonLat, 2);
   */ }
    
    