<%@ page
  contentType="text/html; charset=UTF-8"
  import="javax.servlet.*"
  import="javax.servlet.http.*"
  import="java.io.*"
  import="java.util.*"
  import="kr.co.jaso.blog.thrift.jetty.*"
  import="kr.co.jaso.blog.thrift.jetty.generated.*"
%>

<%
List<BlogArticle> blogArticles = BlogServer.blogServer.getBlogService().searchBlogByUserId("user01");
%>
<html> 
<head> 
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
</head>
<body>
<table border="1">
<%
for(BlogArticle eachArticle: blogArticles) {
%>
<tr><td><%=eachArticle.getTitle()%></td></tr>
<%
}
%>
</table>
</body>
</html>  