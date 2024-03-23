var que = "";
var ans = 0;
var numero = "0";
var item = 0;
var eksp = 0;
var base = "";
var eksponent = 3;
var solucao = 0;
var decimais = 14;
var asf = 0;
var rdg = 0;
var sep1000 = 0;
var enter = "";
var mr = "";
var pi = 3.141592653589793;
var e = 2.71828182845905;
var abc = "";

function start(){
	enter = "\r\n";
	mr = document.mainform.memoria.value;
	document.mainform.notes.value =  "";
	document.mainform.tarefa.focus();
}

function isEmpty(str) {
    return (!str || 0 === str.length);
}

function memory(operator) {
	switch(operator) {
	case 3:	// CLS
		if (document.mainform.tarefa.value == "") {document.mainform.resultado.value = "";}
		else {document.mainform.tarefa.value = "";	}	
		break; 
	case 5:	// Reset
		document.mainform.notes.value = enter;
		document.mainform.memoria.value = "";
		decimais = 14;
		asf = 0;
		rdg = 0;
		sep1000 = 0;
		ans = 0;
		break; 
	}

	document.mainform.tarefa.focus();
}

function atualizaTela(valor) {
	document.mainform.tarefa.focus();
	document.mainform.tarefa.value += valor;
}

function calcular(operacao) {
	var pergunta = "";
	var resposta = "";
	
	if (operacao >= 1) {
		if (document.mainform.tarefa.value == "") {
			numero = document.mainform.resultado.value;
		}
		else {
	    	numero = document.mainform.tarefa.value;
			if (separarresultado(numero.charAt(0))) {
				numero = document.mainform.resultado.value + numero;
			}
		}
	}

	for (var i=0; i<numero.length; i++) {
		var mm = numero.charAt(i);
		var mmup = numero.charAt(i+1);
		var mmdn = numero.charAt(i-1);

		if (mm == ",") {mm = ".";}
		else if (mm == "}" || mm == "]") {mm = ")";}
		else if (mm == "{" || mm == "[") {mm = "(";}
		else if (mm == " " || mm == "=") {mm = "";}
		else if (mm == "*" && mmup == "*") {mm = "^"; i += 1;}
		else if (mm == "+" && mmup == "-") {mm = "-"; i += 1;}
		else if (mm == "E" && kemilimat(mmup)) {mm = "e";}
	
		if (mm == "." && numeroAtoma(mmdn)==false) {mm = "0.";}
		else if (velikoslovo(mm)) {var kem = 1;}

		if (pergunta == "0") {
			if (operator(mm)) {}
			else if (mm != ".") {pergunta = "";}
		}
		pergunta += mm;
	}

	if (operacao == 1) {
		if(isEmpty(document.mainform.notes.value)){
			document.mainform.notes.value += pergunta;
		}else{
			document.mainform.notes.value += '\n'+pergunta;
		}	
		
		document.mainform.anterior.value = pergunta;
		if (kem == 1) {
			//var atom = "+" + pergunta;
			resposta = masa(pergunta);
		}
		else {
			resposta = interpretar(pergunta);
		}
	}
	else {
		resposta = matematica(operacao, pergunta);
	}
	
	resposta = resposta.toString();
	document.mainform.oldresultado.value = resposta;
	ans = parseFloat(resposta);
	
	formatonumero(resposta);

	document.mainform.tarefa.value = "";
 	document.mainform.tarefa.focus();
}


function matematica(operacao, solucao) {
var pergunta = "";
 with (Math) {
	if (operacao == 17) {
		pergunta = solucao + "%";
		solucao = solucao/100;
	}
 }
	document.mainform.notes.value += pergunta;
	document.mainform.anterior.value = pergunta;
	return solucao;
}

function interpretar(xnumero) {
    var intZagClose = 0;
    var intZagOpen = 0;
	var strnovoZnumero = "";

	do {
		xnumero = xnumero.replace(/--/g,"-1*-");
		intZagClose = xnumero.indexOf(")");
		if (intZagClose != -1) {
			for (var i = intZagClose; i >= 0; i--) {
				if (xnumero.charAt(i)=="(") {
					intZagOpen = i;
					strnovoZnumero = xnumero.substring(intZagOpen+1,intZagClose);
					break;
				}
			}
		}
		else {
			strnovoZnumero = xnumero;
		}

		strnovoZnumero = strnovoZnumero + "*1";
		strnovoZnumero = percentagem(strnovoZnumero);
		strnovoZnumero = trigonometria(strnovoZnumero);
		strnovoZnumero = potencia(strnovoZnumero);
		strnovoZnumero = conta(strnovoZnumero);

		if (intZagClose != -1) {
			xnumero = xnumero.replace(xnumero.substring(intZagOpen, intZagClose+1), strnovoZnumero);
		}
		else {
			xnumero = strnovoZnumero;
		}	
	}
	while (intZagClose > 0);
	
	return xnumero;
}

function conta(znumero) {
	with (Math) {znumero = eval(znumero);}
	return znumero;
}

function potencia(ulaz) {
    var intZagClose = 0;
    var intZagOpen = 0;
	var intXnumero = ulaz.indexOf("^");  
	
	while (intXnumero > 0) {
		for (var i = intXnumero - 1; i >= 0; i--) {
			if (operator(ulaz.charAt(i)) && ulaz.charAt(i-1)!="e") {
				intZagOpen = i+1;
				break;
			}
		}
		
		if (ulaz.charAt(i) == "-"){
			if (i == 0) {intZagOpen = 0;}
			else if (i > 0 && operator(ulaz.charAt(i-1))) {intZagOpen = i;}
		}
		
		var strnovoXnumero = ulaz.substring(intZagOpen,intXnumero);
		
		for (var i = intXnumero + 2; i < ulaz.length; i++) {
			if (operator(ulaz.charAt(i)) && ulaz.charAt(i-1)!="e") {intZagClose = i-1; break;}
		}
		var strnovoYnumero = ulaz.substring(intXnumero+1,intZagClose+1);
 if (strnovoXnumero == 'e') {strnovoXnumero = e;}; //cps	
		with (Math) {
			intXnumero = pow(strnovoXnumero, strnovoYnumero);
		}

		ulaz = ulaz.replace(ulaz.substring(intZagOpen, intZagClose+1), intXnumero);

		intXnumero = ulaz.indexOf("^");
	}

	return ulaz;
}


function percentagem(ulaz) {
    var intZagClose = 0;
    var intZagOpen = 0;

	var strDesnoFun = new Array ("!", "%");

	for (var f = 0; f < 2; f++) {
		var intXnumero = ulaz.indexOf(strDesnoFun[f]);
		
		while (intXnumero > 0) {
			for (var i = intXnumero - 1; i >= 0; i--) {
				if (operator(ulaz.charAt(i)) && ulaz.charAt(i-1)!="e") {intZagOpen = i+1; break;}
			}
			var strnovoXnumero = ulaz.substring(intZagOpen,intXnumero);
			intZagClose = intXnumero+1;
			with (Math) {
				if (f == 0) {
					intXnumero = factorial(strnovoXnumero);
				}
				else {
					intXnumero = strnovoXnumero/100;
				}
			}
			ulaz = ulaz.replace(ulaz.substring(intZagOpen, intZagClose), intXnumero);
			intXnumero = ulaz.indexOf(strDesnoFun[f]);
		}
	}

	return ulaz;
}


function trigonometria(kut) {
    var intZagClose = 0;
	var intKut = 0;
	var strnovoKut = "";
	var strKrozPi = ")";
	var strPiKroz = ")";
	
	switch(rdg) {
	case 1:
		strKrozPi = ")*180/pi";
		strPiKroz = "*pi/180)";
		break;    
	case 2:
		strKrozPi = ")*200/pi";
		strPiKroz = "*pi/200)";
		break;
	default:
		strKrozPi = ")";
		strPiKroz = ")";
	}

	var strTrigFun = new Array ("sin", "cos", "tan");

	for (var f = 0; f < 3; f++) {
		intKut = kut.indexOf(strTrigFun[f]);

		if (intKut >= 0) {
			do {
				intZagClose = kut.length;
				for (var i = intKut+4; i < intZagClose; i++) {

					if (operator(kut.charAt(i)) && kut.charAt(i-1)!="e") {
						intZagClose = i;
						strnovoKut = kut.substring(intKut+3, intZagClose);
						break;
					}
				}

				if (intKut>0 && kut.charAt(intKut-1)=="a") {
					intKut = intKut - 1;
					strnovoKut = "a" + strTrigFun[f] + "(" + strnovoKut + strKrozPi;
				}
				else {
					strnovoKut = strTrigFun[f] + "(" + strnovoKut + strPiKroz;
				}

				strnovoKut = conta(strnovoKut);
				strnovoKut = Math.round(strnovoKut * Math.pow(10,14)) / Math.pow(10,14);
				
				kut = kut.replace(kut.substring(intKut, intZagClose), strnovoKut);
				
				intKut = kut.indexOf(strTrigFun[f]);

			}
			while (intKut != -1);
		}
	}

	return kut;
}


function izaoperacaoa(novonumero) {
 with (Math) {

	if (eksp == -1) {
		var duzina = item;
		if (duzina == -1) {duzina = novonumero.length}
		var direito = "";

		if (duzina > 16) {
			var temporaria = round(novonumero*pow(10, 16)) + " ";
			var novoe = temporaria.indexOf("e");
			var esquerda = (temporaria.substring(0,novoe));

			esquerda = round(esquerda*pow(10, 15))/pow(10, 15) + " ";
			direito = (temporaria.substring(novoe+2,temporaria.length-1));
			direito = "e+" + (direito-18);
		}
		else {
			var esquerda = round(novonumero*pow(10, decimais))/pow(10, decimais) + " ";
		}
	}
	else {
		var esquerda = novonumero.substring(0,eksp);
		var direito = novonumero.substring(eksp,novonumero.length);

		esquerda = round(esquerda*pow(10, decimais))/pow(10, decimais) + " ";
	}

	esquerda = esquerda.substring(0,esquerda.length - 1);

	if (esquerda.charAt(0) == ".") {esquerda = "0" + esquerda;}

	if (decimais < 14) {
		if (esquerda.indexOf(".") == -1 && decimais != 0) {esquerda += "."}
		var nula = (item + decimais) - (esquerda.length - 1);
		if (nula > 0 && decimais > 0) {
			for (var n = 0; n < nula; n++) {
				esquerda += "0";
			}
		}
	}

	return (esquerda + direito);
 }
}


function factorial(n) {
	if ((n == 0) || (n == 1)) {
		return 1;
	}
	else {
		var resposta = (n * factorial(n-1));
		return resposta;
	}
}


function masa(atom) {
 with (Math) {
 	var atominfo = false;
	var mm="";
	var mmdn="";
	var mmup="";
	var znak="";
	var izraz="";
		
	for (var i=0; i<atom.length; i++) {
		mm = atom.charAt(i);
		mmup = atom.charAt(i+1);
		bigup = mm.toUpperCase();
		mmdn = atom.charAt(i-1);

		if (mm == "[") {mm = "(";}
		else if (mm == "]") {mm = ")";}
		else if (mm == ",") {mm = ".";}

		if (slovo(mm)) {atominfo = true;}
		if (matoperator(mm)) {atominfo = false; znak="";}
		if (atominfo) {
			if (matoperator(mmup)) {znak=")";}
			if (matoperator(mmdn)) {izraz += "(" + mm + znak;}
			else if (mmdn=="(") {izraz += mm + znak;}
			else if (mmdn=="[") {izraz += mm + znak;}
			else if (slovo(mm)) {izraz += "+" + mm + znak;}
			else if (numeroAtoma(mmdn)) {izraz += mm + znak;}
			else if (numeroAtoma(mm)) {izraz += "*" + mm + znak;}
			else {izraz += mm + znak;}
		}
		else {izraz += mm;}
	}
	resposta = eval(izraz);
	return resposta;
 }
}


function slovo(znak) {
	var slovo="(ABCDEFGHIKLMNOPRSTUVWXYZ";
	for (var i=0; i<slovo.length; i++)
		if (znak == slovo.charAt(i)) {return true;} {return false;}
}

function velikoslovo(znak) {
	var slovo="ABCDEFGHIKLMNOPRSTUVWXYZ";
	for (var i=0; i<slovo.length; i++)
		if (znak == slovo.charAt(i)) {return true;} {return false;}
}

function matoperator(znak) {
	var matoperator="*/+-";
	for (var i=0; i<matoperator.length; i++)
		if (znak == matoperator.charAt(i)) {return true;}
		if (znak == "") {return true;}
		if (znak == null) {return true;}
	return false;
}

function operator(znak) {
	var matoperator="^*/+-";
	if (matoperator.indexOf(znak) >= 0) {return true;} {return false;}
}

function separarresultado(znak) {
	var separarresultado="^*/+";
	for (var i=0; i<separarresultado.length; i++)
		if (znak == separarresultado.charAt(i)) {return true;}
	return false;
}

function numeroAtoma(znak) {
	var atom = "1234567890";
	for (var i=0; i<atom.length; i++)
		if (znak == atom.charAt(i)) {return true;} {return false;}
}

function kemilimat(znak) {
	var atom = "rsu";
	if (atom.indexOf(znak) == -1) {return true;} {return false;}
}


function numformat(x){
	var kut = new Array("auto", "sci", "fix");
	decimais = x;
	var numero = document.mainform.oldresultado.value;

	if (decimais < 13) {
		document.mainform.notes.value += "[" + kut[asf] + decimais + "]";
	}
	else {
		decimais = 14;
		document.mainform.notes.value += "[" + kut[asf] + "]";
	}
	
	if (numero != "0" && numero != "") {
		formatonumero(numero);
	}
	else {
		document.mainform.notes.value += enter;
	}
	document.mainform.tarefa.focus();
}

function formatonumero(numero) {
	var nule = "000000000000000";
	var minus = "";
	if (numero.charAt(0) == "-") {minus = "-";}

	if (asf == 2) {
		if (Math.eval(numero+"*"+minus+"1-1e-"+decimais) < 0) {numero = "0";}
	}
	
	var eplace = numero.indexOf("e");

	if (eplace > 0) {
		var x = numero.substring(0, eplace);
		var y = numero.substring(eplace);
	}
	else if (asf == 1) {
		var x = parseFloat(numero) * 1E50;
		numero = x.toString();
		eplace = numero.indexOf("e");
		x = numero.substring(0, eplace);
		var y = numero.substring(eplace+1);

		y = parseInt(y) - 50;
		y = "e" + y;
	}
	else {
		var x = numero;
		var y = "";
	}
	
	var oplace = numero.indexOf(".");
	numero = parseInt(x);
	if (oplace == -1) {x = "0";} {x = "0" + x.substring(oplace);}
	x = parseFloat(x);

	with (Math) {
		x = round(x * pow(10,decimais)) / pow(10,decimais);
		numero = abs(numero) + x;
	}

	x = numero.toString();

	oplace = x.indexOf(".");

	if (oplace == 0) {
		numero = "0" + x + nule.substring(0, decimais);
	}
	else if (oplace > 0) {
		x = x.concat(nule);
		numero = x.substring(0, oplace);
		numero += x.substring(oplace, oplace+decimais+1);
	}
	else {
		numero = x + "." + nule.substring(0, decimais);
	}

	if (decimais == 14) {numero = parseFloat(numero);}
	numero = minus.concat(numero, y);

	if (sep1000) {

		var resultado = "";
		var strexp = "";

		oplace = numero.indexOf('.');

		eplace = numero.indexOf('e');
		if (eplace == -1) {eplace = numero.length;} {strexp = numero.substring(eplace);}

		var tri = 0;
		for (var i=oplace+1; i<eplace; i++) {
			tri += 1;
			if (tri == 3) {
				resultado += numero.charAt(i) + " ";
				tri = 0;
			}
			else {
				resultado += numero.charAt(i);
			}
		}
		resultado += strexp;
	
		if (oplace > -1) {
			resultado = "." + resultado;
			tri = 0;
			for (var i=oplace-1; i>=0; i--) {
				if (tri == 3) {
					resultado = numero.charAt(i) + " " + resultado;
					tri = 0;
				}
				else {
					resultado = numero.charAt(i) + resultado;
				}
				tri += 1;
			}
		}
		resultado = resultado.replace("- ", "-");
		numero = resultado;
	}
	document.mainform.resultado.value = numero;
	document.mainform.notes.value += " = " + numero + enter;
	
	return numero;
}