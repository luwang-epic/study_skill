package com.wang.network;

/*
TCP为什么需要三次握手四次挥手 (参考：https://baijiahao.baidu.com/s?id=1695910428789927548&wfr=spider&for=pc)
    第一次握手：第一次握手是客户端发送同步报文到服务端，这个时候客户端是知道自己具备发送数据的能力的，
        但是不知道服务端是否有接收和发送数据的能力；
    第二次握手：当服务端接收到同步报文后，回复确认同步报文，此时服务端是知道客户端具有发送报文的能力，
        并且知道自己具有接收和发送数据的能力，但是并不知道客户端是否有接收数据的能力；
    第三次握手：当客户端收到服务端的确认报文后，知道服务端具备接收和发送数据的能力，
        但是此时服务端并不知道自己具有接收的能力，所以还需要发送一个确认报文，告知服务端自己是具有接收能力的。

    当整个三次握手结束过后，客户端和服务端都知道自己和对方具备发送和接收数据的能力，
    随后整个连接建立就完成了，可以进行后续数据的传输了。

    看到这里，如果大家理解了就会知道很明显，两次握手是不行的，因为服务端并不知道客户端是具备接收数据的能力，
    所以就不能成为面向连接的可靠的传输协议。就像我们上面提到的打电话的例子，也是为了双方能够正常的进行交流，
    只不过我们现实生活中不会那么严谨，并不是每次都这样，但是程序是不一样的。

    第一次挥手客户端发起关闭连接的请求给服务端；
    第二次挥手：服务端收到关闭请求的时候可能这个时候数据还没发送完，所以服务端会先回复一个确认报文，
        表示自己知道客户端想要关闭连接了，但是因为数据还没传输完，所以还需要等待；
    第三次挥手：当数据传输完了，服务端会主动发送一个 FIN 报文，告诉客户端，
        表示数据已经发送完了，服务端这边准备关闭连接了。
    第四次挥手：当客户端收到服务端的 FIN 报文过后，会回复一个 ACK 报文，
        告诉服务端自己知道了，再等待一会就关闭连接。

    为什么握手要三次，挥手却要四次呢？那是因为握手的时候并没有数据传输，所以服务端的 SYN 和 ACK 报文可以一起发送，
    但是挥手的时候有数据在传输，所以 ACK 和 FIN 报文不能同时发送，需要分两步，所以会比握手多一步。

    为什么客户端在第四次挥手后还会等待 2MSL？等待 2MSL 是因为保证服务端接收到了 ACK 报文，因为网络是复杂了，
    很有可能 ACK 报文丢失了，如果服务端没接收到 ACK 报文的话，会重新发送 FIN 报文，
    只有当客户端等待了 2MSL 都没有收到重发的 FIN 报文时就表示服务端是正常收到了 ACK 报文，
    那么这个时候客户端就可以关闭了。

1xx:表示目前是协议的中间状态,还需要后续请求 2xx:表示请求成功
3xx:表示重定向状态,需要重新请求 4xx:表示请求报文错误 5xx:服务器端错误
常用状态码: 101 切换请求协议，从 HTTP 切换到 WebSocket，200 请求成功，有响应体，服务器成功返回网页
    301 永久重定向：会缓存，302 临时重定向：不会缓存，304 协商缓存命中，403 服务器禁止访问
    404 资源未找到，请求的网页不存在，400 请求错误，500 服务器端错误，503 服务器繁忙


XSS攻击的全称是跨站脚本攻击，是Web应用程序中最常见到的攻击手段。跨站脚本攻击指的是攻击者在网页中嵌入恶意脚本程序，
当用户打开该网页时，脚本程序便开始在客户端的浏览器上运行，便于获取cookie,用户名密码。分为：存储型、反射型和文档型
三种XSS攻击的原理，我们能发现一个共同点: 都是让恶意脚本直接能在浏览器中执行。那么要防范它，就是要避免这些脚本代码的执行。
为了完成这一点，常见的防范措施有：
    对输入脚本进行过滤或转码
        本着千万不要相信任何用户输入的信念，无论是在前端和服务端，都要对用户的输入进行转码或者过滤
    利用CSP
        CSP，即浏览器中的内容安全策略，它的核心思想是 服务器决定浏览器加载哪些资源，主要包括：
            (1)限制加载其他域下的资源文件；1
            (2)禁止向其他域提交数据；
            (3)提供上报机制，能帮助我们及时发现 XSS 攻击。
            (4)禁止执行内联脚本和未授权的脚本；
    利用HttpOnly
        HttpOnly 是服务器通过 HTTP 响应头来设置的，设置 Cookie 的 HttpOnly 属性后，
        JavaScript 便无法读取 Cookie 的值。这样也能很好的防范 XSS 攻击

 */
public class NetWorkDemo {
    public static void main(String[] args) {

    }
}
