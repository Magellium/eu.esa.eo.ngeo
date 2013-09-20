<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>
<h1><spring:message code="first_startup_config.heading" /></h1>
<form:form commandName="FIRSTSTARTUPCONFIGSETTINGS" name="settingsForm" id="settingsForm" >
	<p style="text-align:center;"><form:errors path="*" cssStyle="color:red;"/></p>
	<div style="width:550px;margin-left:auto;margin-right:auto;">
		<table>
			<tr><td colspan="2" style="text-align:center;"><spring:message code="first_startup_config.description" /><br /><br /></td></tr>
			<tr><td>SSO username : </td><td><form:input path="ssoUsername" /></td></tr>
			<tr><td>SSO password : </td><td><form:password path="ssoPassword" showPassword="true" /></td></tr>
			<tr><td>Name of DM instance : </td><td><form:input path="dmFriendlyName" size="40" /></td></tr>
			<tr><td>Download directory : </td><td><form:input path="baseDownloadFolder" size="40" /></td></tr>
			<tr><td>DM web interface port no. : </td><td><form:input path="webInterfacePortNo" size="5"/></td></tr>
			<tr><td colspan="2" style="text-align:center;"><br /><input type="submit" value="Save Configuration and Register Download Manager" /></td></tr>
        </table>	
	</div>
	<p><b>Version:</b> <spring:message code="version"/></p>
</form:form>
