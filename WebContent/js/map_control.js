//var myControl = new ol.control.Control({element: myElement});
EVLayerControl = OpenLayers.Class(OpenLayers.Control, {

	initialize : function(options) {
		OpenLayers.Control.prototype.initialize.apply(this, [ options ]);

	},
	draw : function() {
		if (this.div == null) {cx																																																																																																																																																																													;
			this.div = OpenLayers.Util.createDiv(this.id);
			this.div.className = "map_maptype";

			var ul = document.createElement('ul');

			this.div.appendChild(ul);

			var li0 = document.createElement('li');
			li0.className = "t_sep";
			ul.appendChild(li0);

			var li1 = document.createElement('li');
			li1.className = "";
			li1.innerHTML = "<p>地图</p>"
			ul.appendChild(li1);

			var li2 = document.createElement('li');
			li2.className = "t_sep";
			ul.appendChild(li2);

			var li3 = document.createElement('li');
			li3.className = "";
			li3.innerHTML = "<p>影像</p>"
			ul.appendChild(li3);

			var li4 = document.createElement('li');
			li4.className = "t_sep";
			ul.appendChild(li4);

			var li5 = document.createElement('li');
			li5.className = "";
			li5.innerHTML = "<p>混合地图</p>"
			ul.appendChild(li5);

			var li6 = document.createElement('li');
			li6.className = "t_sep";
			ul.appendChild(li6);
			var self = this;

			li1.onmousedown = function() {

				self.setCurrent(0);
			}

			li3.onmousedown = function() {
				li2.className = "t_sep t_sep_c";
				li3.className = "now_li";
				li4.className = "t_sep t_sep_c";

				self.setCurrent(1);
			}

			li5.onmousedown = function() {
				li4.className = "t_sep t_sep_c";
				li5.className = "now_li";
				li6.className = "t_sep t_sep_c";

				self.setCurrent(2);
			}

			this.li0 = li0;
			this.li1 = li1;
			this.li2 = li2;
			this.li3 = li3;
			this.li4 = li4;
			this.li5 = li5;
			this.li6 = li6;

			this.setCurrent(0);
		}

		return this.div;
	},
	setCurrent : function(t) {
		this.li0.className = (t == 0) ? "t_sep t_sep_c" : "t_sep";

		this.li1.className = (t == 0) ? "now_li" : "";
		this.li2.className = (t == 0 || t == 1) ? "t_sep t_sep_c" : "t_sep";

		this.li3.className = (t == 1) ? "now_li" : "";
		this.li4.className = (t == 2 || t == 1) ? "t_sep t_sep_c" : "t_sep";

		this.li5.className = (t == 2) ? "now_li" : "";
		this.li6.className = (t == 2) ? "t_sep t_sep_c" : "t_sep";

		if (this.map)
			this.map.setMapTypeNoControl(t);
	},
	CLASS_NAME : "EVLayerControl"
});
