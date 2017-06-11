<%@ page import="com.travomate.UserProfile" %>



<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'name', 'error')} ">
	<label for="name">
		<g:message code="userProfile.name.label" default="Name" />
		
	</label>
	<g:textField name="name" value="${userProfileInstance?.name}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'occupation', 'error')} ">
	<label for="occupation">
		<g:message code="userProfile.occupation.label" default="Occupation" />
		
	</label>
	<g:textField name="occupation" value="${userProfileInstance?.occupation}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'nationality', 'error')} ">
	<label for="nationality">
		<g:message code="userProfile.nationality.label" default="Nationality" />
		
	</label>
	<g:textField name="nationality" value="${userProfileInstance?.nationality}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'languages', 'error')} ">
	<label for="languages">
		<g:message code="userProfile.languages.label" default="Languages" />
		
	</label>
	<g:textField name="languages" value="${userProfileInstance?.languages}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'verificationStatus', 'error')} ">
	<label for="verificationStatus">
		<g:message code="userProfile.verificationStatus.label" default="Verification Status" />
		
	</label>
	<g:select name="verificationStatus" from="${com.travomate.Constants.VerificationStatus.values()}" value="${userProfileInstance?.verificationStatus}" />
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'isActive', 'error')} ">
	<label for="isActive">
		<g:message code="userProfile.isActive.label" default="Is Active" />
		
	</label>
	<g:checkBox name="isActive" value="${userProfileInstance?.isActive}" />
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'idProofLoc', 'error')} ">
	<label for="idProofLoc">
		<g:message code="userProfile.idProofLoc.label" default="Id Proof Loc" />
		
	</label>
	<g:textField name="idProofLoc" value="${userProfileInstance?.idProofLoc}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'userIntro', 'error')} ">
	<label for="userIntro">
		<g:message code="userProfile.userIntro.label" default="User Intro" />
		
	</label>
	<g:textField name="userIntro" value="${userProfileInstance?.userIntro}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'profileImageLoc', 'error')} ">
	<label for="profileImageLoc">
		<g:message code="userProfile.profileImageLoc.label" default="Profile Image Loc" />
		
	</label>
	<g:textField name="profileImageLoc" value="${userProfileInstance?.profileImageLoc}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'coverImageLoc', 'error')} ">
	<label for="coverImageLoc">
		<g:message code="userProfile.coverImageLoc.label" default="Cover Image Loc" />
		
	</label>
	<g:textField name="coverImageLoc" value="${userProfileInstance?.coverImageLoc}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'city', 'error')} ">
	<label for="city">
		<g:message code="userProfile.city.label" default="City" />
		
	</label>
	<g:textField name="city" value="${userProfileInstance?.city}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'state', 'error')} ">
	<label for="state">
		<g:message code="userProfile.state.label" default="State" />
		
	</label>
	<g:textField name="state" value="${userProfileInstance?.state}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'country', 'error')} ">
	<label for="country">
		<g:message code="userProfile.country.label" default="Country" />
		
	</label>
	<g:textField name="country" value="${userProfileInstance?.country}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: userProfileInstance, field: 'user', 'error')} required">
	<label for="user">
		<g:message code="userProfile.user.label" default="User" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="user" name="user.id" from="${com.travomate.User.list()}" optionKey="id" required="" value="${userProfileInstance?.user?.id}" class="many-to-one"/>
</div>

