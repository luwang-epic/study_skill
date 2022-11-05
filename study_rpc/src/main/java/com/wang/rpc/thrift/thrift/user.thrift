/**
* thrift文件，可以通过命令行的方式生成代码，也可以让maven通过插件编译前自动生成
*/
namespace java com.wang.rpc.thrift.api

struct User {
    1:i32 id
    2:string name
    3:i32 age = 0
}

service UserService {
    User getById(1:i32 id)
    bool isExist(1:string name)
}
