<%@ page import="com.travomate.UserProfile" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'userProfile.label', default: 'UserProfile')}" />
		<title><g:message code="default.edit.label" args="[entityName]" /></title>
		<style>
		#myImg {
			border-radius: 5px;
			cursor: pointer;
			transition: 0.3s;
		}

		#myImg:hover {opacity: 0.7;}

		/* The Modal (background) */
		.modal {
			display: none; /* Hidden by default */
			position: fixed; /* Stay in place */
			z-index: 20; /* Sit on top */
			padding-top: 100px; /* Location of the box */
			left: 0;
			top: 0;
			width: 100%; /* Full width */
			height: 100%; /* Full height */
			overflow: auto; /* Enable scroll if needed */
			background-color: rgb(0,0,0); /* Fallback color */
			background-color: rgba(0,0,0,0.9); /* Black w/ opacity */
		}

		/* Modal Content (image) */
		.modal-content {
			margin: auto;
			display: block;
			width: 80%;
			max-width: 700px;
		}

		/* Caption of Modal Image */
		#caption {
			margin: auto;
			display: block;
			width: 80%;
			max-width: 700px;
			text-align: center;
			color: #ccc;
			padding: 10px 0;
			height: 150px;
		}

		/* Add Animation */
		.modal-content, #caption {
			-webkit-animation-name: zoom;
			-webkit-animation-duration: 0.6s;
			animation-name: zoom;
			animation-duration: 0.6s;
		}

		@-webkit-keyframes zoom {
			from {-webkit-transform:scale(0)}
			to {-webkit-transform:scale(1)}
		}

		@keyframes zoom {
			from {transform:scale(0)}
			to {transform:scale(1)}
		}

		/* The Close Button */
		.close {
			position: absolute;
			top: 15px;
			right: 35px;
			color: #f1f1f1;
			font-size: 40px;
			font-weight: bold;
			transition: 0.3s;
		}

		.close:hover,
		.close:focus {
			color: #bbb;
			text-decoration: none;
			cursor: pointer;
		}

		/* 100% Image Width on Smaller Screens */
		@media only screen and (max-width: 700px){
			.modal-content {
				width: 100%;
			}
		}
		</style>
	</head>
	<body>
		<a href="#edit-userProfile" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				%{--<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>--}%
				%{--<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>--}%
			</ul>
		</div>
		<div id="edit-userProfile" class="content scaffold-edit" role="main">
			<h1><g:message code="default.edit.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${userProfileInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${userProfileInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form method="post" >
				<g:hiddenField name="id" value="${userProfileInstance?.id}" />
				<g:hiddenField name="version" value="${userProfileInstance?.version}" />
				<fieldset class="form">
					%{--<g:render template="form"/>--}%
					<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'name', 'error')} ">
						<label for="name">
							<g:message code="userProfile.name.label" default="Name" />

						</label>
						<g:textField name="name" value="${userProfileInstance?.name}"/>
					</div>

					<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'nationality', 'error')} ">
						<label for="nationality">
							<g:message code="userProfile.nationality.label" default="Nationality" />

						</label>
						<g:textField name="nationality" value="${userProfileInstance?.nationality}"/>
					</div>


					<g:if test="${userProfileInstance?.idProofLoc}">
						<div class="fieldcontain">
							<span id="idProofLoc-label" class="property-label"><g:message code="userProfile.idProofLoc.label" default="Id Proof" /></span>
							<span class="property-value" aria-labelledby="idProofLoc-label">
								<img id="myImg" src="${createLink(controller: 'userProfile', action:'renderImage', params: [id:userProfileInstance.id,imageType:'idProof'])}" width="50" height="40">

								<div id="myModal" class="modal">
									<span class="close">X</span>
									<img class="modal-content" id="img01">
									<div id="caption"></div>
								</div>

								<script>
									// Get the modal
									var modal = document.getElementById('myModal');

									// Get the image and insert it inside the modal - use its "alt" text as a caption
									var img = document.getElementById('myImg');
									var modalImg = document.getElementById("img01");
									var captionText = document.getElementById("caption");
									img.onclick = function(){
										modal.style.display = "block";
										modalImg.src = this.src;
										captionText.innerHTML = this.alt;
									}

									// Get the <span> element that closes the modal
									var span = document.getElementsByClassName("close")[0];

									// When the user clicks on <span> (x), close the modal
									span.onclick = function() {
										modal.style.display = "none";
									}
								</script>
							</span>

						</div>
					</g:if>

					<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'verificationStatus', 'error')} ">
						<label for="verificationStatus">
							<g:message code="userProfile.verificationStatus.label" default="Verification Status" />

						</label>
						<g:select name="verificationStatus" from="${com.travomate.Constants.VerificationStatus.values()}" value="${userProfileInstance?.verificationStatus}" />
					</div>


				</fieldset>
				<fieldset class="buttons">
					<g:actionSubmit class="save" action="update" value="${message(code: 'default.button.update.label', default: 'Update')}" />
					%{--<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" formnovalidate="" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />--}%
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
