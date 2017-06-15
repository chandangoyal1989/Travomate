<!DOCTYPE html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="Travomate"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">
		<g:layoutHead/>
		<r:layoutResources />
	</head>
	<body>
		<div id="grailsLogo" role="banner">
            <img id="leftDiv" style=" display:inline; margin-right: -20px" src="${resource(dir: 'images', file: 'travomate.png')}" alt="Travomate"/>
            <div id="rightDiv" style=" display:inline;">
                <img id="coverImg"  src="${resource(dir: 'images', file: 'cover.png')}" alt="cover"/>
            </div>
            <div style="float:right">
                <sec:ifLoggedIn>
                    <g:link controller="logout"><b style="color: #2e3741">Logout</b></g:link>
                </sec:ifLoggedIn>
            </div>
        </div>
		<g:layoutBody/>
		<div class="footer" role="contentinfo"></div>
		<g:javascript library="application"/>
		<r:layoutResources />
	</body>
</html>
