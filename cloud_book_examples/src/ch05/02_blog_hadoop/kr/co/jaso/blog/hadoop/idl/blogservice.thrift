include "common.thrift" 
namespace cpp blog
namespace java kr.co.jaso.blog.hadoop.generated
namespace php blog

struct BlogArticle { 
    1: string userId,
    2: i64 articleId, 
    3: string title,         
    4: binary contents
} 

exception BlogException { 
1: string hostName, 
2: string message 
} 
service BlogService extends common.ApplicationService { 
    oneway void saveBlog(1:BlogArticle article)
    BlogArticle getBlog(1:string userId, 2:i64 articleId) throws (1:BlogException be), 
    list<BlogArticle> searchBlogByUserId(1:string userId) throws (1:BlogException be)
}
