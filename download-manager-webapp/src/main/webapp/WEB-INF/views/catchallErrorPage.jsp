<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

	<h2>Error</h2>
	
	<p>An error has occurred. Please <a href=""> return to the Home page.</a> </p>
	
	<%--
		The following was a copy/paste/edit job on nonRecoverableException.jsp.
		Note that the code to render stack trace elements doesn't assume that  
		"exception" has a type more specific than Throwable. 
	 --%>
	
	<span id="techInfoLink">Click <a href="javascript:showTechInfo()">here</a> to see further technical information about the error. </span>
	
	<div id="techInfo" style="display:none;">
		<p><span style="font-weight:bold;">Technical Information</span> <a href="javascript:hideTechInfo()">(hide)</a> </p>
		<c:choose>
			<c:when test="${! empty exception.message}">
				<p>Error message: <c:out value="${exception.message}"/>
			</c:when>
			<c:when test="${! empty exception.localizedMessage}">
				<p>Error message: <c:out value="${exception.localizedMessage}"/>
			</c:when>
		</c:choose>
		<p>Exception class: <c:out value="${exception['class'].name}"/></p> 
		<c:forEach items="${exception.stackTrace}" var="line">
			<p><c:out value="${line}"/></p>
		</c:forEach>
	</div>


<script src="resources/js/techInfoVisibility.js"></script>