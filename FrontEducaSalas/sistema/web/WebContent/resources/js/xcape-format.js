String.repeat = function(chr, count) {
	var str = "";
	for (var x = 0; x < count; x++) {
		str += chr;
	}
	;
	return str;
};

String.prototype.lpad = function(pSize, pCharPad) {
	var str = this;
	var dif = pSize - str.length;
	var ch = String(pCharPad).charAt(0);
	for (; dif > 0; dif--)
		str = ch + str;
	return (str);
};

$.fn.fonemask = function() {

	return this.each(function() {
		var el = this;
		var fnc = function() {
			var phone, element;
			element = $(el);
			element.unmask();
			phone = element.val().replace(/\D/g, '');
			if (phone.length > 10) {
				element.mask("(99) 99999-999?9");
			} else {
				element.mask("(99) 9999-9999?9");
			}
		};
		$(el).focus(fnc);
		$(el).focusout(fnc);
	});
};

var Format = {
	cpf : function(_componet) {
		var value = _componet.value;
		if (value == "")
			return;
		value = String(value).replace(/\D/g, "").replace(/^0+/, "");
		value = value.lpad(11, '0');
		reCpf = /(\d{3})(\d{3})(\d{3})(\d{2})$/;
		value = value.replace(reCpf, "$1.$2.$3-$4");

		_componet.value = value;
	},

	cnpj : function(_componet) {
		var value = _componet.value;
		if (value == "")
			return;
		value = String(value).replace(/\D/g, "").replace(/^0+/, "");
		value = value.lpad(14, '0');
		reCnpj = /(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/;
		value = value.replace(reCnpj, "$1.$2.$3/$4-$5");
		_componet.value = value;
	},
	date : function(_componet) {
		var value = _componet.value;
		value = new String(value.replace(/[^\d\/,\.\s-;:]/g, ""));

		// Data corrida EX 12082008 ou 120808
		var expCorrida;
		var expSeparador;
		if (value.length <= 2) {
			expCorrida = /^(\d+)$/;
			expSeparador = /^(\d+)[\/,\.\s-;:]?$/;
		} else {
			expCorrida = /^(\d{2})(\d{2})?(\d{2,4})?$/;
			expSeparador = /^(\d{1,2})[\/,\.\s-;:](\d{1,2})?[\/,\.\s-;:]?(\d{1,4})?$/;
		}

		// Data separador EX 12/8/2008 ou 12,8,8 ou 12.2.2008 ou ainda 12,2/08

		var _arrD;

		if ((_arrD = expCorrida.exec(value)) != null
				|| (_arrD = expSeparador.exec(value)) != null) {
			var dt = new Date();
			var _y = _arrD[3] ? Format._getYear(_arrD[3]) : dt.getFullYear();
			var _m = (_arrD[2] ? _arrD[2] - 1 : dt.getMonth()) + 1;
			var _d = _arrD[1];
			var _dia = ("00" + _d).slice(-2);
			var _mes = ("00" + _m).slice(-2);
			var _ano = ("0000" + _y).slice(-4);

			_componet.value = new String(_dia + "/" + _mes + "/" + _ano);
		}
	},

	dateTime : function(_componet) {
		var value = _componet.value;

		value = value.replace(/[^\d\/,\.\s-;:]/g, "");

		// Data corrida EX 12082008 ou 120808
		var expCorrida = /^(\d{2})(\d{2})(\d{2,4})(\d{2})(\d{2})$/;
		// Data separador EX 12/8/2008 ou 12,8,8 ou 12.2.2008
		var expSeparador = /^(\d+)[\/,\.\s-;:](\d+)[\/,\.\s-;:](\d+)[\/,\.\s-;:](\d+)[\/,\.\s-;:](\d+)$/;
		var _arrD;

		if ((_arrD = expCorrida.exec(value)) != null
				|| (_arrD = expSeparador.exec(value)) != null) {
			var dt = new Date();
			dt.setFullYear(Format._getYear(_arrD[3]), _arrD[2] - 1, _arrD[1]);
			dt.setHours(_arrD[4], _arrD[5], 0, 0);

			var _dia = ("00" + dt.getDate()).slice(-2);
			var _mes = ("00" + (dt.getMonth() + 1)).slice(-2);
			var _ano = ("0000" + dt.getFullYear()).slice(-4);
			var _hora = ("00" + dt.getHours()).slice(-2);
			var _min = ("00" + dt.getMinutes()).slice(-2);

			_componet.value = new String(_dia + "/" + _mes + "/" + _ano + " "
					+ _hora + ":" + _min);
		}
	},

	_getYear : function(_year) {
		var _y = new Number(_year);

		if (_y < 100) {
			var _hoje = new Date();
			var _yh = new Number((new String(_hoje.getFullYear())).slice(-2));
			_yh = _yh + 50;
			if (_y >= _yh) {
				_y += 1900;
			} else {
				_y += 2000;
			}
		} else if (_y > 3000) {
			_y = 3000;
		}

		return _y;
	},

	time : function(_componet) {
		var value = _componet.value;
		value = value.replace(/[^\d\/,\.\s-;:]/g, "");
		// Hora corrida EX 0100
		var expCorrida = /^(\d{1,2})(\d{1,2})$/;
		// Data separador EX 12/8/2008 ou 12,8,8 ou 12.2.2008
		var expSeparador = /^(\d+)[\/,\.\s-;:](\d+)[\/,\.\s-;:](\d+)[\/,\.\s-;:](\d+)[\/,\.\s-;:](\d+)$/;
		var _arrD;
		if ((_arrD = expCorrida.exec(value)) != null
				|| (_arrD = expSeparador.exec(value)) != null) {
			var dt = new Date();
			dt.setHours(_arrD[1], _arrD[2], 0, 0);

			var _hora = ("00" + dt.getHours()).slice(-2);
			var _min = ("00" + dt.getMinutes()).slice(-2);

			_componet.value = new String(_hora + ":" + _min);
		}
	},

	number : function(_campo, _precision, _scale, _group, _decSep, _groupSep) {
		var valor = new String(_campo.value);

		if (valor == "") {
			return;
		}
		var negativo = valor.substr(0, 1) == "-";
		var posDec = valor.lastIndexOf(_decSep);
		if (posDec == -1) {
			if (_decSep.length > 1) {
				for (i = 0; i < _decSep.length; i++) {
					posDec = valor.lastIndexOf(_decSep.charAt(i));
					if (posDec > -1) {
						break;
					}
				}
			}
			if (posDec == -1) {
				posDec = valor.length;
			}
		}
		var decimal = valor.slice(posDec + 1);
		var inteiro = valor.substring(0, posDec);
		decimal = decimal.replace(/\D/g, "");
		inteiro = inteiro.replace(/\D/g, "");

		if (inteiro.length > (_precision - _scale)) {
			inteiro = inteiro.slice(-(_precision - _scale));
		}

		if (decimal.length > _scale) {
			decimal = decimal.substring(0, _scale);
		} else if (decimal.length < _scale) {
			decimal += String.repeat('0', _scale - decimal.length);
		}

		if (_group) {
			var newInt = new String();
			var _a;
			for (_a = inteiro.length; _a > 3; _a -= 3) {
				newInt = _groupSep + inteiro.substr(_a - 3, 3) + newInt;
			}
			inteiro = inteiro.substring(0, _a) + newInt;
		}
		if (inteiro == "" && decimal != "") {
			inteiro = "0";
		}
		retorno = (negativo ? "-" : "") + inteiro
				+ ((_scale > 0) ? _decSep.charAt(0) + decimal : "");
		_campo.value = retorno;
	}
};