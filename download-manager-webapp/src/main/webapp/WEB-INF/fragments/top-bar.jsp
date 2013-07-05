<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<div id="top-bar">
	<div id="container"><h1><spring:eval expression="@propertyConfigurer.getProperty('DM_TITLE')" /></h1></div>
</div>