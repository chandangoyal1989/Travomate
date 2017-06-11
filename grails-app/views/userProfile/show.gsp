
<%@ page import="com.travomate.UserProfile" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'userProfile.label', default: 'UserProfile')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
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
		<a href="#show-userProfile" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-userProfile" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list userProfile">
			
				<g:if test="${userProfileInstance?.name}">
				<li class="fieldcontain">
					<span id="name-label" class="property-label"><g:message code="userProfile.name.label" default="Name" /></span>
					
						<span class="property-value" aria-labelledby="name-label"><g:fieldValue bean="${userProfileInstance}" field="name"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.occupation}">
				<li class="fieldcontain">
					<span id="occupation-label" class="property-label"><g:message code="userProfile.occupation.label" default="Occupation" /></span>
					
						<span class="property-value" aria-labelledby="occupation-label"><g:fieldValue bean="${userProfileInstance}" field="occupation"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.nationality}">
				<li class="fieldcontain">
					<span id="nationality-label" class="property-label"><g:message code="userProfile.nationality.label" default="Nationality" /></span>
					
						<span class="property-value" aria-labelledby="nationality-label"><g:fieldValue bean="${userProfileInstance}" field="nationality"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.languages}">
				<li class="fieldcontain">
					<span id="languages-label" class="property-label"><g:message code="userProfile.languages.label" default="Languages" /></span>
					
						<span class="property-value" aria-labelledby="languages-label"><g:fieldValue bean="${userProfileInstance}" field="languages"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.verificationStatus}">
				<li class="fieldcontain">
					<span id="verificationStatus-label" class="property-label"><g:message code="userProfile.verificationStatus.label" default="Verification Status" /></span>
					
						<span class="property-value" aria-labelledby="verificationStatus-label"><g:fieldValue bean="${userProfileInstance}" field="verificationStatus"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.isActive}">
				<li class="fieldcontain">
					<span id="isActive-label" class="property-label"><g:message code="userProfile.isActive.label" default="Is Active" /></span>
					
						<span class="property-value" aria-labelledby="isActive-label"><g:formatBoolean boolean="${userProfileInstance?.isActive}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.idProofLoc}">
				<li class="fieldcontain">
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

				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.userIntro}">
				<li class="fieldcontain">
					<span id="userIntro-label" class="property-label"><g:message code="userProfile.userIntro.label" default="User Intro" /></span>
					
						<span class="property-value" aria-labelledby="userIntro-label"><g:fieldValue bean="${userProfileInstance}" field="userIntro"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.profileImageLoc}">
				<li class="fieldcontain">
					<span id="profileImageLoc-label" class="property-label"><g:message code="userProfile.profileImageLoc.label" default="Profile Image" /></span>
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.coverImageLoc}">
				<li class="fieldcontain">
					<span id="coverImageLoc-label" class="property-label"><g:message code="userProfile.coverImageLoc.label" default="Cover Image Loc" /></span>
					
						<span class="property-value" aria-labelledby="coverImageLoc-label"><g:fieldValue bean="${userProfileInstance}" field="coverImageLoc"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.city}">
				<li class="fieldcontain">
					<span id="city-label" class="property-label"><g:message code="userProfile.city.label" default="City" /></span>
					
						<span class="property-value" aria-labelledby="city-label"><g:fieldValue bean="${userProfileInstance}" field="city"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.state}">
				<li class="fieldcontain">
					<span id="state-label" class="property-label"><g:message code="userProfile.state.label" default="State" /></span>
					
						<span class="property-value" aria-labelledby="state-label"><g:fieldValue bean="${userProfileInstance}" field="state"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.country}">
				<li class="fieldcontain">
					<span id="country-label" class="property-label"><g:message code="userProfile.country.label" default="Country" /></span>
					
						<span class="property-value" aria-labelledby="country-label"><g:fieldValue bean="${userProfileInstance}" field="country"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${userProfileInstance?.user}">
				<li class="fieldcontain">
					<span id="user-label" class="property-label"><g:message code="userProfile.user.label" default="User" /></span>
					
						<span class="property-value" aria-labelledby="user-label"><g:link controller="user" action="show" id="${userProfileInstance?.user?.id}">${userProfileInstance?.user?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form>
				<fieldset class="buttons">
					<g:hiddenField name="id" value="${userProfileInstance?.id}" />
					<g:link class="edit" action="edit" id="${userProfileInstance?.id}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
