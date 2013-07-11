<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ page session="false" %>
<h1>
	Advanced configuration  
</h1>
<form:form commandName="ADVANCEDCONFIGSETTINGS" name="settingsForm" id="settingsForm" >
	<p style="text-align:center;"><form:errors path="*" cssStyle="color:red;"/></p>
	<div style="width:550px;margin-left:auto;margin-right:auto;">
		<table>
			<tr><td>SSO username : </td><td><form:input path="ssoUsername" /></td></tr>
			<tr><td>SSO password : </td><td><form:password path="ssoPassword" showPassword="true"/></td></tr>
			<tr><td>Name of DM instance : </td><td><form:input path="dmFriendlyName" size="40"/></td></tr>
			<tr><td>Download directory : </td><td><form:input path="baseDownloadFolder" size="40" /></td></tr>
			<tr><td>DM web interface port no. : </td><td><form:input path="webInterfacePortNo"  size="5"/></td></tr>
			<tr><td>Number of parallel product download threads : </td><td><form:input path="noOfParallelProductDownloadThreads" /></td></tr> <%-- TODO: Maybe use a control with inc/dec buttons --%>
			<tr><td>Web proxy URL : </td><td><form:input path="webProxyUrl" /></td></tr>
			<tr><td>Web proxy port : </td><td><form:input path="webProxyPort" /></td></tr>
			<tr><td>Web proxy username : </td><td><form:input path="webProxyUsername" /></td></tr>
			<tr><td>Web proxy password : </td><td><form:input path="webProxyPassword" showPassword="true" /></td></tr>
			<tr><td>Product download complete command : </td><td><form:input path="productDownloadCompleteCommand" /></td></tr>
			<tr><td>Web interface username : </td><td><form:input path="webInterfaceUsername" /></td></tr>
			<tr><td>Web interface password : </td><td><form:input path="webInterfacePassword" showPassword="true"/></td></tr>
			<tr><td>Web interface remote access enabled : </td><td><form:checkbox path="webInterfaceRemoteAccessEnabled" /></td></tr>
			<tr>
				<td colspan="2" style="text-align:center;">
					<br />
					<input type="button" value="<< Back" class="headerButton" onclick="document.location.href=''"/>&nbsp;
					<input type="submit" value="Save Configuration" />
				</td>
			</tr>
		</table>	
	</div>
	<p><b>Version:</b> <spring:message code="version"/></p>
</form:form>
