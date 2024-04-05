var XCStorage = {
	init : function(_componet) {
		$(window).on('storage', XCStorage.message_receive);
	},

	message_receive : function(ev) {
		if (ev.originalEvent.key != 'message')
			return;
		var message = JSON.parse(ev.originalEvent.newValue);
		if (!message)
			return; // ignore empty msg or msg reset

		if (message.command == 'reload') {
			document.location.reload(true);
		}
	},

	reload_tabs : function() {
		var msg = {
			'command' : 'reload'
		};
		localStorage.setItem('message', JSON.stringify(msg));
		localStorage.removeItem('message');
	}
};