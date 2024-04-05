$(document).ready(function() {
	// create the OUTER LAYOUT
	$("body").layout(layoutAtalhos);

});

var layoutAtalhos = {
	east : {
		size : 100,
		togglerTip_open : "Fechar",
		togglerTip_closed : "Abrir",
		resizable : false,
		initClosed : true,
		slideTrigger_open : "mouseover",
		fxName : "drop",
		fxSpeed : "normal",
		fxSettings : {
			easing : ""
		}
	},
	north : {
		resizable : false,
		togglerTip_open : "Fechar",
		togglerTip_closed : "Abrir"
	},
	west : {
		resizable : false,
		togglerTip_open : "Fechar",
		togglerTip_closed : "Abrir"
	}
};