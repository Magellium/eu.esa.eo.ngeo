<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page session="false"%>

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix='fn' uri='http://java.sun.com/jsp/jstl/functions' %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
	<head>
		<meta charset="utf-8" />
		<c:set var="req" value="${pageContext.request}" />
		<c:set var="baseURL" value="${fn:replace(req.requestURL, req.requestURI, req.contextPath)}" />
		<base href="<c:url value="${baseURL}/" />" />
		<link rel="stylesheet" type="text/css" href="resources/css/jquery.dataTables.css"/>
		<link rel="stylesheet" type="text/css" href="resources/css/ui-lightness/jquery-ui-1.10.2.custom.min.css"/>	
		<link rel="stylesheet" type="text/css" href="resources/css/styles.css" />
		<script src="resources/js/jquery-1.9.1.min.js"></script>
		<title>
			<spring:eval expression="@propertyConfigurer.getProperty('DM_TITLE')" /><tiles:insertAttribute name="page-sub-title"/>			
		</title>
	</head>
	<body>
	    <tiles:insertAttribute name="top-bar"/>
	    <div id="main">
		    <div id="content">
			    <tiles:insertAttribute name="content"/>
			</div>
		</div>
		<div class="clearboth"></div>
		<script src="resources/js/jquery-ui-1.10.2.custom.min.js"></script>
		<script src="resources/js/jquery.dataTables.js"></script>
		<script src="resources/js/downloadMonitor.js"></script>
	</body>
</html>