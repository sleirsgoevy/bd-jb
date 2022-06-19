#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <stddef.h>
#include <unistd.h>

typedef int dlsym_t(int, const char*, void*);

char data[64];

int main(dlsym_t* dlsym)
{
    typeof(socket)* f_socket;
    dlsym(0x2001, "socket", &f_socket);
    typeof(bind)* f_bind;
    dlsym(0x2001, "bind", &f_bind);
    typeof(listen)* f_listen;
    dlsym(0x2001, "listen", &f_listen);
    typeof(accept)* f_accept;
    dlsym(0x2001, "accept", &f_accept);
    typeof(read)* f_read;
    dlsym(0x2001, "read", &f_read);
    typeof(write)* f_write;
    dlsym(0x2001, "write", &f_write);
    typeof(close)* f_close;
    dlsym(0x2001, "close", &f_close);
    int sock = f_socket(AF_INET, SOCK_STREAM, 0);
    struct sockaddr_in addr = {
        .sin_family = AF_INET,
        .sin_addr = {.s_addr = 0},
        .sin_port = 0xd204,
    };
    f_bind(sock, (void*)&addr, sizeof(addr));
    f_listen(sock, 1);
    char* dst = data;
    const char* src = "Hello, BD-JB!\n";
    while(*src)
        *dst++ = *src++;
    for(;;)
    {
        int sock2 = f_accept(sock, NULL, NULL);
        f_write(sock2, data, dst-data);
        f_close(sock2);
    }
    return 0;
}
