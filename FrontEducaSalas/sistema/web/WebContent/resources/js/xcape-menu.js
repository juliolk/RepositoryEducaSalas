var XcpMenu = {
	openPage : function(_pageName, _codObjeto, _title) {

		var target = '_self';
		var url = getContext();
		url += '/secure/';
		url += _pageName;

		var query = false;

		if (_codObjeto != null && _codObjeto.length > 0) {
			url += '?com.xcape.codObjeto=' + _codObjeto;
			query = true;
		}

		var opt = '';
		var tipJanela = PF('selTipoMenu').getSelectedValue();

		if (tipJanela == '4') {
			url += (query ? '&' : '?') + 'com.xcape.TemplateName=popup';
			if (!isHomePage()) {
				alert(getMsgMultiJanelaNaoHome());
				return;
			}
			cmdXcpMultiDialogCtrlOpen([ {
				name : 'xcp_dlg_url',
				value : url
			}, {
				name : 'xcp_dlg_title',
				value : _title
			} ]);
			return false;
		} else if (tipJanela == '2') {
			target = '_blank';
			opt = XcpMenu.getPopupOpt();
			url += (query ? '&' : '?') + 'com.xcape.TemplateName=popup';
		} else if (tipJanela == '3') {
			target = '_blank';
		}

		var w = window.open(url, target, opt);

		//if (_title != null && _title.length > 0) {
		//	w.onload = function() {
		//		w.document.title = _title;
		//	};
		//}

		return false;
	},
	desplugar : function(xhr, status, args) {
		var target = '_blank';
		var url = getContext();
		url += args['pageName'];
		opt = XcpMenu.getPopupOpt();
		url += '?com.xcape.TemplateName=popup';
		url += '&com.xcape.ejectId=' + args['ejectId'];
		window.open(url, target, opt);
	},
	openHelpPage : function(basepath, _numRotina) {
		window.open(basepath + '/help/' + _numRotina, '_blank', 'status=no, scrollbars=yes, width=850, height=600');
	},
	openHelp : function(basepath, _nomeHelp) {
		window.open(basepath + '/help/h/' + _nomeHelp, '_blank', 'status=no, scrollbars=yes, width=850, height=600');
	},
	popup : function(_pageName) {
		XcpMenu.popup(_pageName, null);
	},
	popup : function(_pageName, _pars) {
		target = '_blank';
		var url = getContext();
		url += '/secure/';
		url += _pageName;
		url += '?com.xcape.TemplateName=popup';
		if (_pars != null && _pars != 'undefined') {
			url += '&' + _pars;
		}
		window.open(url, target, XcpMenu.getPopupOpt());
	},
	getPopupOpt : function() {
		var left = (screen.width ? (screen.width / 2 - 1220 / 2) : 50);
		var top = (screen.height ? (screen.height / 2 - 700 / 2) : 50);
		return 'toolbar=no,location=no,directories=no,status=no,menubar=no,titlebar=no,scrollbars=yes,resizable=yes,height=700,width=1220,left=' + left
				+ ',top=' + top;
	},
	openFlow : function(page) {
		$("input[localid='flowlnk_" + page + "']").click();
	}
};