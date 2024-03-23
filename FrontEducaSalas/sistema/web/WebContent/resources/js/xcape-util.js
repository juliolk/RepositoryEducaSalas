var XCUtil = {
	/*
	 * searchFocus : function() { var bFound = false; for (f = 0; f <
	 * document.forms.length; f++) { for (i = 0; i < document.forms[f].length;
	 * i++) { if (document.forms[f][i].type != "hidden") { if
	 * (document.forms[f][i].disabled != true) { document.forms[f][i].focus();
	 * var bFound = true; } } if (bFound == true) break; } if (bFound == true)
	 * break; } },
	 */

	execBtnOnEnter : function(_evt, _campo, _srcId, _btnId) {
		if (XCUtil.getKeyCode(_evt) == 13) {
			var _btn = XCUtil.findElement(_campo, _srcId, _btnId);
			if (_btn) {
				XCUtil.fireClick(_btn, XCUtil.getEvent(_evt));
			}
			return false;
		}
	},

	getKeyCode : function(_evt) {
		if (window.event) {
			return window.event.keyCode;
		} else if (_evt) {
			return _evt.which;
		}
		return -1;
	},

	findElement : function(_campo, _idSrc, _idFind) {
		var _idValue = _campo.id;
		var _idx = _idValue.indexOf(_idSrc);
		var _prefix = _idValue.substring(0, _idx);
		return document.getElementById(_prefix + _idFind);
	},

	getEvent : function(_evt) {
		if (window.event) {
			return window.event;
		}
		return _evt;
	},

	fireClick : function(_element, evt) {
		if (_element.fireEvent) {
			_element.fireEvent("onclick");
		} else if (document.createEvent) {
			var evObj = document.createEvent('MouseEvents');
			evObj.initMouseEvent('click', true, true, window, 1, evt.screenY, evt.screenY, evt.clientX, evt.clientY, evt.ctrlKey, evt.altKey,
					evt.shiftKey, evt.metaKey, 0, null);
			_element.dispatchEvent(evObj);
		}
	},

	scrollTop : function(_y) {
		layout_widget.layout.center.content.scrollTop(_y);
	},
	
	hintDatatable: function(tblid,hintedit,hintcheck,hintcancel){
		var tbl = $(PrimeFaces.escapeClientId(tblid));
		
		tbl.find( "span.ui-icon-pencil" ).each(function () {
		   $(this).attr("title", hintedit);
		});
		
		tbl.find( "span.ui-icon-check" ).each(function () {
			   $(this).attr("title", hintcheck);
		});
		
		tbl.find( "span.ui-icon-close" ).each(function () {
			   $(this).attr("title", hintcancel);
		});
		
	},
	selecttab : function(widget, tab){ 
		try{
			var idx = $('a[href*=\"'+tab+'\"]').parent().index();
			PF(widget).select(idx);
		}catch (e) {
		}
	}
/*
 * 
 * 
 * execFuncOnEnter: function (_evt, _func) { if (Util.getKeyCode(_evt)==13) {
 * _func(); return false; } },
 * 
 * execBtnOnEnter: function (_evt, _srcId ,_btnId) { if
 * (Util.getKeyCode(_evt)==13) { var _btn = Util.findElement(this, _srcId,
 * _btnId); if(_btn){ fireClick(_btn,Util.getEvent(_evt)); } return false; } }
 */
};