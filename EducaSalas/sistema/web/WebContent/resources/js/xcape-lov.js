var XCLov = {
	inputKeyPress : function(_evt, _cmp, _upd, _lovId, _btnId) {
		var _key = XCUtil.getKeyCode(_evt);
		if (_key == 13) {
			var _btn = XCUtil.findElement(_cmp, _lovId, _btnId);
			if (_btn) {
				if (_cmp.value == '') {
					_btn.click();
				} else {
					_btn.focus();
				}
			}
		} else if (_key == 119) {
			var _btn = XCUtil.findElement(_cmp, _lovId, _btnId);
			if (_btn) {
				_btn.click();
			}
		}
	}
};