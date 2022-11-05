/**
* 使用mvn来编译，然后到target/generated-sources/thrift文件夹中，在idea中右击将其设置源码目录；
* 这样就可以在程序中访问到相关的目录，然后直接进行逻辑开发，推荐这种
*   也可以在cmd中使用thrift的命令 thrift -gen java user.thrift生成java代码，然后将其copy到指定的包下
*
* 这个只是生成模板代码，具体的接口实现还需要自己写，框架会调用我们写的代码
* 对于开发人员来说，使用原生的Thrift框架，仅需要关注以下四个核心内部接口/类：Iface，AsyncIface，Client,AsyncClient
*   Iface: 服务端通过实现UserService.Iface接口，向客户端提供具体的同步业务逻辑
*   AsyncIface: 服务端通过实现UserService.AsyncIface接口，向客户端提供具体的异步业务逻辑
*   Client: 客户端通过UserService.Client实例对象，以同步的方式访问服务器提供的服务方法
*   AsyncClient: 客户端通过UserService.AsyncClient实例对象，以异步的方式访问服务器提供的服务方法
**/

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
