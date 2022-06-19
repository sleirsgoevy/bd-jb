use64

global _start
extern main

section .text.startup

_start:
jmp start2
db 'P', 'L', 'D'
dq _sdata-_start
start2
jmp main

section .data

_sdata:
db 1
