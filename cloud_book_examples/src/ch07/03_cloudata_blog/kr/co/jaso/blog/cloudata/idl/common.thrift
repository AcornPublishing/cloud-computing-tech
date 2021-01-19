namespace cpp common 
namespace java kr.co.jaso.blog.cloudata.generated
namespace php common
 
struct ServiceStatus { 
1: string hostName, 
2: i32 port,
3: string status
} 

service ApplicationService { 
  ServiceStatus getServiceStatus()  
}