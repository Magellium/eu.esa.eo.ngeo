<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!-- <div id="contentBody">	 -->
	<h2>Error</h2>
	
	<p>An unrecoverable error has occurred. <a href="">Return to Home page.</a> </p>
	
	<span id="techInfoLink">Click <a href="javascript:showTechInfo()">here</a> to see further technical information about the error. </span>
	
	<div id="techInfo" style="display:none;">
		<p><span style="font-weight:bold;">Technical Information</span> <a href="javascript:hideTechInfo()">(hide)</a> </p>
		<p><c:out value="${exception.message}"/></p> 
		<c:forEach items="${exception.stackTraceAsStringArray}" var="line">
			<p><c:out value="${line}"/></p>
		</c:forEach>
	</div>

<!-- </div> -->

<script src="resources/js/techInfoVisibility.js"></script>