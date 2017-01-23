<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Insert title here</title>
<link rel="stylesheet" href="css/ol.css">
<SCRIPT type="text/javascript" src="js/ol-debug.js"></SCRIPT>
</head>
<body>
<div id="map" class="map"></div>
<form class="form-inline">
      <label>Geometry type &nbsp;</label>
      <select id="type">
        <option value="Point">Point</option>
        <option value="LineString">LineString</option>
        <option value="Polygon">Polygon</option>
        <option value="Circle">Circle</option>
        <option value="Square">Square</option>
        <option value="Box">Box</option>
        <option value="None">None</option>
      </select>
    </form>
<script>
	var raster = new ol.layer.Tile({ 
	 	source: new ol.source.XYZ({
	         url: 'http://a.map.icttic.cn:81/engine?st=GetImage&box={x},{y}&lev={z}&type=vect&uid=yiweihang'//地图
	         	//http://a.map.icttic.cn:81//engine?st=GetImage&box=15,7&lev=4&type=tran&uid=ctticB6156A2D23AEF0F07EF8E2337558F372//混合地图
	         	//http://a.map.icttic.cn:81//engine?st=GetImage&box=15,7&lev=4&type=sate&uid=ctticB6156A2D23AEF0F07EF8E2337558F372//影像地图
	     })
	   });
	  var source = new ol.source.Vector({wrapX: false});
	
	  var vector = new ol.layer.Vector({
	    source: source,
	    style: new ol.style.Style({
	      fill: new ol.style.Fill({
	        color: 'rgba(255, 255, 255, 0.2)'
	      }),
	      stroke: new ol.style.Stroke({
	        color: '#ffcc33',
	        width: 2
	      }),
	      image: new ol.style.Circle({
	        radius: 7,
	        fill: new ol.style.Fill({
	          color: '#ffcc33'
	        })
	      })
	    })
	  });
	
    var map = new ol.Map({
        view: new ol.View({
            center: [116.39885, 39.96571], //视图的初始中心116.39885, 39.96571
            projection: 'EPSG:4326',//EPSG:4326的经纬度坐标，转换为EPSG:3857是web墨卡托坐标
            zoom: 12       //用于缩放视图的初始分辨率
        }),
       // layers: [
      //  new ol.layer.Tile({  //Tile预渲染层
      //      source: new ol.source.MapQuest({layer: 'osm'})  
       // })
      
          
      //  ],
        layers: [raster, vector],
        renderer: 'canvas',
     // 在默认控件的基础上，再加上其他内置的控件
     //   controls: ol.control.defaults().extend([
      //      new ol.control.FullScreen(),
     //       new ol.control.MousePosition(),
      //      new ol.control.OverviewMap(),
       //     new ol.control.ScaleLine(),
      //      new ol.control.ZoomSlider(),
      //      new ol.control.ZoomToExtent()
      //  ]),
        target: 'map'
    });
  //添加比例尺控件
   // map.addControl(new ol.control.ScaleLine());
  //添加全屏控件
  //  map.addControl(new ol.control.FullScreen());
  //添加缩放控件
  //  map.addControl(new ol.control.Zoom());
      var typeSelect = document.getElementById('type');
  	  var draw; // global so we can remove it later
      function addInteraction(value) {
        if (value !== 'None') {
          var geometryFunction, maxPoints;
          if (value === 'Square') {
        	
            value = 'Circle';
            geometryFunction = ol.interaction.Draw.createRegularPolygon(4);
          } else if (value === 'Box') {
            value = 'LineString';
            maxPoints = 2;
            geometryFunction = function(coordinates, geometry) {
              if (!geometry) {
                geometry = new ol.geom.Polygon(null);
              }
              var start = coordinates[0];
              var end = coordinates[1];
              geometry.setCoordinates([
                [start, [start[0], end[1]], end, [end[0], start[1]], start]
              ]);
              return geometry;
            };
          }
          draw = new ol.interaction.Draw({
            source: source,
            type: /** @type {ol.geom.GeometryType} */ (value),
            geometryFunction: geometryFunction,
            maxPoints: maxPoints
          });
          map.addInteraction(draw);
        }
      }
      /**
       * Handle change event.
       */
      typeSelect.onchange = function() {
        map.removeInteraction(draw);
        var value = typeSelect.value;
        addInteraction(value);
      };

</script>
</body>
</html>